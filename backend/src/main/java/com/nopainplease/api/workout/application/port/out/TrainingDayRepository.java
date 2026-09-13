package com.nopainplease.api.workout.application.port.out;

import com.nopainplease.api.workout.domain.model.TrainingDay;
import java.util.List;

public interface TrainingDayRepository {
    List<TrainingDay> findAll(String userId);

    TrainingDay save(String userId, TrainingDay trainingDay);
}
