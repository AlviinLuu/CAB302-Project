package com.example.cab302project.controllers;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CalenderController {

    // === FXML UI Elements ===
    @FXML
    private Label monthLabel;
    @FXML
    private GridPane calendarGrid;
    @FXML
    private GridPane weekGrid;
    @FXML
    private GridPane monthGrid;
    @FXML
    private GridPane yearGrid;
    @FXML
    private GridPane dayGrid;
    @FXML
    private ImageView logoImage;

    @FXML
    private VBox weekView;
    @FXML
    private VBox yearView;
    @FXML
    private VBox monthView;
    @FXML
    private VBox dayView;

    @FXML
    private VBox aiSidebar;
    @FXML
    private SplitPane splitPane;

    @FXML
    private VBox mainContent;

    @FXML
    private ComboBox<String> monthComboBox;
    @FXML
    private ComboBox<Integer> yearComboBox;
    @FXML
    private ComboBox<Integer> dayComboBox;


    @FXML
    private ToggleGroup viewToggleGroup;
    @FXML
    private RadioButton dayRadio;
    @FXML
    private RadioButton weekRadio;
    @FXML
    private RadioButton monthRadio;
    @FXML
    private RadioButton yearRadio;
    @FXML
    private TextArea userInputArea;
    @FXML
    private VBox responseArea;
    @FXML
    private GridPane miniDayView;

    // === State ===
    private LocalDate currentDate = LocalDate.now();

    // === Initialization ===
    @FXML
    public void initialize() {
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
    }

    // === Navigation Buttons ===
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

        monthRadio.setSelected(true); // Ensure the Month button stays selected.hiii
    }

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
    }

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

    @FXML
    private void onLogoHover() {
        logoImage.setScaleX(1.2);
        logoImage.setScaleY(1.2);
    }

    @FXML
    private void onLogoExit() {
        logoImage.setScaleX(1.0);
        logoImage.setScaleY(1.0);
    }

    // AI response
    //@FXML
    //private void onSendButtonClick() {
      //  String userInput = userInputArea.getText().trim();
       // if (!userInput.isEmpty()) {
         //   Label responseLabel = new Label("AI says: " + generateResponse(userInput));
     //       responseLabel.setWrapText(true);
     //       responseLabel.setStyle("-fx-background-color: #D8B9FF; -fx-padding: 10; -fx-background-radius: 10; -fx-text-fill: #1A1A1A;");
//
   //          // Add the label to the response area
     //       responseArea.getChildren().add(responseLabel);
//
   //          // Check if there are more than 3 responses
     //       if (responseArea.getChildren().size() > 3) {
       //         // Remove the oldest (first) response
         //       responseArea.getChildren().remove(0);
           // }
//
            // Show AI Response with animated typing
  //          String fullText = "AI says: " + generateResponse(userInput);
    //        playTypingAnimation(responseLabel, fullText);
