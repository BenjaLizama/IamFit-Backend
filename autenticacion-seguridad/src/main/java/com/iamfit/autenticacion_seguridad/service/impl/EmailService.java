package com.iamfit.autenticacion_seguridad.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp, int expirationMinutes) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("IAMFIT — Código de verificación");
            message.setText(String.format("""
                Hola,
                
                Tu código de verificación para restablecer tu contraseña es:
                
                %s
                
                Este código expira en %d minutos.
                
                Si no solicitaste este código, ignora este mensaje.
                
                El equipo IAMFIT
                """, otp, expirationMinutes));

            mailSender.send(message);
            log.info("[EMAIL] OTP enviado correctamente a: {}", toEmail);

        } catch (Exception e) {
            log.error("[EMAIL] Error enviando OTP a {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Error al enviar el correo. Intenta nuevamente.");
        }
    }
}