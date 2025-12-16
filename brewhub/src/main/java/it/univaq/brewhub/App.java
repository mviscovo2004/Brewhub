package it.univaq.brewhub;

import it.univaq.brewhub.Utente.TipoUtente;
import it.univaq.brewhub.UI.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        LoginView login=new LoginView(stage);
        Scene scene = new Scene(login.getView(),400, 500);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
    	Utente jsonUtente=new Utente();
    	
    
    	jsonUtente.creaCollezione();
    	
        launch();
    }

}