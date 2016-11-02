package org.airavata.xbaya.ui.login;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginWindow extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
        primaryStage.setTitle("XBaya-Login");
        primaryStage.setScene(new Scene(root, 300, 125));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    public void displayLoginAndWait() throws IOException {
        Stage primaryStage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/views/login/login.fxml"));
        primaryStage.setTitle("XBaya-Login");
        primaryStage.setScene(new Scene(root, 300, 125));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
