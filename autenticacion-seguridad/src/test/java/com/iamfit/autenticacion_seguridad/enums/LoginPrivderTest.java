package com.iamfit.autenticacion_seguridad.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginProviderTest {

    @ParameterizedTest
    @EnumSource(LoginProvider.class)
    @DisplayName("Debería validar que cada valor del enum no sea nulo")
    void shouldCheckAllEnumValues(LoginProvider provider) {
        assertNotNull(provider);
    }

    @Test
    @DisplayName("Debería tener los proveedores exactos definidos")
    void shouldHaveExactProviders() {
        LoginProvider[] expected = {
                LoginProvider.LOCAL,
                LoginProvider.GOOGLE,
                LoginProvider.GITHUB,
                LoginProvider.APPLE
        };

        assertEquals(4, LoginProvider.values().length);
        assertEquals(LoginProvider.LOCAL, LoginProvider.valueOf("LOCAL"));
        assertEquals(LoginProvider.GOOGLE, LoginProvider.valueOf("GOOGLE"));
        assertEquals(LoginProvider.GITHUB, LoginProvider.valueOf("GITHUB"));
        assertEquals(LoginProvider.APPLE, LoginProvider.valueOf("APPLE"));
    }
}
