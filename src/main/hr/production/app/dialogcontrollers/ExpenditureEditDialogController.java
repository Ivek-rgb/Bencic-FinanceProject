package main.hr.production.app.dialogcontrollers;

import com.jfoenix.controls.JFXComboBox;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import main.hr.production.app.fxutils.FXUtils;
import main.hr.production.exceptions.checked.UnsuportedConversionException;
import main.hr.production.exceptions.unchecked.TextFieldEmptyException;
import main.hr.production.logback.SessionLogger;
import main.hr.production.model.*;
import main.hr.production.threads.DatabaseRunnableFactory;
import main.hr.production.threads.FileThreadFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenditureEditDialogController extends ChangeDialogController<Expenditure>{

    @FXML
    private TextField cashAmountField;
    @FXML
    private Label currencyText;
    @FXML
    private DatePicker datePicker;
    @FXML
    private JFXComboBox<ExpenditureCategory> categoryCombobox;
    @FXML
    private TextField descriptionField;
    private Expenditure selectedExpenditure;

    public void initialize(){
        FXUtils.setTextFieldFormatterToNumericOnly(cashAmountField);

        cashAmountField.setOnMousePressed(event -> {
            FXUtils.clearRedOfTextField(cashAmountField);
        });
    }

    @Override
    public void setSelected(Expenditure expenditure, Account currentLoginAccount){

        selectedExpenditure = expenditure;
        try(ExecutorService executor = Executors.newFixedThreadPool(2)){
            executor.execute(DatabaseRunnableFactory.setExpenditureCategoryCombobox(categoryCombobox, expenditure.getExpenditureCategory()));
        }
        descriptionField.setText(expenditure.getTransactionDescription());
        this.currentLoginAccount = currentLoginAccount;


        datePicker.setValue(expenditure.getDateOfTransaction());
        cashAmountField.setText(expenditure.getTransactionAmount().toString().replace(".", ","));
        currencyText.setText(CurrencyMask.defaultConversionValue);
    }

    @Override
    public void applyChange() {

        try{
            FXUtils.makeTextFieldBordersRedOnRejection(cashAmountField);
        }catch (TextFieldEmptyException textFieldEmptyException){
            return;
        }

        AccountUpdateChange<Expenditure> expenditureAccountUpdateChange = new AccountUpdateChange<>(currentLoginAccount, selectedExpenditure);

        selectedExpenditure.setTransactionDescription(descriptionField.getText());
        selectedExpenditure.setExpenditureCategory(categoryCombobox.getSelectionModel().getSelectedItem());
        try {
            BigDecimal amountOfCash = FXUtils.handleBigDecimalConversionFromTextField(cashAmountField);
            selectedExpenditure.setTransactionAmount(new CurrencyMask(amountOfCash, CurrencyMask.defaultConversionValue));
        } catch (UnsuportedConversionException e) {
            SessionLogger.sessionLogger.error("User input cannot be parsed to BigDecimal", e);
            cashAmountField.clear();
            FXUtils.makeTextFieldBordersRedOnRejection(cashAmountField);
            return;
        }
        selectedExpenditure.setDateOfTransaction(datePicker.getValue());

        try(ExecutorService threadExecutor = Executors.newFixedThreadPool(2)){
            threadExecutor.execute(DatabaseRunnableFactory.updateExpenditure(selectedExpenditure));
            threadExecutor.execute(FileThreadFactory.serializeAccountChangeToFile(expenditureAccountUpdateChange));
        }

    }

    @Override
    public void clear() {
        cashAmountField.clear();
        FXUtils.clearRedOfTextField(cashAmountField);
        descriptionField.clear();
        datePicker.setValue(LocalDate.now());
        categoryCombobox.getSelectionModel().select(0);
    }

}
