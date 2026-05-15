package com.iamfit.autenticacion_seguridad.config.filters;

import com.iamfit.autenticacion_seguridad.util.ClientContextHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

import java.io.IOException;

public class IpFilter implements Filter {

    private static final String USER_IP = "userIp";
    private static final String DEVICE_ID_HEADER = "X-Device-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String ipAddress = extractIp(httpRequest);
            String deviceId = httpRequest.getHeader(DEVICE_ID_HEADER);
            String userAgent = httpRequest.getHeader("User-Agent");
            String authHeader = httpRequest.getHeader("Authorization");

            String token = (authHeader != null && authHeader.startsWith("Bearer "))
                    ? authHeader.substring(7)
                    : null;

            ClientContextHolder.setContext(ipAddress, userAgent, token, deviceId);
            MDC.put("userIp", ipAddress);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(USER_IP);
            ClientContextHolder.clear();
        }
    }

    private String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");

        if (xff != null && !xff.isBlank()) {
            // El primer elemento es la IP real del cliente
            return xff.split(",")[0].trim();
        }

        // Si no hay proxy, tomamos la IP directa
        return request.getRemoteAddr();
    }
}
