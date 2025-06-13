package com.lms.courseservice.grpc;

import com.lms.grpc.UserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {
    @GrpcClient("userService")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceStub() {
        return userStub;
    }
}