package main.hr.production.model;

import java.io.Serializable;

public class AccountAddChange <T extends Serializable> extends Change<Account, T>{

    public AccountAddChange(Account entityThatCausedChange, T objectThatWasChanged) {
        super(entityThatCausedChange, objectThatWasChanged);
    }

    @Override
    public String toString() {
        return "ADDITION" +  super.toString() + "ADDED OBJECT:\n" + super.objectThatWasChanged;
    }

}
