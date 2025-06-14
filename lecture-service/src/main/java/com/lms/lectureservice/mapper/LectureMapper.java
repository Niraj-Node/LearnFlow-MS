package com.lms.lectureservice.mapper;

import com.lms.lectureservice.dto.LectureResponse;
import com.lms.lectureservice.model.Lecture;

public class LectureMapper {
    public static LectureResponse toResponseDto(Lecture lecture) {
        LectureResponse dto = new LectureResponse();
        dto.setId(lecture.getId());
        dto.setLectureTitle(lecture.getLectureTitle());
        dto.setVideoUrl(lecture.getVideoUrl());
        dto.setPublicId(lecture.getPublicId());
        dto.setIsPreviewFree(lecture.getIsPreviewFree());
        dto.setCourseId(lecture.getCourseId());
        dto.setCreatorId(lecture.getCreatorId());
        return dto;
    }
}
