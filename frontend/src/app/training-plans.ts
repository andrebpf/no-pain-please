export interface Exercise { id: string; name: string; sets: number; reps: string; rest: string; group: string; video: string; }
export interface TrainingPlan { id: string; title: string; description: string; cardio: string; exercises: Exercise[]; }
