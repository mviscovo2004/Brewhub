package it.univaq.brewhub.UI;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import it.univaq.brewhub.Commento;
import it.univaq.brewhub.Post;
import it.univaq.brewhub.Post.TipoPost;
import it.univaq.brewhub.Utente;
import javafx.geometry.Insets;
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
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;


public class HomeView {
	
	
    private final Stage stage;
    private final Utente utenteLoggato;
    private MediaPlayer mediaPlayer;

    
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
    	stage.setTitle("Home");
    	stage.centerOnScreen();
    	
    	// --- HEADER ---
    	HBox header = new HBox(20);
        header.setPadding(new Insets(10));
        header.setBackground(new Background(new BackgroundFill(Color.BLUE,null,null)));
        header.setAlignment(Pos.CENTER_LEFT);
    	
        //LOGO
    	Label logo = new Label("BrewHub");
        logo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        //BARRA DI RICERCA
        TextField searchField = new TextField();
        searchField.setPromptText("Cerca post o comunità...");
        searchField.setPrefWidth(300);

        //BOTTONI HEADER
        Button profileBtn = new Button(utenteLoggato.getUsername());
        Button logoutBtn = new Button("Logout");
        
        //METODO PER LOGOUT
        logoutBtn.setOnAction(e ->{
        	LoginView login= new LoginView(stage);
        	stage.getScene().setRoot(login.getView());
        });
        
        
        header.getChildren().addAll(logo, searchField, profileBtn, logoutBtn);
        
        // --- SIDEBAR ---
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #F6F6F6; -fx-border-color: #E0E0E0;");

        //TITOLO
        Label sbTitle = new Label("Categorie");
        sbTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        //CATEGORIE
        Button cat1 = new Button("Torrefattori");
        Button cat2 = new Button("Caffè");
        Button cat3 = new Button("Eventi");

        sidebar.getChildren().addAll(sbTitle, cat1, cat2, cat3);

        // --- FEED POST ---
        VBox feed = new VBox(15);
        feed.setPadding(new Insets(10));

        //MESSAGGIO DI BENVENUTO
        Label benvenuto=new Label("Benvenuto su Brewhub");
        feed.getChildren().add(benvenuto);

        // --- DASHBOARD ---
        
        VBox dashboard = new VBox(10);
        dashboard.setPadding(new Insets(10));
        dashboard.setStyle("-fx-border-color: #E0E0E0; -fx-background-color: #FAFAFA;");

        //TITOLO
        Label dashTitle = new Label("Crea un nuovo post");
        dashTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TextField fldTitolo=new TextField();
        fldTitolo.setPromptText("Inserisci titolo");
        TextArea postArea = new TextArea();
       
        //MENU A TENDINA - TIPO POST
        ChoiceBox<Post.TipoPost> cbxTipo=new ChoiceBox<TipoPost>();
		cbxTipo.setMaxSize(250, 2);
		cbxTipo.getItems().setAll(TipoPost.values());
		cbxTipo.setValue(TipoPost.TESTO);
		
		//BOX PER POST CON MEDIA (FOTO/VIDEO)
		HBox mediaBox = new HBox(10);
		
		//METODO PER CAMBIO UI E SELEZIONE FILE
		cbxTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			
		    mediaBox.getChildren().clear();
		    Label lblFile= new Label("Ancora nessun media selezionato...");
		    Button btnCaricaFile = new Button("Seleziona il media che vuoi caricare");
		    FileChooser fileChooser = new FileChooser();	
		    
		    if (newVal==TipoPost.FOTO) { //RAMO PER SELEZIONE FOTO
		    	
		    	btnCaricaFile.setOnAction(e ->{
		    		fileChooser.setTitle("Seleziona un'immagine");
		    		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini","*.jpg","*.jpeg","*.png")
		    		);
		    	
		    		File selectedFile = fileChooser.showOpenDialog(stage);
		    		
		    		if(selectedFile!=null) {
		    			mediaBox.setUserData(selectedFile);
		    			lblFile.setText(selectedFile.getName());
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
		    			mediaBox.setUserData(selectedFile);
		    			lblFile.setText(selectedFile.getName());
		    		}
		    	});
		    	
