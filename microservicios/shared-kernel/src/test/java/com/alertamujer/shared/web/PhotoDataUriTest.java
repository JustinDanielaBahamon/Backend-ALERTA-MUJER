package com.alertamujer.shared.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhotoDataUriTest {

    @Test
    void acceptsABase64Image() {
        String photo = "data:image/jpeg;base64,/9j/4AAQSkZJRg==";
        assertEquals(photo, PhotoDataUri.validate(photo));
    }

    @Test
    void emptyRemovesThePhoto() {
        assertNull(PhotoDataUri.validate(""));
        assertNull(PhotoDataUri.validate(null));
    }

    @Test
    void rejectsWhatIsNotAnImage() {
        assertThrows(IllegalArgumentException.class, () -> PhotoDataUri.validate("data:text/html;base64,PGgxPg=="));
        assertThrows(IllegalArgumentException.class, () -> PhotoDataUri.validate("https://ejemplo.com/foto.jpg"));
    }

    @Test
    void rejectsPhotosThatAreTooLarge() {
        String large = "data:image/jpeg;base64," + "A".repeat(PhotoDataUri.MAX_CHARACTERS);
        assertThrows(IllegalArgumentException.class, () -> PhotoDataUri.validate(large));
    }
}
