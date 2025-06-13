package com.lms.cloudinaryservice.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.util.Base64;
import java.util.Map;

public class CloudinaryUtil {

    public static String extractPublicId(String url) {
        String[] parts = url.split("/");
        String last = parts[parts.length - 1];
        return last.split("\\.")[0];
    }

    public static void deleteImage(Cloudinary cloudinary, String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image from Cloudinary", e);
        }
    }

    public static Map uploadImage(Cloudinary cloudinary, String base64) {
        try {
            // If base64 includes a data URI prefix, remove it
            String pureBase64 = base64.contains(",")
                    ? base64.split(",")[1]
                    : base64;

            byte[] imageBytes = Base64.getDecoder().decode(pureBase64);

            // Tell Cloudinary it's a raw image upload (not from file path/URL)
            Map<String, Object> options = ObjectUtils.asMap("resource_type", "image");

            return cloudinary.uploader().upload(imageBytes, options);

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }

    public static Map uploadVideo(Cloudinary cloudinary, byte[] videoBytes) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "resource_type", "video"
            );
            return cloudinary.uploader().upload(videoBytes, options);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload video to Cloudinary", e);
        }
    }

}

