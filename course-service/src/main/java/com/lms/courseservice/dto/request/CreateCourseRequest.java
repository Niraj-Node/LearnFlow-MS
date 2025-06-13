package com.lms.courseservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCourseRequest {

    @NotBlank(message = "Course title is required")
    private String courseTitle;

    @NotBlank(message = "Category is required")
    private String category;
}
