package com.lms.courseservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class EditCourseRequest {

    @NotBlank(message = "Course title is required")
    private String courseTitle;

    @NotBlank(message = "Subtitle is required")
    private String subTitle;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Course level is required")
    private String courseLevel;

    @NotNull(message = "Course price is required")
    @PositiveOrZero(message = "Course price must be zero or positive")
    private Double coursePrice;

    private MultipartFile courseThumbnail;
}