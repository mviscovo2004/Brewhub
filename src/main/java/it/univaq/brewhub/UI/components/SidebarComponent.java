package it.univaq.brewhub.UI.components;

import it.univaq.brewhub.Categoria;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.dao.impl.CategoriaDAOImpl;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.utility.Log;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Componente riutilizzabile per la Sidebar laterale.
 *
 * Mostra le opzioni di navigazione (Home, Esplora, Profilo, ecc.) e evidenzia
 * la sezione attiva.
 * Utilizza una callback per gestire il cambio di vista nel componente padre.
 *
 */
public class SidebarComponent extends ScrollPane {

    private final Utente utenteLoggato;
    private final VBox content;
    private final Consumer<String> onNavigation; // Callback per gestire il cambio vista
    private String currentSection = "Home";

    // DAO diretti per ora, in un refactoring successivo passerebbero ai Service
    private final CategoriaDAOImpl categoriaDAO = new CategoriaDAOImpl();
    private final UtenteDAOImpl utenteDAO = new UtenteDAOImpl();

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

    public void refresh() {
        content.getChildren().clear();

        addLabel("FEEDS");
        addNavButton("\uD83C\uDFE0  Home", "Home");
        addNavButton("\uD83D\uDD25  Popolari", "Popolari");

        if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
            addNavButton("\uD83D\uDC65  Seguiti", "Followed");
        }

        addSeparator();
        addLabel("COMMUNITY");
        addNavButton("\u2615  Torrefattori", "Torrefattori");

        try {
            List<Categoria> cats = categoriaDAO.findAll();
            for (Categoria c : cats) {
                if (c.getNome().equalsIgnoreCase("Torrefattori"))
                    continue;
                if (c.getNome().equalsIgnoreCase("Eventi"))
                    continue; // Gestiti separatamente

                String icon = c.getIcona() != null && !c.getIcona().isBlank() ? c.getIcona() : "\uD83D\uDCC2";
                addNavButton(icon + "  " + c.getNome(), c.getNome()); // Usa il nome categoria come chiave sezione
            }
        } catch (SQLException e) {
            Log.error("Errore caricamento sidebar categorie", e);
        }

        addSeparator();
        addLabel("ATTIVITÀ");
        addNavButton("\uD83C\uDF89  Eventi", "Eventi");
        addNavButton("\uD83C\uDFC6  Sfide", "Sfide");

        if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
            addSeparator();
            addLabel("IL TUO PROFILO");
            // Profilo ha logica speciale (navigazione diretta, non refresh feed)
            // Per semplicità qui usiamo la chiave "Profilo" che HomeView dovrà intercettare
            addNavButton("\uD83D\uDC64  Profilo", "Profilo");
            addNavButton("\u2764  Mi piace", "MiPiace");
            addNavButton("\uD83D\uDCAC  Messaggi", "Messaggi");

            try {
                int savedCount = utenteDAO.getNumSavedPosts(utenteLoggato.getUsername());
                addNavButton("\u2B50  Salvati (" + savedCount + ")", "Salvati");
            } catch (SQLException e) {
                addNavButton("\u2B50  Salvati", "Salvati");
            }

            if (utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN) {
                addSeparator();
                addLabel("AMMINISTRAZIONE");
                addNavButton("\uD83D\uDCCA  Dashboard", "Dashboard");
                addNavButton("\uD83D\uDC65  Gestione Utenti", "GestioneUtenti");
                addNavButton("\uD83D\uDCC2  Gestione Categorie", "GestioneCategorie");
                addNavButton("\uD83D\uDCC1  Gestione Database", "GestioneDB");
            }
        }
    }

    public void setActiveSection(String section) {
        this.currentSection = section;
        refresh(); // Ridisegna per aggiornare lo stato active
    }

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

    private void addLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("sidebar-section-label");
        content.getChildren().add(lbl);
    }

    private void addSeparator() {
        Region sep = new Region();
        sep.getStyleClass().add("custom-separator");
        sep.setMinHeight(1);
        VBox box = new VBox(sep);
        box.setPadding(new Insets(10, 20, 10, 20));
        content.getChildren().add(box);
    }
}
