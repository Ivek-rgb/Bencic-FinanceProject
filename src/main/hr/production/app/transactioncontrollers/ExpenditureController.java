package main.hr.production.app.transactioncontrollers;

import com.jfoenix.controls.JFXComboBox;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

public class ExpenditureController extends AccountControllers {

    @FXML
    private JFXComboBox<ExpenditureCategory> filterByCategoryCombobox;
    @FXML
    private TextField containsDescriptionField;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private TextField cashAmountField;
    @FXML
    private ComboBox<ExpenditureCategory> categoryCombobox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Label currencyText;
    @FXML
    private TextField descriptionField;
    @FXML
    private TableView<Expenditure> expenditureTableView;
    @FXML
    private TitledPane filterPane;
    private final TableColumn<Expenditure, ExpenditureCategory> categoryColumn = new TableColumn<>("Category");
    private final TableColumn<Expenditure, Expenditure> operationalColumn = new TableColumn<>("Operations");
    public static boolean filterCollapsed = false;
    private final ScheduledExecutorService updateTableView = Executors.newSingleThreadScheduledExecutor();

    public void initialize(){

        try(ExecutorService executor = Executors.newFixedThreadPool(3)){
            executor.execute(DatabaseRunnableFactory.readAllExpendituresFromAccount(currentLinkedBankAccount, expenditureTableView));
            executor.execute(DatabaseRunnableFactory.setExpenditureCategoryCombobox(categoryCombobox));
            executor.execute(DatabaseRunnableFactory.setExpenditureCategoryCombobox(filterByCategoryCombobox));
        }

        filterByCategoryCombobox.getItems().add(ExpenditureCategory.noValueCategory);
        filterByCategoryCombobox.getSelectionModel().select(ExpenditureCategory.noValueCategory);

        FXUtils.setTableCols(Transaction.MAPPED_FIELDS, expenditureTableView);

        currencyText.setText(currentLoginAccount.getCurrencyUsed());

        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("expenditureCategory"));

        categoryColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(ExpenditureCategory usedVal, boolean b) {

                super.updateItem(usedVal, b);

                if (Optional.ofNullable(usedVal).isEmpty() || b) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                setGraphic(FXUtils.returnComboboxColorWrapper(usedVal));

            }
        });

        expenditureTableView.getColumns().get(2).setPrefWidth(150);
        operationalColumn.setCellValueFactory(expenditureExpenditureCellDataFeatures -> new SimpleObjectProperty<>(expenditureExpenditureCellDataFeatures.getValue()));

        operationalColumn.setCellFactory(param -> new TableCell<>(){
            @Override
            protected void updateItem(Expenditure expenditure, boolean b) {

                super.updateItem(expenditure, b);

                if(Optional.ofNullable(expenditure).isEmpty() || b){
                    setGraphic(null);
                    setText(null);
                    return;
                }

                setGraphic(FXUtils.returnOperationalColumnGraphic(() -> returnDeleteButton(expenditure), () -> returnUpdateButton(expenditure)));
            }

        });

        expenditureTableView.getColumns().add(categoryColumn);
        expenditureTableView.getColumns().add(operationalColumn);

        filterPane.expandedProperty().set(!filterCollapsed);

        datePicker.setValue(LocalDate.now());

        FXUtils.setTextFieldFormatterToNumericOnly(cashAmountField);

        cashAmountField.setOnMousePressed(mouseEvent -> FXUtils.clearRedOfTextField(cashAmountField));

        updateTableView.scheduleAtFixedRate(DatabaseRunnableFactory.readAllExpendituresFromAccount(currentLinkedBankAccount, expenditureTableView, categoryCombobox.getSelectionModel().getSelectedItem(), containsDescriptionField.getText(), fromDatePicker.getValue(), toDatePicker.getValue()),
                    0, 10, TimeUnit.SECONDS);

    }

    public void setOnExpansionRemember(){
        filterCollapsed = !filterCollapsed;
    }

    public Button returnDeleteButton(Expenditure expenditure) {

        Button button = new Button("Delete");

        button.setOnAction(actionEvent -> {

            if(FXUtils.createAndWaitConfirmation()){
                try(ExecutorService executorService = Executors.newFixedThreadPool(3)){
                    executorService.execute(DatabaseRunnableFactory.deleteObjectByIDThread("EXPENDITURE", expenditure.getObjectID()));
                    executorService.execute(DatabaseRunnableFactory.readAllExpendituresFromAccount(currentLinkedBankAccount, expenditureTableView, filterByCategoryCombobox.getSelectionModel().getSelectedItem(), containsDescriptionField.getText(), fromDatePicker.getValue(), toDatePicker.getValue()));
                    executorService.execute(FileThreadFactory.serializeAccountChangeToFile(new AccountDeleteChange<>(currentLoginAccount, expenditure)));
                }
            }

        });

        return button;
    }


    public Button returnUpdateButton(Expenditure expenditure){

        Button button= new Button();

        button.setOnAction(actionEvent -> {

            FXUtils.startEditExpenditureDialog(expenditure, currentLoginAccount);
            refreshTableWithFilters();
        });

        return button;
    }


    public void parseInputIntoExpenditure(){

        try{
            FXUtils.makeTextFieldBordersRedOnRejection(cashAmountField);
        }catch(TextFieldEmptyException textFieldEmptyException){
            return;
        }

        BigDecimal amountInBigDecimal;

        try {
            amountInBigDecimal = FXUtils.handleBigDecimalConversionFromTextField(cashAmountField);
        } catch (UnsuportedConversionException e) {
            SessionLogger.sessionLogger.error("User input cannot be parsed to BigDecimal",  e);
            cashAmountField.clear();
            FXUtils.makeTextFieldBordersRedOnRejection(cashAmountField);
            return;
        }


        Expenditure createdExpenditure = new Expenditure.ExpenditureBuilder()
                                                        .withDescription(descriptionField.getText())
                                                        .withCategory(categoryCombobox.getSelectionModel().getSelectedItem())
                                                        .doneOnDate(datePicker.getValue())
                                                        .ofAmount(new CurrencyMask(amountInBigDecimal, CurrencyMask.defaultConversionValue))
                                                        .finishCreation();


        writeAndRefresh(createdExpenditure);
    }

    public void refreshTableWithFilters(){
        new Thread(DatabaseRunnableFactory.readAllExpendituresFromAccount(currentLinkedBankAccount, expenditureTableView, filterByCategoryCombobox.getSelectionModel().getSelectedItem(), containsDescriptionField.getText(), fromDatePicker.getValue(), toDatePicker.getValue())).start();
    }


    public void writeAndRefresh(Expenditure createdExpenditure){
        try(ExecutorService executor = Executors.newFixedThreadPool(2)){
            executor.execute(DatabaseRunnableFactory.writeExpenditureToDatabase(createdExpenditure, currentLinkedBankAccount));
            executor.execute(DatabaseRunnableFactory.readAllExpendituresFromAccount(currentLinkedBankAccount, expenditureTableView, filterByCategoryCombobox.getSelectionModel().getSelectedItem(), containsDescriptionField.getText(), fromDatePicker.getValue(), toDatePicker.getValue()));
            executor.execute(FileThreadFactory.serializeAccountChangeToFile(new AccountAddChange<>(currentLoginAccount, createdExpenditure)));
        }
    }


    public void clearFiltersAndSearch(){

        containsDescriptionField.clear();
        filterByCategoryCombobox.getSelectionModel().select(ExpenditureCategory.noValueCategory);
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        refreshTableWithFilters();

    }

    public void clearInsertionBox(){
        descriptionField.clear();
        datePicker.setValue(LocalDate.now());
        cashAmountField.clear();
    }

    @Override
    public void shutDownScheduledExecutors() {
        updateTableView.shutdownNow();
    }
}