package it.univaq.brewhub.view.components;

import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.view.DialogUtils;
import it.univaq.brewhub.utility.MediaManager;
import it.univaq.brewhub.business.PostService;
import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.utility.Log;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Componente UI che rappresenta un singolo post nel feed.
 * Aggrega informazioni sull'autore, contenuto (testo/media), azioni (like,
 * commenti) e recensioni.
 */
public class PostCard extends BaseCard {
    private final Post post;
    private final Utente utenteLoggato;
    private final Runnable onRefreshNeeded;
    private final Runnable onSaveAction;
    private final PostService postService = PostService.getInstance();

    private PostMediaComponent mediaComponent;

    /**
     * Costruisce una nuova card per un post.
     *
     * @param post            Il post da visualizzare.
     * @param utenteLoggato   L'utente attualmente loggato.
     * @param onRefreshNeeded Callback da eseguire quando è necessario aggiornare la
     *                        vista (es. post eliminato).
     */
    public PostCard(Post post, Utente utenteLoggato, Runnable onRefreshNeeded) {
        this(post, utenteLoggato, onRefreshNeeded, null);
    }

    /**
     * Costruisce una nuova card per un post con azione di salvataggio opzionale.
     *
     * @param post            Il post da visualizzare.
     * @param utenteLoggato   L'utente attualmente loggato.
     * @param onRefreshNeeded Callback per il refresh.
     * @param onSaveAction    Callback per l'azione di salvataggio.
     */
    public PostCard(Post post, Utente utenteLoggato, Runnable onRefreshNeeded, Runnable onSaveAction) {
        super();
        this.post = post;
        this.utenteLoggato = utenteLoggato;
        this.onRefreshNeeded = onRefreshNeeded;
        this.onSaveAction = onSaveAction;
        initUI();
    }

    /**
     * Inizializza l'interfaccia utente componendo header, contenuto, media,
     * recensioni, azioni e commenti.
     */
    private void initUI() {
        // Init style base da BaseCard
        // --- HEADER ---
        HBox header = createHeader();

        // --- TITLE ---
        Label titleLbl = createTitleLabel(post.getTitolo());

        this.getChildren().addAll(header, titleLbl);

        // --- MEDIA ---
        if (post.getMedia() != null && !post.getMedia().isBlank()) {
            mediaComponent = new PostMediaComponent(post);
            this.getChildren().add(mediaComponent);
        }

        // --- CONTENT ---
        if (post.getContenuto() != null) {
            Label content = createContentLabel(post.getContenuto());
            this.getChildren().add(content);
        }

        // --- REVIEW SECTION ---
        if (post.getCategoria() != null && "Miscele".equalsIgnoreCase(post.getCategoria().getNome())) {
            createReviewSection();
        }

        // --- ACTIONS BAR ---
        PostActionsComponent actions = new PostActionsComponent(post, utenteLoggato, onSaveAction);
        this.getChildren().add(actions);

        // --- COMMENTS SECTION ---
        PostCommentsComponent comments = new PostCommentsComponent(post, utenteLoggato);
        this.getChildren().add(comments);
    }

    /**
     * Crea l'header del post contenente avatar, nome autore, data e badge
     * opzionali.
     *
     * @return HBox contenente l'header configurato.
     */
    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarContainer = new StackPane();
        Circle avatar = new Circle(20);
        boolean imageLoaded = false;
        try {
            String foto = post.getAutore().getFotoProfilo();
            if (foto != null && !foto.isBlank()) {
                File f = MediaManager.getMediaFile(foto);
                if (f != null && f.exists()) {
                    Image img = new Image(f.toURI().toString(), true);
                    avatar.setFill(new javafx.scene.paint.ImagePattern(img));
                    imageLoaded = true;
                }
            }
        } catch (Exception e) {
        }
        if (!imageLoaded) {
            String username = post.getAutore().getUsername();
            int hash = username.hashCode();
            int r = (hash & 0xFF0000) >> 16;
            int g = (hash & 0x00FF00) >> 8;
            int b = hash & 0x0000FF;
            javafx.scene.paint.Color color = javafx.scene.paint.Color.rgb(Math.abs(r) % 255, Math.abs(g) % 255,
                    Math.abs(b) % 255);
            avatar.setFill(color);
            avatar.setOpacity(0.7);
            String initial = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();
            Label initialLbl = new Label(initial);
            initialLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
            avatarContainer.getChildren().addAll(avatar, initialLbl);
        } else {
            avatarContainer.getChildren().add(avatar);
        }

