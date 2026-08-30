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

import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
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

        // Rating (Stars)
        HBox starsBox = new HBox(5);
        starsBox.setAlignment(Pos.CENTER);

        final int[] currentVote = { 5 }; // Default 5
        Label[] stars = new Label[5];

        for (int i = 0; i < 5; i++) {
            final int starVal = i + 1;
            Label star = new Label("\u2B50");
            star.getStyleClass().add("star-icon");
            star.getStyleClass().add("text-score-5"); // Initial color

            // Hover: Preview
            star.setOnMouseEntered(mouseEvent -> {
                for (int j = 0; j < 5; j++) {
                    Label s = stars[j];
                    s.getStyleClass().removeAll("text-score-0", "text-score-1", "text-score-2", "text-score-3",
                            "text-score-4", "text-score-5");
                    if (j < starVal) {
                        s.getStyleClass().add("text-score-" + starVal);
                        s.setOpacity(1.0);
                    } else {
                        s.getStyleClass().add("text-score-0");
                        s.setOpacity(0.3);
                    }
                }
            });

            // Click: Select
            star.setOnMouseClicked(mouseEvent -> {
                currentVote[0] = starVal;
            });

            stars[i] = star;
            starsBox.getChildren().add(star);
        }

        // Mouse Exit: Revert to selected
        starsBox.setOnMouseExited(mouseEvent -> {
            int vote = currentVote[0];
            for (int j = 0; j < 5; j++) {
                Label s = stars[j];
                s.getStyleClass().removeAll("text-score-0", "text-score-1", "text-score-2", "text-score-3",
                        "text-score-4", "text-score-5");
                if (j < vote) {
                    s.getStyleClass().add("text-score-" + vote);
                    s.setOpacity(1.0);
                } else {
                    s.getStyleClass().add("text-score-0");
                    s.setOpacity(0.3);
                }
            }
        });

        // Review Text
        TextArea txtReview = new TextArea();
        txtReview.setPromptText("Scrivi una recensione (opzionale)...");
        txtReview.setWrapText(true);
        txtReview.setPrefHeight(100);

        Button btnSubmit = new Button("Invia Recensione");
        btnSubmit.getStyleClass().add("button-primary");

        btnSubmit.setOnAction(e -> {
            int voto = currentVote[0];
            String testo = txtReview.getText().trim();

            Recensione r = new Recensione();
            // Corretti i metodi per matchare il modello Recensione
            r.setPost(post);
            r.setAutore(utente);
            r.setVoto(voto);
            r.setTesto(testo);
            r.setDataCreazione(java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            try {
                postService.addReview(r);
                stage.close();
                if (onSuccess != null)
                    onSuccess.run();
            } catch (BusinessException ex) {
                DialogUtils.showError("Errore", ex.getMessage(), stage);
            }
        });

        root.getChildren().addAll(lblTitle, starsBox, txtReview, btnSubmit);

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(ReviewDialogManager.class.getResource("/style.css").toExternalForm());
        } catch (Exception e) {
        }
        stage.setScene(scene);
        stage.showAndWait();
    }

    /*
    -permette di cambiare testo e voto attuale mostrato.
    -chiama postService.updateReview(recensione).
    esegue refresh con onSuccess.
    */

    public static void showEditReviewDialog(
        Window ownerWindow,
        Recensione recensione,
        Runnable onSuccess) {

        Stage stage = new Stage();

        if (ownerWindow instanceof Stage) {
            stage.initOwner((Stage) ownerWindow);
        }

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Modifica Recensione");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(350);
        root.setStyle("-fx-background-color: #FFF8E1;");

        Label lblTitle = new Label("Modifica la tua Recensione");
        lblTitle.setStyle(
                "-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #3E2723;");

        // Rating
        HBox starsBox = new HBox(5);
        starsBox.setAlignment(Pos.CENTER);

        final int[] currentVote = { recensione.getVoto() };
        Label[] stars = new Label[5];

        for (int i = 0; i < 5; i++) {
            final int starVal = i + 1;

            Label star = new Label("\u2B50");
            star.getStyleClass().add("star-icon");

            if (i < currentVote[0]) {
                star.getStyleClass().add("text-score-" + currentVote[0]);
                star.setOpacity(1.0);
            } else {
                star.getStyleClass().add("text-score-0");
                star.setOpacity(0.3);
            }

            star.setOnMouseEntered(mouseEvent -> {
                for (int j = 0; j < 5; j++) {
                    Label s = stars[j];

                    s.getStyleClass().removeAll(
                            "text-score-0",
                            "text-score-1",
                            "text-score-2",
                            "text-score-3",
                            "text-score-4",
                            "text-score-5");

                    if (j < starVal) {
                        s.getStyleClass().add("text-score-" + starVal);
                        s.setOpacity(1.0);
                    } else {
                        s.getStyleClass().add("text-score-0");
                        s.setOpacity(0.3);
                    }
                }
            });

            star.setOnMouseClicked(mouseEvent -> {
                currentVote[0] = starVal;
            });

            stars[i] = star;
            starsBox.getChildren().add(star);
        }

        starsBox.setOnMouseExited(mouseEvent -> {
            int vote = currentVote[0];

            for (int j = 0; j < 5; j++) {
                Label s = stars[j];

                s.getStyleClass().removeAll(
                        "text-score-0",
                        "text-score-1",
                        "text-score-2",
                        "text-score-3",
                        "text-score-4",
                        "text-score-5");

                if (j < vote) {
                    s.getStyleClass().add("text-score-" + vote);
                    s.setOpacity(1.0);
                } else {
                    s.getStyleClass().add("text-score-0");
                    s.setOpacity(0.3);
                }
            }
        });

        TextArea txtReview = new TextArea();
        txtReview.setPromptText("Scrivi una recensione (opzionale)...");
        txtReview.setText(
                recensione.getTesto() != null ? recensione.getTesto() : "");
        txtReview.setWrapText(true);
        txtReview.setPrefHeight(100);

        Button btnSave = new Button("Salva Modifiche");
        btnSave.getStyleClass().add("button-primary");

        btnSave.setOnAction(e -> {
            recensione.setVoto(currentVote[0]);
            recensione.setTesto(txtReview.getText().trim());

            try {
                postService.updateReview(recensione);

                stage.close();

                if (onSuccess != null) {
                    onSuccess.run();
                }

            } catch (BusinessException ex) {
                DialogUtils.showError(
                        "Errore Modifica",
                        ex.getMessage(),
                        stage);
            }
        });

        root.getChildren().addAll(
                lblTitle,
                starsBox,
                txtReview,
                btnSave);

        Scene scene = new Scene(root);

        try {
            scene.getStylesheets().add(
                    ReviewDialogManager.class
                            .getResource("/style.css")
                            .toExternalForm());
        } catch (Exception e) {
        }

        stage.setScene(scene);
        stage.showAndWait();
    }
}
