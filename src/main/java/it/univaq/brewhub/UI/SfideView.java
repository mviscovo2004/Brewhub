package it.univaq.brewhub.UI;

import it.univaq.brewhub.model.Sfida;
import it.univaq.brewhub.UI.components.SfidaCard;
import it.univaq.brewhub.dao.impl.SfidaDAOImpl;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.utility.Log;
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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SfideView extends BorderPane {

    private final Utente utenteLoggato;
    private final SfidaDAOImpl sfidaDAO = new SfidaDAOImpl();
    private VBox sfideContainer;

    public SfideView(Utente utenteLoggato) {
        this.utenteLoggato = utenteLoggato;
        initUI();
        loadSfide();
    }

    private void initUI() {
        this.setPadding(new Insets(20));

        // Header
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

        sfideContainer = new VBox(15);
        sfideContainer.setAlignment(Pos.TOP_CENTER);
        sfideContainer.setPadding(new Insets(10));

        scrollPane.setContent(sfideContainer);
        this.setCenter(scrollPane);
    }

    private void loadSfide() {
        sfideContainer.getChildren().clear();
        try {
            List<Sfida> allSfide = sfidaDAO.findAll();

            List<Sfida> active = new ArrayList<>();
            List<Sfida> expired = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate now = LocalDate.now();

            for (Sfida s : allSfide) {
                try {
                    LocalDate deadline = LocalDate.parse(s.getScadenza(), formatter);
                    if (deadline.isBefore(now)) {
                        expired.add(s);
                    } else {
                        active.add(s);
                    }
                } catch (Exception ex) {
                    active.add(s);
                }
            }

            // Section: Active
            Label activeLabel = new Label("\uD83D\uDD25 Sfide Attive");
            activeLabel.setStyle(
                    "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #E65100; -fx-padding: 10 0 5 0;");
            sfideContainer.getChildren().add(activeLabel);

            if (active.isEmpty()) {
                sfideContainer.getChildren().add(createEmptyState("\uD83D\uDD25", "Nessuna sfida attiva al momento."));
            } else {
                for (Sfida s : active) {
                    sfideContainer.getChildren().add(new SfidaCard(s, utenteLoggato));
                }
            }

            // Separator
            Region separator = new Region();
            separator.setMinHeight(20);
            sfideContainer.getChildren().add(separator);

            // Section: Expired
            Label expiredLabel = new Label("\uD83C\uDFC1 Sfide Concluse");
            expiredLabel.setStyle(
                    "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #5D4037; -fx-padding: 10 0 5 0; -fx-opacity: 0.8;");
            sfideContainer.getChildren().add(expiredLabel);

            if (expired.isEmpty()) {
                sfideContainer.getChildren().add(createEmptyState("\uD83C\uDFC1", "Nessuna sfida conclusa recente."));
            } else {
                for (Sfida s : expired) {
                    SfidaCard card = new SfidaCard(s, utenteLoggato);
                    card.setOpacity(0.7);
                    sfideContainer.getChildren().add(card);
                }
            }

        } catch (SQLException e) {
            Log.error("Errore caricamento sfide", e);
            DialogUtils.showError("Errore", "Impossibile caricare le sfide.", this.getScene().getWindow());
        }
    }

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
                sfidaDAO.create(nuovaSfida);
                loadSfide();
                dialog.close();
            } catch (SQLException ex) {
                Log.error("Errore creazione sfida", ex);
                DialogUtils.showError("Errore", "Impossibile creare la sfida.", dialog);
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
