package main.hr.production.threads;

import javafx.scene.control.TextField;
import main.hr.production.app.fxutils.FXUtils;
import main.hr.production.database.DatabaseUtils;
import main.hr.production.exceptions.unchecked.ExpenditureCategoryDuplicateException;
import main.hr.production.model.BankAccount;
import main.hr.production.model.Expenditure;
import main.hr.production.model.ExpenditureCategory;
import main.hr.production.model.Income;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class DatabaseSynchronizedOperations {

    protected static final Object MUTEX_OBJECT = new Object();

    public void createBankAccount(BankAccount bankAccountToWrite){
        synchronized (MUTEX_OBJECT){
            DatabaseUtils.writeBankAccountToDatabase(bankAccountToWrite);
        }
    }

    public List<ExpenditureCategory> readALlExpenditureCategories(){
        synchronized (MUTEX_OBJECT){
            return DatabaseUtils.readAllExpenditureCategories();
        }
    }

    public void writeExpenditureToDatabase(Expenditure madeExpense, BankAccount linkedBankAccount){
        synchronized (MUTEX_OBJECT){
            DatabaseUtils.writeExpenditureIntoDatabase(madeExpense, linkedBankAccount);
        }
    }

    public Optional<BankAccount> searchBankAccountByAccountID(Long accountID){
        synchronized (MUTEX_OBJECT){
            return DatabaseUtils.searchBankAccountByAccountID(accountID);
        }
    }

    public void deleteObjectByID(String tableName, Long ObjectID){
        synchronized (MUTEX_OBJECT){
            DatabaseUtils.deleteObjectByID(tableName, ObjectID);
        }
    }

    public List<Expenditure> readAllExpendituresToBankAccount(BankAccount linkedBankAccount){
        synchronized (MUTEX_OBJECT){
            return DatabaseUtils.readAllExpendituresToBankAccount(linkedBankAccount);
        }
    }

    public List<Expenditure> readAllExpendituresToBankAccountFiltered(BankAccount linkedBankAccount, ExpenditureCategory expenditureCategory, String descriptionHasToHave, LocalDate from, LocalDate to){
        synchronized (MUTEX_OBJECT){
            return DatabaseUtils.readAllExpendituresWithFilters(linkedBankAccount, expenditureCategory, descriptionHasToHave, from, to);
        }
    }

    public List<Income> readAllIncomeToBankAccountFiltered(BankAccount linkedBankAccount, String descriptionHasToHave, LocalDate from, LocalDate to){
        synchronized (MUTEX_OBJECT){
            return DatabaseUtils.readAllIncomeWithFilters(linkedBankAccount, descriptionHasToHave, from, to);
        }
    }

    public List<Income> readALlIncomeToBankAccount(BankAccount linkedBankAccount){
        synchronized (MUTEX_OBJECT){
            return DatabaseUtils.readAllIncomesToBankAccount(linkedBankAccount);
        }
    }

    public void writeIncomeToDataBase(Income income, BankAccount linkedBankAccount){
        synchronized (MUTEX_OBJECT){
            DatabaseUtils.writeIncomeIntoDatabase(income, linkedBankAccount);
        }
    }

    public void updateExpenditure(Expenditure expenditure){
        synchronized (MUTEX_OBJECT){
            DatabaseUtils.updateExpenditure(expenditure);
        }
    }

    public void deleteAllTracesOfAccount(Long accountID){
        synchronized (MUTEX_OBJECT){

            Optional<BankAccount> linkedBankAccount = DatabaseUtils.searchBankAccountByAccountID(accountID);

            linkedBankAccount.ifPresent(bankAccount ->{
                DatabaseUtils.clearInTableAllWhereNumericalIDEquals("INCOME", "ID_BANK", bankAccount.getObjectID());
                DatabaseUtils.clearInTableAllWhereNumericalIDEquals("EXPENDITURE", "ID_BANK", bankAccount.getObjectID());
                DatabaseUtils.deleteObjectByID("ACCOUNT_BANK", bankAccount.getObjectID());
            });

        }
    }

    public void updateIncome(Income income){
        synchronized (MUTEX_OBJECT){
            DatabaseUtils.updateIncome(income);
        }
    }

    public void writeExpenditureCategoryToDatabase(ExpenditureCategory expenditureCategory){
        synchronized (MUTEX_OBJECT){
            DatabaseUtils.writeExpenditureCategoryToDatabase(expenditureCategory);
        }
    }

    public void checkForAlreadyExistingExpenditureCategory(ExpenditureCategory expenditureCategory, TextField redOnContainsTextField) throws ExpenditureCategoryDuplicateException {

        if(new HashSet<>(readALlExpenditureCategories()).contains(expenditureCategory)) {

            redOnContainsTextField.clear();
            redOnContainsTextField.setPromptText("Collection already contains that name");
            FXUtils.makeTextFieldBordersRedOnRejection(redOnContainsTextField);

            throw new ExpenditureCategoryDuplicateException();
        }

    }

    public void clearAllInTableWhereID(String tableName, String colName, Long objectID){
        synchronized (MUTEX_OBJECT){
            DatabaseUtils.clearInTableAllWhereNumericalIDEquals(tableName, colName, objectID);
        }
    }


}
