package main.hr.production.app.menubar;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import main.hr.production.app.AccountControllers;
import main.hr.production.app.HelloApplication;
import main.hr.production.app.dashboard.WelcomeController;
import main.hr.production.app.administrativepanel.AdministrativePanelController;
import main.hr.production.app.fxutils.FXUtils;
import main.hr.production.app.settings.SettingsController;
import main.hr.production.app.transactioncontrollers.ExpenditureController;
import main.hr.production.app.transactioncontrollers.IncomeController;
import main.hr.production.logback.SessionLogger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MenuBarController {

    @FXML
    private VBox sliderVBox;
    @FXML
    private MenuBar innerMenu;
    @FXML
    private ImageView arrowImage;
    @FXML
    private Menu settingMenu;

    @FXML
    private Button adminPanelButton;

    private boolean isCollapsed = false;
    private boolean finishedRotating = true;
    private int yMenuTranslate = 40;
    private static boolean isAdministrator = false;

    public void initialize(){

        if(!isAdministrator){
            adminPanelButton.setPrefWidth(0);
            adminPanelButton.setDisable(true);
            adminPanelButton.setOpacity(0);
        }

    }

    public void slideMenuUpwards(){

        if(finishedRotating){

            finishedRotating = false;

            yMenuTranslate *= -1;

            TranslateTransition slideAnimation = new TranslateTransition();
            RotateTransition arrowAnimation = new RotateTransition();

            arrowAnimation.setNode(arrowImage);
            arrowAnimation.setDuration(Duration.seconds(0.25));

            slideAnimation.setDuration(Duration.seconds(0.5));
            slideAnimation.setNode(sliderVBox);

            slideAnimation.setToY(Math.min(yMenuTranslate, 0));
            arrowAnimation.setByAngle(180);

            slideAnimation.play();
            arrowAnimation.play();

            sliderVBox.setTranslateY(Math.min(yMenuTranslate, 0));

            slideAnimation.setOnFinished((event) ->{
                finishedRotating = true;
                if(!isCollapsed){
                    sliderVBox.setTranslateY(yMenuTranslate);
                    isCollapsed = true;
                }else isCollapsed = false;
            });
        }

    }

    public <T extends AccountControllers> void changeScreen(String fxmlResource, List<Method> startUpMethods, Class<T> controllerClass){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(fxmlResource));
            Parent parentNode = fxmlLoader.load();
            T instantiatedController = fxmlLoader.getController();
            AccountControllers.getCurrentOpenController().shutDownScheduledExecutors();
            AccountControllers.setCurrentOpenController(instantiatedController);
            startUpMethods.forEach(method -> {
                try {
                    method.invoke(instantiatedController);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    SessionLogger.sessionLogger.error("Could now invoke method on account controller", e);
                }
            });
            HelloApplication.getMainStage().setScene(new Scene(parentNode));
        } catch (IOException e) {
            SessionLogger.sessionLogger.error("Failed to construct fxml resource", e);
        }
    }

    public void changeScreenSettings(){
        changeScreen("settings.fxml", List.of(), SettingsController.class);
    }

    public void changeScreenAdminPanel(){
        changeScreen("admin-panel.fxml", List.of(), AdministrativePanelController.class);
    }

    public void changeScreenExpenditures(){
        changeScreen("expenditure-section.fxml", List.of() , ExpenditureController.class);
    }

    public void changeScreenExport(){
        changeScreen("export-section.fxml", List.of() , ExpenditureController.class);
    }

    public void changeScreenIncome(){
        changeScreen("income-section.fxml", List.of(), IncomeController.class);
    }

    public void changeScreenHome(){
        try {
            List<Method> startMethod = List.of(WelcomeController.class.getMethod("setWelcomeText"), WelcomeController.class.getMethod("setTotalBalance"));
            changeScreen("welcome-screen.fxml", startMethod, SettingsController.class);
        } catch (NoSuchMethodException e) {
            SessionLogger.sessionLogger.error("No method was found for this Class", e);
        }
    }

    public static void setAdministrator(boolean check){
        isAdministrator = check;
    }

    public void logOutOfApp(){
        SessionLogger.sessionLogger.info(AccountControllers.getCurrentLoginAccount() + "has logged out of Application");
        AccountControllers.getCurrentOpenController().shutDownScheduledExecutors();
        AccountControllers.setCurrentOpenController(null);
        AccountControllers.setAccountFromClass(null, null);
        FXUtils.switchScenes("login-page.fxml");
    }

}
