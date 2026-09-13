package com.nopainplease.api.workout.domain.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public record TrainingDay(
        LocalDate date,
        String planId,
        boolean attended,
        List<ExerciseProgress> exercises) {
    public TrainingDay {
        Objects.requireNonNull(date, "date is required");
        if (planId == null || !planId.matches("[A-Za-z0-9_-]{1,80}")) {
            throw new IllegalArgumentException("invalid training plan id");
        }
        exercises = List.copyOf(exercises);
        if (exercises.size() > 100) {
            throw new IllegalArgumentException("too many exercises");
        }

        var exerciseIds = new HashSet<String>();
        if (exercises.stream().anyMatch(exercise -> !exerciseIds.add(exercise.id()))) {
            throw new IllegalArgumentException("duplicate exercise id");
        }
        attended = attended || exercises.stream().anyMatch(ExerciseProgress::completed);
    }
}
