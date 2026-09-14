package com.nopainplease.api.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {
    public static final String PRINCIPAL_ATTRIBUTE = FirebasePrincipal.class.getName();
    private static final Logger LOG = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return org.springframework.web.cors.CorsUtils.isPreFlightRequest(request)
                || (!request.getRequestURI().equals("/api") && !request.getRequestURI().startsWith("/api/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            unauthorized(response);
            return;
        }
        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(header.substring(7));
            request.setAttribute(PRINCIPAL_ATTRIBUTE, new FirebasePrincipal(token.getUid(), token.getEmail()));
            chain.doFilter(request, response);
        } catch (FirebaseAuthException | IllegalArgumentException exception) {
            LOG.warn("Rejected Firebase ID token: {}", exception.getClass().getSimpleName());
            unauthorized(response);
        }
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"A valid Firebase ID token is required\"}");
    }
}
