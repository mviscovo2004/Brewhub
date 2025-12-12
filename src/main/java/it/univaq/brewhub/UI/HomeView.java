package it.univaq.brewhub.UI;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import it.univaq.brewhub.Commento;
import it.univaq.brewhub.Post;
import it.univaq.brewhub.Post.TipoPost;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.MediaManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;


public class HomeView {
	
	
    private final Stage stage;
    private final Utente utenteLoggato;

    
    // --- COSTRUTTORE ---
    public HomeView(Stage stage, Utente utenteLoggato) {
        this.stage = stage;
        this.utenteLoggato = utenteLoggato;
    }

    
    // --- METODO PER RITORNARE LA UI ---
    public Parent getView() {
    	
    	//IMPOSTAZIONI FINESTRA
    	stage.setWidth(1280);
    	stage.setHeight(720);
    	stage.setResizable(true);
    	stage.setMaximized(true);
    	stage.setTitle("BrewHub - Home");
    	stage.centerOnScreen();
    	
    	// --- HEADER ---
    	HBox header = new HBox(20);
        header.setPadding(new javafx.geometry.Insets(12, 20, 12, 20));
        header.setStyle("-fx-background-color: " + ThemeManager.Colors.MEDIUM_COFFEE + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        header.setAlignment(Pos.CENTER_LEFT);
    	
        //LOGO
    	Label logo = new Label("☕ BrewHub");
        logo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + ThemeManager.Colors.WHITE_CREAM + ";");

        //BARRA DI RICERCA
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Cerca post o comunità...");
        searchField.setPrefWidth(350);
        searchField.setStyle("-fx-padding: 8; -fx-font-size: 13; -fx-control-inner-background: " + ThemeManager.Colors.WHITE_CREAM + "; -fx-border-radius: 5; -fx-background-radius: 5;");

        //BOTTONI HEADER
        Button profileBtn = new Button("👤 " + utenteLoggato.getUsername());
        profileBtn.setStyle("-fx-padding: 8 15 8 15; -fx-font-size: 13; -fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: " + ThemeManager.Colors.WHITE_CREAM + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        
        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle("-fx-padding: 8 15 8 15; -fx-font-size: 13; -fx-background-color: " + ThemeManager.Colors.ACCENT_BROWN + "; -fx-text-fill: " + ThemeManager.Colors.WHITE_CREAM + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        
        //METODO PER LOGOUT
        logoutBtn.setOnAction(e ->{
        	LoginView login= new LoginView(stage);
        	stage.getScene().setRoot(login.getView());
        });
        
        Region spacer = new Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(logo, searchField, spacer, profileBtn, logoutBtn);
        
        // --- SIDEBAR ---
        VBox sidebar = new VBox(0);
        sidebar.setPadding(new javafx.geometry.Insets(0));
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color: " + ThemeManager.Colors.CREAM + "; -fx-border-color: " + ThemeManager.Colors.COPPER + "; -fx-border-width: 0 1 0 0;");
        
        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setPrefWidth(240);
        sidebarScroll.getStyleClass().add("scroll-pane");

        // --- SEZIONE PROFILO ---
        VBox profileSection = new VBox(10);
        profileSection.setPadding(new javafx.geometry.Insets(15, 15, 15, 15));
        profileSection.setStyle("-fx-background-color: " + ThemeManager.Colors.WHITE_CREAM + "; -fx-border-color: " + ThemeManager.Colors.COPPER + "; -fx-border-width: 0 0 1 0;");
        
        HBox profileHeader = new HBox(10);
        profileHeader.setAlignment(Pos.CENTER_LEFT);
        Label profileIcon = new Label("👤");
        profileIcon.setStyle("-fx-font-size: 24px;");
        
        VBox profileInfo = new VBox(2);
        Label userName = new Label(utenteLoggato.getUsername());
        userName.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + ThemeManager.Colors.DARK_COFFEE + ";");
        Label userType = new Label(utenteLoggato.getTipo().toString());
        userType.setStyle("-fx-font-size: 11px; -fx-text-fill: " + ThemeManager.Colors.PALE_COFFEE + ";");
        profileInfo.getChildren().addAll(userName, userType);
        profileHeader.getChildren().addAll(profileIcon, profileInfo);
        profileSection.getChildren().add(profileHeader);

        // --- SEZIONE CATEGORIE ---
        VBox categorySection = new VBox(10);
        categorySection.setPadding(new javafx.geometry.Insets(15));
        categorySection.setStyle("-fx-background-color: " + ThemeManager.Colors.CREAM + ";");
        
        Label catTitle = new Label("📚 CATEGORIE");
        catTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + ThemeManager.Colors.DARK_COFFEE + "; -fx-letter-spacing: 1;");
        
        Button cat1 = new Button("☕ Torrefattori");
        cat1.getStyleClass().add("category-button");
        cat1.setPrefWidth(210);
        
        Button cat2 = new Button("🫘 Caffè");
        cat2.getStyleClass().add("category-button");
        cat2.setPrefWidth(210);
        
        Button cat3 = new Button("🎉 Eventi");
        cat3.getStyleClass().add("category-button");
        cat3.setPrefWidth(210);
        
        Separator sep1 = new Separator();
        sep1.setStyle("-fx-padding: 5 0 5 0;");
        
        categorySection.getChildren().addAll(catTitle, cat1, cat2, cat3, sep1);

        // --- SEZIONE I MIEI POST ---
        VBox myPostsSection = new VBox(10);
        myPostsSection.setPadding(new javafx.geometry.Insets(15));
        myPostsSection.setStyle("-fx-background-color: " + ThemeManager.Colors.CREAM + ";");
        
        Label myPostsTitle = new Label("✍️ I MIEI POST");
        myPostsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + ThemeManager.Colors.DARK_COFFEE + "; -fx-letter-spacing: 1;");
        
