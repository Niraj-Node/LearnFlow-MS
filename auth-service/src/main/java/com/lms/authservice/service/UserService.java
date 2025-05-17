package com.lms.authservice.service;

import com.lms.grpc.GetUserByEmailRequest;
import com.lms.grpc.GetUserByEmailResponse;
import com.lms.grpc.User;
import com.lms.grpc.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    public UserService(@Value("${user.service.address}") String serverAddress,
                       @Value("${user.service.grpc.port}") int serverPort) {
        log.info("Connecting to Billing Service GRPC service at {}:{}", serverAddress, serverPort);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress,
                serverPort).usePlaintext().build();
        userStub = UserServiceGrpc.newBlockingStub(channel);
    }

    public Optional<User> findByEmail(String email) {
        try {
            GetUserByEmailResponse response = userStub.getUserByEmail(
                    GetUserByEmailRequest.newBuilder().setEmail(email).build()
            );

            com.lms.grpc.User grpcUser = response.getUser();

            return Optional.of(grpcUser);

        } catch (StatusRuntimeException e) {
            return Optional.empty(); // user not found or grpc failed
        }
    }
}