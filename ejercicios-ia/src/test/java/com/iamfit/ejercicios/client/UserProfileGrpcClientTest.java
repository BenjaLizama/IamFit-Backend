package com.iamfit.ejercicios.client;

import com.iamfit.grpc.common.GetUserProfileRequest;
import com.iamfit.grpc.common.UserProfileGrpcServiceGrpc;
import com.iamfit.grpc.common.UserProfileResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.grpc.client.GrpcChannelFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserProfileGrpcClientTest {

    private GrpcChannelFactory grpcChannelFactory;
    private io.grpc.Server inProcessServer;
    private UserProfileGrpcClient userProfileGrpcClient;
    private final String serverName = InProcessServerBuilder.generateName();

    // Variables de control para manejar las respuestas del servidor simulado de forma segura
    private final AtomicReference<UserProfileResponse> nextResponse = new AtomicReference<>();
    private final AtomicReference<Throwable> nextError = new AtomicReference<>();
    private final AtomicReference<GetUserProfileRequest> capturedRequest = new AtomicReference<>();

    // Implementación manual ultraligera del servicio gRPC para evitar fallos de agentes de Mockito en Java 21
    private final UserProfileGrpcServiceGrpc.UserProfileGrpcServiceImplBase serviceImpl =
            new UserProfileGrpcServiceGrpc.UserProfileGrpcServiceImplBase() {
                @Override
                public void getUserProfile(GetUserProfileRequest request, StreamObserver<UserProfileResponse> responseObserver) {
                    capturedRequest.set(request); // Guardamos la petición que llegó para verificarla después
                    if (nextError.get() != null) {
                        responseObserver.onError(nextError.get());
                    } else if (nextResponse.get() != null) {
                        responseObserver.onNext(nextResponse.get());
                        responseObserver.onCompleted();
                    } else {
                        responseObserver.onError(Status.UNIMPLEMENTED.asRuntimeException());
                    }
                }
            };

    @BeforeEach
    void setUp() throws IOException {
        grpcChannelFactory = mock(GrpcChannelFactory.class);

        // Limpiamos los estados de control antes de cada test
        nextResponse.set(null);
        nextError.set(null);
        capturedRequest.set(null);

        // Levantamos el servidor en memoria con nuestro mock nativo
        inProcessServer = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(serviceImpl)
                .build()
                .start();

        var channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
        when(grpcChannelFactory.createChannel("usuarios-perfiles-service")).thenReturn(channel);

        userProfileGrpcClient = new UserProfileGrpcClient(grpcChannelFactory);
    }

    @AfterEach
    void tearDown() {
        if (inProcessServer != null) {
            inProcessServer.shutdownNow();
        }
    }

    @Test
    @DisplayName("Debería retornar UserProfileResponse con datos correctos desde el Proto")
    void getUserProfile_Success() {
        // GIVEN
        String credentialId = "cred-123";
        UserProfileResponse mockResponse = UserProfileResponse.newBuilder()
                .setCredentialId(credentialId)
                .setNickname("GymBro99")
                .setAge(25)
                .setHeight(180)
                .setWeight(75)
                .setSex("M")
                .setFound(true)
                .build();

        nextResponse.set(mockResponse); // Configuramos la respuesta esperada

        // WHEN
        UserProfileResponse result = userProfileGrpcClient.getUserProfile(credentialId);

        // THEN
        assertNotNull(result);
        assertTrue(result.getFound());
        assertEquals("GymBro99", result.getNickname());
        assertEquals(25, result.getAge());

        // Verificamos de forma segura la petición recibida por el servidor
        assertNotNull(capturedRequest.get());
        assertEquals(credentialId, capturedRequest.get().getCredentialId());
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException si gRPC falla con un error de estatus")
    void getUserProfile_Failure() {
        // GIVEN
        String credentialId = "cred-error";
        nextError.set(Status.INTERNAL.withDescription("Internal Database Error").asRuntimeException());

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userProfileGrpcClient.getUserProfile(credentialId);
        });

        assertTrue(exception.getMessage().contains("Error comunicando con usuarios-perfiles"));
    }
}