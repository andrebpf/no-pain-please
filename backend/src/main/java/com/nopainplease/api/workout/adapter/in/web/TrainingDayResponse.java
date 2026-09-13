package com.nopainplease.api.workout.adapter.in.web;

import com.nopainplease.api.workout.domain.model.TrainingDay;
import java.util.List;

public record TrainingDayResponse(
        String date,
        String plan,
        boolean attended,
        List<ExerciseProgressResponse> exercises) {
    static TrainingDayResponse from(TrainingDay day) {
        return new TrainingDayResponse(
                day.date().toString(),
                day.planId(),
                day.attended(),
                day.exercises().stream()
                        .map(exercise -> new ExerciseProgressResponse(
                                exercise.id(), exercise.weightKg(), exercise.completed()))
                        .toList());
    }

    public record ExerciseProgressResponse(String id, Double weightKg, boolean completed) {
    }
}
