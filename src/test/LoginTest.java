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
    public void testLoginNullValues() {assertFalse(authService.login(null, null));}

    @Test
    public void testLoginInvalidEmailFormat() {assertFalse(authService.login("alice@invalid", "pass123"));}

    @Test
    public void testLoginNonRegisteredUser() {assertFalse(authService.login("newuser", "newpassword"));}

    @Test
    public void testLoginWhitespaceOnly() {assertFalse(authService.login("   ", "   "));}
    @Test
    public void testLoginSpecialCharacters() {
        User specialUser = new User("user!@#", "pass!@#", "special@email.com");
        authService.register(specialUser);
        assertTrue(authService.login("user!@#", "pass!@#"));
    }
    @Test
    public void testLoginLongCredentials() {
        String longUsername = "user".repeat(50);
        String longPassword = "pass".repeat(50);
        User longUser = new User(longUsername, longPassword, "longuser@email.com");
        authService.register(longUser);
        assertTrue(authService.login(longUsername, longPassword));
    }
    @Test
    public void testLoginCaseSensitivity() {
        assertFalse(authService.login("ALICE", "pass123"));  // "ALICE" is different from "alice"
    }



}
