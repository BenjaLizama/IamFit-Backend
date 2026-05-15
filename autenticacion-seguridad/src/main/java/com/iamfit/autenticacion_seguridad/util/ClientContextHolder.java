package com.iamfit.autenticacion_seguridad.util;

public class ClientContextHolder {

    private static final ThreadLocal<ClientMetadata> context = new ThreadLocal<>();

    public record ClientMetadata(String ip, String userAgent, String token, String deviceId) {}

    public static void setContext(String ip, String userAgent, String token, String deviceId) {
        context.set(new ClientMetadata(ip, userAgent, token, deviceId));
    }

    public static String getDeviceId() {
        return context.get() != null ? context.get().deviceId() : null;
    }

    public static String getToken() {
        return context.get() != null ? context.get().token() : null;
    }

    public static String getIp() {
        return context.get() != null ? context.get().ip() : null;
    }

    public static String getUserAgent() {
        return context.get() != null ? context.get().userAgent() : null;
    }

    public static void clear() {
        context.remove();
    }
}
