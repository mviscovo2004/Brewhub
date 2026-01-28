package it.univaq.brewhub.view;

import it.univaq.brewhub.business.SfidaService;
import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.model.Sfida;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.view.components.SfidaCard;
import java.time.LocalDate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista per visualizzare e gestire le Sfide (Contest).
 *
 * Mostra le sfide attive e concluse, e permette ai Torrefattori di crearne di
 * nuove.
 *
 */
public class SfideView extends BorderPane {

    private final Utente utenteLoggato;
    private final SfidaService sfidaService = SfidaService.getInstance();
    private VBox challengesContainer;

    /**
     * Costruttore.
     * 
     * @param utenteLoggato L'utente che visualizza la pagina.
     */
    public SfideView(Utente utenteLoggato) {
        this.utenteLoggato = utenteLoggato;
        initUI();
        loadSfide();
    }

    /**
     * Inizializza l'interfaccia utente.
     * Configura il layout, l'intestazione e il contenitore scorrevole per le sfide.
     */
    private void initUI() {
        this.setPadding(new Insets(20));
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("\uD83C\uDFC6 Sfide");
        title.getStyleClass().add("section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer);
        if (utenteLoggato.getTipo() == Utente.TipoUtente.TORREFATTORE
                || utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN) {
            Button btnCreate = new Button("+ Crea Sfida");
            btnCreate.getStyleClass().add("button-primary");
            btnCreate.setOnAction(e -> showCreateSfidaDialog());
            header.getChildren().add(btnCreate);
        }
        this.setTop(header);
        // Container scrollabile
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        challengesContainer = new VBox(15);
        challengesContainer.setAlignment(Pos.TOP_CENTER);
        challengesContainer.setPadding(new Insets(10));
        scrollPane.setContent(challengesContainer);
        this.setCenter(scrollPane);
    }

    /**
     * Carica le sfide dal database e le visualizza nella vista.
     * Separa le sfide in "Attive" e "Concluse" in base alla data di scadenza.
     */
    private void loadSfide() {
        challengesContainer.getChildren().clear();
        List<Sfida> allSfide = sfidaService.getAllChallenges();
        List<Sfida> activeChallenges = new ArrayList<>();
        List<Sfida> pastChallenges = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        java.time.LocalDate now = java.time.LocalDate.now();
        for (Sfida s : allSfide) {
            try {
                // Parsing data scadenza (formato dd/MM/yyyy)
                java.time.LocalDate deadline = java.time.LocalDate.parse(s.getScadenza(), formatter);
                if (deadline.isBefore(now)) {
                    pastChallenges.add(s);
                } else {
                    activeChallenges.add(s);
                }
            } catch (Exception ex) {
                activeChallenges.add(s);
            }
        }
        // Attive
        Label activeLabel = new Label("\uD83D\uDD25 Sfide Attive");
        activeLabel.getStyleClass().add("section-header");
        activeLabel.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3E2723; -fx-padding: 10 0 5 0;");
        challengesContainer.getChildren().add(activeLabel);
        if (activeChallenges.isEmpty()) {
            challengesContainer.getChildren()
                    .add(createEmptyState("\uD83D\uDCA4", "Nessuna sfida attiva al momento."));
        } else {
            for (Sfida s : activeChallenges) {
                challengesContainer.getChildren().add(new SfidaCard(s, utenteLoggato));
            }
        }
        // Separatore
        Region separator = new Region();
        separator.setMinHeight(20);
        challengesContainer.getChildren().add(separator);
        // Concluse
        Label pastLabel = new Label("\uD83C\uDFC6 Sfide Concluse");
        pastLabel.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #5D4037; -fx-padding: 10 0 5 0; -fx-opacity: 0.8;");
        challengesContainer.getChildren().add(pastLabel);
        if (pastChallenges.isEmpty()) {
            challengesContainer.getChildren()
                    .add(createEmptyState("\uD83D\uDCC2", "Nessuna sfida conclusa recente."));
        } else {
            for (Sfida s : pastChallenges) {
                SfidaCard card = new SfidaCard(s, utenteLoggato);
                card.setOpacity(0.7);
                challengesContainer.getChildren().add(card);
            }
        }
    }

    /**
     * Mostra una finestra di dialogo per la creazione di una nuova sfida.
     * Permette di inserire titolo, descrizione, premio e scadenza.
     */
    private void showCreateSfidaDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Nuova Sfida");
        dialog.initOwner(this.getScene().getWindow());
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #FFF8E1;");
        TextField txtTitolo = new TextField();
        txtTitolo.setPromptText("Titolo Sfida");
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Descrizione e Regole");
        txtDesc.setPrefRowCount(3);
        TextField txtPremio = new TextField();
        txtPremio.setPromptText("Premio in palio");
        TextField txtScadenza = new TextField();
        txtScadenza.setPromptText("Scadenza (YYYY-MM-DD)");
        Button btnConfirm = new Button("Lancia Sfida");
        btnConfirm.getStyleClass().add("button-primary");
        btnConfirm.setMaxWidth(Double.MAX_VALUE);
        btnConfirm.setOnAction(e -> {
            if (txtTitolo.getText().isBlank() || txtScadenza.getText().isBlank() || txtPremio.getText().isBlank()) {
                DialogUtils.showError("Attenzione", "Compila tutti i campi obbligatori.", dialog);
                return;
            }
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate deadline = LocalDate.parse(txtScadenza.getText(), formatter);
                if (deadline.isBefore(LocalDate.now())) {
                    DialogUtils.showError("Errore Data", "La scadenza non può essere nel passato!", dialog);
                    return;
                }
            } catch (Exception ex) {
                DialogUtils.showError("Formato Data Errato", "Usa il formato: YYYY-MM-DD", dialog);
                return;
            }
            try {
                Sfida nuovaSfida = new Sfida(
                        txtTitolo.getText(),
                        txtDesc.getText(),
                        txtPremio.getText(),
                        txtScadenza.getText(),
                        utenteLoggato.getUsername());
                sfidaService.createChallenge(nuovaSfida);
                loadSfide();
                dialog.close();
            } catch (BusinessException ex) {
                // Log.error("Errore creazione sfida", ex);
                DialogUtils.showError("Errore", ex.getMessage(), dialog);
            }
        });
        root.getChildren().addAll(
                new Label("Titolo *"), txtTitolo,
                new Label("Premio *"), txtPremio,
                new Label("Scadenza (YYYY-MM-DD) *"), txtScadenza,
                new Label("Descrizione"), txtDesc,
                btnConfirm);
        Scene scene = new Scene(root, 400, 500);
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception ex) {
        }
        dialog.setScene(scene);
        dialog.show();
    }

    /**
     * Crea un componente grafico per lo stato vuoto (nessuna sfida).
     * 
     * @param emoji   Emoji da visualizzare.
     * @param message Messaggio da visualizzare.
     * @return VBox contenente l'emoji e il messaggio.
     */
    private VBox createEmptyState(String emoji, String message) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));
        box.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.5); -fx-background-radius: 10; -fx-border-color: #D7CCC8; -fx-border-radius: 10; -fx-border-style: dashed;");
        Label iconLbl = new Label(emoji);
        iconLbl.setStyle("-fx-font-size: 40px;");
        Label msgLbl = new Label(message);
        msgLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #5D4037; -fx-font-style: italic;");
        box.getChildren().addAll(iconLbl, msgLbl);
        return box;
    }
}
