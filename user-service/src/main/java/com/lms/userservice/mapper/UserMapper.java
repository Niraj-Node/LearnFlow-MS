package com.lms.userservice.mapper;

import com.lms.userservice.dto.UserResponse;
import com.lms.userservice.model.User;

public class UserMapper {
    public static UserResponse toResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setPhotoUrl(user.getPhotoUrl());
        return dto;
    }
}