package com.lms.userservice.controller;

import com.lms.userservice.auth.UserContextHolder;
import com.lms.userservice.dto.RegisterRequest;
import com.lms.userservice.dto.UserResponse;
import com.lms.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of("success", true, "message", "User service is up and running"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        String msg = userService.register(request);
        return ResponseEntity.status(201).body(Map.of("success", true, "message", msg));
    }


    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
        UUID userId = UserContextHolder.getCurrentUserId();
        UserResponse user = userService.getUserProfile(userId);
        return ResponseEntity.ok(Map.of("success", true, "user", user));
    }

    @PutMapping(value = "/profile/update", consumes = "multipart/form-data")
    public ResponseEntity<?> updateProfile(
            HttpServletRequest request,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) MultipartFile profilePhoto
    ) {
        UUID userId = UserContextHolder.getCurrentUserId();
        UserResponse updatedUser = userService.updateProfile(userId, name, profilePhoto);
        return ResponseEntity.ok(Map.of("success", true, "user", updatedUser));
    }
}

