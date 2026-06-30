package com.iamfit.usuarios_perfiles.service;

import com.iamfit.grpc.common.GetUserProfileRequest;
import com.iamfit.grpc.common.UserCreatedEvent;
import com.iamfit.grpc.common.UserCreatedResponse;
import com.iamfit.grpc.common.UserProfileGrpcServiceGrpc;
import com.iamfit.grpc.common.UserProfileResponse;
import com.iamfit.usuarios_perfiles.entity.UserEntity;
import com.iamfit.usuarios_perfiles.enums.SexType;
import com.iamfit.usuarios_perfiles.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserProfileGrpcService extends UserProfileGrpcServiceGrpc.UserProfileGrpcServiceImplBase {

    private final UserRepository userRepository;

    @Override
    public void onUserCreated(UserCreatedEvent request, StreamObserver<UserCreatedResponse> responseObserver) {
        log.info("Mensaje gRPC recibido para el usuario ID: {}", request.getCredentialId());

        try {
            UserEntity newUser = new UserEntity();
            newUser.setCredentialId(UUID.fromString(request.getCredentialId()));
            newUser.setAge(request.getAge());
            newUser.setNickname(request.getNickname());
            newUser.setHeight(request.getHeight());
            newUser.setWeight(request.getWeight());
            String sexStr = request.getSex();
            newUser.setSex(mapSexType(sexStr));

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

    @Override
    public void getUserProfile(GetUserProfileRequest request,
                               StreamObserver<UserProfileResponse> responseObserver) {
        log.info("Consulta gRPC GetUserProfile para credentialId: {}", request.getCredentialId());

        try {
            UUID credentialId = UUID.fromString(request.getCredentialId());
            Optional<UserEntity> userOpt = userRepository.findByCredentialId(credentialId);

            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                UserProfileResponse response = UserProfileResponse.newBuilder()
                        .setCredentialId(user.getCredentialId().toString())
                        .setNickname(user.getNickname())
                        .setAge(user.getAge())
                        .setHeight(user.getHeight())
                        .setWeight(user.getWeight())
                        .setSex(user.getSex().name())
                        .setFound(true)
                        .build();

                responseObserver.onNext(response);
            } else {
                log.warn("Usuario no encontrado para credentialId: {}", request.getCredentialId());
                UserProfileResponse response = UserProfileResponse.newBuilder()
                        .setFound(false)
                        .build();
                responseObserver.onNext(response);
            }

        } catch (Exception ex) {
            log.error("Error en GetUserProfile: {}", ex.getMessage());
            UserProfileResponse response = UserProfileResponse.newBuilder()
                    .setFound(false)
                    .build();
            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }
    private SexType mapSexType(String sexStr) {
        if (sexStr == null) return SexType.MALE;
        return switch (sexStr.toUpperCase().trim()) {
            case "MALE", "M", "MASCULINO", "HOMBRE" -> SexType.MALE;
            case "FEMALE", "F", "FEMENINO", "MUJER" -> SexType.FEMALE;
            default -> {
                log.warn("Sexo no reconocido: {}. Aplicando MALE por defecto", sexStr);
                yield SexType.MALE;
            }
        };
    }
}