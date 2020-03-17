package socotra.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import socotra.util.Util;

public class RegisterController {
    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    public void registerNewAccount() {
        try {
            String usernameText = usernameField.getText();
            String passwordText = passwordField.getText();

            if (usernameText.isEmpty() || passwordText.isEmpty()) {
                Util.generateAlert(Alert.AlertType.ERROR, "Fill in all fields", "Form invalid.", "Try again.").show();
            }
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
        }
    }
}
