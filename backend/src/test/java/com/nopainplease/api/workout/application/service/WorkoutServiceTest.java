package com.nopainplease.api.workout.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nopainplease.api.workout.application.port.in.CreateWorkoutUseCase.CreateWorkoutCommand;
import com.nopainplease.api.workout.application.port.out.WorkoutRepository;
import com.nopainplease.api.workout.domain.model.Workout;
import com.nopainplease.api.workout.domain.model.WorkoutDraft;
import com.nopainplease.api.workout.domain.model.WorkoutSet;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class WorkoutServiceTest {
    @Test
    void createsWorkoutWithApplicationClock() {
        var now = Instant.parse("2026-09-13T12:00:00Z");
        var repository = new RecordingWorkoutRepository();
        var service = new WorkoutService(repository, Clock.fixed(now, ZoneOffset.UTC));
        var set = new WorkoutSet(new BigDecimal("42.5"), 10);

        var result = service.create(
                "user-1", new CreateWorkoutCommand("Leg press", null, List.of(set)));

        assertEquals("workout-1", result.id());
        assertEquals(now, repository.saved.performedAt());
        assertEquals("", repository.saved.notes());
    }

    private static final class RecordingWorkoutRepository implements WorkoutRepository {
        private WorkoutDraft saved;

        @Override
        public Workout save(String userId, WorkoutDraft workout) {
            saved = workout;
            return new Workout(
                    "workout-1",
                    workout.exercise(),
                    workout.notes(),
                    workout.performedAt(),
                    workout.sets());
        }

        @Override
        public List<Workout> findRecent(String userId, int limit) {
            return List.of();
        }
    }
}
