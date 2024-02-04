package main.hr.production.threads;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.util.Callback;
import main.hr.production.app.fxutils.FXUtils;
import main.hr.production.database.DatabaseUtils;
import main.hr.production.exceptions.unchecked.ExpenditureCategoryDuplicateException;
import main.hr.production.exceptions.unchecked.TextFieldEmptyException;
import main.hr.production.logback.SessionLogger;
import main.hr.production.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

abstract class DatabaseRunnable extends DatabaseSynchronizedOperations implements Runnable{}

public class DatabaseRunnableFactory {

    public static Runnable createBankAccount(BankAccount bankAccount){
        return new DatabaseRunnable() {
            @Override
            public void run() {
                super.createBankAccount(bankAccount);
            }

        };
    }

    public static Runnable writeExpenditureToDatabase(Expenditure madeExpense, BankAccount currentLinkedBankAccount){
        return new DatabaseRunnable() {
            @Override
            public void run() {
                super.writeExpenditureToDatabase(madeExpense, currentLinkedBankAccount);
            }
        };
    }

    public static Runnable updateExpenditure(Expenditure expenditure){
        return new DatabaseRunnable() {
            @Override
            public void run() {
                super.updateExpenditure(expenditure);
            }

        };
    }

    public static Runnable deleteAllTracesOfAccount(Long accountID){
        return new DatabaseRunnable() {
            @Override
            public void run() {
                super.deleteAllTracesOfAccount(accountID);
            }
        };
    }

    public static Runnable updateIncome(Income income){
        return new DatabaseRunnable() {
            @Override
            public void run() {
                super.updateIncome(income);
            }
        };
    }

    public static Runnable writeExportToTextArea(TextArea textAreaToWriteExport, BankAccount currentLinkedBankAccount){
        return new DatabaseRunnable() {
            @Override
            public void run() {

                CurrencyMask total = new CurrencyMask(BigDecimal.ZERO);

                List<Expenditure> listOfExpenditures = super.readAllExpendituresToBankAccount(currentLinkedBankAccount);
                List<Income> listOfIncomes = super.readALlIncomeToBankAccount(currentLinkedBankAccount);

                listOfExpenditures.stream().map(Transaction::getTransactionAmountCM).forEach(total::subtractFromAmount);
                listOfIncomes.stream().map(Transaction::getTransactionAmountCM).forEach(total::addOntoAmount);

                List<Transaction> listOfTransactions =new ArrayList<>(listOfIncomes);
                listOfTransactions.addAll(listOfExpenditures);

                Platform.runLater(() -> textAreaToWriteExport.setText(listOfTransactions.stream().map(Object::toString).collect(Collectors.joining("\n")) + "\nTOTAL SUM---------------------------------------------->\n" + total.getTextualAmount()));
            }

        };

    }

    public static Runnable setExpenditureCategoryCombobox(ComboBox<ExpenditureCategory> eCComboBox){
        return setExpenditureCategoryCombobox(eCComboBox, null);
    }


    public static Runnable setExpenditureCategoryCombobox(ComboBox<ExpenditureCategory> eCComboBox, ExpenditureCategory selectOption){

        return new DatabaseRunnable() {

            @Override
            public void run() {

                List<ExpenditureCategory> listOfExpenditure = super.readALlExpenditureCategories();

                Platform.runLater(() -> {

                    eCComboBox.getItems().addAll(listOfExpenditure);

                    Callback<ListView<ExpenditureCategory>, ListCell<ExpenditureCategory>> cellFactory = new Callback<ListView<ExpenditureCategory>, ListCell<ExpenditureCategory>>() {
                        @Override
                        public ListCell<ExpenditureCategory> call(ListView<ExpenditureCategory> expenditureCategoryListView) {

                            return new ListCell<ExpenditureCategory>() {
                                @Override
                                protected void updateItem(ExpenditureCategory expenditureCategory, boolean b) {

                                    if (Optional.ofNullable(expenditureCategory).isEmpty() || b) {
                                        setText(null);
                                        setGraphic(null);
                                        return;
                                    }

                                    super.updateItem(expenditureCategory, b);
                                    setGraphic(FXUtils.returnComboboxColorWrapper(expenditureCategory));

                                }

                            };
                        }

                    };

                    eCComboBox.setCellFactory(cellFactory);
                    eCComboBox.setButtonCell(cellFactory.call(null));

                    if(Optional.ofNullable(selectOption).isPresent()){
                        eCComboBox.getSelectionModel().select(selectOption);
                    }
                    else{
                        eCComboBox.getSelectionModel().select(0);
                    }

                });
            }

        };
    }

    public static Runnable deleteObjectByIDThread(String tableName, Long objectID){
        return new DatabaseRunnable() {
            @Override
            public void run() {
                super.deleteObjectByID(tableName, objectID);
            }
        };
    }

