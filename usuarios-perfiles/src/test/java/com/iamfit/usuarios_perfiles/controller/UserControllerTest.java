package com.iamfit.usuarios_perfiles.controller;

import com.iamfit.usuarios_perfiles.dto.ProfileContextDTO;
import com.iamfit.usuarios_perfiles.dto.UpdateProfileRequest;
import com.iamfit.usuarios_perfiles.dto.UserProfileDTO;
import com.iamfit.usuarios_perfiles.service.ProfileContextService;
import com.iamfit.usuarios_perfiles.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private ProfileContextService profileContextService;

    @InjectMocks
    private UserController userController;

    private Jwt jwt;
    private final String userId = "user-123";

    @BeforeEach
    void setUp() {
        jwt = mock(Jwt.class);
        lenient().when(jwt.getClaim("userId")).thenReturn(userId);
    }

    @Test
    void getProfile_returnsOk() {
        UserProfileDTO dto = UserProfileDTO.builder().nickname("Benja").build();
        when(userProfileService.getProfile(userId)).thenReturn(dto);

        ResponseEntity<UserProfileDTO> response = userController.getProfile(jwt);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void getProfile_returnsNotFoundOnError() {
        when(userProfileService.getProfile(userId)).thenThrow(new RuntimeException("not found"));

        ResponseEntity<UserProfileDTO> response = userController.getProfile(jwt);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateProfile_returnsOk() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        UserProfileDTO dto = UserProfileDTO.builder().nickname("Nuevo").build();
        when(userProfileService.updateProfile(userId, request)).thenReturn(dto);

        ResponseEntity<UserProfileDTO> response = userController.updateProfile(request, jwt);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void updateProfile_returnsNotFoundOnError() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        when(userProfileService.updateProfile(userId, request)).thenThrow(new RuntimeException());

        ResponseEntity<UserProfileDTO> response = userController.updateProfile(request, jwt);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getProfileContext_returnsOkAndStripsBearer() {
        UserProfileDTO profile = UserProfileDTO.builder().build();
        ProfileContextDTO context = ProfileContextDTO.builder().profile(profile).build();
        when(userProfileService.getProfile(userId)).thenReturn(profile);
        when(profileContextService.buildContext(profile, "abc.def")).thenReturn(context);

        ResponseEntity<ProfileContextDTO> response =
                userController.getProfileContext(jwt, "Bearer abc.def");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(context);
        verify(profileContextService).buildContext(profile, "abc.def");
    }

    @Test
    void getProfileContext_returnsNotFoundOnError() {
        when(userProfileService.getProfile(userId)).thenThrow(new RuntimeException());

        ResponseEntity<ProfileContextDTO> response =
                userController.getProfileContext(jwt, "Bearer abc.def");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = userController.delete(jwt);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userProfileService).deleteProfile(userId);
    }
}
