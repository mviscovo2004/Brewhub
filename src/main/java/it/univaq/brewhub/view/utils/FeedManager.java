package it.univaq.brewhub.view.utils;

import it.univaq.brewhub.business.PostService;
import it.univaq.brewhub.model.Categoria;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.utility.AsyncTaskHelper;
import it.univaq.brewhub.view.components.PostCard;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce il caricamento e la visualizzazione dei vari tipi di feed (Home,
 * Popolari, Seguiti, Categorie, ecc.).
 * Centralizza la logica di recupero asincrono dei post e la gestione della UI.
 */
public class FeedManager {

    private final PostService postService = PostService.getInstance();
    private final Utente utenteLoggato;
    private final VBox feedContainer;
    private final ScrollPane scrollPane;
    private final List<PostCard> activeCards = new ArrayList<>();
    private Runnable onRefreshCallback;

    /**
     * Costruisce il FeedManager.
     *
     * @param utenteLoggato L'utente corrente.
     * @param feedContainer Il VBox in cui verranno aggiunte le card dei post.
     * @param scrollPane    Il contenitore scrollabile principale (per reset
     *                      scroll).
     */
    public FeedManager(Utente utenteLoggato, VBox feedContainer, ScrollPane scrollPane) {
        this.utenteLoggato = utenteLoggato;
        this.feedContainer = feedContainer;
        this.scrollPane = scrollPane;
    }

