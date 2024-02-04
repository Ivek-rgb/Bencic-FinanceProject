package main.hr.production.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankAccount extends IdentifiableObject{
    protected Long accountID;
    protected LocalDateTime dateOfCreation;

    public BankAccount(Long accountID) {
        super(-1L);
        this.accountID = accountID;
        this.dateOfCreation = LocalDateTime.now();
    }

    public BankAccount(Long objectID, Long accountID, LocalDateTime dateOfCreation) {
        super(objectID);
        this.accountID = accountID;
        this.dateOfCreation = dateOfCreation;
    }

    public BankAccount(){
        super(-1L);
    }

    public Long getAccountID() {
        return accountID;
    }

    public void setAccountID(Long accountID) {
        this.accountID = accountID;
    }



    public LocalDateTime getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(LocalDateTime dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

}
