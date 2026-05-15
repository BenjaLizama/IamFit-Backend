package com.iamfit.alimentacion.client;

import com.iamfit.grpc.common.GetUserProfileRequest;
import com.iamfit.grpc.common.UserProfileGrpcServiceGrpc;
import com.iamfit.grpc.common.UserProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserProfileGrpcClient {

    private final UserProfileGrpcServiceGrpc.UserProfileGrpcServiceBlockingStub stub;

    public UserProfileGrpcClient(GrpcChannelFactory channels) {
        var channel = channels.createChannel("usuarios-perfiles-service");
        this.stub = UserProfileGrpcServiceGrpc.newBlockingStub(channel);
    }

    public UserProfileResponse getUserProfile(String credentialId) {
        try {
            var request = GetUserProfileRequest.newBuilder()
                    .setCredentialId(credentialId)
                    .build();
            return stub.getUserProfile(request);
        } catch (Exception e) {
            log.error("Error obteniendo perfil: {}", e.getMessage());
            throw new RuntimeException("Error comunicando con usuarios-perfiles", e);
        }
    }
}
