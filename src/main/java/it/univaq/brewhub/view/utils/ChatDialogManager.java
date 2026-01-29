package it.univaq.brewhub.view.utils;

import it.univaq.brewhub.business.ChatService;
import it.univaq.brewhub.business.UserService;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.view.DialogUtils;
import it.univaq.brewhub.view.model.ChatSession;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Gestisce i dialoghi relativi alla chat: creazione nuove conversazioni private
 * o di gruppo.
 */
public class ChatDialogManager {

    private static final UserService userService = UserService.getInstance();
    private static final ChatService chatService = ChatService.getInstance();

    /**
     * Mostra il dialogo principale per scegliere tra nuova chat privata o di
     * gruppo.
     *
     * @param owner     Lo stage proprietario.
     * @param onPrivate Callback eseguita se l'utente sceglie "Chat Privata".
     * @param onGroup   Callback eseguita se l'utente sceglie "Gruppo".
     */
    public static void showNewChatDialog(Stage owner, Runnable onPrivate, Runnable onGroup) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nuova Conversazione");
        try {
            dialog.getDialogPane().getStylesheets()
                    .add(ChatDialogManager.class.getResource("/style.css").toExternalForm());
        } catch (Exception e) {
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Node closeBtn = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        if (closeBtn != null)
            closeBtn.setVisible(false);

        VBox root = new VBox(25);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #FFFBF5; -fx-min-width: 400px;");

        Label title = new Label("Nuova Conversazione");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #3E2723;");
        Label subtitle = new Label("Scegli come vuoi comunicare");
        subtitle.setStyle("-fx-text-fill: #8D6E63; -fx-font-size: 14px;");

        HBox buttonsBox = new HBox(20);
        buttonsBox.setAlignment(Pos.CENTER);

        VBox btnPrivate = createOptionButton("👤", "Chat Privata", "Inizia una conversazione singola");
        btnPrivate.setOnMouseClicked(e -> {
            dialog.close();
            onPrivate.run();
        });

        VBox btnGroup = createOptionButton("👥", "Nuovo Gruppo", "Crea un gruppo con più amici");
        btnGroup.setOnMouseClicked(e -> {
            dialog.close();
            onGroup.run();
        });

        buttonsBox.getChildren().addAll(btnPrivate, btnGroup);

        Button btnCancel = new Button("Annulla");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #A1887F; -fx-underline: true; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());

        root.getChildren().addAll(title, subtitle, buttonsBox, btnCancel);
        dialog.getDialogPane().setContent(root);
        dialog.showAndWait();
    }

    /**
     * Crea un pulsante visuale per le opzioni di chat (card cliccabile).
     */
    private static VBox createOptionButton(String icon, String title, String desc) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setPrefWidth(180);
        box.setPrefHeight(160);
        box.setStyle(
                "-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-cursor: hand; -fx-border-color: #EFEBE9; -fx-border-radius: 15;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 40px;");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #5D4037; -fx-font-size: 16px;");
        Label descLbl = new Label(desc);
        descLbl.setWrapText(true);
        descLbl.setAlignment(Pos.CENTER);
        descLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        descLbl.setStyle("-fx-text-fill: #A1887F; -fx-font-size: 11px;");
        box.getChildren().addAll(iconLbl, titleLbl, descLbl);

