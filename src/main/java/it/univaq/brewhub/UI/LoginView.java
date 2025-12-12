package it.univaq.brewhub.UI;

import it.univaq.brewhub.Utente;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class LoginView {
	private final Stage stage;
	
	public LoginView(Stage stage) {
		this.stage=stage;
	}
	
	public Parent getView() {
		stage.setHeight(600);
		stage.setWidth(500);
		stage.setResizable(false);
		stage.setTitle("Login - BrewHub");
		stage.centerOnScreen();
		
		VBox login=new VBox(15);
		login.setAlignment(Pos.CENTER);
		login.setPadding(new javafx.geometry.Insets(40, 20, 40, 20));
		login.setStyle("-fx-background-color: " + ThemeManager.Colors.WHITE_CREAM + ";");
		
		VBox formBox=new VBox(12);
		formBox.setAlignment(Pos.CENTER);
		formBox.getStyleClass().add("form-box");
		
		HBox bottoni=new HBox(10);
		bottoni.setAlignment(Pos.CENTER);
		
		Label lblErrore=new Label();
		lblErrore.setVisible(false);
		lblErrore.getStyleClass().add("error-label");
		lblErrore.setWrapText(true);
		
		Label lblTitolo=new Label("BrewHub");
		lblTitolo.getStyleClass().add("title-label");
		
		Label lblSottotitolo=new Label("Accedi al tuo account");
		lblSottotitolo.getStyleClass().add("subtitle-label");
		
		TextField fldUsername=new TextField();
		fldUsername.setPromptText("Username");
		fldUsername.getStyleClass().add("text-field");
		fldUsername.setPrefHeight(40);
		fldUsername.setPrefWidth(300);
		
		PasswordField fldPassword= new PasswordField();
		fldPassword.setPromptText("Password");
		fldPassword.getStyleClass().add("password-field");
		fldPassword.setPrefHeight(40);
		fldPassword.setPrefWidth(300);

		Button btnAccedi=new Button("Accedi");
		btnAccedi.setDefaultButton(true);
		btnAccedi.setStyle(ThemeManager.Styles.buttonPrimary());
		btnAccedi.setPrefWidth(200);
		btnAccedi.setPrefHeight(40);
		
		Button btnRegistrati=new Button("Registrati");
		btnRegistrati.setStyle(ThemeManager.Styles.buttonSecondary());
		btnRegistrati.setPrefWidth(200);
		btnRegistrati.setPrefHeight(40);
		
		Hyperlink linkOspite=new Hyperlink("Continua come ospite");
		linkOspite.getStyleClass().add("hyperlink");
		
		bottoni.getChildren().addAll(btnAccedi,btnRegistrati);
		formBox.getChildren().addAll(lblTitolo,lblSottotitolo,fldUsername,fldPassword,bottoni,lblErrore);
		login.getChildren().addAll(formBox,linkOspite);
		
		btnAccedi.setOnAction(e->{
			String user=fldUsername.getText();
			String pw=fldPassword.getText();
			lblErrore.setVisible(false);
			if(user.isBlank() || pw.isBlank()) {
				lblErrore.setText("⚠ Inserisci username e password");
				lblErrore.setVisible(true);
				
			} else {
				Utente profilo = new Utente().login(user, pw);
				if(profilo == null) {
					lblErrore.setText("✗ Credenziali non valide");
					lblErrore.setVisible(true);
					fldPassword.clear();
				} else {
					HomeView home=new HomeView(stage, profilo);
					stage.getScene().setRoot(home.getView());
				}
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
}
