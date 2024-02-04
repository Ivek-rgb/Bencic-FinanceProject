package main.hr.production.app.logincontrollers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import main.hr.production.app.fxutils.FXUtils;
import main.hr.production.model.Account;
import main.hr.production.threads.FileThreadFactory;

import java.util.List;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    @FXML
    public void loginLogic(){

        String output = FXUtils.checkForEmptyTextFields(List.of(usernameField, passwordField));

        if(!output.isEmpty()) {
            errorLabel.setText(output);
            return;
        }

        String name = usernameField.getText();
        String password = passwordField.getText();

        new Thread(FileThreadFactory.loginLogicThread(new Account(name, password), errorLabel)).start();

    }

    @FXML
    public void clearTextFields(){
        usernameField.clear();
        passwordField.clear();
        errorLabel.setText("");
    }

    @FXML
    public void loadRegisterPage(){
        FXUtils.switchScenes("register-screen.fxml");
    }

}