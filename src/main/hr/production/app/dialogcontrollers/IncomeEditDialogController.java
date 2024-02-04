package main.hr.production.app.dialogcontrollers;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import main.hr.production.app.fxutils.FXUtils;
import main.hr.production.exceptions.checked.UnsuportedConversionException;
import main.hr.production.exceptions.unchecked.TextFieldEmptyException;
import main.hr.production.logback.SessionLogger;
import main.hr.production.model.Account;
import main.hr.production.model.AccountUpdateChange;
import main.hr.production.model.CurrencyMask;
import main.hr.production.model.Income;
import main.hr.production.threads.DatabaseRunnableFactory;
import main.hr.production.threads.FileThreadFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class IncomeEditDialogController extends ChangeDialogController<Income>{

    @FXML
    private TextField cashAmountField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField descriptionField;
    @FXML
    private Label currencyText;

    private Income selectedIncome;

    public void initialize(){
        FXUtils.setTextFieldFormatterToNumericOnly(cashAmountField);

        cashAmountField.setOnMousePressed(event -> {
            FXUtils.clearRedOfTextField(cashAmountField);
        });
    }

    @Override
    public void setSelected(Income income, Account currentLoginAccount){

        selectedIncome = income;
        descriptionField.setText(income.getTransactionDescription());
        this.currentLoginAccount = currentLoginAccount;

        datePicker.setValue(income.getDateOfTransaction());
        cashAmountField.setText(income.getTransactionAmount().toString().replace(".", ","));
        currencyText.setText(CurrencyMask.defaultConversionValue);

    }

    @Override
    public void applyChange() {

        try{
            FXUtils.makeTextFieldBordersRedOnRejection(cashAmountField);
        }catch (TextFieldEmptyException textFieldEmptyException){
            return;
        }

        AccountUpdateChange<Income> incomeAccountUpdateChange = new AccountUpdateChange<>(currentLoginAccount, selectedIncome);

        selectedIncome.setTransactionDescription(descriptionField.getText());
        try {
            BigDecimal amountOfCash = FXUtils.handleBigDecimalConversionFromTextField(cashAmountField);
            selectedIncome.setTransactionAmount(new CurrencyMask(amountOfCash, CurrencyMask.defaultConversionValue));
        } catch (UnsuportedConversionException e) {
            SessionLogger.sessionLogger.error("User input cannot be parsed to BigDecimal", e);
            cashAmountField.clear();
            FXUtils.makeTextFieldBordersRedOnRejection(cashAmountField);
            return;
        }

        selectedIncome.setDateOfTransaction(datePicker.getValue());

        Executor threadExecutor = Executors.newFixedThreadPool(2);
        threadExecutor.execute(DatabaseRunnableFactory.updateIncome(selectedIncome));
        threadExecutor.execute(FileThreadFactory.serializeAccountChangeToFile(incomeAccountUpdateChange));

    }

    @Override
    public void clear() {

        cashAmountField.clear();
        FXUtils.clearRedOfTextField(cashAmountField);
        descriptionField.clear();
        datePicker.setValue(LocalDate.now());

    }
}
