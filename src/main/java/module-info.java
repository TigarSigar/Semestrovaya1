module org.example.sem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires org.json;
    requires org.controlsfx.controls;
    requires java.sql;
    requires ch.qos.logback.classic;
    requires org.slf4j;


    opens org.example.sem to javafx.fxml;
    exports org.example.sem;
}