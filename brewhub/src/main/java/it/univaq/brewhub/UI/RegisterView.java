package it.univaq.brewhub.UI;

import java.io.File;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import it.univaq.brewhub.Utente;
import it.univaq.brewhub.Utente.TipoUtente;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class RegisterView {
	private final Stage stage;
	private final Utente jsonUtente=new Utente();
	private Image immagine=null;

	private final List<Utente> utentiRegistrati=jsonUtente.leggiJson();
	public RegisterView(Stage stage) {
		this.stage=stage;
	}
	
	public Parent getView() {
		stage.setResizable(false);
		stage.setTitle("Registrazione");
		
		
		VBox register=new VBox(10);
		register.setAlignment(Pos.CENTER);
		
		
		HBox bottoni=new HBox(10);
		bottoni.setAlignment(Pos.CENTER);
		
		HBox persona=new HBox(10);
		persona.setAlignment(Pos.CENTER);
		
		Label lblErrore=new Label();
		lblErrore.setVisible(false);
		lblErrore.setFont(Font.font( "Arial", FontWeight.BOLD, null, 14));
		lblErrore.setTextFill(Color.RED);
		
		Label lblTitolo=new Label("Registrazione");
		lblTitolo.setFont(Font.font( "Arial", FontWeight.BLACK, null, 24));
		
		TextField fldNome=new TextField();
		fldNome.setPromptText("Nome");
		fldNome.setMaxSize(120,20);
		
		TextField fldCognome=new TextField();
		fldCognome.setPromptText("Cognome");
		fldCognome.setMaxSize(120,20);
		
		TextField fldUsername=new TextField();
		fldUsername.setPromptText("Username");
		fldUsername.setMaxSize(250,20);
		
		PasswordField fldPassword= new PasswordField();
		fldPassword.setPromptText("Password");
		fldPassword.setMaxSize(250,20);
		
		ChoiceBox<TipoUtente> cbxTipo=new ChoiceBox<TipoUtente>();
		cbxTipo.setMaxSize(250, 2);
		cbxTipo.getItems().setAll(TipoUtente.values());
		cbxTipo.getItems().remove(TipoUtente.ADMIN);
		cbxTipo.getItems().remove(TipoUtente.OSPITE);
		cbxTipo.setValue(TipoUtente.UTENTE_MEDIO);
		

		Button btnAccedi=new Button("Accedi");
		Button btnRegistrati=new Button("Registrati");
		Button btnFoto=new Button("Scegli foto profilo");
		btnRegistrati.setDefaultButton(true);
		
		
		
		Hyperlink linkOspite=new Hyperlink("Continua come ospite");
		
		persona.getChildren().addAll(fldNome,fldCognome);
		bottoni.getChildren().addAll(btnAccedi,btnRegistrati);
		register.getChildren().addAll(lblTitolo,btnFoto,persona,cbxTipo,fldUsername,fldPassword,bottoni,linkOspite,lblErrore);
		
		btnAccedi.setOnAction(e->{
			LoginView login=new LoginView(stage);
			stage.getScene().setRoot(login.getView());
		});
		
		btnFoto.setOnAction(e->{
			FileChooser fileFoto=new FileChooser();
			fileFoto.getExtensionFilters().add( new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png", "*.gif", "*.jpeg"));
			fileFoto.setTitle("Selezione una foto");
			File fotoSelezionata=fileFoto.showOpenDialog(stage);
			if(fotoSelezionata!=null) {
				immagine=new Image(fotoSelezionata.toURI().toString());
				
			}
		});
		
		btnRegistrati.setOnAction(e->{
			lblErrore.setVisible(false);
			String nome=fldNome.getText();
			String cognome=fldCognome.getText();
			TipoUtente tipo=cbxTipo.getValue();
			String user=fldUsername.getText();
			String pw=fldPassword.getText();
			
			if(nome.isBlank()||cognome.isBlank()||user.isBlank()||pw.isBlank()||immagine== null) {
				
				lblErrore.setText("Inserire le credenziali");
				lblErrore.setVisible(true);
			}else if(controlloCredenziali(user)) {
				lblErrore.setText("L'utente con questo username è già registrato");
				lblErrore.setVisible(true);
			}else if(pw.length()<8) {
				lblErrore.setText("La password è troppo corta (minimo 8 caratteri)");
				lblErrore.setVisible(true);
			}else {
				Utente profilo=new Utente(nome,cognome,user,pw,tipo,immagine);
				jsonUtente.salvaJson(profilo);
				HomeView home=new HomeView(stage,profilo);
				stage.getScene().setRoot(home.getView());
				
			}
		});
		
		linkOspite.setOnAction(e->{
			Utente profilo=new Utente("guest");
			HomeView home=new HomeView(stage,profilo);
			stage.getScene().setRoot(home.getView());	
		});
		
		
		
		
		
		return register;
		
		
		
	}
	
	public boolean controlloCredenziali(String username) {
		return utentiRegistrati.stream().anyMatch(utente -> utente.getUsername().equals(username));
	}
}
