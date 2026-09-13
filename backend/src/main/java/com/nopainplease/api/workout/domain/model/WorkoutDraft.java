package com.nopainplease.api.workout.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record WorkoutDraft(
        String exercise,
        String notes,
        Instant performedAt,
        List<WorkoutSet> sets) {
    public WorkoutDraft {
        if (exercise == null || exercise.isBlank()) {
            throw new IllegalArgumentException("exercise is required");
        }
        Objects.requireNonNull(performedAt, "performedAt is required");
        sets = List.copyOf(sets);
        if (sets.isEmpty()) {
            throw new IllegalArgumentException("at least one set is required");
        }
        notes = notes == null ? "" : notes;
    }
}
