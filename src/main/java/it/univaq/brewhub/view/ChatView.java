package it.univaq.brewhub.view;

import it.univaq.brewhub.model.Gruppo;
import it.univaq.brewhub.model.Messaggio;
import it.univaq.brewhub.model.Utente;

import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.view.components.PostCard;
import it.univaq.brewhub.business.ChatService;
import it.univaq.brewhub.business.UserService;
import it.univaq.brewhub.business.PostService;
import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.utility.MediaManager;
import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce la vista della messaggistica (Chat).
 *
 * Permette agli utenti di visualizzare le conversazioni attive (private e
 * gruppi),
 * inviare nuovi messaggi, creare gruppi e gestire le interazioni in chat.
 *
 */
public class ChatView {

    private final Stage stage;
    private final Utente currentUser;
    private ChatSession activeSession;
    private final ChatService chatService = ChatService.getInstance();
    private final UserService userService = UserService.getInstance();
    private final PostService postService = PostService.getInstance();

    private VBox messageContainer;
    private ScrollPane chatScroll;
    private ListView<ChatSession> privateChatList;
    private ListView<ChatSession> groupChatList;
    private Label lblChatUser;
    private HBox inputArea;

    /**
     * Classe interna per rappresentare una sessione di chat (Utente o Gruppo).
     */
    public static class ChatSession {
        private String name;
        private boolean isGroup;
        private int groupId;
        private String username;

        /** Crea una sessione per una chat privata. */
        public static ChatSession user(String username) {
            ChatSession s = new ChatSession();
            s.name = username;
            s.isGroup = false;
            s.username = username;
            s.groupId = -1;
            return s;
        }

        /** Crea una sessione per una chat di gruppo. */
        public static ChatSession group(int id, String name) {
            ChatSession s = new ChatSession();
            s.name = name;
            s.isGroup = true;
            s.username = null;
            s.groupId = id;
            return s;
        }

        public String getName() {
            return name;
        }

        public boolean isGroup() {
            return isGroup;
        }

        public String getUsername() {
            return username;
        }

        public int getGroupId() {
            return groupId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ChatSession that = (ChatSession) o;
            if (isGroup != that.isGroup)
                return false;
            if (isGroup)
                return groupId == that.groupId;
            return username != null ? username.equals(that.username) : that.username == null;
        }
    }

    /**
     * Cella personalizzata per la lista delle conversazioni.
     */
    private class ChatListCell extends ListCell<ChatSession> {
        public ChatListCell() {
            getStyleClass().add("chat-conversation-item");
        }

        @Override
        public void updateSelected(boolean selected) {
            super.updateSelected(selected);
            if (getGraphic() != null) {
                if (selected) {
                    getGraphic().setStyle("-fx-background-color: #D7CCC8; -fx-background-radius: 10;");
                } else {
                    getGraphic().setStyle("-fx-background-color: transparent; -fx-background-radius: 10;");
                }
            }
        }

