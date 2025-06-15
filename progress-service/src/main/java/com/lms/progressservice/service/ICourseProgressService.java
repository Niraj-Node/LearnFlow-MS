package com.lms.progressservice.service;

import java.util.UUID;

public interface ICourseProgressService {
    void updateLectureProgress(UUID courseId, UUID lectureId, UUID userId);
}
