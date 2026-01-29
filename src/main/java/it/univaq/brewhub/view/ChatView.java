package it.univaq.brewhub.view;

import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.business.ChatService;
import it.univaq.brewhub.business.PostService;
import it.univaq.brewhub.model.Gruppo;
import it.univaq.brewhub.model.Messaggio;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.utility.MediaManager;
import it.univaq.brewhub.view.components.ChatListCell;
import it.univaq.brewhub.view.components.PostCard;
import it.univaq.brewhub.view.model.ChatSession;
import it.univaq.brewhub.view.utils.ChatDialogManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce la vista principale della messaggistica (Chat).
 * Permette agli utenti di:
 * <ul>
 * <li>Visualizzare interfacce a due colonne: lista conversazioni e area
 * messaggi.</li>
 * <li>Navigare tra chat private e di gruppo.</li>
 * <li>Inviare e ricevere messaggi in tempo reale (simulato tramite
 * refresh/polling).</li>
 * <li>Creare nuovi gruppi o chat private.</li>
 * <li>Gestire gruppi (rinomina, abbandona, elimina).</li>
 * </ul>
 */
public class ChatView {

    private final Stage stage;
    private final Utente currentUser;
    private ChatSession activeSession;
    private final ChatService chatService = ChatService.getInstance();
    private final PostService postService = PostService.getInstance();

    private VBox messageContainer;
    private ScrollPane chatScroll;
    private ListView<ChatSession> privateChatList;
    private ListView<ChatSession> groupChatList;
    private Label lblChatUser;
    private HBox inputArea;

    private final ChatListCell.ChatActionListener chatActionListener = new ChatListCell.ChatActionListener() {
        @Override
        public void onRenameGroup(ChatSession session) {
            handleRenameGroup(session);
        }

        @Override
        public void onLeaveGroup(ChatSession session) {
            handleLeaveGroup(session);
        }

        @Override
        public void onDeleteGroup(ChatSession session) {
            handleDeleteGroup(session);
        }

        @Override
        public void onDeletePrivateChat(ChatSession session) {
            handleDeletePrivateChat(session);
        }
    };

    /**
     * Costruttore della vista Chat.
     *
     * @param stage          Lo stage principale dell'applicazione.
     * @param currentUser    L'utente attualmente loggato.
     * @param activeChatUser Username opzionale con cui aprire direttamente una
     *                       conversazione.
     *                       Se null, non viene selezionata nessuna chat
     *                       inizialmente.
     */
    public ChatView(Stage stage, Utente currentUser, String activeChatUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        if (activeChatUser != null) {
            this.activeSession = ChatSession.single(activeChatUser);
        }
    }

