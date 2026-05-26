package com.iamfit.alimentacion.repository;

import com.iamfit.alimentacion.entity.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyLogRepository extends JpaRepository<DailyLog, UUID> {

    Optional<DailyLog> findByUserIdAndLogDate(String userId, LocalDate logDate);

    boolean existsByUserIdAndLogDate(String userId, LocalDate logDate);
}