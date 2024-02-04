package main.hr.production.model;

import java.io.Serializable;

public class AccountDeleteChange <T extends Serializable> extends Change<Account, T>{

    public AccountDeleteChange(Account entityThatCausedChange, T objectThatWasChanged) {
        super(entityThatCausedChange, objectThatWasChanged);
    }

    @Override
    public String toString() {
        return "DELETION" +  super.toString() + "REMOVED OBJECT:\n" + super.objectThatWasChanged.toString();
    }

}
