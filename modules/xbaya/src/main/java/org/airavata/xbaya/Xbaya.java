package org.airavata.xbaya;

import java.io.IOException;

import org.airavata.xbaya.ui.home.HomeWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Xbaya extends Application{

    private final static Logger logger = LoggerFactory.getLogger(Xbaya.class);

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		 HomeWindow homeWindow =  new HomeWindow();
		 Screen screen = Screen.getPrimary();
         Rectangle2D bounds = screen.getVisualBounds();
         primaryStage.setX(bounds.getMinX());
         primaryStage.setY(bounds.getMinY());
         primaryStage.setWidth(bounds.getWidth());
         primaryStage.setHeight(bounds.getHeight());
         homeWindow.start(primaryStage);
         primaryStage.setOnCloseRequest(t -> {
             Platform.exit();
             System.exit(0);
         });    
	}
	
	public static void main(String[] args) throws IOException {
        launch(args);
    }

}
