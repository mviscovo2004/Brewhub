package it.univaq.brewhub.view.utils;

import java.util.List;

import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.business.PostService;
import it.univaq.brewhub.business.CategoriaService;
import it.univaq.brewhub.model.Categoria;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.view.DialogUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PostEditDialog {

    private static final PostService postService = PostService.getInstance();

    private PostEditDialog() {
    }

    public static void show(Stage owner, Post post, Runnable onPostUpdated) {

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Modifica Post");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("modal-root");
        root.setPrefWidth(600);
        root.setPrefHeight(450);

        try {
            root.getStylesheets().add(
                    PostEditDialog.class.getResource("/style.css").toExternalForm());
        } catch (Exception ignored) {
        }

        Label lblTitle = new Label("Modifica Post");
        lblTitle.getStyleClass().add("modal-title");
        lblTitle.setMaxWidth(Double.MAX_VALUE);
        lblTitle.setAlignment(Pos.CENTER);

        root.setTop(lblTitle);

        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(20, 0, 20, 0));

        TextField txtTitle = new TextField();
        txtTitle.setPromptText("Titolo del post...");
        txtTitle.setText(post.getTitolo());
        txtTitle.getStyleClass().add("input-large");

        TextArea txtContent = new TextArea();
        txtContent.setPromptText("Contenuto del post...");
        txtContent.setText(post.getContenuto());
        txtContent.setWrapText(true);
        txtContent.setPrefRowCount(8);
        txtContent.getStyleClass().add("text-area");
        VBox.setVgrow(txtContent, Priority.ALWAYS);

        ComboBox<Categoria> comboCategory = new ComboBox<>();
        comboCategory.setMaxWidth(Double.MAX_VALUE);

        CategoriaService categoriaService = CategoriaService.getInstance();

        List<Categoria> categorie = categoriaService.getAllCategories();
        comboCategory.getItems().addAll(categorie);

        if (post.getCategoria() != null) {
            for (Categoria categoria : categorie) {
                if (categoria.getId() == post.getCategoria().getId()) {
                    comboCategory.setValue(categoria);
                    break;
                }
            }
        }

        comboCategory.setPromptText("Seleziona categoria");

        centerBox.getChildren().addAll(
                txtTitle,
                comboCategory,
                txtContent);

        root.setCenter(centerBox);

        HBox actionsBar = new HBox(15);
        actionsBar.setAlignment(Pos.CENTER_RIGHT);
        actionsBar.getStyleClass().add("dialog-actions");

        Button btnCancel = new Button("Annulla");
        btnCancel.getStyleClass().add("header-action-btn");

        btnCancel.setOnAction(e -> dialog.close());

        Button btnSave = new Button("Salva modifiche");
        btnSave.getStyleClass().add("button-primary");
        btnSave.setMinWidth(150);

        btnSave.setOnAction(e -> {

            String titolo = txtTitle.getText().trim();
            String contenuto = txtContent.getText().trim();

            if (titolo.isEmpty()) {
                DialogUtils.showWarning(
                        "Attenzione",
                        "Il titolo del post è obbligatorio.",
                        dialog);
                return;
            }

            post.setTitolo(titolo);
            post.setContenuto(contenuto);
            post.setCategoria(comboCategory.getValue());

            try {
                postService.updatePost(post);

                if (onPostUpdated != null) {
                    onPostUpdated.run();
                }

                dialog.close();

            } catch (BusinessException ex) {
                DialogUtils.showError(
                        "Errore Modifica",
                        ex.getMessage(),
                        dialog);
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        actionsBar.getChildren().addAll(
                spacer,
                btnCancel,
                btnSave);

        root.setBottom(actionsBar);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}