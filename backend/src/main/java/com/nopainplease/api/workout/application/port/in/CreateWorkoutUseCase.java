package com.nopainplease.api.workout.application.port.in;

import com.nopainplease.api.workout.domain.model.Workout;
import com.nopainplease.api.workout.domain.model.WorkoutSet;
import java.util.List;

public interface CreateWorkoutUseCase {
    Workout create(String userId, CreateWorkoutCommand command);

    record CreateWorkoutCommand(String exercise, String notes, List<WorkoutSet> sets) {
        public CreateWorkoutCommand {
            sets = List.copyOf(sets);
        }
    }
}
