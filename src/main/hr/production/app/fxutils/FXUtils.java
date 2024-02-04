package main.hr.production.app.fxutils;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import main.hr.production.app.HelloApplication;
import main.hr.production.app.dialogcontrollers.ChangeDialogController;
import main.hr.production.exceptions.checked.UnsuportedConversionException;
import main.hr.production.exceptions.unchecked.TextFieldEmptyException;
import main.hr.production.logback.SessionLogger;
import main.hr.production.model.Account;
import main.hr.production.model.Expenditure;
import main.hr.production.model.ExpenditureCategory;
import main.hr.production.model.Income;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FXUtils {

    public static HBox returnComboboxColorWrapper(ExpenditureCategory usedVal){

        HBox mainWrapper = new HBox();
        mainWrapper.setSpacing(10);
        mainWrapper.setAlignment(Pos.BASELINE_LEFT);

        Rectangle colorDisplay = new Rectangle(10, 10, usedVal.getAppendedColorForReference());

        Label categoryName = new Label(usedVal.getName());
        categoryName.setTextFill(Color.BLACK);

        mainWrapper.getChildren().add(colorDisplay);
        mainWrapper.getChildren().add(categoryName);

        return mainWrapper;

    }

    public static <T> HBox returnOperationalColumnGraphic(Supplier<Button> buttonDeleteFactory){

        HBox mainWrapper = new HBox();
        mainWrapper.setSpacing(10);
        mainWrapper.setAlignment(Pos.BASELINE_CENTER);

        Button deleteButton = buttonDeleteFactory.get();
        deleteButton.setId("deleteButton");
        deleteButton.setText("");

        ImageView trashCanImage;

        try {
            trashCanImage = new ImageView(new Image(new FileInputStream("src/main/resources/image-resources/icons8-trash-can-120.png")));
        } catch (FileNotFoundException e) {
            SessionLogger.sessionLogger.error("Crucial application resource missing", e);
            trashCanImage = new ImageView();
        }

        trashCanImage.setFitHeight(12);
        trashCanImage.setFitWidth(12);

        deleteButton.setGraphic(trashCanImage);

        mainWrapper.getChildren().addAll(deleteButton);

        return mainWrapper;
    }


    public static <T> HBox returnOperationalColumnGraphic(Supplier<Button> buttonDeleteFactory, Supplier<Button> buttonEditFactory){

        Button updateButton = buttonEditFactory.get();
        updateButton.setId("updateButton");
        updateButton.setText("");

        ImageView updateImage;

        try {
            updateImage = new ImageView(new Image(new FileInputStream("src/main/resources/image-resources/icons8-settings-100.png")));
        } catch (FileNotFoundException e) {
            SessionLogger.sessionLogger.error("Crucial application resource missing", e);
            updateImage = new ImageView();
        }

        updateImage.setFitHeight(12);
        updateImage.setFitWidth(12);

        updateButton.setGraphic(updateImage);

        HBox returnVal = returnOperationalColumnGraphic(buttonDeleteFactory);
        returnVal.getChildren().add(updateButton);

        return returnVal;
    }

    public static String checkForEmptyTextFields(List<TextField> listOfFields){
        return listOfFields.stream().map((textField) ->
            textField.getText().isEmpty() ? ("* field " + textField.getId().split("Field")[0] + " cannot be empty") : ""
        ).filter(string -> !string.isEmpty()).collect(Collectors.joining("\n"));
    }

    public static void switchScenes(String fxmlResourceName){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(fxmlResourceName));
            Parent root = fxmlLoader.load();
            HelloApplication.getMainStage().setScene(new Scene(root));
        } catch (IOException e) {
            SessionLogger.sessionLogger.error("Failure to extract fxml resource", e);
        }
    }

    public static void makeTextFieldBordersRedOnRejection(TextField chosenTextField) throws TextFieldEmptyException {
        if(chosenTextField.getText().isEmpty()){
            chosenTextField.setStyle(chosenTextField.getStyle() + "-fx-border-color: red; -fx-border-width: 1px;");
            throw new TextFieldEmptyException();
        }
    }

    public static BigDecimal handleBigDecimalConversionFromTextField(TextField textField) throws UnsuportedConversionException {
        String textToBeConverted = textField.getText().replace(",", ".");
        if(textToBeConverted.equals("."))
            textToBeConverted = "0";
        try{
            return new BigDecimal(textToBeConverted);
        }catch (RuntimeException runtimeException){
            SessionLogger.logExceptionOnCurrentAccount(runtimeException);
            throw new UnsuportedConversionException(runtimeException);
        }
    }

    protected static <T> void editDialogFactory(String fxmlResource, T insertionObject, String titleMessage, Account currentLoginAccount){

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(fxmlResource));

        try {

            DialogPane dialogPane = fxmlLoader.load();
            Dialog<ButtonType> myDialog = new Dialog<>();
            ChangeDialogController<T> expenditureEditDialogController = fxmlLoader.getController();
            expenditureEditDialogController.setSelected(insertionObject, currentLoginAccount);

            myDialog.setTitle(titleMessage);
            myDialog.setDialogPane(dialogPane);
            myDialog.showAndWait();

        } catch (IOException e) {
            SessionLogger.sessionLogger.error("Failure to extract fxml resource", e);
        }

    }

    public static void startEditExpenditureDialog(Expenditure insertionExpenditure, Account currentLoginAccount){
        editDialogFactory("expenditure-changer.fxml", insertionExpenditure, "Edit current expenditure", currentLoginAccount);
    }

    public static void startEditIncomeDialog(Income insertionIncome, Account currentLoginAccount){
        editDialogFactory("income-changer.fxml", insertionIncome, "Edit current income", currentLoginAccount);
    }

    public static void setTextFieldFormatterToNumericOnly(TextField chosenTextField){

        chosenTextField.setTextFormatter(new TextFormatter<>(change -> {
            String changedText = change.getText();

            if(changedText.length() > 1 && changedText.matches("\\d*|\\d+,\\d*"))
                return change;

            if(changedText.matches("\\d*") || (changedText.matches(",") && !chosenTextField.getText().contains(","))){
                return change;
            }else{
                return null;
            }
        }));

    }

    public static boolean createAndWaitConfirmation(){

        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setContentText("Do you agree to delete this object.");
        confirmationDialog.setTitle("Confirm the deletion");

        ButtonType returnValue = confirmationDialog.showAndWait().orElse(ButtonType.CANCEL);

        return returnValue.equals(ButtonType.OK);

    }

    public static void clearRedOfTextField(TextField chosenTextField){
        chosenTextField.setStyle("");
    }

    public static <T> void setTableCols(Map<String, String> fields, TableView<T> tableView){
        tableView.getColumns().clear();
        fields.keySet().stream().toList().forEach(column ->{
            TableColumn<T, String> tCol = new TableColumn<>(fields.get(column));
            tCol.setCellValueFactory(new PropertyValueFactory<T, String>(column));
            tableView.getColumns().add(tCol);
        });
    }

}