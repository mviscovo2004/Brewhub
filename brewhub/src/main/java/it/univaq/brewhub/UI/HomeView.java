package it.univaq.brewhub.UI;

import java.io.File;
import java.time.format.DateTimeFormatter;

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
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class HomeView {

    private final Stage stage;
    private final Utente utenteLoggato;

    public HomeView(Stage stage, Utente utenteLoggato) {
        this.stage = stage;
        this.utenteLoggato = utenteLoggato;
    }

    public Parent getView() {
    	stage.setWidth(1280);
    	stage.setHeight(720);
    	stage.setResizable(true);
    	stage.setMaximized(true);
    	stage.setTitle("Home");
    	stage.centerOnScreen();
    	
    	HBox header = new HBox(20);
        header.setPadding(new Insets(10));
        header.setBackground(new Background(new BackgroundFill(Color.BLUE,null,null)));
        header.setAlignment(Pos.CENTER_LEFT);
    	
    	Label logo = new Label("BrewHub");
        logo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField searchField = new TextField();
        searchField.setPromptText("Cerca post o comunità...");
        searchField.setPrefWidth(300);

        Button profileBtn = new Button(utenteLoggato.getUsername());
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e ->{
        	
        	LoginView login= new LoginView(stage);
        	stage.getScene().setRoot(login.getView());
  
       
        });
        
        header.getChildren().addAll(logo, searchField, profileBtn, logoutBtn);
        
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #F6F6F6; -fx-border-color: #E0E0E0;");

        Label sbTitle = new Label("Categorie");
        sbTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Button cat1 = new Button("Torrefattori");
        Button cat2 = new Button("Caffè");
        Button cat3 = new Button("Eventi");

        sidebar.getChildren().addAll(sbTitle, cat1, cat2, cat3);

        VBox feed = new VBox(15);
        feed.setPadding(new Insets(10));

        // Placeholder post
        feed.getChildren().add(new Label("Benvenuto su BrewHub!"));

        
        
        
        
        VBox dashboard = new VBox(10);
        dashboard.setPadding(new Insets(10));
        dashboard.setStyle("-fx-border-color: #E0E0E0; -fx-background-color: #FAFAFA;");

        Label dashLabel = new Label("Crea un nuovo post"); // Evidenziata come dashboard
        dashLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextArea postArea = new TextArea();
       
    //choicebox TIPI <3
        ChoiceBox<Post.TipoPost> cbxTipo=new ChoiceBox<TipoPost>();
		cbxTipo.setMaxSize(250, 2);
		cbxTipo.getItems().setAll(TipoPost.values());
		
		
	//BOX FOTO
		HBox mediaBox = new HBox(10);
		cbxTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
		    mediaBox.getChildren().clear();
		    Label lblFile= new Label("Ancora nessun media selezionato...");
		    Button btnCaricaFile = new Button("Seleziona il media che vuoi caricare");
		    FileChooser fileChooser = new FileChooser();	
		    if (newVal==TipoPost.FOTO) {
		    	
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
		    	
		    }else if(newVal==TipoPost.VIDEO) {
		    	btnCaricaFile.setOnAction(e ->{
		    		fileChooser.setTitle("Seleziona un'immagine");
		    		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video","*.mp4")
		    		);
		    	
		    		File selectedFile = fileChooser.showOpenDialog(stage);
		    		
		    		if(selectedFile!=null) {
		    			mediaBox.setUserData(selectedFile);
		    			lblFile.setText(selectedFile.getName());
		    		}
		    	});
		    	
		    	mediaBox.getChildren().addAll(btnCaricaFile, lblFile);
		    }else {
		    	
		    }
		    
		});
    
		
        postArea.setPromptText("Scrivi qui il tuo post...");
        postArea.setPrefRowCount(4);
        

        
        
        Button publishBtn = new Button("Pubblica");
        publishBtn.setStyle("-fx-background-color: #FF4500; -fx-text-fill: white; -fx-font-weight: bold;");
        publishBtn.setOnAction(e -> {
            String content = postArea.getText().trim();
            TipoPost tipo = cbxTipo.getValue();


            if (tipo == null) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Seleziona la tipologia del post.");
                return;
            }
            
            switch(tipo) {
	            case TESTO:
	            	if(content.isEmpty()) {
	            		showAlert(AlertType.ERROR, "ERRORE","il contenuto del post non puo essere vuoto :( ");
	            	return;
	            	}
	            	feed.getChildren().add(0, creaCardPost(new Post("abc",content, utenteLoggato, tipo, null)));
	            	break;
            
	            case FOTO:
	            	File img = (File) mediaBox.getUserData();
	            	if(img==null) {    
	            		showAlert(AlertType.ERROR, "ERRORE","seleziona prima un'immagine!  :) ");
	            		return;
	            	}
	            	feed.getChildren().add(0, creaCardPost(new Post("abc",content, utenteLoggato, tipo, img)));
	            	break;
	            	
	            case VIDEO:
	            	File video = (File) mediaBox.getUserData();
	            	if(video==null) {    
	            		showAlert(AlertType.ERROR, "ERRORE","seleziona prima un'immagine!  :) ");
	            		return;
	            	}
	            	feed.getChildren().add(0, creaCardPost(new Post("abc",content, utenteLoggato, tipo, video)));
	            	break;
	            default:
            		showAlert(AlertType.ERROR, "ERRORE","Tipo di post non supportato  :) ");
            		return;
            	}
            //pulizia dei campi
            postArea.clear();
            mediaBox.getChildren().clear();
            cbxTipo.setValue(null);
          
           
            	
        	});
        dashboard.getChildren().addAll(dashLabel,cbxTipo, postArea, publishBtn, mediaBox);

        //---------------------------
        // FEED + DASHBOARD combinati
        //---------------------------
        VBox feedArea = new VBox(20, dashboard, feed);
        feedArea.setPadding(new Insets(10));
        feedArea.setPrefWidth(600);

        //---------------------------
        // LAYOUT PRINCIPALE
        //---------------------------
        HBox mainContent = new HBox(sidebar, feedArea);
        HBox.setHgrow(feedArea, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(mainContent);

        return root;
	}



	private void showAlert(AlertType type, String title, String message) {
		Alert alert= new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
		
	}

	private VBox creaCardPost(Post post) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #D3D3D3;");

        Label autore = new Label("Autore: " + post.getAutore().getUsername());
        autore.setStyle("-fx-font-weight: bold;");


        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label data = new Label("Data Pubblicazione: " + post.getDataCreazione().format(fmt));
        
        //X TESTO
        if(post.getTipo() == TipoPost.TESTO) {
        	Label contenuto = new Label(post.getContenuto());
        	contenuto.setWrapText(true);
        	card.getChildren().add(contenuto);
        //X FOTO	
        }else if (post.getTipo() == TipoPost.FOTO) {
        	//sezione testo se presente
        	if(!post.getContenuto().isEmpty()) {
            	Label contenuto = new Label(post.getContenuto());
            	contenuto.setWrapText(true);
            	card.getChildren().add(contenuto);

        	}
        	
        	
        	//mostra img
        	try {
        		Image img = new Image(post.getImmagine().toURI().toString(), 400, 0, true, true);
        		ImageView iv = new ImageView(img);
        		card.getChildren().add(iv);
        	}catch(Exception ex) {
        		ex.printStackTrace();
        	}
        	}
        card.getChildren().addAll(autore, data);
        return card;
    }
}
