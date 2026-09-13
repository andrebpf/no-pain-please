package com.nopainplease.api.workout.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record Workout(
        String id,
        String exercise,
        String notes,
        Instant performedAt,
        List<WorkoutSet> sets) {
    public Workout {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(exercise, "exercise is required");
        Objects.requireNonNull(performedAt, "performedAt is required");
        sets = List.copyOf(sets);
        notes = notes == null ? "" : notes;
    }
}
