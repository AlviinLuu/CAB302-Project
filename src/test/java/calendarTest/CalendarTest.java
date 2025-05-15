package calendarTest;

import com.example.cab302project.models.Event;
import com.example.cab302project.models.User;
import com.example.cab302project.services.Authentication;
import com.example.cab302project.models.CalendarDAO;
import com.example.cab302project.services.CalendarImportView;
import com.example.cab302project.util.Session;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalendarTest {
    private CalendarDAO calendarDAO;
    private Authentication authService;

    @BeforeEach
    public void setup(){
        //log in to test account
        authService = new Authentication();
        authService.login("test@test.com","test");
        User testUser = new User("Test","test","test@test.com");
        Session.setLoggedInUser(testUser);

        //Create Calendar object (retrieves events from logged in user)
        calendarDAO = new CalendarDAO();
        //loadCalendar();

    }

    private void loadCalendar(){
        //URL calURL = getClass().getResource("timeTable.com.ics");
        //assert calURL != null;

        File calFile = new File("./src/test/java/calendarTest/testcalendar2.ics");
        CalendarImportView.importCalendarFile(calFile, Session.getLoggedInUser().getId());
    }
    @Test
    public void TestCanReadCalendar(){

    }
    @Test
    public void testGetEventByDateNoEvent(){
        //there is no event on this day, so return value should be null
        var date = LocalDate.of(2025,1,4);
        List<Event> res = new ArrayList<Event>();
        res = calendarDAO.getEventsByDate(date);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testCalendarImport(){
        calendarDAO.ClearEvents();
        loadCalendar();
        var cal = calendarDAO.getAllEvents();
        assertFalse(cal.isEmpty());
        assertEquals(cal.size(),4);
    }

    //@Test
    public void testEvent1A(){
        LocalDate date = LocalDate.of(2025,5,7);
        List<Event> res;
        Event event = null;
        try {
            res = calendarDAO.getEventsByDate(date);
            event = res.getFirst();
        }catch (Exception e){
            System.out.println("error: no events found");
        }
        if (event != null){
            assertEquals("Event 1 A", event.getName());
        }else{
            fail();
        }

    }

    @Test
    public void testSingleEvent(){
        LocalDate date = LocalDate.of(2025,5,8);
        List<Event> res;
        Event event = null;
        try {
            res = calendarDAO.getEventsByDate(date);
            event = res.getFirst();
        }catch (Exception e){
            System.out.println("error: no events found");
        }

        assertEquals("Event 2 A",event.getName());
    }

    @Test
    public void testTwoEventsSameDay(){
        LocalDate date = LocalDate.of(2025,5,9);
        var res = calendarDAO.getEventsByDate(date);
        assertFalse(res.isEmpty());
        assertEquals(2,res.size());
    }

    //@Test
    public void testclearevents(){
        calendarDAO.ClearEvents();

    }

}
