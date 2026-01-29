package it.univaq.brewhub.view.components;

import it.univaq.brewhub.model.Categoria;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.utility.Log;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Componente riutilizzabile per la Sidebar laterale di navigazione.
 * Gestisce la visualizzazione dinamica delle categorie, dei pulsanti di
 * navigazione
 * e la gestione dello stato attivo della sezione corrente.
 */
public class SidebarComponent extends ScrollPane {

    private final Utente utenteLoggato;
    private final VBox content;
    private final Consumer<String> onNavigation; // Callback per gestire il cambio vista
    private String currentSection = "Home";

    // Service
    private final it.univaq.brewhub.business.CategoriaService categoriaService = it.univaq.brewhub.business.CategoriaService
            .getInstance();
    private final it.univaq.brewhub.business.UserService userService = it.univaq.brewhub.business.UserService
            .getInstance();

    /**
     * Costruisce la sidebar.
     *
     * @param utente       L'utente attualmente loggato (per personalizzare le
     *                     voci).
     * @param onNavigation Callback invocato quando l'utente clicca su una voce di
     *                     menu.
     */
    public SidebarComponent(Utente utente, Consumer<String> onNavigation) {
        this.utenteLoggato = utente;
        this.onNavigation = onNavigation;

        this.content = new VBox(0);
        this.content.setPrefWidth(260);
        this.content.getStyleClass().add("sidebar");

        this.setContent(content);
        this.setFitToWidth(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.getStyleClass().add("sidebar-scroll");

        refresh();
    }

    /**
     * Ricarica il contenuto della sidebar, rigenerando i pulsanti e aggiornando i
     * conteggi.
     */
    public void refresh() {
        content.getChildren().clear();

        addLabel("FEEDS");
        addNavButton("🏠  Home", "Home");
        addNavButton("🔥  Popolari", "Popolari");

        if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
            addNavButton("👥  Seguiti", "Followed");
        }

        addSeparator();
        addLabel("COMMUNITY");
        addNavButton("☕  Torrefattori", "Torrefattori");
        addNavButton("📖  Guide", "Guide");
        addNavButton("☕  Miscele", "Miscele");

        try {
            List<Categoria> cats = categoriaService.getAllCategories();
            for (Categoria c : cats) {
                if (c.getNome().equalsIgnoreCase("Torrefattori"))
                    continue;
                if (c.getNome().equalsIgnoreCase("Guide"))
                    continue;
                if (c.getNome().equalsIgnoreCase("Miscele"))
                    continue;
                if (c.getNome().equalsIgnoreCase("Eventi"))
                    continue;
                if (c.getNome().equalsIgnoreCase("Sfide"))
                    continue;

                String icon = c.getIcona() != null && !c.getIcona().isBlank() ? c.getIcona() : "📂";
                addNavButton(icon + "  " + c.getNome(), c.getNome()); // Usa il nome categoria come chiave sezione
            }
        } catch (Exception e) {
            Log.error("Errore caricamento sidebar categorie", e);
        }

        addSeparator();
        addLabel("ATTIVITÀ");
        addNavButton("🎉  Eventi", "Eventi");
        addNavButton("🏆  Sfide", "Sfide");

        if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
            addSeparator();
            addLabel("IL TUO PROFILO");
            // Profilo ha logica speciale (navigazione diretta, non refresh feed)
            // Per semplicità qui usiamo la chiave "Profilo" che HomeView dovrà intercettare
            addNavButton("👤  Profilo", "Profilo");
            addNavButton("❤  Mi piace", "MiPiace");
            addNavButton("💬  Messaggi", "Messaggi");

            try {
                int savedCount = userService.getSavedPostsCount(utenteLoggato.getUsername());
                addNavButton("⭐  Salvati (" + savedCount + ")", "Salvati");
            } catch (Exception e) {
                addNavButton("⭐  Salvati", "Salvati");
            }

            if (utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN) {
                addSeparator();
                addLabel("AMMINISTRAZIONE");
                addNavButton("📊  Dashboard", "Dashboard");
                addNavButton("👥  Gestione Utenti", "GestioneUtenti");
                addNavButton("📂  Gestione Categorie", "GestioneCategorie");
                addNavButton("🗂  Gestione Database", "GestioneDB");
            }
        }
    }

    /**
     * Imposta la sezione attualmente attiva ed evidenzia il relativo pulsante.
     *
     * @param section La chiave della sezione attiva.
     */
    public void setActiveSection(String section) {
        this.currentSection = section;
        refresh(); // Ridisegna per aggiornare lo stato active
    }

    /**
     * Aggiunge un pulsante di navigazione alla sidebar.
     *
     * @param label      Il testo del pulsante.
     * @param sectionKey La chiave identificativa della sezione.
     */
    private void addNavButton(String label, String sectionKey) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("nav-btn");
        if (sectionKey.equals(currentSection)) {
            btn.getStyleClass().add("nav-btn-active");
        }
        btn.setOnAction(e -> {
            currentSection = sectionKey;
            refresh(); // Aggiorna UI locale
            if (onNavigation != null) {
                onNavigation.accept(sectionKey); // Notifica HomeView
            }
        });
        content.getChildren().add(btn);
    }

    /**
     * Aggiunge un'etichetta di sezione (header).
     */
    private void addLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("sidebar-section-label");
        content.getChildren().add(lbl);
    }

    /**
     * Aggiunge un separatore visivo.
     */
    private void addSeparator() {
        Region sep = new Region();
        sep.getStyleClass().add("custom-separator");
        sep.setMinHeight(1);
        VBox box = new VBox(sep);
        box.setPadding(new Insets(10, 20, 10, 20));
        content.getChildren().add(box);
    }
}