        String displayAuthor = post.getAutore().getUsername();
        if (displayAuthor.startsWith("deleted_")) {
            displayAuthor = "Utente eliminato";
        }
        Label authorLbl = new Label(displayAuthor);
        authorLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #3E2723;");
        if (!displayAuthor.equals("Utente eliminato")) {
            authorLbl.setCursor(javafx.scene.Cursor.HAND);
            avatarContainer.setCursor(javafx.scene.Cursor.HAND);
            javafx.event.EventHandler<javafx.scene.input.MouseEvent> navHandler = e -> {
                if (this.getScene() != null && this.getScene().getWindow() instanceof javafx.stage.Stage) {
                    javafx.stage.Stage s = (javafx.stage.Stage) this.getScene().getWindow();
                    this.dispose(); // Importante pulizia risorse (es. video)
                    it.univaq.brewhub.view.UserProfileView profileView = new it.univaq.brewhub.view.UserProfileView(s,
                            utenteLoggato, post.getAutore());
                    s.getScene().setRoot(profileView.getView());
                }
            };
            authorLbl.setOnMouseClicked(navHandler);
            avatarContainer.setOnMouseClicked(navHandler);
        }

        HBox authorBox = new HBox(5, authorLbl);
        authorBox.setAlignment(Pos.CENTER_LEFT);
        if (post.getAutore().getTipo() == Utente.TipoUtente.TORREFATTORE) {
            VerificationBadge badge = new VerificationBadge(16);
            authorBox.getChildren().add(badge);
        }

        Label userTypeBadge = null;
        if (post.getAutore() != null && post.getAutore().getTipo() != null) {
            userTypeBadge = new Label(post.getAutore().getTipo().toString());
            userTypeBadge.getStyleClass().addAll("badge", "badge-user-type");
        }

        Label categoryBadge = null;
        if (post.getCategoria() != null) {
            categoryBadge = new Label(post.getCategoria().getNome());
            categoryBadge.getStyleClass().addAll("badge", "badge-category");
        }

        Label dateLbl = new Label(post.getDataCreazione().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateLbl.setStyle("-fx-font-size: 10px; -fx-opacity: 0.6;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDelete = null;
        if (post.getAutore().getUsername().equals(utenteLoggato.getUsername())
                || utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN) {
            btnDelete = new Button("🗑");
            btnDelete.getStyleClass().addAll("button", "post-delete-btn");
            btnDelete.setOnAction(e -> {
                boolean confirmed = DialogUtils.showConfirmation("Elimina Post", "Eliminare questo post?",
                        this.getScene().getWindow());
                if (confirmed) {
                    try {
                        postService.deletePost(post.getId());
                        if (onRefreshNeeded != null)
                            onRefreshNeeded.run();
                    } catch (BusinessException ex) {
                        DialogUtils.showError("Errore", ex.getMessage(), this.getScene().getWindow());
                    }
                }
            });
        }

        header.getChildren().addAll(avatarContainer, authorBox);
        if (userTypeBadge != null) {
            header.getChildren().add(userTypeBadge);
        }
        if (categoryBadge != null) {
            header.getChildren().add(categoryBadge);
        }
        header.getChildren().add(dateLbl);
        header.getChildren().add(spacer);
        if (btnDelete != null) {
            header.getChildren().add(btnDelete);
        }
        return header;
    }

    /**
     * Crea la sezione recensioni (visibile solo per post di categoria "Miscele").
     */
    private void createReviewSection() {
        VBox reviewBox = new VBox(0);
        reviewBox.getStyleClass().add("review-section");

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getStyleClass().add("review-header-box");

        double avg = 0.0;
        try {
            avg = postService.getAverageRating(post.getId());
        } catch (Exception e) {
            Log.error("Errore media recensioni", e);
        }

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("Recensioni Miscela");
        lblTitle.getStyleClass().add("review-title-label");
        Label lblAvg = new Label(String.format("%.1f ⭐", avg));
        lblAvg.getStyleClass().add("review-avg-badge");

        // Calcolo classe CSS per colore basato sul voto
        int roundedScore = (int) Math.round(avg);
        if (roundedScore < 0)
            roundedScore = 0;
        if (roundedScore > 5)
            roundedScore = 5;

        headerBox.getStyleClass().add("header-score-" + roundedScore);
        lblAvg.getStyleClass().add("badge-score-" + roundedScore);
        reviewBox.getStyleClass().add("section-score-" + roundedScore);

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getChildren().addAll(lblTitle, lblAvg);
        titleBox.getChildren().add(titleRow);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnReview = new Button("Lascia Recensione");
        btnReview.getStyleClass().add("review-btn");
        btnReview.getStyleClass().add("btn-score-" + roundedScore);

        boolean canReview = false;
        try {
            if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE &&
                    !post.getAutore().getUsername().equals(utenteLoggato.getUsername()) &&
                    !postService.hasUserReviewed(post.getId(), utenteLoggato.getUsername())) {
                canReview = true;
            }
        } catch (Exception e) {
        }

        if (!canReview) {
            btnReview.setDisable(true);
            if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {
                btnReview.setTooltip(new Tooltip("Accedi per recensire"));
            } else if (post.getAutore().getUsername().equals(utenteLoggato.getUsername())) {
                btnReview.setTooltip(new Tooltip("Non puoi recensire la tua miscela"));
            } else {
                btnReview.setText("Grazie!");
            }
        }
        btnReview.setOnAction(e -> showAddReviewDialog());

        headerBox.getChildren().addAll(titleBox, spacer, btnReview);
        reviewBox.getChildren().add(headerBox);

        int count = getReviewsCount();
        TitledPane pane = new TitledPane("Visualizza " + count + " Recensioni", createReviewList());
        pane.setExpanded(false);
        pane.getStyleClass().add("review-titled-pane");
        reviewBox.getChildren().add(pane);

        this.getChildren().add(reviewBox);
    }

