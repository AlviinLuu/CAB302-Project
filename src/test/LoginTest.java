import com.example.cab302project.models.User;
import com.example.cab302project.services.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {
    private Authentication authService;

    @BeforeEach
    public void setUp() {
        authService = new Authentication();
        User testUser = new User("alice", "pass123", "alice@email.com");
        authService.register(testUser);  // Simulate user registration
    }

    @Test
    public void testLoginSuccess() {
        assertTrue(authService.login("alice", "pass123"));
    }

    @Test
    public void testLoginWrongPassword() {
        assertFalse(authService.login("alice", "wrong"));
    }

    @Test
    public void testLoginUserNotFound() {
        assertFalse(authService.login("bob", "pass123"));
    }

    @Test
    public void testLoginEmptyUsername() { assertFalse(authService.login("", "pass123"));}

    @Test
    public void testLoginEmptyPassword() {assertFalse(authService.login("alice", ""));}

    @Test
    public void testLoginNullValues() {assertThrows(NullPointerException.class, () -> {authService.login(null, null);});}
}
