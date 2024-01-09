module main.hr.production.app {

    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires java.sql;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;

    opens main.hr.production.app to javafx.fxml;
    exports main.hr.production.app;

}