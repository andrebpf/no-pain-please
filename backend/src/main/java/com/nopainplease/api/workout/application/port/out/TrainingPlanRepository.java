package com.nopainplease.api.workout.application.port.out;

import com.nopainplease.api.workout.domain.model.TrainingPlan;
import java.util.List;
import java.util.Optional;

public interface TrainingPlanRepository {
    List<TrainingPlan> findAll(String userId);

    Optional<TrainingPlan> findById(String userId, String planId);
}
