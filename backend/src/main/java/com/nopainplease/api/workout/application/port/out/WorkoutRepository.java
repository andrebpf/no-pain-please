package com.nopainplease.api.workout.application.port.out;

import com.nopainplease.api.workout.domain.model.Workout;
import com.nopainplease.api.workout.domain.model.WorkoutDraft;
import java.util.List;

public interface WorkoutRepository {
    Workout save(String userId, WorkoutDraft workout);

    List<Workout> findRecent(String userId, int limit);
}
