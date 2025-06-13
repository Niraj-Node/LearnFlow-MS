package com.lms.cloudinaryservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryService {
    Map<String, Object> uploadVideo(MultipartFile file);
}
