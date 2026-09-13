package com.nopainplease.api.workout.application.service;

import com.nopainplease.api.workout.application.exception.InvalidTrainingDayException;
import com.nopainplease.api.workout.application.port.in.ListTrainingDaysUseCase;
import com.nopainplease.api.workout.application.port.in.SaveTrainingDayUseCase;
import com.nopainplease.api.workout.application.port.out.TrainingDayRepository;
import com.nopainplease.api.workout.application.port.out.TrainingPlanRepository;
import com.nopainplease.api.workout.domain.model.TrainingDay;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public final class TrainingDayService implements ListTrainingDaysUseCase, SaveTrainingDayUseCase {
    private final TrainingDayRepository trainingDays;
    private final TrainingPlanRepository trainingPlans;
    private final Clock clock;

    public TrainingDayService(
            TrainingDayRepository trainingDays,
            TrainingPlanRepository trainingPlans,
            Clock clock) {
        this.trainingDays = trainingDays;
        this.trainingPlans = trainingPlans;
        this.clock = clock;
    }

    @Override
    public List<TrainingDay> list(String userId) {
        return trainingDays.findAll(userId);
    }

    @Override
    public TrainingDay save(String userId, LocalDate requestedDate, TrainingDay trainingDay) {
        if (!requestedDate.equals(trainingDay.date())
                || requestedDate.isAfter(LocalDate.now(clock))) {
            throw new InvalidTrainingDayException("Invalid training date");
        }

        var plan = trainingPlans.findById(userId, trainingDay.planId())
                .orElseThrow(() -> new InvalidTrainingDayException("Unknown training plan"));
        var allowedExerciseIds = plan.exercises().stream()
                .map(exercise -> exercise.id())
                .collect(Collectors.toUnmodifiableSet());
        if (trainingDay.exercises().stream()
                .anyMatch(exercise -> !allowedExerciseIds.contains(exercise.id()))) {
            throw new InvalidTrainingDayException("Exercise does not belong to training plan");
        }

        return trainingDays.save(userId, trainingDay);
    }
}