    /**
     * Recupera il numero di recensioni per il post corrente.
     * 
     * @return Il numero di recensioni.
     */
    private int getReviewsCount() {
        try {
            return postService.getReviews(post.getId()).size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Crea la lista visuale delle recensioni.
     * 
     * @return VBox contenente le recensioni o un messaggio se vuoto.
     */
    private VBox createReviewList() {
        VBox list = new VBox();
        list.getStyleClass().add("review-list-container");
        List<it.univaq.brewhub.model.Recensione> reviews = postService.getReviews(post.getId());
        if (reviews.isEmpty()) {
            Label emptyLbl = new Label("Nessuna recensione ancora. Sii il primo!");
            emptyLbl.setStyle("-fx-font-style: italic; -fx-text-fill: #8D6E63; -fx-padding: 10;");
            list.getChildren().add(emptyLbl);
        } else {
            for (it.univaq.brewhub.model.Recensione r : reviews) {
                VBox item = new VBox(5);
                item.getStyleClass().add("review-card");
                HBox rHeader = new HBox(10);
                rHeader.setAlignment(Pos.CENTER_LEFT);
                Label avatar = new Label(r.getAutore().getUsername().substring(0, 1).toUpperCase());
                avatar.getStyleClass().add("review-avatar");
                avatar.getStyleClass().add("bg-score-" + r.getVoto());
                VBox metaBox = new VBox(2);
                Label rUser = new Label(r.getAutore().getUsername());
                rUser.getStyleClass().add("review-user-name");
                String stars = "⭐".repeat(r.getVoto());
                Label rVote = new Label(stars);
                rVote.getStyleClass().add("review-stars");
                metaBox.getChildren().addAll(rUser, rVote);
                rHeader.getChildren().addAll(avatar, metaBox);
                Label rText = new Label(r.getTesto());
                rText.setWrapText(true);
                rText.getStyleClass().add("review-text");
                item.getChildren().addAll(rHeader, rText);
                list.getChildren().add(item);
            }
        }
        return list;
    }

    /**
     * Mostra il dialogo per aggiungere una nuova recensione.
     */
    private void showAddReviewDialog() {
        it.univaq.brewhub.view.utils.ReviewDialogManager.showAddReviewDialog(this.getScene().getWindow(), utenteLoggato,
                post, onRefreshNeeded);
    }

    /**
     * Rilascia le risorse associate alla card, ad esempio fermando i player video.
     * Deve essere chiamato quando la card viene rimossa dalla scena o si naviga
     * altrove.
     */
    public void dispose() {
        if (mediaComponent != null) {
            mediaComponent.dispose();
        }
    }
}
