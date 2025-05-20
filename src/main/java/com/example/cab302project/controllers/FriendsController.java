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
import java.io.IOException;
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

/**
 * Controller class for the Friends view of the application.
 * Manages friend lists, friend requests (sending, accepting, declining, deleting),
 * user search, displaying friend profiles, and interacting with an ollama AI
 * to find common free time based on user and friend schedules.
 */
public class FriendsController {
    /**
     * The main VBox container for the friends view content.
     */
    @FXML private VBox mainContent;
    /**
     * TextArea where the user inputs prompts for Ollama.
     */
    @FXML private TextArea aiPromptField;
    /**
     * Button in the sidebar that shows the current user's profile name.
     */
    @FXML private Button profileButton;
    /**
     * TextArea where the Ollama  response is displayed with a typing animation.
     */
    @FXML private TextArea aiResponseLabel; // Note: Label is a TextArea here
    /**
     * Label displaying the header for the currently viewed friend's profile
     */
    @FXML private Label profileHeaderLabel;
    /**
     * ImageView displaying the profile picture of the currently viewed friend.
     */
    @FXML private ImageView profileImage;
    /**
     * Label displaying the username of the currently viewed friend.
     */
    @FXML private Label usernameLabel;
    /**
     * Label displaying the email address of the currently viewed friend
     */
    @FXML private Label nameLabel; // Displays friend's email
    /**
     * Label displaying the bio of the currently viewed friend.
     */
    @FXML private Label bioLabel;
    /**
     * GridPane used to display a mini-calendar view for the current day in the sidebar.
     */
    @FXML private GridPane miniDayView;
    /**
     * ImageView for displaying the application logo.
     */
    @FXML private ImageView logoImage;
    /**
     * ListView to display the results of searching for users.
     */
    @FXML private ListView<String> searchResultsList;
    /**
     * TextField for the user to enter search queries for users.
     */
    @FXML private TextField searchUserField;
    /**
     * ListView to display the friend requests sent by the current user that are still pending (outgoing requests).
     */
    @FXML private ListView<String> pendingRequestsList;   // outgoing requests from current user
    /**
     * ListView to display the friend requests received by the current user that are still pending (incoming requests).
     */
    @FXML private ListView<String> incomingRequestsList;  // incoming requests to current user
    /**
     * ComboBox to select a friend whose profile and schedule will be viewed.
     */
    @FXML private ComboBox<String> friendSelector;
    /**
     * ObservableList holding usernames of all users in the system (excluding the current user) for search.
     */
    private final ObservableList<String> allUsers        = FXCollections.observableArrayList();
    /**
     * ObservableList holding usernames of users to whom the current user has sent a pending friend request.
     */
    private final ObservableList<String> pendingOutgoing = FXCollections.observableArrayList();
    /**
     * ObservableList holding usernames of users who have sent a pending friend request to the current user.
     */
    private final ObservableList<String> incomingReqs    = FXCollections.observableArrayList();
    /**
     * ObservableList holding usernames of users who are friends with the current user.
     */
    private final ObservableList<String> friendList      = FXCollections.observableArrayList();
    /**
     * Stores the last response received from the AI assistant. Used for conversation history.
     */
    private String lastAIResponse = null;
    /**
     * Stores the list of events for the currently logged-in user from the last query prompt.
     */
    private List<Event> lastUserEvents = null;
    /**
     * Stores the list of events for the currently loaded friend from the last query prompt.
     */
    private List<Event> lastFriendEvents = null;
    /**
     * Stores the date that was used in the last Ollama schedule query.
     */
    private LocalDate lastQueryDate = null;
    /**
     * The current date displayed in the mini-day view sidebar.
     */
    private LocalDate currentDate = LocalDate.now();
    /**
     * The database connection instance.
     */
    private Connection connection;
    /**
     * An instance of the User Data Access Object for interacting with user data.
     */
    private SqliteUserDAO userDAO; // Using the concrete implementation
    /**
     * The email address of the friend whose profile is currently loaded and displayed.
     */
    private String loadedFriendEmail = null;
    /**
     * ObjectMapper instance for JSON processing, specifically for parsing Ollama API responses.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Regex pattern to identify date strings in user prompts
     */
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "\\b(?:January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{1,2}(?:,\\s*\\d{4})?\\b",
            Pattern.CASE_INSENSITIVE // Match month names regardless of case
    );

    /**
     * DateTimeFormatter for the standard event time format used internally and in the database ("MM/dd/yyyy HH:mm:ss").
     */
    private static final DateTimeFormatter EVENT_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    /**
     * DateTimeFormatter to parse date strings from user prompts, supporting formats like "Month Day" and "Month Day, Year".
     * Defaults the year to the current year if not explicitly provided.
     */
    private static final DateTimeFormatter FLEXIBLE_DATE_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMMM d")
            .optionalStart()
            .appendLiteral(",")
            .optionalStart()
            .appendLiteral(" ")
            .optionalEnd()
            .appendPattern("yyyy")
            .optionalEnd()
            .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear()) // If year is not present, use the current year
            .toFormatter();

    /**
     * Initializes the controller after the FXML file has been loaded.
     * Ensures a user is logged in, establishes database connection, loads UI logo,
     * fetches initial data (all users, friend lists, requests) from the database,
     * wire UI list view, and renders the mini-day view.
     */
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

        renderMiniDayView();
    }

    /**
     * Loads the usernames of all users from the database into the allUsers observable list.
     * Excludes the currently logged-in user.
     */
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
    /**
     * Refreshes the list of the current user's friends by fetching data from the database
     * and updating the observable list friendList.
     */
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
    /**
     * Refreshes the list of pending friend requests sent to the current user (incoming requests).
     * Fetches data from the database and updates the incomingReqs observable list.
     */
    private void refreshIncomingRequests() {
        incomingReqs.setAll(
                userDAO.getPendingFriendRequests(Session.getLoggedInUser().getUsername())
                        .stream()
                        .map(User::getUsername)
                        .collect(Collectors.toList())
        );
    }
    /**
     * Refreshes the list of pending friend requests sent by the current user (outgoing requests).
     * Fetches data directly using a SQL query and updates the pendingOutgoing observable list.
     */
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
    /**
     * Loads and displays the profile details of a specified user
     * in the profile display area. Fetches user data and profile image using the user DAO.
     * @param username The username of the user whose profile should be loaded.
     */
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
    /**
     * Handles the action for the Remove Friend button.
     * Removes the selected friend from the current user's friend list,
     * clears the displayed profile information, and updates the UI.
     * Displays a confirmation or error alert based on selection.
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

            Scene scene = new Scene(loginRoot, 380, 500);
            loginStage.setScene(scene);

            loginStage.show();

            // Close the current Settings page
            Stage currentStage = (Stage) mainContent.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
    /**
     * Handles the action for the Send Request button.
     * Sends a friend request to the user selected.
     * Validates the selection and existing friendships/requests before sending.
     * Updates the outgoing requests list on success.
     */
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
    /**
     * Handles the action for the  Accept Request button.
     * Accepts the selected incoming friend request.
     * Updates the database via the user DAO and refreshes the incoming requests and friend lists on success.
     */
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

    /**
     * Handles the action for the Decline Request button.
     * Declines the selected incoming friend request.
     * Updates the database via the user DAO and refreshes the incoming requests list on success.
     */
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
    /**
     * Handles the action for the Delete Request button.
     * Deletes the selected outgoing friend request from the database.
     * Updates the database directly using SQL and refreshes the outgoing requests list on success.
     */
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

    /**
     * Navigates to the calendar view (home).
     * Loads the calendar-view FXML and closes the current Friends view.
     * @throws RuntimeException if the FXML file fails to load.
     */
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
    /**
     * Opens the settings page and closes the current window.
     * @throws Exception if the FXML file cannot be loaded or displayed.
     */
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
    /**
     * Opens the user profile page and closes the current view.
     * @throws Exception if the FXML file cannot be loaded.
     */
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
    /**
     * Handles the event triggered by the Ollama prompt field (e.g., hitting Enter or clicking a send button).
     * Retrieves the user's prompt, builds a detailed prompt including user/friend schedule data,
     * calls the Ollama AI service in a background thread, and updates the AI response area with the result
     * using a typing animation.
     * Handles cases where no user is logged in or the Ollama API call fails.
     */
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

    /**
     * Helps ollama calculates potential free time slots between 7 AM and 11 PM (Typical waking hours) for a given date,
     * based on the combined schedules of the user and a friend.
     * Identifies gaps between events within the defined time window.
     *
     * @param userEvents A list of Event objects for the user on the target date.
     * @param friendEvents A list of Event objects for the friend on the target date.
     * @param date The LocalDate for which to calculate free time.
     * @return A formatted string listing the free time slots, or a message if none are found.
     */
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

    /**
     * Formats a time range from a start and end LocalDateTime into a "HH:mm to HH:mm" string.
     * @param start The start LocalDateTime of the time range.
     * @param end The end LocalDateTime of the time range.
     * @return A formatted string representing the time range.
     */
    private String formatTimeRange(LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return start.format(timeFormatter) + " to " + end.format(timeFormatter);
    }

    /**
     * Attempts to retrieve the email address of the friend from the lastFriendEvents list.
     * This assumes all events in lastFriendEvents belong to the same friend.
     * @return The email address of the friend whose events were last loaded, or null if the list is empty or contains no valid user.
     */
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

    /**
     * Constructs a detailed prompt string for the AI assistant based on the user's input,
     * previous conversation, and relevant schedule data (user's events, friend's events, calculated free time).
     * Includes system instructions for the AI.
     * @param userPrompt The original text prompt from the user.
     * @param currentUser The currently logged-in User object.
     * @return The combined prompt string formatted for the AI API.
     */
    private String buildAIPrompt(String userPrompt, User currentUser) {
        StringBuilder prompt = new StringBuilder();

        // System instruction - Tell AI to simply output the provided free time slots and other instructions
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


    /**
     * Attempts to extract a date from a user prompt string using a predefined regex pattern.
     * Supports month names (full or abbreviated) followed by a day, optionally with a comma and year.
     * Defaults the year to the current year if not present.
     * @param prompt The input string from the user.
     * @return An Optional object containing the extracted LocalDate if successful, otherwise an empty Optional.
     */
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

    /**
     * Checks if a user prompt string is related to calendar or scheduling based on keywords.
     * Used to determine if event data and free time calculations should be included in the AI prompt.
     * @param prompt The input string from the user.
     * @return true if the prompt contains relevant keywords, false otherwise.
     */
    private boolean isCalendarRelated(String prompt) {
        String lower = prompt.toLowerCase();
        return lower.contains("free") ||
                lower.contains("busy") ||
                lower.contains("schedule") ||
                lower.contains("available") ||
                lower.contains("calendar");
    }

    /**
     * Makes an HTTP POST request to the local Ollama API endpoint to get a chat completion response.
     * Sends the constructed prompt as a JSON payload.
     * @param prompt The prompt string to send to the Ollama model.
     * @return The response body string from the API if successful, or null if an error occurs.
     */
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

    /**
     * Parses the JSON response string from the Ollama chat API to extract the AI's message content.
     * Uses Jackson ObjectMapper to navigate the JSON structure.
     * @param json The JSON response string received from the API.
     * @return The text content of the AI's message if found, or null if parsing fails or the content is not text.
     */
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

    /**
     * Cleans up the AI's response string by removing potential thinking tags (`<think>...</think>`)
     * and leading/trailing quotes.
     * @param response The raw response string from the AI.
     * @return A cleaned string.
     */
    private String cleanResponse(String response) {
        if (response == null) return "";

        String cleaned = response.replaceAll("(?s)\\s*<think>.*?<\\/think>\\s*", "");

        cleaned = cleaned.replaceAll("^\"|\"$", "").trim();

        return cleaned;
    }

    /**
     * Simulates a typing animation for displaying text in a TextArea.
     * Updates the text character by character on the JavaFX Application Thread.
     * Runs in a background thread to not block the UI.
     * @param textArea The TextArea UI element to display the text in.
     * @param text The full text string to display.
     */
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
    /**
     * Renders a mini sidebar view showing hourly slots for the current day.
     * Displays 24 rows from 00:00 to 23:00 with time labels and empty event labels.
     * This is used to give a quick glance at the day's structure.
     */
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

    /**
     * Displays a standard JavaFX Alert dialog
     * The dialog has a title and a main content message.
     * Pauses the application thread until the user closes the dialog.
     * @param title The title text for the alert window.
     * @param message The main message content to display in the alert dialog.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
