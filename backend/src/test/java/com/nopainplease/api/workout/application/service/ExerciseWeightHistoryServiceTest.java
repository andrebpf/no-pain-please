package com.nopainplease.api.workout.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nopainplease.api.workout.application.port.in.ListExerciseWeightHistoryUseCase.WeightPoint;
import com.nopainplease.api.workout.application.port.out.TrainingDayRepository;
import com.nopainplease.api.workout.application.port.out.TrainingPlanRepository;
import com.nopainplease.api.workout.domain.model.ExerciseProgress;
import com.nopainplease.api.workout.domain.model.TrainingDay;
import com.nopainplease.api.workout.domain.model.TrainingPlan;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExerciseWeightHistoryServiceTest {
    @Test
    void listsRecordedWeightsForOneExerciseInChronologicalOrder() {
        var days = List.of(
                day("2026-09-12", new ExerciseProgress("A-1", 50.0, true)),
                day("2026-09-10", new ExerciseProgress("A-1", 47.5, true)),
                day("2026-09-11", new ExerciseProgress("A-2", 20.0, true)),
                day("2026-09-09", new ExerciseProgress("A-1", null, true)),
                day("2026-09-08", new ExerciseProgress("A-1", 0.0, true)));
        var service = new TrainingDayService(new FixedDays(days), emptyPlans(), Clock.system(ZoneOffset.UTC));

        assertEquals(List.of(
                new WeightPoint(LocalDate.parse("2026-09-08"), 0.0),
                new WeightPoint(LocalDate.parse("2026-09-10"), 47.5),
                new WeightPoint(LocalDate.parse("2026-09-12"), 50.0)),
                service.list("user-1", "A-1"));
    }

    private TrainingDay day(String date, ExerciseProgress exercise) {
        return new TrainingDay(LocalDate.parse(date), "A", true, List.of(exercise));
    }

    private TrainingPlanRepository emptyPlans() {
        return new TrainingPlanRepository() {
            @Override
            public List<TrainingPlan> findAll(String userId) {
                return List.of();
            }

            @Override
            public Optional<TrainingPlan> findById(String userId, String planId) {
                return Optional.empty();
            }
        };
    }

    private record FixedDays(List<TrainingDay> days) implements TrainingDayRepository {
        @Override
        public List<TrainingDay> findAll(String userId) {
            return days;
        }

        @Override
        public TrainingDay save(String userId, TrainingDay trainingDay) {
            return trainingDay;
        }
    }
}
