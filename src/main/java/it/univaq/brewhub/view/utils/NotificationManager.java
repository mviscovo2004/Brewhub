package it.univaq.brewhub.view.utils;

import it.univaq.brewhub.business.NotificaService;
import it.univaq.brewhub.model.Notifica;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.utility.AsyncTaskHelper;
import it.univaq.brewhub.utility.Log;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Gestisce la logica e l'interfaccia delle notifiche.
 * Include il polling automatico per il badge (nuove notifiche) e la gestione
 * del menu a tendina.
 */
public class NotificationManager {

    private final Utente utenteLoggato;
    private final Button btnNotifiche;
    private final NotificaService notificaService = NotificaService.getInstance();
    private final ContextMenu notifDropdown;
    private ScheduledExecutorService scheduler;

    /**
     * Costruisce il NotificationManager.
     *
     * @param stage         Lo stage dell'applicazione.
     * @param utenteLoggato L'utente corrente.
     * @param btnNotifiche  Il pulsante della toolbar dove mostrare il badge.
     */
    public NotificationManager(Stage stage, Utente utenteLoggato, Button btnNotifiche) {
        this.utenteLoggato = utenteLoggato;
        this.btnNotifiche = btnNotifiche;
        this.notifDropdown = new ContextMenu();
        this.notifDropdown.getStyleClass().add("notification-menu");
    }

    /**
     * Inizializza il manager configurando le azioni e avviando il polling in
     * background.
     */
    public void initialize() {
        setupButtonActions();
        startPolling();
    }

    /**
     * Ferma il polling periodico. Da chiamare alla chiusura della vista.
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    private void setupButtonActions() {
        btnNotifiche.setOnAction(e -> showNotifications());
    }

    private void startPolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // Ensure it doesn't block app exit
            return t;
        });

        // Poll every 30 seconds
        scheduler.scheduleAtFixedRate(this::refreshBadge, 0, 30, TimeUnit.SECONDS);
    }

    /**
     * Aggiorna il badge delle notifiche (icona e contatore).
     */
    private void refreshBadge() {
        AsyncTaskHelper.runAsync(
                () -> notificaService.getUnreadCount(utenteLoggato.getUsername()),
                count -> {
                    if (count > 0) {
                        btnNotifiche.setText("🔔 " + count);
                        if (!btnNotifiche.getStyleClass().contains("has-notifications")) {
                            btnNotifiche.getStyleClass().add("has-notifications");
                        }
                    } else {
                        btnNotifiche.setText("🔔");
                        btnNotifiche.getStyleClass().remove("has-notifications");
                    }
                },
                error -> Log.error("Errore polling notifiche", error));
    }

    /**
     * Mostra la lista delle notifiche scaricandole dal server.
     */
    private void showNotifications() {
        AsyncTaskHelper.runAsync(
                () -> notificaService.getUserNotifications(utenteLoggato.getUsername()),
                this::renderNotificationList,
                error -> Log.error("Errore caricamento notifiche logic", error));
    }

    /**
     * Renderizza il contenuto del menu a tendina delle notifiche.
     */
    private void renderNotificationList(List<Notifica> notifiche) {
        notifDropdown.getItems().clear();

        if (notifiche.isEmpty()) {
            Label emptyLbl = new Label("Nessuna notifica recente");
            emptyLbl.getStyleClass().add("notification-empty");
            notifDropdown.getItems().add(new CustomMenuItem(emptyLbl));
        } else {
            for (Notifica n : notifiche) {
                CustomMenuItem item = createNotificationItem(n);
                notifDropdown.getItems().add(item);
            }

            // Footer Actions (Clear All + Mark as Read)
            HBox actionsBox = new HBox(15);
            actionsBox.setPadding(new Insets(8, 12, 8, 12));
            actionsBox.setAlignment(Pos.CENTER);

            Button btnMarkAll = new Button("Segna tutti come letti");
            btnMarkAll.getStyleClass().add("btn-mark-all");
            btnMarkAll.setOnAction(e -> markAllAsRead());

            Button btnClearAll = new Button("Cancella Tutte");
            btnClearAll.getStyleClass().add("small-button");
            btnClearAll.setOnAction(e -> deleteAllNotifications());

            actionsBox.getChildren().addAll(btnMarkAll, btnClearAll);
            CustomMenuItem actionItem = new CustomMenuItem(actionsBox);
            actionItem.setHideOnClick(false);
            notifDropdown.getItems().add(actionItem);
        }

        if (!notifDropdown.isShowing()) {
            notifDropdown.show(btnNotifiche, Side.BOTTOM, 0, 0);
        }
    }

