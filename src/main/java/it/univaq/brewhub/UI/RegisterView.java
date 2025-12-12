package it.univaq.brewhub.UI;

import java.io.File;
import java.sql.SQLException;

import it.univaq.brewhub.Utente;
import it.univaq.brewhub.Utente.TipoUtente;
import it.univaq.brewhub.MediaManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RegisterView {
	private final Stage stage;
	private String immagine = null;

	public RegisterView(Stage stage) {
		this.stage = stage;
	}

	public Parent getView() {
		stage.setResizable(false);
		stage.setTitle("Registrazione - BrewHub");
		stage.setWidth(600);
		stage.setHeight(750);

		VBox register = new VBox(15);
		register.setAlignment(Pos.TOP_CENTER);
		register.setPadding(new javafx.geometry.Insets(30, 20, 30, 20));
		register.setStyle("-fx-background-color: " + ThemeManager.Colors.WHITE_CREAM + ";");

		VBox formBox = new VBox(12);
		formBox.setAlignment(Pos.CENTER);
		formBox.getStyleClass().add("form-box");

		HBox bottoni = new HBox(10);
		bottoni.setAlignment(Pos.CENTER);

		HBox persona = new HBox(15);
		persona.setAlignment(Pos.CENTER);

		Label lblErrore = new Label();
		lblErrore.setVisible(false);
		lblErrore.getStyleClass().add("error-label");
		lblErrore.setWrapText(true);

		Label lblTitolo = new Label("Crea un account");
		lblTitolo.getStyleClass().add("title-label");

		Label lblSottotitolo = new Label("Registrati per iniziare");
		lblSottotitolo.getStyleClass().add("subtitle-label");

		TextField fldNome = new TextField();
		fldNome.setPromptText("Nome");
		fldNome.getStyleClass().add("text-field");
		fldNome.setPrefHeight(40);
		fldNome.setPrefWidth(150);

		TextField fldCognome = new TextField();
		fldCognome.setPromptText("Cognome");
		fldCognome.getStyleClass().add("text-field");
		fldCognome.setPrefHeight(40);
		fldCognome.setPrefWidth(150);

		TextField fldUsername = new TextField();
		fldUsername.setPromptText("Username");
		fldUsername.getStyleClass().add("text-field");
		fldUsername.setPrefHeight(40);
		fldUsername.setPrefWidth(300);

		PasswordField fldPassword = new PasswordField();
		fldPassword.setPromptText("Password (min 8 caratteri)");
		fldPassword.getStyleClass().add("password-field");
		fldPassword.setPrefHeight(40);
		fldPassword.setPrefWidth(300);

		ChoiceBox<TipoUtente> cbxTipo = new ChoiceBox<TipoUtente>();
		cbxTipo.getStyleClass().add("choice-box");
		cbxTipo.setPrefWidth(300);
		cbxTipo.getItems().setAll(TipoUtente.values());
		cbxTipo.getItems().remove(TipoUtente.ADMIN);
		cbxTipo.getItems().remove(TipoUtente.OSPITE);
		cbxTipo.setValue(TipoUtente.UTENTE_MEDIO);

		Button btnAccedi = new Button("Accedi");
		btnAccedi.setStyle(ThemeManager.Styles.buttonSecondary());
		btnAccedi.setPrefWidth(180);
		btnAccedi.setPrefHeight(40);

		Button btnRegistrati = new Button("Registrati");
		btnRegistrati.setDefaultButton(true);
		btnRegistrati.setStyle(ThemeManager.Styles.buttonSuccess());
		btnRegistrati.setPrefWidth(180);
		btnRegistrati.setPrefHeight(40);

		Button btnFoto = new Button("📷 Scegli foto profilo");
		btnFoto.setStyle("-fx-padding: 10 20 10 20; -fx-font-size: 13; -fx-background-color: "
				+ ThemeManager.Colors.MEDIUM_COFFEE + "; -fx-text-fill: " + ThemeManager.Colors.WHITE_CREAM
				+ "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
		btnFoto.setPrefHeight(40);

		HBox fotoBox = new HBox(15);
		Label lblFile = new Label("Nessuna foto selezionata");
		lblFile.setStyle("-fx-text-fill: " + ThemeManager.Colors.PALE_COFFEE + "; -fx-font-style: italic;");
		fotoBox.setAlignment(Pos.CENTER);

		Hyperlink linkOspite = new Hyperlink("Continua come ospite");
		linkOspite.getStyleClass().add("hyperlink");

		persona.getChildren().addAll(fldNome, fldCognome);
		bottoni.getChildren().addAll(btnAccedi, btnRegistrati);
		fotoBox.getChildren().addAll(btnFoto, lblFile);
		formBox.getChildren().addAll(lblTitolo, lblSottotitolo, fotoBox, persona, cbxTipo, fldUsername, fldPassword,
				bottoni, lblErrore);
		register.getChildren().addAll(formBox, linkOspite);

		btnAccedi.setOnAction(e -> {
			LoginView login = new LoginView(stage);
			stage.getScene().setRoot(login.getView());
		});

		btnFoto.setOnAction(e -> {
			FileChooser fileFoto = new FileChooser();
			fileFoto.getExtensionFilters()
					.add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png", "*.gif", "*.jpeg"));
			fileFoto.setTitle("Selezione una foto");
			File fotoSelezionata = fileFoto.showOpenDialog(stage);
			if (fotoSelezionata != null) {
				// Inizializza la cartella media
				MediaManager.initMediaFolder();
				// Copia il file nella cartella media
				String percorsoMedia = MediaManager.copyMediaToFolder(fotoSelezionata);
				if (percorsoMedia != null) {
					immagine = percorsoMedia;
					lblFile.setText(fotoSelezionata.getName());
				} else {
					lblFile.setText("Errore nel caricamento della foto");
				}
			}
		});

		btnRegistrati.setOnAction(e -> {
			lblErrore.setVisible(false);
			String nome = fldNome.getText();
			String cognome = fldCognome.getText();
			TipoUtente tipo = cbxTipo.getValue();
			String user = fldUsername.getText();
			String pw = fldPassword.getText();

			if (nome.isBlank() || cognome.isBlank() || user.isBlank() || pw.isBlank() || immagine == null) {

				lblErrore.setText("⚠ Completa tutti i campi, inclusa la foto profilo");
				lblErrore.setVisible(true);
			} else if (pw.length() < 8) {
				lblErrore.setText("✗ Password troppo corta (minimo 8 caratteri)");
				lblErrore.setVisible(true);
			} else {
				try {
					System.out.println("DEBUG - Inizio registrazione utente: " + user);
					Utente nuovoUtente = new Utente(nome, cognome, user, pw, tipo, immagine);

					
					Utente dao = new Utente();
					dao.registraUtente(nuovoUtente);
					System.out.println("DEBUG - Registrazione completata, vado alla Home");

					// Passiamo alla Home
					HomeView home = new HomeView(stage, nuovoUtente);
					stage.getScene().setRoot(home.getView());
					System.out.println("DEBUG - Home caricata con successo");

				} catch (SQLException ex) {
					System.out.println("DEBUG - Errore SQLException: " + ex.getMessage());
					ex.printStackTrace();
					if (ex.getMessage().contains("Username già registrato")) {
						lblErrore.setText("✗ Username già registrato");
					} else if (ex.getMessage().contains("PRIMARY KEY")) {
						lblErrore.setText("✗ Username già esistente!");
					} else {
						lblErrore.setText("✗ Errore Database: " + ex.getMessage());
					}
					lblErrore.setVisible(true);
				} catch (Exception ex) {
					System.out.println("DEBUG - Errore generico: " + ex.getMessage());
					ex.printStackTrace();
					lblErrore.setText("✗ Errore: " + ex.getMessage());
					lblErrore.setVisible(true);
				}

			}
		});

		linkOspite.setOnAction(e -> {
			Utente profilo = new Utente("guest");
			HomeView home = new HomeView(stage, profilo);
			stage.getScene().setRoot(home.getView());
		});

		return register;

	}
}
