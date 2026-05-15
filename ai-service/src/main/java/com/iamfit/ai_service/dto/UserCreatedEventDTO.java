package com.iamfit.ai_service.dto;


import lombok.Data;

@Data
public class UserCreatedEventDTO {
    private String credentialId;
    private String nickname;
    private int age;
    private int height;
    private int weight;
    private String sex;
}