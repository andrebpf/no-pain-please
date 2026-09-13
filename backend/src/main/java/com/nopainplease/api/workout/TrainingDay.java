package com.nopainplease.api.workout;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record TrainingDay(
    @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String date,
    @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,80}") String plan,
    boolean attended,
    @NotNull @Size(max = 100) List<@NotNull @Valid ExerciseProgress> exercises) {
  public record ExerciseProgress(
      @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,80}") String id,
      @DecimalMin("0.0") @DecimalMax("2000.0") Double weightKg,
      boolean completed) {}
}
