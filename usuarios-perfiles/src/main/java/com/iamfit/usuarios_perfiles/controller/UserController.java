package com.iamfit.usuarios_perfiles.controller;

import com.iamfit.usuarios_perfiles.dto.UserProfileDTO;
import com.iamfit.usuarios_perfiles.entity.UserEntity;
import com.iamfit.usuarios_perfiles.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/get-profile")
    public ResponseEntity<UserProfileDTO> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        log.info("Obteniendo perfil para userId: {}", userId);

        return userRepository.findByCredentialId(UUID.fromString(userId))
                .map(user -> ResponseEntity.ok(toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        log.info("Eliminando perfil para userId: {}", userId);

        return userRepository.findByCredentialId(UUID.fromString(userId))
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.<Void>noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private UserProfileDTO toDTO(UserEntity user) {
        return UserProfileDTO.builder()
                .credentialId(user.getCredentialId().toString())
                .nickname(user.getNickname())
                .age(user.getAge())
                .height(user.getHeight())
                .weight(user.getWeight())
                .sex(user.getSex().name())
                .build();
    }
}