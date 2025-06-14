package com.lms.lectureservice.service;

import com.lms.lectureservice.dto.LectureRequest;
import com.lms.lectureservice.dto.LectureResponse;
import com.lms.lectureservice.model.Lecture;

import java.util.List;
import java.util.UUID;

public interface LectureService {
    LectureResponse createLecture(UUID currentUserId, UUID courseId, LectureRequest dto);
    LectureResponse editLecture(UUID currentUserId, UUID lectureId, LectureRequest request);
    void deleteLecture(UUID currentUserId, UUID lectureId);
    List<LectureResponse> getLecturesByCourse(UUID userId, UUID courseId);
}
