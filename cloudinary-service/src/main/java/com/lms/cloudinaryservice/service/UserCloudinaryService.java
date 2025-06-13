package com.lms.cloudinaryservice.service;

public interface UserCloudinaryService {
    String handleUserPhotoUpdate(String userId, String oldUrl, byte[] imageBytes);
}