        @Override
        protected void updateItem(ChatSession item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
                setContextMenu(null);
            } else {
                HBox cell = new HBox(12);
                cell.setAlignment(Pos.CENTER_LEFT);
                cell.setPadding(new Insets(8));
                Circle avatar = new Circle(20);
                avatar.setFill(javafx.scene.paint.Color.web(item.isGroup() ? "#5D4037" : "#8D6E63"));
                String initialChar = item.getName().length() > 0 ? item.getName().substring(0, 1).toUpperCase()
                        : "?";
                Label initial = new Label(initialChar);
                initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                StackPane avatarStack = new StackPane(avatar, initial);
                String displayName = item.isGroup() ? item.getName() : "@" + item.getName();
                Label name = new Label(displayName);
                name.setStyle("-fx-font-weight: bold; -fx-text-fill: #3E2723; -fx-font-size: 14px;");
                cell.getChildren().addAll(avatarStack, name);
                setGraphic(cell);
                setText(null);
                if (isSelected()) {
                    cell.setStyle("-fx-background-color: #D7CCC8; -fx-background-radius: 10;");
                } else {
                    cell.setStyle("-fx-background-color: transparent; -fx-background-radius: 10;");
                }
                ContextMenu cm = new ContextMenu();
                if (item.isGroup()) {
                    MenuItem renameItem = new MenuItem("Rinomina Gruppo");
                    renameItem.setOnAction(e -> handleRenameGroup(item));
                    MenuItem leaveItem = new MenuItem("Abbandona Gruppo");
                    leaveItem.setOnAction(e -> handleLeaveGroup(item));
                    MenuItem deleteItem = new MenuItem("Elimina Gruppo");
                    deleteItem.setOnAction(e -> handleDeleteGroup(item));
                    cm.getItems().addAll(renameItem, leaveItem, deleteItem);
                } else {
                    MenuItem deleteItem = new MenuItem("Elimina Conversazione");
                    deleteItem.setOnAction(e -> handleDeletePrivateChat(item));
                    cm.getItems().add(deleteItem);
                }
                setContextMenu(cm);
            }
        }
    }

    /**
     * Costruttore della vista Chat.
     * 
     * @param stage          Lo stage principale.
     * @param currentUser    L'utente loggato.
     * @param activeChatUser Username opzionale per aprire direttamente una chat
     *                       (può essere null).
     */
    public ChatView(Stage stage, Utente currentUser, String activeChatUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        if (activeChatUser != null) {
            this.activeSession = ChatSession.user(activeChatUser);
        }
    }

    /**
     * Costruisce e restituisce l'interfaccia grafica della Chat.
     * 
     * @return Il nodo {@link Parent} root.
     */
    public Parent getView() {
        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(280);
        sidebar.getStyleClass().add("chat-sidebar");
        sidebar.setPadding(new Insets(15));
        HBox sidebarHeader = new HBox(10);
        sidebarHeader.setAlignment(Pos.CENTER_LEFT);
        Button btnBack = new Button("\u2190");
        btnBack.getStyleClass().add("button-secondary");
        btnBack.setStyle("-fx-padding: 5 10; -fx-font-size: 14px;");
        btnBack.setOnAction(e -> {
            HomeView hv = new HomeView(stage, currentUser);
            stage.getScene().setRoot(hv.getView());
        });
        Label sidebarTitle = new Label("Messaggi");
        sidebarTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3E2723;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnNew = new Button("+");
        btnNew.getStyleClass().add("button-primary");
        btnNew.setStyle("-fx-font-size: 16px; -fx-padding: 2 8; -fx-min-width: 30px;");
        btnNew.setTooltip(new Tooltip("Nuova chat"));
        btnNew.setOnAction(e -> showNewChatDialog());
        sidebarHeader.getChildren().addAll(btnBack, sidebarTitle, spacer, btnNew);
        // Lista Sidebar
        // Sezione Chat Private
        Label lblPrivate = new Label("CHAT PRIVATE");
        lblPrivate
                .setStyle("-fx-font-weight: bold; -fx-text-fill: #8D6E63; -fx-font-size: 11px; -fx-padding: 10 0 5 0;");
        privateChatList = new ListView<>();
        privateChatList.getStyleClass().add("chat-conversation-list");
        privateChatList.setCellFactory(param -> new ChatListCell());
        // Sezione Gruppi
        Label lblGroups = new Label("GRUPPI");
        lblGroups
                .setStyle("-fx-font-weight: bold; -fx-text-fill: #8D6E63; -fx-font-size: 11px; -fx-padding: 15 0 5 0;");
        groupChatList = new ListView<>();
        groupChatList.getStyleClass().add("chat-conversation-list");
        groupChatList.setCellFactory(param -> new ChatListCell());
        loadConversations();
        sidebar.getChildren().addAll(sidebarHeader, lblPrivate, privateChatList, lblGroups, groupChatList);
        VBox.setVgrow(privateChatList, Priority.ALWAYS);
        VBox.setVgrow(groupChatList, Priority.ALWAYS);
        // --- Area Chat Principale ---
        VBox chatArea = new VBox();
        chatArea.getStyleClass().add("chat-area");
        // Header
        HBox chatHeader = new HBox(15);
        chatHeader.getStyleClass().add("chat-header");
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        lblChatUser = new Label();
        lblChatUser.getStyleClass().add("chat-header-title");
        chatHeader.getChildren().add(lblChatUser);
        // Scroll
        messageContainer = new VBox(15);
        messageContainer.setPadding(new Insets(20));
        chatScroll = new ScrollPane(messageContainer);
        chatScroll.setFitToWidth(true);
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScroll.getStyleClass().add("scroll-pane");
        chatScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(chatScroll, Priority.ALWAYS);
        // Input
        inputArea = new HBox(15);
        inputArea.getStyleClass().add("chat-input-area");
        inputArea.setAlignment(Pos.CENTER);
        TextField msgField = new TextField();
        msgField.setPromptText("Scrivi un messaggio...");
        msgField.getStyleClass().add("text-field");
        msgField.setStyle("-fx-background-radius: 20; -fx-padding: 10 15;");
        HBox.setHgrow(msgField, Priority.ALWAYS);
        Button btnSend = new Button("\u27A4");
        btnSend.getStyleClass().add("button-primary");
        btnSend.setStyle(
                "-fx-background-radius: 50; -fx-min-width: 40px; -fx-min-height: 40px; -fx-padding: 0; -fx-font-size: 18px;");
        Runnable sendAction = () -> {
            String text = msgField.getText();
            if (text == null || text.isBlank() || activeSession == null)
                return;
            Messaggio m = new Messaggio();
            m.setSender(currentUser.getUsername());
            m.setContenuto(text.trim());
            m.setTimestamp(LocalDateTime.now().toString());
            m.setLetto(false);
            if (activeSession.isGroup()) {
                m.setIdGruppo(activeSession.getGroupId());
                // Receiver è null o gestito dal trigger
            } else {
                m.setReceiver(activeSession.getUsername());
            }
            try {
                chatService.sendMessage(m);
                msgField.clear();
                loadChat(activeSession);
            } catch (BusinessException e) {
                DialogUtils.showError("Errore Invio", e.getMessage(), stage);
            }
        };
        btnSend.setOnAction(e -> sendAction.run());
        msgField.setOnAction(e -> sendAction.run());
        inputArea.getChildren().addAll(msgField, btnSend);
        chatArea.getChildren().addAll(chatHeader, chatScroll, inputArea);
        // Logica Iniziale
        if (activeSession != null) {
            updateHeader(activeSession);
            // Seleziona se presente
            if (activeSession.isGroup()) {
                if (groupChatList.getItems().contains(activeSession)) {
                    groupChatList.getSelectionModel().select(activeSession);
                } else {
                    loadChat(activeSession);
                }
            } else {
                if (privateChatList.getItems().contains(activeSession)) {
                    privateChatList.getSelectionModel().select(activeSession);
                } else {
                    loadChat(activeSession);
                }
            }
        } else {
            lblChatUser.setText("Seleziona una conversazione");
            inputArea.setDisable(true);
            messageContainer.getChildren().add(createEmptyPlaceholder());
        }
        // Listener per sincronizzazione selezione
        privateChatList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                groupChatList.getSelectionModel().clearSelection();
                activeSession = newVal;
                updateHeader(newVal);
                inputArea.setDisable(false);
                loadChat(newVal);
            }
        });
        groupChatList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                privateChatList.getSelectionModel().clearSelection();
                activeSession = newVal;
                updateHeader(newVal);
                inputArea.setDisable(false);
                loadChat(newVal);
            }
        });
        root.setLeft(sidebar);
        root.setCenter(chatArea);
        return root;
    }

    private void updateHeader(ChatSession session) {
        if (session.isGroup()) {
            lblChatUser.setText("Gruppo: " + session.getName());
        } else {
            lblChatUser.setText("Conversazione con @" + session.getName());
        }
    }

    private Node createEmptyPlaceholder() {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(50));
        Label icon = new Label("\uD83D\uDCAC");
        icon.setStyle("-fx-font-size: 64px; -fx-text-fill: #D7CCC8;");
        Label text = new Label("Scegli una chat o creane una nuova");
        text.setStyle("-fx-font-size: 18px; -fx-text-fill: #A1887F; -fx-font-weight: bold;");
        box.getChildren().addAll(icon, text);
        return box;
    }

    private void loadConversations() {
        // 1. Gruppi
        List<ChatSession> groupItems = new ArrayList<>();
        List<Gruppo> gruppi = chatService.getUserGroups(currentUser.getUsername());
        for (Gruppo g : gruppi) {
            groupItems.add(ChatSession.group(g.getId(), g.getNome()));
        }
        groupChatList.getItems().setAll(groupItems);
        // 2. Chat Private
        List<ChatSession> privateItems = new ArrayList<>();
        List<String> users = chatService.getActiveConversations(currentUser.getUsername());
        for (String u : users) {
            privateItems.add(ChatSession.user(u));
        }
        privateChatList.getItems().setAll(privateItems);
        // Aggiungi solo se non presente ed è attivo (es. 1-to-1 appena iniziata)
        if (activeSession != null) {
            if (activeSession.isGroup()) {
                if (!groupChatList.getItems().contains(activeSession))
                    groupChatList.getItems().add(0, activeSession);
                groupChatList.getSelectionModel().select(activeSession);
            } else {
                if (!privateChatList.getItems().contains(activeSession))
                    privateChatList.getItems().add(0, activeSession);
                privateChatList.getSelectionModel().select(activeSession);
            }
        }
    }

    private void loadChat(ChatSession session) {
        messageContainer.getChildren().clear();
        List<Messaggio> messaggi;
        if (session.isGroup()) {
            messaggi = chatService.getGroupMessages(session.getGroupId());
        } else {
            messaggi = chatService.getPrivateMessages(currentUser.getUsername(), session.getUsername());
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        String lastDate = "";
        for (Messaggio m : messaggi) {
            try {
                String currentDate = LocalDateTime.parse(m.getTimestamp())
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
                if (!currentDate.equals(lastDate)) {
                    Label dateDiv = new Label(currentDate);
                    dateDiv.setStyle(
                            "-fx-background-color: rgba(0,0,0,0.05); -fx-background-radius: 10; -fx-padding: 2 10; -fx-text-fill: #888; -fx-font-size: 11px;");
                    HBox box = new HBox(dateDiv);
                    box.setAlignment(Pos.CENTER);
                    box.setPadding(new Insets(10, 0, 5, 0));
                    messageContainer.getChildren().add(box);
                    lastDate = currentDate;
                }
            } catch (Exception ignored) {
            }
            boolean isMe = m.getSender().equals(currentUser.getUsername());
            HBox bubbleRow = new HBox();
            bubbleRow.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            // Gestione Post Condiviso
            if (m.getContenuto() != null && m.getContenuto().startsWith("[POST:::")
                    && m.getContenuto().endsWith("]")) {
                try {
                    String idStr = m.getContenuto().substring(8, m.getContenuto().length() - 1);
                    int postId = Integer.parseInt(idStr);
                    Post sharedPost = postService.getPostById(postId);
                    Node postBubble;
                    if (sharedPost != null) {
                        postBubble = createSharedPostBubble(sharedPost, isMe);
                    } else {
                        // Post eliminato
                        Label errLbl = new Label("Post non disponibile (Eliminato)");
                        errLbl.setStyle("-fx-font-style: italic; -fx-text-fill: #777;");
                        VBox errBox = new VBox(errLbl);
                        errBox.getStyleClass().add("chat-bubble");
                        errBox.getStyleClass().add(isMe ? "chat-bubble-sent" : "chat-bubble-received");
                        postBubble = errBox;
                    }
                    if (session.isGroup() && !isMe) {
                        VBox groupBubbleEnv = new VBox(2);
                        Label senderName = new Label("@" + m.getSender());
                        senderName.setStyle("-fx-font-size: 10px; -fx-text-fill: #5D4037; -fx-font-weight: bold;");
                        groupBubbleEnv.getChildren().addAll(senderName, postBubble);
                        bubbleRow.getChildren().add(groupBubbleEnv);
                    } else {
                        bubbleRow.getChildren().add(postBubble);
                    }

                    Label time = new Label();
                    try {
                        time.setText(LocalDateTime.parse(m.getTimestamp()).format(dtf));
                    } catch (Exception e) {
                        time.setText("??:??");
                    }
                    time.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #888; -fx-padding: 0 5;");

                } catch (Exception ex) {
                    // Fallback testuale
                    bubbleRow.getChildren().add(createSimpleMessageBubble(m, isMe, session, dtf));
                }
            } else {
                bubbleRow.getChildren().add(createSimpleMessageBubble(m, isMe, session, dtf));
            }
            messageContainer.getChildren().add(bubbleRow);
        }
        chatScroll.layout();
        chatScroll.setVvalue(1.0);
    }

    private void showNewChatDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nuova Conversazione");
        // Carica CSS
        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
        }
        // Aggiungi tipo bottone Close per permettere la chiusura (sarà
        // nascosto/gestito)
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Node closeBtn = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        if (closeBtn != null)
            closeBtn.setVisible(false); // Nascondi bottone default
        VBox root = new VBox(25);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        // Assicura background corretto
        root.setStyle("-fx-background-color: #FFFBF5; -fx-min-width: 400px;");
        Label title = new Label("Nuova Conversazione");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #3E2723;");
        Label subtitle = new Label("Scegli come vuoi comunicare");
        subtitle.setStyle("-fx-text-fill: #8D6E63; -fx-font-size: 14px;");
        HBox buttonsBox = new HBox(20);
        buttonsBox.setAlignment(Pos.CENTER);
        // Bottone Chat Privata
        VBox btnPrivate = createOptionButton("\uD83D\uDC64", "Chat Privata", "Inizia una conversazione singola");
        btnPrivate.setOnMouseClicked(e -> {
            dialog.close();
            handleNewPrivateChat();
        });
        // Bottone Chat Gruppo
        VBox btnGroup = createOptionButton("\uD83D\uDC65", "Nuovo Gruppo", "Crea un gruppo con più amici");
        btnGroup.setOnMouseClicked(e -> {
            dialog.close();
            handleNewGroupChat();
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

    private VBox createOptionButton(String icon, String title, String desc) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setPrefWidth(180);
        box.setPrefHeight(160);
        // Stile card/bottone
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
        // Animazione Hover
        box.setOnMouseEntered(e -> box.setStyle(
                "-fx-background-color: #FFF8E1; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(93,64,55,0.2), 8, 0, 0, 4); -fx-cursor: hand; -fx-border-color: #D7CCC8; -fx-border-radius: 15; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        box.setOnMouseExited(e -> box.setStyle(
                "-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-cursor: hand; -fx-border-color: #EFEBE9; -fx-border-radius: 15; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
        return box;
    }

    private void handleNewPrivateChat() {
        Stage stage = new Stage();
        stage.setTitle("Nuova Chat Privata");
        stage.initModality(Modality.APPLICATION_MODAL);
        // Usa BorderPane per layout affidabile
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setPrefWidth(420);
        root.setPrefHeight(450);
        root.setStyle("-fx-background-color: #FFFBF5;");
        // TOP: Titolo + Ricerca
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
        // CENTER: ListView
        ListView<Utente> userList = new ListView<>();
        userList.getStyleClass().add("list-view");
        userList.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #EFEBE9; -fx-border-radius: 8;");
        Label placeholder = new Label("Caricamento...");
        placeholder.setStyle("-fx-text-fill: #A1887F; -fx-font-style: italic;");
        userList.setPlaceholder(placeholder);
        // Wrap ListView in StackPane
        StackPane listContainer = new StackPane(userList);
        listContainer.setPadding(new Insets(0));
        VBox.setVgrow(listContainer, Priority.ALWAYS);
        root.setCenter(listContainer);
        // BOTTOM: Bottone Annulla
        Button btnCancel = new Button("Annulla");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setOnAction(e -> stage.close());
        HBox bottomBox = new HBox(btnCancel);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(bottomBox);
        // Logica Caricamento Iniziale (Sincrona)
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
        // Logica Ricerca (Asincrona)
        javafx.concurrent.Service<List<Utente>> searchService = new javafx.concurrent.Service<>() {
            @Override
            protected javafx.concurrent.Task<List<Utente>> createTask() {
                final String query = searchField.getText() == null ? "" : searchField.getText().trim();
                return new javafx.concurrent.Task<>() {
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
        searchService.setOnFailed(e -> {
            Throwable ex = searchService.getException();
            if (ex != null)
                ex.printStackTrace();
            placeholder.setText("Errore ricerca: " + (ex != null ? ex.getMessage() : "Sconosciuto"));
        });
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
            private final Button btnStart = new Button("\u27A4");
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
                        openChatForUser(item);
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
                openChatForUser(userList.getSelectionModel().getSelectedItem());
                stage.close();
            }
        });
        // Scene setup
        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
        }
        stage.setScene(scene);
        stage.setOnShown(e -> userList.requestFocus()); // Focus trick
        stage.show();
        loadInitial.run();
    }

    private void openChatForUser(Utente u) {
        try {
            ChatSession session = ChatSession.user(u.getUsername());
            activeSession = session;
            loadConversations();
            // User, quindi lista privata
            privateChatList.getSelectionModel().select(session);
            loadChat(session);
            inputArea.setDisable(false);
            updateHeader(session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleNewGroupChat() {
        Stage stage = new Stage();
        stage.setTitle("Crea Nuovo Gruppo");
        stage.initModality(Modality.APPLICATION_MODAL);
        // Use BorderPane per layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setPrefWidth(380);
        root.setPrefHeight(450);
        root.setStyle("-fx-background-color: #FFFBF5;");
        // Area TOP
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
        // Wrap ListView in StackPane
        StackPane listContainer = new StackPane(userList);
        listContainer.setPadding(new Insets(0));
        VBox.setVgrow(listContainer, Priority.ALWAYS);
        root.setCenter(listContainer);
        // BOTTOM: Bottoni
        Button btnCreate = new Button("Crea");
        btnCreate.getStyleClass().add("button-primary");
        Button btnCancel = new Button("Annulla");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setOnAction(e -> stage.close());
        HBox bottomBox = new HBox(10, btnCancel, btnCreate);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(bottomBox);
        java.util.Set<String> selectedUsernames = new java.util.HashSet<>();
        // Servizio Background
        javafx.concurrent.Service<List<Utente>> searchService = new javafx.concurrent.Service<>() {
            @Override
            protected javafx.concurrent.Task<List<Utente>> createTask() {
                final String query = searchField.getText() == null ? "" : searchField.getText().trim();
                return new javafx.concurrent.Task<>() {
                    @Override
                    protected List<Utente> call() throws Exception {
                        List<Utente> res;
                        if (query.isEmpty()) {
                            // OTTIMIZZAZIONE: Carica top 30 utenti attivi
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
        // Cella Ottimizzata
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
                    // Row Hover
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
        userList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1 && userList.getSelectionModel().getSelectedItem() != null) {
                // Nessuna azione per click singolo in group mode per evitare confusione
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
                    ChatSession session = ChatSession.group(gid, name);
                    activeSession = session;
                    loadConversations();
                    groupChatList.getSelectionModel().select(session);
                    loadChat(session);
                    inputArea.setDisable(false);
                    updateHeader(session);
                    stage.close();
                } else {
                    DialogUtils.showError("Errore", "Errore creazione gruppo.", stage);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                DialogUtils.showError("Errore", "Errore DB: " + ex.getMessage(), stage);
            }
        });
        // Scene setup
        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
        }
        stage.setScene(scene);
        stage.setOnShown(e -> userList.requestFocus());
        stage.show();
    }

    private Node createSharedPostBubble(Post post, boolean isMe) {
        VBox bubble = new VBox(5);
        bubble.setMaxWidth(300);
        bubble.getStyleClass().add("chat-bubble");
        bubble.getStyleClass().add(isMe ? "chat-bubble-sent" : "chat-bubble-received");
        bubble.setStyle(bubble.getStyle() + "; -fx-padding: 8; -fx-cursor: hand;");
        // Header: Autore
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label authorName = new Label(post.getAutore().getUsername());
        authorName.setStyle("-fx-font-weight: bold; -fx-text-fill: #3E2723; -fx-font-size: 12px;");
        Label tag = new Label("CONDIVISO");
        tag.setStyle(
                "-fx-font-size: 9px; -fx-text-fill: #888; -fx-background-color: #EEE; -fx-padding: 2 4; -fx-background-radius: 4;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(authorName, spacer, tag);
        // Preview Contenuto
        Label title = new Label(post.getTitolo());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        title.setWrapText(true);
        bubble.getChildren().addAll(header, title);
        // Thumbnail se immagine
        if (post.getTipo() == Post.TipoPost.FOTO && post.getMedia() != null) {
            try {
                File f = MediaManager.getMediaFile(post.getMedia());
                if (f != null && f.exists()) {
                    Image img = new Image(f.toURI().toString(), 280, 0, true, true);
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(280);
                    iv.setPreserveRatio(true);
                    iv.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);"); // Leggera
                                                                                                         // ombra
                    // Clip angoli arrotondati
                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(280, 150);
                    clip.setArcWidth(10);
                    clip.setArcHeight(10);
                    iv.setClip(null); // Vista semplice per ora
                    bubble.getChildren().add(iv);
                }
            } catch (Exception e) {
            }
        }
        // Interazione
        bubble.setOnMouseClicked(e -> showPostDialog(post));
        return bubble;
    }

    private Node createSimpleMessageBubble(Messaggio m, boolean isMe, ChatSession session, DateTimeFormatter dtf) {
        VBox bubble = new VBox(2);
        bubble.setMaxWidth(400);
        bubble.getStyleClass().add("chat-bubble");
        bubble.getStyleClass().add(isMe ? "chat-bubble-sent" : "chat-bubble-received");
        if (!isMe && !m.isLetto() && !session.isGroup()) {
            chatService.markAsRead(m.getId());
        }
        if (session.isGroup() && !isMe) {
            Label senderName = new Label("@" + m.getSender());
            senderName.setStyle("-fx-font-size: 10px; -fx-text-fill: #5D4037; -fx-font-weight: bold;");
            bubble.getChildren().add(senderName);
        }
        Label content = new Label(m.getContenuto());
        content.setWrapText(true);
        content.setStyle(isMe ? "-fx-text-fill: #3E2723;" : "-fx-text-fill: #212121;");
        Label time = new Label();
        try {
            time.setText(LocalDateTime.parse(m.getTimestamp()).format(dtf));
        } catch (Exception e) {
            time.setText("??:??");
        }
        time.getStyleClass().add("chat-timestamp");
        HBox timeBox = new HBox(time);
        timeBox.setAlignment(Pos.BOTTOM_RIGHT);
        bubble.getChildren().addAll(content, timeBox);
        return bubble;
    }

    private void showPostDialog(Post post) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Post Condiviso");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.initOwner(stage.getScene().getWindow());
        PostCard card = new PostCard(post, currentUser, null); // Callback refresh non necessaria
        // Aggiungi padding alla card
        card.setPadding(new Insets(10));
        ScrollPane sp = new ScrollPane(card);
        sp.setFitToWidth(true);
        sp.setPrefHeight(600);
        sp.setPrefWidth(720);
        sp.getStyleClass().add("scroll-pane"); // Riutilizza stili
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        dialog.getDialogPane().setContent(sp);
        dialog.showAndWait();
    }

    private void handleRenameGroup(ChatSession session) {
        String name = DialogUtils.showInputDialog("Rinomina Gruppo", "Inserisci il nuovo nome per il gruppo:",
                session.getName(), stage);
        if (name != null && !name.isBlank()) {
            try {
                chatService.renameGroup(session.getGroupId(), name.trim());
                loadConversations();
                if (activeSession != null && activeSession.equals(session)) {
                    updateHeader(ChatSession.group(session.getGroupId(), name.trim()));
                }
            } catch (BusinessException e) {
                DialogUtils.showError("Errore", e.getMessage(), stage);
            }
        }
    }

    private void handleLeaveGroup(ChatSession session) {
        if (DialogUtils.showConfirmation("Abbandona Gruppo",
                "Sei sicuro di voler abbandonare il gruppo '" + session.getName() + "'?", stage)) {
            chatService.removeGroupMember(session.getGroupId(), currentUser.getUsername());
            // Se attivo, pulisci
            if (activeSession != null && activeSession.equals(session)) {
                activeSession = null;
                messageContainer.getChildren().clear();
                messageContainer.getChildren().add(createEmptyPlaceholder());
                lblChatUser.setText("Seleziona una conversazione");
                inputArea.setDisable(true);
            }
            loadConversations();
        }
    }

    private void handleDeleteGroup(ChatSession session) {
        // Verifica se creatore
        Gruppo g = chatService.getGroupById(session.getGroupId());
        if (g != null && g.getCreatore().equals(currentUser.getUsername())) {
            if (DialogUtils.showConfirmation("Elimina Gruppo",
                    "Sei sicuro di voler ELIMINARE DEFINITIVAMENTE il gruppo '" + session.getName()
                            + "'? L'azione è irreversibile.",
                    stage)) {
                chatService.deleteGroup(session.getGroupId());
                if (activeSession != null && activeSession.equals(session)) {
                    activeSession = null;
                    messageContainer.getChildren().clear();
                    messageContainer.getChildren().add(createEmptyPlaceholder());
                    lblChatUser.setText("Seleziona una conversazione");
                    inputArea.setDisable(true);
                }
                loadConversations();
            }
        } else {
            DialogUtils.showWarning("Attenzione",
                    "Solo il creatore (" + (g != null ? g.getCreatore() : "?") + ") può eliminare il gruppo.", stage);
        }
    }

    private void handleDeletePrivateChat(ChatSession session) {
        if (DialogUtils.showConfirmation("Elimina Chat",
                "Sei sicuro di voler eliminare la conversazione con @" + session.getName() + "?", stage)) {
            chatService.deleteConversation(currentUser.getUsername(), session.getName());
            if (activeSession != null && activeSession.equals(session)) {
                activeSession = null;
                messageContainer.getChildren().clear();
                messageContainer.getChildren().add(createEmptyPlaceholder());
                lblChatUser.setText("Seleziona una conversazione");
                inputArea.setDisable(true);
            }
            loadConversations();
        }
    }
}
