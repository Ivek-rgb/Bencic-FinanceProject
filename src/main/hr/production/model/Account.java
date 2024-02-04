package main.hr.production.model;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class Account extends IdentifiableObject implements Serializable {

    private String  name,
                    password,
                    currencyUsed = "EUR";
    private AccountType accountType = AccountType.ACCOUNT_REGULAR;
    public static Map<String, String> MAPPED_FIELDS = new TreeMap<>(Map.of("name", "Name", "currencyUsed", "Used Currency", "accountType", "Is Admin?"));

    public enum AccountType{
        ACCOUNT_REGULAR(0),
        ACCOUNT_ADMIN(1);

        final Integer power;
        AccountType(Integer power){
            this.power = power;
        }

        public static AccountType getAccountType(String isAdmin){
            return isAdmin.equals("YES") ? ACCOUNT_ADMIN : ACCOUNT_REGULAR;
        }

        @Override
        public String toString() {
            return (power > 0) ? "YES" : "NO";
        }
    }

    public static class AccountBuilder extends NamedEntity{

        private String password, currencyUsed;

        private AccountType accountType = AccountType.ACCOUNT_REGULAR;

        public AccountBuilder(String name){
            super(-1L, name);
        }

        public AccountBuilder withID(Long id){
            setObjectID(id);
            return this;
        }

        public AccountBuilder withPassword(String password){
            this.password = password;
            return this;
        }

        public AccountBuilder usingCurrency(String currencyUsed){
            this.currencyUsed = currencyUsed;
            return this;
        }

        public AccountBuilder ofAccountType(AccountType accountType){
            this.accountType = accountType;
            return this;
        }


        public Account finishBuildingAccount(){
            return new Account(getObjectID(), name, password, accountType, currencyUsed);
        }

    }

    public Account(Long accountId, String name, String password) {
        super(accountId);
        this.name = name;
        this.password = password;
    }

    // for file reading ofc only
    public Account(Long accountId, String name, String password, Integer power) {
        super(accountId);
        this.name = name;
        this.password = password;
        this.accountType = AccountType.values()[power];
    }

    public Account(Long objectID, String name, String password, AccountType accountType, String currencyUsed) {
        super(objectID);
        this.name = name;
        this.password = password;
        this.currencyUsed = currencyUsed;
        this.accountType = accountType;
    }

    public Account(String name, String password) {
        super(-1L);
        this.name = name;
        this.password = password;
    }

    public void switchRoles(){
        accountType = (accountType.equals(AccountType.ACCOUNT_ADMIN)) ? AccountType.ACCOUNT_REGULAR : AccountType.ACCOUNT_ADMIN;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getCurrencyUsed() { return currencyUsed; }

    public void setCurrencyUsed(String currencyUsed) {
        this.currencyUsed = currencyUsed;
    }

    @Override
    public String toString() {
        return " ACCOUNT: " + name + (accountType.equals(AccountType.ACCOUNT_ADMIN) ? " (admin)" : "");
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Account account)
            return account.getName().equals(name);
        return false;
    }

}
