package main.hr.production.model;

import java.io.Serializable;

public class AccountUpdateChange <T extends Serializable> extends Change<Account, T> {
    String memorizedObjectString;

    public AccountUpdateChange(Account entityThatCausedChange, T objectThatWasChanged) {
        super(entityThatCausedChange, objectThatWasChanged);
        this.memorizedObjectString = objectThatWasChanged.toString();
    }

    @Override
    public String toString() {
        return "UPDATE" + super.toString() + "UPDATED OBJECT FROM\n" + memorizedObjectString + "\nTO\n" + objectThatWasChanged.toString();
    }

}
