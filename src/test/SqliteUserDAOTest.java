import com.example.cab302project.models.SqliteUserDAO;
import com.example.cab302project.models.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class SqliteUserDAOTest {
    private SqliteUserDAO userDAO;

    @Test
    public void testAddAndGetUserByEmail() {
        User user = new User("john", "secret", "john@email.com");
        userDAO.addUser(user);

        User fetched = userDAO.getUserByEmail("john@email.com");
        assertNotNull(fetched);
        assertEquals("john", fetched.getUsername());
    }
}