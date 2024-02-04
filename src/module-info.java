module main.hr.production.app {

    requires javafx.controls;
    requires com.jfoenix;
    requires javafx.fxml;
    requires org.slf4j;
    requires java.sql;
    requires bcrypt;

    exports main.hr.production.model;
    exports main.hr.production.exceptions.checked;
    exports main.hr.production.exceptions.unchecked;

    exports main.hr.production.app;
    opens main.hr.production.app to javafx.fxml, com.jfoenix;

    exports main.hr.production.app.logincontrollers;
    opens main.hr.production.app.logincontrollers to javafx.fxml, com.jfoenix;

    exports main.hr.production.app.dialogcontrollers;
    opens main.hr.production.app.dialogcontrollers to javafx.fxml, com.jfoenix;

    exports main.hr.production.app.menubar;
    opens main.hr.production.app.menubar to com.jfoenix, javafx.fxml;

    exports main.hr.production.app.transactioncontrollers;
    opens main.hr.production.app.transactioncontrollers to com.jfoenix, javafx.fxml;

    exports main.hr.production.app.export;
    opens main.hr.production.app.export to com.jfoenix, javafx.fxml;

    exports main.hr.production.app.settings;
    opens main.hr.production.app.settings to javafx.fxml;

    exports main.hr.production.app.administrativepanel;
    opens main.hr.production.app.administrativepanel to com.jfoenix, javafx.fxml;

    exports main.hr.production.app.dashboard;
    opens main.hr.production.app.dashboard to com.jfoenix, javafx.fxml;
}