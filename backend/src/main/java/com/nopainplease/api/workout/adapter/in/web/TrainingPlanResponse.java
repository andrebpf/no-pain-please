package com.nopainplease.api.workout.adapter.in.web;

import com.nopainplease.api.workout.domain.model.TrainingPlan;
import java.util.List;

public record TrainingPlanResponse(
        String id,
        String title,
        String description,
        String cardio,
        List<ExerciseResponse> exercises) {
    static TrainingPlanResponse from(TrainingPlan plan) {
        return new TrainingPlanResponse(
                plan.id(),
                plan.title(),
                plan.description(),
                plan.cardio(),
                plan.exercises().stream().map(ExerciseResponse::from).toList());
    }

    public record ExerciseResponse(
            String id,
            String name,
            int sets,
            String reps,
            String rest,
            String group,
            String video) {
        static ExerciseResponse from(TrainingPlan.Exercise exercise) {
            return new ExerciseResponse(
                    exercise.id(),
                    exercise.name(),
                    exercise.sets(),
                    exercise.reps(),
                    exercise.rest(),
                    exercise.group(),
                    exercise.video());
        }
    }
}
