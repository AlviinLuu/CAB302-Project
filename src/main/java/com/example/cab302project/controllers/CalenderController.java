package com.example.cab302project.controllers;

import com.example.cab302project.models.CalendarDAO;
import com.example.cab302project.models.SqliteUserDAO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.time.*;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import com.example.cab302project.models.Event;

import com.example.cab302project.models.User;
import com.example.cab302project.util.Session;

/**
 * Controller class for managing the main calendar view in the application.
 * Handles calendar rendering for day, week, month, and year views,
 * user interface interaction, and navigation between pages.
 */
public class CalenderController {
    // === FXML UI Elements ===
    @FXML private Label monthLabel;
    @FXML private GridPane calendarGrid;
    @FXML private GridPane weekGrid;
    @FXML private GridPane monthGrid;
    @FXML private GridPane yearGrid;
    @FXML private GridPane dayGrid;
    @FXML private ImageView logoImage;
    @FXML private VBox todayButton;
    @FXML private VBox weekView;
    @FXML private VBox yearView;
    @FXML private VBox monthView;
    @FXML private VBox dayView;

    @FXML private VBox aiSidebar;
    @FXML private SplitPane splitPane;

    @FXML private VBox mainContent;

    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private ComboBox<Integer> dayComboBox;


    @FXML private ToggleGroup viewToggleGroup;
    @FXML private RadioButton dayRadio;
    @FXML private RadioButton weekRadio;
    @FXML private RadioButton monthRadio;
    @FXML private RadioButton yearRadio;
    @FXML private TextArea userInputArea;
    @FXML private VBox responseArea;
    @FXML private Button profileButton;
    @FXML private GridPane miniDayView;

    private List<com.example.cab302project.models.Event> testEvents = new ArrayList<Event>();
    private Event newEvent = new Event("Lunch", "20250314T032000Z","20250314T033000Z","test@test.com");

    private final String fillColour = "-fx-background-color: rgb(115,49,196); -fx-text-fill: white";

    // === State ===
    private LocalDate currentDate = LocalDate.now();
    private CalendarDAO weekDao;
    private static final double HEADER_PERCENT = 8;     // header row share
    private static final double SIDEBAR_PERCENT = 10;   // hour-column share

