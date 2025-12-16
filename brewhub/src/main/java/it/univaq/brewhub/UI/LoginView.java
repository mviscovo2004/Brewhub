package it.univaq.brewhub.UI;

import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import it.univaq.brewhub.Utente;
import it.univaq.brewhub.Utente.TipoUtente;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;


public class LoginView {
	//private int guestNum;
	private final Stage stage;
	private final Utente jsonUtente=new Utente();
	private final List<Utente> utentiRegistrati=jsonUtente.leggiJson();	
	
	public LoginView(Stage stage) {
		this.stage=stage;
	}
	
	public Parent getView() {
		stage.setHeight(500);
		stage.setWidth(400);
		stage.setResizable(false);
		stage.setTitle("Login");
		stage.centerOnScreen();
		
		VBox login=new VBox(10);
		login.setAlignment(Pos.CENTER);
		
		
		HBox bottoni=new HBox(10);
		bottoni.setAlignment(Pos.CENTER);
		
		Label lblErrore=new Label();
		lblErrore.setVisible(false);
		lblErrore.setFont(Font.font( "Arial", FontWeight.BOLD, null, 14));
		lblErrore.setTextFill(Color.RED);
		
		Label lblTitolo=new Label("Login");
		lblTitolo.setFont(Font.font( "Arial", FontWeight.BLACK, null, 24));
		
		TextField fldUsername=new TextField();
		fldUsername.setPromptText("Username");
		fldUsername.setMaxSize(250,20);
		
		PasswordField fldPassword= new PasswordField();
		fldPassword.setPromptText("Password");
		fldPassword.setMaxSize(250,20);

		Button btnAccedi=new Button("Accedi");
		btnAccedi.setDefaultButton(true);
		
		Button btnRegistrati=new Button("Registrati");
		
		Hyperlink linkOspite=new Hyperlink("Continua come ospite");
		
		bottoni.getChildren().addAll(btnAccedi,btnRegistrati);
		login.getChildren().addAll(lblTitolo,fldUsername,fldPassword,bottoni,linkOspite,lblErrore);
		
		btnAccedi.setOnAction(e->{
			String user=fldUsername.getText();
			String pw=fldPassword.getText();
			lblErrore.setVisible(false);
			if(user.isBlank() || pw.isBlank()) {
				lblErrore.setText("Inserire credenziali");
				lblErrore.setVisible(true);
				
			}else if(!controlloCredenziali(user, pw)) {
				lblErrore.setText("Credenziali errate!");
				lblErrore.setVisible(true);
			}else {
				Utente profilo=jsonUtente.leggiSingoloJson(user);
				HomeView home=new HomeView(stage,profilo);
				stage.getScene().setRoot(home.getView());
			}
		});
		
		btnRegistrati.setOnAction(e->{
			
			RegisterView register=new RegisterView(stage);
			stage.getScene().setRoot(register.getView());
			
		});
		
		linkOspite.setOnAction(e->{
			String user="guest";
			Utente profilo=new Utente(user);
			HomeView home=new HomeView(stage,profilo);
			stage.getScene().setRoot(home.getView());
		});
		
		
		
		
		
		return login;
		
		
		
	}
	
	
	public boolean controlloCredenziali(String username,String password) {
		return utentiRegistrati.stream().anyMatch(utente -> utente.getUsername().equals(username) && BCrypt.checkpw(password, utente.getPasswordCrypto()));
	}
}
