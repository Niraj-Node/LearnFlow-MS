package com.lms.userservice.grpc;

import com.lms.grpc.GetUserByEmailRequest;
import com.lms.grpc.GetUserByEmailResponse;
import com.lms.grpc.UserServiceGrpc;
import com.lms.userservice.model.User;
import com.lms.userservice.repository.UserRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;

@GrpcService
public class GrpcUserService extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;

    public GrpcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void getUserByEmail(GetUserByEmailRequest request,
                               StreamObserver<GetUserByEmailResponse> responseObserver) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            com.lms.grpc.User grpcUser = com.lms.grpc.User.newBuilder()
                    .setId(user.getId().toString())
                    .setName(user.getName())
                    .setEmail(user.getEmail())
                    .setPassword(user.getPassword())
                    .setRole(user.getRole().toString())
                    .setPhotoUrl(user.getPhotoUrl() != null ? user.getPhotoUrl() : "")
                    .build();

            GetUserByEmailResponse response = GetUserByEmailResponse.newBuilder()
                    .setUser(grpcUser)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("User not found with email: " + request.getEmail())
                    .asRuntimeException());
        }
    }
}