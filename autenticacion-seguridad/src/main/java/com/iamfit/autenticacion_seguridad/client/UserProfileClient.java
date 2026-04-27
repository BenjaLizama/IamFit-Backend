package com.iamfit.autenticacion_seguridad.client;

import com.iamfit.grpc.common.UserProfileGrpcServiceGrpc;
import com.iamfit.grpc.common.UserCreatedEvent;
import com.iamfit.grpc.common.UserCreatedResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserProfileClient {

    private final UserProfileGrpcServiceGrpc.UserProfileGrpcServiceBlockingStub stub;

    public UserProfileClient(GrpcChannelFactory channels) {
        this.stub = UserProfileGrpcServiceGrpc.newBlockingStub(channels.createChannel("usuarios-perfiles-service"));
    }

    @PostConstruct
    public void init() {
        log.info("=== Cliente gRPC inicializado correctamente para el canal: usuarios-perfiles-service ===");
    }

    public void sendUserCreatedEvent(String credentialId, String nickname, Integer age , Integer weight, Integer height, String sex) {
        try {
            UserCreatedEvent event = UserCreatedEvent.newBuilder()
                    .setCredentialId(credentialId)
                    .setNickname(nickname)
                    .setAge(age)
                    .setWeight(weight)
                    .setHeight(height)
                    .setSex(sex)
                    .build();

            log.info("Enviando evento gRPC a Perfiles para el usuario: {}", credentialId);
            UserCreatedResponse response = stub.onUserCreated(event);
            log.info("Respuesta de Perfiles: {}", response.getMessage());

        } catch (Exception e) {
            log.error("Error al comunicar con el microservicio de Perfiles: {}", e.getMessage());
        }
    }
}
