package main.hr.production.threads;

import main.hr.production.fileutils.AccountFileUtils;
import main.hr.production.fileutils.FileUtils;
import main.hr.production.logback.SessionLogger;
import main.hr.production.model.Account;
import main.hr.production.model.IdentifiableObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public non-sealed class AccountFileOperations extends AccountFileUtils implements FormattedFileOperations<Account>{

    protected final static Object MUTEX_OBJ = new Object();

    @Override
    public void writeObjsIntoFile(Account account) {
        synchronized (MUTEX_OBJ) {
            FileUtils.templateWriteObjToFormattedFile(ACCOUNT_FILE_PATH, List.of(account), AccountFileUtils::accountToWritableForm, true, NUMBER_OF_LINES_PER_ACCOUNT);
        }
    }

    public void truncateAndWriteCollectionOfAccounts(Collection<Account> collectionOfAccounts){
        synchronized (MUTEX_OBJ){
            try {
                Files.writeString(Path.of(ACCOUNT_FILE_PATH), "", StandardOpenOption.TRUNCATE_EXISTING);
                List<Account> listOfAccounts = collectionOfAccounts.stream().sorted(Comparator.comparing(IdentifiableObject::getObjectID)).toList();
                listOfAccounts.forEach(account -> FileUtils.templateWriteIdentifiedObjectsToFormattedFile(ACCOUNT_FILE_PATH, List.of(account), AccountFileUtils::alreadyIdentifiedAccountToWritableForm, true));
            } catch (IOException e) {
                SessionLogger.sessionLogger.error("Exception thrown at truncate operation on account file", e);
            }
        }
    }

    public void deleteAccountAndSaveToFile(Account account){
        List<Account> filteredAllAccountsList = readAllAccountsFromFile().stream().filter(filterAcc -> !filterAcc.equalsID(account.getObjectID())).toList();
        truncateAndWriteCollectionOfAccounts(filteredAllAccountsList);
    }

    @Override
    public List<Account> readObjsFromFile() {
        synchronized (MUTEX_OBJ){
            return FileUtils.templateReadObjFromFormattedFile(ACCOUNT_FILE_PATH, NUMBER_OF_LINES_PER_ACCOUNT, AccountFileUtils::readAccountFromFormattedSerializedForm);
        }
    }

    public List<String> readAllLinesFromAccountFile(){
        synchronized (MUTEX_OBJ){
            return readAllLinesFromFile(Path.of(ACCOUNT_FILE_PATH));
        }
    }

    public static List<Account> readAllAccountsFromFile(){
        synchronized (MUTEX_OBJ){
            return FileUtils.templateReadObjFromFormattedFile(ACCOUNT_FILE_PATH, NUMBER_OF_LINES_PER_ACCOUNT, AccountFileUtils::readAccountFromFormattedSerializedForm);
        }
    }

    public static Long returnLatestAddedId(){
        synchronized (MUTEX_OBJ){
            return AccountFileUtils.returnLatestIdentification(ACCOUNT_FILE_PATH, NUMBER_OF_LINES_PER_ACCOUNT);
        }
    }

    public void updateAccountFiles(List<Account> listOfAccounts){
        truncateAndWriteCollectionOfAccounts(listOfAccounts);
    }

    public void updateAccountFile(List<String> listOfLines){
        synchronized (MUTEX_OBJ){
            try {
                Files.writeString(Path.of(AccountFileUtils.ACCOUNT_FILE_PATH), String.join("\n", listOfLines) + "\n");
            } catch (IOException e) {
                SessionLogger.sessionLogger.error("Exception thrown while updating Accounts", e);
            }
        }
    }

    public static Map<String, Account> returnAccountHashedByName(){
        synchronized (MUTEX_OBJ){
            return readAllAccountsFromFile().stream().collect(Collectors.toMap(Account::getName, account -> account));
        }
    }

    public static Set<Account> returnAccountsCollectedAsSet(){
        synchronized (MUTEX_OBJ){
            return new HashSet<>(readAllAccountsFromFile());
        }
    }

}