package com.iamfit.usuarios_perfiles.client;

import com.iamfit.grpc.common.UserCreatedEvent;
import com.iamfit.grpc.common.UserCreatedResponse;
import com.iamfit.grpc.common.UserProfileGrpcServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserProfileGrpcService extends UserProfileGrpcServiceGrpc.UserProfileGrpcServiceImplBase {

    @Override
    public void onUserCreated(UserCreatedEvent request, StreamObserver<UserCreatedResponse> responseObserver) {
        log.info("Mensaje gRPC recibido para el usuario ID: {}", request.getCredentialId());

        // Aquí va la lógica para guardar el usuario.
        UserCreatedResponse response = UserCreatedResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Perfil creado exitosamente")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
