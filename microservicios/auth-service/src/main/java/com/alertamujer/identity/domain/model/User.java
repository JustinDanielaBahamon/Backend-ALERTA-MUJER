package com.alertamujer.identity.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Registered person (user or admin): personal data and role. Credentials live in
 * Account and the photo in UserProfile. Domain model: knows neither the database nor Spring.
 */
public class User {

    private Long id;
    /** 1 = usuaria, 2 = administradora. */
    private Long roleId;
    /** Role name in the database (user, admin). */
    private String roleName;
    private String firstName;
    private String lastName;
    private String telephone;
    private String email;
    private String documentNumber;
    private String documentType;
    private LocalDate birthdate;
    private LocalDateTime createdAt = LocalDateTime.now();
    /** When she accepted the terms and the privacy policy, and which version (null on old accounts). */
    private LocalDateTime termsAcceptedAt;
    private String termsVersion;

    /** Current version of the terms and the privacy policy. Change it when publishing another one. */
    public static final String CURRENT_TERMS = "2026-10";

    public static final long USER_ROLE = 1L;
    public static final long ADMIN_ROLE = 2L;
    private static final int MAX_NAME = 100;
    private static final DateTimeFormatter FORM_DATE =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    // ── Reglas ──────────────────────────────────────────────────────

    /** New user from the registration form (full name in a single field). */
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

    /** New user signing in for the first time with Google (no phone or document). */
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

    // "300 123-4567" and "3001234567" are the same number: stored without spaces or separators
    public static String cleanPhone(String telephone) {
        return emptyToNull(telephone == null ? null : telephone.replaceAll("[\\s().-]", ""));
    }

    public static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** DD/MM/YYYY date as the forms write it; null if missing or invalid. */
    public static LocalDate formDate(String ddMmYyyy) {
        if (ddMmYyyy == null || ddMmYyyy.isBlank()) return null;
        try {
            return LocalDate.parse(ddMmYyyy.trim(), FORM_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Records that she accepted the current terms. */
    public void acceptTerms(LocalDateTime when) {
        termsAcceptedAt = when;
        termsVersion = CURRENT_TERMS;
    }

    public boolean esAdmin() {
        return roleId != null && roleId == ADMIN_ROLE;
    }

    /** Role carried in the JWT. */
    public String roleOfToken() {
        return esAdmin() ? "admin" : "user";
    }

    public String fullName() {
        return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
    }

    /** The user's own profile changes: only the sent fields (empty Optional = erase it). */
    public void updateData(UserChanges c) {
        if (c.firstName() != null) firstName = c.firstName().orElse(null);
        if (c.lastName() != null) lastName = c.lastName().orElse(null);
        if (c.telephone() != null) telephone = c.telephone().orElse(null);
        if (c.documentNumber() != null) documentNumber = c.documentNumber().orElse(null);
        if (c.documentType() != null) documentType = c.documentType().orElse(null);
        if (c.birthdate() != null) birthdate = c.birthdate();
    }

    /** Changes by the administration: only the fields that arrive with a value. */
    public void updateFromAdministration(String name, String newLastName, String newTelephone, String newEmail) {
        if (name != null) firstName = name;
        if (newLastName != null) lastName = newLastName;
        if (newTelephone != null) telephone = newTelephone;
        if (newEmail != null) email = newEmail;
    }

    /** "Deletes" the account without breaking the history: erases the personal data. */
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
