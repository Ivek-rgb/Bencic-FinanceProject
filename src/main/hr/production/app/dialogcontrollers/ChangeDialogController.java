package main.hr.production.app.dialogcontrollers;

import main.hr.production.model.Account;

public abstract class ChangeDialogController<T> {

    protected Account currentLoginAccount;
    public abstract void applyChange();

    public abstract void clear();

    public abstract void setSelected(T selectedObj, Account currentLoginAccount);

}