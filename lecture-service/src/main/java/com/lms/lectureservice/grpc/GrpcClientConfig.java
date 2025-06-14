package com.lms.lectureservice.grpc;

import com.lms.grpc.CourseServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {
    @GrpcClient("courseService")
    private CourseServiceGrpc.CourseServiceBlockingStub courseStub;

    @Bean
    public CourseServiceGrpc.CourseServiceBlockingStub courseServiceStub() {
        return courseStub;
    }
}