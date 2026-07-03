package com.iamfit.alimentacion.integration;

import com.iamfit.alimentacion.entity.DailyLog;
import com.iamfit.alimentacion.entity.FoodEntry;
import com.iamfit.alimentacion.entity.FoodEntry.MealType;
import com.iamfit.alimentacion.entity.FoodItem;
import com.iamfit.alimentacion.repository.DailyLogRepository;
import com.iamfit.alimentacion.repository.FoodEntryRepository;
import com.iamfit.alimentacion.repository.FoodItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.datasource.url=jdbc:h2:mem:iamfit_testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class FoodLogPersistenceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager; // <-- Inyectamos el manejador de caché

    @Autowired
    private FoodItemRepository foodItemRepository;
    @Autowired
    private DailyLogRepository dailyLogRepository;
    @Autowired
    private FoodEntryRepository foodEntryRepository;

    @Test
    void persistsAndQueriesFoodLogFlow() {
        FoodItem item = new FoodItem();
        item.setName("Avena");
        item.setNameEn("Oats");
        item.setCalories(389.0);
        item.setProtein(16.9);
        item.setCarbohydrates(66.0);
        item.setFat(6.9);
        item.setFoodCategory("cereales");
        item = foodItemRepository.save(item);

        DailyLog log = new DailyLog();
        log.setUserId("user-1");
        log.setLogDate(LocalDate.of(2024, 1, 10));
        log = dailyLogRepository.save(log);

        FoodEntry entry = new FoodEntry();
        entry.setDailyLog(log);
        entry.setFoodItem(item);
        entry.setQuantity(50.0);
        entry.setMealType(MealType.DESAYUNO);
        entry.setCalculatedCalories(194.5);
        foodEntryRepository.save(entry);

        // --- EL TRUCO MAGICO ---
        // Sincroniza los datos hacia H2 y borra el caché de memoria
        entityManager.flush();
        entityManager.clear();
        // -----------------------

        Optional<DailyLog> found =
                dailyLogRepository.findByUserIdAndLogDate("user-1", LocalDate.of(2024, 1, 10));

        assertThat(found).isPresent();
        assertThat(found.get().getEntries()).hasSize(1);
        assertThat(found.get().getEntries().get(0).getFoodItem().getName()).isEqualTo("Avena");
        assertThat(foodItemRepository.searchByNameOrNameEn("avena")).hasSize(1);
        assertThat(dailyLogRepository.existsByUserIdAndLogDate("user-1", LocalDate.of(2024, 1, 10)))
                .isTrue();
    }
}
