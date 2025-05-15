package calendarTest;

import com.example.cab302project.models.Event;
import com.example.cab302project.models.User;
import com.example.cab302project.services.Authentication;
import com.example.cab302project.services.Calendar;
import com.example.cab302project.services.CalendarImportView;
import com.example.cab302project.util.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalendarAccessTest {
    private Calendar calendar;
    private Authentication authService;



    @BeforeEach
    public void setup(){
        //log in to test account
        authService = new Authentication();
        authService.login("test@test.com","test");
        User testUser = new User("Test","test","test@test.com");
        Session.setLoggedInUser(testUser);

        //Create Calendar object (retrieves events from logged in user)
        calendar = new Calendar();
        //loadCalendar();

    }

    private void loadCalendar(){
        //URL calURL = getClass().getResource("timeTable.com.ics");
        //assert calURL != null;
        File directory = new File("./");
        System.out.println(directory.getAbsolutePath());

        File calFile = new File("./src/test/java/calendarTest/testcalendar.ics");
        CalendarImportView.importCalendarFile(calFile, Session.getLoggedInUser().getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {""})
    public void testEventsForDate(String date){

    }

    @Test
    public void testGetEventByDate(){
        var lDate = LocalDate.of(2025,5,8);
        List<Event> res;
        res = calendar.getEventsByDate(lDate);

        assertFalse(res.isEmpty());
        assertEquals("Event 3 A", res.getFirst().getName());
    }

    @Test
    public void testGetEventByDateTwoEvents(){
        var lDate = LocalDate.of(2025,5,9);
        List<Event> res;
        res = calendar.getEventsByDate(lDate);

        assertFalse(res.isEmpty());
        assertEquals("Event 3 A", res.getFirst().getName());
    }

    @Test
    public void testGetEventByDateNoEvent(){
        //there is no event on this day, so return value should be null
        var date = LocalDate.of(2025,1,4);
        List<Event> res = new ArrayList<Event>();
        res = calendar.getEventsByDate(date);
        assertTrue(res.isEmpty());
    }


}
