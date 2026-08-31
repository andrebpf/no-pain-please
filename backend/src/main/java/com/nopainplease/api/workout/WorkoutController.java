package com.nopainplease.api.workout;

import com.nopainplease.api.security.FirebaseAuthenticationFilter;
import com.nopainplease.api.security.FirebasePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {
  private final WorkoutService service;
  public WorkoutController(WorkoutService service) { this.service = service; }
  @GetMapping public List<Workout> list(HttpServletRequest request) { return service.recent(principal(request).uid()); }
  @PostMapping @ResponseStatus(HttpStatus.CREATED) public Workout create(HttpServletRequest request, @Valid @RequestBody CreateWorkoutRequest body) { return service.create(principal(request).uid(), body); }
  private FirebasePrincipal principal(HttpServletRequest request) { return (FirebasePrincipal) request.getAttribute(FirebaseAuthenticationFilter.PRINCIPAL_ATTRIBUTE); }
}
