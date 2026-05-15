package com.iamfit.usuarios_perfiles.entity;

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

}
