package main.hr.production.app.administrativepanel;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import main.hr.production.app.AccountControllers;
import main.hr.production.app.fxutils.FXUtils;
import main.hr.production.exceptions.unchecked.TextFieldEmptyException;
import main.hr.production.model.*;
import main.hr.production.threads.AccountFileOperations;
import main.hr.production.threads.DatabaseRunnableFactory;
import main.hr.production.threads.FileThreadFactory;

import java.util.Optional;
import java.util.concurrent.*;

public class AdministrativePanelController extends AccountControllers {

    @FXML
    private TextField categoryName;
    @FXML
    private ColorPicker categoryColorPicker;
    @FXML
    private TableView<Account> accountTableView;
    @FXML
    private TableView<ExpenditureCategory> expenditureCategoryTableView;
    @FXML
    private ListView<Change> listOfRecordedChanges;

    private final TableColumn<Account, Account> operationalColumn = new TableColumn<>("Operations");

    private final TableColumn<ExpenditureCategory, ExpenditureCategory> colorColumn = new TableColumn<>("Color");

    private final TableColumn<ExpenditureCategory, ExpenditureCategory> operationalCategoryColumn = new TableColumn<>("Operations");

    private final ScheduledExecutorService refreshRecordedChanges = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService refreshAccountList = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService refreshCategoryExpenditure = Executors.newSingleThreadScheduledExecutor();


    public void initialize(){

        FXUtils.setTableCols(Account.MAPPED_FIELDS, accountTableView);
        accountTableView.setItems(FXCollections.observableArrayList(AccountFileOperations.readAllAccountsFromFile()));

        FXUtils.setTableCols(ExpenditureCategory.MAPPED_FIELDS, expenditureCategoryTableView);


        operationalColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Account, Account>, ObservableValue<Account>>() {
            @Override
            public ObservableValue<Account> call(TableColumn.CellDataFeatures<Account, Account> accountAccountCellDataFeatures) {
                return new SimpleObjectProperty<>(accountAccountCellDataFeatures.getValue());
            }

        });

        operationalCategoryColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ExpenditureCategory, ExpenditureCategory>, ObservableValue<ExpenditureCategory>>() {
            @Override
            public ObservableValue<ExpenditureCategory> call(TableColumn.CellDataFeatures<ExpenditureCategory, ExpenditureCategory> expenditureCategoryExpenditureCategoryCellDataFeatures) {
                return new SimpleObjectProperty<>(expenditureCategoryExpenditureCategoryCellDataFeatures.getValue());
            }
        });

        operationalCategoryColumn.setCellFactory(new Callback<TableColumn<ExpenditureCategory, ExpenditureCategory>, TableCell<ExpenditureCategory, ExpenditureCategory>>() {
            @Override
            public TableCell<ExpenditureCategory, ExpenditureCategory> call(TableColumn<ExpenditureCategory, ExpenditureCategory> expenditureCategoryExpenditureCategoryTableColumn) {
                return new TableCell<>(){
                    @Override
                    protected void updateItem(ExpenditureCategory expenditureCategory, boolean b) {
                        super.updateItem(expenditureCategory, b);

                        if(Optional.ofNullable(expenditureCategory).isEmpty() || b){
                            setGraphic(null);
                            setText(null);
                            return;
                        }

                        setGraphic(FXUtils.returnOperationalColumnGraphic(() -> returnCategoryDeleteButton(expenditureCategory)));
                    }
                };
            }
        });


        operationalColumn.setCellFactory(new Callback<TableColumn<Account, Account>, TableCell<Account, Account>>() {
            @Override
            public TableCell<Account, Account> call(TableColumn<Account, Account> accountAccountTableColumn) {
                return new TableCell<>(){
                    @Override
                    protected void updateItem(Account account, boolean b) {

                        super.updateItem(account, b);

                        if(Optional.ofNullable(account).isEmpty() || b){
                            setGraphic(null);
                            setText(null);
                            return;
                        }

                        if(account.equalsID(currentLoginAccount.getObjectID())){
                            setText("[CURRENT USER]");
                            setGraphic(null);
                            return;
                        }

                        setGraphic(FXUtils.returnOperationalColumnGraphic(() -> returnDeleteButton(account), () -> returnUpdateButton(account)));

                    }
                };
            }
        });

        colorColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ExpenditureCategory, ExpenditureCategory>, ObservableValue<ExpenditureCategory>>() {
            @Override
            public ObservableValue<ExpenditureCategory> call(TableColumn.CellDataFeatures<ExpenditureCategory, ExpenditureCategory> expenditureCategoryExpenditureCategoryCellDataFeatures) {
                return new SimpleObjectProperty<>(expenditureCategoryExpenditureCategoryCellDataFeatures.getValue());
            }
        });

        colorColumn.setCellFactory(new Callback<TableColumn<ExpenditureCategory, ExpenditureCategory>, TableCell<ExpenditureCategory, ExpenditureCategory>>() {
            @Override
            public TableCell<ExpenditureCategory, ExpenditureCategory> call(TableColumn<ExpenditureCategory, ExpenditureCategory> expenditureCategoryExpenditureCategoryTableColumn) {
                return new TableCell<>(){
                    @Override
                    protected void updateItem(ExpenditureCategory expenditureCategory, boolean b) {
                        super.updateItem(expenditureCategory, b);

                        if(Optional.ofNullable(expenditureCategory).isEmpty() || b){
                            setGraphic(null);
                            setText(null);
                            return;
                        }

                        HBox mainWrapper = new HBox();
                        mainWrapper.setAlignment(Pos.CENTER_LEFT);
                        mainWrapper.setSpacing(10);

                        Rectangle colorRectangle = new Rectangle(12, 12);
                        colorRectangle.setFill(expenditureCategory.getAppendedColorForReference());
                        Label colorStringVal = new Label(expenditureCategory.getAppendedColorForReference().toString());

                        mainWrapper.getChildren().addAll(colorRectangle, colorStringVal);

                        setGraphic(mainWrapper);

                    }
                };
            }
        });

        expenditureCategoryTableView.getColumns().add(colorColumn);
        expenditureCategoryTableView.getColumns().add(operationalCategoryColumn);
        accountTableView.getColumns().add(operationalColumn);
        expenditureCategoryTableView.getColumns().get(2).setPrefWidth(30);

        new Thread(DatabaseRunnableFactory.setTableViewForExpenditureCategory(expenditureCategoryTableView)).start();

        categoryName.setOnMousePressed(event ->{
            FXUtils.clearRedOfTextField(categoryName);
            categoryName.setPromptText("Enter unique name");
        });

        listOfRecordedChanges.setCellFactory(new Callback<ListView<Change>, ListCell<Change>>() {
            @Override
            public ListCell<Change> call(ListView<Change> changeListView) {
                return new ListCell<>(){
                    @Override
                    protected void updateItem(Change change, boolean b) {
                        super.updateItem(change, b);

                        if(Optional.ofNullable(change).isEmpty() || b){
                            setText(null);
                            return;
                        }

                        setPadding(new Insets(5));

                        setText(change.toString());
                    }
                };
            }
        });



        refreshRecordedChanges.scheduleAtFixedRate(FileThreadFactory.refreshAndReadChanges(listOfRecordedChanges), 0, 5, TimeUnit.SECONDS);
        refreshAccountList.scheduleAtFixedRate(FileThreadFactory.refreshAccountTable(accountTableView), 0, 5, TimeUnit.SECONDS);
        refreshCategoryExpenditure.execute(DatabaseRunnableFactory.setTableViewForExpenditureCategory(expenditureCategoryTableView));

    }

    public Button returnUpdateButton(Account account){

        Button button = new Button();

        button.setOnAction(event -> {

            AccountUpdateChange<Account> accountUpdateChange = new AccountUpdateChange<>(currentLoginAccount, account);

            Executor accountUpdateExecutor = Executors.newFixedThreadPool(2);
            accountUpdateExecutor.execute(FileThreadFactory.promoteAccountToAdmin(account, accountTableView));
            accountUpdateExecutor.execute(FileThreadFactory.serializeAccountChangeToFile(accountUpdateChange));

        });

        return button;
    }

    public Button returnDeleteButton(Account account){

        Button button = new Button();

        button.setOnAction(event -> {
            if(!FXUtils.createAndWaitConfirmation())
                return;

            Executor buttonThreadsExecutor = Executors.newFixedThreadPool(3);

            buttonThreadsExecutor.execute(FileThreadFactory.deleteAccountAndSaveToFile(account, accountTableView));
            buttonThreadsExecutor.execute(DatabaseRunnableFactory.deleteAllTracesOfAccount(account.getObjectID()));
            buttonThreadsExecutor.execute(FileThreadFactory.serializeAccountChangeToFile(new AccountDeleteChange<>(currentLoginAccount, account)));
        });

        return button;

    }


    public Button returnCategoryDeleteButton(ExpenditureCategory expenditureCategory){

        Button button = new Button();

        button.setOnAction(event ->{

            if(!FXUtils.createAndWaitConfirmation())
                return;

            Executor buttonThreadsExecutor = Executors.newFixedThreadPool(2);
            buttonThreadsExecutor.execute(DatabaseRunnableFactory.deleteAndRefreshExpenditureCategoryTableView(expenditureCategory, expenditureCategoryTableView));
            buttonThreadsExecutor.execute(FileThreadFactory.serializeAccountChangeToFile(new AccountDeleteChange<>(currentLoginAccount, expenditureCategory)));

        });

        return button;
    }

    public void writeExpenditureCategoryToDatabase(){

        try{
            FXUtils.makeTextFieldBordersRedOnRejection(categoryName);
        }catch (TextFieldEmptyException textFieldEmptyException){
            // log user
            return;
        }

        ExpenditureCategory passToWriteCategory = new ExpenditureCategory.ExpenditureCategoryBuilder(categoryName.getText())
                                                                        .withRepresentativeColor(categoryColorPicker.getValue())
                                                                        .finishBuilding();

        Executor addExpenditureCategoryExecutor = Executors.newFixedThreadPool(2);
        addExpenditureCategoryExecutor.execute(DatabaseRunnableFactory.writeExpenditureCategoryAndRefreshTableView(passToWriteCategory, expenditureCategoryTableView, categoryName, currentLoginAccount));

    }

    public void clear(){
        FXUtils.clearRedOfTextField(categoryName);
        categoryName.setPromptText("Enter unique name");
        categoryColorPicker.setValue(Color.BLACK);
        categoryName.clear();
    }

    @Override
    public void shutDownScheduledExecutors() {
        refreshAccountList.shutdownNow();
        refreshCategoryExpenditure.shutdownNow();
        refreshRecordedChanges.shutdownNow();
    }
}