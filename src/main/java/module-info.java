module com.alonso.conversor_divisas_demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;


    opens com.alonso.conversor_divisas_demo to javafx.fxml;
    exports com.alonso.conversor_divisas_demo;
}