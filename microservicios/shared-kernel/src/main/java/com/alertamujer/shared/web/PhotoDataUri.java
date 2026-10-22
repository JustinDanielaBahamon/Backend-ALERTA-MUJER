package com.alertamujer.shared.web;

import java.util.regex.Pattern;

/**
 * Fotos (perfil, contactos) que llegan como data URI de imagen en base64. Las apps las reducen a
 * ~400 px antes de enviarlas; aquí solo se acepta una imagen y se pone un tope de tamaño para que
 * nadie guarde archivos grandes en la base.
 */
public final class PhotoDataUri {

    /** ~300 KB de imagen: de sobra para una foto de 400 px. */
    public static final int MAX_CHARACTERS = 400_000;

    private static final Pattern FORMAT =
            Pattern.compile("^data:image/(jpeg|jpg|png|webp);base64,[A-Za-z0-9+/]+={0,2}$");

    private PhotoDataUri() {
    }

    /**
     * Devuelve la foto lista para guardar: null si viene vacía (quitar la foto). Lanza
     * IllegalArgumentException (400) si no es una imagen válida o es demasiado grande.
     */
    public static String validate(String photo) {
        if (photo == null || photo.isBlank()) return null;
        String value = photo.trim();
        if (value.length() > MAX_CHARACTERS) {
            throw new IllegalArgumentException("La foto es demasiado grande");
        }
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("La foto no es una imagen válida");
        }
        return value;
    }
}
