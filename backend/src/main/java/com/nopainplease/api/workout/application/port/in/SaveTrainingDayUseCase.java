package com.nopainplease.api.workout.application.port.in;

import com.nopainplease.api.workout.domain.model.TrainingDay;
import java.time.LocalDate;

public interface SaveTrainingDayUseCase {
    TrainingDay save(String userId, LocalDate requestedDate, TrainingDay trainingDay);
}
