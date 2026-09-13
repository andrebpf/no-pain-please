package com.nopainplease.api.workout.application.port.in;

import com.nopainplease.api.workout.domain.model.TrainingPlan;
import java.util.List;

public interface ListTrainingPlansUseCase {
    List<TrainingPlan> list(String userId);
}
