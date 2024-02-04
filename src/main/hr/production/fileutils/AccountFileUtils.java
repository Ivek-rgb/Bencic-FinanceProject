package main.hr.production.fileutils;

import at.favre.lib.crypto.bcrypt.BCrypt;
import main.hr.production.model.Account;

import java.util.List;

public class AccountFileUtils extends FileUtils {

    public static final String ACCOUNT_FILE_PATH = "dat/accounts.txt";
    public static final Integer NUMBER_OF_LINES_PER_ACCOUNT = AccountFields.values().length;
    private static final int logRounds = 12;

    protected enum AccountFields{
        INDEX,
        NAME,
        HASHED_PASSWORD,
        ACCOUNT_POWER,
        CURRENCY_USED

    }

    public static String hashPassword(String password){
        return BCrypt.withDefaults().hashToString(logRounds, password.toCharArray());
    }

    protected static boolean verifyPassword(String inputPassword, String hashedPassword){
        return BCrypt.verifyer().verify(inputPassword.toCharArray(), hashedPassword).verified;
    }

    protected static String accountToWritableForm(Account account, boolean hashPassword){
        return String.join("\n", account.getName(), hashPassword ? hashPassword(account.getPassword()) : account.getPassword(), account.getAccountType().toString(), account.getCurrencyUsed()) + "\n";
    }

    protected static String accountToWritableForm(Account account){
        return accountToWritableForm(account, true);
    }

    protected static String alreadyIdentifiedAccountToWritableForm(Account account){
        return account.getObjectID() + "\n" + accountToWritableForm(account, false);
    }

    protected static Account readAccountFromFormattedSerializedForm(List<String> listOfData){

        Long id = Long.valueOf(listOfData.get(AccountFields.INDEX.ordinal()));
        String userName = listOfData.get(AccountFields.NAME.ordinal());
        String hashedPassword = listOfData.get(AccountFields.HASHED_PASSWORD.ordinal());
        Account.AccountType accountType = Account.AccountType.getAccountType(listOfData.get(AccountFields.ACCOUNT_POWER.ordinal()));
        String currencyUsed = listOfData.get(AccountFields.CURRENCY_USED.ordinal());

        return new Account.AccountBuilder(userName)
                .withID(id)
                .withPassword(hashedPassword)
                .usingCurrency(currencyUsed)
                .ofAccountType(accountType).finishBuildingAccount();

    }

}
