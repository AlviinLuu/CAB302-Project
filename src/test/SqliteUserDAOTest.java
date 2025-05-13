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

    @Test
    public void testValidateUserSuccess() {
        User user = new User("alice", "password123", "alice@email.com");
        userDAO.addUser(user);

        assertTrue(userDAO.validateUser("alice@email.com", "password123"));
    }

    @Test
    public void testValidateUserFailure() {
        User user = new User("bob", "mypassword", "bob@email.com");
        userDAO.addUser(user);

        assertFalse(userDAO.validateUser("bob@email.com", "wrongpassword"));
    }

    @Test
    public void testUpdateBio() {
        User user = new User("charlie", "pass", "charlie@email.com");
        userDAO.addUser(user);

        assertTrue(userDAO.updateBio("charlie@email.com", "Updated bio"));
        User updated = userDAO.getUserByEmail("charlie@email.com");
        assertEquals("Updated bio", updated.getBio());
    }

    @Test
    public void testUpdatePassword() {
        User user = new User("diana", "oldpass", "diana@email.com");
        userDAO.addUser(user);

        assertTrue(userDAO.updatePassword("diana@email.com", "newpass"));
        assertTrue(userDAO.validateUser("diana@email.com", "newpass"));
    }

    @Test
    public void testSendAndAcceptFriendRequest() throws SQLException {
        User user1 = new User("eva", "pass1", "eva@email.com");
        User user2 = new User("frank", "pass2", "frank@email.com");
        userDAO.addUser(user1);
        userDAO.addUser(user2);

        assertTrue(userDAO.sendFriendRequest("eva", "frank"));
        assertTrue(userDAO.acceptFriendRequest("eva", "frank"));

        assertEquals(1, userDAO.getFriends("eva").size());
        assertEquals("frank", userDAO.getFriends("eva").get(0).getUsername());
    }

    @Test
    public void testDeclineFriendRequest() throws SQLException {
        User sender = new User("george", "pass", "george@email.com");
        User receiver = new User("hannah", "pass", "hannah@email.com");
        userDAO.addUser(sender);
        userDAO.addUser(receiver);

        assertTrue(userDAO.sendFriendRequest("george", "hannah"));
        assertTrue(userDAO.declineFriendRequest("george", "hannah"));
        assertEquals(0, userDAO.getFriends("hannah").size());
    }

    @Test
    public void testGetPendingFriendRequests() {
        User sender = new User("ian", "pass", "ian@email.com");
        User receiver = new User("jane", "pass", "jane@email.com");
        userDAO.addUser(sender);
        userDAO.addUser(receiver);

        userDAO.sendFriendRequest("ian", "jane");
        var pendingRequests = userDAO.getPendingFriendRequests("jane");

        assertEquals(1, pendingRequests.size());
        assertEquals("ian", pendingRequests.get(0).getUsername());
    }

    @Test
    public void testUpdateEmail() {
        User user = new User("kelly", "pass", "kelly@email.com");
        userDAO.addUser(user);

        assertTrue(userDAO.updateEmail("kelly@email.com", "kelly.new@email.com"));
        assertNotNull(userDAO.getUserByEmail("kelly.new@email.com"));
        assertNull(userDAO.getUserByEmail("kelly@email.com"));
    }

    @Test
    public void testDeleteUser() {
        User user = new User("leo", "pass", "leo@email.com");
        userDAO.addUser(user);

        userDAO.deleteUser("leo");
        assertNull(userDAO.getUserByUsername("leo"));
    }
}