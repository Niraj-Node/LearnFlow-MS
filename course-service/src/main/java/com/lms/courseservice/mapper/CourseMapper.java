package com.lms.courseservice.mapper;

import com.lms.courseservice.dto.response.CourseResponse;
import com.lms.courseservice.model.Course;

public class CourseMapper {

    // Full mapper (used in detailed endpoints)
    public static CourseResponse toFullResponse(Course course, String creatorName, String creatorPhotoUrl) {
        CourseResponse dto = new CourseResponse();
        dto.setId(course.getId());
        dto.setCourseTitle(course.getCourseTitle());
        dto.setSubTitle(course.getSubTitle());
        dto.setDescription(course.getDescription());
        dto.setCategory(course.getCategory());
        dto.setCourseLevel(course.getCourseLevel());
        dto.setCoursePrice(course.getCoursePrice());
        dto.setCourseThumbnail(course.getCourseThumbnail());
        dto.setIsPublished(course.getIsPublished());

        // Dummy values for now; gRPC will populate later
        dto.setCreatorName(creatorName != null ? creatorName : "Demo Creator");
        dto.setCreatorPhotoUrl(creatorPhotoUrl != null ? creatorPhotoUrl : "https://dummy.photo.url");

        return dto;
    }

    // Slim version for listing courses
    public static CourseResponse toSummaryResponse(Course course) {
        CourseResponse dto = new CourseResponse();
        dto.setId(course.getId());
        dto.setCourseTitle(course.getCourseTitle());
        dto.setSubTitle(course.getSubTitle());
        dto.setDescription(course.getDescription());
        dto.setCategory(course.getCategory());
        dto.setCourseLevel(course.getCourseLevel());
        dto.setCoursePrice(course.getCoursePrice());
        dto.setCourseThumbnail(course.getCourseThumbnail());
        dto.setIsPublished(course.getIsPublished());
        dto.setCreatorId(course.getCreatorId());

        return dto;
    }

    public static CourseResponse toCreateCourseResponse(Course course) {
        CourseResponse dto = new CourseResponse();
        dto.setId(course.getId());
        dto.setCourseTitle(course.getCourseTitle());
        dto.setCategory(course.getCategory());
        dto.setCourseLevel(course.getCourseLevel());
        dto.setIsPublished(course.getIsPublished());
        dto.setCreatorId(course.getCreatorId());

        return dto;
    }
}
