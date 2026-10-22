package com.alertamujer.shared.web;

import java.util.regex.Pattern;

/**
 * Photos (profile, contacts) that arrive as a base64 image data URI. The apps shrink them to
 * ~400 px before sending; here only an image is accepted, with a size cap so nobody stores
 * large files in the database.
 */
public final class PhotoDataUri {

    /** ~300 KB of image: more than enough for a 400 px photo. */
    public static final int MAX_CHARACTERS = 400_000;

    private static final Pattern FORMAT =
            Pattern.compile("^data:image/(jpeg|jpg|png|webp);base64,[A-Za-z0-9+/]+={0,2}$");

    private PhotoDataUri() {
    }

    /**
     * Returns the photo ready to store: null if empty (remove the photo). Throws
     * IllegalArgumentException (400) if it is not a valid image or is too large.
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