		    	mediaBox.getChildren().addAll(btnCaricaFile, lblFile);
		    }
		    
		});
    
		
        postArea.setPromptText("Scrivi qui il tuo post...");
        postArea.setPrefRowCount(4);
        

        
        
        Button publishBtn = new Button("Pubblica");
        publishBtn.setStyle("-fx-background-color: #FF4500; -fx-text-fill: white; -fx-font-weight: bold;");
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
	            	feed.getChildren().add(0, creaCardPost(post));
	            	break;
            
	            case FOTO:
	            	File img = (File) mediaBox.getUserData();
	            	if(img==null) {    
	            		showAlert(AlertType.ERROR, "ERRORE","Seleziona prima un'immagine!  ");
	            		return;
	            	}else if(titolo.isBlank()){
	            		showAlert(AlertType.ERROR, "ERRORE","Scrivere un titolo!");
	            		return;
	            	}
	            	post=new Post(titolo,content, utenteLoggato, tipo, img);
	            	feed.getChildren().add(0, creaCardPost(post));
	            	break;
	            	
	            case VIDEO:
	            	File video = (File) mediaBox.getUserData();
	            	if(video==null) {    
	            		showAlert(AlertType.ERROR, "ERRORE","Seleziona prima un video! ");
	            		return;
	            	}else if(titolo.isBlank()){
	            		showAlert(AlertType.ERROR, "ERRORE","Scrivere un titolo!");
	            		return;
	            	}
	            	post=new Post(titolo,content, utenteLoggato, tipo, video);
	            	feed.getChildren().add(0, creaCardPost(post));
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
        dashboard.getChildren().addAll(dashTitle,cbxTipo,fldTitolo, postArea,mediaBox, publishBtn);
        ScrollPane feedScroll=new ScrollPane(feed);
        feedScroll.setFitToWidth(true);
        
        // COMBINAZIONE FEED E DASHBOARD
        VBox feedArea = new VBox(20, dashboard, feedScroll);
        feedArea.setPadding(new Insets(10));
        feedArea.setPrefWidth(600);

        
        // --- LAYOUT PRINCIPALE ---
        HBox mainContent = new HBox(sidebar, feedArea);
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
	    card.setPadding(new Insets(15));
	    card.setStyle("-fx-background-color: white; -fx-border-color: #D3D3D3; -fx-background-radius: 5; -fx-border-radius: 5;");

	    // HEADER CARD
	    HBox headerBox = new HBox(10);
	    headerBox.setAlignment(Pos.CENTER_LEFT);
	    
	    Label autore = new Label(post.getAutore().getUsername());
	    autore.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
	    
	    Label titolo=new Label(post.getTitolo());

	    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	    Label data = new Label(post.getDataCreazione().format(fmt));
	    data.setStyle("-fx-text-fill: #808080; -fx-font-size: 12px;");
	    
	    Region spacer = new Region();
	    HBox.setHgrow(spacer, Priority.ALWAYS);
	    
	    // BOTTONE ARCHIVIO
	    Button btnArchive = new Button("Salva");
	    btnArchive.setStyle("-fx-background-color: transparent; -fx-text-fill: #2ecc71; -fx-border-color: #2ecc71; -fx-border-radius: 3;");
	    btnArchive.setOnAction(e -> {
	        //CONTROLLO SUL SALVATAGGIO
	        boolean exists = utenteLoggato.getArchivio().stream().anyMatch(p -> 
	            p.getTitolo().equals(post.getTitolo()) && 
	            p.getAutore().getUsername().equals(post.getAutore().getUsername())
	        );
	        
	        if (!exists) {
	            utenteLoggato.getArchivio().add(post);
	            btnArchive.setText("Salvato!");
	            btnArchive.setDisable(true);
	        } else {
	            showAlert(AlertType.WARNING, "Archivio", "Questo post è già nel tuo archivio.");
	        }
	    });

	    headerBox.getChildren().addAll(autore, spacer, data, btnArchive);
	    card.getChildren().addAll(headerBox,titolo);
	    
	    // CONTENUTO MEDIA E TESTO
	    if(post.getTipo() == TipoPost.TESTO) { 
	        Label contenuto = new Label(post.getContenuto());
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
	            controls.setPadding(new Insets(5));
	            controls.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5;");

	            // BOTTONE PLAY
	            Button btnPlay = new Button("Play");
	            btnPlay.setOnAction(e -> {
	                if (player.getStatus() == MediaPlayer.Status.PLAYING) {
	                    player.pause();
	                    btnPlay.setText("Play");
	                } else {
	                    player.play();
	                    btnPlay.setText("Pausa");
	                }
	            });

	            //SLIDER PER SCORRERE IL TEMPO
	            Slider timeSlider = new Slider();
	            HBox.setHgrow(timeSlider, Priority.ALWAYS); // Lo slider occupa tutto lo spazio disponibile
	            
	           
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
	            Label lblVol = new Label("Vol");
	            Slider volSlider = new Slider(0, 1, 0.5); 
	            volSlider.setPrefWidth(80);
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
	            contenuto.setWrapText(true);
	            card.getChildren().add(contenuto);
	        }
	    }
	    
	    card.getChildren().add(new Separator());

	    // --- AREA AZIONI (LIKE & COMMENTI) ---
	    HBox actions = new HBox(15);
	    actions.setAlignment(Pos.CENTER_LEFT);

	    //GESTIONE LIKE
	    Button btnLike = new Button();
	    Runnable updateLikeLabel = () -> {
	        int likes = post.getMiPiace().size();
	        boolean liked = post.getMiPiace().stream()
	                .anyMatch(u -> u.getUsername().equals(utenteLoggato.getUsername()));
	        btnLike.setText(liked ? "Non mi piace più (" + likes + ")" : "Mi piace (" + likes + ")");
	        btnLike.setStyle(liked ? "-fx-background-color: #e74c3c; -fx-text-fill: white;" : "");
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
	    VBox commentsBox = new VBox(5);
	    commentsBox.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 10;");
	    
	    Label lblCommenti = new Label("Commenti:");
	    lblCommenti.setStyle("-fx-font-weight: bold;");
	    
	    VBox commentsList = new VBox(5); 
	    
	    // FUNZIONE PER AGGIORNARE LA LISTA COMMENTI
	    Runnable refreshComments = () -> {
	        commentsList.getChildren().clear();
	        for (Commento c : post.getCommenti()) {
	            String autoreComm = (c.getUtente() != null) ? c.getUtente().getUsername() : "Anonimo";
	            Label l = new Label(autoreComm + ": " + c.getContenuto());
	            l.setWrapText(true);
	            commentsList.getChildren().add(l);
	        }
	    };
	    refreshComments.run(); 

	    // AREA COMMENTO
	    HBox newCommentBox = new HBox(5);
	    TextField txtCommento = new TextField();
	    txtCommento.setPromptText("Scrivi un commento...");
	    HBox.setHgrow(txtCommento, Priority.ALWAYS);
	    
	    Button btnInvia = new Button("Invia");
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
}
