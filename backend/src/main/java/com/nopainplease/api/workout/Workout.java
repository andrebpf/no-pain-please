package com.nopainplease.api.workout;

import java.time.Instant;
import java.util.List;

public record Workout(String id, String exercise, String notes, Instant performedAt, List<WorkoutSet> sets) {}
