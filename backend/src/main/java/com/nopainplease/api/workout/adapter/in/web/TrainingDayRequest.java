package com.nopainplease.api.workout.adapter.in.web;

import com.nopainplease.api.workout.application.exception.InvalidTrainingDayException;
import com.nopainplease.api.workout.domain.model.ExerciseProgress;
import com.nopainplease.api.workout.domain.model.TrainingDay;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

public record TrainingDayRequest(
        @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String date,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,80}") String plan,
        boolean attended,
        @NotNull @Size(max = 100) List<@NotNull @Valid ExerciseProgressRequest> exercises) {
    TrainingDay toDomain() {
        try {
            return new TrainingDay(
                    LocalDate.parse(date),
                    plan,
                    attended,
                    exercises.stream().map(ExerciseProgressRequest::toDomain).toList());
        } catch (DateTimeException | IllegalArgumentException exception) {
            throw new InvalidTrainingDayException("Invalid training day", exception);
        }
    }

    public record ExerciseProgressRequest(
            @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,80}") String id,
            @DecimalMin("0.0") @DecimalMax("2000.0") Double weightKg,
            boolean completed) {
        ExerciseProgress toDomain() {
            return new ExerciseProgress(id, weightKg, completed);
        }
    }
}
