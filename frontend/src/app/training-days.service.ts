import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { TrainingPlan } from './training-plans';

export interface ExerciseProgress { id: string; weightKg: number | null; completed: boolean; }
export interface TrainingDay { date: string; plan: string; attended: boolean; exercises: ExerciseProgress[]; }
export interface ExerciseWeightPoint { date: string; weightKg: number; }

@Injectable({ providedIn: 'root' })
export class TrainingDaysService {
  private readonly http = inject(HttpClient);
  private readonly url = environment.apiBaseUrl;
  plans() { return this.http.get<TrainingPlan[]>(`${this.url}/training-plans`); }
  list() { return this.http.get<TrainingDay[]>(`${this.url}/training-days`); }
  save(day: TrainingDay) { return this.http.put<TrainingDay>(`${this.url}/training-days/${day.date}`, day); }
  weightHistory(exerciseId: string) {
    return this.http.get<ExerciseWeightPoint[]>(`${this.url}/exercise-weight-history/${encodeURIComponent(exerciseId)}`);
  }
}
