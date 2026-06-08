package com.iamfit.usuarios_perfiles.entity;

import com.iamfit.usuarios_perfiles.enums.ActivityLevel;
import com.iamfit.usuarios_perfiles.enums.GoalType;
import com.iamfit.usuarios_perfiles.enums.SexType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SuperBuilder
@Table(name = "PROFILE", indexes = {
        @Index(name = "idx_credential_id", columnList = "credential_id", unique = true)
})
public class UserEntity extends BaseEntity {

    @Column(name = "credential_id", unique = true, nullable = false, updatable = false)
    private UUID credentialId;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private Integer height;

    @Column(nullable = false) // Este es una especie de "caché" para consultas rápidas.
    private Integer weight;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WeightHistoryEntity> weightHistory = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SexType sex;

    // Método de ayuda
    public void updateWeight(Integer newWeight) {
        this.weight = newWeight;
        this.weightHistory.add(WeightHistoryEntity.builder()
                .weight(newWeight)
                .user(this)
                .build());
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "goal")
    private GoalType goal;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level")
    private ActivityLevel activityLevel;

    @Column(name = "limitations", length = 500)
    private String limitations;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_dietary_preferences",
            joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "preference")
    @Builder.Default
    private List<String> dietaryPreferences = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_allergies",
            joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "allergy")
    @Builder.Default
    private List<String> allergies = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_dislikes",
            joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "dislike")
    @Builder.Default
    private List<String> dislikes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_equipment",
            joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "equipment")
    @Builder.Default
    private List<String> availableEquipment = new ArrayList<>();

}
