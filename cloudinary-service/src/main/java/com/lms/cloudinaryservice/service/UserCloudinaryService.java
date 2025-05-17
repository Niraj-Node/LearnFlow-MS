package com.lms.cloudinaryservice.service;

public interface UserCloudinaryService {
    void handleUserPhotoUpdate(String userId, String oldUrl, String newBase64);
}