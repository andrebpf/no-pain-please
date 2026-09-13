package com.nopainplease.api.workout.adapter.in.web;

import com.nopainplease.api.workout.domain.model.Workout;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record WorkoutResponse(
        String id,
        String exercise,
        String notes,
        Instant performedAt,
        List<WorkoutSetResponse> sets) {
    static WorkoutResponse from(Workout workout) {
        return new WorkoutResponse(
                workout.id(),
                workout.exercise(),
                workout.notes(),
                workout.performedAt(),
                workout.sets().stream()
                        .map(set -> new WorkoutSetResponse(set.weightKg(), set.repetitions()))
                        .toList());
    }

    public record WorkoutSetResponse(BigDecimal weightKg, int repetitions) {
    }
}
