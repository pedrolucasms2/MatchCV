module org.example.matchcvjava {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.pdfbox;
    requires com.google.gson;
    requires java.net.http;
    requires javafx.graphics;

    opens org.example to javafx.base, javafx.fxml, com.google.gson;
    exports org.example to javafx.graphics;
}