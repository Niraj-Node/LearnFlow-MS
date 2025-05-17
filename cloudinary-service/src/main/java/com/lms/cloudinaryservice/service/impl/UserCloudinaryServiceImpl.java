package com.lms.cloudinaryservice.service.impl;

import com.cloudinary.Cloudinary;
import com.lms.cloudinaryservice.kafka.KafkaProducer;
import com.lms.cloudinaryservice.model.UserEventType;
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
    public void handleUserPhotoUpdate(String userId, String oldUrl, String newBase64) {
        try {
            if (oldUrl != null && !oldUrl.isEmpty()) {
                String publicId = CloudinaryUtil.extractPublicId(oldUrl);
                CloudinaryUtil.deleteImage(cloudinary, publicId);
            }

            Map uploadResult = CloudinaryUtil.uploadImage(cloudinary, newBase64);
            String newUrl = uploadResult.get("secure_url").toString();

            System.out.println("Uploaded New Photo to Cloudinary: " + newUrl);
            // Emit a success event with newUrl
            kafkaProducer.produceUserEvent(userId, newUrl,UserEventType.USER_PHOTO_UPLOAD_COMPLETED);

        } catch (Exception e) {
            throw new RuntimeException("Photo update failed in Cloudinary", e);
        }
    }
}
