package com.nopainplease.api.workout.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nopainplease.api.workout.application.exception.InvalidTrainingDayException;
import com.nopainplease.api.workout.application.port.out.TrainingDayRepository;
import com.nopainplease.api.workout.application.port.out.TrainingPlanRepository;
import com.nopainplease.api.workout.domain.model.ExerciseProgress;
import com.nopainplease.api.workout.domain.model.TrainingDay;
import com.nopainplease.api.workout.domain.model.TrainingPlan;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TrainingDayServiceTest {
    private static final String USER_ID = "user-1";
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 13);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-09-13T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    private final InMemoryTrainingDayRepository days = new InMemoryTrainingDayRepository();
    private final TrainingPlanRepository plans = new TrainingPlanRepository() {
        @Override
        public List<TrainingPlan> findAll(String userId) {
            return List.of(plan());
        }

        @Override
        public Optional<TrainingPlan> findById(String userId, String planId) {
            return "A".equals(planId) ? Optional.of(plan()) : Optional.empty();
        }
    };
    private final TrainingDayService service = new TrainingDayService(days, plans, CLOCK);

    @Test
    void savesAValidTrainingDayThroughTheOutputPort() {
        var trainingDay = day(TODAY, "A-1");

        assertEquals(trainingDay, service.save(USER_ID, TODAY, trainingDay));
        assertEquals(trainingDay, days.saved);
    }

    @Test
    void rejectsFutureOrMismatchingDates() {
        assertThrows(InvalidTrainingDayException.class,
                () -> service.save(USER_ID, TODAY.plusDays(1), day(TODAY.plusDays(1), "A-1")));
        assertThrows(InvalidTrainingDayException.class,
                () -> service.save(USER_ID, TODAY.minusDays(1), day(TODAY, "A-1")));
    }

    @Test
    void rejectsUnknownPlansAndExercisesOutsideThePlan() {
        assertThrows(InvalidTrainingDayException.class,
                () -> service.save(USER_ID, TODAY, day(TODAY, "B-1")));
        var unknownPlan = new TrainingDay(
                TODAY, "missing", false, List.of(new ExerciseProgress("A-1", null, false)));
        assertThrows(InvalidTrainingDayException.class,
                () -> service.save(USER_ID, TODAY, unknownPlan));
    }

    private TrainingDay day(LocalDate date, String exerciseId) {
        return new TrainingDay(
                date, "A", false, List.of(new ExerciseProgress(exerciseId, 10.0, false)));
    }

    private TrainingPlan plan() {
        return new TrainingPlan(
                "A",
                "Plan A",
                "Description",
                "Cardio",
                List.of(new TrainingPlan.Exercise(
                        "A-1", "Exercise", 3, "8-12", "60 s", "Legs", "https://example.com")));
    }

    private static final class InMemoryTrainingDayRepository implements TrainingDayRepository {
        private TrainingDay saved;

        @Override
        public List<TrainingDay> findAll(String userId) {
            return saved == null ? List.of() : List.of(saved);
        }

        @Override
        public TrainingDay save(String userId, TrainingDay trainingDay) {
            saved = trainingDay;
            return trainingDay;
        }
    }
}
