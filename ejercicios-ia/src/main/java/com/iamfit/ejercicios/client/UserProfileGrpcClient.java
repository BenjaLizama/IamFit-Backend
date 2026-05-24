package com.iamfit.ejercicios.client;

import com.iamfit.grpc.common.GetUserProfileRequest;
import com.iamfit.grpc.common.UserProfileGrpcServiceGrpc;
import com.iamfit.grpc.common.UserProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "iamfit.grpc.usuarios-perfiles.enabled", havingValue = "true")
public class UserProfileGrpcClient {

    private final UserProfileGrpcServiceGrpc.UserProfileGrpcServiceBlockingStub stub;

    public UserProfileGrpcClient(GrpcChannelFactory channels) {
        var channel = channels.createChannel("usuarios-perfiles-service");
        this.stub = UserProfileGrpcServiceGrpc.newBlockingStub(channel);
        log.info(">>> gRPC client inicializado para usuarios-perfiles-service");
    }

    public UserProfileResponse getUserProfile(String credentialId) {
        try {
            var request = GetUserProfileRequest.newBuilder()
                    .setCredentialId(credentialId)
                    .build();
            log.debug("Consultando perfil gRPC para credentialId: {}", credentialId);
            return stub.getUserProfile(request);
        } catch (Exception e) {
            log.error("Error obteniendo perfil: {}", e.getMessage());
            throw new RuntimeException("Error comunicando con usuarios-perfiles", e);
        }
    }
}