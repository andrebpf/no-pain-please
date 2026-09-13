package com.nopainplease.api.workout.application.port.in;

import com.nopainplease.api.workout.domain.model.TrainingDay;
import java.util.List;

public interface ListTrainingDaysUseCase {
    List<TrainingDay> list(String userId);
}
