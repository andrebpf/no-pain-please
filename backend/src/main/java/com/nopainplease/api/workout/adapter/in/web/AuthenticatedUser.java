package com.nopainplease.api.workout.adapter.in.web;

import com.nopainplease.api.security.FirebaseAuthenticationFilter;
import com.nopainplease.api.security.FirebasePrincipal;
import jakarta.servlet.http.HttpServletRequest;

final class AuthenticatedUser {
    private AuthenticatedUser() {
    }

    static String id(HttpServletRequest request) {
        var principal = (FirebasePrincipal) request.getAttribute(
                FirebaseAuthenticationFilter.PRINCIPAL_ATTRIBUTE);
        return principal.uid();
    }
}
