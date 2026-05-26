package com.iamfit.alimentacion.repository;

import com.iamfit.alimentacion.entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, UUID> {

    List<FoodItem> findByNameContainingIgnoreCase(String name);

    List<FoodItem> findByFoodCategory(String category);

    @Query("SELECT f FROM FoodItem f WHERE " +
            "LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(f.nameEn) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<FoodItem> searchByNameOrNameEn(@Param("query") String query);

    boolean existsByExternalId(String externalId);
}