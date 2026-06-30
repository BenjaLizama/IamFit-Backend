package com.iamfit.usuarios_perfiles.service;

import com.iamfit.usuarios_perfiles.dto.UpdateProfileRequest;
import com.iamfit.usuarios_perfiles.dto.UserProfileDTO;
import com.iamfit.usuarios_perfiles.entity.UserEntity;
import com.iamfit.usuarios_perfiles.enums.ActivityLevel;
import com.iamfit.usuarios_perfiles.enums.GoalType;
import com.iamfit.usuarios_perfiles.enums.SexType;
import com.iamfit.usuarios_perfiles.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    private UUID credentialId;
    private String userId;

    @BeforeEach
    void setUp() {
        credentialId = UUID.randomUUID();
        userId = credentialId.toString();
    }

    private UserEntity buildUser() {
        return UserEntity.builder()
                .credentialId(credentialId)
                .nickname("Benja")
                .age(25)
                .height(180)
                .weight(80)
                .sex(SexType.MALE)
                .goal(GoalType.GAIN_MUSCLE)
                .activityLevel(ActivityLevel.MODERATE)
                .dietaryPreferences(List.of("Alta proteína"))
                .allergies(List.of("mariscos"))
                .dislikes(List.of("brócoli"))
                .availableEquipment(List.of("mancuernas"))
                .limitations("ninguna")
                .build();
    }

    @Test
    void getProfile_returnsMappedDto() {
        when(userRepository.findByCredentialId(credentialId)).thenReturn(Optional.of(buildUser()));

        UserProfileDTO dto = userProfileService.getProfile(userId);

        assertThat(dto).isNotNull();
        assertThat(dto.getCredentialId()).isEqualTo(userId);
        assertThat(dto.getNickname()).isEqualTo("Benja");
        assertThat(dto.getSex()).isEqualTo("MALE");
        assertThat(dto.getGoal()).isEqualTo("GAIN_MUSCLE");
        assertThat(dto.getActivityLevel()).isEqualTo("MODERATE");
        assertThat(dto.getAllergies()).containsExactly("mariscos");
    }

    @Test
    void getProfile_throwsWhenNotFound() {
        when(userRepository.findByCredentialId(credentialId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.getProfile(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Perfil no encontrado");
    }

    @Test
    void getProfile_handlesNullEnums() {
        UserEntity user = UserEntity.builder()
                .credentialId(credentialId)
                .nickname("Sin enums")
                .age(30)
                .height(170)
                .weight(70)
                .build();
        when(userRepository.findByCredentialId(credentialId)).thenReturn(Optional.of(user));

        UserProfileDTO dto = userProfileService.getProfile(userId);

        assertThat(dto.getSex()).isNull();
        assertThat(dto.getGoal()).isNull();
        assertThat(dto.getActivityLevel()).isNull();
    }

    @Test
    void updateProfile_updatesOnlyNonNullFields() {
        UserEntity user = buildUser();
        when(userRepository.findByCredentialId(credentialId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("NuevoNick");
        request.setWeight(85);

        UserProfileDTO dto = userProfileService.updateProfile(userId, request);

        assertThat(dto.getNickname()).isEqualTo("NuevoNick");
        assertThat(dto.getWeight()).isEqualTo(85);
        // unchanged fields preserved
        assertThat(dto.getAge()).isEqualTo(25);
        assertThat(dto.getGoal()).isEqualTo("GAIN_MUSCLE");
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_updatesAllFields() {
        UserEntity user = buildUser();
        when(userRepository.findByCredentialId(credentialId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("Nick");
        request.setAge(40);
        request.setSex(SexType.FEMALE);
        request.setHeight(165);
        request.setWeight(60);
        request.setGoal(GoalType.LOSE_WEIGHT);
        request.setActivityLevel(ActivityLevel.ACTIVE);
        request.setDietaryPreferences(List.of("vegano"));
        request.setAllergies(List.of("gluten"));
        request.setDislikes(List.of("cebolla"));
        request.setAvailableEquipment(List.of("banda"));
        request.setLimitations("rodilla");

        UserProfileDTO dto = userProfileService.updateProfile(userId, request);

        assertThat(dto.getNickname()).isEqualTo("Nick");
        assertThat(dto.getAge()).isEqualTo(40);
        assertThat(dto.getSex()).isEqualTo("FEMALE");
        assertThat(dto.getHeight()).isEqualTo(165);
        assertThat(dto.getWeight()).isEqualTo(60);
        assertThat(dto.getGoal()).isEqualTo("LOSE_WEIGHT");
        assertThat(dto.getActivityLevel()).isEqualTo("ACTIVE");
        assertThat(dto.getDietaryPreferences()).containsExactly("vegano");
        assertThat(dto.getLimitations()).isEqualTo("rodilla");
    }

    @Test
    void updateProfile_throwsWhenNotFound() {
        when(userRepository.findByCredentialId(credentialId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.updateProfile(userId, new UpdateProfileRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Perfil no encontrado");
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteProfile_deletesWhenPresent() {
        UserEntity user = buildUser();
        when(userRepository.findByCredentialId(credentialId)).thenReturn(Optional.of(user));

        userProfileService.deleteProfile(userId);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).delete(captor.capture());
        assertThat(captor.getValue()).isEqualTo(user);
    }

    @Test
    void deleteProfile_doesNothingWhenAbsent() {
        when(userRepository.findByCredentialId(credentialId)).thenReturn(Optional.empty());

        userProfileService.deleteProfile(userId);

        verify(userRepository, never()).delete(any());
    }
}
