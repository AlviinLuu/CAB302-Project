import com.example.cab302project.models.SqliteConnection;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class SqliteConnectionTest {

    @Test
    public void testConnectionIsNotNull() { //Test Connection is not Null
        Connection conn = SqliteConnection.getInstance();
        assertNotNull(conn, "Connection should not be null");
    }
}