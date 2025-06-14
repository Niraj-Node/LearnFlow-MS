package com.lms.cloudinaryservice.auth;

import com.lms.cloudinaryservice.enums.Role;

public class UserContextHolder {

    private static final ThreadLocal<Role> currentUserRole = new ThreadLocal<>();

    public static Role getCurrentUserRole() {
        return currentUserRole.get();
    }

    public static void setCurrentUserRole(Role role) {
        currentUserRole.set(role);
    }

    public static void clear() {
        currentUserRole.remove();
    }
}
