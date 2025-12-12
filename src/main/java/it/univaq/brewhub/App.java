package it.univaq.brewhub;


import it.univaq.brewhub.UI.LoginView;
import it.univaq.brewhub.UI.ThemeManager;
import javafx.application.Application;
import javafx.scene.Scene;

import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        LoginView login=new LoginView(stage);
        Scene scene = new Scene(login.getView(),400, 500);
        
        // Applica il tema caffè
        ThemeManager.applyTheme(scene);
        
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
    	
        
    	DatabaseManager.init();
    	
        launch();
    }

}