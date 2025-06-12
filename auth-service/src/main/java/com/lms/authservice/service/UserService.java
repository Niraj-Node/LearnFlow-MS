package com.lms.authservice.service;

import com.lms.grpc.GetUserByEmailRequest;
import com.lms.grpc.GetUserByEmailResponse;
import com.lms.grpc.User;
import com.lms.grpc.UserServiceGrpc;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    public UserService(UserServiceGrpc.UserServiceBlockingStub userStub) {
        this.userStub = userStub;
    }

    public Optional<User> findByEmail(String email) {
        try {
            GetUserByEmailResponse response = userStub.getUserByEmail(
                    GetUserByEmailRequest.newBuilder().setEmail(email).build()
            );
            return Optional.of(response.getUser());
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed: {}", e.getStatus());
            return Optional.empty();
        }
    }
}
