package main.hr.production.app;

import main.hr.production.model.Account;
import main.hr.production.model.BankAccount;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class AccountControllers {

    protected static Account currentLoginAccount;
    protected static BankAccount currentLinkedBankAccount;
    protected static AccountControllers currentOpenController;

    public void setAccountFromInstance(Account account, BankAccount linkedBankAccount){
        currentLoginAccount = account;
        currentLinkedBankAccount = linkedBankAccount;
    }

    public static void setAccountFromClass(Account account, BankAccount linkedBankAccount){
        currentLoginAccount = account;
        currentLinkedBankAccount = linkedBankAccount;
    }

    public static Account getCurrentLoginAccount() {
        return currentLoginAccount;
    }

    public static BankAccount getCurrentLinkedBankAccount() {
        return currentLinkedBankAccount;
    }

    public abstract void shutDownScheduledExecutors();

    public static AccountControllers getCurrentOpenController() {
        return currentOpenController;
    }

    public static void setCurrentOpenController(AccountControllers currentOpenController) {
        AccountControllers.currentOpenController = currentOpenController;
    }
}
