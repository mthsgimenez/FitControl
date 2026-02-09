package com.mthsgimenez.fitcontrol.infra.ratelimit;

import com.mthsgimenez.fitcontrol.infra.cache.CacheService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, RateLimitConfig> config = Map.of(
            "/auth/login", new RateLimitConfig(10L, Duration.ofMinutes(1)),
            "/auth/verify-email", new RateLimitConfig(2L, Duration.ofMinutes(1)),
            "/auth/register", new RateLimitConfig(15L, Duration.ofMinutes(5))
    );
    private final CacheService cacheService;

    public RateLimitFilter(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !config.containsKey(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        RateLimitConfig rateLimitConfig = config.get(path);
        String cacheKey = "rl:" + getClientIp(request);

        Long count = cacheService.increment(cacheKey);
        if (count == 1) {
            cacheService.expire(cacheKey, rateLimitConfig.window());
        }

        if (count > rateLimitConfig.limit()) {
            ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS.value());

            Long ttl = cacheService.getTTL(cacheKey);

            problem.setTitle(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
            problem.setDetail("Too many requests sent, retry after: " + ttl + " seconds");
            problem.setInstance(URI.create(request.getRequestURI()));
            problem.setProperty("RetryAfterSeconds", ttl);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Retry-After", ttl.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(problem));

            return;
        }

        doFilter(request, response, filterChain);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

}
