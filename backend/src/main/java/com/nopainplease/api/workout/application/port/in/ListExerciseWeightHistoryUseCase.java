package com.nopainplease.api.workout.application.port.in;

import java.time.LocalDate;
import java.util.List;

public interface ListExerciseWeightHistoryUseCase {
    List<WeightPoint> list(String userId, String exerciseId);

    record WeightPoint(LocalDate date, double weightKg) {
    }
}
