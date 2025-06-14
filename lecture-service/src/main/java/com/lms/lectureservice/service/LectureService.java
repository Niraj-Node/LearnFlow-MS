package com.lms.lectureservice.service;

import com.lms.lectureservice.dto.LectureRequest;
import com.lms.lectureservice.dto.LectureResponse;

import java.util.UUID;

public interface LectureService {
    LectureResponse createLecture(UUID currentUserId, UUID courseId, LectureRequest dto);
}
