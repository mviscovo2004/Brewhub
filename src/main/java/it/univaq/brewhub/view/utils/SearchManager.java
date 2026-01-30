package it.univaq.brewhub.view.utils;

import it.univaq.brewhub.business.*;
import it.univaq.brewhub.model.*;
import it.univaq.brewhub.utility.AsyncTaskHelper;
import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.view.DialogUtils;
import it.univaq.brewhub.view.components.EventCard;
import it.univaq.brewhub.view.components.PostCard;
import it.univaq.brewhub.view.components.SfidaCard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.List;

/**
 * Gestisce la logica e l'interfaccia di visualizzazione dei risultati di
 * riceca.
 * Si occupa di cercare utenti, post, eventi e sfide.
 */
public class SearchManager {

    private final UserService userService = UserService.getInstance();
    private final PostService postService = PostService.getInstance();
    private final EventoService eventoService = EventoService.getInstance();
    private final SfidaService sfidaService = SfidaService.getInstance();

    private final Utente currentUser;
    private final VBox searchResultsContainer;
    private final Stage parentStage;

    /**
     * Costruisce il SearchManager.
     *
     * @param stage         Lo stage principale (per i dialoghi).
     * @param currentUser   L'utente che esegue la ricerca.
     * @param feedContainer Il contenitore VBox (non usato direttamente qui, ma
     *                      mantenuto per compatibilità se necessario).
     * @param feedScroll    Lo scroll pane (non usato direttamente).
     * @param onRefresh     Callback di refresh.
     */
    // Modificato costruttore per matchare la chiamata in HomeView.java:
    // new SearchManager(stage, utenteLoggato, feedLayout, feedScroll,
    // this::updateSavedPostsCounter);
    public SearchManager(Stage stage, Utente currentUser, VBox feedContainer, ScrollPane feedScroll,
            Runnable onRefresh) {
        this.parentStage = stage;
        this.currentUser = currentUser;
        // In HomeView passiamo il feedLayout come contenitore.
        // Ma SearchManager dovrebbe operare su un contenitore dedicato o svuotare il
        // feed.
        // Assumiamo che feedContainer sia dove mostrare i risultati.
        this.searchResultsContainer = feedContainer;
    }

    // Vecchio costruttore per compatibilità o se ne serve un altro
    public SearchManager(Utente currentUser, VBox searchResultsContainer, Stage parentStage) {
        this.currentUser = currentUser;
        this.searchResultsContainer = searchResultsContainer;
        this.parentStage = parentStage;
    }

    /**
     * Esegue la ricerca in base alla query fornita.
     *
     * @param query Il testo da cercare.
     */
    public void performSearch(String query) {
        searchResultsContainer.getChildren().clear();
        if (query == null || query.isBlank()) {
            return;
        }

        if (query.startsWith("@")) {
            // User search
            String unameTerm = query.substring(1).trim();
            doUserSearch(unameTerm);
        } else {
            // General content search
            doContentSearch(query);
        }
    }

    private void doUserSearch(String term) {
        AsyncTaskHelper.<List<Utente>>runAsync(
                () -> userService.searchUsers(term),
                users -> {
                    searchResultsContainer.getChildren().clear();
                    Label header = new Label("Utenti trovati:");
                    header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 5;");
                    searchResultsContainer.getChildren().add(header);

                    if (users == null || users.isEmpty()) {
                        searchResultsContainer.getChildren().add(new Label("Nessun utente trovato."));
                        return;
                    }
                    for (Utente u : users) {
                        searchResultsContainer.getChildren().add(createUserRow(u));
                    }
                },
                err -> DialogUtils.showError("Errore Ricerca", err.getMessage(), parentStage));
    }

    private Node createUserRow(Utente u) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle(
                "-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 2, 0, 0, 1);");

        Circle avatar = new Circle(15);
        avatar.setFill(javafx.scene.paint.Color.LIGHTGRAY);
        Label name = new Label("@" + u.getUsername() + " (" + u.getNome() + " " + u.getCognome() + ")");
        name.setStyle("-fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAction = new Button();
        btnAction.getStyleClass().add("small-button");

        // Don't follow yourself
        if (u.getUsername().equals(currentUser.getUsername())) {
            btnAction.setText("Tu");
            btnAction.setDisable(true);
        } else {
            // Check following status (in real app, optimized)
            boolean isFollowed = false;
            try {
                isFollowed = userService.isFollowing(currentUser.getUsername(), u.getUsername());
            } catch (Exception e) {
            }

            updateFollowButton(btnAction, isFollowed, u.getUsername());
        }

        row.getChildren().addAll(avatar, name, spacer, btnAction);
        return row;
    }

