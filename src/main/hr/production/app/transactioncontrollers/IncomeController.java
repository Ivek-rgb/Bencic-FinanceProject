package main.hr.production.app.transactioncontrollers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import main.hr.production.app.AccountControllers;
import main.hr.production.app.fxutils.FXUtils;
import main.hr.production.exceptions.checked.UnsuportedConversionException;
import main.hr.production.exceptions.unchecked.TextFieldEmptyException;
import main.hr.production.logback.SessionLogger;
import main.hr.production.model.*;
import main.hr.production.threads.DatabaseRunnableFactory;
import main.hr.production.threads.FileThreadFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.*;

public class IncomeController extends AccountControllers {

    @FXML
    private TextField containsDescriptionField;
    @FXML
    private TextField cashAmountField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private Label currencyText;
    @FXML
    private TextField descriptionField;
    @FXML
    private TableView<Income> incomeTableView;
    @FXML
    private TitledPane filterPane;
    private final TableColumn<Income, Income> operationalColumn = new TableColumn<>("Operations");
    public static boolean filterCollapsed = false;
    private final ScheduledExecutorService refreshTableExecutor = Executors.newSingleThreadScheduledExecutor();

    public void initialize(){

        datePicker.setValue(LocalDate.now());

        FXUtils.setTextFieldFormatterToNumericOnly(cashAmountField);
        FXUtils.setTableCols(Transaction.MAPPED_FIELDS, incomeTableView);

        filterPane.expandedProperty().set(!filterCollapsed);

        incomeTableView.getColumns().get(2).setPrefWidth(150);

        currencyText.setText(currentLoginAccount.getCurrencyUsed());

        new Thread(DatabaseRunnableFactory.readAllIncomeFromAccount(currentLinkedBankAccount, incomeTableView)).start();

        operationalColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Income, Income>, ObservableValue<Income>>() {
            @Override
            public ObservableValue<Income> call(TableColumn.CellDataFeatures<Income, Income> incomeIncomeCellDataFeatures) {
                return new SimpleObjectProperty<>(incomeIncomeCellDataFeatures.getValue());
            }
        });

        operationalColumn.setCellFactory(param -> new TableCell<>(){
            @Override
            protected void updateItem(Income income, boolean b) {

                super.updateItem(income, b);

                if(Optional.ofNullable(income).isEmpty() || b){
                    setGraphic(null);
                    setText(null);
                    return;
                }


                setGraphic(FXUtils.returnOperationalColumnGraphic(() -> returnDeleteButton(income), () -> returnUpdateButton(income)));

            }

        });

        incomeTableView.getColumns().add(operationalColumn);


        refreshTableExecutor.scheduleAtFixedRate(DatabaseRunnableFactory.readAllIncomeFromAccount(currentLinkedBankAccount, incomeTableView, containsDescriptionField.getText(), fromDatePicker.getValue(), toDatePicker.getValue()), 0, 10, TimeUnit.SECONDS);

    }

    public Button returnDeleteButton(Income income) {

        Button button = new Button("Delete");

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                if(FXUtils.createAndWaitConfirmation()){
                    try(ExecutorService executorService = Executors.newFixedThreadPool(1)){
                        executorService.execute(DatabaseRunnableFactory.deleteObjectByIDThread("INCOME", income.getObjectID()));
                        executorService.execute(FileThreadFactory.serializeAccountChangeToFile(new AccountDeleteChange<>(currentLoginAccount, income)));
                        refreshTableWithFilters();
                    }
                }

            }

        });

        return button;
    }


    public Button returnUpdateButton(Income income){

        Button button= new Button();

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                FXUtils.startEditIncomeDialog(income, currentLoginAccount);
                refreshTableWithFilters();

            }

        });

        return button;
    }

    public void clearInsertionBox(){
        descriptionField.clear();
        datePicker.setValue(LocalDate.now());
        cashAmountField.clear();
    }

    public void setOnExpansionRemember(){
        filterCollapsed = !filterCollapsed;
    }

    public void refreshTableWithFilters(){
        new Thread(DatabaseRunnableFactory.readAllIncomeFromAccount(currentLinkedBankAccount, incomeTableView, containsDescriptionField.getText(), fromDatePicker.getValue(), toDatePicker.getValue())).start();
    }

    public void writeAndRefresh(Income income){
        try(ExecutorService refreshExecutor = Executors.newFixedThreadPool(3)){
            refreshExecutor.execute(DatabaseRunnableFactory.writeIncomeToDatabase(income, currentLinkedBankAccount));
            refreshExecutor.execute(DatabaseRunnableFactory.readAllIncomeFromAccount(currentLinkedBankAccount, incomeTableView, containsDescriptionField.getText(), fromDatePicker.getValue(), toDatePicker.getValue()));
            refreshExecutor.execute(FileThreadFactory.serializeAccountChangeToFile(new AccountAddChange<>(currentLoginAccount, income)));
        }
    }

    public void parseIntoIncome(){

        try{
            FXUtils.makeTextFieldBordersRedOnRejection(cashAmountField);
        }catch (TextFieldEmptyException textFieldEmptyException){
            return;
        }

        BigDecimal incomeAmountInBigDecimal;

        try {
            incomeAmountInBigDecimal = FXUtils.handleBigDecimalConversionFromTextField(cashAmountField);
        } catch (UnsuportedConversionException e) {
            SessionLogger.sessionLogger.error("User input cannot be parsed to BigDecimal",  e);
            cashAmountField.clear();
            FXUtils.makeTextFieldBordersRedOnRejection(cashAmountField);
            return;
        }

        String descriptionOfIncome = descriptionField.getText();
        LocalDate dateOfIncome = datePicker.getValue();

        Income income = new Income.IncomeBuilder().withDescription(descriptionOfIncome).doneOnDate(dateOfIncome).ofAmount(new CurrencyMask(incomeAmountInBigDecimal, CurrencyMask.defaultConversionValue)).finishCreation();

        writeAndRefresh(income);
    }

    public void clearFiltersAndSearch(){

        containsDescriptionField.clear();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        refreshTableWithFilters();

    }

    @Override
    public void shutDownScheduledExecutors() {
        refreshTableExecutor.shutdownNow();
    }

}