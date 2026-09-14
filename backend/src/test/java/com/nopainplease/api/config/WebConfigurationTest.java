package com.nopainplease.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.nopainplease.api.security.FirebaseAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class WebConfigurationTest {
    private static final String ALLOWED_ORIGIN = "https://no-pain-please.web.app";

    @Test
    void addsCorsHeadersToAnUnauthorizedApiResponse() throws Exception {
        var request = requestFrom(ALLOWED_ORIGIN);
        var response = new MockHttpServletResponse();
        var corsFilter = new WebConfiguration(ALLOWED_ORIGIN).apiCorsFilter().getFilter();

        corsFilter.doFilter(request, response, (servletRequest, servletResponse) ->
                new FirebaseAuthenticationFilter().doFilter(servletRequest, servletResponse,
                        (ignoredRequest, ignoredResponse) -> { }));

        assertEquals(401, response.getStatus());
        assertEquals(ALLOWED_ORIGIN, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void doesNotAllowAnUnexpectedOrigin() throws Exception {
        var request = requestFrom("https://unexpected.example");
        var response = new MockHttpServletResponse();
        var corsFilter = new WebConfiguration(ALLOWED_ORIGIN).apiCorsFilter().getFilter();

        corsFilter.doFilter(request, response, (servletRequest, servletResponse) ->
                new FirebaseAuthenticationFilter().doFilter(servletRequest, servletResponse,
                        (ignoredRequest, ignoredResponse) -> { }));

        assertEquals(403, response.getStatus());
        assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void acceptsPreflightFromTheConfiguredOrigin() throws Exception {
        var request = new MockHttpServletRequest("OPTIONS", "/api/training-plans");
        request.addHeader(HttpHeaders.ORIGIN, ALLOWED_ORIGIN);
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET");
        var response = new MockHttpServletResponse();
        var corsFilter = new WebConfiguration(ALLOWED_ORIGIN).apiCorsFilter().getFilter();

        corsFilter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new AssertionError("The preflight request must be handled before authentication");
        });

        assertEquals(200, response.getStatus());
        assertEquals(ALLOWED_ORIGIN, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void requiresAuthenticationAtTheApiRoot() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api");
        var response = new MockHttpServletResponse();

        new FirebaseAuthenticationFilter().doFilter(request, response,
                (servletRequest, servletResponse) -> {
                    throw new AssertionError("Unauthenticated requests must not reach the API");
                });

        assertEquals(401, response.getStatus());
    }

    private MockHttpServletRequest requestFrom(String origin) {
        var request = new MockHttpServletRequest("GET", "/api/training-plans");
        request.addHeader(HttpHeaders.ORIGIN, origin);
        return request;
    }
}
