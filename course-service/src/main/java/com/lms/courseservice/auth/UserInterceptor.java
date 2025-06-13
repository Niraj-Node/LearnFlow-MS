package com.lms.courseservice.auth;

import com.lms.courseservice.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.UUID;

@Component
public class UserInterceptor implements HandlerInterceptor {

    private static final List<String> PUBLIC_PATTERNS = List.of(
            "/courses/public/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        boolean isPublic = PUBLIC_PATTERNS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (isPublic) {
            return true;
        }

        String userId = request.getHeader("X-User-Id");
        String roleHeader = request.getHeader("X-Role");
        if (userId == null || roleHeader == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Missing X-User-Id or X-Role header");
            return false;
        }

        try {
            UserContextHolder.setCurrentUserId(UUID.fromString(userId));
            UserContextHolder.setCurrentUserRole(Role.valueOf(roleHeader));
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid UUID format in X-User-Id");
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContextHolder.clear();
    }
}
