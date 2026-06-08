package com.iamfit.usuarios_perfiles.controller;

import com.iamfit.usuarios_perfiles.dto.ProfileContextDTO;
import com.iamfit.usuarios_perfiles.dto.UpdateProfileRequest;
import com.iamfit.usuarios_perfiles.dto.UserProfileDTO;
import com.iamfit.usuarios_perfiles.service.ProfileContextService;
import com.iamfit.usuarios_perfiles.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;
    private final ProfileContextService profileContextService;

    @GetMapping("/get-profile")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileDTO> getProfile(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        log.info("Obteniendo perfil para userId: {}", userId);
        try {
            return ResponseEntity.ok(userProfileService.getProfile(userId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/profile")
    @Transactional
    public ResponseEntity<UserProfileDTO> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        log.info("Actualizando perfil para userId: {}", userId);
        try {
            return ResponseEntity.ok(userProfileService.updateProfile(userId, request));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/profile/context")
    @Transactional(readOnly = true)
    public ResponseEntity<ProfileContextDTO> getProfileContext(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwt.getClaim("userId");
        String token = authHeader.replace("Bearer ", "");
        try {
            UserProfileDTO profile = userProfileService.getProfile(userId);
            ProfileContextDTO context = profileContextService.buildContext(profile, token);
            return ResponseEntity.ok(context);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        log.info("Eliminando perfil para userId: {}", userId);
        userProfileService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }
}