    // === Initialization ===
    /**
     * Initializes the calendar view, UI elements, and sets default values.
     * Also binds event listeners and loads session user.
     * @throws NullPointerException of session user is null
     */
    @FXML
    public void initialize() {
        User sessionUser = Session.getLoggedInUser();
        if (sessionUser == null) {
            System.err.println("ERROR: No user in session Call Session.setLoggedInUser(...) after login.");
            return;
        }

        testEvents.add(newEvent);
        // Load logo image
        Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
        logoImage.setImage(logo);

        // Initialize ComboBoxes
        monthComboBox.getItems().addAll(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        );

        int currentYear = LocalDate.now().getYear();
        int minYear = currentYear - 10;
        int maxYear = currentYear + 50;

        for (int year = minYear; year <= maxYear + 10; year++) {
            yearComboBox.getItems().add(year);
        }

        // Set up combo boxes
        monthComboBox.setValue(currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        yearComboBox.setValue(currentDate.getYear());

        // Populate day combo box with default 1-31
        for (int i = 1; i <= 31; i++) {
            dayComboBox.getItems().add(i);
        }
        dayComboBox.setValue(currentDate.getDayOfMonth());

        dayComboBox.setVisible(false);
        dayComboBox.setManaged(false);


        // Update dayComboBox based on month/year changes
        monthComboBox.setOnAction(e -> updateDayComboBox());
        monthComboBox.setOnAction(e -> updateDayComboBox());

        // Set up toggle group
        viewToggleGroup = new ToggleGroup();
        dayRadio.setToggleGroup(viewToggleGroup);
        weekRadio.setToggleGroup(viewToggleGroup);
        monthRadio.setToggleGroup(viewToggleGroup);
        yearRadio.setToggleGroup(viewToggleGroup);
        monthRadio.setSelected(true); // Default view
        splitPane.setDividerPositions(0.2);
        renderMiniDayView();

        updateDayComboBox();
        viewToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                RadioButton selected = (RadioButton) newVal;
                boolean isDayView = selected == dayRadio || selected == weekRadio;

                dayComboBox.setVisible(isDayView);
                dayComboBox.setManaged(isDayView);

                updateCalendar();
            }
        });
        updateCalendar();

        // Show username on the profile button
        try {
            if (sessionUser != null && sessionUser.getUsername() != null) {
                profileButton.setText(sessionUser.getUsername());
            } else {
                profileButton.setText("Profile");
                System.err.println("Warning: sessionUser or username is null.");
            }
        } catch (Exception e) {
            profileButton.setText("Profile");
            e.printStackTrace(); //
        }


    }


    // === Navigation Buttons ===
    /**
     * Handles updates when the month or year is changed via ComboBoxes.
     * Updates the current date and re-renders the calendar.
     */
    @FXML
    private void onMonthYearSelected() {
        String selectedMonth = monthComboBox.getValue();
        Integer selectedYear = yearComboBox.getValue();
        Integer selectedDay = dayComboBox.getValue(); // <-- store before clearing

        if (selectedMonth != null && selectedYear != null) {
            int monthNumber = monthComboBox.getItems().indexOf(selectedMonth) + 1;

            // Fallback to 1 if null
            int preservedDay = (selectedDay != null) ? selectedDay : 1;

            // Update currentDate using preserved day
            currentDate = LocalDate.of(selectedYear, monthNumber, 1);

            // Get actual number of days in that month
            int daysInMonth = currentDate.lengthOfMonth();

            // Repopulate day combo box
            dayComboBox.getItems().clear();
            for (int i = 1; i <= daysInMonth; i++) {
                dayComboBox.getItems().add(i);
            }

            // Set selected day to the preserved value (or last day if out of range)
            int correctedDay = Math.min(preservedDay, daysInMonth);
            dayComboBox.setValue(correctedDay);

            // Finally update currentDate with correct day
            currentDate = LocalDate.of(selectedYear, monthNumber, correctedDay);

            updateCalendar();
        }
    }

    // === View Handlers ===
    /**
     * Sets the view to the Year layout and updates visibility accordingly.
     */
    @FXML
    private void onYearView() {
        yearView.setVisible(true);
        yearView.setManaged(true);

        monthView.setVisible(false);
        monthView.setManaged(false);

        weekView.setVisible(false);
        weekView.setManaged(false);

        dayView.setVisible(false);
        dayView.setManaged(false);

        yearRadio.setSelected(true); // Ensure the Year button stays selected.
    }
    /**
     * Sets the view to the Month layout and updates visibility accordingly.
     */
    @FXML
    private void onMonthView() {
        monthView.setVisible(true);
        monthView.setManaged(true);

        yearView.setVisible(false);
        yearView.setManaged(false);

        weekView.setVisible(false);
        weekView.setManaged(false);

        dayView.setVisible(false);
        dayView.setManaged(false);

        monthRadio.setSelected(true);

        updateCalendar();
    }
    /**
     * Sets the view to the Week layout and updates visibility accordingly.
     */
    @FXML
    private void onWeekView() {
        weekView.setVisible(true);
        weekView.setManaged(true);

        monthView.setVisible(false);
        monthView.setManaged(false);

        yearView.setVisible(false);
        yearView.setManaged(false);

        dayView.setVisible(false);
        dayView.setManaged(false);

        weekRadio.setSelected(true); // Ensure the Week button stays selected.
        updateCalendar();
    }
    /**
     * Sets the view to the Day layout and updates visibility accordingly.
     * Also updates the current date.
     */
    @FXML
    private void onDayView() {
        dayView.setVisible(true);
        dayView.setManaged(true);

        monthView.setVisible(false);
        monthView.setManaged(false);

        weekView.setVisible(false);
        weekView.setManaged(false);

        yearView.setVisible(false);
        yearView.setManaged(false);

        dayRadio.setSelected(true); // Ensure the Day button stays selected.

        // Update selected day from combo
        int selectedDay = dayComboBox.getValue();
        currentDate = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), selectedDay);
        updateCalendar();
    }
    /**
     * Enlarges the logo on mouse hover.
     */
    @FXML
    private void onLogoHover() {
        logoImage.setScaleX(1.2);
        logoImage.setScaleY(1.2);
    }
    /**
     * Resets logo scale on mouse exit.
     */
    @FXML
    private void onLogoExit() {
        logoImage.setScaleX(1.0);
        logoImage.setScaleY(1.0);
    }
    /**
     * Opens the settings page and closes the current window.
     * @throws Exception if the FXML file cannot be loaded or displayed.
     */
    @FXML
    private void openSettingsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/settings-view.fxml"));
            Parent settingsRoot = loader.load();
            Stage currentStage = (Stage) mainContent.getScene().getWindow();
            Scene scene = currentStage.getScene();
            scene.setRoot(settingsRoot);
            currentStage.setTitle("Settings"); // Optional: Update window title
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the friends page and closes the current calendar window.
     * @throws Exception if the FXML file cannot be loaded or displayed.
     */
    @FXML
    private void openFriendsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/friends-view.fxml"));
            Parent friendsRoot = loader.load();
            Stage currentStage = (Stage) mainContent.getScene().getWindow();
            Scene scene = currentStage.getScene();
            scene.setRoot(friendsRoot);
            currentStage.setTitle("Friends"); // Optional: Update window title
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the user profile page and closes the current calendar view.
     * @throws Exception if the FXML file cannot be loaded.
     */
    @FXML
    private void handleLogOut() {
        Session.clear();
        System.out.println("Logging out...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/login-view.fxml"));
            Parent loginRoot = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Smart Schedule Assistant");

            // Match initial launch size exactly
            Scene scene = new Scene(loginRoot, 450, 600);
            loginStage.setScene(scene);
            loginStage.setResizable(true); // Match startup behavior
            loginStage.centerOnScreen();   // for polish

            loginStage.show();

            // Close the settings window
            Stage currentStage = (Stage) mainContent.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void openProfilePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/profile-view.fxml"));
            Parent profileRoot = loader.load();
            Stage currentStage = (Stage) mainContent.getScene().getWindow();
            Scene scene = currentStage.getScene();
            scene.setRoot(profileRoot);
            currentStage.setTitle("Profile");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // === Calendar Rendering ===
    /**
     * Updates the calendar based on the currently selected view (Day/Week/Month/Year).
     */
    private void updateCalendar() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthLabel.setText(currentDate.format(formatter));

        // Clear all grids to reset them
        weekGrid.getChildren().clear();
        monthGrid.getChildren().clear();
        yearGrid.getChildren().clear();
        dayGrid.getChildren().clear();

        // Add weekday headers to the appropriate grid (depending on the selected view)
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);

            // Bind font size to window size for responsive design
            dayLabel.styleProperty().bind(
                    Bindings.createStringBinding(() -> {
                        double size = weekGrid.getWidth() / 50; // Adjusted for weekGrid
                        return String.format(
                                "-fx-font-size: %.2fpx; -fx-font-weight: bold; -fx-text-fill: #444; -fx-border-color: #ccc; -fx-border-width: 0.5px; -fx-border-style: solid; -fx-alignment: center; -fx-background-color: #f2f2f2;",
                                size
                        );
                    }, weekGrid.widthProperty()) // Bind to weekGrid for proper font scaling
            );

            // Add the day label to the weekGrid
            GridPane.setHgrow(dayLabel, Priority.ALWAYS);
            GridPane.setVgrow(dayLabel, Priority.ALWAYS);
            weekGrid.add(dayLabel, i, 0);
        }

        // Determine which view is selected (Day, Week, Month, Year)
        RadioButton selectedView = (RadioButton) viewToggleGroup.getSelectedToggle();
        String view = selectedView != null ? selectedView.getText() : "Month";

        // Switch between different views: Day, Week, Month, Year
        switch (view) {
            case "Day":
                renderDayView();  // Call renderDayView when the Day view is selected
                break;
            case "Week":
                renderWeekView();  // Call renderWeekView when the Week view is selected
                break;
            case "Year":
                renderYearView();  // Call renderYearView when the Year view is selected
                break;
            case "Month":
                renderMonthView();
                break;
            default:
                renderMonthView();  // Default to Month view if no specific view is selected
                break;
        }
    }

    /**
     * Renders the month view in the calendar UI.
     * This method populates the `monthGrid` with header labels and daily cells,
     * including date numbers and any associated events.
     * It also highlights the current day.
     */
    private void renderMonthView() {

        // Clear previous layout
        monthGrid.getChildren().clear();
        monthGrid.getColumnConstraints().clear();
        monthGrid.getRowConstraints().clear();

        // Let the GridPane itself stretch inside its parent
        GridPane.setHgrow(monthGrid, Priority.ALWAYS);
        GridPane.setVgrow(monthGrid, Priority.ALWAYS);

        // Columns – seven equal slices
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.ALWAYS);
            monthGrid.getColumnConstraints().add(cc);
        }

        // How many rows will this month need?
        LocalDate first   = currentDate.withDayOfMonth(1);
        int daysInMonth   = currentDate.lengthOfMonth();
        int startCol      = (first.getDayOfWeek().getValue() + 6) % 7; // Monday-based (Mon = 0)

        // Ceil( (offset + days) / 7 )
        int weeks = (startCol + daysInMonth + 6) / 7;   // 5 or 6 for normal calendars
        int totalRows = weeks + 1;                      // +1 for the weekday header

        // Row constraints – share the full height
        double rowPercent = 100.0 / totalRows;
        for (int i = 0; i < totalRows; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(rowPercent);   // every row, incl. header, gets an equal slice
            rc.setVgrow(Priority.ALWAYS);
            rc.setFillHeight(true);
            monthGrid.getRowConstraints().add(rc);
        }

        // Weekday header (row 0)
        DayOfWeek[] daysOfWeek = DayOfWeek.values();   // MON…SUN
        for (int i = 0; i < 7; i++) {
            String name = daysOfWeek[i].getDisplayName(TextStyle.SHORT, Locale.getDefault());

            Label header = new Label(name);
            header.setAlignment(Pos.CENTER);
            header.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            header.styleProperty().bind(
                    Bindings.createStringBinding(() -> {
                        double size = monthGrid.getWidth() / 40;
                        return String.format(
                                "-fx-font-size: %.2fpx; -fx-font-weight: bold; -fx-text-fill: black;" +
                                        " -fx-border-color: #ccc; -fx-border-width: 0.5px;" +
                                        " -fx-background-color: #eee;", size);
                    }, monthGrid.widthProperty())
            );

            monthGrid.add(header, i, 0);
        }

        // Day cells (start at row 1)
        int row = 1;
        int col = startCol;
        CalendarDAO dao = new CalendarDAO(first, Period.ofMonths(1), TimeUnit.DAYS);

        for (int d = 1; d <= daysInMonth; d++) {

            LocalDate date = currentDate.withDayOfMonth(d);

            VBox cell = new VBox(2);
            cell.setAlignment(Pos.TOP_CENTER);
            cell.setPadding(new Insets(4));
            cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // Date number
            Label dateLbl = new Label(Integer.toString(d));
            dateLbl.setStyle("-fx-font-size: 15px; -fx-text-fill: black; -fx-font-weight: bold");
            cell.getChildren().add(dateLbl);

            // Events
            for (Event e : dao.getAllEventsOnDay(date)) {
                if (e != null) {
                    Label ev = new Label(e.getName());
                    ev.setMaxWidth(Double.MAX_VALUE);
                    ev.setWrapText(true);
                    ev.setStyle(
                            "-fx-background-color: #9B59B6; -fx-text-fill: white;" +
                                    " -fx-font-size: 10px; -fx-padding: 2 4; -fx-background-radius: 4; -fx-font-weight: bold");
                    cell.getChildren().add(ev);
                }
            }

            // Highlight today
            cell.styleProperty().bind(
                    Bindings.createStringBinding(() -> {
                        String s = "-fx-border-color: #ccc; -fx-border-width: 0.5px;";
                        if (date.isEqual(LocalDate.now())) {
                            s += "-fx-background-color: rgba(216,185,255,0.4);" +
                                    " -fx-border-color: #D8B9FF; -fx-border-width: 2px;" +
                                    " -fx-border-radius: 10px; -fx-background-radius: 10px;";
                        }
                        return s;
                    }, monthGrid.widthProperty())
            );

            monthGrid.add(cell, col, row);

            // next grid position
            if (++col > 6) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Renders the week view in the calendar UI.
     * Displays a grid with hour labels on the left and seven days of the week,
     * showing events that occur within each hourly time slot.
     */
    private void renderWeekView() {
        weekGrid.getChildren().clear(); // Clear previous content
        weekGrid.getColumnConstraints().clear();
        weekGrid.getRowConstraints().clear();

        // Set column constraints: first column for hour labels, then 7 columns for days
        ColumnConstraints hourCol = new ColumnConstraints();
        hourCol.setPercentWidth(10); // 10% width for hour labels column
        hourCol.setHgrow(Priority.NEVER);
        hourCol.setMinWidth(0);
        weekGrid.getColumnConstraints().add(hourCol);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints dayCol = new ColumnConstraints();
            dayCol.setPercentWidth(90.0 / 7); // Remaining 90% divided equally
            dayCol.setHgrow(Priority.ALWAYS);
            dayCol.setMinWidth(0);
            weekGrid.getColumnConstraints().add(dayCol);
        }

        // Set row constraints: one row for day headers, then 24 rows for hours
        RowConstraints headerRow = new RowConstraints();
        headerRow.setPercentHeight(5); // 5% for headers
        headerRow.setVgrow(Priority.NEVER);
        headerRow.setMinHeight(0);
        weekGrid.getRowConstraints().add(headerRow);

        for (int i = 0; i < 24; i++) {
            RowConstraints hourRow = new RowConstraints();
            hourRow.setPercentHeight(95.0 / 24); // Remaining 95% divided evenly
            hourRow.setVgrow(Priority.ALWAYS);
            hourRow.setMinHeight(30); // Set a minimum height for each hour row
            weekGrid.getRowConstraints().add(hourRow);
        }

        LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);
        var eventCalendar = new CalendarDAO(startOfWeek, Period.of(0, 0, 7), TimeUnit.HOURS);

        // Add day headers (Monday, Tuesday, etc.)
        for (int col = 1; col <= 7; col++) {
            LocalDate day = startOfWeek.plusDays(col - 1);
            final LocalDate currentDay = day; // For lambda usage

            Label dayLabel = new Label(day.getDayOfMonth() + " (" + day.getDayOfWeek().name().substring(0, 3) + ")");
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dayLabel.setMinHeight(60);

            dayLabel.styleProperty().bind(
                    Bindings.createStringBinding(() -> {
                        double size = Math.max(10, weekGrid.getWidth() / 40);
                        String baseStyle = String.format(
                                "-fx-font-size: %.2fpx; -fx-font-weight: bold; -fx-background-color: #eee; -fx-border-color: #ccc; -fx-border-width: 0.5px;",
                                size
                        );

                        // Highlight today
                        if (currentDay.isEqual(LocalDate.now())) {
                            baseStyle += "-fx-background-color: rgba(216,185,255,0.5); "
                                    + "-fx-border-color: #D8B9FF; "
                                    + "-fx-border-width: 2px; "
                                    + "-fx-border-radius: 20px; "
                                    + "-fx-background-radius: 20px;";
                        }
                        return baseStyle;
                    }, weekGrid.widthProperty())
            );

            GridPane.setHgrow(dayLabel, Priority.ALWAYS);
            GridPane.setVgrow(dayLabel, Priority.ALWAYS);
            weekGrid.add(dayLabel, col, 0);
        }

        // Add hour labels on the side (00:00 to 23:00)
        for (int row = 1; row <= 24; row++) {
            Label hourLabel = new Label(String.format("%02d:00", row - 1));
            hourLabel.setAlignment(Pos.CENTER);
            hourLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            hourLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-font-weight: bold;");
            hourLabel.setMinHeight(30); // Set a minimum height for hour labels
            GridPane.setHgrow(hourLabel, Priority.ALWAYS);
            GridPane.setVgrow(hourLabel, Priority.ALWAYS);
            weekGrid.add(hourLabel, 0, row);
        }

        // Add cells for each hour of each day
        for (int col = 1; col <= 7; col++) {
            for (int row = 1; row <= 24; row++) {
                Label cell = new Label();
                cell.setWrapText(true);
                cell.setAlignment(Pos.TOP_LEFT);
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                cell.setMinHeight(0);
                int pTB = 4;
                int pLR = 2;
                cell.setPadding(new Insets(pTB, pLR, pTB, pLR));
                cell.setMinHeight(Region.USE_PREF_SIZE);

                weekGrid.setMinHeight(Region.USE_PREF_SIZE);

                LocalDate day = startOfWeek.plusDays(col - 1);
                LocalTime time = LocalTime.of(row - 1, 0);
                LocalDateTime currentTime = day.atTime(time);

                Event cEvent = eventCalendar.getFirstEventForInterval(currentTime, TimeUnit.HOURS);

                String labelText = "";
                boolean hasEvent = false;
                if (cEvent != null) {
                    labelText = cEvent.getName();
                    hasEvent = true;
                }

                cell.setText(labelText);

                // Build base style
                double fontSize = 16;
                String borderStyle = "-fx-border-color: #ccc; -fx-border-width: 0.5px;";
                String fontStyle = String.format(
                        "-fx-font-size: %.3fpx; -fx-text-fill: white; -fx-font-weight: bold;", fontSize
                );

                // Use the purple highlight for events
                String eventHighlightStyle = hasEvent
                        ? "-fx-background-color: #9B59B6; -fx-font-size: 10px; -fx-padding: 2 4; -fx-background-radius: 4;"
                        : "";

                cell.setStyle(fontStyle + borderStyle + eventHighlightStyle);
                cell.setAlignment(Pos.CENTER_LEFT);

                GridPane.setHgrow(cell, Priority.ALWAYS);
                GridPane.setVgrow(cell, Priority.ALWAYS);
                weekGrid.add(cell, col, row);
            }
        }
    }

    /**
     * Renders the day view in the calendar UI.
     * Displays a timeline of 24 hours and populates the timeline based on event times.
     */
    private void renderDayView() {
        dayGrid.getChildren().clear();
        dayGrid.getRowConstraints().clear();

        // Add RowConstraint for the header (date label) row
        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(80); // Tall enough for large date label
        dayGrid.getRowConstraints().add(headerRow);

        // Add RowConstraints for 24 hour rows (rows 1 to 24)
        for (int hour = 0; hour < 24; hour++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(50); // Consistent height
            dayGrid.getRowConstraints().add(rowConstraints);
        }

        // Create and add the date label at the top
        Label dateLabel = new Label(currentDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        dateLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 1px;");
        dateLabel.setAlignment(Pos.TOP_LEFT);
        dateLabel.setMaxWidth(Double.MAX_VALUE);
        dateLabel.setMaxHeight(Double.MAX_VALUE);
        GridPane.setColumnSpan(dateLabel, 2); // Span across both columns
        dayGrid.add(dateLabel, 0, 0); // Row 0 = header

        var eventCalendar = new CalendarDAO(currentDate, Period.of(0, 0, 1), TimeUnit.HOURS);

        // Create the hourly rows
        for (int hour = 0; hour < 24; hour++) {
            // Left column: Time label
            Label timeLabel = new Label(String.format("%02d:00", hour));
            timeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #666;");
            timeLabel.setPrefHeight(50);
            timeLabel.setMaxWidth(Double.MAX_VALUE);
            timeLabel.setAlignment(Pos.CENTER_RIGHT);
            timeLabel.setPadding(new Insets(0, 10, 0, 0)); // Top, Right, Bottom, Left

            // Right column: Event slot
            Label eventSlot = new Label();
            eventSlot.setPrefHeight(50);
            eventSlot.setMaxWidth(Double.MAX_VALUE);
            eventSlot.setAlignment(Pos.CENTER_LEFT);
            eventSlot.setPadding(new Insets(0, 10, 0, 10)); // Padding inside event box
            eventSlot.setWrapText(true);

            // Base and conditional style
            String baseStyle = "-fx-border-color: #ccc; -fx-border-width: 0 0 1px 0; -fx-font-size: 14px;";
            String highlightStyle = "";

            var fEvent = eventCalendar.getFirstEventForInterval(currentDate, hour, TimeUnit.HOURS);
            if (fEvent != null) {
                eventSlot.setText(fEvent.getName());
                highlightStyle = "-fx-background-color: #9B59B6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 10px; " +
                        "-fx-padding: 2 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-font-weight: bold;"; // Light purple
            }

            eventSlot.setStyle(baseStyle + highlightStyle);

            // Add to grid (rows shifted by 1 to account for header)
            dayGrid.add(timeLabel, 0, hour + 1);
            dayGrid.add(eventSlot, 1, hour + 1);

            GridPane.setHgrow(eventSlot, Priority.ALWAYS); // Let it fill width
        }
    }

    /**
     * Renders a full year view using multiple mini-month grids in the calendar UI.
     * Current day is highlighted
     */
    private void renderYearView() {
        yearGrid.getChildren().clear(); // Clear any existing calendar views
        yearGrid.getColumnConstraints().clear(); // Remove column constraints for fresh layout
        yearGrid.getRowConstraints().clear(); // Remove row constraints for fresh layout

        int monthsPerRow = 3; // 3 months across each row
        int monthsPerCol = 4; // 4 rows, total of 12 months
        int month = 1; // Start from January

        yearGrid.setHgap(10); // Horizontal space between mini month grids
        yearGrid.setVgap(10); // Vertical space between mini month grids

        // Set layout constraints so all months fill grid evenly
        for (int col = 0; col < monthsPerRow; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / monthsPerRow); // Each column takes up 1/3 of width
            colConstraints.setHgrow(Priority.ALWAYS);
            yearGrid.getColumnConstraints().add(colConstraints);
        }

        for (int row = 0; row < monthsPerCol; row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / monthsPerCol); // Each row takes up 1/4 of height
            rowConstraints.setVgrow(Priority.ALWAYS);
            yearGrid.getRowConstraints().add(rowConstraints);
        }

        // Loop through each month and render its own mini calendar grid
        for (int row = 0; row < monthsPerCol; row++) {
            for (int col = 0; col < monthsPerRow; col++) {
                if (month > 12) break; // Only 12 months total

                GridPane miniMonthGrid = new GridPane(); // Small calendar for a single month
                miniMonthGrid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                miniMonthGrid.setPadding(new Insets(8)); // Padding around each mini grid

                // Add 7 columns to represent days of the week
                for (int i = 0; i < 7; i++) {
                    ColumnConstraints colConst = new ColumnConstraints();
                    colConst.setPercentWidth(100.0 / 7); // Each day takes up 1/7th of the width
                    colConst.setHgrow(Priority.ALWAYS);
                    miniMonthGrid.getColumnConstraints().add(colConst);
                }

                // Add 7 rows: 1 for month title, 6 for potential weeks
                for (int i = 0; i < 7; i++) {
                    RowConstraints rowConst = new RowConstraints();
                    rowConst.setPercentHeight(100.0 / 7);
                    rowConst.setVgrow(Priority.ALWAYS);
                    miniMonthGrid.getRowConstraints().add(rowConst);
                }

                // Month header (e.g., Jan, Feb, etc.)
                Label monthLabel = new Label(YearMonth.of(currentDate.getYear(), month).getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
                monthLabel.setAlignment(Pos.CENTER);
                monthLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                // Month header styling with binding
                monthLabel.styleProperty().bind(
                        Bindings.createStringBinding(() -> {
                            return "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: black; "
                                    + "-fx-border-color: #ccc; -fx-border-width: 0.5px; -fx-background-color: #f2f2f2;";
                        }, miniMonthGrid.widthProperty())
                );

                GridPane.setColumnSpan(monthLabel, 7); // Span across all 7 columns
                miniMonthGrid.add(monthLabel, 0, 0); // Add to top row

                // Generate all days for the current month
                LocalDate firstDay = LocalDate.of(currentDate.getYear(), month, 1);
                LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

                // Determine starting column index based on the day of week of the 1st
                int startCol = (firstDay.getDayOfWeek().getValue() + 6) % 7; // Make Sunday = 6
                int rowIdx = 1;
                int colIdx = startCol;

                // Fill mini month grid with day numbers
                for (int day = 1; day <= lastDay.getDayOfMonth(); day++) {
                    final int currentDay = day;
                    Label dayLabel = new Label(String.valueOf(day));
                    dayLabel.setAlignment(Pos.CENTER);
                    dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                    // Style the day cell and highlight today
                    dayLabel.styleProperty().bind(
                            Bindings.createStringBinding(() -> {
                                String style = "-fx-font-size: 16px; -fx-text-fill: black; "
                                        + "-fx-border-color: #ccc; -fx-border-width: 0.5px;";
                                LocalDate dateForLabel = firstDay.withDayOfMonth(currentDay);
                                if (dateForLabel.isEqual(LocalDate.now())) {
                                    style += "-fx-background-color: rgba(216, 185, 255, 0.4); "
                                            + "-fx-border-color: D8B9FF; -fx-border-width: 2px; "
                                            + "-fx-border-radius: 18px; -fx-background-radius: 18px;";
                                }
                                return style;
                            }, miniMonthGrid.widthProperty())
                    );

                    GridPane.setHgrow(dayLabel, Priority.ALWAYS);
                    GridPane.setVgrow(dayLabel, Priority.ALWAYS);
                    miniMonthGrid.add(dayLabel, colIdx, rowIdx); // Add day to its position

                    // Move to next grid cell
                    colIdx++;
                    if (colIdx > 6) { // If it's end of the week, go to next row
                        colIdx = 0;
                        rowIdx++;
                    }
                }

                // Add the mini month grid to the full year grid
                yearGrid.add(miniMonthGrid, col, row);
                GridPane.setHgrow(miniMonthGrid, Priority.ALWAYS);
                GridPane.setVgrow(miniMonthGrid, Priority.ALWAYS);

                month++; // Move to the next month
            }
        }
    }

    /**
     * Renders a mini sidebar view showing hourly slots for the current day.
     * Displays 24 rows from 00:00 to 23:00 with time labels and empty event labels.
     * This is used to give a quick glance at the day's structure.
     */
    private void renderMiniDayView() {
        miniDayView.getChildren().clear();
        miniDayView.getRowConstraints().clear();

        // Header row
        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(50);
        headerRow.setVgrow(Priority.NEVER);
        miniDayView.getRowConstraints().add(headerRow);

        // Date header label
        Label header = new Label(currentDate.format(DateTimeFormatter.ofPattern("d MMM")));
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 32px; -fx-text-fill: #2e014f;");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.TOP_LEFT);
        GridPane.setColumnSpan(header, 2);
        miniDayView.add(header, 0, 0);

        var dayCalendar = new CalendarDAO(currentDate, Period.of(0, 0, 1), TimeUnit.HOURS);

        for (int hour = 0; hour < 24; hour++) {
            // Row constraints (keeping your fixed sizing)
            RowConstraints row = new RowConstraints();
            row.setMinHeight(30);
            row.setPrefHeight(Region.USE_COMPUTED_SIZE);
            row.setMaxHeight(Double.MAX_VALUE);
            row.setVgrow(Priority.ALWAYS);
            miniDayView.getRowConstraints().add(row);

            // Time label (keeping your styling)
            Label time = new Label(String.format("%02d:00", hour));
            time.setStyle("-fx-font-size: 13px; -fx-text-fill: #666; -fx-font-weight: bold;");
            time.setPrefHeight(30);
            time.setMaxWidth(Double.MAX_VALUE);
            time.setAlignment(Pos.CENTER_RIGHT);
            time.setPadding(new Insets(0, 6, 0, 0));
            miniDayView.add(time, 0, hour + 1);

            // Event label - now pulling real events
            Label event = new Label();
            event.setWrapText(true);
            event.setMaxHeight(Double.MAX_VALUE);
            event.setPrefHeight(Region.USE_COMPUTED_SIZE);
            event.setMaxWidth(Double.MAX_VALUE);
            event.setAlignment(Pos.CENTER_LEFT);
            event.setPadding(new Insets(0, 6, 0, 6)); // Reduced padding for compact view

            // Base style (from original)
            String baseStyle = "-fx-border-color: #adadad; -fx-border-width: 0 0 1px 0; -fx-font-size: 13px;";
            String highlightStyle = "";

            // Get actual event (from original)
            var fEvent = dayCalendar.getFirstEventForInterval(currentDate, hour, TimeUnit.HOURS);
            if (fEvent != null) {
                event.setText(fEvent.getName());
                highlightStyle = "-fx-background-color: #9B59B6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 2 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-font-weight: bold;";
            }

            event.setStyle(baseStyle + highlightStyle);

            GridPane.setHgrow(event, Priority.ALWAYS);
            GridPane.setVgrow(event, Priority.ALWAYS);
            miniDayView.add(event, 1, hour + 1);
        }
    }

    /**
     * Updates the dayComboBox based on selected month and year.
     */
    private void updateDayComboBox() {
        Integer year = yearComboBox.getValue();
        String monthStr = monthComboBox.getValue();
        if (monthStr != null && year != null) {
            int month = monthComboBox.getItems().indexOf(monthStr) + 1;
            int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
            dayComboBox.getItems().clear();
            for (int i = 1; i <= daysInMonth; i++) dayComboBox.getItems().add(i);
            int today = LocalDate.now().getDayOfMonth();
            dayComboBox.setValue(Math.min(today, daysInMonth));
        }
    }
}