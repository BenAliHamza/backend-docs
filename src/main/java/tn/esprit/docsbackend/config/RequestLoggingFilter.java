package tn.esprit.docsbackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Simple logging filter that logs every HTTP request/response
 * with method, path, status, duration and authenticated user.
 *
 * Logs will appear AFTER application startup, whenever endpoints are called.
 */
@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Skip very noisy or irrelevant endpoints if you want
        if (path.startsWith("/actuator")) {
            return true;
        }
        if (path.startsWith("/favicon.ico")) {
            return true;
        }
        if (path.startsWith("/error")) {
            return true;
        }

        // You can also skip static resources if you ever add them
        if (path.startsWith("/css") || path.startsWith("/js") || path.startsWith("/images")) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long start = System.currentTimeMillis();

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String fullPath = (query != null && !query.isBlank()) ? uri + "?" + query : uri;

        // Correlation ID: either take existing header or generate one
        String correlationId = getOrCreateCorrelationId(request, response);

        // Authenticated user info
        String username = getCurrentUsername();

        try {
            // log BEFORE request is processed
            log.debug("REQ [{}] {} {} user={}", correlationId, method, fullPath, username);

            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            int status = response.getStatus();

            // log AFTER request is processed
            log.info(
                    "RES [{}] {} {} -> {} ({} ms) user={}",
                    correlationId,
                    method,
                    fullPath,
                    status,
                    durationMs,
                    username
            );
        }
    }

    private String getOrCreateCorrelationId(HttpServletRequest request, HttpServletResponse response) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // propagate it on response so clients can see/use it
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        return correlationId;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return "anonymous";
        }

        String name = auth.getName();
        if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) {
            return "anonymous";
        }

        return name;
    }
}
