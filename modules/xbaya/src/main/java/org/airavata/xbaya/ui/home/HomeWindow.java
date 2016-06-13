package org.airavata.xbaya.ui.home;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeWindow extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
    	Parent root = FXMLLoader.load(getClass().getResource("/views/home.fxml"));    
    	primaryStage.setTitle("XBaya GUI");
        primaryStage.setScene(new Scene(root, 1060, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}