package com.lms.courseservice.auth;

import com.lms.courseservice.enums.Role;

import java.util.UUID;

public class UserContextHolder {

    private static final ThreadLocal<UUID> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<Role> currentUserRole = new ThreadLocal<>();

    public static UUID getCurrentUserId() {
        return currentUserId.get();
    }

    public static void setCurrentUserId(UUID userId) {
        currentUserId.set(userId);
    }

    public static Role getCurrentUserRole() {
        return currentUserRole.get();
    }

    public static void setCurrentUserRole(Role role) {
        currentUserRole.set(role);
    }

    public static void clear() {
        currentUserId.remove();
    }
}
