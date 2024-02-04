package main.hr.production.database;

import javafx.scene.paint.Color;
import main.hr.production.logback.SessionLogger;
import main.hr.production.model.*;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class DatabaseUtils {

    public static final String DATABASE_PROPERTIES = "properties/db.properties";

    public synchronized static Connection startConnection() throws SQLException {

        Properties dbProps = new Properties();
        try {
            dbProps.load(new FileReader(DATABASE_PROPERTIES));
        } catch (IOException e) {
            SessionLogger.sessionLogger.error("Error reading from database properties file", e);
        }

        String databaseUrl = dbProps.getProperty("databaseUrl");
        String userName = dbProps.getProperty("userName");
        String password = dbProps.getProperty("password");

        return DriverManager.getConnection(databaseUrl, userName, password);
    }

    public synchronized static void closeConnection(Connection connection) throws SQLException{
       connection.close();
    }

    public static <T> Optional<T> fetchObjectById(String tableName, long id, Function<ResultSet, Optional<T>> resultFactory){

        try(Connection connection = startConnection()){
            return resultFactory.apply(connection.createStatement().executeQuery("SELECT * FROM " + tableName + " WHERE ID=" + id));
        } catch (SQLException e) {
            SessionLogger.sessionLogger.error(e.getMessage());
            return Optional.empty();
        }
    }

    public static StringBuilder queryAppendContains(StringBuilder query, String column, String valueToResemble){

        if(!query.isEmpty()) query.append(" AND ");
        else query.append(" WHERE ");
        query.append(column).append(" LIKE '%").append(valueToResemble).append("%'");
        return query;

    }

    public static StringBuilder queryAppendEquals(StringBuilder query, String column, String valueToEqual){

        valueToEqual = "'" + valueToEqual + "'";
        if(!query.isEmpty()) query.append(" AND ");
        else query.append(" WHERE ");
        query.append(column).append("=").append(valueToEqual);
        return query;

    }

    private static <T> List<T>  databaseReadWithFilter(String table, Function<ResultSet, Optional<T>> resultFactory, String customQuery){

        try(Connection dbConnection = startConnection();
            Statement statement = dbConnection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM " + table + customQuery)){

            List<T> listOfObjects = new ArrayList<>();

            while(rs.next())
                resultFactory.apply(rs).ifPresent(listOfObjects::add);

            return listOfObjects;
        } catch (SQLException e) {
            SessionLogger.sessionLogger.error("Failed to read from database with filter", e);
            return new ArrayList<>();
        }

    }

    private static <T> List<T> databaseReadOrderedBy(String table, Function<ResultSet, Optional<T>> resultFactory, String columnName, boolean ascending) throws SQLException, IOException {

        String order = (ascending) ? " ASC " : " DESC ";
        return databaseReadWithFilter(table, resultFactory, " ORDER BY " + columnName + order);

    }

    private static <T> Optional<T> readObjWithExtremeProperty(String table, String colName, Function<ResultSet, T> resultFactory, boolean max) throws SQLException, IOException {

        String streamDirection = max ? "DESC" : "ASC";
        try (Connection connection = startConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + table + " ORDER BY " + colName + " " + streamDirection + " LIMIT 1")
        ) {
            if (resultSet.next())
                return Optional.of(resultFactory.apply(resultSet));
            return Optional.empty();
        }

    }

    private static <T> List<T> universalDatabaseRead(String table, Function<ResultSet, Optional<T>> resultFactory){

        return databaseReadWithFilter(table, resultFactory, "");

    }

    public static void writeBankAccountToDatabase(BankAccount bankAccount){


        try (Connection connection = startConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO ACCOUNT_BANK (ACCOUNT_ID, DATE_OF_CREATION) VALUES (?, ?)")
        ) {
            preparedStatement.setLong(1, bankAccount.getAccountID());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(bankAccount.getDateOfCreation()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            SessionLogger.sessionLogger.error("Failed to write bank account to database", e);
        }

    }

    public static Optional<BankAccount> readOneBankAccount(ResultSet resultSet){

        try {

            Long    accountId = resultSet.getLong("ACCOUNT_ID"),
                    bankAccId = resultSet.getLong("ID");

            LocalDateTime dateOfCreation = resultSet.getTimestamp("DATE_OF_CREATION").toLocalDateTime();

            return Optional.of(new BankAccount(bankAccId, accountId, dateOfCreation));

        } catch (SQLException e) {

            SessionLogger.sessionLogger.error("Failed to read one bank account", e);
            return Optional.empty();

        }
    }

    public static Optional<BankAccount> searchBankAccountByAccountID(Long accountID){
        try (Connection connection = startConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT_BANK WHERE ACCOUNT_ID=?")
        ) {

            preparedStatement.setLong(1, accountID);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return readOneBankAccount(preparedStatement.getResultSet());

        } catch (SQLException e) {
            SessionLogger.sessionLogger.error("Failed search for bank account with specified ID", e);
            return Optional.empty();
        }
    }

    public static void deleteObjectByID(String tableName, Long objectID){
        try (Connection connection = startConnection();
             PreparedStatement preparedDeleteStatement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE ID=?")
        ) {
            preparedDeleteStatement.setLong(1, objectID);
            preparedDeleteStatement.executeUpdate();
        } catch (SQLException e) {
            SessionLogger.sessionLogger.error("Failed to delete wanted object", e);
        }
    }


    protected static Optional<ExpenditureCategory> readOneExpenditureCategory(ResultSet resultSet){

        try {

            if (resultSet.isBeforeFirst())
                resultSet.next();

            Long id = resultSet.getLong("id");

            String name = resultSet.getString("name");
            Color representationColor = Color.valueOf(resultSet.getString("representing_color"));

            return Optional.of(new ExpenditureCategory(id, name, representationColor));

        } catch (SQLException e) {
            SessionLogger.sessionLogger.error("Failed to read one expenditure category", e);
            return Optional.empty();
        }

    }

    protected static Optional<Expenditure> readOneExpenditure(ResultSet resultSet){

        try {

            if (resultSet.isBeforeFirst())
                resultSet.next();

            Long id = resultSet.getLong("ID");
            long categoryID = resultSet.getLong("ID_CATEGORY");

            ExpenditureCategory expenditureCategory = fetchObjectById("EXPENDITURE_CATEGORY", categoryID, DatabaseUtils::readOneExpenditureCategory).orElse(ExpenditureCategory.noValueCategory);
            CurrencyMask amountOfMoney = new CurrencyMask(resultSet.getBigDecimal("AMOUNT"));
            LocalDate dateOfTransaction = resultSet.getDate("DATE_OF_TRANSACTION").toLocalDate();
            String description = resultSet.getString("DESCRIPTION");

            return Optional.ofNullable(new Expenditure.ExpenditureBuilder().doneOnDate(dateOfTransaction)
                    .withID(id)
                    .withDescription(description)
                    .withCategory(expenditureCategory)
                    .ofAmount(amountOfMoney)
                    .finishCreation());

        } catch (SQLException e) {
            SessionLogger.sessionLogger.error("Failed to read one expenditure", e);
            return Optional.empty();
        }

    }

    protected static Optional<Income> readOneIncome(ResultSet resultSet){
        try {

            if (resultSet.isBeforeFirst())
                resultSet.next();

            Long id = resultSet.getLong("ID");

            CurrencyMask amountOfMoney = new CurrencyMask(resultSet.getBigDecimal("AMOUNT"));
            LocalDate dateOfTransaction = resultSet.getDate("DATE_OF_TRANSACTION").toLocalDate();
            String description = resultSet.getString("DESCRIPTION");

            return Optional.of(new Income.IncomeBuilder().ofAmount(amountOfMoney)
                    .doneOnDate(dateOfTransaction)
                    .withDescription(description)
                    .withID(id).finishCreation());

        } catch (SQLException e) {
            SessionLogger.sessionLogger.error("Failed to read one income", e);
            return Optional.empty();
        }
    }

    public static void writeExpenditureIntoDatabase(Expenditure madeExpense, BankAccount currentLinkedBankAccount){
        try(Connection connection = startConnection();
            PreparedStatement writeStatement = connection.prepareStatement("INSERT INTO EXPENDITURE (" +
                    "id_category, id_bank, name, description, amount, date_of_transaction) VALUES (?, ?, ?, ?, ?, ?)")
        ){

            writeStatement.setLong(1, madeExpense.getExpenditureCategory().getObjectID().intValue());
            writeStatement.setLong(2, currentLinkedBankAccount.getObjectID());
            writeStatement.setString(3, "EXPENSE");
            writeStatement.setString(4, madeExpense.getTransactionDescription());
            writeStatement.setDouble(5, madeExpense.getTransactionAmountDefault().doubleValue());
            writeStatement.setDate(6, Date.valueOf(madeExpense.getDateOfTransaction()));

            writeStatement.executeUpdate();
        } catch (SQLException e) {
            SessionLogger.sessionLogger.error("Failed to write expenditure to database", e);
        }
    }

    public static void writeIncomeIntoDatabase(Income income, BankAccount currentLinkedBankAccount){
        try(Connection connection = startConnection();
            PreparedStatement writeStatement = connection.prepareStatement("INSERT INTO INCOME (" +
                    "id_bank, name, description, amount, date_of_transaction) VALUES (?, ?, ?, ?, ?)")
        ){

            writeStatement.setLong(1, currentLinkedBankAccount.getObjectID());
            writeStatement.setString(2, "INCOME");
            writeStatement.setString(3, income.getTransactionDescription());
            writeStatement.setDouble(4, income.getTransactionAmountDefault().doubleValue());
            writeStatement.setDate(5, Date.valueOf(income.getDateOfTransaction()));
            writeStatement.executeUpdate();

        } catch (SQLException e) {
            SessionLogger.sessionLogger.error("Failed to write income to database", e);
        }
    }

    public static List<ExpenditureCategory> readAllExpenditureCategories(){
        return universalDatabaseRead("EXPENDITURE_CATEGORY", DatabaseUtils::readOneExpenditureCategory);
    }

    public static List<Expenditure> readAllExpendituresToBankAccount(BankAccount currentLinkedBankAccount){
        return databaseReadWithFilter("EXPENDITURE", DatabaseUtils::readOneExpenditure, " WHERE ID_BANK=" + currentLinkedBankAccount.getObjectID());
    }

    public static List<Income> readAllIncomesToBankAccount(BankAccount currentLinkedBankAccount){
        return databaseReadWithFilter("INCOME", DatabaseUtils::readOneIncome, " WHERE ID_BANK=" + currentLinkedBankAccount.getObjectID());
    }

    public static List<Expenditure> readAllExpendituresWithFilters(BankAccount currentLinkedBankAccount, ExpenditureCategory categorySelected, String textContained, LocalDate fromDate, LocalDate toDate){

        StringBuilder customEditedQuery = new StringBuilder(" WHERE ID_BANK=" + currentLinkedBankAccount.getObjectID());

        if(categorySelected.getObjectID() != -1)
            customEditedQuery.append(" AND ID_CATEGORY=").append(categorySelected.getObjectID());

        if(!textContained.isEmpty())
            customEditedQuery.append(" AND DESCRIPTION LIKE '%").append(textContained).append("%'");

        Stream<Expenditure> streamOfExpenditures = databaseReadWithFilter("EXPENDITURE", DatabaseUtils::readOneExpenditure, customEditedQuery.toString()).stream();

        return returnFilterByDate(streamOfExpenditures, fromDate, toDate);

    }

    public static <T extends Transaction>  List<T>  returnFilterByDate(Stream<T> streamOfTransactions, LocalDate fromDate, LocalDate toDate){

        if(fromDate != null)
            streamOfTransactions = streamOfTransactions.filter(expenditure -> expenditure.getDateOfTransaction().isAfter(fromDate.minusDays(1)));

        if(toDate != null)
            streamOfTransactions = streamOfTransactions.filter(expenditure -> expenditure.getDateOfTransaction().isBefore(toDate));

        return streamOfTransactions.toList();
    }

    public static List<Income> readAllIncomeWithFilters(BankAccount currentLinkedBankAccount, String textContained, LocalDate fromDate, LocalDate toDate){

        StringBuilder customEditedQuery = new StringBuilder(" WHERE ID_BANK=" + currentLinkedBankAccount.getObjectID());

        if(!textContained.isEmpty())
            customEditedQuery.append(" AND DESCRIPTION LIKE '%").append(textContained).append("%'");

        Stream<Income> streamOfIncome = databaseReadWithFilter("INCOME", DatabaseUtils::readOneIncome, customEditedQuery.toString()).stream();

        return returnFilterByDate(streamOfIncome, fromDate, toDate);

    }

    public static void updateExpenditure(Expenditure expenditure){
        try (Connection connection = startConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE EXPENDITURE " +
                     "SET DESCRIPTION=?, AMOUNT=?, DATE_OF_TRANSACTION=?, ID_CATEGORY=? WHERE ID=?")
        ) {

            preparedStatement.setString(1, expenditure.getTransactionDescription());
            preparedStatement.setBigDecimal(2, expenditure.getTransactionAmountCM().getAmount());
            preparedStatement.setDate(3, Date.valueOf(expenditure.getDateOfTransaction()));
            preparedStatement.setLong(4, expenditure.getExpenditureCategory().getObjectID());
            preparedStatement.setLong(5, expenditure.getObjectID());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            SessionLogger.logExceptionOnCurrentAccount(e);
        }
    }

    public static void updateIncome(Income income){
        try (Connection connection = startConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE INCOME " +
                     "SET DESCRIPTION=?, AMOUNT=?, DATE_OF_TRANSACTION=? WHERE ID=?")
        ) {

            preparedStatement.setString(1, income.getTransactionDescription());
            preparedStatement.setBigDecimal(2, income.getTransactionAmountCM().getAmount());
            preparedStatement.setDate(3, Date.valueOf(income.getDateOfTransaction()));
            preparedStatement.setLong(4, income.getObjectID());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            SessionLogger.logExceptionOnCurrentAccount(e);
        }
    }

    public static void clearInTableAllWhereNumericalIDEquals(String table, String col, Long identificationResult){
        try (Connection connection = startConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + table + " WHERE "  + col + "=?")
        ) {
            preparedStatement.setLong(1, identificationResult);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            SessionLogger.sessionLogger.error("Failed to completely delete wanted object", e);
        }
    }

    public static void writeExpenditureCategoryToDatabase(ExpenditureCategory expenditureCategory){
        try (Connection connection = startConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO EXPENDITURE_CATEGORY (" +
                     "NAME, REPRESENTING_COLOR) VALUES (?, ?)")
        ) {
            preparedStatement.setString(1, expenditureCategory.getName());
            preparedStatement.setString(2, expenditureCategory.getAppendedColorInHexString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            SessionLogger.sessionLogger.error("Failed to write expenditure category to database", e);
        }
    }

}