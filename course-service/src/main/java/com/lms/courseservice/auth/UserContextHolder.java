package com.lms.courseservice.auth;

import java.util.UUID;

public class UserContextHolder {

    private static final ThreadLocal<UUID> currentUserId = new ThreadLocal<>();

    public static UUID getCurrentUserId() {
        return currentUserId.get();
    }

    public static void setCurrentUserId(UUID userId) {
        currentUserId.set(userId);
    }

    public static void clear() {
        currentUserId.remove();
    }
}