    public static Runnable writeIncomeToDatabase(Income income, BankAccount currentLinkedBankAccount){
        return new DatabaseRunnable() {
            @Override
            public void run() {
                super.writeIncomeToDataBase(income, currentLinkedBankAccount);
            }
        };

    }

    public static Runnable readAllIncomeFromAccount(BankAccount currentLinkedBankAccount, TableView<Income> modifiableTableView){
        return new DatabaseRunnable() {
            @Override
            public void run() {
                List<Income> listOfIncome = super.readALlIncomeToBankAccount(currentLinkedBankAccount);
                Platform.runLater(() ->{
                    modifiableTableView.getItems().clear();
                    modifiableTableView.setItems(FXCollections.observableArrayList(listOfIncome));
                });
            }
        };
    }

    public static Runnable readAllIncomeFromAccount(BankAccount currentLinkedBankAccount, TableView<Income> modifiableTableView, String hasToHaveDescription, LocalDate from, LocalDate to){
        return new DatabaseRunnable() {
            @Override
            public void run() {
                List<Income> listOfIncome = super.readAllIncomeToBankAccountFiltered(currentLinkedBankAccount, hasToHaveDescription, from, to);
                Platform.runLater(() ->{
                    modifiableTableView.getItems().clear();
                    modifiableTableView.setItems(FXCollections.observableArrayList(listOfIncome));
                });
            }
        };
    }

    public static Runnable readAllExpendituresFromAccount(BankAccount currentLinkedBankAccount, TableView<Expenditure> modifiableTableView){
        return new DatabaseRunnable() {
            @Override
            public void run() {
                List<Expenditure> listOfExpenditures = super.readAllExpendituresToBankAccount(currentLinkedBankAccount);
                Platform.runLater(() -> {
                    modifiableTableView.getItems().clear();
                    modifiableTableView.setItems(FXCollections.observableArrayList(listOfExpenditures));
                });
            }
        };
    }

    public static Runnable readAllExpendituresFromAccount(BankAccount currentLinkedBankAccount, TableView<Expenditure> modifiableTableView, ExpenditureCategory selectedCategory, String descriptionHasToHave, LocalDate from, LocalDate to){
        return new DatabaseRunnable() {
            @Override
            public void run() {
                List<Expenditure> listOfExpenditures = super.readAllExpendituresToBankAccountFiltered(currentLinkedBankAccount, selectedCategory, descriptionHasToHave, from, to);
                Platform.runLater(() -> {
                    modifiableTableView.getItems().clear();
                    modifiableTableView.setItems(FXCollections.observableArrayList(listOfExpenditures));
                });
            }
        };
    }

    public static Runnable setTableViewForExpenditureCategory(TableView<ExpenditureCategory> modifiableTableView){
        return new DatabaseRunnable() {
            @Override
            public void run() {
                List<ExpenditureCategory> refreshedList = super.readALlExpenditureCategories();
                Platform.runLater(() ->{
                    modifiableTableView.setItems(FXCollections.observableArrayList(refreshedList));
                });
            }
        };
    }

    public static Runnable deleteAndRefreshExpenditureCategoryTableView(ExpenditureCategory expenditureCategoryToBeDeleted ,TableView<ExpenditureCategory> modifiableTableView){
        return new DatabaseRunnable() {
            @Override
            public void run() {
                super.clearAllInTableWhereID("EXPENDITURE", "ID_CATEGORY", expenditureCategoryToBeDeleted.getObjectID());
                super.deleteObjectByID("EXPENDITURE_CATEGORY", expenditureCategoryToBeDeleted.getObjectID());
                List<ExpenditureCategory> refreshedList = super.readALlExpenditureCategories();
                Platform.runLater(() ->{
                    modifiableTableView.getItems().clear();
                    modifiableTableView.setItems(FXCollections.observableArrayList(refreshedList));
                });
            }
        };
    }

    public static Runnable writeExpenditureCategoryAndRefreshTableView(ExpenditureCategory expenditureCategory, TableView<ExpenditureCategory> modifiableTableView, TextField redOnContainsTextField, Account currentLoginAccount){
        return new DatabaseRunnable() {
            @Override
            public void run() {

                try{
                    checkForAlreadyExistingExpenditureCategory(expenditureCategory, redOnContainsTextField);
                }catch (ExpenditureCategoryDuplicateException | TextFieldEmptyException fieldEntryExceptions){
                    SessionLogger.logExceptionOnCurrentAccount(fieldEntryExceptions);
                    return;
                }

                super.writeExpenditureCategoryToDatabase(expenditureCategory);
                List<ExpenditureCategory> refreshedList = super.readALlExpenditureCategories();
                new Thread(FileThreadFactory.serializeAccountChangeToFile(new AccountAddChange<>(currentLoginAccount, expenditureCategory)));
                Platform.runLater(() ->{
                    modifiableTableView.getItems().clear();
                    modifiableTableView.setItems(FXCollections.observableArrayList(refreshedList));
                });

            }
        };
    }

}