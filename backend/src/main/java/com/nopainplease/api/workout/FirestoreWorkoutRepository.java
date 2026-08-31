package com.nopainplease.api.workout;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.Timestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class FirestoreWorkoutRepository {
  private final Firestore firestore;
  public FirestoreWorkoutRepository(Firestore firestore) { this.firestore = firestore; }

  public Workout save(String uid, CreateWorkoutRequest request) {
    var reference = firestore.collection("users").document(uid).collection("workouts").document();
    Instant now = Instant.now();
    List<Map<String, Object>> sets = request.sets().stream()
        .map(set -> Map.<String, Object>of("weightKg", set.weightKg().doubleValue(), "repetitions", set.repetitions()))
        .toList();
    Map<String, Object> data = Map.of("exercise", request.exercise(), "notes", request.notes() == null ? "" : request.notes(), "performedAt", Timestamp.ofTimeSecondsAndNanos(now.getEpochSecond(), now.getNano()), "createdAt", Timestamp.now(), "sets", sets);
    try { reference.set(data).get(); } catch (Exception exception) { throw new IllegalStateException("Could not save workout", exception); }
    return new Workout(reference.getId(), request.exercise(), request.notes(), now, request.sets());
  }

  public List<Workout> findRecent(String uid) {
    try {
      QuerySnapshot snapshot = firestore.collection("users").document(uid).collection("workouts").orderBy("performedAt", com.google.cloud.firestore.Query.Direction.DESCENDING).limit(50).get().get();
      return snapshot.getDocuments().stream().map(this::toWorkout).toList();
    } catch (Exception exception) { throw new IllegalStateException("Could not load workouts", exception); }
  }

  @SuppressWarnings("unchecked") private Workout toWorkout(DocumentSnapshot document) {
    Timestamp performedAt = document.getTimestamp("performedAt");
    List<Map<String, Object>> rawSets = (List<Map<String, Object>>) document.get("sets");
    List<WorkoutSet> sets = rawSets == null ? List.of() : rawSets.stream().map(set -> new WorkoutSet(new BigDecimal(set.get("weightKg").toString()), ((Number) set.get("repetitions")).intValue())).toList();
    return new Workout(document.getId(), document.getString("exercise"), document.getString("notes"), performedAt == null ? Instant.EPOCH : performedAt.toDate().toInstant(), sets);
  }
}
