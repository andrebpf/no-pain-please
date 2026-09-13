package com.nopainplease.api.workout.domain.model;

public record ExerciseProgress(String id, Double weightKg, boolean completed) {
    public ExerciseProgress {
        if (id == null || !id.matches("[A-Za-z0-9_-]{1,80}")) {
            throw new IllegalArgumentException("invalid exercise id");
        }
        if (weightKg != null
                && (!Double.isFinite(weightKg) || weightKg < 0.0 || weightKg > 2000.0)) {
            throw new IllegalArgumentException("invalid exercise weight");
        }
    }
}
