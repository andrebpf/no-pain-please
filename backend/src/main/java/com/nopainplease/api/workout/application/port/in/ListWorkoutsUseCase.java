package com.nopainplease.api.workout.application.port.in;

import com.nopainplease.api.workout.domain.model.Workout;
import java.util.List;

public interface ListWorkoutsUseCase {
    List<Workout> listRecent(String userId);
}
