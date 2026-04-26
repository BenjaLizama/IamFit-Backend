package com.iamfit.autenticacion_seguridad.config.filters;

import com.iamfit.autenticacion_seguridad.util.ClientContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IpFilterTest {

    private IpFilter ipFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        ipFilter = new IpFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        MDC.clear();
        ClientContextHolder.clear();
    }

    @Test
    @DisplayName("Debería extraer IP de X-Forwarded-For y limpiar el contexto al finalizar")
    void shouldExtractIpFromXForwardedForAndClearContext() throws IOException, ServletException {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");

        doAnswer(invocation -> {
            assertEquals("192.168.1.1", MDC.get("userIp"));
            assertEquals("192.168.1.1", ClientContextHolder.getIp());
            assertEquals("token-valido", ClientContextHolder.getToken());
            return null;
        }).when(chain).doFilter(any(), any());

        ipFilter.doFilter(request, response, chain);

        assertNull(MDC.get("userIp"));
        assertNull(ClientContextHolder.getIp());
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Debería usar remoteAddr si X-Forwarded-For no está presente")
    void shouldUseRemoteAddrWhenXffIsMissing() throws IOException, ServletException {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        doAnswer(invocation -> {
            assertEquals("127.0.0.1", MDC.get("userIp"));
            return null;
        }).when(chain).doFilter(any(), any());

        ipFilter.doFilter(request, response, chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    @DisplayName("Debería manejar Authorization nulo o sin formato Bearer")
    void shouldHandleMissingOrInvalidAuthHeader() throws IOException, ServletException {
        when(request.getHeader("Authorization")).thenReturn("Basic user:pass");

        doAnswer(invocation -> {
            assertNull(ClientContextHolder.getToken());
            return null;
        }).when(chain).doFilter(any(), any());

        ipFilter.doFilter(request, response, chain);
    }

    @Test
    @DisplayName("Debería ignorar la lógica de IP si el request no es HttpServletRequest")
    void shouldIgnoreNonHttpRequest() throws IOException, ServletException {
        var nonHttpRequest = mock(jakarta.servlet.ServletRequest.class);

        ipFilter.doFilter(nonHttpRequest, response, chain);

        verify(chain, times(1)).doFilter(nonHttpRequest, response);
        assertNull(MDC.get("userIp"));
    }

    @Test
    @DisplayName("Debería limpiar el contexto incluso si ocurre una excepción en la cadena")
    void shouldClearContextEvenOnException() throws IOException, ServletException {
        doThrow(new RuntimeException("Error fatal")).when(chain).doFilter(any(), any());

        assertThrows(RuntimeException.class, () -> ipFilter.doFilter(request, response, chain));

        assertNull(MDC.get("userIp"));
        assertNull(ClientContextHolder.getIp());
    }
}