    /**
     * Crea l'item visuale per una singola notifica.
     */
    private CustomMenuItem createNotificationItem(Notifica n) {
        HBox container = new HBox(12);
        container.getStyleClass().add("notification-card");
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPrefWidth(350);
        container.setMinWidth(350);
        container.setMaxWidth(350);

        javafx.scene.shape.Circle statusIndicator = new javafx.scene.shape.Circle(4);
        if (!n.isLetto()) {
            statusIndicator.getStyleClass().add("notification-dot-unread");
        } else {
            statusIndicator.getStyleClass().add("notification-dot-read");
        }

        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.getStyleClass().add("notification-icon-box");
        Label icon = new Label("📣");
        if (n.getMessaggio().toLowerCase().contains("like")) {
            icon.setText("❤️");
            iconBox.getStyleClass().add("icon-like");
        } else if (n.getMessaggio().toLowerCase().contains("comment")) {
            icon.setText("💬");
            iconBox.getStyleClass().add("icon-comment");
        } else if (n.getMessaggio().toLowerCase().contains("benvenuto")) {
            icon.setText("👋");
            iconBox.getStyleClass().add("icon-system");
        }
        icon.getStyleClass().add("notification-emoji");
        iconBox.getChildren().add(icon);

        VBox contentBox = new VBox(2);
        HBox.setHgrow(contentBox, javafx.scene.layout.Priority.ALWAYS);

        HBox headerLine = new HBox(5);
        headerLine.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Notifica");
        if (n.getMessaggio().toLowerCase().contains("like"))
            title.setText("Nuovo Mi Piace");
        else if (n.getMessaggio().toLowerCase().contains("comment"))
            title.setText("Nuovo Commento");
        else
            title.setText("Sistema");

        title.getStyleClass().add("notification-title");

        Label time = new Label(n.getDataCreazione().format(DateTimeFormatter.ofPattern("HH:mm")));
        time.getStyleClass().add("notification-time");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        headerLine.getChildren().addAll(title, spacer, time);

        Label msgLbl = new Label(n.getMessaggio());
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(260); // Slightly less than 300
        msgLbl.getStyleClass().add("notification-body");
        if (!n.isLetto())
            msgLbl.getStyleClass().add("text-unread");

        contentBox.getChildren().addAll(headerLine, msgLbl);

        VBox actionBox = new VBox();
        actionBox.setAlignment(Pos.CENTER);
        Button btnDelete = new Button("✕");
        btnDelete.getStyleClass().add("notification-close-btn");

        actionBox.getChildren().add(btnDelete);

        container.getChildren().addAll(statusIndicator, iconBox, contentBox, actionBox);

        CustomMenuItem item = new CustomMenuItem(container);
        item.setHideOnClick(false);

        btnDelete.setOnAction(ev -> deleteNotification(n, item));

        container.setOnMouseClicked(ev -> {
            if (!n.isLetto()) {
                markAsRead(n, container, msgLbl);
                statusIndicator.getStyleClass().remove("notification-dot-unread");
                statusIndicator.getStyleClass().add("notification-dot-read");
            }
        });

        return item;
    }

    private void markAllAsRead() {
        AsyncTaskHelper.runAsync(
                () -> {
                    try {
                        notificaService.markAllAsRead(utenteLoggato.getUsername());
                        return true;
                    } catch (Exception e) {
                        Log.error("Errore markAllAsRead", e);
                        return false;
                    }
                },
                success -> {
                    if (success) {
                        refreshBadge();
                        showNotifications(); // Ricarica la lista per aggiornare i pallini
                    }
                },
                error -> Log.error("Errore markAllAsRead async failure", error));
    }

    private void markAsRead(Notifica n, HBox container, Label msgLabel) {
        AsyncTaskHelper.runAsync(
                () -> {
                    try {
                        notificaService.markAsRead(n.getId());
                        return true;
                    } catch (Exception e) {
                        Log.error("Errore markAsRead", e);
                        return false;
                    }
                },
                success -> {
                    if (success) {
                        n.setLetto(true);
                        // Update visuals
                        container.getStyleClass().remove("unread");
                        container.getStyleClass().add("read");
                        msgLabel.getStyleClass().remove("unread-text");
                        msgLabel.getStyleClass().add("read-text");
                        refreshBadge();
                    }
                },
                error -> Log.error("Errore markAsRead async failure", error));
    }

    private void deleteNotification(Notifica n, MenuItem item) {
        AsyncTaskHelper.runAsync(
                () -> {
                    try {
                        notificaService.deleteNotification(n.getId());
                        return true;
                    } catch (Exception e) {
                        Log.error("Errore eliminazione notifica", e);
                        return false;
                    }
                },
                success -> {
                    if (success) {
                        notifDropdown.getItems().remove(item);
                        refreshBadge();
                    }
                },
                error -> Log.error("Errore eliminazione notifica async failure", error));
    }

    private void deleteAllNotifications() {
        AsyncTaskHelper.runAsync(
                () -> {
                    try {
                        notificaService.deleteAllNotifications(utenteLoggato.getUsername());
                        return true;
                    } catch (Exception e) {
                        Log.error("Errore eliminazione tutto", e);
                        return false;
                    }
                },
                success -> {
                    if (success) {
                        notifDropdown.getItems().clear();
                        refreshBadge();
                    }
                },
                error -> Log.error("Errore eliminazione tutto async failure", error));
    }
}