        box.setOnMouseEntered(e -> box.setStyle(
                "-fx-background-color: #FFF8E1; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(93,64,55,0.2), 8, 0, 0, 4); -fx-cursor: hand; -fx-border-color: #D7CCC8; -fx-border-radius: 15; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        box.setOnMouseExited(e -> box.setStyle(
                "-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-cursor: hand; -fx-border-color: #EFEBE9; -fx-border-radius: 15; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
        return box;
    }

    /**
     * Mostra il dialogo per avviare una nuova chat privata con un utente.
     *
     * @param owner          Lo stage proprietario.
     * @param currentUser    L'utente corrente.
     * @param onUserSelected Callback invocata con l'utente selezionato.
     */
    public static void showNewPrivateChatDialog(Stage owner, Utente currentUser, Consumer<Utente> onUserSelected) {
        Stage stage = new Stage();
        stage.setTitle("Nuova Chat Privata");
        stage.initModality(Modality.APPLICATION_MODAL);
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setPrefWidth(420);
        root.setPrefHeight(450);
        root.setStyle("-fx-background-color: #FFFBF5;");

        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(0, 0, 10, 0));
        Label titleLbl = new Label("Seleziona Utente");
        titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3E2723;");
        TextField searchField = new TextField();
        searchField.setPromptText("Cerca per nome...");
        searchField.getStyleClass().add("text-field");
        searchField.setStyle("-fx-background-radius: 20; -fx-padding: 8;");
        topBox.getChildren().addAll(titleLbl, searchField);
        root.setTop(topBox);

        ListView<Utente> userList = new ListView<>();
        userList.getStyleClass().add("list-view");
        userList.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #EFEBE9; -fx-border-radius: 8;");
        Label placeholder = new Label("Caricamento...");
        placeholder.setStyle("-fx-text-fill: #A1887F; -fx-font-style: italic;");
        userList.setPlaceholder(placeholder);

        StackPane listContainer = new StackPane(userList);
        listContainer.setPadding(new Insets(0));
        VBox.setVgrow(listContainer, Priority.ALWAYS);
        root.setCenter(listContainer);

        Button btnCancel = new Button("Annulla");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setOnAction(e -> stage.close());
        HBox bottomBox = new HBox(btnCancel);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(bottomBox);

        Runnable loadInitial = () -> {
            try {
                placeholder.setText("Caricamento suggerimenti...");
                List<Utente> res = userService.getTopActiveUsers(30);
                res.removeIf(u -> u.getUsername().equals(currentUser.getUsername()));
                userList.getItems().setAll(res);
                if (res.isEmpty())
                    placeholder.setText("Nessun utente trovato.");
            } catch (Exception ex) {
                ex.printStackTrace();
                placeholder.setText("Errore inizializzazione: " + ex.getMessage());
            }
        };

        Service<List<Utente>> searchService = new Service<>() {
            @Override
            protected Task<List<Utente>> createTask() {
                final String query = searchField.getText() == null ? "" : searchField.getText().trim();
                return new Task<>() {
                    @Override
                    protected List<Utente> call() throws Exception {
                        List<Utente> res;
                        if (query.isEmpty()) {
                            res = userService.getTopActiveUsers(30);
                        } else {
                            res = userService.searchUsers(query);
                        }
                        res.removeIf(u -> u.getUsername().equals(currentUser.getUsername()));
                        return res;
                    }
                };
            }
        };
        searchService.setOnSucceeded(e -> {
            userList.getItems().setAll(searchService.getValue());
            if (userList.getItems().isEmpty()) {
                if (searchField.getText() == null || searchField.getText().isEmpty())
                    placeholder.setText("Nessun utente suggerito trovato.");
                else
                    placeholder.setText("Nessun utente trovato per '" + searchField.getText() + "'");
            }
        });
        searchService.setOnFailed(e -> placeholder.setText("Errore ricerca"));

        searchField.textProperty().addListener((obs, old, val) -> {
            searchService.cancel();
            searchService.restart();
        });

        userList.setCellFactory(param -> new ListCell<>() {
            private final Circle avatar = new Circle(16);
            private final Label initial = new Label();
            private final StackPane avatarStack = new StackPane(avatar, initial);
            private final Label name = new Label();
            private final Label nameFull = new Label();
            private final VBox infoBox = new VBox(0, name, nameFull);
            private final Button btnStart = new Button("➤");
            private final Region spacer = new Region();
            private final HBox rootBox = new HBox(10, avatarStack, infoBox, spacer, btnStart);
            {
                avatar.setFill(javafx.scene.paint.Color.web("#8D6E63"));
                initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
                name.setStyle("-fx-font-weight: bold; -fx-text-fill: #3E2723; -fx-font-size: 13px;");
                nameFull.setStyle("-fx-text-fill: #8D6E63; -fx-font-size: 11px;");
                btnStart.getStyleClass().add("button-primary");
                btnStart.setStyle(
                        "-fx-background-radius: 50; -fx-min-width: 28px; -fx-min-height: 28px; -fx-padding: 0; -fx-font-size: 11px;");
                HBox.setHgrow(spacer, Priority.ALWAYS);
                rootBox.setAlignment(Pos.CENTER_LEFT);
                rootBox.setPadding(new Insets(4, 8, 4, 8));
            }

            @Override
            protected void updateItem(Utente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    initial.setText(item.getUsername().substring(0, 1).toUpperCase());
                    name.setText("@" + item.getUsername());
                    nameFull.setText(item.getNome() + " " + item.getCognome());
                    btnStart.setOnAction(e -> {
                        onUserSelected.accept(item);
                        stage.close();
                    });
                    setOnMouseEntered(e -> setStyle("-fx-background-color: #FFF8E1;"));
                    setOnMouseExited(e -> setStyle("-fx-background-color: transparent;"));
                    setGraphic(rootBox);
                }
            }
        });

