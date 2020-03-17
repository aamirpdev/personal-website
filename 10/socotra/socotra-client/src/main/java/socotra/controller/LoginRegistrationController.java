package socotra.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import socotra.Client;
import socotra.model.LoginRegistrationModel;
import socotra.util.Util;

import java.io.IOException;

public class LoginRegistrationController {

    @FXML
    private TextField serverField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    /**
     * Login event of loginButton.
     *
     * @param event The login event.
     */
    @FXML
    public void login(ActionEvent event) {
        String serverName = serverField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (Util.isEmpty(username) || Util.isEmpty(password)) {
            Util.generateAlert(Alert.AlertType.WARNING, "Warning", "Validate Error.", "Please input the info.").show();
            return;
        }
        loginButton.setText("login...");
        Client.setLoginRegistrationModel(new LoginRegistrationModel());
        int errorType = Client.getLoginRegistrationModel().handleLogin(serverName, username, password);
        loginButton.setText("login");
        handleErrorType(errorType);
    }

    /**
     * Registration event of register button.
     *
     * @param event The register event.
     */
    @FXML
    public void register(ActionEvent event) {
        String serverName = serverField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (Util.isEmpty(username) || Util.isEmpty(password)) {
            Util.generateAlert(Alert.AlertType.WARNING, "Warning", "Validate Error.", "Please input the info.").show();
            return;
        }
        registerButton.setText("register...");
        Client.setLoginRegistrationModel(new LoginRegistrationModel());
        int errorType = Client.getLoginRegistrationModel().handleRegistration(serverName, username, password);
        registerButton.setText("register");
        handleErrorType(errorType);
    }

    private void handleErrorType(int errorType) {
        System.out.println("handleErrorType " + errorType);
        switch (errorType) {
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
                loadHomePage();
                break;
        }
    }

    public void register() { LoadPage("/view/register.fxml");
    }

    private void loadHomePage() {
        // Load .fxml file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/home.fxml"));
        Pane tempPane = null;
        try {
            tempPane = loader.load();

            Client.setHomeController(loader.getController());

            // Construct scene
            Scene tempScene = new Scene(tempPane);
            // Set scene
            Client.setScene(tempScene);
        } catch (Exception e) {
            e.printStackTrace();
            Util.generateAlert(Alert.AlertType.ERROR, "Error", "Unexpected Error.", "Try again.").show();
        }
    }

    private void LoadPage(String pageName) {
        Parent root = null;
        Stage stage = null;
        try {
            root = FXMLLoader.load(getClass().getResource(pageName));
            Scene scene = new Scene(root, 400, 400);
            stage = new Stage();
            stage.setTitle("REGISTER");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
