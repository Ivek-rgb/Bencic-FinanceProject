package main.hr.production.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public abstract class Transaction extends NamedEntity implements Serializable {
    protected CurrencyMask transactionAmount;
    protected LocalDate dateOfTransaction;
    protected String transactionDescription;

    public static final Map<String, String> MAPPED_FIELDS = new TreeMap<>(Map.of("formattedDateOfTransaction", "Date", "transactionAmountFull", "Transaction size", "transactionDescription", "Description"));
    public static final Map<String, String> MAPPED_FOR_TRANSACTION = new TreeMap<>(Map.of("formattedDateOfTransaction", "Date", "transactionDescription", "Description"));

    public Transaction(Long objectID, String name) {
        super(objectID, name);
    }

    public Transaction(Long objectID, String name, CurrencyMask transactionAmount, LocalDate dateOfTransaction, String transactionDescription) {
        super(objectID, name);
        this.transactionAmount = transactionAmount;
        this.dateOfTransaction = dateOfTransaction;
        this.transactionDescription = transactionDescription;
    }

    public BigDecimal getTransactionAmount(){
        return transactionAmount.getChangedAmount();
    }

    public BigDecimal getTransactionAmountDefault(){
        return transactionAmount.getAmount();
    }

    public CurrencyMask getTransactionAmountCM(){
        return transactionAmount;
    }

    public String getTransactionAmountFull(){
        return transactionAmount.getTextualAmount();
    }

    public String getFormattedDateOfTransaction(){
        return dateOfTransaction.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    @Override
    public String toString() {
        return  "Date: "+ getFormattedDateOfTransaction() + "\nDescription: " + transactionDescription + "\n";
    }

    public LocalDate getDateOfTransaction() {
        return dateOfTransaction;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionAmount(CurrencyMask transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public void setDateOfTransaction(LocalDate dateOfTransaction) {
        this.dateOfTransaction = dateOfTransaction;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

}
