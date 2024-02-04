package main.hr.production.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Income extends Transaction implements Serializable {

    public Income(Long objectID, String name) {
        super(objectID, name);
    }

    public Income(Long objectID, String name, CurrencyMask transactionAmount, LocalDate dateOfTransaction, String transactionDescription) {
        super(objectID, name, transactionAmount, dateOfTransaction, transactionDescription);
    }

    public static class IncomeBuilder extends Transaction{

        public IncomeBuilder(){
            super(-1L, "INCOME");
        }

        public Income.IncomeBuilder ofAmount(CurrencyMask amount){
            this.transactionAmount = new CurrencyMask(amount.getAmount());
            return this;
        }

        public Income.IncomeBuilder withDescription(String description){
            this.transactionDescription = description;
            return this;
        }

        public Income.IncomeBuilder doneOnDate(LocalDate dateOnDone){
            this.dateOfTransaction = dateOnDone;
            return this;
        }

        public Income.IncomeBuilder withID(Long id){
            setObjectID(id);
            return this;
        }

        public Income finishCreation(){
            return new Income(getObjectID(), getName(), transactionAmount, dateOfTransaction, transactionDescription);
        }

    }

    @Override
    public String toString() {
        return "INCOME:\n" +  super.toString() + "Amount: +" + getTransactionAmountFull() + "\nCategory: Income\n";
    }

}