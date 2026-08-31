package com.nopainplease.api.workout;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateWorkoutRequest(
    @NotBlank @Size(max = 120) String exercise,
    @Size(max = 1000) String notes,
    @NotEmpty List<@Valid WorkoutSet> sets) {}
