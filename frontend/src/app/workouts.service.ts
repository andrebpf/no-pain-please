import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

export interface WorkoutSet { weightKg: number; repetitions: number; }
export interface Workout { id: string; exercise: string; performedAt: string; notes?: string; sets: WorkoutSet[]; }
export interface CreateWorkout { exercise: string; notes?: string; sets: WorkoutSet[]; }

@Injectable({ providedIn: 'root' })
export class WorkoutsService {
  private readonly url = `${environment.apiBaseUrl}/workouts`;
  constructor(private readonly http: HttpClient) {}

  list() { return this.http.get<Workout[]>(this.url); }
  create(workout: CreateWorkout) { return this.http.post<Workout>(this.url, workout); }
}