    private Label createHeaderLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("section-title");
        return lbl;
    }

    /**
     * Imposta la callback da eseguire quando viene richiesto un refresh (es. dopo
     * eliminazione post).
     *
     * @param callback L'azione di refresh.
     */
    public void setOnRefreshCallback(Runnable callback) {
        this.onRefreshCallback = callback;
    }

    /**
     * Carica il feed principale recuperando tutti i post disponibili.
     */
    public void loadFeed() {
        stopAllPlayers();
        feedContainer.getChildren().clear();
        feedContainer.getChildren().add(createHeaderLabel("🏠 Home Feed"));

        AsyncTaskHelper.<List<Post>>runAsync(
                () -> postService.getAllPosts(),
                posts -> {
                    if (posts == null || posts.isEmpty()) {
                        feedContainer.getChildren().add(
                                createEmptyStateNode("📭 Nessun Post", "Non ci sono ancora post da visualizzare."));
                    } else {
                        for (Post p : posts) {
                            PostCard card = new PostCard(p, utenteLoggato, this::loadFeed, onRefreshCallback);
                            activeCards.add(card);
                            feedContainer.getChildren().add(card);
                        }
                    }
                    scrollPane.setVvalue(0);
                },
                error -> {
                    feedContainer.getChildren().add(
                            createEmptyStateNode("⚠️ Errore", "Impossibile caricare il feed: " + error.getMessage()));
                });
    }

    /**
     * Carica i post più popolari.
     */
    public void loadFeedPopular() {
        stopAllPlayers();
        feedContainer.getChildren().clear();
        feedContainer.getChildren().add(createHeaderLabel("🔥 Post Popolari"));

        AsyncTaskHelper.<List<Post>>runAsync(
                () -> postService.getPopularPosts(),
                posts -> {
                    if (posts == null || posts.isEmpty()) {
                        feedContainer.getChildren().add(
                                createEmptyStateNode("🔥 Nessun Post Popolare",
                                        "Non ci sono post popolari al momento."));
                    } else {
                        for (Post p : posts) {
                            PostCard card = new PostCard(p, utenteLoggato, this::loadFeedPopular, onRefreshCallback);
                            activeCards.add(card);
                            feedContainer.getChildren().add(card);
                        }
                    }
                    scrollPane.setVvalue(0);
                },
                error -> {
                    feedContainer.getChildren().add(
                            createEmptyStateNode("⚠️ Errore",
                                    "Impossibile caricare i post popolari: " + error.getMessage()));
                });
    }

    /**
     * Carica i post degli utenti seguiti.
     */
    public void loadFeedFollowed() {
        stopAllPlayers();
        feedContainer.getChildren().clear();
        feedContainer.getChildren().add(createHeaderLabel("👥 Post Seguiti"));

        AsyncTaskHelper.<List<Post>>runAsync(
                () -> postService.getPostsFromFollowed(utenteLoggato.getUsername()),
                posts -> {
                    if (posts == null || posts.isEmpty()) {
                        feedContainer.getChildren().add(
                                createEmptyStateNode("👥 Nessun Post",
                                        "Gli utenti che segui non hanno ancora pubblicato nulla."));
                    } else {
                        for (Post p : posts) {
                            PostCard card = new PostCard(p, utenteLoggato, this::loadFeedFollowed, onRefreshCallback);
                            activeCards.add(card);
                            feedContainer.getChildren().add(card);
                        }
                    }
                    scrollPane.setVvalue(0);
                },
                error -> {
                    feedContainer.getChildren().add(
                            createEmptyStateNode("⚠️ Errore",
                                    "Impossibile caricare i post seguiti: " + error.getMessage()));
                });
    }

    /**
     * Carica i post piaciuti dall'utente (Mi Piace).
     */
    public void loadFeedLiked() {
        stopAllPlayers();
        feedContainer.getChildren().clear();
        feedContainer.getChildren().add(createHeaderLabel("\u2764 Post che ti piacciono"));

        AsyncTaskHelper.<List<Post>>runAsync(
                () -> postService.getLikedPosts(utenteLoggato.getUsername()),
                posts -> {
                    if (posts == null || posts.isEmpty()) {
                        feedContainer.getChildren().add(
                                createEmptyStateNode("\u2764 Nessun Post Piaciuto",
                                        "Non hai ancora messo mi piace a nessun post."));
                    } else {
                        for (Post p : posts) {
                            PostCard card = new PostCard(p, utenteLoggato, this::loadFeedLiked, onRefreshCallback);
                            activeCards.add(card);
                            feedContainer.getChildren().add(card);
                        }
                    }
                    scrollPane.setVvalue(0);
                },
                error -> {
                    feedContainer.getChildren().add(
                            createEmptyStateNode("⚠️ Errore",
                                    "Impossibile caricare i post piaciuti: " + error.getMessage()));
                });
    }

    /**
     * Carica i post filtrati per una specifica categoria.
     *
     * @param categoria La categoria selezionata.
     */
    public void loadFeedByCategory(Categoria categoria) {
        stopAllPlayers();
        feedContainer.getChildren().clear();
        feedContainer.getChildren().add(createHeaderLabel("Categoria: " + categoria.getNome()));

        AsyncTaskHelper.<List<Post>>runAsync(
                () -> postService.getPostsByCategory(categoria.getId()),
                posts -> {
                    if (posts == null || posts.isEmpty()) {
                        feedContainer.getChildren().add(
                                createEmptyStateNode("📂 Nessun Post",
                                        "Non ci sono post nella categoria \"" + categoria.getNome() + "\"."));
                    } else {
                        for (Post p : posts) {
                            PostCard card = new PostCard(p, utenteLoggato,
                                    () -> loadFeedByCategory(categoria), onRefreshCallback);
                            activeCards.add(card);
                            feedContainer.getChildren().add(card);
                        }
                    }
                    scrollPane.setVvalue(0);
                },
                error -> {
                    feedContainer.getChildren().add(
                            createEmptyStateNode("⚠️ Errore",
                                    "Impossibile caricare i post della categoria: " + error.getMessage()));
                });
    }

    /**
     * Carica i post pubblicati dai Torrefattori.
     */
    public void loadFeedByTorrefattori() {
        stopAllPlayers();
        feedContainer.getChildren().clear();
        feedContainer.getChildren().add(createHeaderLabel("☕ Post dai Torrefattori"));

        AsyncTaskHelper.<List<Post>>runAsync(
                () -> postService.getPostsByTorrefattori(),
                posts -> {
                    if (posts == null || posts.isEmpty()) {
                        feedContainer.getChildren().add(
                                createEmptyStateNode("☕ Nessun Post",
                                        "I torrefattori non hanno ancora pubblicato nulla."));
                    } else {
                        for (Post p : posts) {
                            PostCard card = new PostCard(p, utenteLoggato, this::loadFeedByTorrefattori,
                                    onRefreshCallback);
                            activeCards.add(card);
                            feedContainer.getChildren().add(card);
                        }
                    }
                    scrollPane.setVvalue(0);
                },
                error -> {
                    feedContainer.getChildren().add(
                            createEmptyStateNode("⚠️ Errore",
                                    "Impossibile caricare i post dei torrefattori: " + error.getMessage()));
                });
    }

    /**
     * Carica i post salvati dall'utente (Preferiti).
     */
    public void loadFeedBySaved() {
        stopAllPlayers();
        feedContainer.getChildren().clear();
        feedContainer.getChildren().add(createHeaderLabel("⭐ Post Salvati"));

        AsyncTaskHelper.<List<Post>>runAsync(
                () -> postService.getSavedPosts(utenteLoggato.getUsername()),
                posts -> {
                    if (posts == null || posts.isEmpty()) {
                        feedContainer.getChildren().add(
                                createEmptyStateNode("🔖 Nessun Post Salvato", "Non hai ancora salvato nessun post."));
                    } else {
                        for (Post p : posts) {
                            PostCard card = new PostCard(p, utenteLoggato, this::loadFeedBySaved, onRefreshCallback);
                            activeCards.add(card);
                            feedContainer.getChildren().add(card);
                        }
                    }
                    scrollPane.setVvalue(0);
                },
                error -> {
                    feedContainer.getChildren().add(
                            createEmptyStateNode("⚠️ Errore",
                                    "Impossibile caricare i post salvati: " + error.getMessage()));
                });
    }

    /**
     * Ferma tutti i media player attivi nelle card e pulisce la lista delle card
     * attive.
     */
    public void stopAllPlayers() {
        Platform.runLater(() -> {
            for (PostCard card : activeCards) {
                card.dispose();
            }
            activeCards.clear();
        });
    }

    /**
     * Crea un nodo UI per rappresentare uno stato vuoto (nessun post).
     *
     * @param title    Titolo del messaggio.
     * @param subtitle Sottotitolo descrittivo.
     * @return VBox configurato.
     */
    private VBox createEmptyStateNode(String title, String subtitle) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle("-fx-background-color: rgba(255,255,255,0.6); -fx-background-radius: 10;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #5D4037;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #8D6E63; -fx-font-style: italic;");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(400);
        subtitleLabel.setAlignment(Pos.CENTER);

        box.getChildren().addAll(titleLabel, subtitleLabel);
        return box;
    }
}