//
            // Clear the user input
      //      userInputArea.clear();
    //    }
  //  }

    @FXML
    private void openSettingsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/settings-view.fxml"));
            Parent settingsRoot = loader.load();

            Stage settingsStage = new Stage();
            settingsStage.setTitle("Settings");

            Scene scene = new Scene(settingsRoot);
            settingsStage.setScene(scene);

            // Set it to full screen
            settingsStage.setMaximized(true);

            // Close the current Calendar window
            Stage currentStage = (Stage) mainContent.getScene().getWindow();  // mainContent is your root VBox
            currentStage.close();

            settingsStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openFriendsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/friends-view.fxml"));
            Parent friendsRoot = loader.load();

            Stage friendsStage = new Stage();
            friendsStage.setTitle("Friends");

            Scene scene = new Scene(friendsRoot);
            friendsStage.setScene(scene);

            // Set it to full screen
            friendsStage.setMaximized(true);

            // Close the current Calendar window
            Stage currentStage = (Stage) mainContent.getScene().getWindow();  // mainContent is your root VBox
            currentStage.close();

            friendsStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void openProfilePage() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/cab302project/profile-view.fxml")));
            Stage stage = (Stage)mainContent.getScene().getWindow();
            stage.close();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Example simple AI response ****NEED TO BE REPLACED WHEN AI SET UP*******
    //private String generateResponse(String userInput) {
     //   return "You said \"" + userInput;
    //}

    // Just a fun animation effect which shows AI typing while it generates a response
    //private void playTypingAnimation(Label label, String fullText) {
    //    final int[] charIndex = {0};

      //  javafx.animation.Timeline timeline = new javafx.animation.Timeline(
        //        new javafx.animation.KeyFrame(
          //              javafx.util.Duration.millis(40), // Speed of typing
            //            event -> {
              //              if (charIndex[0] < fullText.length()) {
                //                label.setText(fullText.substring(0, charIndex[0] + 1));
                  //              charIndex[0]++;
                    //        }
                      //  }
                //)
        //);
        //timeline.setCycleCount(fullText.length());
        //timeline.play();
    //}

    // === Calendar Rendering hi harpi===
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
            default:
                renderMonthView();  // Default to Month view if no specific view is selected
                break;
        }
    }

    private void renderMonthView() {
        // Clear the grid first
        monthGrid.getChildren().clear();

        // Get system-localized weekday names
        DayOfWeek[] daysOfWeek = DayOfWeek.values();
        for (int i = 0; i < daysOfWeek.length; i++) {
            String dayName = daysOfWeek[i].getDisplayName(TextStyle.SHORT, Locale.getDefault());

            Label dayLabel = new Label(dayName);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // Bind the font size for header too
            dayLabel.styleProperty().bind(
                    Bindings.createStringBinding(() -> {
                        double size = monthGrid.getWidth() / 40; // Same scaling as days
                        return String.format(
                                "-fx-font-size: %.2fpx; -fx-font-weight: bold; -fx-text-fill: black; -fx-border-color: #ccc; -fx-border-width: 0.5px; -fx-border-style: solid; -fx-background-color: #eee",
                                size
                        );
                    }, monthGrid.widthProperty())
            );

            GridPane.setHgrow(dayLabel, Priority.ALWAYS);
            GridPane.setVgrow(dayLabel, Priority.ALWAYS);
            monthGrid.add(dayLabel, i, 0);
        }

        // Add the actual days of the month
        LocalDate firstDay = currentDate.withDayOfMonth(1);
        LocalDate lastDay = currentDate.withDayOfMonth(currentDate.lengthOfMonth());

        int startCol = (firstDay.getDayOfWeek().getValue() + 6) % 7; // Monday = 0
        int row = 1;
        int col = startCol;

        for (int day = 1; day <= lastDay.getDayOfMonth(); day++) {
            final int currentDay = day;

            Label label = new Label(String.valueOf(day));
            label.setAlignment(Pos.CENTER);
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            label.styleProperty().bind(
                    Bindings.createStringBinding(() -> {
                        double size = monthGrid.getWidth() / 40;
                        String style = String.format(
                                "-fx-font-size: %.2fpx; -fx-text-fill: black; -fx-border-color: #ccc; -fx-border-width: 0.5px; -fx-border-style: solid;",
                                size
                        );

                        // Check if this label represents today's date
                        LocalDate dateForLabel = currentDate.withDayOfMonth(currentDay);
                        if (dateForLabel.isEqual(LocalDate.now())) {
                            style += "-fx-background-color: rgba(216, 185, 255, 0.4); -fx-border-color: #D8B9FF; -fx-border-width: 2px; -fx-border-radius: 20px; -fx-background-radius: 20px;";
                        }
                        return style;
                    }, monthGrid.widthProperty())
            );

            GridPane.setHgrow(label, Priority.ALWAYS);
            GridPane.setVgrow(label, Priority.ALWAYS);
            monthGrid.add(label, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private void renderWeekView() {
        weekGrid.getChildren().clear(); // Clear previous content

        LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);

        // Add day headers (Monday, Tuesday, etc.)
        for (int col = 1; col <= 7; col++) {
            LocalDate day = startOfWeek.plusDays(col - 1);
            final LocalDate currentDay = day; // Make it final for lambda usage

            Label dayLabel = new Label(day.getDayOfMonth() + " (" + day.getDayOfWeek().name().substring(0, 3) + ")");
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dayLabel.setMinHeight(60);

            dayLabel.styleProperty().bind(
                    Bindings.createStringBinding(() -> {
                        double size = weekGrid.getWidth() / 40;
                        String baseStyle = String.format(
                                "-fx-font-size: %.2fpx; -fx-font-weight: bold; -fx-background-color: #eee; -fx-border-color: #ccc; -fx-border-width: 0.5px;",
                                size
                        );

                        // If the current day is today, apply the highlight
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
            weekGrid.add(dayLabel, col, 0); // Using weekGrid
        }

        // Add hour labels on the side (00:00 to 23:00)
        for (int row = 1; row <= 24; row++) {
            Label hourLabel = new Label(String.format("%02d:00", row - 1));
            hourLabel.setAlignment(Pos.CENTER);
            hourLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            hourLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-font-weight: bold;");
            GridPane.setHgrow(hourLabel, Priority.ALWAYS);
            GridPane.setVgrow(hourLabel, Priority.ALWAYS);
            weekGrid.add(hourLabel, 0, row); // Using weekGrid
        }

        // Add cells for each hour of each day
        for (int col = 1; col <= 7; col++) {
            for (int row = 1; row <= 24; row++) {
                Label cell = new Label();
                cell.setAlignment(Pos.CENTER);
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                // Bind font size to the width of the grid
                cell.styleProperty().bind(
                        Bindings.createStringBinding(() -> {
                            double size = weekGrid.getWidth() / 40; // Using weekGrid here
                            return String.format(
                                    "-fx-font-size: %.2fpx; -fx-border-color: #ccc; -fx-border-width: 0.5px; -fx-border-style: solid; -fx-alignment: center;",
                                    size
                            );
                        }, weekGrid.widthProperty()) // Using weekGrid width for size binding
                );

                GridPane.setHgrow(cell, Priority.ALWAYS);
                GridPane.setVgrow(cell, Priority.ALWAYS);
                weekGrid.add(cell, col, row); // Using weekGrid
            }
        }
    }

    private void renderDayView() {
        dayGrid.getChildren().clear();
        dayGrid.getRowConstraints().clear(); // Clear previous row constraints

        // Add RowConstraints to give more vertical space between rows
        for (int hour = 0; hour < 24; hour++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(50); // Set minimum height for each row (increase this value for more space)
            dayGrid.getRowConstraints().add(rowConstraints);
        }

        // Create and add the date label at the top (spanning both columns)
        Label dateLabel = new Label(currentDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

        dateLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 1px;");
        dateLabel.setAlignment(Pos.TOP_LEFT);
        dateLabel.setMaxWidth(Double.MAX_VALUE);
        dateLabel.setMaxHeight(Double.MAX_VALUE);
        GridPane.setColumnSpan(dateLabel, 2); // Span across both columns (time and event columns)
        dayGrid.add(dateLabel, 0, 0); // Place it in the first row (row 0)

        // Create the hourly labels and event slots
        for (int hour = 0; hour < 24; hour++) {
            // Create the time label (left side)
            Label timeLabel = new Label(String.format("%02d:00", hour));
            timeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #666;");
            timeLabel.setPrefHeight(40);
            timeLabel.setMaxWidth(Double.MAX_VALUE);
            timeLabel.setAlignment(Pos.CENTER);

            // Create the event slot (right side)
            Label eventSlot = new Label(); // Empty for now
            eventSlot.setPrefHeight(40);
            eventSlot.setMaxWidth(Double.MAX_VALUE);
            eventSlot.setStyle("-fx-border-color: #ccc; -fx-border-width: 0 0 1px 0;"); // Bottom line only
            eventSlot.setAlignment(Pos.CENTER_LEFT);

            // Add both to the grid
            dayGrid.add(timeLabel, 0, hour + 1); // Column 0 = Time (shifted by 1 row)
            dayGrid.add(eventSlot, 1, hour + 1); // Column 1 = Event (shifted by 1 row)

            // Optional: GridPane.setHgrow(timeLabel, Priority.ALWAYS);
            GridPane.setHgrow(eventSlot, Priority.ALWAYS);
        }
    }

    private void renderYearView() {
        yearGrid.getChildren().clear();
        yearGrid.getColumnConstraints().clear();
        yearGrid.getRowConstraints().clear();

        int monthsPerRow = 3;
        int monthsPerCol = 4;
        int month = 1;

        // Ensure the main year grid stretches across the entire available space
        yearGrid.setHgap(10);
        yearGrid.setVgap(10);

        // Create column and row constraints to ensure proper stretching
        for (int col = 0; col < monthsPerRow; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setHgrow(Priority.ALWAYS);
            yearGrid.getColumnConstraints().add(colConstraints);
        }

        for (int row = 0; row < monthsPerCol; row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.ALWAYS);
            yearGrid.getRowConstraints().add(rowConstraints);
        }

        // Create mini month grids for each month of the year
        for (int row = 0; row < monthsPerCol; row++) {
            for (int col = 0; col < monthsPerRow; col++) {
                if (month > 12) break;

                // Mini month grid for each month
                GridPane miniMonthGrid = new GridPane();
                miniMonthGrid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // Ensures grid stretches
                miniMonthGrid.setPadding(new Insets(8)); // Padding inside each mini grid

                // Month name label (like in month view)
                Label monthLabel = new Label(YearMonth.of(currentDate.getYear(), month).getMonth().toString().substring(0, 3));
                monthLabel.setAlignment(Pos.CENTER);
                monthLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                // Bind the font size for the month name
                monthLabel.styleProperty().bind(
                        Bindings.createStringBinding(() -> {
                            double size = miniMonthGrid.getWidth() / 6; // Adjust size based on mini grid
                            return String.format(
                                    "-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: black; -fx-border-color: #ccc; -fx-border-width: 0.5px; -fx-border-style: solid; -fx-background-color: #f2f2f2",
                                    size
                            );
                        }, miniMonthGrid.widthProperty()) // Use miniMonthGrid width for font size binding
                );

                GridPane.setColumnSpan(monthLabel, 7);
                miniMonthGrid.add(monthLabel, 0, 0);

                // Determine the first and last day of the month
                LocalDate firstDay = LocalDate.of(currentDate.getYear(), month, 1);
                LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

                int startCol = (firstDay.getDayOfWeek().getValue() + 6) % 7; // Monday = 0
                int rowIdx = 1;
                int colIdx = startCol;

                // Add the actual days to the mini month grid
                for (int day = 1; day <= lastDay.getDayOfMonth(); day++) {
                    final int currentDay = day;

                    Label dayLabel = new Label(String.valueOf(day));
                    dayLabel.setAlignment(Pos.CENTER);
                    dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                    // Bind the font size for the day label
                    dayLabel.styleProperty().bind(
                            Bindings.createStringBinding(() -> {
                                double size = miniMonthGrid.getWidth() / 10; // Adjust size based on mini grid
                                String style = String.format(
                                        "-fx-font-size: 18px; -fx-text-fill: black; -fx-border-color: #ccc; -fx-border-width: 0.5px; -fx-border-style: solid;",
                                        size
                                );

                                LocalDate dateForLabel = firstDay.withDayOfMonth(currentDay); // Use currentDay now
                                if (dateForLabel.isEqual(LocalDate.now())) {
                                    style += "-fx-background-color: rgba(216, 185, 255, 0.4); -fx-border-color: D8B9FF; -fx-border-width: 2px; -fx-border-radius: 18px; -fx-background-radius: 18px;";
                                }
                                return style;
                            }, miniMonthGrid.widthProperty())
                    );

                    GridPane.setHgrow(dayLabel, Priority.ALWAYS);
                    GridPane.setVgrow(dayLabel, Priority.ALWAYS);

                    // Add the day to the mini month grid
                    miniMonthGrid.add(dayLabel, colIdx, rowIdx);

                    colIdx++;
                    if (colIdx > 6) {
                        colIdx = 0;
                        rowIdx++;
                    }
                }

                // Add the mini month grid to the year grid
                yearGrid.add(miniMonthGrid, col, row);

                month++;
            }
        }
    }

    private void renderMiniDayView() {
        miniDayView.getChildren().clear();
        miniDayView.getRowConstraints().clear();

        // Top date header
        Label header = new Label(currentDate.format(DateTimeFormatter.ofPattern("d MMM")));
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 32px; -fx-text-fill: #2e014f;");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.TOP_LEFT);
        miniDayView.add(header, 0, 0, 2, 1);

        // Spacer
        RowConstraints spacer = new RowConstraints(30);
        miniDayView.getRowConstraints().add(spacer);

        // Hour slots
        for (int hour = 0; hour < 24; hour++) {
            miniDayView.getRowConstraints().add(new RowConstraints(30));

            Label time = new Label(String.format("%02d:00", hour));
            time.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
            time.setMaxWidth(Double.MAX_VALUE);
            time.setAlignment(Pos.CENTER_RIGHT);

            Label event = new Label();
            event.setStyle("-fx-border-color: #bbb; -fx-border-width: 0 0 1px 0;");
            event.setMaxWidth(Double.MAX_VALUE);
            event.setAlignment(Pos.CENTER_LEFT);

            miniDayView.add(time, 0, hour + 1);
            miniDayView.add(event, 1, hour + 1);
            GridPane.setHgrow(event, Priority.ALWAYS);
        }
    }

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