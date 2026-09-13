package com.nopainplease.api.workout.domain.model;

import java.util.List;

public record TrainingPlan(
        String id,
        String title,
        String description,
        String cardio,
        List<Exercise> exercises) {
    public TrainingPlan {
        exercises = List.copyOf(exercises);
    }

    public record Exercise(
            String id,
            String name,
            int sets,
            String reps,
            String rest,
            String group,
            String video) {
    }
}
