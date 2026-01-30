package it.univaq.brewhub.view.admin;

import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.business.UserService;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.view.DialogUtils;
import it.univaq.brewhub.view.UserProfileView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.List;

/**
 * Vista per la gestione degli utenti da parte dell'amministratore.
 * Consente di visualizzare e cancellare gli utenti registrati.
 */
public class UserManagementView extends VBox {

    private final Stage stage;
    private final Utente utenteLoggato;
    private final UserService userService = UserService.getInstance();

    /**
     * Costruisce la vista di gestione utenti.
     *
     * @param stage         Lo stage dell'applicazione.
     * @param utenteLoggato L'utente amministratore corrente.
     */
    public UserManagementView(Stage stage, Utente utenteLoggato) {
        this.stage = stage;
        this.utenteLoggato = utenteLoggato;
        initUI();
    }

    /**
     * Inizializza l'interfaccia utente.
     */
    private void initUI() {
        this.setSpacing(10);
        this.setPadding(new Insets(20));

        Label title = new Label("👤 Gestione Utenti");
        title.getStyleClass().add("section-title");
        this.getChildren().add(title);

        performSearch("");
    }

    /**
     * Esegue la ricerca degli utenti (attualmente mostra tutti gli utenti se query
     * è vuota)
     * e aggiorna la lista visuale.
     *
     * @param query Stringa di ricerca (non usata al momento, ma predisposta per
     *              estensioni).
     */
    private void performSearch(String query) {
        this.getChildren().removeIf(node -> node instanceof VBox); // Clear previous results container

        try {
            List<Utente> allUsers = userService.searchUsers(query);
            if (allUsers.isEmpty()) {
                this.getChildren().add(new Label("Nessun utente trovato."));
                return;
            }

            VBox usersContainer = new VBox(10);
            for (Utente u : allUsers) {
                if (u.getUsername().startsWith("deleted_"))
                    continue;

                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4,0,0,2);");
                row.setMaxWidth(Double.MAX_VALUE);

                Circle avatar = new Circle(24);
                String initial = u.getUsername().isEmpty() ? "?" : u.getUsername().substring(0, 1).toUpperCase();
                Label initLbl = new Label(initial);
                initLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
                StackPane avStack = new StackPane(avatar, initLbl);
                avatar.setFill(javafx.scene.paint.Color.web("#8D6E63"));

                VBox info = new VBox(4);
                Label usernameLbl = new Label("@" + u.getUsername() + " [" + u.getTipo() + "]");
                usernameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #3E2723;");
                Label nameLbl = new Label(u.getNome() + " " + u.getCognome());
                nameLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #795548;");
                info.getChildren().addAll(usernameLbl, nameLbl);

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                HBox actions = new HBox(10);
                Button btnProfile = new Button("Visita");
                btnProfile.getStyleClass().add("button-secondary");
                btnProfile.setOnAction(e -> {
                    UserProfileView upv = new UserProfileView(stage, utenteLoggato, u);
                    stage.getScene().setRoot(upv.getView());
                });
                actions.getChildren().add(btnProfile);

                if (!u.getUsername().equals(utenteLoggato.getUsername())
                        && utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN
                        && u.getTipo() != Utente.TipoUtente.ADMIN) {
                    Button btnDelete = new Button("Elimina");
                    btnDelete.getStyleClass().add("button-danger");
                    btnDelete.setStyle("-fx-background-color: #e57373; -fx-text-fill: white;");
                    btnDelete.setOnAction(e -> {
                        boolean confirmed = DialogUtils.showConfirmation("Eliminazione Profilo",
                                "Eliminare utente " + u.getUsername() + "?", stage);
                        if (confirmed) {
                            try {
                                userService.deleteUser(u.getUsername());
                                performSearch(query);
                            } catch (BusinessException ex) {
                                DialogUtils.showError("Errore Eliminazione", ex.getMessage(), stage);
                            }
                        }
                    });
                    actions.getChildren().add(btnDelete);
                }
                row.getChildren().addAll(avStack, info, sp, actions);
                usersContainer.getChildren().add(row);
            }
            this.getChildren().add(usersContainer);
        } catch (Exception e) {
            DialogUtils.showError("Errore", e.getMessage(), stage);
        }
    }
}
