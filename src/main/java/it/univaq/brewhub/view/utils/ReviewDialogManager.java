package it.univaq.brewhub.view.utils;

import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.business.PostService;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.model.Recensione;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.view.DialogUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Gestisce il dialogo per lasciare una recensione (voto e testo) su un post.
 */
public class ReviewDialogManager {

    private static final PostService postService = PostService.getInstance();

    /**
     * Mostra il dialogo di recensione.
     *
     * @param ownerWindow Window proprietaria.
     * @param utente      L'utente che lascia la recensione.
     * @param post        Il post da recensire.
     * @param onSuccess   Callback eseguita dopo l'invio corretto della recensione.
     */
    public static void showAddReviewDialog(Window ownerWindow, Utente utente, Post post, Runnable onSuccess) {
        Stage stage = new Stage();
        if (ownerWindow instanceof Stage) {
            stage.initOwner((Stage) ownerWindow);
        }
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Lascia una Recensione");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(350);
        root.setStyle("-fx-background-color: #FFF8E1;");

        Label lblTitle = new Label("Valuta questo Post");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #3E2723;");

        // Rating
        Label lblRating = new Label("Voto: 3");
        Slider ratingSlider = new Slider(1, 5, 3);
        ratingSlider.setBlockIncrement(1);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setSnapToTicks(true);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.valueProperty().addListener((obs, oldV, newV) -> {
            lblRating.setText("Voto: " + newV.intValue());
        });

        // Review Text
        TextArea txtReview = new TextArea();
        txtReview.setPromptText("Scrivi una recensione (opzionale)...");
        txtReview.setWrapText(true);
        txtReview.setPrefHeight(100);

        Button btnSubmit = new Button("Invia Recensione");
        btnSubmit.getStyleClass().add("button-primary");

        btnSubmit.setOnAction(e -> {
            int voto = (int) ratingSlider.getValue();
            String testo = txtReview.getText().trim();

            Recensione r = new Recensione();
            // Corretti i metodi per matchare il modello Recensione
            r.setPost(post);
            r.setAutore(utente);
            r.setVoto(voto);
            r.setTesto(testo);
            // r.setDataCreazione viene impostata dal DB o Server di solito,
            // ma qui possiamo mettere manualmente se RecensioneDAO lo richiede.
            // RecensioneDAOImpl.create usa data corrente se non specificata?
            // Verifichiamo se serve. Per ora lasciamo null/vuota.

            try {
                postService.addReview(r);
                stage.close();
                if (onSuccess != null)
                    onSuccess.run();
            } catch (BusinessException ex) {
                DialogUtils.showError("Errore", ex.getMessage(), stage);
            }
        });

        root.getChildren().addAll(lblTitle, lblRating, ratingSlider, txtReview, btnSubmit);

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(ReviewDialogManager.class.getResource("/style.css").toExternalForm());
        } catch (Exception e) {
        }
        stage.setScene(scene);
        stage.showAndWait();
    }
}
