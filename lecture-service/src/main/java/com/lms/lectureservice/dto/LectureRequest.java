package com.lms.lectureservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LectureRequest {
    @NotBlank(message = "Lecture title is required")
    private String lectureTitle;

    private String videoUrl;
    private String publicId;
    private Boolean isPreviewFree;
}
