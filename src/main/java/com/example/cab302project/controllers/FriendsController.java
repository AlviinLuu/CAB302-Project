package com.example.cab302project.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class FriendsController {
    @FXML
    private ListView<String> friendsList;

    @FXML
    private void onAddFriend() {
        // Just for demo: add a dummy friend
        friendsList.getItems().add("New Friend " + (friendsList.getItems().size() + 1));
    }

    @FXML
    private void onRemoveFriend() {
        // Remove selected friend
        String selected = friendsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            friendsList.getItems().remove(selected);
        }
    }
}
