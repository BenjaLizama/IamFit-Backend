package com.iamfit.usuarios_perfiles.service;

import com.iamfit.grpc.common.UserCreatedEvent;
import com.iamfit.grpc.common.UserCreatedResponse;
import com.iamfit.grpc.common.UserProfileGrpcServiceGrpc;
import com.iamfit.usuarios_perfiles.entity.UserEntity;
import com.iamfit.usuarios_perfiles.enums.SexType;
import com.iamfit.usuarios_perfiles.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserProfileGrpcService extends UserProfileGrpcServiceGrpc.UserProfileGrpcServiceImplBase {

    private final UserRepository userRepository;

    @Override
    public void onUserCreated(UserCreatedEvent request, StreamObserver<UserCreatedResponse> responseObserver) {
        log.info("Mensaje gRPC recibido para el usuario ID: {}", request.getCredentialId());

        // Aquí va la lógica para guardar el usuario.
        try {
            UserEntity newUser = new UserEntity();
            newUser.setCredentialId(UUID.fromString(request.getCredentialId()));
            newUser.setAge(request.getAge());
            newUser.setNickname(request.getNickname());
            newUser.setHeight(request.getHeight());
            newUser.setWeight(request.getWeight());
            String sexStr = request.getSex();

            if (sexStr.equals("MALE") || sexStr.equals("FEMALE")) {
                newUser.setSex(SexType.valueOf(sexStr));
            } else {
                log.warn("Sexo no reconocido: {}. Aplicando valor por defecto", sexStr);
                newUser.setSex(SexType.MALE);
            }

            userRepository.save(newUser);

        } catch (Exception ex) {
            log.error("Error en la conversión de datos: {}", ex.getMessage());
        }

        UserCreatedResponse response = UserCreatedResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Perfil creado exitosamente")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
