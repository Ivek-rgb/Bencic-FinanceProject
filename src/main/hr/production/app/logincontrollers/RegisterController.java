package main.hr.production.app.logincontrollers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import main.hr.production.app.fxutils.FXUtils;
import main.hr.production.database.DatabaseUtils;
import main.hr.production.logback.SessionLogger;
import main.hr.production.model.Account;
import main.hr.production.model.BankAccount;
import main.hr.production.threads.AccountFileOperations;
import main.hr.production.threads.DatabaseRunnableFactory;
import main.hr.production.threads.FileThreadFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
public class RegisterController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField passwordCheckField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label confirmPassword;
    boolean passwordsMatch;

    public void setPasswordMatchText(String newValue, PasswordField checker){
        if(newValue.isEmpty()){
            confirmPassword.setText("");
            passwordsMatch = false;
            return;
        }
        if(passwordCheckLogic(checker.getText(), newValue)){
            confirmPassword.setText("Passwords do match");
            confirmPassword.setTextFill(Color.GREEN);
            passwordsMatch = true;
        }else{
            confirmPassword.setText("Passwords do not match");
            confirmPassword.setTextFill(Color.RED);
            passwordsMatch = false;
        }
    }

    public void initialize(){
        passwordCheckField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            setPasswordMatchText(newValue, passwordField);
        });
        passwordField.textProperty().addListener((observableValue, oldValue, newValue) ->{
            setPasswordMatchText(newValue, passwordCheckField);
        });
    }

    public void returnToLogin(){
        FXUtils.switchScenes("login-page.fxml");
    }

    public void registerLogic(){

        try(Connection testConnection = DatabaseUtils.startConnection()){
        } catch (SQLException e) {
            SessionLogger.sessionLogger.error("Could not establish connection to database", e);
            Platform.runLater(() -> errorLabel.setText("* Could not establish connection to database"));
            return;
        }

        String output = FXUtils.checkForEmptyTextFields(List.of(usernameField, passwordField));

        if(output.isEmpty() && passwordsMatch){

            Account writeAccount = new Account(usernameField.getText(), passwordField.getText());

            if(AccountFileOperations.returnAccountHashedByName().containsKey(writeAccount.getName())){
                errorLabel.setText("* There already exists account with the same name!");
                return;
            }

            Thread writeThread = new Thread(FileThreadFactory.writeAccountToFileThread(writeAccount));
            writeThread.start();
            clearTextFields();
            Thread createBankAccount = new Thread(DatabaseRunnableFactory.createBankAccount(new BankAccount(AccountFileOperations.returnLatestAddedId())));
            createBankAccount.start();

            try {
                createBankAccount.join();
            } catch (InterruptedException e) {
                SessionLogger.sessionLogger.error("Failure in thread to write bank account, deleting account...", e);
                new Thread(FileThreadFactory.deleteAccount(writeAccount)).start();
                errorLabel.setText("* Error while creating bank account for your account, please retry");
                return;
            }

            errorLabel.setText("* Account " + writeAccount.getName() + " successfully created!");

        }else{
            errorLabel.setText(output);
            if(passwordCheckField.getText().isEmpty())
                errorLabel.setText(errorLabel.getText() + "\n* \"repeat password\" field must not be empty");
            else
                errorLabel.setText(errorLabel.getText() + "\n* passwords need to match");
        }

    }

    public boolean passwordCheckLogic(String password, String checkPassword){
        if(password.length() != checkPassword.length())
            return false;
        return password.equals(checkPassword);
    }

    @FXML
    public void clearTextFields(){
        usernameField.clear();
        passwordField.clear();
        passwordCheckField.clear();
        errorLabel.setText("");
    }

}
