package com.lms.cloudinaryservice.service.impl;

import com.cloudinary.Cloudinary;
import com.lms.cloudinaryservice.exception.BadRequestException;
import com.lms.cloudinaryservice.exception.CloudinaryUploadException;
import com.lms.cloudinaryservice.exception.FileProcessingException;
import com.lms.cloudinaryservice.service.CloudinaryService;
import com.lms.cloudinaryservice.util.CloudinaryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public Map uploadVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is missing or empty");
        }

        try {
            byte[] videoBytes = StreamUtils.copyToByteArray(file.getInputStream());
            return CloudinaryUtil.uploadVideo(cloudinary, videoBytes);
        } catch (IOException e) {
            throw new FileProcessingException("Error reading uploaded file", e);
        } catch (Exception e) {
            throw new CloudinaryUploadException("Video upload failed", e);
        }
    }
}
