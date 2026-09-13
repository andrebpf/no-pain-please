package com.nopainplease.api.workout.adapter.out.firestore;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.nopainplease.api.workout.application.port.out.TrainingPlanRepository;
import com.nopainplease.api.workout.domain.model.TrainingPlan;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class FirestoreTrainingPlanRepository implements TrainingPlanRepository {
    private final Firestore firestore;

    public FirestoreTrainingPlanRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<TrainingPlan> findAll(String userId) {
        var snapshot = FirestoreOperation.await(
                plans(userId).orderBy("id").get(),
                "Could not load training plans");
        return snapshot.getDocuments().stream().map(this::toTrainingPlan).toList();
    }

    @Override
    public Optional<TrainingPlan> findById(String userId, String planId) {
        var document = FirestoreOperation.await(
                plans(userId).document(planId).get(),
                "Could not load training plan");
        return document.exists() ? Optional.of(toTrainingPlan(document)) : Optional.empty();
    }

    private com.google.cloud.firestore.CollectionReference plans(String userId) {
        return firestore.collection("users")
                .document(userId)
                .collection("trainingPlans");
    }

    @SuppressWarnings("unchecked")
    private TrainingPlan toTrainingPlan(DocumentSnapshot document) {
        var entries = (List<Map<String, Object>>) document.get("exercises");
        var exercises = entries == null
                ? List.<TrainingPlan.Exercise>of()
                : entries.stream()
                        .map(item -> new TrainingPlan.Exercise(
                                (String) item.get("id"),
                                (String) item.get("name"),
                                ((Number) item.get("sets")).intValue(),
                                (String) item.get("reps"),
                                (String) item.get("rest"),
                                (String) item.get("group"),
                                (String) item.get("video")))
                        .toList();
        return new TrainingPlan(
                document.getString("id"),
                document.getString("title"),
                document.getString("description"),
                document.getString("cardio"),
                exercises);
    }
}
