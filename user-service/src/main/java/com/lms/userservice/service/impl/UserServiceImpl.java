package com.lms.userservice.service.impl;

import com.lms.userservice.dto.RegisterRequest;
import com.lms.userservice.dto.UserResponse;
import com.lms.userservice.enums.Role;
import com.lms.userservice.exception.ResourceNotFoundException;
import com.lms.userservice.exception.UserAlreadyExistsException;
import com.lms.userservice.kafka.KafkaProducer;
import com.lms.userservice.mapper.UserMapper;
import com.lms.userservice.model.User;
import com.lms.userservice.enums.UserEventType;
import com.lms.userservice.repository.UserRepository;
import com.lms.userservice.service.UserService;
import com.lms.userservice.util.PasswordUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final KafkaProducer kafkaProducer;

    @Override
    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists with this email.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(PasswordUtil.hashPassword(request.getPassword()));
//        user.setRole(Role.STUDENT);
        userRepository.save(user);

        return "Account created successfully.";
    }

    @Override
    public UserResponse getUserProfile(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID id, String name, MultipartFile profilePhoto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (name != null && !name.isBlank()) user.setName(name);

        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            try {
                // Convert MultipartFile to base64 string
                String base64Image = "data:" + profilePhoto.getContentType() + ";base64," +
                        Base64.getEncoder().encodeToString(profilePhoto.getBytes());

                // Send Kafka event to Cloudinary microservice
                kafkaProducer.produceUserEvent(
                        user.getId().toString(),
                        user.getPhotoUrl() != null ? user.getPhotoUrl() : "",
                        base64Image,
                        UserEventType.USER_PHOTO_UPDATED
                );

            } catch (Exception e) {
                throw new RuntimeException("Failed to process photo", e);
            }
        }

        // Save user without photo update (it will be updated after confirmation)
        return UserMapper.toResponse(userRepository.save(user));
    }
}
