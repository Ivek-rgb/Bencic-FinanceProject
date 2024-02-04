package main.hr.production.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Expenditure extends Transaction implements Serializable {

    ExpenditureCategory expenditureCategory;

    public static class ExpenditureBuilder extends Transaction{

        ExpenditureCategory expenditureCategory;

        public ExpenditureBuilder(){
            super(-1L, "expenditure");
            this.transactionDescription = "";
        }

        public ExpenditureBuilder withCategory(ExpenditureCategory expenditureCategory){
            this.expenditureCategory = expenditureCategory;
            return this;
        }

        public ExpenditureBuilder ofAmount(CurrencyMask amount){
            this.transactionAmount = new CurrencyMask(amount.getAmount());
            return this;
        }

        public ExpenditureBuilder withDescription(String description){
            this.transactionDescription = description;
            return this;
        }

        public ExpenditureBuilder doneOnDate(LocalDate dateOnDone){
            this.dateOfTransaction = dateOnDone;
            return this;
        }

        public ExpenditureBuilder withID(Long id){
            setObjectID(id);
            return this;
        }

        public Expenditure finishCreation(){
            return new Expenditure(getObjectID(), getName(), transactionAmount, dateOfTransaction, transactionDescription, expenditureCategory);
        }

    }

    public Expenditure(Long objectID, String name) {
        super(objectID, name);
    }

    public Expenditure(Long objectID, String name, ExpenditureCategory expenditureCategory) {
        super(objectID, name);
        this.expenditureCategory = expenditureCategory;
    }

    public Expenditure(Long objectID, String name, CurrencyMask transactionAmount, LocalDate dateOfTransaction, String transactionDescription, ExpenditureCategory expenditureCategory) {
        super(objectID, name, transactionAmount, dateOfTransaction, transactionDescription);
        this.expenditureCategory = expenditureCategory;
    }

    @Override
    public String toString() {
        return "EXPENSE:\n"  + super.toString() + "Amount: -" + getTransactionAmountFull() + "\nCategory: " + expenditureCategory.getName() + "\n";
    }

    public ExpenditureCategory getExpenditureCategory() {
        return expenditureCategory;
    }

    public void setExpenditureCategory(ExpenditureCategory expenditureCategory) {
        this.expenditureCategory = expenditureCategory;
    }
}
