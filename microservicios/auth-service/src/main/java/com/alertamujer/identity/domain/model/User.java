package com.alertamujer.identity.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Persona registrada (usuaria o administradora): datos personales y rol. Las credenciales están en
 * Account y la foto en UserProfile. Modelo del dominio: no conoce la base de datos ni Spring.
 */
public class User {

    private Long id;
    /** 1 = usuaria, 2 = administradora. */
    private Long roleId;
    /** Nombre del rol en la base (user, admin). */
    private String roleName;
    private String firstName;
    private String lastName;
    private String telephone;
    private String email;
    private String documentNumber;
    private String documentType;
    private LocalDate birthdate;
    private LocalDateTime createdAt = LocalDateTime.now();
    /** Cuándo aceptó los términos y la política de privacidad, y qué versión (null en cuentas antiguas). */
    private LocalDateTime termsAcceptedAt;
    private String termsVersion;

    /** Versión vigente de los términos y la política de privacidad. Cambiarla al publicar otra. */
    public static final String CURRENT_TERMS = "2026-10";

    public static final long USER_ROLE = 1L;
    public static final long ADMIN_ROLE = 2L;
    private static final int MAX_NAME = 100;
    private static final DateTimeFormatter FORM_DATE =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    // ── Reglas ──────────────────────────────────────────────────────

    /** Usuaria nueva desde el formulario de registro (nombre completo en un solo campo). */
    public static User newOne(String name, String email, String telephone, String documentType,
                             String documentNumber, LocalDate birth) {
        String[] parts = name.split(" ");
        User u = new User();
        u.roleId = USER_ROLE;
        u.firstName = parts[0];
        u.lastName = parts.length > 1 ? parts[1] : "";
        u.email = email;
        u.telephone = cleanPhone(telephone);
        u.documentType = emptyToNull(documentType);
        u.documentNumber = emptyToNull(documentNumber);
        u.birthdate = birth;
        return u;
    }

    /** Usuaria nueva que entra por primera vez con Google (sin teléfono ni documento). */
    public static User fromGoogle(String email, String name, String givenName, String familyName) {
        String firstName = givenName != null && !givenName.isBlank() ? givenName : (name != null ? name.split(" ")[0] : "");
        String lastName = familyName != null ? familyName
                : (name != null && name.contains(" ") ? name.substring(name.indexOf(' ') + 1) : "");
        if (firstName.isBlank()) firstName = email.substring(0, email.indexOf('@'));
        User u = new User();
        u.roleId = USER_ROLE;
        u.firstName = truncate(firstName);
        u.lastName = truncate(lastName);
        u.email = email;
        return u;
    }

    // "300 123-4567" y "3001234567" son el mismo número: se guarda sin espacios ni separadores
    public static String cleanPhone(String telephone) {
        return emptyToNull(telephone == null ? null : telephone.replaceAll("[\\s().-]", ""));
    }

    public static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** Fecha DD/MM/AAAA como la escriben los formularios; null si falta o no es válida. */
    public static LocalDate formDate(String ddMmYyyy) {
        if (ddMmYyyy == null || ddMmYyyy.isBlank()) return null;
        try {
            return LocalDate.parse(ddMmYyyy.trim(), FORM_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Deja constancia de que aceptó los términos vigentes. */
    public void acceptTerms(LocalDateTime when) {
        termsAcceptedAt = when;
        termsVersion = CURRENT_TERMS;
    }

    public boolean esAdmin() {
        return roleId != null && roleId == ADMIN_ROLE;
    }

    /** Rol que viaja en el JWT. */
    public String roleOfToken() {
        return esAdmin() ? "admin" : "user";
    }

    public String fullName() {
        return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
    }

    /** Cambios de la propia usuaria en su perfil: solo los campos enviados (Optional vacío = borrarlo). */
    public void updateData(UserChanges c) {
        if (c.firstName() != null) firstName = c.firstName().orElse(null);
        if (c.lastName() != null) lastName = c.lastName().orElse(null);
        if (c.telephone() != null) telephone = c.telephone().orElse(null);
        if (c.documentNumber() != null) documentNumber = c.documentNumber().orElse(null);
        if (c.documentType() != null) documentType = c.documentType().orElse(null);
        if (c.birthdate() != null) birthdate = c.birthdate();
    }

    /** Cambios de la administración: solo los campos que llegan con valor. */
    public void updateFromAdministration(String name, String newLastName, String newTelephone, String newEmail) {
        if (name != null) firstName = name;
        if (newLastName != null) lastName = newLastName;
        if (newTelephone != null) telephone = newTelephone;
        if (newEmail != null) email = newEmail;
    }

    /** "Elimina" la cuenta sin romper el historial: borra los datos personales. */
    public void anonymize() {
        firstName = "Cuenta";
        lastName = "eliminada";
        email = "eliminada-" + id + "@alertamujer.invalid";
        telephone = null;
        documentNumber = null;
        documentType = null;
        birthdate = null;
    }

    private static String truncate(String text) {
        return text.length() > MAX_NAME ? text.substring(0, MAX_NAME) : text;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getTermsAcceptedAt() { return termsAcceptedAt; }
    public void setTermsAcceptedAt(LocalDateTime termsAcceptedAt) { this.termsAcceptedAt = termsAcceptedAt; }
    public String getTermsVersion() { return termsVersion; }
    public void setTermsVersion(String termsVersion) { this.termsVersion = termsVersion; }
}
