package com.lms.cloudinaryservice.service.impl;

import com.cloudinary.Cloudinary;
import com.lms.cloudinaryservice.kafka.KafkaProducer;
import com.lms.cloudinaryservice.service.UserCloudinaryService;
import com.lms.cloudinaryservice.util.CloudinaryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserCloudinaryServiceImpl implements UserCloudinaryService {

    private final Cloudinary cloudinary;
    private final KafkaProducer kafkaProducer;

    @Override
    public void handleUserPhotoUpdate(String userId, String oldUrl, byte[] imageBytes) {
        try {
            if (oldUrl != null && !oldUrl.isEmpty()) {
                String publicId = CloudinaryUtil.extractPublicId(oldUrl);
                CloudinaryUtil.deleteImage(cloudinary, publicId);
            }

            // Convert byte[] to base64 String
            String newBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
            Map uploadResult = CloudinaryUtil.uploadImage(cloudinary, newBase64);
            String newUrl = uploadResult.get("secure_url").toString();

            System.out.println("Uploaded New Photo to Cloudinary: " + newUrl);

            // Emit success event using new Protobuf message
            kafkaProducer.produceUserPhotoUploadCompletedEvent(userId, newUrl);

        } catch (Exception e) {
            throw new RuntimeException("Photo update failed in Cloudinary", e);
        }
    }
}
