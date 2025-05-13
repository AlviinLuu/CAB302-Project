import com.example.cab302project.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;

    @BeforeEach
    public void setUp() {
        // Create a new user before each test
        user = new User("alice", "password123", "alice@example.com");
    }

    @Test
    public void testGetUsername() {
        // Test the getter for username
        assertEquals("alice", user.getUsername(), "Username should be 'alice'");
    }

    @Test
    public void testSetUsername() {
        // Test the setter for username
        user.setUsername("bob");
        assertEquals("bob", user.getUsername(), "Username should be 'bob' after setting it");
    }

    @Test
    public void testGetPassword() {
        // Test the getter for password
        assertEquals("password123", user.getPassword(), "Password should be 'password123'");
    }

    @Test
    public void testSetPassword() {
        // Test the setter for password
        user.setPassword("newpassword");
        assertEquals("newpassword", user.getPassword(), "Password should be 'newpassword' after setting it");
    }

    @Test
    public void testSetPasswordWeird() {
        // Test the setter for password
        String pass = "sdkgiroo 45kfs";
        user.setPassword(pass);
        assertEquals(pass, user.getPassword(), "Password should be 'newpassword' after setting it");
    }

    //@Test Seems like this should be handled differently by the app so ill leave this commented for now
    public void testSetPasswordNull() {
        // Test the setter for password

            // Test the setter for password
            String pass = "password";
            user.setPassword(null);
            assertEquals(pass, user.getPassword(), "Password should be 'newpassword' after setting it");

    }


}