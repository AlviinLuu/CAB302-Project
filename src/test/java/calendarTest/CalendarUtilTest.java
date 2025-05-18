package calendarTest;

import com.example.cab302project.models.CalendarDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CalendarUtilTest {
    @BeforeEach
    public void setup(){

    }

    @Test
    public void testIsEventInProgressBasic1_True(){
        //Basic case
        LocalDateTime start = LocalDateTime.of
                (2025,4,2,4,0,0);
        LocalDateTime end = LocalDateTime.of
                (2025,4,2,8,0,0);
        LocalDateTime current = LocalDateTime.of
                (2025,4,2,7,0,0);
        assertTrue(CalendarDAO.IsEventInProgress(start,end,current));
    }
    @Test
    public void testIsEventInProgressBasic1_True2(){
        //Basic case
        LocalDateTime start = LocalDateTime.of
                (2025,8,2,10,30,0);
        LocalDateTime end = LocalDateTime.of
                (2025,8,2,14,45,0);
        LocalDateTime current = LocalDateTime.of
                (2025,8,2,12,0,0);
        assertTrue(CalendarDAO.IsEventInProgress(start,end,current));
    }
    @Test
    public void testIsEventInProgressBasic1_False1(){
        //Basic case
        LocalDateTime start = LocalDateTime.of
                (2025,8,2,10,30,0);
        LocalDateTime end = LocalDateTime.of
                (2025,8,2,14,45,0);
        LocalDateTime current = LocalDateTime.of
                (2025,2,2,12,0,0);
        assertFalse(CalendarDAO.IsEventInProgress(start,end,current));
    }

    @Test
    public void testIsEventInProgressEdge2(){
        //current time is start time (should be true)
        LocalDateTime start = LocalDateTime.of
                (2025,4,2,4,0,0);
        LocalDateTime end = LocalDateTime.of
                (2025,4,2,8,0,0);
        LocalDateTime current = LocalDateTime.of
                (2025,4,2,4,0,0);
        assertTrue(CalendarDAO.IsEventInProgress(start,end,current));
    }

    /**
     * tests overload of method which takes seperate LocalDate and LocalTime
     * Otherwise same as test case 1
     */
    @Test
    public void testIsEventInProgressOverload1(){

        LocalDate startDate = LocalDate.of
                (2025,4,2);
        LocalTime startTime = LocalTime.of
                (4,0,0);

        LocalDate endDate = LocalDate.of
                (2025,4,2);
        LocalTime endTime = LocalTime.of
                (8,0,0);

        LocalDate currentDate = LocalDate.of
                (2025,4,2);
        LocalTime currentTime = LocalTime.of
                (7,0,0);

        assertTrue(CalendarDAO.IsEventInProgress(startDate,startTime,endDate,endTime,currentDate,currentTime));
    }

    /**
     * tests overload of method which tak/es seperate LocalDate and LocalTime
     * Otherwise same as edge test case (start time and current time are the same)
     */
    @Test
    public void testIsEventInProgressOverload1_True(){

        LocalDate startDate = LocalDate.of
                (2025,4,2);
        LocalTime startTime = LocalTime.of
                (4,0,0);

        LocalDate endDate = LocalDate.of
                (2025,4,2);
        LocalTime endTime = LocalTime.of
                (8,0,0);

        LocalDate currentDate = LocalDate.of
                (2025,4,2);
        LocalTime currentTime = LocalTime.of
                (7,0,0);

        assertTrue(CalendarDAO.IsEventInProgress(startDate,startTime,endDate,endTime,currentDate,currentTime));
    }
    @Test
    public void testIsEventInProgressOverload1_False(){

        LocalDate startDate = LocalDate.of
                (2025,4,2);
        LocalTime startTime = LocalTime.of
                (4,0,0);

        LocalDate endDate = LocalDate.of
                (2025,4,2);
        LocalTime endTime = LocalTime.of
                (8,0,0);

        LocalDate currentDate = LocalDate.of
                (2025,4,2);
        LocalTime currentTime = LocalTime.of
                (9,0,0);

        assertFalse(CalendarDAO.IsEventInProgress(startDate,startTime,endDate,endTime,currentDate,currentTime));
    }



    @Test
    public void testIsEventInProgressOverload2_True(){

        LocalDateTime start = LocalDateTime.of
                (2025,4,2,4,0,0);
        LocalDateTime end = LocalDateTime.of
                (2025,4,2,8,0,0);
        LocalDate currentDate = LocalDate.of
                (2025,4,2);
        LocalTime currentTime = LocalTime.of
                (7,0,0);

        assertTrue(CalendarDAO.IsEventInProgress(start,end,currentDate,currentTime));
    }
    @Test
    public void testIsEventInProgressOverload2_False(){

        LocalDateTime start = LocalDateTime.of
                (2025,4,2,4,0,0);
        LocalDateTime end = LocalDateTime.of
                (2025,4,2,8,0,0);
        LocalDate currentDate = LocalDate.of
                (2025,4,2);
        LocalTime currentTime = LocalTime.of
                (9,0,0);

        assertFalse(CalendarDAO.IsEventInProgress(start,end,currentDate,currentTime));
    }
}
