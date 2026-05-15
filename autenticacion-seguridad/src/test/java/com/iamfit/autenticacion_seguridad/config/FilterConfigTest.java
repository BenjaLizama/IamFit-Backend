package com.iamfit.autenticacion_seguridad.config;

import com.iamfit.autenticacion_seguridad.config.filters.IpFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilterConfigTest {

    private final FilterConfig filterConfig = new FilterConfig();

    @Test
    @DisplayName("Debería registrar el IpFilter con la configuración correcta")
    void loggingFilterRegistrationTest() {
        // Ejecución
        FilterRegistrationBean<IpFilter> registrationBean = filterConfig.loggingFilter();

        // Verificaciones
        assertNotNull(registrationBean, "El RegistrationBean no debería ser nulo");
        assertNotNull(registrationBean.getFilter(), "El filtro interno no debería ser nulo");
        assertTrue(registrationBean.getFilter() instanceof IpFilter, "El filtro debería ser de tipo IpFilter");

        // Verificar patrones de URL
        Collection<String> urlPatterns = registrationBean.getUrlPatterns();
        assertTrue(urlPatterns.contains("/*"), "Debería aplicarse a todos los patrones /*");

        // Verificar precedencia (Orden)
        assertEquals(Ordered.HIGHEST_PRECEDENCE, registrationBean.getOrder(),
                "El filtro debe tener la máxima prioridad");
    }
}