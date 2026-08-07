package com.ecommerce.api.ratelimit;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    @Test
    void shouldAllowRequestsWithinLimit() throws ServletException, IOException {
        RateLimitFilter filter = new RateLimitFilter(properties(true, 2, 2, 60));

        MockHttpServletResponse firstResponse = performRequest(filter, "/api/orders", "127.0.0.1", null);
        MockHttpServletResponse secondResponse = performRequest(filter, "/api/orders", "127.0.0.1", null);

        assertThat(firstResponse.getStatus()).isEqualTo(200);
        assertThat(secondResponse.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldReturnTooManyRequestsWhenLimitIsExceeded() throws ServletException, IOException {
        RateLimitFilter filter = new RateLimitFilter(properties(true, 1, 1, 60));

        performRequest(filter, "/api/orders", "127.0.0.1", null);
        MockHttpServletResponse response = performRequest(filter, "/api/orders", "127.0.0.1", null);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).contains("\"status\": 429");
        assertThat(response.getContentAsString()).contains("\"error\": \"Too Many Requests\"");
        assertThat(response.getContentAsString())
                .contains("\"message\": \"Muitas requisições realizadas. Tente novamente em alguns instantes.\"");
        assertThat(response.getContentAsString()).contains("\"path\": \"/api/orders\"");
    }

    @Test
    void shouldIgnoreHealthCheckEndpoint() throws ServletException, IOException {
        RateLimitFilter filter = new RateLimitFilter(properties(true, 1, 1, 60));

        performRequest(filter, "/actuator/health", "127.0.0.1", null);
        MockHttpServletResponse response = performRequest(filter, "/actuator/health", "127.0.0.1", null);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldIgnoreSwaggerEndpoint() throws ServletException, IOException {
        RateLimitFilter filter = new RateLimitFilter(properties(true, 1, 1, 60));

        performRequest(filter, "/docs", "127.0.0.1", null);
        MockHttpServletResponse response = performRequest(filter, "/docs", "127.0.0.1", null);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldUseForwardedForHeaderAsClientKey() throws ServletException, IOException {
        RateLimitFilter filter = new RateLimitFilter(properties(true, 1, 1, 60));

        performRequest(filter, "/api/orders", "127.0.0.1", "10.0.0.1, 10.0.0.2");
        MockHttpServletResponse response = performRequest(filter, "/api/orders", "127.0.0.2", "10.0.0.1");

        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    void shouldNotApplyRateLimitWhenDisabled() throws ServletException, IOException {
        RateLimitFilter filter = new RateLimitFilter(properties(false, 1, 1, 60));

        performRequest(filter, "/api/orders", "127.0.0.1", null);
        MockHttpServletResponse response = performRequest(filter, "/api/orders", "127.0.0.1", null);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    private MockHttpServletResponse performRequest(
            RateLimitFilter filter,
            String path,
            String remoteAddress,
            String forwardedFor
    ) throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setRemoteAddr(remoteAddress);

        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());

        return response;
    }

    private RateLimitProperties properties(
            boolean enabled,
            long capacity,
            long refillTokens,
            long refillDurationSeconds
    ) {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(enabled);
        properties.setCapacity(capacity);
        properties.setRefillTokens(refillTokens);
        properties.setRefillDurationSeconds(refillDurationSeconds);

        return properties;
    }
}
