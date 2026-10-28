package com.alertamujer.identity.adapter.out.google;

import com.alertamujer.identity.application.port.out.GoogleVerifierPort;
import com.alertamujer.shared.exception.InvalidCredentialsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Output adapter: validates the token the client sends when signing in with Google and returns the
 * verified identity.
 *
 * Accepts two kinds of token:
 * - Firebase Authentication token (web, signInWithPopup): validated with firebase-admin.
 * - Google token (mobile app, expo-auth-session): validated against the allowed Client IDs.
 *
 * The content is never trusted without verifying the signature: the issuer is only read to pick the verifier.
 */
@Component
class GoogleVerifierAdapter implements GoogleVerifierPort {

    private static final Logger log = LoggerFactory.getLogger(GoogleVerifierAdapter.class);

    private static final String FIREBASE_ISSUER_PREFIX = "https://securetoken.google.com/";
    private static final List<String> GOOGLE_ISSUERS = List.of("accounts.google.com", "https://accounts.google.com");

    private final FirebaseApp firebaseApp;
    private final GoogleIdTokenVerifier googleVerifier;
    private final ObjectMapper objectMapper = new ObjectMapper();

    GoogleVerifierAdapter(FirebaseApp firebaseApp,
                                  @Value("${google.client-ids:}") String clientIds) {
        this.firebaseApp = firebaseApp;
        List<String> audiences = Arrays.stream(clientIds.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        this.googleVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(audiences)
                .setIssuers(GOOGLE_ISSUERS)
                .build();
    }

    @Override
    public GoogleIdentity verify(String idToken) {
        String issuer = readIssuer(idToken);
        if (issuer.startsWith(FIREBASE_ISSUER_PREFIX)) {
            return verifyFirebase(idToken);
        }
        if (GOOGLE_ISSUERS.contains(issuer)) {
            return verifyGoogle(idToken);
        }
        log.warn("Token de Google rechazado: emisor desconocido {}", issuer);
        throw new InvalidCredentialsException("Token de Google no válido");
    }

    private GoogleIdentity verifyFirebase(String idToken) {
        try {
            FirebaseToken token = FirebaseAuth.getInstance(firebaseApp).verifyIdToken(idToken, true);
            if (token.getEmail() == null || !token.isEmailVerified()) {
                throw new InvalidCredentialsException("La cuenta de Google no tiene un correo verificado");
            }
            String name = token.getName();
            return new GoogleIdentity(token.getEmail().toLowerCase(), name, null, null);
        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            // Only the reason, never the token
            log.warn("Token de Firebase rechazado: {}", e.getMessage());
            throw new InvalidCredentialsException("Token de Google no válido o vencido");
        }
    }

    private GoogleIdentity verifyGoogle(String idToken) {
        try {
            GoogleIdToken token = googleVerifier.verify(idToken);
            if (token == null) {
                log.warn("Token de Google rechazado: firma, audiencia o vencimiento no válidos");
                throw new InvalidCredentialsException("Token de Google no válido o vencido");
            }
            GoogleIdToken.Payload p = token.getPayload();
            if (p.getEmail() == null || !Boolean.TRUE.equals(p.getEmailVerified())) {
                throw new InvalidCredentialsException("La cuenta de Google no tiene un correo verificado");
            }
            return new GoogleIdentity(p.getEmail().toLowerCase(), (String) p.get("name"),
                    (String) p.get("given_name"), (String) p.get("family_name"));
        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Token de Google rechazado: {}", e.getMessage());
            throw new InvalidCredentialsException("Token de Google no válido o vencido");
        }
    }

    // Only to pick the verifier; the signature is validated afterwards
    private String readIssuer(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException();
            JsonNode payload = objectMapper.readTree(new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8));
            return payload.path("iss").asText("");
        } catch (Exception e) {
            throw new InvalidCredentialsException("Token de Google no válido");
        }
    }
}
