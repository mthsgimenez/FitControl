package com.mthsgimenez.fitcontrol.filter;

import com.mthsgimenez.fitcontrol.dto.RateLimitDTO;
import com.mthsgimenez.fitcontrol.service.CacheService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
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

    private final Map<String, RateLimitDTO> config = Map.of(
            "/auth/login", new RateLimitDTO(10L, Duration.ofMinutes(1)),
            "/auth/verify-email", new RateLimitDTO(2L, Duration.ofMinutes(1)),
            "/auth/register", new RateLimitDTO(15L, Duration.ofMinutes(5))
    );
    private final CacheService cacheService;
    private final MessageSource messageSource;

    public RateLimitFilter(CacheService cacheService, MessageSource messageSource) {
        this.cacheService = cacheService;
        this.messageSource = messageSource;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !config.containsKey(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        RateLimitDTO rateLimitDTO = config.get(path);
        String cacheKey = "rl:" + getClientIp(request);

        Long count = cacheService.increment(cacheKey);
        if (count == 1) {
            cacheService.expire(cacheKey, rateLimitDTO.window());
        }

        if (count > rateLimitDTO.limit()) {
            ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS.value());

            Long ttl = cacheService.getTTL(cacheKey);

            problem.setTitle(messageSource.getMessage("problem.too-many-requests.title", null, request.getLocale()));
            problem.setDetail(messageSource.getMessage("problem.too-many-requests.detail", new Object[]{ttl}, request.getLocale()));
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
