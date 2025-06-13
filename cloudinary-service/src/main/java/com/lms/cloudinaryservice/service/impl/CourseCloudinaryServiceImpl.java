package com.lms.cloudinaryservice.service.impl;

import com.cloudinary.Cloudinary;
import com.lms.cloudinaryservice.service.CourseCloudinaryService;
import com.lms.cloudinaryservice.util.CloudinaryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseCloudinaryServiceImpl implements CourseCloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String handleCourseThumbnailUpdate(String oldUrl, byte[] imageBytes) {
        try {
            if (oldUrl != null && !oldUrl.isEmpty()) {
                String publicId = CloudinaryUtil.extractPublicId(oldUrl);
                CloudinaryUtil.deleteImage(cloudinary, publicId);
            }

            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            Map uploadResult = CloudinaryUtil.uploadImage(cloudinary, base64);
            String newUrl = uploadResult.get("secure_url").toString();
            System.out.println("Uploaded New Thumbnail to Cloudinary: " + newUrl);
            return newUrl;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update course thumbnail", e);
        }
    }
}
