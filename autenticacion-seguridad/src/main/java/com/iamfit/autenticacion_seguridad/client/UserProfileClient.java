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
        var channel = channels.createChannel("usuarios-perfiles-service");
        if (channel == null) {
            throw new IllegalStateException("Canal gRPC 'usuarios-perfiles-service' es null");
        }
        this.stub = UserProfileGrpcServiceGrpc.newBlockingStub(channel);
        log.info("=== Stub gRPC creado: {} ===", this.stub);
    }

    @PostConstruct
    public void init() {
        log.info("=== Cliente gRPC inicializado correctamente para el canal: usuarios-perfiles-service ===");
    }

    public void sendUserCreatedEvent(String credentialId, String nickname,
                                     Integer age, Integer weight,
                                     Integer height, String sex) {
        try {
            log.info("Datos del evento - credentialId: {}, nickname: {}, age: {}, weight: {}, height: {}, sex: {}",
                    credentialId, nickname, age, weight, height, sex);

            if (credentialId == null || nickname == null || age == null ||
                    weight == null || height == null || sex == null) {
                log.error("Uno o más campos son null, no se puede enviar evento gRPC");
                return;
            }

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
            log.error("Error al comunicar con el microservicio de Perfiles: {} - Tipo: {} - Causa: {}",
                    e.getMessage(),
                    e.getClass().getName(),
                    e.getCause() != null ? e.getCause().getMessage() : "sin causa");
        }
    }
}
