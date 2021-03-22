package socotra.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import socotra.Client;
import socotra.model.RegistrationModel;
import socotra.util.Util;

import java.io.IOException;

public class RegistrationController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField serverField;

    @FXML
    private Button registerButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    void register(ActionEvent event) {
        String serverName = serverField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (Util.isEmpty(username) || Util.isEmpty(password)) {
            Util.generateAlert(Alert.AlertType.WARNING, "Warning", "Validate Error.", "Please input the info.").show();
            return;
        }
        registerButton.setText("register...");
        Client.setRegistrationModel(new RegistrationModel());
        int responseType = Client.getRegistrationModel().handleRegister(serverName, username, password);
        registerButton.setText("register");
        handleResponseType(responseType);
    }

    private void handleResponseType(int responseType) {
        System.out.println("handleResponseType " + responseType);
        switch (responseType) {
            // If the server name is not correct.
            case 1:
                Util.generateAlert(Alert.AlertType.ERROR, "Connection Error", "Invalidated Server.", "Try again.").show();
                break;
            // If the user is invalidated.
            case 2:
                Util.generateAlert(Alert.AlertType.ERROR, "Validation Error", "Invalidated user.", "Try again.").show();
                break;
            case 3:
                Util.generateAlert(Alert.AlertType.ERROR, "Validation Error", "User already registered.", "Try Login").show();
                break;
            default:
                goBackToLogin(new ActionEvent());
                break;
        }
    }

    @FXML
    void goBackToLogin(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        try {
            Pane tempPane = loader.load();
            Client.setLoginController(loader.getController());
            // Construct scene
            Scene tempScene = new Scene(tempPane);
            // Set scene
            Client.setScene(tempScene);
        } catch (IOException e) {
            e.printStackTrace();
            Util.generateAlert(Alert.AlertType.ERROR, "Error", "Unexpected Error.", "Try again.").show();
        }
    }


}

