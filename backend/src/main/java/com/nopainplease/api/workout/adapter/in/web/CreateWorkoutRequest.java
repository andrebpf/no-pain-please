package com.nopainplease.api.workout.adapter.in.web;

import com.nopainplease.api.workout.application.port.in.CreateWorkoutUseCase.CreateWorkoutCommand;
import com.nopainplease.api.workout.domain.model.WorkoutSet;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CreateWorkoutRequest(
        @NotBlank @Size(max = 120) String exercise,
        @Size(max = 1000) String notes,
        @NotEmpty List<@NotNull @Valid WorkoutSetRequest> sets) {
    CreateWorkoutCommand toCommand() {
        return new CreateWorkoutCommand(
                exercise,
                notes,
                sets.stream().map(WorkoutSetRequest::toDomain).toList());
    }

    public record WorkoutSetRequest(
            @NotNull @DecimalMin("0.0") BigDecimal weightKg,
            @Min(1) int repetitions) {
        WorkoutSet toDomain() {
            return new WorkoutSet(weightKg, repetitions);
        }
    }
}
