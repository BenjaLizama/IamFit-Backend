package com.iamfit.usuarios_perfiles.service;

import com.iamfit.usuarios_perfiles.dto.UpdateProfileRequest;
import com.iamfit.usuarios_perfiles.dto.UserProfileDTO;
import com.iamfit.usuarios_perfiles.entity.UserEntity;
import com.iamfit.usuarios_perfiles.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(String userId) {
        return userRepository.findByCredentialId(UUID.fromString(userId))
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));
    }

    @Transactional
    public UserProfileDTO updateProfile(String userId, UpdateProfileRequest request) {
        UserEntity user = userRepository.findByCredentialId(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

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

        return toDTO(userRepository.save(user));
    }

    @Transactional
    public void deleteProfile(String userId) {
        userRepository.findByCredentialId(UUID.fromString(userId))
                .ifPresent(userRepository::delete);
    }

    public UserProfileDTO toDTO(UserEntity user) {
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