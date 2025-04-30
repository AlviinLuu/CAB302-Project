import com.example.cab302project.models.SqliteUserDAO;
import com.example.cab302project.models.User;
import com.example.cab302project.models.SqliteConnection;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class SqliteUserDAOTest {
    private SqliteUserDAO userDAO;
    @BeforeEach
    public void setUp() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        SqliteConnection.injectTestConnection(connection);
        userDAO = new SqliteUserDAO();
    }

    @Test
    public void testAddAndGetUserByEmail() {
        User user = new User("john", "secret", "john@email.com");
        userDAO.addUser(user);

        User fetched = userDAO.getUserByEmail("john@email.com");
        assertNotNull(fetched);
        assertEquals("john", fetched.getUsername());
    }

    @Test
    public void testAddUserAlreadyExists() {
        User user1 = new User("john", "secret", "john@email.com");
        User user2 = new User("john", "secret", "john@email.com");

        userDAO.addUser(user1);
        userDAO.addUser(user2);  // Should not insert

        assertNotNull(userDAO.getUserByEmail("john@email.com"));
    }
}