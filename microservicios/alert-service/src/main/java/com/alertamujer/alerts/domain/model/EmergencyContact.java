package com.alertamujer.alerts.domain.model;

import com.alertamujer.shared.web.FotoDataUri;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Contacto de emergencia de una usuaria: a quién se avisa cuando activa una alerta.
 */
public class EmergencyContact {

    private Long id;
    private Long userProfileId;
    private String contactName;
    private String telephone;
    private String email;
    private String relationship;
    /** Foto del contacto como data URI de imagen pequeña (ver FotoDataUri). */
    private String photo;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    // ── Reglas ──────────────────────────────────────────────────────

    public static EmergencyContact nuevo(Long perfil, DatosContacto datos) {
        EmergencyContact c = new EmergencyContact();
        c.userProfileId = perfil;
        c.contactName = datos.contactName();
        c.telephone = datos.telephone();
        c.email = datos.email();
        c.relationship = datos.relationship();
        c.photo = FotoDataUri.validar(datos.photo());
        return c;
    }

    public boolean esDe(Long perfil) {
        return perfil != null && Objects.equals(userProfileId, perfil);
    }

    /** Solo se cambian los campos enviados: un PUT con datos parciales no debe borrar el resto. */
    public void actualizar(DatosContacto datos) {
        if (datos.contactName() != null) contactName = datos.contactName();
        if (datos.telephone() != null) telephone = datos.telephone();
        if (datos.email() != null) email = datos.email().isBlank() ? null : datos.email().trim();
        if (datos.relationship() != null) relationship = datos.relationship();
        // "" quita la foto; null la deja como estaba
        if (datos.photo() != null) photo = FotoDataUri.validar(datos.photo());
    }

    /** Correo sin espacios ("" si no tiene): sin correo no se le puede avisar. */
    public String correoParaAvisar() {
        return email == null ? "" : email.trim();
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserProfileId() { return userProfileId; }
    public void setUserProfileId(Long userProfileId) { this.userProfileId = userProfileId; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