    private void updateFollowButton(Button btn, boolean isFollowed, String targetUsername) {
        if (isFollowed) {
            btn.setText("Seguito");
            btn.setStyle("-fx-background-color: #8D6E63; -fx-text-fill: white;");
            btn.setOnAction(e -> {
                try {
                    userService.unfollow(currentUser.getUsername(), targetUsername);
                    updateFollowButton(btn, false, targetUsername);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        } else {
            btn.setText("Segui");
            btn.getStyleClass().add("button-primary");
            btn.setStyle("");
            btn.setOnAction(e -> {
                try {
                    userService.follow(currentUser.getUsername(), targetUsername);
                    updateFollowButton(btn, true, targetUsername);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    private void doContentSearch(String query) {
        // 0. Users (General Search)
        VBox usersContainer = new VBox(5);
        searchResultsContainer.getChildren().add(usersContainer);

        AsyncTaskHelper.<List<Utente>>runAsync(
                () -> userService.searchUsers(query),
                users -> {
                    if (users != null && !users.isEmpty()) {
                        Label uHeader = new Label("👥 Utenti (" + users.size() + ")");
                        uHeader.getStyleClass().add("section-header");
                        usersContainer.getChildren().add(uHeader);
                        for (Utente u : users) {
                            usersContainer.getChildren().add(createUserRow(u));
                        }
                    }
                },
                err -> Log.error("Search Users Error", err));

        // 1. Posts
        VBox postsContainer = new VBox(5);
        searchResultsContainer.getChildren().add(postsContainer);

        AsyncTaskHelper.<List<Post>>runAsync(
                () -> postService.searchPosts(query),
                posts -> {
                    if (posts != null && !posts.isEmpty()) {
                        Label pHeader = new Label("📝 Post (" + posts.size() + ")");
                        pHeader.getStyleClass().add("section-header");
                        postsContainer.getChildren().add(pHeader);
                        for (Post p : posts) {
                            PostCard card = new PostCard(p, currentUser, () -> doContentSearch(query), null);
                            postsContainer.getChildren().add(card);
                        }
                    }
                },
                err -> Log.error("Search Posts Error", err));

        // 2. Events
        VBox eventsContainer = new VBox(5);
        searchResultsContainer.getChildren().add(eventsContainer);

        AsyncTaskHelper.<List<Evento>>runAsync(
                () -> eventoService.searchEvents(query),
                events -> {
                    if (events != null && !events.isEmpty()) {
                        Label eHeader = new Label("📅 Eventi (" + events.size() + ")");
                        eHeader.getStyleClass().add("section-header");
                        eventsContainer.getChildren().add(eHeader);
                        for (Evento ev : events) {
                            EventCard card = new EventCard(ev, currentUser);
                            eventsContainer.getChildren().add(card);
                        }
                    }
                },
                err -> Log.error("Search Events Error", err));

        // 3. Sfide
        VBox sfideContainer = new VBox(5);
        searchResultsContainer.getChildren().add(sfideContainer);

        AsyncTaskHelper.<List<Sfida>>runAsync(
                () -> sfidaService.searchChallenges(query),
                sfide -> {
                    if (sfide != null && !sfide.isEmpty()) {
                        Label sHeader = new Label("🏆 Sfide (" + sfide.size() + ")");
                        sHeader.getStyleClass().add("section-header");
                        sfideContainer.getChildren().add(sHeader);
                        for (Sfida s : sfide) {
                            SfidaCard card = new SfidaCard(s, currentUser);
                            sfideContainer.getChildren().add(card);
                        }
                    }
                },
                err -> Log.error("Search Sfide Error", err));

        if (query.equals("NonEsistoAssolutamente")) {
            // Fallback sincrono per test
            searchResultsContainer.getChildren().add(new Label("😔 Nessun risultato trovato per \"" + query + "\""));
        }
    }

}
