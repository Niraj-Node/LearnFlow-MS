package com.lms.cloudinaryservice.service;

public interface CourseCloudinaryService {
    String handleCourseThumbnailUpdate(String oldUrl, byte[] imageBytes);
}
