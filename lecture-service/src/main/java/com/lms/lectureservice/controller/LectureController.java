package com.lms.lectureservice.controller;

import com.lms.lectureservice.auth.UserContextHolder;
import com.lms.lectureservice.dto.LectureRequest;
import com.lms.lectureservice.dto.LectureResponse;
import com.lms.lectureservice.service.LectureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/lectures")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    @PostMapping("/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    public LectureResponse createLecture(
            @PathVariable UUID courseId,
            @RequestBody @Valid LectureRequest request
    ) {
        UUID currentUserId = UserContextHolder.getCurrentUserId();
        return lectureService.createLecture(currentUserId, courseId, request);
    }

    @PatchMapping("/{lectureId}")
    public LectureResponse editLecture(
            @PathVariable UUID lectureId,
            @RequestBody LectureRequest request
    ) {
        UUID currentUserId = UserContextHolder.getCurrentUserId();
        return lectureService.editLecture(currentUserId, lectureId, request);
    }
}
