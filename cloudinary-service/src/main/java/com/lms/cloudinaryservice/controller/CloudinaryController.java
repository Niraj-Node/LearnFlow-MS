package com.lms.cloudinaryservice.controller;

import com.lms.cloudinaryservice.auth.UserContextHolder;
import com.lms.cloudinaryservice.enums.Role;
import com.lms.cloudinaryservice.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
public class CloudinaryController {
    private final CloudinaryService cloudinaryService;

    @PostMapping("/video-upload")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        Role role = UserContextHolder.getCurrentUserRole();
        if (role != Role.INSTRUCTOR) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Forbidden: Only instructors can upload videos"
            ));
        }

        Map<String, Object> result = cloudinaryService.uploadVideo(file);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Video uploaded successfully",
                "data", result
        ));
    }
}
