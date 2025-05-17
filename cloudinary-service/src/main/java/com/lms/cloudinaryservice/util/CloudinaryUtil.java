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
            byte[] imageBytes = Base64.getDecoder().decode(base64.split(",")[1]);
            return cloudinary.uploader().upload(imageBytes, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }
}

