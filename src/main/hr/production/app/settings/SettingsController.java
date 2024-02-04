package main.hr.production.app.settings;

import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import main.hr.production.app.AccountControllers;
import main.hr.production.app.HelloApplication;
import main.hr.production.app.dialogcontrollers.AccountEditDialogController;
import main.hr.production.app.menubar.MenuBarController;
import main.hr.production.logback.SessionLogger;
import main.hr.production.model.CurrencyEntryRecord;
import main.hr.production.model.CurrencyMask;
import main.hr.production.threads.AccountFileOperations;
import main.hr.production.threads.FileThreadFactory;

import java.io.IOException;

public class SettingsController extends AccountControllers {

    @FXML
    private Label settingsText;
    @FXML
    private JFXComboBox<String> currencyComboBox;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label dateOfCreationLabel;
    @FXML
    private MenuBarController menuComponentController;

    public void initializeFormDialog(){
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("custom-dialog-pane.fxml"));

        try {

            DialogPane testPane = fxmlLoader.load();
            Dialog<ButtonType> myDialog = new Dialog<>();

            AccountEditDialogController accountEditDialogController = fxmlLoader.getController();
            accountEditDialogController.setSelected(currentLoginAccount, currentLoginAccount);

            myDialog.setTitle("Edit your account dialog");

            myDialog.setDialogPane(testPane);

            myDialog.showAndWait();

            currentLoginAccount = AccountFileOperations.readAllAccountsFromFile().get(currentLoginAccount.getObjectID().intValue() - 1);

            menuComponentController.changeScreenSettings();

        } catch (IOException e) {
            SessionLogger.logExceptionOnCurrentAccount(e);
        }

    }

    public void initialize(){

        usernameLabel.setText(currentLoginAccount.getName());

        dateOfCreationLabel.setText(currentLinkedBankAccount.getDateOfCreation().toLocalDate().toString());
        currencyComboBox.setItems(FXCollections.observableArrayList(CurrencyMask.getConversionMap().values().stream().map(CurrencyEntryRecord::toInformativeString).toList()));

        currencyComboBox.getSelectionModel().select(CurrencyMask.getConversionMap().get(currentLoginAccount.getCurrencyUsed()).toInformativeString());
    }

    public void selectCurrency(){

        String selectedValue = currencyComboBox.getSelectionModel().getSelectedItem().split(" - ")[0];

        CurrencyMask.setDefaultConversionValue(selectedValue);

        new Thread(FileThreadFactory.updateChosenCurrencyOfAccountThread(selectedValue)).start();
    }

    @Override
    public void shutDownScheduledExecutors() {

    }
}
