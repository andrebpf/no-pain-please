package com.nopainplease.api.workout.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record WorkoutSet(BigDecimal weightKg, int repetitions) {
    public WorkoutSet {
        Objects.requireNonNull(weightKg, "weightKg is required");
        if (weightKg.signum() < 0) {
            throw new IllegalArgumentException("weightKg must not be negative");
        }
        if (repetitions < 1) {
            throw new IllegalArgumentException("repetitions must be positive");
        }
    }
}
