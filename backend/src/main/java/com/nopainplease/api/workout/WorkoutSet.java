package com.nopainplease.api.workout;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record WorkoutSet(@DecimalMin("0.0") BigDecimal weightKg, @Min(1) int repetitions) {}
