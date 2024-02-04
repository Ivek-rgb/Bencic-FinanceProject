package main.hr.production.threads;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import main.hr.production.app.AccountControllers;
import main.hr.production.app.HelloApplication;
import main.hr.production.app.dashboard.WelcomeController;
import main.hr.production.app.menubar.MenuBarController;
import main.hr.production.database.DatabaseUtils;
import main.hr.production.fileutils.AccountFileUtils;
import main.hr.production.logback.SessionLogger;
import main.hr.production.model.Account;
import main.hr.production.model.BankAccount;
import main.hr.production.model.Change;
import main.hr.production.model.CurrencyMask;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

abstract class RunnableFileOperation extends AccountFileOperations implements Runnable{}
abstract class RunnableCurrencyOperation extends CurrencyFileOperations implements Runnable{}
abstract class RunnableSerializationOperation extends ObjectFileOperations implements Runnable{}

public class FileThreadFactory{

    public static Runnable offlineCurrencyExchangeMap(){
        return new Thread(new RunnableCurrencyOperation() {
            @Override
            public void run() {
                CurrencyFileOperations.updateCurrencyConversionMap(super.readObjsFromFile().stream());
            }

        });
    }

    public static Runnable writeAccountToFileThread(Account account){
        return new Thread(new RunnableFileOperation(){
            @Override
            public void run() {
                super.writeObjsIntoFile(account);
            }
        });
    }

    public static Runnable truncateAndWriteAccountsToFile(Collection<Account> collectionOfAccounts){
        return new RunnableFileOperation() {
            @Override
            public void run() {
                super.truncateAndWriteCollectionOfAccounts(collectionOfAccounts);
            }
        };
    }

    public static Runnable deleteAccountAndSaveToFile(Account account, TableView<Account> modifiableTableView){
        return new RunnableFileOperation() {
            @Override
            public void run() {
                super.deleteAccountAndSaveToFile(account);

                Platform.runLater(() ->{
                    modifiableTableView.setItems(FXCollections.observableArrayList(AccountFileOperations.readAllAccountsFromFile()));
                });

            }
        };
    }

    public static Runnable deleteAccount(Account account){
        return new RunnableFileOperation() {
            @Override
            public void run() {
                super.deleteAccountAndSaveToFile(account);
            }
        };
    }

    public static Runnable refreshAccountTable(TableView<Account> modifiableTableView){
        return new RunnableFileOperation() {
            @Override
            public void run() {

                Platform.runLater(() ->{
                    modifiableTableView.setItems(FXCollections.observableArrayList(AccountFileOperations.readAllAccountsFromFile()));
                });

            }
        };
    }

    public static Runnable updateChosenCurrencyOfAccountThread(String selectedValue){
        return new Thread(
                new RunnableFileOperation() {
                    @Override
                    public void run() {
                        Map<String, Account> hashMapOfAccounts = returnAccountHashedByName();
                        hashMapOfAccounts.get(AccountControllers.getCurrentLoginAccount().getName()).setCurrencyUsed(selectedValue);
                        AccountControllers.getCurrentLoginAccount().setCurrencyUsed(selectedValue);
                        truncateAndWriteCollectionOfAccounts(hashMapOfAccounts.values());
                    }
                }
        );
    }

    public static Runnable promoteAccountToAdmin(Account account, TableView<Account> modifiableTableView){
        return new RunnableFileOperation() {
            @Override
            public void run() {

                Map<String, Account> hashMapOfAccounts = returnAccountHashedByName();
                hashMapOfAccounts.get(account.getName()).switchRoles();
                truncateAndWriteCollectionOfAccounts(hashMapOfAccounts.values());

                Platform.runLater(() -> {
                    modifiableTableView.getItems().clear();
                    modifiableTableView.setItems(FXCollections.observableArrayList(AccountFileOperations.readAllAccountsFromFile()));
                });

            }
        };
    }

    public static <T extends Serializable> Runnable serializeAccountChangeToFile(Change<Account, T> change){

        return new RunnableSerializationOperation() {
            @Override
            public void run() {
                List<Change> listOfChanges = super.readChangesFromFile();
                listOfChanges.add(change);
                super.writeObjToFile(listOfChanges);
            }

        };

    }

    public static Runnable refreshAndReadChanges(ListView<Change> listOfChanges){
        return new RunnableSerializationOperation() {
            @Override
            public void run() {
                Platform.runLater(() ->{
                    listOfChanges.getItems().clear();
                    listOfChanges.setItems(FXCollections.observableArrayList(super.readChangesFromFile().stream().sorted(Comparator.comparing(Change::getDateTimeOfChange)).toList()));
                });
            }
        };
    }

    public static Runnable loginLogicThread(Account account, Label errorLabel){

        return new RunnableFileOperation() {
            @Override
            public void run() {

                try(Connection testConnection = DatabaseUtils.startConnection()){
                } catch (SQLException e) {
                    SessionLogger.sessionLogger.error("Could not establish connection to database", e);
                    Platform.runLater(() ->{
                        errorLabel.setText("* Could not establish connection to database");
                    });
                    return;
                }

                Map<String, Account> mapOfAccounts = AccountFileOperations.returnAccountHashedByName();

                if(mapOfAccounts.containsKey(account.getName())){

                    if(AccountFileUtils.verifyPassword(account.getPassword(), mapOfAccounts.get(account.getName()).getPassword())){

                        new CurrencyFileOperations().updateCurrencyMap();

                        Account loggedAccount = mapOfAccounts.get(account.getName());
                        Optional<BankAccount> linkedBankAccount =  new DatabaseSynchronizedOperations().searchBankAccountByAccountID(loggedAccount.getObjectID());

                        linkedBankAccount.ifPresentOrElse(

                                presentBankAccount ->{

                                    SessionLogger.sessionLogger.info( account + " has logged in");

                                    CurrencyMask.setDefaultConversionValue(loggedAccount.getCurrencyUsed());
                                    AccountControllers.setAccountFromClass(loggedAccount, presentBankAccount);

                                    MenuBarController.setAdministrator(loggedAccount.getAccountType().equals(Account.AccountType.ACCOUNT_ADMIN));


                                    Platform.runLater(() -> {

                                        try {

                                            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("welcome-screen.fxml"));
                                            Parent root = fxmlLoader.load();
                                            WelcomeController welcomeController = fxmlLoader.getController();
                                            AccountControllers.setCurrentOpenController(welcomeController);

                                            welcomeController.setTotalBalance();
                                            welcomeController.setWelcomeText();

                                            HelloApplication.getMainStage().setScene(new Scene(root));

                                        } catch (IOException e) {
                                            SessionLogger.sessionLogger.error("Failed to set up 'Welcome screen'", e);
                                        }

                                    });

                                },

                                () ->{
                                    Platform.runLater(() ->{
                                        errorLabel.setText("* [login failed] could not find your linked bank account");
                                    });
                                }

                        );

                    }

                    Platform.runLater(() -> {

                        errorLabel.setText("* password does not match with the one used on account");

                    });

                }else{

                    Platform.runLater(() ->{

                        errorLabel.setText("* no account with this name has been found");

                    });

                }

            }
        };
    }
}