package com.nopainplease.api.workout.application.service;

import com.nopainplease.api.workout.application.port.in.ListTrainingPlansUseCase;
import com.nopainplease.api.workout.application.port.out.TrainingPlanRepository;
import com.nopainplease.api.workout.domain.model.TrainingPlan;
import java.util.List;

public final class TrainingPlanService implements ListTrainingPlansUseCase {
    private final TrainingPlanRepository repository;

    public TrainingPlanService(TrainingPlanRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TrainingPlan> list(String userId) {
        return repository.findAll(userId);
    }
}
