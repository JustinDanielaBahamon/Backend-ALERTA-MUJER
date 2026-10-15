import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "Admin2024!";
        String hash = encoder.encode(password);
        System.out.println("Hash generado: " + hash);
        System.out.println("Verificación: " + encoder.matches(password, hash));
    }
}
