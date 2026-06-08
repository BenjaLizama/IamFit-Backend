package com.iamfit.usuarios_perfiles.dto;

import com.iamfit.usuarios_perfiles.enums.ActivityLevel;
import com.iamfit.usuarios_perfiles.enums.GoalType;
import com.iamfit.usuarios_perfiles.enums.SexType;
import lombok.Data;
import java.util.List;

@Data
public class UpdateProfileRequest {
    private String nickname;
    private Integer age;
    private SexType sex;
    private Integer weight;
    private Integer height;
    private GoalType goal;
    private ActivityLevel activityLevel;
    private List<String> dietaryPreferences;
    private List<String> allergies;
    private List<String> dislikes;
    private List<String> availableEquipment;
    private String limitations;
}