module ru.gb.javafxchat {
    requires javafx.controls;
    requires javafx.fxml;


    exports ru.gb.javafxchat.client;
    opens ru.gb.javafxchat.client to javafx.fxml;
}