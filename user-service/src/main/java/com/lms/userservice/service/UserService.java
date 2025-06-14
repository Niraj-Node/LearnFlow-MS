package com.lms.userservice.service;

import com.lms.userservice.dto.RegisterRequest;
import com.lms.userservice.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface UserService {
    String register(RegisterRequest request);
    UserResponse getUserProfile(UUID id);
    UserResponse updateProfile(UUID id, String name, MultipartFile profilePhoto);
    void enrollInCourse(UUID userId, UUID courseId);
}
