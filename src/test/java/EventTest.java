import com.example.cab302project.models.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {

    private Event eventValidFormat;
    private Event eventIcsFormat;
    private Event eventInvalidFormat;

    @BeforeEach
    public void setUp() {
        // Event with already formatted date strings (MM/dd/yyyy HH:mm:ss)
        eventValidFormat = new Event("ValidEvent",
                "05/31/2025 14:00:00",
                "05/31/2025 15:00:00",
                "testuser");

        // Event with ICS date format (yyyyMMdd'T'HHmmss) that needs +10 hours offset
        eventIcsFormat = new Event("IcsEvent",
                "20250531T040000",
                "20250531T050000",
                "testuser");

        // Event with invalid date format
        eventInvalidFormat = new Event("InvalidEvent",
                "invalid-date-start",
                "invalid-date-end",
                "testuser");
    }

    @Test
    public void testGetStartTime_parsesValidFormatCorrectly() {
        String parsed = eventValidFormat.getStart_time();
        assertEquals("05/31/2025 14:00:00", parsed);
    }

    @Test
    public void testGetEndTime_parsesValidFormatCorrectly() {
        String parsed = eventValidFormat.getEnd_time();
        assertEquals("05/31/2025 15:00:00", parsed);
    }

    @Test
    public void testGetStartTime_parsesIcsFormatWithOffset() {
        String parsed = eventIcsFormat.getStart_time();
        // 04:00 + 10 hours = 14:00 on same day
        assertEquals("05/31/2025 14:00:00", parsed);
    }

    @Test
    public void testGetEndTime_parsesIcsFormatWithOffset() {
        String parsed = eventIcsFormat.getEnd_time();
        // 05:00 + 10 hours = 15:00 on same day
        assertEquals("05/31/2025 15:00:00", parsed);
    }

    @Test
    public void testGetStartTime_returnsInvalidDateForBadInput() {
        String parsed = eventInvalidFormat.getStart_time();
        assertEquals("Invalid Date", parsed);
    }

    @Test
    public void testGetEndTime_returnsInvalidDateForBadInput() {
        String parsed = eventInvalidFormat.getEnd_time();
        assertEquals("Invalid Date", parsed);
    }

    @Test
    public void testGetStart_Time_LocalDateTime_parsesCorrectly() {
        LocalDateTime expected = LocalDateTime.of(2025, 5, 31, 14, 0, 0);
        assertEquals(expected, eventValidFormat.getStart_Time_LocalDateTime());
    }

    @Test
    public void testGetEnd_Time_LocalDateTime_parsesCorrectly() {
        LocalDateTime expected = LocalDateTime.of(2025, 5, 31, 15, 0, 0);
        assertEquals(expected, eventValidFormat.getEnd_Time_LocalDateTime());
    }

    @Test
    public void testIsEventInProgress_returnsTrueDuringEvent() {
        LocalDateTime duringEvent = LocalDateTime.of(2025, 5, 31, 14, 30, 0);
        assertTrue(eventValidFormat.IsEventInProgress(duringEvent));
    }

    @Test
    public void testIsEventInProgress_returnsTrueAtStartTime() {
        LocalDateTime atStart = LocalDateTime.of(2025, 5, 31, 14, 0, 0);
        assertTrue(eventValidFormat.IsEventInProgress(atStart));
    }

    @Test
    public void testIsEventInProgress_returnsTrueAtEndTime() {
        LocalDateTime atEnd = LocalDateTime.of(2025, 5, 31, 15, 0, 0);
        assertTrue(eventValidFormat.IsEventInProgress(atEnd));
    }

    @Test
    public void testIsEventInProgress_returnsFalseOutsideEvent() {
        LocalDateTime beforeEvent = LocalDateTime.of(2025, 5, 31, 13, 59, 59);
        LocalDateTime afterEvent = LocalDateTime.of(2025, 5, 31, 15, 0, 1);
        assertFalse(eventValidFormat.IsEventInProgress(beforeEvent));
        assertFalse(eventValidFormat.IsEventInProgress(afterEvent));
    }
}
