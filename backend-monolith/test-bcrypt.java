import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBcrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "Admin2024!";
        String hash = encoder.encode(password);
        System.out.println("Generated hash: " + hash);
        
        String existingHash = "$2a$10$rZ6qJYzQX9EZDfGvK5HY4.KmVBZK2pXJ8YqZGqN5YWKPzL5JsXw9u";
        boolean matches = encoder.matches(password, existingHash);
        System.out.println("Password matches existing hash: " + matches);
    }
}
