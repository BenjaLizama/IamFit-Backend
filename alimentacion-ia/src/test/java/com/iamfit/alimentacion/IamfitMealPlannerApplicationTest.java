package com.iamfit.alimentacion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// Inyectamos las propiedades directamente aquí para domar a H2
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.sql.init.mode=never",
                "spring.datasource.url=jdbc:h2:mem:iamfit_testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password="
        }
)
@ActiveProfiles("test")
public class IamfitMealPlannerApplicationTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
        // Si el test pasa, significa que el contexto arrancó correctamente
    }

}