    /**
     * Costruisce e restituisce l'interfaccia grafica completa della Chat.
     *
     * @return Il nodo root (BorderPane) della vista.
     */
    public Parent getView() {
        BorderPane root = new BorderPane();
        try {
            root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            // Ignora se non trova CSS
        }

        VBox sidebar = createSidebar();
        VBox chatArea = createChatArea();

        root.setLeft(sidebar);
        root.setCenter(chatArea);

        // Caricamento iniziale dei dati
        loadConversations();
        initializeSelection();

        return root;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(280);
        sidebar.getStyleClass().add("chat-sidebar");
        sidebar.setPadding(new Insets(15));

        // Header Sidebar
        HBox sidebarHeader = new HBox(10);
        sidebarHeader.setAlignment(Pos.CENTER_LEFT);

        Button btnBack = new Button("←");
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
        btnNew.setOnAction(e -> ChatDialogManager.showNewChatDialog(stage,
                () -> ChatDialogManager.showNewPrivateChatDialog(stage, currentUser, this::openChatForUser),
                () -> ChatDialogManager.showNewGroupChatDialog(stage, currentUser, session -> {
                    activeSession = session;
                    loadConversations();
                    groupChatList.getSelectionModel().select(session);
                    loadChat(session);
                    inputArea.setDisable(false);
                    updateHeader(session);
                })));

        sidebarHeader.getChildren().addAll(btnBack, sidebarTitle, spacer, btnNew);

        // Liste Chat
        Label lblPrivate = new Label("CHAT PRIVATE");
        lblPrivate.setStyle(
                "-fx-font-weight: bold; -fx-text-fill: #8D6E63; -fx-font-size: 11px; -fx-padding: 10 0 5 0;");

        privateChatList = new ListView<>();
        privateChatList.getStyleClass().add("chat-conversation-list");
        privateChatList.setCellFactory(param -> new ChatListCell(chatActionListener));
        VBox.setVgrow(privateChatList, Priority.ALWAYS);

        Label lblGroups = new Label("GRUPPI");
        lblGroups.setStyle(
                "-fx-font-weight: bold; -fx-text-fill: #8D6E63; -fx-font-size: 11px; -fx-padding: 15 0 5 0;");

        groupChatList = new ListView<>();
        groupChatList.getStyleClass().add("chat-conversation-list");
        groupChatList.setCellFactory(param -> new ChatListCell(chatActionListener));
        VBox.setVgrow(groupChatList, Priority.ALWAYS);

        // Listener selezione
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

        sidebar.getChildren().addAll(sidebarHeader, lblPrivate, privateChatList, lblGroups, groupChatList);
        return sidebar;
    }

    private VBox createChatArea() {
        VBox chatArea = new VBox();
        chatArea.getStyleClass().add("chat-area");

        // Header Area Chat
        HBox chatHeader = new HBox(15);
        chatHeader.getStyleClass().add("chat-header");
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        lblChatUser = new Label();
        lblChatUser.getStyleClass().add("chat-header-title");
        chatHeader.getChildren().add(lblChatUser);

        // Container Messaggi Scrollabile
        messageContainer = new VBox(15);
        messageContainer.setPadding(new Insets(20));

        chatScroll = new ScrollPane(messageContainer);
        chatScroll.setFitToWidth(true);
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScroll.getStyleClass().add("scroll-pane");
        chatScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(chatScroll, Priority.ALWAYS);

        // Area Input
        inputArea = createInputArea();

        chatArea.getChildren().addAll(chatHeader, chatScroll, inputArea);
        return chatArea;
    }

    private HBox createInputArea() {
        HBox area = new HBox(15);
        area.getStyleClass().add("chat-input-area");
        area.setAlignment(Pos.CENTER);

        TextField msgField = new TextField();
        msgField.setPromptText("Scrivi un messaggio...");
        msgField.getStyleClass().add("text-field");
        msgField.setStyle("-fx-background-radius: 20; -fx-padding: 10 15;");
        HBox.setHgrow(msgField, Priority.ALWAYS);

        Button btnSend = new Button("➤");
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
            } else {
                m.setReceiver(activeSession.getIdentifier());
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

        area.getChildren().addAll(msgField, btnSend);
        return area;
    }

    private void initializeSelection() {
        if (activeSession != null) {
            updateHeader(activeSession);
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
    }

    private void updateHeader(ChatSession session) {
        if (session.isGroup()) {
            lblChatUser.setText("Gruppo: " + session.getLabel());
        } else {
            lblChatUser.setText("Conversazione con " + session.getLabel());
        }
    }

    private Node createEmptyPlaceholder() {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(50));
        Label icon = new Label("💬");
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
            privateItems.add(ChatSession.single(u));
        }
        privateChatList.getItems().setAll(privateItems);

        // Aggiungi sessione attiva se non presente
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
            messaggi = chatService.getPrivateMessages(currentUser.getUsername(), session.getIdentifier());
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        String lastDate = "";