        Button myPostsBtn = new Button("📄 Visualizza i miei post");
        myPostsBtn.setStyle("-fx-padding: 8 12 8 12; -fx-font-size: 12; -fx-background-color: " + ThemeManager.Colors.WHITE_CREAM + "; -fx-border-color: " + ThemeManager.Colors.COPPER + "; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-text-fill: " + ThemeManager.Colors.DARK_COFFEE + ";");
        myPostsBtn.setPrefWidth(210);
        myPostsBtn.setWrapText(true);
        
        Separator sep2 = new Separator();
        sep2.setStyle("-fx-padding: 5 0 5 0;");
        
        myPostsSection.getChildren().addAll(myPostsTitle, myPostsBtn, sep2);

        // --- SEZIONE ARCHIVIO ---
        VBox archiveSection = new VBox(10);
        archiveSection.setPadding(new javafx.geometry.Insets(15));
        archiveSection.setStyle("-fx-background-color: " + ThemeManager.Colors.CREAM + ";");
        
        Label archiveTitle = new Label("⭐ ARCHIVIO");
        archiveTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + ThemeManager.Colors.DARK_COFFEE + "; -fx-letter-spacing: 1;");
        
        Button archiveBtn = new Button("📑 Visualizza archivio");
        archiveBtn.setStyle("-fx-padding: 8 12 8 12; -fx-font-size: 12; -fx-background-color: " + ThemeManager.Colors.ACCENT_GREEN + "; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-text-fill: " + ThemeManager.Colors.WHITE_CREAM + "; -fx-font-weight: bold;");
        archiveBtn.setPrefWidth(210);
        archiveBtn.setWrapText(true);
        
        Separator sep3 = new Separator();
        sep3.setStyle("-fx-padding: 5 0 5 0;");
        
        archiveSection.getChildren().addAll(archiveTitle, archiveBtn, sep3);

        // --- SEZIONE STATISTICHE ---
        VBox statsSection = new VBox(10);
        statsSection.setPadding(new javafx.geometry.Insets(15));
        statsSection.setStyle("-fx-background-color: " + ThemeManager.Colors.CREAM + ";");
        
        Label statsTitle = new Label("📊 STATISTICHE");
        statsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + ThemeManager.Colors.DARK_COFFEE + "; -fx-letter-spacing: 1;");
        
        HBox statBox1 = new HBox(10);
        Label statLabel1 = new Label("Post creati:");
        statLabel1.setStyle("-fx-font-size: 11px; -fx-text-fill: " + ThemeManager.Colors.DARK_COFFEE + ";");
        Label statValue1 = new Label("0");
        statValue1.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + ThemeManager.Colors.MEDIUM_COFFEE + ";");
        Region spacer1 = new Region();
        javafx.scene.layout.HBox.setHgrow(spacer1, javafx.scene.layout.Priority.ALWAYS);
        statBox1.getChildren().addAll(statLabel1, spacer1, statValue1);
        
        HBox statBox2 = new HBox(10);
        Label statLabel2 = new Label("Nel tuo archivio:");
        statLabel2.setStyle("-fx-font-size: 11px; -fx-text-fill: " + ThemeManager.Colors.DARK_COFFEE + ";");
        Label statValue2 = new Label(String.valueOf(utenteLoggato.getArchivio().size()));
        statValue2.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + ThemeManager.Colors.ACCENT_GREEN + ";");
        Region spacer2 = new Region();
        javafx.scene.layout.HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);
        statBox2.getChildren().addAll(statLabel2, spacer2, statValue2);
        
        statsSection.getChildren().addAll(statsTitle, statBox1, statBox2);

        // --- AGGREGAZIONE SIDEBAR ---
        sidebar.getChildren().addAll(profileSection, categorySection, myPostsSection, archiveSection, statsSection);
        
        ScrollPane sidebarPane = new ScrollPane(sidebar);
        sidebarPane.setFitToWidth(true);
        sidebarPane.setPrefWidth(240);
        sidebarPane.getStyleClass().add("scroll-pane");

        // --- FEED POST ---
        VBox feed = new VBox(15);
        feed.setPadding(new javafx.geometry.Insets(15));

        //MESSAGGIO DI BENVENUTO
        Label benvenuto=new Label("🎯 Benvenuto su BrewHub");
        benvenuto.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + ThemeManager.Colors.DARK_COFFEE + ";");
        feed.getChildren().add(benvenuto);

        // CARICA I POST DAL DATABASE
        caricaPostDalDatabase(feed);

        // --- DASHBOARD ---
        
        VBox dashboard = new VBox(12);
        dashboard.setPadding(new javafx.geometry.Insets(15));
        dashboard.getStyleClass().add("dashboard");

        //TITOLO
        Label dashTitle = new Label("✍️ Crea un nuovo post");
        dashTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: " + ThemeManager.Colors.DARK_COFFEE + ";");
        
        TextField fldTitolo=new TextField();
        fldTitolo.setPromptText("Titolo del post...");
        fldTitolo.getStyleClass().add("text-field");
        fldTitolo.setPrefHeight(38);
        
        TextArea postArea = new TextArea();
        postArea.getStyleClass().add("text-area");
       
        //MENU A TENDINA - TIPO POST
        ChoiceBox<Post.TipoPost> cbxTipo=new ChoiceBox<TipoPost>();
		cbxTipo.getStyleClass().add("choice-box");
		cbxTipo.setPrefWidth(150);
		cbxTipo.getItems().setAll(TipoPost.values());
		cbxTipo.setValue(TipoPost.TESTO);
		
		//BOX PER POST CON MEDIA (FOTO/VIDEO)
		HBox mediaBox = new HBox(10);
		
		//METODO PER CAMBIO UI E SELEZIONE FILE
		cbxTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			
		    mediaBox.getChildren().clear();
		    Label lblFile= new Label("Ancora nessun media selezionato...");
		    Button btnCaricaFile = new Button("Seleziona il media che vuoi caricare");
		    btnCaricaFile.setStyle("-fx-padding: 8 15 8 15; -fx-font-size: 12; -fx-background-color: " + ThemeManager.Colors.MEDIUM_COFFEE + "; -fx-text-fill: " + ThemeManager.Colors.WHITE_CREAM + "; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
		    FileChooser fileChooser = new FileChooser();	
		    
		    if (newVal==TipoPost.FOTO) { //RAMO PER SELEZIONE FOTO
		    	
		    	btnCaricaFile.setOnAction(e ->{
		    		fileChooser.setTitle("Seleziona un'immagine");
		    		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini","*.jpg","*.jpeg","*.png")
		    		);
		    	
		    		File selectedFile = fileChooser.showOpenDialog(stage);
		    		
		    		if(selectedFile!=null) {
		    			// Inizializza la cartella media
		    			MediaManager.initMediaFolder();
		    			// Copia il file nella cartella media
		    			String percorsoMedia = MediaManager.copyMediaToFolder(selectedFile);
		    			if (percorsoMedia != null) {
		    				mediaBox.setUserData(percorsoMedia);
		    				lblFile.setText(selectedFile.getName());
		    			} else {
		    				lblFile.setText("Errore nel caricamento dell'immagine");
		    			}
		    		}
		    	});
		    	
		    	mediaBox.getChildren().addAll(btnCaricaFile, lblFile);
		    	
		    }else if(newVal==TipoPost.VIDEO) { //RAMO PER SELEZIONE VIDEO
		    	btnCaricaFile.setOnAction(e ->{
		    		fileChooser.setTitle("Seleziona un video");
		    		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video","*.mp4")
		    		);
		    	
		    		File selectedFile = fileChooser.showOpenDialog(stage);
		    		
		    		if(selectedFile!=null) {
		    			// Inizializza la cartella media
		    			MediaManager.initMediaFolder();
		    			// Copia il file nella cartella media
		    			String percorsoMedia = MediaManager.copyMediaToFolder(selectedFile);
		    			if (percorsoMedia != null) {
		    				mediaBox.setUserData(percorsoMedia);
		    				lblFile.setText(selectedFile.getName());
		    			} else {
		    				lblFile.setText("Errore nel caricamento del video");
		    			}
		    		}
		    	});
		    	
		    	mediaBox.getChildren().addAll(btnCaricaFile, lblFile);
		    }
		    
		});
    
		
        postArea.setPromptText("Scrivi qui il tuo post...");
        postArea.setPrefRowCount(4);
        postArea.setWrapText(true);
        

        
        
        Button publishBtn = new Button("📤 Pubblica");
        publishBtn.setStyle(ThemeManager.Styles.buttonSuccess());
        publishBtn.setPrefHeight(38);
        publishBtn.setOnAction(e -> {
        	String titolo= fldTitolo.getText();
            String content = postArea.getText().trim();
            TipoPost tipo = cbxTipo.getValue();
            Post post;

            if (tipo == null) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Seleziona la tipologia del post.");
                return;
            }
            
            switch(tipo) {
	            case TESTO:
	            	if(content.isBlank()) {
	            		showAlert(AlertType.ERROR, "ERRORE","Il contenuto del post non può essere vuoto  ");
	            		return;
	            	}else if(titolo.isBlank()){
	            		showAlert(AlertType.ERROR, "ERRORE","Scrivere un titolo!");
	            		return;
	            	}
	            	post=new Post(titolo,content, utenteLoggato, tipo, null);
	            	try {
	            		post.salvaPost();
	            		feed.getChildren().add(1, creaCardPost(post));
	            		showAlert(AlertType.INFORMATION, "Successo", "Post pubblicato!");
	            	} catch (SQLException ex) {
	            		showAlert(AlertType.ERROR, "Errore Database", ex.getMessage());
	            	}
	            	break;
            
	            case FOTO:
	            	String imgPath = (String) mediaBox.getUserData();
	            	if(imgPath==null) {    
	            		showAlert(AlertType.ERROR, "ERRORE","Seleziona prima un'immagine!  ");
	            		return;
	            	}else if(titolo.isBlank()){
	            		showAlert(AlertType.ERROR, "ERRORE","Scrivere un titolo!");
	            		return;
	            	}
					File imgFile = MediaManager.getMediaFile(imgPath);
					post=new Post(titolo,content, utenteLoggato, tipo, imgFile);
	            	try {
	            		post.salvaPost();
	            		feed.getChildren().add(1, creaCardPost(post));
	            		showAlert(AlertType.INFORMATION, "Successo", "Post pubblicato!");
	            	} catch (SQLException ex) {
	            		showAlert(AlertType.ERROR, "Errore Database", ex.getMessage());
	            	}
	            	break;
	            	
	            case VIDEO:
	            	String videoPath = (String) mediaBox.getUserData();
	            	if(videoPath==null) {    
	            		showAlert(AlertType.ERROR, "ERRORE","Seleziona prima un video! ");
	            		return;
	            	}else if(titolo.isBlank()){
	            		showAlert(AlertType.ERROR, "ERRORE","Scrivere un titolo!");
	            		return;
	            	}
					File videoFile = MediaManager.getMediaFile(videoPath);
					post=new Post(titolo,content, utenteLoggato, tipo, videoFile);
	            	try {
	            		post.salvaPost();
	            		feed.getChildren().add(1, creaCardPost(post));
	            		showAlert(AlertType.INFORMATION, "Successo", "Post pubblicato!");
	            	} catch (SQLException ex) {
	            		showAlert(AlertType.ERROR, "Errore Database", ex.getMessage());
	            	}
	            	break;
	            default:
            		showAlert(AlertType.ERROR, "ERRORE","Tipo di post non supportato  :) ");
            		return;
            	}
            
            //PULIZIA CAMPI
            postArea.clear();
            mediaBox.getChildren().clear();
            cbxTipo.setValue(TipoPost.TESTO);
            fldTitolo.clear();
          
           
            	
        	});
        
        HBox dashButtonBox = new HBox(10);
        dashButtonBox.setAlignment(Pos.CENTER_LEFT);
        dashButtonBox.getChildren().addAll(cbxTipo, publishBtn);
        
        dashboard.getChildren().addAll(dashTitle,fldTitolo, postArea,mediaBox, dashButtonBox);
        ScrollPane feedScroll=new ScrollPane(feed);
        feedScroll.setFitToWidth(true);
        feedScroll.getStyleClass().add("scroll-pane");
        feedScroll.setStyle("-fx-padding: 0; -fx-control-inner-background: " + ThemeManager.Colors.WHITE_CREAM + ";");
        
        // COMBINAZIONE FEED E DASHBOARD
        VBox feedArea = new VBox(20, dashboard, feedScroll);
        feedArea.setPadding(new javafx.geometry.Insets(15));
        feedArea.setPrefWidth(600);
        feedArea.setStyle("-fx-background-color: " + ThemeManager.Colors.WHITE_CREAM + ";");

        
        // --- LAYOUT PRINCIPALE ---
        HBox mainContent = new HBox(sidebarPane, feedArea);
        HBox.setHgrow(feedArea, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(mainContent);

        return root;
	}


    // --- ALERT PER ERRORE SUI POST ---
	private void showAlert(AlertType type, String title, String message) {
		Alert alert= new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
		
	}

	// --- METODO PER CREAZIONE CARD POST ---
	private VBox creaCardPost(Post post) {
	    
	    // --- CARD PER POST ---
	    VBox card = new VBox(10); 
	    card.setPadding(new javafx.geometry.Insets(18));
	    card.getStyleClass().add("post-card");

	    // HEADER CARD
	    HBox headerBox = new HBox(10);
	    headerBox.setAlignment(Pos.CENTER_LEFT);
	    
	    Label autore = new Label(post.getAutore().getUsername());
	    autore.getStyleClass().add("post-author");
	    
	    Label titolo=new Label(post.getTitolo());
	    titolo.getStyleClass().add("post-title");

	    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	    Label data = new Label(post.getDataCreazione().format(fmt));
	    data.getStyleClass().add("post-date");
	    
	    Region spacer = new Region();
	    javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
	    
	    // BOTTONE ARCHIVIO
	    Button btnArchive = new Button("⭐ Salva");
	    btnArchive.getStyleClass().add("save-button");
	    btnArchive.setOnAction(e -> {
	        //CONTROLLO SUL SALVATAGGIO
	        boolean exists = utenteLoggato.getArchivio().stream().anyMatch(p -> 
	            p.getTitolo().equals(post.getTitolo()) && 
	            p.getAutore().getUsername().equals(post.getAutore().getUsername())
	        );
	        
	        if (!exists) {
	            utenteLoggato.getArchivio().add(post);
	            btnArchive.setText("✓ Salvato");
	            btnArchive.getStyleClass().clear();
	            btnArchive.getStyleClass().add("save-button-saved");
	            btnArchive.setDisable(true);
	        } else {
	            showAlert(Alert.AlertType.WARNING, "Archivio", "Questo post è già nel tuo archivio.");
	        }
	    });

	    headerBox.getChildren().addAll(autore, data, spacer, btnArchive);
	    card.getChildren().addAll(headerBox, titolo);
	    
	    // CONTENUTO MEDIA E TESTO
	    if(post.getTipo() == TipoPost.TESTO) { 
	        Label contenuto = new Label(post.getContenuto());
	        contenuto.getStyleClass().add("post-content");
	        contenuto.setWrapText(true);
	        card.getChildren().add(contenuto);
	        
	    } else if (post.getTipo() == TipoPost.FOTO) {
	    	
	    	//GESTIONE FOTO
	        try {
	            File dirImg = post.getMedia();
	            Image img = new Image(dirImg.toURI().toString(), 550, 0, true, true);
	            ImageView imgView = new ImageView(img);
	            imgView.setFitWidth(550);
	            imgView.setPreserveRatio(true);
	            VBox immagine=new VBox(imgView);
	            immagine.setAlignment(Pos.CENTER);
	            card.getChildren().add(immagine);
	        } catch(Exception e) {
	            card.getChildren().add(new Label("Impossibile caricare l'immagine."));
	        }
	        
	        //GESTIONE TESTO SE PRESENTE
	        if(!post.getContenuto().isBlank()) {
	            Label contenuto = new Label(post.getContenuto());
	            contenuto.setWrapText(true);
	            card.getChildren().add(contenuto);
	        }

	    } else if (post.getTipo() == TipoPost.VIDEO) {
	    	
	    	//GESTIONE VIDEO
	        try {
	        	File dirVideo = post.getMedia();
	            Media video = new Media(dirVideo.toURI().toString());
	            MediaPlayer player = new MediaPlayer(video); 
	            MediaView view = new MediaView(player);
	       
	            view.setFitWidth(550); 
	            view.setPreserveRatio(true);
	            

	            // --- CREAZIONE BARRA DEI CONTROLLI ---
	            HBox controls = new HBox(10);
	            controls.setAlignment(Pos.CENTER);
	            controls.setPadding(new javafx.geometry.Insets(8));
	            controls.setStyle("-fx-background-color: " + ThemeManager.Colors.CREAM + "; -fx-background-radius: 5;");

	            // BOTTONE PLAY
	            Button btnPlay = new Button("▶ Play");
	            btnPlay.setStyle("-fx-padding: 6 16 6 16; -fx-font-size: 12; -fx-background-color: " + ThemeManager.Colors.MEDIUM_COFFEE + "; -fx-text-fill: " + ThemeManager.Colors.WHITE_CREAM + "; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
	            btnPlay.setOnAction(e -> {
	                if (player.getStatus() == MediaPlayer.Status.PLAYING) {
	                    player.pause();
	                    btnPlay.setText("▶ Play");
	                } else {
	                    player.play();
	                    btnPlay.setText("⏸ Pausa");
	                }
	            });

	            //SLIDER PER SCORRERE IL TEMPO
	            Slider timeSlider = new Slider();
	            timeSlider.setStyle("-fx-padding: 5;");
	            javafx.scene.layout.HBox.setHgrow(timeSlider, javafx.scene.layout.Priority.ALWAYS);
	            
	           
	            player.setOnReady(() -> {
	                timeSlider.setMax(video.getDuration().toSeconds());
	            });

	            
	            player.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
	                if (!timeSlider.isValueChanging()) {
	                    timeSlider.setValue(newTime.toSeconds());
	                }
	            });

	            // GESTIONE SPOSTAMENTO SU SLIDER
	            timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
	                if (timeSlider.isValueChanging()) {
	                    player.seek(Duration.seconds(newVal.doubleValue()));
	                }
	            });
	            
	            // GESTIONE CLICK SULLO SLIDER
	            timeSlider.setOnMouseClicked(e -> {
	                 player.seek(Duration.seconds(timeSlider.getValue()));
	            });

	            // SLIDER VOLUME
	            Label lblVol = new Label("🔊");
	            lblVol.setStyle("-fx-font-size: 12;");
	            Slider volSlider = new Slider(0, 1, 0.5); 
	            volSlider.setPrefWidth(80);
	            volSlider.setStyle("-fx-padding: 0;");
	            player.volumeProperty().bind(volSlider.valueProperty());

	            
	            controls.getChildren().addAll(btnPlay, timeSlider, lblVol, volSlider);

	            // UNIONE VIDEO E CONTROLLI
	            VBox videoBox = new VBox(5, view, controls);
	            videoBox.setAlignment(Pos.CENTER);
	            card.getChildren().add(videoBox);
	        } catch(Exception e) {
	            card.getChildren().add(new Label("Impossibile caricare il video."));
	        }
	        
	        //GESTIONE TESTO SE PRESENTE
	        if(!post.getContenuto().isBlank()) {
	            Label contenuto = new Label(post.getContenuto());
	            contenuto.getStyleClass().add("post-content");
	            contenuto.setWrapText(true);
	            card.getChildren().add(contenuto);
	        }
	    }
	    
	    card.getChildren().add(new Separator());

	    // --- AREA AZIONI (LIKE & COMMENTI) ---
	    HBox actions = new HBox(15);
	    actions.setAlignment(Pos.CENTER_LEFT);
	    actions.setStyle("-fx-padding: 5;");

	    //GESTIONE LIKE
	    Button btnLike = new Button();
	    Runnable updateLikeLabel = () -> {
	        int likes = post.getMiPiace().size();
	        boolean liked = post.getMiPiace().stream()
	                .anyMatch(u -> u.getUsername().equals(utenteLoggato.getUsername()));
	        btnLike.setText(liked ? "❤️ " + likes : "🤍 " + likes);
	        if (liked) {
	        	btnLike.getStyleClass().clear();
	        	btnLike.getStyleClass().add("like-button-active");
	        } else {
	        	btnLike.getStyleClass().clear();
	        	btnLike.getStyleClass().add("like-button");
	        }
	    };
	    updateLikeLabel.run(); 

	    btnLike.setOnAction(e -> {
	        boolean removed = post.getMiPiace().removeIf(u -> u.getUsername().equals(utenteLoggato.getUsername()));
	        if (!removed) {
	            post.getMiPiace().add(utenteLoggato);
	        }
	        updateLikeLabel.run();
	    });

	    actions.getChildren().add(btnLike);
	    card.getChildren().add(actions);

	    // --- SEZIONE COMMENTI ---
	    VBox commentsBox = new VBox(8);
	    commentsBox.getStyleClass().add("comments-box");
	    
	    Label lblCommenti = new Label("💬 Commenti");
	    lblCommenti.getStyleClass().add("comments-label");
	    
	    VBox commentsList = new VBox(6); 
	    commentsList.setStyle("-fx-padding: 8 0 8 0;");
	    
	    // FUNZIONE PER AGGIORNARE LA LISTA COMMENTI
	    Runnable refreshComments = () -> {
	        commentsList.getChildren().clear();
	        for (Commento c : post.getCommenti()) {
	            String autoreComm = (c.getUtente() != null) ? c.getUtente().getUsername() : "Anonimo";
	            Label l = new Label(autoreComm + ": " + c.getContenuto());
	            l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + ThemeManager.Colors.DARK_COFFEE + ";");
	            l.setWrapText(true);
	            commentsList.getChildren().add(l);
	        }
	    };
	    refreshComments.run(); 

	    // AREA COMMENTO
	    HBox newCommentBox = new HBox(5);
	    newCommentBox.setStyle("-fx-padding: 8 0 0 0;");
	    TextField txtCommento = new TextField();
	    txtCommento.setPromptText("Scrivi un commento...");
	    txtCommento.getStyleClass().add("comment-field");
	    txtCommento.setPrefHeight(32);
	    javafx.scene.layout.HBox.setHgrow(txtCommento, javafx.scene.layout.Priority.ALWAYS);
	    
	    Button btnInvia = new Button("Invia");
	    btnInvia.setStyle("-fx-padding: 8 20 8 20; -fx-font-size: 12; -fx-background-color: " + ThemeManager.Colors.MEDIUM_COFFEE + "; -fx-text-fill: " + ThemeManager.Colors.WHITE_CREAM + "; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
	    btnInvia.setPrefHeight(32);
	    btnInvia.setOnAction(e -> {
	        if (!txtCommento.getText().isBlank()) {
	            Commento nuovo = new Commento(utenteLoggato, post, txtCommento.getText(), LocalDateTime.now());
	            post.getCommenti().add(nuovo);
	            txtCommento.clear();
	            refreshComments.run();
	        }
	    });
	    
	    newCommentBox.getChildren().addAll(txtCommento, btnInvia);
	    commentsBox.getChildren().addAll(lblCommenti, commentsList, newCommentBox);
	    
	    card.getChildren().add(commentsBox);
	    
	    return card;
	}

	// --- METODO PER CARICARE POST DAL DATABASE ---
	private void caricaPostDalDatabase(VBox feed) {
	    try {
	        List<Post> posts = Post.caricaTuttiPost();
	        for (Post post : posts) {
	            feed.getChildren().add(creaCardPost(post));
	        }
	    } catch (SQLException e) {
	        showAlert(Alert.AlertType.ERROR, "Errore", "Errore nel caricamento dei post: " + e.getMessage());
	    }
	}
}
