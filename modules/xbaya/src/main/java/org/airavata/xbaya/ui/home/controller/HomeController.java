package org.airavata.xbaya.ui.home.controller;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeController {

	public void newWorkflow() throws IOException{
		Stage primaryStage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/views/workflow.fxml"));    
    	primaryStage.setTitle("New Workflow");
        primaryStage.setScene(new Scene(root, 500, 350));
        primaryStage.show();
	}
}
