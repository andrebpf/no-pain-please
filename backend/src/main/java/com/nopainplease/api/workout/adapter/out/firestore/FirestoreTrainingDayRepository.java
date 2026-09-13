package com.nopainplease.api.workout.adapter.out.firestore;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.nopainplease.api.workout.application.port.out.TrainingDayRepository;
import com.nopainplease.api.workout.domain.model.ExerciseProgress;
import com.nopainplease.api.workout.domain.model.TrainingDay;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class FirestoreTrainingDayRepository implements TrainingDayRepository {
    private final Firestore firestore;

    public FirestoreTrainingDayRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<TrainingDay> findAll(String userId) {
        var snapshot = FirestoreOperation.await(
                days(userId).orderBy("date", Query.Direction.DESCENDING).get(),
                "Could not load training days");
        return snapshot.getDocuments().stream().map(this::toTrainingDay).toList();
    }

    @Override
    public TrainingDay save(String userId, TrainingDay trainingDay) {
        var exercises = trainingDay.exercises().stream().map(exercise -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", exercise.id());
            item.put("weightKg", exercise.weightKg());
            item.put("completed", exercise.completed());
            return item;
        }).toList();
        var data = Map.<String, Object>of(
                "date", trainingDay.date().toString(),
                "plan", trainingDay.planId(),
                "attended", trainingDay.attended(),
                "exercises", exercises);
        FirestoreOperation.await(
                days(userId).document(trainingDay.date().toString()).set(data),
                "Could not save training day");
        return trainingDay;
    }

    private com.google.cloud.firestore.CollectionReference days(String userId) {
        return firestore.collection("users")
                .document(userId)
                .collection("trainingDays");
    }

    @SuppressWarnings("unchecked")
    private TrainingDay toTrainingDay(DocumentSnapshot document) {
        var entries = (List<Map<String, Object>>) document.get("exercises");
        var exercises = entries == null
                ? List.<ExerciseProgress>of()
                : entries.stream()
                        .map(item -> new ExerciseProgress(
                                (String) item.get("id"),
                                item.get("weightKg") == null
                                        ? null
                                        : ((Number) item.get("weightKg")).doubleValue(),
                                Boolean.TRUE.equals(item.get("completed"))))
                        .toList();
        return new TrainingDay(
                LocalDate.parse(document.getString("date")),
                document.getString("plan"),
                Boolean.TRUE.equals(document.getBoolean("attended")),
                exercises);
    }
}
