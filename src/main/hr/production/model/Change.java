package main.hr.production.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Change <T extends Serializable, U extends Serializable> implements Serializable, Comparable<Change<T, U>> {

    protected T entityThatCausedChange;
    protected U objectThatWasChanged;
    protected LocalDateTime dateTimeOfChange;

    public Change(T entityThatCausedChange, U objectThatWasChanged) {
        this.entityThatCausedChange = entityThatCausedChange;
        this.objectThatWasChanged = objectThatWasChanged;
        this.dateTimeOfChange = LocalDateTime.now();
    }

    public T getEntityThatCausedChange() {
        return entityThatCausedChange;
    }

    public void setEntityThatCausedChange(T entityThatCausedChange) {
        this.entityThatCausedChange = entityThatCausedChange;
    }

    public U getObjectThatWasChanged() {
        return objectThatWasChanged;
    }

    public void setObjectThatWasChanged(U objectThatWasChanged) {
        this.objectThatWasChanged = objectThatWasChanged;
    }

    public LocalDateTime getDateTimeOfChange() {
        return dateTimeOfChange;
    }

    public void setDateTimeOfChange(LocalDateTime dateTimeOfChange) {
        this.dateTimeOfChange = dateTimeOfChange;
    }

    @Override
    public String toString() {
        return " CHANGE - done by" + entityThatCausedChange.toString() + " on DATE " + dateTimeOfChange.format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + ", ";
    }

    @Override
    public int compareTo(Change<T, U> o) {
        return dateTimeOfChange.compareTo(o.dateTimeOfChange);
    }

}
