
import com.example.cab302project.models.*;
import com.example.cab302project.util.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class CalendarDAOTest {

    private static final LocalDate TEST_DATE   = LocalDate.of(2021, 9, 15);
    private static final String    ALICE_EMAIL = "alice@example.com";
    private static final String    BOB_EMAIL   = "bob@example.com";

    private SqliteUserDAO userDAO;

    @BeforeEach
    public void setUp() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        SqliteConnection.injectTestConnection(conn);

        userDAO = new SqliteUserDAO();
        userDAO.addUser(new User("alice", "pw", ALICE_EMAIL));
        userDAO.addUser(new User("bob",   "pw", BOB_EMAIL));
        User alice = userDAO.getUserByEmail(ALICE_EMAIL);
        User bob   = userDAO.getUserByEmail(BOB_EMAIL);

        Session.setLoggedInUser(alice);
        userDAO.insertEvent(alice.getId(), ALICE_EMAIL,
                "Meeting",   "09/15/2021 10:00:00", "09/15/2021 11:00:00"
        );
        userDAO.insertEvent(alice.getId(), ALICE_EMAIL,
                "EarlyBird", "09/15/2021 00:30:00", "09/15/2021 00:45:00"
        );
        userDAO.insertEvent(alice.getId(), ALICE_EMAIL,
                "NextDay",   "09/16/2021 00:30:00", "09/16/2021 01:00:00"
        );

        Session.setLoggedInUser(bob);
        userDAO.insertEvent(bob.getId(), BOB_EMAIL,
                "BobEvent", "09/15/2021 12:00:00", "09/15/2021 13:00:00"
        );

        Session.setLoggedInUser(alice);
    }

    @AfterEach
    public void tearDown() {
        Session.clear();
    }

    @Test
    public void testDatabaseConnectionIsValid() throws Exception {
        Connection raw = SqliteConnection.getInstance();
        assertNotNull(raw, "Connection should not be null");
        assertTrue(raw.isValid(1), "Connection should be valid");
    }

    @Test
    public void testGetAllEventsOnDay_returnsOnlyMeeting() {
        CalendarDAO dao = new CalendarDAO(TEST_DATE, Period.ofDays(1), TimeUnit.DAYS);
        List<Event> evts = dao.getAllEventsOnDay(TEST_DATE);
        assertEquals(1, evts.size());
        assertEquals("Meeting", evts.get(0).getName());
    }

    @Test
    public void testRetrieveSelectedUserEvents_bob() {
        Session.setLoggedInUser(userDAO.getUserByEmail(BOB_EMAIL));
        CalendarDAO dao = new CalendarDAO(TEST_DATE, Period.ofDays(1), TimeUnit.DAYS);
        List<Event> evts = dao.getAllEventsOnDay(TEST_DATE);
        assertEquals(1, evts.size());
        assertEquals("BobEvent", evts.get(0).getName());
    }

    @Test
    public void testGetFirstEventForInterval_days_exactMatchOnly() {
        CalendarDAO dao = new CalendarDAO(TEST_DATE, Period.ofDays(1), TimeUnit.DAYS);
        assertNull(dao.getFirstEventForInterval(TEST_DATE.atTime(9, 0), TimeUnit.DAYS));
        Event at10 = dao.getFirstEventForInterval(TEST_DATE.atTime(10, 0), TimeUnit.DAYS);
        assertNotNull(at10);
        assertEquals("Meeting", at10.getName());
    }

    @Test
    public void testGetFirstEventForInterval_hours() {
        CalendarDAO dao = new CalendarDAO(TEST_DATE, Period.ofDays(1), TimeUnit.DAYS);
        Event ten = dao.getFirstEventForInterval(TEST_DATE, 10, TimeUnit.HOURS);
        assertNotNull(ten);
        assertEquals("Meeting", ten.getName());
        assertNull(dao.getFirstEventForInterval(TEST_DATE, 8, TimeUnit.HOURS));
    }

    @Test
    public void testCurrentEvents_and_isAnyEventInProgress() {
        CalendarDAO dao = new CalendarDAO(TEST_DATE, Period.ofDays(1), TimeUnit.DAYS);
        LocalDateTime at10 = TEST_DATE.atTime(10, 0);
        assertTrue(dao.IsAnyEventInProgress(at10));
        List<Event> cur = dao.getCurrentEvents(at10);
        assertEquals(1, cur.size());
        assertEquals("Meeting", cur.get(0).getName());

        LocalDateTime at8 = TEST_DATE.atTime(8, 0);
        assertFalse(dao.IsAnyEventInProgress(at8));
        assertTrue(dao.getCurrentEvents(at8).isEmpty());
    }

    @Test
    public void testWeekView_includesNextDayEvent() {
        CalendarDAO weekDao = new CalendarDAO(TEST_DATE, Period.ofDays(7), TimeUnit.DAYS);
        List<Event> tom = weekDao.getAllEventsOnDay(TEST_DATE.plusDays(1));
        assertEquals(1, tom.size());
        Event e = tom.get(0);
        assertEquals("NextDay", e.getName());
        assertEquals("09/16/2021 00:30:00", e.getStart_time());
        assertEquals("09/16/2021 01:00:00", e.getEnd_time());
    }

    @Test
    public void testMonthView_includesAllAliceEvents() {
        CalendarDAO monthDao = new CalendarDAO(TEST_DATE, Period.ofMonths(1), TimeUnit.DAYS);
        Event m = monthDao.getFirstEventForInterval(TEST_DATE.atTime(10,0), TimeUnit.DAYS);
        assertEquals("Meeting", m.getName());
        Event n = monthDao.getFirstEventForInterval(TEST_DATE.plusDays(1).atTime(0,30), TimeUnit.DAYS);
        assertEquals("NextDay", n.getName());
    }

    @Test
    public void testZipDateAndTime_concatenatesCorrectly() {
        LocalDate d = LocalDate.of(2021, 9, 15);
        LocalTime t = LocalTime.of(14, 30);
        LocalDateTime dt = CalendarDAO.zipDateAndTime(d, t);
        assertEquals(LocalDateTime.of(2021, 9, 15, 14, 30), dt);
    }
}
