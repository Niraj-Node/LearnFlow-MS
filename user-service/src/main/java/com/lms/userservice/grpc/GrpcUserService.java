package com.lms.userservice.grpc;

import com.lms.grpc.*;
import com.lms.userservice.model.User;
import com.lms.userservice.repository.UserRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class GrpcUserService extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;

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

    @Override
    public void getUsersByIds(GetUsersByIdsRequest request,
                              StreamObserver<GetUsersByIdsResponse> responseObserver) {
        List<User> users = userRepository.findAllById(
                request.getIdsList().stream().map(UUID::fromString).toList()
        );

        List<SlimUser> grpcUsers = users.stream()
                .map(user -> SlimUser.newBuilder()
                        .setId(user.getId().toString())
                        .setName(user.getName())
                        .setPhotoUrl(user.getPhotoUrl() != null ? user.getPhotoUrl() : "")
                        .build()
                ).toList();

        GetUsersByIdsResponse response = GetUsersByIdsResponse.newBuilder()
                .addAllUsers(grpcUsers)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllUserIds(GetAllUserIdsRequest request,
                              StreamObserver<GetAllUserIdsResponse> responseObserver) {
        List<UUID> userIds = userRepository.findAllUserIds();
        GetAllUserIdsResponse response = GetAllUserIdsResponse.newBuilder()
                .addAllUserIds(userIds.stream()
                        .map(UUID::toString)
                        .toList())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}