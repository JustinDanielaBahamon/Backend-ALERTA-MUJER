package com.alertamujer.identity.adapter.in.web;

import com.alertamujer.identity.adapter.in.web.dto.AuthResponse;
import com.alertamujer.identity.adapter.in.web.dto.ForgotPasswordRequest;
import com.alertamujer.identity.adapter.in.web.dto.GoogleLoginRequest;
import com.alertamujer.identity.adapter.in.web.dto.LoginRequest;
import com.alertamujer.identity.adapter.in.web.dto.RegisterRequest;
import com.alertamujer.identity.adapter.in.web.dto.ResetPasswordRequest;
import com.alertamujer.identity.adapter.in.web.dto.VerifyResetCodeRequest;
import com.alertamujer.identity.application.port.in.DatosRegistro;
import com.alertamujer.identity.application.port.in.IniciarSesionUseCase;
import com.alertamujer.identity.application.port.in.RecuperarContrasenaUseCase;
import com.alertamujer.identity.application.port.in.RegistrarUsuariaUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Adaptador de entrada REST: registro, sesión y recuperación de contraseña. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrarUsuariaUseCase registro;
    private final IniciarSesionUseCase sesion;
    private final RecuperarContrasenaUseCase recuperacion;

    public AuthController(RegistrarUsuariaUseCase registro, IniciarSesionUseCase sesion,
                          RecuperarContrasenaUseCase recuperacion) {
        this.registro = registro;
        this.sesion = sesion;
        this.recuperacion = recuperacion;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest r) {
        return ResponseEntity.ok(AuthResponse.of(registro.registrar(new DatosRegistro(r.getNombre(), r.getEmail(),
                r.getPassword(), r.getTelefono(), r.getTipoDocumento(), r.getNumeroDocumento(), r.getFechaNacimiento()))));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(AuthResponse.of(sesion.conCorreo(request.getEmail(), request.getPassword())));
    }

    /** Inicio de sesión con Google: acepta el token de Firebase (web) o de Google (móvil). */
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(AuthResponse.of(sesion.conGoogle(request.getIdToken())));
    }

    /** Deja constancia del cierre de sesión (el cliente borra el token). Responde 204 aunque no haya sesión. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication, @RequestBody(required = false) Map<String, String> body) {
        Long userId = authentication != null && authentication.getPrincipal() instanceof Long id ? id : null;
        sesion.cerrar(userId, body == null ? null : body.get("reason"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                              HttpServletRequest http) {
        recuperacion.solicitar(request.getEmail(), http.getRemoteAddr());
        // Misma respuesta exista o no la cuenta
        return ResponseEntity.ok(Map.of("message", "Si el correo está registrado, te enviamos un código de 6 dígitos"));
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<Void> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        recuperacion.verificar(request.email(), request.code());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        recuperacion.restablecer(request.getEmail(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400, "error", "Bad Request", "message", "El campo refreshToken es obligatorio"));
        }
        try {
            return ResponseEntity.ok(Map.of("token", sesion.renovar(refreshToken)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401, "error", "Unauthorized", "message", e.getMessage()));
        }
    }
}
