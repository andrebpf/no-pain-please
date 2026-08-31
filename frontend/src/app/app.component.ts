import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AsyncPipe, DatePipe, DecimalPipe, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Auth, GoogleAuthProvider, authState, signInWithPopup, signOut } from '@angular/fire/auth';
import { BehaviorSubject, EMPTY, switchMap } from 'rxjs';
import { WorkoutsService, Workout } from './workouts.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [AsyncPipe, DatePipe, DecimalPipe, FormsModule, NgFor, NgIf],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main>
      <header><span class="brand">No Pain Please</span><button *ngIf="user$ | async" (click)="logout()">Sign out</button></header>
      <section class="hero" *ngIf="!(user$ | async); else dashboard">
        <p class="eyebrow">YOUR TRAINING LOG</p><h1>Progress, one set at a time.</h1>
        <p>Log workouts, see your history, and pick up wherever you train.</p>
        <button class="primary" (click)="login()">Continue with Google</button>
      </section>
      <ng-template #dashboard>
        <section class="dashboard">
          <div><p class="eyebrow">TODAY'S TRAINING</p><h1>Log a set</h1></div>
          <form (ngSubmit)="save()">
            <label>Exercise <input name="exercise" [(ngModel)]="exercise" required placeholder="e.g. Barbell squat"></label>
            <div class="fields"><label>Weight (kg) <input name="weight" [(ngModel)]="weightKg" type="number" min="0" step="0.5" required></label><label>Reps <input name="repetitions" [(ngModel)]="repetitions" type="number" min="1" required></label></div>
            <label>Notes <input name="notes" [(ngModel)]="notes" placeholder="Optional"></label>
            <button class="primary" [disabled]="saving">{{ saving ? 'Saving…' : 'Save workout' }}</button>
          </form>
          <p class="error" *ngIf="error">{{ error }}</p>
          <section class="history"><h2>Recent workouts</h2><p *ngIf="(workouts$ | async)?.length === 0">Your first workout is waiting.</p>
            <article *ngFor="let workout of workouts$ | async"><div><strong>{{ workout.exercise }}</strong><span>{{ workout.performedAt | date:'mediumDate' }}</span></div><span *ngFor="let set of workout.sets">{{ set.weightKg | number:'1.0-1' }} kg × {{ set.repetitions }}</span></article>
          </section>
        </section>
      </ng-template>
    </main>
  `,
  styles: [`:host{font-family:Inter,system-ui,sans-serif;color:#1c1917}main{max-width:760px;margin:auto;padding:24px}header{display:flex;justify-content:space-between;align-items:center}.brand{font-weight:800}.hero{padding:17vh 0}.eyebrow{font-size:.75rem;font-weight:800;letter-spacing:.12em;color:#b45309}h1{font-size:clamp(2.4rem,8vw,4.5rem);line-height:1;margin:.4rem 0 1rem}.hero>p:not(.eyebrow){font-size:1.2rem;max-width:35rem;color:#57534e}.dashboard{padding-top:64px}form{display:grid;gap:16px;padding:24px;background:#f5f5f4;border-radius:16px}label{display:grid;gap:6px;font-weight:600}.fields{display:grid;grid-template-columns:1fr 1fr;gap:16px}input{border:1px solid #d6d3d1;border-radius:8px;padding:12px;font:inherit;background:#fff}button{border:0;border-radius:8px;padding:10px 16px;font:inherit;font-weight:700;cursor:pointer}.primary{background:#ea580c;color:#fff;width:max-content}.primary:disabled{opacity:.6}.history{margin-top:40px}.history article{display:flex;justify-content:space-between;align-items:center;padding:16px 0;border-top:1px solid #e7e5e4}.history article div{display:grid;gap:3px}.history article div span{font-size:.85rem;color:#78716c}.error{color:#b91c1c}@media(max-width:480px){.fields{grid-template-columns:1fr}.history article{align-items:flex-end;gap:12px}}`]
})
export class AppComponent {
  private readonly auth = inject(Auth);
  private readonly workouts = inject(WorkoutsService);
  private readonly refresh$ = new BehaviorSubject<void>(undefined);
  readonly user$ = authState(this.auth);
  readonly workouts$ = this.user$.pipe(switchMap(user => user ? this.refresh$.pipe(switchMap(() => this.workouts.list())) : EMPTY));
  exercise = ''; weightKg = 20; repetitions = 8; notes = ''; saving = false; error = '';

  login() { signInWithPopup(this.auth, new GoogleAuthProvider()).catch(() => this.error = 'Could not sign in. Check the Firebase web configuration.'); }
  logout() { signOut(this.auth); }
  save() {
    this.saving = true; this.error = '';
    this.workouts.create({ exercise: this.exercise, notes: this.notes || undefined, sets: [{ weightKg: this.weightKg, repetitions: this.repetitions }] }).subscribe({
      next: () => { this.exercise = ''; this.notes = ''; this.saving = false; this.refresh$.next(); },
      error: () => { this.error = 'Could not save the workout. Please try again.'; this.saving = false; }
    });
  }
}
