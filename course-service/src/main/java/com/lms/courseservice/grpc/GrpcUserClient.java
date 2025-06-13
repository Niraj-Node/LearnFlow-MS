package com.lms.courseservice.grpc;

import com.lms.grpc.GetUsersByIdsRequest;
import com.lms.grpc.GetUsersByIdsResponse;
import com.lms.grpc.UserServiceGrpc;
import io.grpc.StatusRuntimeException;
import com.lms.grpc.SlimUser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrpcUserClient {

    private static final Logger log = LoggerFactory.getLogger(GrpcUserClient.class);
    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    public Map<UUID, SlimUser> getUsersByIds(List<UUID> ids) {
        GetUsersByIdsRequest request = GetUsersByIdsRequest.newBuilder()
                .addAllIds(ids.stream().map(UUID::toString).toList())
                .build();

        try {
            GetUsersByIdsResponse response = userStub.getUsersByIds(request);
            return response.getUsersList().stream()
                    .collect(Collectors.toMap(
                            user -> UUID.fromString(user.getId()),
                            user -> user
                    ));
        } catch (StatusRuntimeException e) {
            log.error("gRPC call to getUsersByIds failed: {}", e.getStatus(), e);
            return Collections.emptyMap();
        }
    }
}

