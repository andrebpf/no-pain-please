import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Auth, GoogleAuthProvider, User, authState, signInWithEmailAndPassword, signInWithPopup, signOut } from '@angular/fire/auth';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { Exercise, TrainingPlan } from './training-plans';
import { ExerciseProgress, ExerciseWeightPoint, TrainingDay, TrainingDaysService } from './training-days.service';
import { environment } from '../environments/environment';

// Calendar keys use the user's training timezone, never UTC day boundaries.
export function trainingDate(date = new Date()): string {
  return new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Sao_Paulo', year: 'numeric', month: '2-digit', day: '2-digit' }).format(date);
}

interface WeightChartPoint extends ExerciseWeightPoint {
  x: number;
  y: number;
}

interface WeightAxisTick {
  value: number;
  y: number;
  yPercent: number;
}

interface WeightDateLabel {
  date: string;
  x: number;
  xPercent: number;
  anchor: 'start' | 'middle' | 'end';
}

interface WeightChart {
  points: WeightChartPoint[];
  yTicks: WeightAxisTick[];
  dateLabels: WeightDateLabel[];
}

@Component({
  selector: 'app-root',
  imports: [DatePipe, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  private readonly auth = inject(Auth);
  private readonly api = inject(TrainingDaysService);
  private readonly destroyRef = inject(DestroyRef);
  private generation = 0;
  readonly user = signal<User | null>(null);
  readonly localDemoLogin = !!environment.firebaseEmulator;
  readonly authReady = signal(false);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly unsaved = signal(false);
  readonly saved = signal(false);
  readonly plans = signal<TrainingPlan[]>([]);
  readonly days = signal<TrainingDay[]>([]);
  readonly today = signal(trainingDate());
  readonly selectedDate = signal(this.today());
  readonly selectedPlan = signal('');
  readonly draft = signal<ExerciseProgress[]>([]);
  readonly attended = signal(false);
  readonly view = signal<'training' | 'history'>('training');
  readonly expandedWeightHistory = signal<string | null>(null);
  readonly weightHistory = signal<ExerciseWeightPoint[]>([]);
  readonly loadingWeightHistory = signal(false);
  readonly weightHistoryError = signal('');
  readonly plan = computed(() => this.plans().find(plan => plan.id === this.selectedPlan()));
  readonly completed = computed(() => this.draft().filter(exercise => exercise.completed).length);
  readonly progress = computed(() => this.plan()?.exercises.length ? Math.round(this.completed() / this.plan()!.exercises.length * 100) : 0);
  readonly history = computed(() => this.days().filter(day => day.attended).sort((a, b) => b.date.localeCompare(a.date)));
  readonly week = computed(() => {
    const date = new Date(`${this.today()}T12:00:00Z`);
    date.setUTCDate(date.getUTCDate() - (date.getUTCDay() + 6) % 7);
    return Array.from({ length: 7 }, (_, index) => {
      const day = new Date(date); day.setUTCDate(day.getUTCDate() + index);
      const key = trainingDate(day);
      return { date: key, number: day.getUTCDate(), label: ['SEG', 'TER', 'QUA', 'QUI', 'SEX', 'SÁB', 'DOM'][index], attended: this.days().some(item => item.date === key && item.attended) };
    });
  });
  readonly weekCount = computed(() => this.week().filter(day => day.attended).length);
  readonly firstName = computed(() => this.user()?.displayName?.split(' ')[0] || 'Atleta');
  readonly weightChart = computed<WeightChart>(() => {
    const points = this.weightHistory();
    if (!points.length) return { points: [], yTicks: [], dateLabels: [] };
    const weights = points.map(point => point.weightKg);
    const minimum = Math.min(...weights);
    const maximum = Math.max(...weights);
    const margin = Math.max((maximum - minimum) * .15, 2.5);
    const lowerBound = minimum - margin;
    const range = Math.max(maximum + margin - lowerBound, 1);
    const left = 44;
    const right = 304;
    const bottom = 88;
    const top = 14;
    const y = (weight: number) => bottom - (weight - lowerBound) / range * (bottom - top);
    const chartPoints = points.map((point, index) => ({
      ...point,
      x: points.length === 1 ? (left + right) / 2 : left + index * (right - left) / (points.length - 1),
      y: y(point.weightKg)
    }));
    const tickValues = minimum === maximum ? [minimum] : [minimum, (minimum + maximum) / 2, maximum];
    const labelIndexes = [...new Set([0, Math.round((points.length - 1) / 2), points.length - 1])];
    return {
      points: chartPoints,
      yTicks: tickValues.map(value => ({ value, y: y(value), yPercent: y(value) / 1.2 })),
      dateLabels: labelIndexes.map(index => ({
        date: points[index].date,
        x: chartPoints[index].x,
        xPercent: chartPoints[index].x / 3.2,
        anchor: index === 0 ? 'start' : index === points.length - 1 ? 'end' : 'middle'
      }))
    };
  });
  readonly weightChartPolyline = computed(() => this.weightChart().points.map(point => `${point.x},${point.y}`).join(' '));
  readonly weightGridPath = computed(() => this.weightChart().yTicks.map(tick => `M44 ${tick.y}H304`).join(' '));
  readonly latestWeight = computed(() => this.weightHistory().at(-1)?.weightKg ?? null);
  readonly weightChange = computed(() => {
    const points = this.weightHistory();
    return points.length < 2 ? null : points.at(-1)!.weightKg - points[0].weightKg;
  });
  private readonly weightFormatter = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 1 });
  private readonly historyDateFormatter = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short' });
  private readonly chartDateFormatter = new Intl.DateTimeFormat('pt-BR', { day: 'numeric', month: 'short' });

  constructor() {
    authState(this.auth).pipe(takeUntilDestroyed()).subscribe(user => {
      this.generation++; this.user.set(user); this.authReady.set(true);
      this.days.set([]); this.plans.set([]); this.draft.set([]); this.error.set(''); this.unsaved.set(false); this.saving.set(false);
      this.expandedWeightHistory.set(null); this.weightHistory.set([]); this.loadingWeightHistory.set(false); this.weightHistoryError.set('');
      if (user) this.load();
    });
    const timer = setInterval(() => this.checkDate(), 15000);
    const onFocus = () => this.checkDate();
    window.addEventListener('focus', onFocus);
    const beforeUnload = (event: BeforeUnloadEvent) => {
      if (this.unsaved() || this.saving()) { event.preventDefault(); event.returnValue = ''; }
    };
    window.addEventListener('beforeunload', beforeUnload);
    this.destroyRef.onDestroy(() => { clearInterval(timer); window.removeEventListener('focus', onFocus); window.removeEventListener('beforeunload', beforeUnload); });
  }

  checkDate() {
    const next = trainingDate();
    if (next === this.today()) return;
    const previous = this.today(); this.today.set(next);
    if (this.selectedDate() === previous && !this.unsaved() && !this.saving()) this.selectDate(next);
  }

  load() {
    const generation = this.generation;
    this.loading.set(true); this.error.set('');
    forkJoin({ plans: this.api.plans(), days: this.api.list() }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: ({ plans, days }) => {
        if (generation !== this.generation) return;
        this.plans.set(plans); this.days.set(days); this.loading.set(false); this.restoreDay();
      },
      error: () => { if (generation === this.generation) { this.loading.set(false); this.error.set('Não foi possível carregar os treinos. Confira se a API e os emuladores estão ligados.'); } }
    });
  }

  selectDate(date: string) {
    if (this.saving() || this.unsaved() || !/^\d{4}-\d{2}-\d{2}$/.test(date) || date > this.today()) return;
    this.selectedDate.set(date); this.restoreDay(); this.view.set('training');
  }

  private restoreDay() {
    const day = this.days().find(day => day.date === this.selectedDate());
    const last = this.history().find(day => day.date < this.selectedDate());
    const index = last ? this.plans().findIndex(plan => plan.id === last.plan) : -1;
    this.selectedPlan.set(day?.plan || this.plans()[(index + 1) % this.plans().length]?.id || '');
    this.attended.set(day?.attended || false); this.draft.set(day?.exercises.map(item => ({ ...item })) || []);
    this.saved.set(false); this.error.set('');
  }

  choosePlan(id: string) {
    if (this.saving() || this.unsaved() || this.attended() || this.draft().length) return;
    this.selectedPlan.set(id); this.saved.set(false);
  }

  entry(id: string) { return this.draft().find(item => item.id === id); }
  previousWeight(id: string): number | null {
    for (const day of [...this.days()].sort((a, b) => b.date.localeCompare(a.date))) {
      if (day.date >= this.selectedDate()) continue;
      const exercise = day.exercises.find(item => item.id === id && item.weightKg !== null);
      if (exercise) return exercise.weightKg;
    }
    return null;
  }

  toggleWeightHistory(exerciseId: string) {
    if (this.expandedWeightHistory() === exerciseId) {
      this.expandedWeightHistory.set(null);
      return;
    }
    this.expandedWeightHistory.set(exerciseId);
    this.weightHistory.set([]); this.loadingWeightHistory.set(true); this.weightHistoryError.set('');
    this.api.weightHistory(exerciseId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: history => {
        if (this.expandedWeightHistory() !== exerciseId) return;
        this.weightHistory.set(history); this.loadingWeightHistory.set(false);
      },
      error: () => {
        if (this.expandedWeightHistory() !== exerciseId) return;
        this.loadingWeightHistory.set(false);
        this.weightHistoryError.set('Não foi possível carregar a evolução agora. Tente novamente.');
      }
    });
  }

  formatWeight(weight: number) { return `${this.weightFormatter.format(weight)} kg`; }
  formatHistoryDate(date: string) { return this.historyDateFormatter.format(new Date(`${date}T12:00:00Z`)); }
  formatChartDate(date: string) { return this.chartDateFormatter.format(new Date(`${date}T12:00:00Z`)); }
  weightAxisTick(index: number): WeightAxisTick {
    const ticks = this.weightChart().yTicks;
    return ticks[index] ?? ticks[0];
  }

  changeWeight(exercise: Exercise, input: HTMLInputElement) {
    if (!input.validity.valid) { input.reportValidity(); return; }
    const weightKg = input.value === '' ? null : Number(input.value);
    if (weightKg !== null && (!Number.isFinite(weightKg) || weightKg < 0 || weightKg > 2000)) return;
    this.update(exercise.id, { weightKg });
  }

  update(id: string, change: Partial<ExerciseProgress>) {
    if (this.saving() || this.loading()) return;
    const current = this.entry(id) || { id, weightKg: null, completed: false };
    const next = { ...current, ...change };
    this.draft.update(entries => [...entries.filter(item => item.id !== id), next]);
    if (next.completed) this.attended.set(true);
    this.persist();
  }

  toggleAttendance() { this.attended.set(!this.attended()); this.persist(); }

  persist() {
    if (!this.user() || !this.plan() || this.saving()) return;
    const generation = this.generation;
    const day: TrainingDay = { date: this.selectedDate(), plan: this.selectedPlan(), attended: this.attended(), exercises: this.draft().map(item => ({ ...item })) };
    this.unsaved.set(true); this.saving.set(true); this.saved.set(false); this.error.set('');
    this.api.save(day).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: saved => {
        if (generation !== this.generation) return;
        this.days.update(days => [...days.filter(item => item.date !== saved.date), saved]);
        this.attended.set(saved.attended); this.saving.set(false); this.unsaved.set(false); this.saved.set(true);
      },
      error: () => {
        if (generation !== this.generation) return;
        this.saving.set(false); this.error.set('Alterações não salvas. Tente novamente antes de trocar de dia.');
      }
    });
  }

  async login() {
    this.error.set('');
    try {
      if (this.localDemoLogin) await signInWithEmailAndPassword(this.auth, 'demo@no-pain-please.local', 'demo-local-password');
      else await signInWithPopup(this.auth, new GoogleAuthProvider());
    }
    catch { this.error.set('Não foi possível entrar. Tente novamente com a conta cadastrada.'); }
  }
  async logout() {
    if (this.unsaved() || this.saving()) return;
    try { await signOut(this.auth); } catch { this.error.set('Não foi possível sair. Tente novamente.'); }
  }
  countCompleted(day: TrainingDay) { return day.exercises.filter(item => item.completed).length; }
  planSize(id: string) { return this.plans().find(plan => plan.id === id)?.exercises.length || 0; }
}
