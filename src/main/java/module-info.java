module no.ntnu.pearproject {
    requires javafx.controls;
    requires javafx.fxml;

    opens no.ntnu.pearproject to javafx.fxml;
    exports no.ntnu.pearproject;
}