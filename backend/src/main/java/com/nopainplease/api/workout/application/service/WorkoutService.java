package com.nopainplease.api.workout.application.service;

import com.nopainplease.api.workout.application.port.in.CreateWorkoutUseCase;
import com.nopainplease.api.workout.application.port.in.ListWorkoutsUseCase;
import com.nopainplease.api.workout.application.port.out.WorkoutRepository;
import com.nopainplease.api.workout.domain.model.Workout;
import com.nopainplease.api.workout.domain.model.WorkoutDraft;
import java.time.Clock;
import java.util.List;

public final class WorkoutService implements CreateWorkoutUseCase, ListWorkoutsUseCase {
    private static final int RECENT_WORKOUT_LIMIT = 50;

    private final WorkoutRepository repository;
    private final Clock clock;

    public WorkoutService(WorkoutRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public Workout create(String userId, CreateWorkoutCommand command) {
        var workout = new WorkoutDraft(
                command.exercise(), command.notes(), clock.instant(), command.sets());
        return repository.save(userId, workout);
    }

    @Override
    public List<Workout> listRecent(String userId) {
        return repository.findRecent(userId, RECENT_WORKOUT_LIMIT);
    }
}
