package com.alertamujer.shared.web;

import java.util.regex.Pattern;

/**
 * Fotos (perfil, contactos) que llegan como data URI de imagen en base64. Las apps las reducen a
 * ~400 px antes de enviarlas; aquí solo se acepta una imagen y se pone un tope de tamaño para que
 * nadie guarde archivos grandes en la base.
 */
public final class FotoDataUri {

    /** ~300 KB de imagen: de sobra para una foto de 400 px. */
    public static final int MAXIMO_CARACTERES = 400_000;

    private static final Pattern FORMATO =
            Pattern.compile("^data:image/(jpeg|jpg|png|webp);base64,[A-Za-z0-9+/]+={0,2}$");

    private FotoDataUri() {
    }

    /**
     * Devuelve la foto lista para guardar: null si viene vacía (quitar la foto). Lanza
     * IllegalArgumentException (400) si no es una imagen válida o es demasiado grande.
     */
    public static String validar(String foto) {
        if (foto == null || foto.isBlank()) return null;
        String valor = foto.trim();
        if (valor.length() > MAXIMO_CARACTERES) {
            throw new IllegalArgumentException("La foto es demasiado grande");
        }
        if (!FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException("La foto no es una imagen válida");
        }
        return valor;
    }
}
