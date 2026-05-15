package com.iamfit.ai_service.client;

import com.iamfit.grpc.common.GetUserProfileRequest;
import com.iamfit.grpc.common.UserCreatedEvent;
import com.iamfit.grpc.common.UserCreatedResponse;
import com.iamfit.grpc.common.UserProfileGrpcServiceGrpc;
import com.iamfit.grpc.common.UserProfileResponse;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserProfileGrpcClient {

    @GrpcClient("usuarios-perfiles")
    private UserProfileGrpcServiceGrpc.UserProfileGrpcServiceBlockingStub stub;

    public UserCreatedResponse notifyUserCreated(String credentialId, String nickname,
                                                 int age, int height, int weight, String sex) {
        try {
            UserCreatedEvent event = UserCreatedEvent.newBuilder()
                    .setCredentialId(credentialId)
                    .setNickname(nickname)
                    .setAge(age)
                    .setHeight(height)
                    .setWeight(weight)
                    .setSex(sex)
                    .build();

            log.info("Enviando evento gRPC OnUserCreated para credentialId: {}", credentialId);
            return stub.onUserCreated(event);

        } catch (Exception e) {
            log.error("Error en gRPC OnUserCreated: {}", e.getMessage());
            throw new RuntimeException("Error comunicando con usuarios-perfiles", e);
        }
    }

    // ← nuevo método
    public UserProfileResponse getUserProfile(String credentialId) {
        try {
            GetUserProfileRequest request = GetUserProfileRequest.newBuilder()
                    .setCredentialId(credentialId)
                    .build();

            log.info("Consultando perfil gRPC para credentialId: {}", credentialId);
            return stub.getUserProfile(request);

        } catch (Exception e) {
            log.error("Error en gRPC GetUserProfile: {}", e.getMessage());
            throw new RuntimeException("Error obteniendo perfil de usuarios-perfiles", e);
        }
    }
}