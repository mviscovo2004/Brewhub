package it.univaq.brewhub.view.admin;

import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.business.CategoriaService;
import it.univaq.brewhub.model.Categoria;
import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.view.DialogUtils;
import it.univaq.brewhub.view.components.SidebarComponent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Vista per la gestione delle categorie da parte dell'amministratore.
 * Permette di visualizzare, aggiungere, modificare ed eliminare le categorie.
 */
public class CategoryManagementView extends VBox {

    private final Stage stage;
    private final CategoriaService categoriaService = CategoriaService.getInstance();
    private final SidebarComponent sidebar;

    /**
     * Costruisce la vista di gestione categorie.
     *
     * @param stage   Lo stage dell'applicazione.
     * @param sidebar Il componente sidebar per aggiornare le categorie al refresh.
     */
    public CategoryManagementView(Stage stage, SidebarComponent sidebar) {
        this.stage = stage;
        this.sidebar = sidebar;
        initUI();
    }

    /**
     * Inizializza l'interfaccia utente con la lista delle categorie e il form di
     * editing.
     */
    private void initUI() {
        this.setSpacing(20);
        this.setPadding(new Insets(20));
        this.setAlignment(Pos.CENTER);

        Label lblTitle = new Label("📂 Gestione Categorie");
        lblTitle.getStyleClass().add("section-title");
        this.getChildren().add(lblTitle);

        VBox content = new VBox(20);
        content.setMaxWidth(800);

        ListView<Categoria> listView = new ListView<>();
        listView.setPrefHeight(350);
        listView.getStyleClass().add("category-list-view");

        listView.setCellFactory(param -> new ListCell<Categoria>() {
            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                updateStyle(selected);
            }

            private void updateStyle(boolean selected) {
                if (getGraphic() == null)
                    return;
                getGraphic().getStyleClass().removeAll("selected-cell");
                if (selected) {
                    getGraphic().setStyle("-fx-background-color: #ffe082; -fx-background-radius: 8; -fx-padding: 10;");
                } else {
                    getGraphic().setStyle(
                            "-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 2,0,0,1);");
                }
            }

            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox cell = new HBox(15);
                    cell.setAlignment(Pos.CENTER_LEFT);
                    cell.getStyleClass().add("category-list-cell-container");

                    Label icon = new Label(item.getIcona() != null ? item.getIcona() : "📂");
                    icon.setStyle("-fx-font-size: 24px;");

                    Label name = new Label(item.getNome());
                    name.getStyleClass().add("text-bold-coffee");
                    name.setStyle("-fx-font-size: 16px;");

                    cell.getChildren().addAll(icon, name);
                    setGraphic(cell);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                    updateStyle(isSelected());
                }
            }
        });

        Runnable refreshList = () -> {
            try {
                List<Categoria> cats = categoriaService.getAllCategories();
                listView.getItems().setAll(cats);
                if (sidebar != null)
                    sidebar.refresh();
            } catch (Exception ex) {
                Log.error("Refresh cat error", ex);
            }
        };
        refreshList.run();

        VBox editorCard = new VBox(15);
        editorCard.getStyleClass().add("admin-card");

        Label editorTitle = new Label("Aggiungi / Modifica Categoria");
        editorTitle.getStyleClass().add("text-bold-coffee");
        editorTitle.setStyle("-fx-font-size: 18px;");

        TextField tfName = new TextField();
        tfName.setPromptText("Nome Categoria");
        tfName.getStyleClass().add("text-field");

        TextField tfIcon = new TextField();
        tfIcon.setPromptText("Emoji");
        tfIcon.setPrefWidth(100);
        tfIcon.getStyleClass().add("text-field");

        Button btnPickEmoji = new Button("😀");
        btnPickEmoji.getStyleClass().add("button-secondary");
        btnPickEmoji.setOnAction(e -> showEmojiPicker(btnPickEmoji, selectedEmoji -> tfIcon.setText(selectedEmoji)));

        HBox inputs = new HBox(10, tfName, tfIcon, btnPickEmoji);
        inputs.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tfName, Priority.ALWAYS);

        Button btnSaveRef = new Button("Aggiungi");
        btnSaveRef.getStyleClass().add("button-success");

        Button btnCancel = new Button("Annulla");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setVisible(false);

        HBox actions = new HBox(10, btnSaveRef, btnCancel);
        actions.setAlignment(Pos.CENTER_RIGHT);

        editorCard.getChildren().addAll(editorTitle, inputs, actions);

        HBox listActions = new HBox(10);
        listActions.setAlignment(Pos.CENTER_RIGHT);
        Button btnEdit = new Button("✏ Modifica");
        btnEdit.getStyleClass().add("button-primary");
        Button btnDelete = new Button("🗑 Elimina");
        btnDelete.getStyleClass().add("button-danger");
        listActions.getChildren().addAll(btnEdit, btnDelete);

        final Categoria[] editingCat = { null };

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSystem = newVal != null && categoriaService.isSystemCategory(newVal.getNome());
            btnEdit.setDisable(isSystem);
            btnDelete.setDisable(isSystem);
        });

        btnEdit.setOnAction(e -> {
            Categoria selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                editingCat[0] = selected;
                tfName.setText(selected.getNome());
                tfIcon.setText(selected.getIcona() != null ? selected.getIcona() : "");
                btnSaveRef.setText("Salva Modifiche");
                btnCancel.setVisible(true);
                editorTitle.setText("Modifica Categoria: " + selected.getNome());
            }
        });

        btnCancel.setOnAction(e -> {
            editingCat[0] = null;
            tfName.clear();
            tfIcon.clear();
            btnSaveRef.setText("Aggiungi");
            btnCancel.setVisible(false);
            editorTitle.setText("Aggiungi / Modifica Categoria");
            listView.getSelectionModel().clearSelection();
        });

        btnSaveRef.setOnAction(e -> {
            String name = tfName.getText().trim();
            String icon = tfIcon.getText().trim();
            if (name.isBlank()) {
                DialogUtils.showWarning("Attenzione", "Il nome è obbligatorio.", stage);
                return;
            }
            try {
                if (editingCat[0] == null) {
                    categoriaService.createCategory(new Categoria(name, icon));
                } else {
                    editingCat[0].setNome(name);
                    editingCat[0].setIcona(icon);
                    categoriaService.updateCategory(editingCat[0]);
                    btnCancel.fire();
                }
                tfName.clear();
                tfIcon.clear();
                refreshList.run();
            } catch (BusinessException ex) {
                refreshList.run();
                DialogUtils.showError("Errore", ex.getMessage(), stage);
            }
        });

        btnDelete.setOnAction(e -> {
            Categoria selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                boolean confirmed = DialogUtils.showConfirmation("Elimina Categoria",
                        "Eliminare " + selected.getNome() + "?", stage);
                if (confirmed) {
                    try {
                        categoriaService.deleteCategory(selected.getId());
                        if (editingCat[0] != null && editingCat[0].getId() == selected.getId()) {
                            btnCancel.fire();
                        }
                        refreshList.run();
                    } catch (BusinessException ex) {
                        DialogUtils.showError("Errore", "Impossibile eliminare: " + ex.getMessage(), stage);
                    }
                }
            }
        });

        content.getChildren().addAll(listView, listActions, editorCard);
        this.getChildren().add(content);
    }

    /**
     * Mostra un popup per la selezione di emoji.
     *
     * @param owner    Il nodo proprietario del popup.
     * @param onSelect Callback invocata quando un emoji viene selezionato.
     */
    private void showEmojiPicker(javafx.scene.Node owner, java.util.function.Consumer<String> onSelect) {
        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.setAutoHide(true);
        VBox box = new VBox(5);
        box.setStyle(
                "-fx-background-color: white; -fx-padding: 10; -fx-border-color: #ccc; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5,0,0,2);");
        Label lblT = new Label("Scegli Emoji");
        lblT.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        ScrollPane sp = new ScrollPane();
        sp.setPrefSize(250, 200);
        sp.setFitToWidth(true);
        FlowPane flow = new FlowPane();
        flow.setHgap(5);
        flow.setVgap(5);
        String[] emojis = {
                "☕", "🪵", "🍵", "🥨", "🍻", "🍷",
                "🥐", "🍰", "🍪", "🍕",
                "📂", "🗂", "📖", "📝", "📦",
                "🔥", "🎉", "🎁", "⭐", "💡",
                "🏠", "🏢", "🌱", "🌳", "🌍",
                "\uD83D\uDE00", "\uD83D\uDE03", "\uD83D\uDE0E", "\uD83E\uDD13", "\uD83D\uDC4D"
        };
        for (String e : emojis) {
            Button b = new Button(e);
            b.setStyle("-fx-font-size: 18px; -fx-background-color: transparent; -fx-cursor: hand;");
            b.setOnAction(ev -> {
                onSelect.accept(e);
                popup.hide();
            });
            flow.getChildren().add(b);
        }
        sp.setContent(flow);
        box.getChildren().addAll(lblT, sp);
        popup.getContent().add(box);
        javafx.geometry.Bounds bounds = owner.localToScreen(owner.getBoundsInLocal());
        popup.show(owner, bounds.getMinX(), bounds.getMaxY() + 5);
    }
}
