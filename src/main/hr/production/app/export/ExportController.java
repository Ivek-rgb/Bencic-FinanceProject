package main.hr.production.app.export;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import main.hr.production.app.AccountControllers;
import main.hr.production.app.HelloApplication;
import main.hr.production.logback.SessionLogger;
import main.hr.production.threads.DatabaseRunnableFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ExportController extends AccountControllers {

    @FXML
    private TextArea displayArea;
    @FXML
    private TextField fileNameField;
    @FXML
    private Label pathLabel;
    private static final String START_OF_PATH_LABEL = "Path: ";
    private File pathToExportTo = new File("dat");
    private String fileName = "export";

    public void initialize(){

        new Thread(DatabaseRunnableFactory.writeExportToTextArea(displayArea, currentLinkedBankAccount)).start();

        pathLabel.setText(START_OF_PATH_LABEL + " default choice -> dat directory ");

    }

    public void choseDirectory(){

        DirectoryChooser directoryChooserDialog = new DirectoryChooser();
        directoryChooserDialog.setTitle("Select directory to export to");

        pathToExportTo = Optional.ofNullable(directoryChooserDialog.showDialog(HelloApplication.getMainStage())).orElse(new File("dat"));

        pathLabel.setText(START_OF_PATH_LABEL + " " + pathToExportTo.getAbsolutePath());

    }

    public void saveToFile(){

        if(!fileNameField.getText().isEmpty())
            fileName = fileNameField.getText();

        String editedFileName = fileName + ".txt";
        int counter = 1;

        while (Paths.get(pathToExportTo.getPath(), editedFileName).toFile().exists())
            editedFileName = String.format(fileName + "(%d).txt", counter++);

        Path pathToWrite = Paths.get(pathToExportTo.getPath(),editedFileName);

        try {
            Files.writeString(pathToWrite, displayArea.getText());
        } catch (IOException e) {
            SessionLogger.logExceptionOnCurrentAccount(e);
        }

    }

    @Override
    public void shutDownScheduledExecutors() {

    }
}
