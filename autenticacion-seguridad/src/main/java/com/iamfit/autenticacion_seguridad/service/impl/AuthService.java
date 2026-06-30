package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.client.UserProfileClient;
import com.iamfit.autenticacion_seguridad.dto.AuthResponse;
import com.iamfit.autenticacion_seguridad.dto.LoginWrapper;
import com.iamfit.autenticacion_seguridad.dto.RegisterWrapper;
import com.iamfit.autenticacion_seguridad.dto.SessionResponse;
import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.entity.RoleEntity;
import com.iamfit.autenticacion_seguridad.exception.EmailAlreadyExistsException;
import com.iamfit.autenticacion_seguridad.exception.RoleNotFoundException;
import com.iamfit.autenticacion_seguridad.repository.CredentialRepository;
import com.iamfit.autenticacion_seguridad.repository.RoleRepository;
import com.iamfit.autenticacion_seguridad.security.SecurityCredential;
import com.iamfit.autenticacion_seguridad.service.IAuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final CredentialRepository credentialRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthStrategyManager authStrategyManager;
    private final SessionService sessionService;
    private final UserProfileClient userProfileClient;

    /**
     * Inicio de sesión
     */
    @Override
    @Transactional
    public AuthResponse login(LoginWrapper wrapper) {
        // 1. La estrategia valida según el provider y nos da la entidad
        CredentialEntity credential = authStrategyManager.executeStrategy(wrapper.login());

        // 2. Convertimos a SecurityCredential para el JWT (como ya lo hacías)
        SecurityCredential userDetails = new SecurityCredential(credential);

        // 3. Generamos tokens y sesión
        String accessToken = jwtService.generateToken(userDetails);
        SessionResponse session = sessionService.createSession(credential.getId(), wrapper.session());

        return new AuthResponse(accessToken, session.rawRefreshToken(), session.expiryDate());
    }

    /**
     * Registro de usuario
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterWrapper request) {
        log.info("Iniciando proceso de registro para el email: {}", request.register().email());

        if (credentialRepository.existsByEmail(request.register().email())) {
            throw new EmailAlreadyExistsException("El correo electrónico ya está registrado");
        }

        RoleEntity defaultRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("Error crítico: ROLE_USER no existe."));

        CredentialEntity credential = new CredentialEntity();
        credential.setEmail(request.register().email().toLowerCase().trim());
        credential.setPassword(passwordEncoder.encode(request.register().password()));
        credential.setIsActive(true);
        credential.setRoleList(Set.of(defaultRole));

        CredentialEntity savedCredential = credentialRepository.save(credential);

        // --- Integración gRPC ---
        userProfileClient.sendUserCreatedEvent(
                savedCredential.getId().toString(),
                request.userProfile().nickname(),
                request.userProfile().age(),
                request.userProfile().weight(),
                request.userProfile().height(),
                request.userProfile().sex()
        );

        SecurityCredential userDetails = new SecurityCredential(savedCredential);

        String accessToken = jwtService.generateToken(userDetails);

        // Crear nueva sesión
        SessionResponse session = sessionService.createSession(savedCredential.getId(), request.session());

        return new AuthResponse(accessToken, session.rawRefreshToken(), session.expiryDate());
    }

}
