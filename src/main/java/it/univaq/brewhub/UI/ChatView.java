package it.univaq.brewhub.UI;

import it.univaq.brewhub.Gruppo;
import it.univaq.brewhub.Messaggio;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.dao.GruppoDAO;
import it.univaq.brewhub.dao.MessaggioDAO;
import it.univaq.brewhub.dao.UtenteDAO;
import it.univaq.brewhub.dao.impl.GruppoDAOImpl;
import it.univaq.brewhub.dao.impl.MessaggioDAOImpl;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChatView {

    private final Stage stage;
    private final Utente currentUser;
    private ChatSession activeSession; // Current active chat

    private final MessaggioDAO messaggioDAO = new MessaggioDAOImpl();
    private final GruppoDAO gruppoDAO = new GruppoDAOImpl();
    private final UtenteDAO utenteDAO = new UtenteDAOImpl();

    private VBox messageContainer;
    private ScrollPane chatScroll;
    private ListView<ChatSession> conversationList;
    private Label lblChatUser;
    private HBox inputArea;

    // Helper class for sidebar items
    public static class ChatSession {
        private String name;
        private boolean isGroup;
        private int groupId; // -1 if user
        private String username; // null if group

        public static ChatSession user(String username) {
            ChatSession s = new ChatSession();
            s.name = username;
            s.isGroup = false;
            s.username = username;
            s.groupId = -1;
            return s;
        }

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

    public ChatView(Stage stage, Utente currentUser, String activeChatUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        if (activeChatUser != null) {
            this.activeSession = ChatSession.user(activeChatUser);
        }
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // --- Sidebar ---
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(280);
        sidebar.getStyleClass().add("chat-sidebar");
        sidebar.setPadding(new Insets(15));

        // Sidebar Header
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

        // Sidebar List
        conversationList = new ListView<>();
        conversationList.getStyleClass().add("chat-conversation-list");
        conversationList.setCellFactory(param -> new ListCell<>() {
            {
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
                } else {
                    HBox cell = new HBox(12);
                    cell.setAlignment(Pos.CENTER_LEFT);
                    cell.setPadding(new Insets(8));

                    // Avatar
                    Circle avatar = new Circle(20);
                    avatar.setFill(javafx.scene.paint.Color.web(item.isGroup() ? "#5D4037" : "#8D6E63"));

                    String initialChar = item.getName().length() > 0 ? item.getName().substring(0, 1).toUpperCase()
                            : "?";
                    Label initial = new Label(initialChar);
                    initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    StackPane avatarStack = new StackPane(avatar, initial);

                    // Name
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
                }
            }
        });

        loadConversations();

        sidebar.getChildren().addAll(sidebarHeader, conversationList);
        VBox.setVgrow(conversationList, Priority.ALWAYS);

        // --- Main Chat Area ---
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
                // Receiver is null or handled by trigger
            } else {
                m.setReceiver(activeSession.getUsername());
            }

            messaggioDAO.create(m);
            msgField.clear();
            loadChat(activeSession);
        };

        btnSend.setOnAction(e -> sendAction.run());
        msgField.setOnAction(e -> sendAction.run());

        inputArea.getChildren().addAll(msgField, btnSend);

        chatArea.getChildren().addAll(chatHeader, chatScroll, inputArea);

        // Initial Logic
        if (activeSession != null) {
            updateHeader(activeSession);
            // Select if present
            if (conversationList.getItems().contains(activeSession)) {
                conversationList.getSelectionModel().select(activeSession);
            } else {
                // If it's a new 1-to-1 chat not in history yet
                loadChat(activeSession);
            }
        } else {
            lblChatUser.setText("Seleziona una conversazione");
            inputArea.setDisable(true);
            messageContainer.getChildren().add(createEmptyPlaceholder());
        }

        // Listener
        conversationList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
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
        List<ChatSession> items = new ArrayList<>();

        // 1. Groups
        List<Gruppo> gruppi = gruppoDAO.getGruppiUtente(currentUser.getUsername());
        for (Gruppo g : gruppi) {
            items.add(ChatSession.group(g.getId(), g.getNome()));
        }

        // 2. Private Chats
        List<String> users = messaggioDAO.getUtentiConversazioni(currentUser.getUsername());
        for (String u : users) {
            items.add(ChatSession.user(u));
        }

        conversationList.getItems().setAll(items);

        if (activeSession != null && !items.contains(activeSession)) {
            conversationList.getItems().add(0, activeSession);
        }
    }

    private void loadChat(ChatSession session) {
        messageContainer.getChildren().clear();
        List<Messaggio> messaggi;

        if (session.isGroup()) {
            messaggi = messaggioDAO.getMessaggiGruppo(session.getGroupId());
        } else {
            messaggi = messaggioDAO.getConversazione(currentUser.getUsername(), session.getUsername());
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

            VBox bubble = new VBox(2);
            bubble.setMaxWidth(400);
            bubble.getStyleClass().add("chat-bubble");
            bubble.getStyleClass().add(isMe ? "chat-bubble-sent" : "chat-bubble-received");

            if (!isMe && !m.isLetto() && !session.isGroup()) {
                // Mark private read. Group read state is complex, skipping for simplicity or
                // mark all
                messaggioDAO.segnaComeLetto(m.getId());
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
            bubbleRow.getChildren().add(bubble);

            messageContainer.getChildren().add(bubbleRow);
        }

        chatScroll.layout();
        chatScroll.setVvalue(1.0);
    }

    private void showNewChatDialog() {
        Alert typeDialog = new Alert(Alert.AlertType.CONFIRMATION);
        typeDialog.setTitle("Nuova Conversazione");
        typeDialog.setHeaderText("Che tipo di chat vuoi creare?");

        ButtonType btnPrivate = new ButtonType("Chat Privata");
        ButtonType btnGroup = new ButtonType("Nuovo Gruppo");
        ButtonType btnCancel = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);

        typeDialog.getButtonTypes().setAll(btnPrivate, btnGroup, btnCancel);

        Optional<ButtonType> result = typeDialog.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnPrivate) {
                handleNewPrivateChat();
            } else if (result.get() == btnGroup) {
                handleNewGroupChat();
            }
        }
    }

    private void handleNewPrivateChat() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuova Chat Privata");
        dialog.setHeaderText("Inserisci lo username dell'utente");
        dialog.setContentText("Username:");

        dialog.showAndWait().ifPresent(username -> {
            try {
                if (username.equalsIgnoreCase(currentUser.getUsername())) {
                    new Alert(Alert.AlertType.WARNING, "Non puoi chattare con te stesso!").show();
                    return;
                }
                Utente u = utenteDAO.findByUsername(username);
                if (u != null) {
                    ChatSession session = ChatSession.user(u.getUsername());
                    activeSession = session;
                    loadConversations(); // might add it to list
                    conversationList.getSelectionModel().select(session);
                    loadChat(session);
                    lblChatUser.setText("Conversazione con @" + session.getName());
                    inputArea.setDisable(false);
                } else {
                    new Alert(Alert.AlertType.ERROR, "Utente non trovato!").show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleNewGroupChat() {
        // Simple dialog for group creation
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Crea Nuovo Gruppo");
        dialog.setHeaderText("Inserisci nome e partecipanti");

        ButtonType createBtnType = new ButtonType("Crea", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Nome del gruppo");

        // Let's list some users to add - simplistic approach: list all followed users?
        // Or text area for csv? Let's use a TextArea for comma separated usernames for
        // MVP safety
        // since getting all users for checkboxes might be heavy if many users.
        // Better: Search user like "Add Member" - but for complexity reduction:
        // multi-select if possible?
        // Let's stick to: Enter usernames (comma separated)

        TextArea membersField = new TextArea();
        membersField.setPromptText("Username partecipanti (separati da virgola)");
        membersField.setPrefRowCount(3);

        grid.add(new Label("Nome Gruppo:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Partecipanti:"), 0, 1);
        grid.add(membersField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createBtnType) {
                String name = nameField.getText();
                String membersStr = membersField.getText();
                if (name == null || name.isBlank())
                    return false;

                List<String> members = new ArrayList<>();
                if (membersStr != null && !membersStr.isBlank()) {
                    String[] parts = membersStr.split(",");
                    for (String p : parts) {
                        String clean = p.trim();
                        if (!clean.isEmpty() && !clean.equalsIgnoreCase(currentUser.getUsername())) {
                            members.add(clean);
                        }
                    }
                }

                int gid = gruppoDAO.createGruppo(name, currentUser.getUsername(), members);
                if (gid > 0) {
                    ChatSession gSession = ChatSession.group(gid, name);
                    activeSession = gSession;
                    return true;
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                loadConversations();
                conversationList.getSelectionModel().select(activeSession);
                loadChat(activeSession);
                updateHeader(activeSession);
                inputArea.setDisable(false);
            } else {
                new Alert(Alert.AlertType.ERROR, "Errore creazione gruppo. Controlla i dati.").show();
            }
        });
    }
}
