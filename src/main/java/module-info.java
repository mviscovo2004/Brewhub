/**
 * Modulo principale dell'applicazione BrewHub.
 *
 * BrewHub è un social network dedicato agli appassionati di caffè,
 * che permette di condividere post, recensioni, partecipare a eventi
 * e interagire con torrefattori e altri utenti.
 *
 * 
 * Dipendenze principali:
 *
 * - JavaFX - Framework per l'interfaccia grafica
 * - SQLite (via JDBC) - Database locale
 * - jBCrypt - Hashing delle password
 */
@SuppressWarnings("module") module it.univaq.brewhub {
    requires transitive javafx.controls;
    requires transitive java.sql;
    requires jbcrypt;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.base;

    opens it.univaq.brewhub;
    opens it.univaq.brewhub.UI;
    opens it.univaq.brewhub.UI.components;
    opens it.univaq.brewhub.business;
    opens it.univaq.brewhub.dao;
    opens it.univaq.brewhub.dao.impl;
    opens it.univaq.brewhub.utility;

    exports it.univaq.brewhub;
    exports it.univaq.brewhub.UI;
    exports it.univaq.brewhub.UI.components;
    exports it.univaq.brewhub.business;
    exports it.univaq.brewhub.dao;
    exports it.univaq.brewhub.dao.impl;
    exports it.univaq.brewhub.utility;
    exports it.univaq.brewhub.model;

    opens it.univaq.brewhub.model;
}
