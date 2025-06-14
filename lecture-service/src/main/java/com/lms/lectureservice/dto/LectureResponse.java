package com.lms.lectureservice.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Setter
@Getter

public class LectureResponse {
    private UUID id;
    private String lectureTitle;
    private String videoUrl;
    private String publicId;
    private Boolean isPreviewFree;
    private UUID courseId;
    private UUID creatorId;
}
