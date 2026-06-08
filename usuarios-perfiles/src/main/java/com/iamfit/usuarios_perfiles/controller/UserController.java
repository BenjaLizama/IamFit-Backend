package com.iamfit.usuarios_perfiles.controller;

import com.iamfit.usuarios_perfiles.dto.ProfileContextDTO;
import com.iamfit.usuarios_perfiles.dto.UpdateProfileRequest;
import com.iamfit.usuarios_perfiles.dto.UserProfileDTO;
import com.iamfit.usuarios_perfiles.entity.UserEntity;
import com.iamfit.usuarios_perfiles.repository.UserRepository;
import com.iamfit.usuarios_perfiles.service.ProfileContextService;
import jakarta.validation.Valid;
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
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final ProfileContextService profileContextService;

    // ─── Obtener perfil ──────────────────────────────────────────────
    @GetMapping("/get-profile")
    public ResponseEntity<UserProfileDTO> getProfile(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        log.info("Obteniendo perfil para userId: {}", userId);

        return userRepository.findByCredentialId(UUID.fromString(userId))
                .map(user -> ResponseEntity.ok(toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Actualizar perfil ───────────────────────────────────────────
    @PatchMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        log.info("Actualizando perfil para userId: {}", userId);

        return userRepository.findByCredentialId(UUID.fromString(userId))
                .map(user -> {
                    if (request.getNickname() != null) user.setNickname(request.getNickname());
                    if (request.getAge() != null) user.setAge(request.getAge());
                    if (request.getSex() != null) user.setSex(request.getSex());
                    if (request.getHeight() != null) user.setHeight(request.getHeight());
                    if (request.getWeight() != null) user.updateWeight(request.getWeight());
                    if (request.getGoal() != null) user.setGoal(request.getGoal());
                    if (request.getActivityLevel() != null) user.setActivityLevel(request.getActivityLevel());
                    if (request.getDietaryPreferences() != null) user.setDietaryPreferences(request.getDietaryPreferences());
                    if (request.getAllergies() != null) user.setAllergies(request.getAllergies());
                    if (request.getDislikes() != null) user.setDislikes(request.getDislikes());
                    if (request.getAvailableEquipment() != null) user.setAvailableEquipment(request.getAvailableEquipment());
                    if (request.getLimitations() != null) user.setLimitations(request.getLimitations());
                    return ResponseEntity.ok(toDTO(userRepository.save(user)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Contexto completo ───────────────────────────────────────────
    @GetMapping("/profile/context")
    public ResponseEntity<ProfileContextDTO> getProfileContext(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwt.getClaim("userId");
        String token = authHeader.replace("Bearer ", "");

        return userRepository.findByCredentialId(UUID.fromString(userId))
                .map(user -> {
                    UserProfileDTO profile = toDTO(user);
                    ProfileContextDTO context = profileContextService.buildContext(profile, token);
                    return ResponseEntity.ok(context);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Eliminar perfil ─────────────────────────────────────────────
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

    // ─── Mapper ──────────────────────────────────────────────────────
    private UserProfileDTO toDTO(UserEntity user) {
        return UserProfileDTO.builder()
                .credentialId(user.getCredentialId().toString())
                .nickname(user.getNickname())
                .age(user.getAge())
                .height(user.getHeight())
                .weight(user.getWeight())
                .sex(user.getSex() != null ? user.getSex().name() : null)
                .goal(user.getGoal() != null ? user.getGoal().name() : null)
                .activityLevel(user.getActivityLevel() != null ? user.getActivityLevel().name() : null)
                .dietaryPreferences(user.getDietaryPreferences())
                .allergies(user.getAllergies())
                .dislikes(user.getDislikes())
                .availableEquipment(user.getAvailableEquipment())
                .limitations(user.getLimitations())
                .build();
    }
}