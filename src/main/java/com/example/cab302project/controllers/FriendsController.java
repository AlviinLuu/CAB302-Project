package com.example.cab302project.controllers;

import com.example.cab302project.models.Event;
import com.example.cab302project.models.SqliteConnection;
import com.example.cab302project.models.SqliteUserDAO;
import com.example.cab302project.models.User;
import com.example.cab302project.util.Session;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FriendsController {

    @FXML private VBox mainContent;
    @FXML private TextArea aiPromptField;
    @FXML private TextArea aiResponseLabel;
    @FXML private Button profileButton;
    @FXML private Label profileHeaderLabel;
    @FXML private ImageView profileImage;
    @FXML private Label usernameLabel;
    @FXML private Label nameLabel;
    @FXML private Label bioLabel;
    @FXML private GridPane miniDayView;
    @FXML private ImageView logoImage;
    @FXML private ListView<String> searchResultsList;
    @FXML private TextField searchUserField;
    @FXML private ListView<String> pendingRequestsList;   // outgoing
    @FXML private ListView<String> incomingRequestsList;  // incoming
    @FXML private ComboBox<String> friendSelector;

    private final ObservableList<String> allUsers        = FXCollections.observableArrayList();
    private final ObservableList<String> pendingOutgoing = FXCollections.observableArrayList();
    private final ObservableList<String> incomingReqs    = FXCollections.observableArrayList();
    private final ObservableList<String> friendList      = FXCollections.observableArrayList();
    private String lastAIResponse = null;
    private List<Event> lastUserEvents = null;
    private List<Event> lastFriendEvents = null;
    private LocalDate lastQueryDate = null;
    private LocalDate currentDate = LocalDate.now();
    private Connection connection;
    private SqliteUserDAO userDAO;
    private String loadedFriendEmail = null;
    private static final ObjectMapper objectMapper = new ObjectMapper();


    private static final Pattern DATE_PATTERN = Pattern.compile(
            "\\b(?:January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{1,2}(?:,\\s*\\d{4})?\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final DateTimeFormatter EVENT_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");


    private static final DateTimeFormatter FLEXIBLE_DATE_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive() // Case-insensitive parsing for month names
            .appendPattern("MMMM d") // Full month name, day
            .optionalStart()         // Year and comma are optional
            .appendLiteral(",")
            .optionalStart()
            .appendLiteral(" ")
            .optionalEnd()
            .appendPattern("yyyy")   // Year
            .optionalEnd()
            .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear()) // Default to current year if not present
            .toFormatter();
    @FXML
    public void initialize() {
        // 1) Ensure session has a user
        User sessionUser = Session.getLoggedInUser();
        if (sessionUser == null) {
            System.err.println("ERROR: No user in session Call Session.setLoggedInUser(...) after login.");
            return;
        }

        // 2) DB & DAO
        connection = SqliteConnection.getInstance();
        userDAO    = new SqliteUserDAO();

        // 3) Load logo
        Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
        logoImage.setImage(logo);

        // 4) Load data from DB
        loadAllUsers();
        refreshFriendList();
        refreshIncomingRequests();
        refreshOutgoingRequests();

        // 5) Wire UI list views
        friendSelector.setItems(friendList);
        // if nothing in list show message
        Label placeholderLabel2 = new Label("Select a user");
        placeholderLabel2.setStyle("-fx-text-fill: #6A4B8B; -fx-font-style: italic;");
        searchResultsList.setPlaceholder(placeholderLabel2);

        pendingRequestsList.setItems(pendingOutgoing);
        // if nothing in list show message
        Label placeholderLabel = new Label("No pending requests");
        placeholderLabel.setStyle("-fx-text-fill: #6A4B8B; -fx-font-style: italic;");
        pendingRequestsList.setPlaceholder(placeholderLabel);

        incomingRequestsList.setItems(incomingReqs);
        // if nothing in list show message
        Label placeholderLabel1 = new Label("No incoming requests");
        placeholderLabel1.setStyle("-fx-text-fill: #6A4B8B; -fx-font-style: italic;");
        incomingRequestsList.setPlaceholder(placeholderLabel1);

        // 6) Show profile on selection
        friendSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldU, newU) -> {
            if (newU != null) loadUserProfile(newU);
        });

        searchUserField.textProperty().addListener((obs, oldText, text) -> {
            if (text == null || text.isBlank()) {
                searchResultsList.setItems(FXCollections.observableArrayList());
            } else {
                List<String> matches = allUsers.stream()
                        .filter(u -> u.toLowerCase().contains(text.toLowerCase()))
                        .collect(Collectors.toList());
                searchResultsList.setItems(FXCollections.observableArrayList(matches));
            }
        });

        // 7) Show username on the profile button
        profileButton.setText(sessionUser.getUsername());

        renderMiniDayView();
    }

    // ─── Data Loaders ──────────────────────────────────────────────────

    private void loadAllUsers() {
        allUsers.clear();
        String me = Session.getLoggedInUser().getUsername();

        String sql = "SELECT username FROM users";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String u = rs.getString("username");
                if (!u.equals(me)) {
                    allUsers.add(u);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshFriendList() {
        try {
            friendList.setAll(
                    userDAO.getFriends(Session.getLoggedInUser().getUsername())
                            .stream()
                            .map(User::getUsername)
                            .collect(Collectors.toList())
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshIncomingRequests() {
        incomingReqs.setAll(
                userDAO.getPendingFriendRequests(Session.getLoggedInUser().getUsername())
                        .stream()
                        .map(User::getUsername)
                        .collect(Collectors.toList())
        );
    }

    private void refreshOutgoingRequests() {
        pendingOutgoing.clear();
        String sql = """
            SELECT receiver_email
              FROM friend_requests
             WHERE sender_email = ?
               AND status = 'pending'
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, Session.getLoggedInUser().getUsername());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("receiver_email");
                    pendingOutgoing.add(username);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUserProfile(String username) {
        User u = userDAO.getUserByUsername(username);

        if (u != null) {
            profileHeaderLabel.setText(username + "'s Profile");
            usernameLabel.setText("@" + u.getUsername());
            nameLabel.setText(u.getEmail());
            bioLabel.setText(u.getBio());
            loadedFriendEmail = u.getEmail();

            // Check if user has a profile image
            byte[] profileImageData = userDAO.getProfileImage(u.getEmail());

            if (profileImageData != null && profileImageData.length > 0) {

                Image image = new Image(new ByteArrayInputStream(profileImageData));
                profileImage.setImage(image);
            } else {

                InputStream is = getClass().getResourceAsStream("/images/default_profile.png");
                assert is != null;
                profileImage.setImage(new Image(is));
            }
        }
    }


    // ─── Button Handlers ────────────────────────────────────────────────

    @FXML
    private void handleRemoveFriend() {
        String selectedFriend = friendSelector.getSelectionModel().getSelectedItem();
        if (selectedFriend != null && friendList.contains(selectedFriend)) {
            friendList.remove(selectedFriend);
            friendSelector.getSelectionModel().clearSelection();

            // Clear profile display
            profileHeaderLabel.setText("Friend profile");
            usernameLabel.setText("");
            nameLabel.setText("");
            bioLabel.setText("");
            profileImage.setImage(null);  // Optional: set to a default image instead

            showAlert("Friend Removed", selectedFriend + " has been removed from your friend list.");
        } else {
            showAlert("No Selection", "Please select a friend to remove.");
        }
    }

    @FXML private void handleSendRequest() {
        String selected = searchResultsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection","Please select a user to send request.");
            return;
        }
        String me = Session.getLoggedInUser().getUsername();
        if (friendList.contains(selected)) {
            showAlert("Already Friends", selected + " is already your friend.");
        } else if (pendingOutgoing.contains(selected)) {
            showAlert("Already Requested","You already sent a request to " + selected + ".");
        } else {
            boolean ok = userDAO.sendFriendRequest(me, selected);
            if (ok) {
                showAlert("Request Sent","Friend request sent to " + selected + ".");
                refreshOutgoingRequests();
            } else {
                showAlert("Error","Could not send request. Ensure user exists and no pending request.");
            }
        }
        searchUserField.clear();
        searchResultsList.setItems(FXCollections.observableArrayList());
    }

    @FXML private void handleAcceptRequest() {
        String sel = incomingRequestsList.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("No Selection","Please select a request to accept.");
            return;
        }
        String me = Session.getLoggedInUser().getUsername();
        if (userDAO.acceptFriendRequest(sel, me)) {
            showAlert("Accepted", sel + " is now your friend!");
            refreshIncomingRequests();
            refreshFriendList();
        } else {
            showAlert("Error","Could not accept. Try again.");
        }
    }

    @FXML private void handleDeclineRequest() {
        String sel = incomingRequestsList.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("No Selection","Please select a request to decline.");
            return;
        }
        String me = Session.getLoggedInUser().getUsername();
        if (userDAO.declineFriendRequest(sel, me)) {
            showAlert("Declined", sel + " has been declined.");
            refreshIncomingRequests();
        } else {
            showAlert("Error","Could not decline. Try again.");
        }
    }

    @FXML private void handleDeleteRequest() {
        String sel = pendingRequestsList.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("No Selection","Please select a request to delete.");
            return;
        }
        String sql = """
            DELETE FROM friend_requests
             WHERE sender_email   = ?
               AND receiver_email = ?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, Session.getLoggedInUser().getUsername());
            ps.setString(2, sel);
            ps.executeUpdate();
            showAlert("Deleted","Request to " + sel + " removed.");
            refreshOutgoingRequests();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─── Navigation & AI ───────────────────────────────────────────────

    @FXML private void goToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/cab302project/calendar-view.fxml"));
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

    @FXML private void openSettingsPage() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/cab302project/settings-view.fxml")));
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

    @FXML
    private void handleAIPrompt() {
        String prompt = aiPromptField.getText().trim();
        if (prompt.isEmpty()) {
            return;
        }

        aiPromptField.clear();
        aiResponseLabel.setText("You: " + prompt + "\n\nAI: Thinking...");

        User currentUser = Session.getLoggedInUser();
        if (currentUser == null) {
            aiResponseLabel.setText("You: " + prompt + "\n\nAI: Error: Please log in first");
            return;
        }

        System.out.println("Original prompt: " + prompt);

        String aiPrompt = buildAIPrompt(prompt, currentUser);

        System.out.println("Final AI prompt: " + aiPrompt);

        new Thread(() -> {
            try {
                String response = callOllama(aiPrompt);

                Platform.runLater(() -> {
                    if (response != null && !response.isEmpty()) {
                        System.out.println("Raw AI response: " + response);

                        String cleanedResponse = cleanResponse(response);
                        lastAIResponse = cleanedResponse;

                        playTypingAnimation(aiResponseLabel,
                                "You: " + prompt + "\n\nAI: " + cleanedResponse);
                    } else {
                        aiResponseLabel.setText("You: " + prompt +
                                "\n\nAI: Sorry, I couldn't get a response. Is Ollama running?");
                        lastAIResponse = null;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    aiResponseLabel.setText("You: " + prompt +
                            "\n\nAI: Error: " + e.getMessage());
                });
                lastAIResponse = null;
            }
        }).start();
    }

    private String calculateFreeTimeSlots(List<Event> userEvents, List<Event> friendEvents, LocalDate date) {
        if (userEvents == null) userEvents = new ArrayList<>();
        if (friendEvents == null) friendEvents = new ArrayList<>();

        List<Event> allEvents = new ArrayList<>(userEvents);
        allEvents.addAll(friendEvents);


        allEvents.sort(Comparator.comparing(event -> LocalDateTime.parse(event.getStart_time(), EVENT_TIME_FORMATTER)));


        LocalTime startOfDay = LocalTime.of(7, 0); // 7 AM
        LocalTime endOfDay = LocalTime.of(23, 0);   // 11 PM

        LocalDateTime windowStart = LocalDateTime.of(date, startOfDay);
        LocalDateTime windowEnd = LocalDateTime.of(date, endOfDay);

        List<String> freeSlots = new ArrayList<>();
        LocalDateTime currentFreeStart = windowStart;

        for (Event event : allEvents) {

            LocalDateTime eventStart = LocalDateTime.parse(event.getStart_time(), EVENT_TIME_FORMATTER);
            LocalDateTime eventEnd = LocalDateTime.parse(event.getEnd_time(), EVENT_TIME_FORMATTER);

            eventStart = eventStart.isBefore(windowStart) ? windowStart : eventStart;
            eventEnd = eventEnd.isAfter(windowEnd) ? windowEnd : eventEnd;


            if (currentFreeStart.isBefore(eventStart)) {

                LocalDateTime freeEnd = eventStart.isAfter(windowEnd) ? windowEnd : eventStart;
                if (currentFreeStart.isBefore(freeEnd)) {
                    freeSlots.add(formatTimeRange(currentFreeStart, freeEnd));
                }
            }

            currentFreeStart = currentFreeStart.isAfter(eventEnd) ? currentFreeStart : eventEnd;
            if (currentFreeStart.isAfter(windowEnd)) {
                break;
            }
        }

        if (currentFreeStart.isBefore(windowEnd)) {
            freeSlots.add(formatTimeRange(currentFreeStart, windowEnd));
        }


        if (freeSlots.isEmpty()) {
            return "No free time slots available between 7 AM and 11 PM.";
        } else {
            StringBuilder sb = new StringBuilder("Free time slots:\n");
            for (String slot : freeSlots) {
                sb.append("- ").append(slot).append("\n");
            }
            return sb.toString();
        }
    }

    private String formatTimeRange(LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return start.format(timeFormatter) + " to " + end.format(timeFormatter);
    }

    private String getFriendEmailFromLastEvents() {
        if (lastFriendEvents != null && !lastFriendEvents.isEmpty()) {

            String sampleEventEmail = null;
            for (Event event : lastFriendEvents) {
                User friendUser = userDAO.getUserByUsername(event.getUsername());
                if (friendUser != null) {
                    sampleEventEmail = friendUser.getEmail();
                    break;
                }
            }
            return sampleEventEmail;

        }
        return null;
    }

    private String buildAIPrompt(String userPrompt, User currentUser) {
        StringBuilder prompt = new StringBuilder();

        // System instruction - Tell AI to simply output the provided free time slots
        prompt.append("You are a helpful calendar assistant. ")
                .append("You have been provided with a list of calculated free time slots based on the user's and their friend's schedules. ")
                .append("Your task is to present these free time slots to the user in a concise and friendly manner. ")
                .append("Do NOT perform any analysis or calculation yourself. ")
                .append("Simply state the free time slots that were provided in the 'CALCULATED FREE TIME SLOTS' section. ")
                .append("Do NOT refer to specific names from the event data; use 'Your events' and 'Friend's events' if necessary for context, but the primary output is the calculated free slots. ")
                .append("If the user's question is not about calendar availability, respond based on the conversation history and the provided event data, still being concise.\n\n");


        // Include previous conversation turn if available
        if (lastAIResponse != null && !lastAIResponse.isEmpty()) {
            prompt.append("PREVIOUS CONVERSATION:\n");
            prompt.append("AI: ").append(lastAIResponse).append("\n\n");
        }



        if (isCalendarRelated(userPrompt)) {
            // Attempt to extract date from the user prompt
            Optional<LocalDate> queryDate = extractDateFromPrompt(userPrompt);
            LocalDate targetDate = queryDate.orElse(LocalDate.now());


            boolean needsNewEvents = lastUserEvents == null || lastFriendEvents == null ||
                    !targetDate.equals(lastQueryDate) ||
                    (loadedFriendEmail != null && (lastFriendEvents == null || !userDAO.getUserByEmail(loadedFriendEmail).getEmail().equals(getFriendEmailFromLastEvents()))); // Simple check if friend changed

            if (needsNewEvents) {

                lastUserEvents = userDAO.getUserEventsByEmailAndDate(currentUser.getEmail(), targetDate);

                if (loadedFriendEmail != null && !loadedFriendEmail.isEmpty()) {
                    lastFriendEvents = userDAO.getUserEventsByEmailAndDate(loadedFriendEmail, targetDate);
                } else {
                    lastFriendEvents = new ArrayList<>();
                }
                lastQueryDate = targetDate;
            }

            // Add the target date to the prompt instruction for clarity
            prompt.append("ANALYZING EVENTS FOR: ").append(lastQueryDate.format(DateTimeFormatter.ofPattern("MMMM d,yyyy"))).append("\n\n");

            // Include current user's events for the target date, using generic term
            prompt.append("YOUR EVENTS ON ").append(lastQueryDate.format(DateTimeFormatter.ofPattern("MMMM d"))).append(":\n");
            if (lastUserEvents == null || lastUserEvents.isEmpty()) {
                prompt.append("No events scheduled.\n\n");
            } else {
                for (Event event : lastUserEvents) {
                    prompt.append("- ").append(event.getName())
                            .append(" (")
                            .append(event.getStart_time())
                            .append(" to ")
                            .append(event.getEnd_time())
                            .append(")\n");
                }
                prompt.append("\n");
            }

            if (loadedFriendEmail != null && !loadedFriendEmail.isEmpty()) {
                User friendUser = userDAO.getUserByEmail(loadedFriendEmail);
                String friendUsername = (friendUser != null) ? friendUser.getUsername() : "Selected Friend";

                prompt.append("FRIEND'S EVENTS ON ").append(lastQueryDate.format(DateTimeFormatter.ofPattern("MMMM d"))).append(":\n");
                if (lastFriendEvents == null || lastFriendEvents.isEmpty()) {
                    prompt.append("No events scheduled.\n\n");
                } else {
                    for (Event event : lastFriendEvents) {
                        prompt.append("- ").append(event.getName())
                                .append(" (")
                                .append(event.getStart_time())
                                .append(" to ")
                                .append(event.getEnd_time())
                                .append(")\n");
                    }
                    prompt.append("\n");
                }
            } else {
                prompt.append("NO FRIEND SELECTED: Cannot compare schedules with a friend because no friend's profile is currently loaded.\n\n");
            }

            String calculatedFreeTime = calculateFreeTimeSlots(lastUserEvents, lastFriendEvents, lastQueryDate);
            prompt.append("CALCULATED FREE TIME SLOTS:\n");
            prompt.append(calculatedFreeTime).append("\n\n");
            // ---------------------------------------------

        } else {
            lastUserEvents = null;
            lastFriendEvents = null;
            lastQueryDate = null;
        }

        prompt.append("USER QUESTION: ").append(userPrompt);

        return prompt.toString();
    }


    private Optional<LocalDate> extractDateFromPrompt(String prompt) {
        Matcher matcher = DATE_PATTERN.matcher(prompt);
        LocalDate today = LocalDate.now();

        while (matcher.find()) {
            String dateString = matcher.group();
            try {
                LocalDate parsedDate = LocalDate.parse(dateString.trim(), FLEXIBLE_DATE_FORMATTER);
                return Optional.of(parsedDate);

            } catch (DateTimeParseException e) {
                System.err.println("Could not parse date from prompt: " + dateString + " - " + e.getMessage());
            }
        }
        return Optional.empty();
    }


    private boolean isCalendarRelated(String prompt) {
        String lower = prompt.toLowerCase();
        return lower.contains("free") ||
                lower.contains("busy") ||
                lower.contains("schedule") ||
                lower.contains("available") ||
                lower.contains("calendar");
    }

    private String callOllama(String prompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String escapedPromptContent = prompt
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");

            String jsonPayload = String.format(
                    "{\"model\":\"deepseek-r1\"," +
                            "\"stream\":false," +
                            "\"messages\":[{" +
                            "\"role\":\"user\"," +
                            "\"content\":\"%s\"" +
                            "}]}",
                    escapedPromptContent
            );

            System.out.println("Sending JSON: " + jsonPayload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/chat"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // Debug: Print the full response
            System.out.println("API Response: " + response.statusCode() +
                    " - " + response.body());

            if (response.statusCode() == 200) {
                return extractContent(response.body());
            } else {
                System.err.println("API Error: " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("API Call Failed:");
            e.printStackTrace();
            return null;
        }
    }

    private String extractContent(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode contentNode = rootNode.path("message").path("content");

            if (contentNode.isTextual()) {
                return contentNode.asText();
            } else {
                System.err.println("JSON response does not contain expected 'message.content' text node.");
                return null;
            }

        } catch (Exception e) {
            System.err.println("Failed to parse response with Jackson:");
            e.printStackTrace();
            return null;
        }
    }

    private String cleanResponse(String response) {
        if (response == null) return "";

        String cleaned = response.replaceAll("(?s)\\s*<think>.*?<\\/think>\\s*", "");

        cleaned = cleaned.replaceAll("^\"|\"$", "").trim();

        return cleaned;
    }

    private void playTypingAnimation(TextArea textArea, String text) {
        System.out.println("Attempting to play typing animation for text of length: " + (text != null ? text.length() : 0)); // Debug log

        textArea.setText("");

        Thread typingThread = new Thread(() -> {
            try {
                for (int i = 0; i < text.length(); i++) {
                    final int index = i;
                    Platform.runLater(() -> textArea.setText(text.substring(0, index + 1)));
                    Thread.sleep(20); // Reduced sleep time for faster typing
                }
                // Ensure the full text is set at the very end and request layout
                Platform.runLater(() -> {
                    textArea.setText(text);
                    textArea.requestLayout(); // Request a layout pass
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
                // Ensure full text is set even if interrupted and request layout
                Platform.runLater(() -> {
                    textArea.setText(text);
                    textArea.requestLayout();
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Ensure full text is set even if other errors occur and request layout
                Platform.runLater(() -> {
                    textArea.setText(text);
                    textArea.requestLayout();
                });
            }
        });

        typingThread.start();
    }

    // ─── Mini Day View ────────────────────────────────────────────────
    private void renderMiniDayView() {
        miniDayView.getChildren().clear();
        miniDayView.getRowConstraints().clear();

        Label header = new Label(currentDate.format(DateTimeFormatter.ofPattern("d MMM")));
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 32px; -fx-text-fill: #2e014f;");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.TOP_LEFT);
        miniDayView.add(header, 0, 0, 2, 1);

        RowConstraints spacer = new RowConstraints(30);
        miniDayView.getRowConstraints().add(spacer);

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

//    private void renderMiniDayView() {
//        miniDayView.getChildren().clear();
//        miniDayView.getRowConstraints().clear();
//
//        Label dateLbl = new Label(currentDate.format(DateTimeFormatter.ofPattern("d MMM")));
//        dateLbl.setStyle("-fx-font-weight:bold; -fx-font-size:50px; -fx-text-fill:#333;");
//        dateLbl.setMaxWidth(Double.MAX_VALUE);
//        dateLbl.setAlignment(Pos.TOP_LEFT);
//        miniDayView.add(dateLbl, 0, 0, 2, 1);
//
//        RowConstraints spacer = new RowConstraints();
//        spacer.setVgrow(Priority.ALWAYS);
//        miniDayView.getRowConstraints().add(spacer);
//    }

    // ─── Helper ────────────────────────────────────────────────────────

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
