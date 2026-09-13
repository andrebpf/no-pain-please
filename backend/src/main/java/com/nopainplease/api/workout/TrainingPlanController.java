package com.nopainplease.api.workout;

import com.google.cloud.firestore.Firestore;
import com.nopainplease.api.security.FirebaseAuthenticationFilter;
import com.nopainplease.api.security.FirebasePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/training-plans")
public class TrainingPlanController {
  private final Firestore firestore;
  public TrainingPlanController(Firestore firestore) { this.firestore = firestore; }

  @GetMapping
  public List<Map<String, Object>> list(HttpServletRequest request) throws Exception {
    var principal = (FirebasePrincipal) request.getAttribute(FirebaseAuthenticationFilter.PRINCIPAL_ATTRIBUTE);
    return firestore.collection("users").document(principal.uid()).collection("trainingPlans")
        .orderBy("id").get().get().getDocuments().stream().map(doc -> doc.getData()).toList();
  }
}