        userList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && userList.getSelectionModel().getSelectedItem() != null) {
                onUserSelected.accept(userList.getSelectionModel().getSelectedItem());
                stage.close();
            }
        });

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(ChatDialogManager.class.getResource("/style.css").toExternalForm());
        } catch (Exception e) {
        }
        stage.setScene(scene);
        stage.setOnShown(e -> userList.requestFocus());
        stage.show();
        loadInitial.run();
    }

    /**
     * Mostra il dialogo per creare un nuovo gruppo di chat.
     *
     * @param owner          Lo stage proprietario.
     * @param currentUser    L'utente corrente (creatore).
     * @param onGroupCreated Callback invocata con la sessione di gruppo creata.
     */
    public static void showNewGroupChatDialog(Stage owner, Utente currentUser, Consumer<ChatSession> onGroupCreated) {
        Stage stage = new Stage();
        stage.setTitle("Crea Nuovo Gruppo");
        stage.initModality(Modality.APPLICATION_MODAL);
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setPrefWidth(380);
        root.setPrefHeight(450);
        root.setStyle("-fx-background-color: #FFFBF5;");

        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(0, 0, 10, 0));
        Label titleLbl = new Label("Crea Gruppo");
        titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3E2723;");
        TextField nameField = new TextField();
        nameField.setPromptText("Nome del gruppo");
        nameField.getStyleClass().add("text-field");
        nameField.setStyle("-fx-background-radius: 20; -fx-padding: 8;");
        Label lblPart = new Label("Seleziona Partecipanti");
        lblPart.setStyle("-fx-font-weight: bold; -fx-text-fill: #5D4037; -fx-font-size: 12px;");
        TextField searchField = new TextField();
        searchField.setPromptText("Cerca utenti...");
        searchField.getStyleClass().add("text-field");
        searchField.setStyle("-fx-font-size: 11px; -fx-background-radius: 15; -fx-padding: 5;");
        topBox.getChildren().addAll(titleLbl, nameField, lblPart, searchField);
        root.setTop(topBox);

        ListView<Utente> userList = new ListView<>();
        userList.getStyleClass().add("list-view");
        userList.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #EFEBE9; -fx-border-radius: 8;");
        Label placeholder = new Label("Caricamento...");
        placeholder.setStyle("-fx-text-fill: #A1887F; -fx-font-style: italic;");
        userList.setPlaceholder(placeholder);

        StackPane listContainer = new StackPane(userList);
        listContainer.setPadding(new Insets(0));
        VBox.setVgrow(listContainer, Priority.ALWAYS);
        root.setCenter(listContainer);

        Button btnCreate = new Button("Crea");
        btnCreate.getStyleClass().add("button-primary");
        Button btnCancel = new Button("Annulla");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setOnAction(e -> stage.close());
        HBox bottomBox = new HBox(10, btnCancel, btnCreate);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(bottomBox);

        Set<String> selectedUsernames = new HashSet<>();

        Service<List<Utente>> searchService = new Service<>() {
            @Override
            protected Task<List<Utente>> createTask() {
                final String query = searchField.getText() == null ? "" : searchField.getText().trim();
                return new Task<>() {
                    @Override
                    protected List<Utente> call() throws Exception {
                        List<Utente> res;
                        if (query.isEmpty()) {
                            res = userService.getTopActiveUsers(30);
                        } else {
                            res = userService.searchUsers(query);
                        }
                        res.removeIf(u -> u.getUsername().equals(currentUser.getUsername()));
                        return res;
                    }
                };
            }
        };
        searchService.setOnSucceeded(e -> {
            userList.getItems().setAll(searchService.getValue());
            if (userList.getItems().isEmpty())
                placeholder.setText("Nessun utente trovato");
        });
        searchService.setOnFailed(e -> placeholder.setText("Errore caricamento"));
        searchService.restart();

        searchField.textProperty().addListener((o, old, v) -> {
            searchService.cancel();
            searchService.restart();
        });

        userList.setCellFactory(param -> new ListCell<>() {
            private final Circle avatar = new Circle(16);
            private final Label initial = new Label();
            private final StackPane avatarStack = new StackPane(avatar, initial);
            private final Label name = new Label();
            private final Label nameFull = new Label();
            private final VBox infoBox = new VBox(0, name, nameFull);
            private final Button btnToggle = new Button();
            private final Region spacer = new Region();
            private final HBox rootBox = new HBox(10, avatarStack, infoBox, spacer, btnToggle);
            {
                avatar.setFill(javafx.scene.paint.Color.web("#8D6E63"));
                initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
                name.setStyle("-fx-font-weight: bold; -fx-text-fill: #3E2723; -fx-font-size: 13px;");
                nameFull.setStyle("-fx-text-fill: #8D6E63; -fx-font-size: 11px;");
                HBox.setHgrow(spacer, Priority.ALWAYS);
                rootBox.setAlignment(Pos.CENTER_LEFT);
                rootBox.setPadding(new Insets(4, 8, 4, 8));
            }

            @Override
            protected void updateItem(Utente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    initial.setText(item.getUsername().substring(0, 1).toUpperCase());
                    name.setText("@" + item.getUsername());
                    nameFull.setText(item.getNome() + " " + item.getCognome());
                    boolean isSelected = selectedUsernames.contains(item.getUsername());
                    updateBtn(isSelected);
                    btnToggle.setOnAction(e -> {
                        boolean sel = !selectedUsernames.contains(item.getUsername());
                        if (sel)
                            selectedUsernames.add(item.getUsername());
                        else
                            selectedUsernames.remove(item.getUsername());
                        updateBtn(sel);
                        userList.refresh();
                    });
                    setOnMouseEntered(e -> setStyle("-fx-background-color: #FFF8E1;"));
                    setOnMouseExited(e -> setStyle("-fx-background-color: transparent;"));
                    setGraphic(rootBox);
                }
            }

            private void updateBtn(boolean sel) {
                if (sel) {
                    btnToggle.setText("\u2713");
                    btnToggle.getStyleClass().remove("button-primary");
                    btnToggle.setStyle(
                            "-fx-background-color: #6B8E23; -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 28px; -fx-min-height: 28px; -fx-font-weight: bold; -fx-font-size: 12px;");
                } else {
                    btnToggle.setText("+");
                    if (!btnToggle.getStyleClass().contains("button-primary"))
                        btnToggle.getStyleClass().add("button-primary");
                    btnToggle.setStyle(
                            "-fx-background-radius: 50; -fx-min-width: 28px; -fx-min-height: 28px; -fx-padding: 0; -fx-font-size: 12px;");
                }
            }
        });

        btnCreate.setOnAction(e -> {
            String name = nameField.getText();
            if (name == null || name.isBlank()) {
                DialogUtils.showWarning("Attenzione", "Inserisci un nome per il gruppo.", stage);
                return;
            }
            if (selectedUsernames.isEmpty()) {
                DialogUtils.showWarning("Attenzione", "Seleziona almeno un partecipante.", stage);
                return;
            }
            try {
                int gid = chatService.createGroup(name, currentUser.getUsername(), new ArrayList<>(selectedUsernames));
                if (gid > 0) {
                    onGroupCreated.accept(ChatSession.group(gid, name));
                    stage.close();
                } else {
                    DialogUtils.showError("Errore", "Errore creazione gruppo.", stage);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                DialogUtils.showError("Errore", "Errore DB: " + ex.getMessage(), stage);
            }
        });

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(ChatDialogManager.class.getResource("/style.css").toExternalForm());
        } catch (Exception e) {
        }
        stage.setScene(scene);
        stage.setOnShown(e -> userList.requestFocus());
        stage.show();
    }
}
