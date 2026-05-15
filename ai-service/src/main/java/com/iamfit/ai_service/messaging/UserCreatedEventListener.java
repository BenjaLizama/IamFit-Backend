package com.iamfit.ai_service.messaging;


import com.iamfit.ai_service.client.UserProfileGrpcClient;
import com.iamfit.ai_service.dto.UserCreatedEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedEventListener {

    private final UserProfileGrpcClient userProfileGrpcClient;

    @RabbitListener(queues = "${iamfit.rabbitmq.queues.user-created}")
    public void onUserCreated(@Payload UserCreatedEventDTO event) {
        log.info("Evento user-created recibido para credentialId: {}",
                event.getCredentialId());

        var response = userProfileGrpcClient.notifyUserCreated(
                event.getCredentialId(),
                event.getNickname(),
                event.getAge(),
                event.getHeight(),
                event.getWeight(),
                event.getSex()
        );

        log.info("Respuesta gRPC: success={} message={}",
                response.getSuccess(), response.getMessage());
    }
}