        for (Messaggio m : messaggi) {
            // Dividi per data
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

            // Gestione Post Condiviso (formato speciale messaggio)
            if (m.getContenuto() != null && m.getContenuto().startsWith("[POST:::")
                    && m.getContenuto().endsWith("]")) {
                handleSharedPostMessage(m, isMe, session, bubbleRow, dtf);
            } else {
                bubbleRow.getChildren().add(createSimpleMessageBubble(m, isMe, session, dtf));
            }

            messageContainer.getChildren().add(bubbleRow);
        }
        chatScroll.layout();
        chatScroll.setVvalue(1.0);
    }

    private void handleSharedPostMessage(Messaggio m, boolean isMe, ChatSession session, HBox bubbleRow,
            DateTimeFormatter dtf) {
        try {
            String idStr = m.getContenuto().substring(8, m.getContenuto().length() - 1);
            int postId = Integer.parseInt(idStr);
            Post sharedPost = postService.getPostById(postId);

            Node postBubble;
            if (sharedPost != null) {
                postBubble = createSharedPostBubble(sharedPost, isMe);
            } else {
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
        } catch (Exception ex) {
            bubbleRow.getChildren().add(createSimpleMessageBubble(m, isMe, session, dtf));
        }
    }

    private void openChatForUser(Utente u) {
        try {
            ChatSession session = ChatSession.single(u.getUsername());
            activeSession = session;
            loadConversations();
            privateChatList.getSelectionModel().select(session);
            loadChat(session);
            inputArea.setDisable(false);
            updateHeader(session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Crea il fumetto per un post condiviso.
     */
    private Node createSharedPostBubble(Post post, boolean isMe) {
        VBox bubble = new VBox(5);
        bubble.setMaxWidth(300);
        bubble.getStyleClass().add("chat-bubble");
        bubble.getStyleClass().add(isMe ? "chat-bubble-sent" : "chat-bubble-received");
        bubble.setStyle(bubble.getStyle() + "; -fx-padding: 8; -fx-cursor: hand;");

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

        Label title = new Label(post.getTitolo());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        title.setWrapText(true);
        bubble.getChildren().addAll(header, title);

        if (post.getTipo() == Post.TipoPost.FOTO && post.getMedia() != null) {
            try {
                File f = MediaManager.getMediaFile(post.getMedia());
                if (f != null && f.exists()) {
                    Image img = new Image(f.toURI().toString(), 280, 0, true, true);
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(280);
                    iv.setPreserveRatio(true);
                    iv.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
                    bubble.getChildren().add(iv);
                }
            } catch (Exception e) {
            }
        }

        bubble.setOnMouseClicked(e -> showPostDialog(post));
        return bubble;
    }

    /**
     * Crea il fumetto per un messaggio testuale semplice.
     */
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

        PostCard card = new PostCard(post, currentUser, null);
        card.setPadding(new Insets(10));

        ScrollPane sp = new ScrollPane(card);
        sp.setFitToWidth(true);
        sp.setPrefHeight(600);
        sp.setPrefWidth(720);
        sp.getStyleClass().add("scroll-pane");
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        dialog.getDialogPane().setContent(sp);
        dialog.showAndWait();
    }

    private void handleRenameGroup(ChatSession session) {
        String name = DialogUtils.showInputDialog("Rinomina Gruppo", "Inserisci il nuovo nome per il gruppo:",
                session.getLabel(), stage);
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
                "Sei sicuro di voler abbandonare il gruppo '" + session.getLabel() + "'?", stage)) {
            chatService.removeGroupMember(session.getGroupId(), currentUser.getUsername());
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
        Gruppo g = chatService.getGroupById(session.getGroupId());
        if (g != null && g.getCreatore().equals(currentUser.getUsername())) {
            if (DialogUtils.showConfirmation("Elimina Gruppo",
                    "Sei sicuro di voler ELIMINARE DEFINITIVAMENTE il gruppo '" + session.getLabel()
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
                "Sei sicuro di voler eliminare la conversazione con " + session.getLabel() + "?", stage)) {
            chatService.deleteConversation(currentUser.getUsername(), session.getIdentifier());
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
