package com.lms.courseservice.dto.response;

import com.lms.courseservice.enums.CourseLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CourseResponse {
    private UUID id;

    private String courseTitle;
    private String subTitle;
    private String description;
    private String category;
    private CourseLevel courseLevel;
    private Double coursePrice;
    private String courseThumbnail;
    private Boolean isPublished;

    private UUID creatorId;
    private String creatorName;
    private String creatorPhotoUrl;
}
