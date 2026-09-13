package com.nopainplease.api.workout.adapter.out.firestore;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.nopainplease.api.workout.application.port.out.WorkoutRepository;
import com.nopainplease.api.workout.domain.model.Workout;
import com.nopainplease.api.workout.domain.model.WorkoutDraft;
import com.nopainplease.api.workout.domain.model.WorkoutSet;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class FirestoreWorkoutRepository implements WorkoutRepository {
    private final Firestore firestore;

    public FirestoreWorkoutRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Workout save(String userId, WorkoutDraft workout) {
        var reference = firestore.collection("users")
                .document(userId)
                .collection("workouts")
                .document();
        var sets = workout.sets().stream()
                .map(set -> Map.<String, Object>of(
                        "weightKg", set.weightKg().doubleValue(),
                        "repetitions", set.repetitions()))
                .toList();
        var performedAt = Timestamp.ofTimeSecondsAndNanos(
                workout.performedAt().getEpochSecond(), workout.performedAt().getNano());
        var data = Map.<String, Object>of(
                "exercise", workout.exercise(),
                "notes", workout.notes(),
                "performedAt", performedAt,
                "createdAt", Timestamp.now(),
                "sets", sets);

        FirestoreOperation.await(reference.set(data), "Could not save workout");
        return new Workout(
                reference.getId(),
                workout.exercise(),
                workout.notes(),
                workout.performedAt(),
                workout.sets());
    }

    @Override
    public List<Workout> findRecent(String userId, int limit) {
        var snapshot = FirestoreOperation.await(
                firestore.collection("users")
                        .document(userId)
                        .collection("workouts")
                        .orderBy("performedAt", Query.Direction.DESCENDING)
                        .limit(limit)
                        .get(),
                "Could not load workouts");
        return snapshot.getDocuments().stream().map(this::toWorkout).toList();
    }

    @SuppressWarnings("unchecked")
    private Workout toWorkout(DocumentSnapshot document) {
        var performedAt = document.getTimestamp("performedAt");
        var rawSets = (List<Map<String, Object>>) document.get("sets");
        var sets = rawSets == null
                ? List.<WorkoutSet>of()
                : rawSets.stream()
                        .map(set -> new WorkoutSet(
                                new BigDecimal(set.get("weightKg").toString()),
                                ((Number) set.get("repetitions")).intValue()))
                        .toList();
        return new Workout(
                document.getId(),
                document.getString("exercise"),
                document.getString("notes"),
                performedAt == null ? Instant.EPOCH : performedAt.toDate().toInstant(),
                sets);
    }
}
