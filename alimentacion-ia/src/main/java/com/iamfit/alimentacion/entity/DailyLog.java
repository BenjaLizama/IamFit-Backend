package com.iamfit.alimentacion.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "daily_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "log_date"}))
public class DailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @OneToMany(mappedBy = "dailyLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FoodEntry> entries = new ArrayList<>();
}