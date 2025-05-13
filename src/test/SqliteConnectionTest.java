import com.example.cab302project.models.SqliteConnection;
import com.example.cab302project.services.Authentication;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

public class SqliteConnectionTest {
    private Authentication Sqlconnection;
    @Test
    public void testConnectionIsNotNull() { //Test Connection is not Null
        Connection conn = SqliteConnection.getInstance();
        assertNotNull(conn, "Connection should not be null");
    }
}