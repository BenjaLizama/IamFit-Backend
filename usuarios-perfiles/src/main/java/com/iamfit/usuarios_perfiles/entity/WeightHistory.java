package com.iamfit.usuarios_perfiles.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SuperBuilder
@Table(name = "WEIGHT_HISTORY", indexes = {
        @Index(name = "idx_weight_user_id", columnList = "user_id"),
        @Index(name = "idx_user_createdAt", columnList = "user_id, createdAt")
})
public class WeightHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private Integer weight;

}
