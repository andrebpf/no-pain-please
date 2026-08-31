package com.nopainplease.api.workout;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WorkoutService {
  private final FirestoreWorkoutRepository repository;
  public WorkoutService(FirestoreWorkoutRepository repository) { this.repository = repository; }
  public Workout create(String uid, CreateWorkoutRequest request) { return repository.save(uid, request); }
  public List<Workout> recent(String uid) { return repository.findRecent(uid); }
}
