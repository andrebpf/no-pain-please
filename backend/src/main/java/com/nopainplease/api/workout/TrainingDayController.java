package com.nopainplease.api.workout;

import com.google.cloud.firestore.Firestore;
import com.nopainplease.api.security.FirebaseAuthenticationFilter;
import com.nopainplease.api.security.FirebasePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/training-days")
public class TrainingDayController {
  private final Firestore firestore;
  public TrainingDayController(Firestore firestore) { this.firestore = firestore; }

  private com.google.cloud.firestore.CollectionReference days(HttpServletRequest request) {
    var principal = (FirebasePrincipal) request.getAttribute(FirebaseAuthenticationFilter.PRINCIPAL_ATTRIBUTE);
    return firestore.collection("users").document(principal.uid()).collection("trainingDays");
  }

  @GetMapping
  public List<TrainingDay> list(HttpServletRequest request) throws Exception {
    return days(request).orderBy("date", com.google.cloud.firestore.Query.Direction.DESCENDING)
        .get().get().getDocuments().stream().map(doc -> {
          @SuppressWarnings("unchecked")
          var entries = (List<Map<String, Object>>) doc.get("exercises");
          var exercises = entries.stream().map(item -> new TrainingDay.ExerciseProgress(
              (String) item.get("id"), item.get("weightKg") == null ? null : ((Number) item.get("weightKg")).doubleValue(),
              Boolean.TRUE.equals(item.get("completed")))).toList();
          return new TrainingDay(doc.getString("date"), doc.getString("plan"), Boolean.TRUE.equals(doc.getBoolean("attended")), exercises);
        }).toList();
  }

  static void validateDay(String date, TrainingDay day) {
    try {
      if (!date.equals(day.date()) || LocalDate.parse(date).isAfter(LocalDate.now(ZoneId.of("America/Sao_Paulo"))))
        throw new DateTimeException("Invalid date");
    } catch (DateTimeException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid training date");
    }
    var ids = new HashSet<String>();
    for (var exercise : day.exercises()) {
      if (!ids.add(exercise.id())
          || (exercise.weightKg() != null && !Double.isFinite(exercise.weightKg())))
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid exercise");
    }
  }

  @PutMapping("/{date}")
  public TrainingDay save(HttpServletRequest request, @PathVariable String date, @Valid @RequestBody TrainingDay day) throws Exception {
    validateDay(date, day);
    var principal = (FirebasePrincipal) request.getAttribute(FirebaseAuthenticationFilter.PRINCIPAL_ATTRIBUTE);
    var plan = firestore.collection("users").document(principal.uid()).collection("trainingPlans").document(day.plan()).get().get();
    if (!plan.exists()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown training plan");
    @SuppressWarnings("unchecked")
    var planExercises = (List<Map<String, Object>>) plan.get("exercises");
    var allowedIds = planExercises.stream().map(item -> (String) item.get("id")).collect(java.util.stream.Collectors.toSet());
    if (day.exercises().stream().anyMatch(item -> !allowedIds.contains(item.id())))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exercise does not belong to training plan");
    var exercises = day.exercises().stream().map(exercise -> {
      Map<String, Object> item = new HashMap<>();
      item.put("id", exercise.id()); item.put("weightKg", exercise.weightKg()); item.put("completed", exercise.completed());
      return item;
    }).toList();
    boolean attended = day.attended() || day.exercises().stream().anyMatch(TrainingDay.ExerciseProgress::completed);
    days(request).document(date).set(Map.of("date", date, "plan", day.plan(), "attended", attended, "exercises", exercises)).get();
    return new TrainingDay(date, day.plan(), attended, day.exercises());
  }
}
