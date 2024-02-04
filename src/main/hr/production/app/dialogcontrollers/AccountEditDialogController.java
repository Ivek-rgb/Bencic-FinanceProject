package main.hr.production.app.dialogcontrollers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import main.hr.production.fileutils.AccountFileUtils;
import main.hr.production.model.Account;
import main.hr.production.model.AccountUpdateChange;
import main.hr.production.threads.AccountFileOperations;
import main.hr.production.threads.FileThreadFactory;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountEditDialogController extends ChangeDialogController<Account>{

    Account selectedAccount;

    @FXML
    private TextField nameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField passwordRepeatField;
    @FXML
    private Label errorLabel;

    public void initialize(){}

    @Override
    public void setSelected(Account passedAccount, Account currentLoginAccount){
        this.selectedAccount = passedAccount;
        this.currentLoginAccount = currentLoginAccount;
        nameField.setText(selectedAccount.getName());
    }

    @Override
    public void applyChange() {

        StringBuilder errorAppender = new StringBuilder();

        Map<String, Account> hashedMapOfAccounts = AccountFileOperations.returnAccountHashedByName();

        AccountUpdateChange<Account> accountUpdatedChange = new AccountUpdateChange<>(currentLoginAccount, selectedAccount);

        if(!nameField.getText().equals(selectedAccount.getName()) && hashedMapOfAccounts.containsKey(nameField.getText())){
            errorAppender.append("*Account with that name already exists in database");
        }

        if(nameField.getText().isEmpty()) errorAppender.append("*You cannot leave your name blank");

        if(!passwordField.getText().isEmpty() && !passwordRepeatField.getText().equals(passwordField.getText())){
            errorAppender.append(errorAppender.isEmpty() ? "" : "\n").append("*Passwords do not match");
        }

        if(!errorAppender.isEmpty()){
            errorLabel.setText(errorAppender.toString());
            return;
        }

        if(!passwordField.getText().isEmpty()){
            selectedAccount.setPassword(AccountFileUtils.hashPassword(passwordField.getText()));
        }

        String oldName = selectedAccount.getName();

        selectedAccount.setName(nameField.getText());

        hashedMapOfAccounts.put(oldName, selectedAccount);

        try(ExecutorService threadExecutor = Executors.newFixedThreadPool(2)){
            threadExecutor.execute(FileThreadFactory.truncateAndWriteAccountsToFile(hashedMapOfAccounts.values()));
            threadExecutor.execute(FileThreadFactory.serializeAccountChangeToFile(accountUpdatedChange));
        }
    }

    @Override
    public void clear() {
        passwordField.clear();
        passwordRepeatField.clear();
        errorLabel.setText("");
    }

}