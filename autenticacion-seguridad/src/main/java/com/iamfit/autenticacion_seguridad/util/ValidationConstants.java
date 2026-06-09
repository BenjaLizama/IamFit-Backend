package com.iamfit.autenticacion_seguridad.util;

public final class ValidationConstants {

    private ValidationConstants() {}

    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$";
    public static final String PASSWORD_MESSAGE = "La contraseña debe tener al menos un número, una mayúscula, una minúscula y un carácter especial.";
}