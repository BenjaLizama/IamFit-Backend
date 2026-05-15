package com.iamfit.usuarios_perfiles.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileDTO {
    private String credentialId;
    private String nickname;
    private Integer age;
    private Integer height;
    private Integer weight;
    private String sex;
}