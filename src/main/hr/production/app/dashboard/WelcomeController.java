package main.hr.production.app.dashboard;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import main.hr.production.app.AccountControllers;
import main.hr.production.app.fxutils.FXUtils;
import main.hr.production.app.menubar.MenuBarController;
import main.hr.production.model.*;
import main.hr.production.threads.DatabaseSynchronizedOperations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WelcomeController extends AccountControllers {

    @FXML
    private Label welcomeText;
    @FXML
    private Label clockText;
    @FXML
    private Label totalBalance;
    @FXML
    private Label currencyName;
    @FXML
    private MenuBarController menuComponentController;
    @FXML
    private HBox fractionHbox;
    @FXML
    private VBox scrollBarVBox;
    @FXML
    private ScrollPane legendScrollPane;
    @FXML
    private TableView<Transaction> transactionTableView;
    @FXML
    private Label dateText;
    private final ScheduledExecutorService refreshDashboardScreen = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService refreshClockExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final TableColumn<Transaction, Transaction> amountColumnForTransactions = new TableColumn<>("Amount spent");
    private final TableColumn<Transaction, Transaction> columnForCategories = new TableColumn<>("Category");

    public static final double WRAPPER_WIDTH = 458.2;

    public void initialize(){

        refreshClockExecutorService.scheduleAtFixedRate(() -> {
            String[] timeAndDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm:ss")).split("-");
            Platform.runLater(() -> {
                clockText.setText(timeAndDate[1]);
                dateText.setText(timeAndDate[0]);
            });
        }, 0, 100, TimeUnit.MILLISECONDS);

        scrollBarVBox.setSpacing(10);
        legendScrollPane.setPadding(new Insets(10));

        FXUtils.setTableCols(Transaction.MAPPED_FOR_TRANSACTION, transactionTableView);

        columnForCategories.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, Transaction>, ObservableValue<Transaction>>() {
            @Override
            public ObservableValue<Transaction> call(TableColumn.CellDataFeatures<Transaction, Transaction> transactionTransactionCellDataFeatures) {
                return new SimpleObjectProperty<>(transactionTransactionCellDataFeatures.getValue());
            }
        });

        columnForCategories.setCellFactory(new Callback<TableColumn<Transaction, Transaction>, TableCell<Transaction, Transaction>>() {
            @Override
            public TableCell<Transaction, Transaction> call(TableColumn<Transaction, Transaction> transactionVoidTableColumn) {
                return new TableCell<>(){
                    @Override
                    protected void updateItem(Transaction transaction, boolean b) {
                        super.updateItem(transaction, b);

                        if (Optional.ofNullable(transaction).isEmpty() || b) {
                            setText(null);
                            setGraphic(null);
                            return;
                        }

                        HBox categoryGraphic;

                        if(transaction instanceof Expenditure expenditure)
                            categoryGraphic = FXUtils.returnComboboxColorWrapper(expenditure.getExpenditureCategory());
                        else
                            categoryGraphic = FXUtils.returnComboboxColorWrapper(ExpenditureCategory.incomeCategory);

                        setGraphic(categoryGraphic);

                    }
                };
            }
        });


        amountColumnForTransactions.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, Transaction>, ObservableValue<Transaction>>() {
            @Override
            public ObservableValue<Transaction> call(TableColumn.CellDataFeatures<Transaction, Transaction> transactionTransactionCellDataFeatures) {
                return new SimpleObjectProperty<>(transactionTransactionCellDataFeatures.getValue());
            }
        });

        amountColumnForTransactions.setCellFactory(new Callback<TableColumn<Transaction, Transaction>, TableCell<Transaction, Transaction>>() {
            @Override
            public TableCell<Transaction, Transaction> call(TableColumn<Transaction, Transaction> transactionTransactionTableColumn) {
                return new TableCell<>(){
                    @Override
                    protected void updateItem(Transaction transaction, boolean b) {

                        super.updateItem(transaction, b);

                        if (Optional.ofNullable(transaction).isEmpty() || b) {
                            setText(null);
                            setGraphic(null);
                            return;
                        }

                        Label amountLabel = new Label();

                        if(transaction instanceof Expenditure expenditure){
                            amountLabel.setText("-" + expenditure.getTransactionAmountFull());
                            amountLabel.setTextFill(Color.INDIANRED);
                        }else{
                            amountLabel.setText("+" + transaction.getTransactionAmountFull());
                            amountLabel.setTextFill(Color.GREEN);
                        }

                        setGraphic(amountLabel);
                    }
                };
            }
        });

        transactionTableView.getColumns().add(amountColumnForTransactions);
        transactionTableView.getColumns().add(columnForCategories);

        currencyName.setText(currentLoginAccount.getCurrencyUsed());

    }


    public void setWelcomeText(){
        welcomeText.setText("Welcome, " + currentLoginAccount.getName() + "!");
    }

    public void setTotalBalance(){

        refreshDashboardScreen.scheduleAtFixedRate(
                ()->{

                    List<Expenditure> listOfExpenditures = new DatabaseSynchronizedOperations().readAllExpendituresToBankAccount(currentLinkedBankAccount);
                    List<Income> listOfIncome = new DatabaseSynchronizedOperations().readALlIncomeToBankAccount(currentLinkedBankAccount);

                    Map<ExpenditureCategory, CurrencyMask> mappedExpenditures = new HashMap<>();

                    CurrencyMask totalAmount = new CurrencyMask(BigDecimal.ZERO);
                    CurrencyMask totalExpenditures = new CurrencyMask(BigDecimal.ZERO);

                    listOfExpenditures.forEach(expenditure -> {
                        totalAmount.subtractFromAmount(expenditure.getTransactionAmountCM());
                        totalExpenditures.addOntoAmount(expenditure.getTransactionAmountCM());
                        if(!mappedExpenditures.containsKey(expenditure.getExpenditureCategory()))
                            mappedExpenditures.put(expenditure.getExpenditureCategory(), new CurrencyMask(BigDecimal.ZERO));
                        mappedExpenditures.get(expenditure.getExpenditureCategory()).addOntoAmount(expenditure.getTransactionAmountCM());
                    });

                    List<Transaction> combinedList = new ArrayList<>(listOfExpenditures);
                    combinedList.addAll(listOfIncome);
                    combinedList.sort(Comparator.comparing(Transaction::getDateOfTransaction).reversed());

                    Platform.runLater(() ->{

                        listOfIncome.forEach(income ->{
                            totalAmount.addOntoAmount(income.getTransactionAmountCM());
                        });

                        setUpHBoxExpenditureView(fractionHbox, scrollBarVBox, mappedExpenditures, totalExpenditures);

                        totalBalance.setText(totalAmount.getChangedAmount().toString());

                        if(totalAmount.getChangedAmount().compareTo(BigDecimal.ZERO) < 0){
                            totalBalance.setTextFill(Color.RED);
                        }

                        transactionTableView.getItems().clear();
                        transactionTableView.setItems(FXCollections.observableArrayList(combinedList));

                    });

                },
                0,
                1,
                TimeUnit.SECONDS
        );


    }

    public static void setUpHBoxExpenditureView(HBox handleWrapper, VBox legendWrapper, Map<ExpenditureCategory, CurrencyMask> mapOfExpenditures, CurrencyMask totalExpenditureBalance){

        Set<Map.Entry<ExpenditureCategory, CurrencyMask>> setOfEntries = mapOfExpenditures.entrySet();

        Platform.runLater(() -> {

            handleWrapper.getChildren().clear();
            legendWrapper.getChildren().clear();

            setOfEntries.forEach(expenditureCategoryEntry -> {

                double ratio = expenditureCategoryEntry.getValue().getAmount().divide(totalExpenditureBalance.getAmount(), 6, RoundingMode.HALF_UP).doubleValue();

                handleWrapper.getChildren().add(getFractionRectangle(ratio, WRAPPER_WIDTH, expenditureCategoryEntry.getKey().getAppendedColorForReference()));

                HBox legendPart = new HBox();
                legendPart.setAlignment(Pos.CENTER_LEFT);

                Rectangle legendRectangle = new Rectangle(12, 12);
                legendRectangle.setFill(expenditureCategoryEntry.getKey().getAppendedColorForReference());

                Label informationText = new Label(expenditureCategoryEntry.getKey().getName() + " - " + expenditureCategoryEntry.getValue().getChangedAmount()+ " " + CurrencyMask.defaultConversionValue + " - " + (ratio * 100) + "%");
                legendPart.setSpacing(10);
                legendPart.getChildren().addAll(legendRectangle, informationText);

                legendWrapper.getChildren().add(legendPart);

            });

        });

    }

    public static Rectangle getFractionRectangle(double ratio, double widthOfWrapper, Color color) {

        Rectangle coloredRepresentation = new Rectangle(widthOfWrapper * ratio, 50);
        coloredRepresentation.setFill(color);

        return coloredRepresentation;
    }

    @Override
    public void shutDownScheduledExecutors() {
        refreshDashboardScreen.shutdownNow();
        refreshClockExecutorService.shutdownNow();
    }
}