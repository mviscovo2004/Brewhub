# BrewHub ☕

**BrewHub** è un social network verticale dedicato agli amanti del caffè. La piattaforma connette baristi, torrefattori, appassionati e curiosi, offrendo uno spazio per condividere esperienze, scoprire nuove miscele e partecipare a eventi esclusivi.

## 🚀 Funzionalità Principali

### 👤 Profili e Autenticazione
*   **Ruoli Utente**: Registrazione diversificata per **Baristi**, **Appassionati**, **Torrefattori** (con badge di verifica) e **Curiosi**.
*   **Accesso Ospite**: Esplorazione limitata per i non registrati.
*   **Profilo Personale**: Gestione bio, foto profilo e archivio post personali.
*   **Social Graph**: Sistema di *Follower* e *Following* per costruire la propria rete.

### 📰 Feed e Contenuti
*   **Post Multimediali**: Creazione di post con **Testo**, **Foto** e **Video**.
*   **Feed Intelligente**:
    *   **Home**: Mix di post recenti e dai seguiti.
    *   **Popolari**: I post con più interazioni.
    *   **Esplora**: Navigazione per categorie (es. *Latte Art*, *Miscele*, *Espresso*).
*   **Interazioni**: Like e Commenti in tempo reale su tutti i contenuti.

### 💬 Comunicazione & Community
*   **Chat System**: Messaggistica privata e **Chat di Gruppo**.
*   **Condivisione**: Inoltro rapido dei post nelle conversazioni.
*   **Eventi**: I Torrefattori possono organizzare edere eventi (degustazioni, corsi) con gestione delle partecipazioni.
*   **Sfide (Contest)**: Competizioni tematiche attive/concluse per ingaggiare la community.

### 🎨 Design
*   **Coffee Theme**: Interfaccia grafica curata (JavaFX + CSS) con palette colori ispirata al mondo del caffè.
*   **Responsive Layout**: Adattamento fluido dei componenti (Sidebar, Feed, Dettagli).

## 🛠 Tecnologia

Il progetto è basato su **Java 11** e utilizza le seguenti tecnologie:

*   **JavaFX**: Framework per la GUI desktop.
*   **SQLite**: Database relazionale embedded (nessuna configurazione server richiesta).
*   **JDBC**: Per l'interazione diretta ed efficiente con il database.
*   **jBCrypt**: Per la sicurezza delle password.
*   **Maven**: Build automation e dependency management.
*   **JUnit 5 & TestFX**: Suite di testing unitario e di interfaccia.

## 📦 Installazione e Avvio

### Prerequisiti
*   [Java JDK 11+](https://www.oracle.com/java/technologies/downloads/)
*   [Maven](https://maven.apache.org/)

### Quick Start

1.  **Clona il repository**:
    ```bash
    git clone https://github.com/mviscovo2004/brewhub.git
    cd brewhub
    ```

2.  **Compila il progetto**:
    ```bash
    mvn clean install
    ```

3.  **Esegui l'applicazione**:
    ```bash
    mvn javafx:run
    ```

> **Nota**: Al primo avvio, il database `brewhub.db` verrà creato automaticamente nella directory utente o locale.

## 🧪 Testing

Per eseguire l'intera suite di test (Unitari + UI):

```bash
mvn test
```

## 📂 Struttura del Progetto

*   `src/main/java`:
    *   `it.univaq.brewhub.UI`: Logica di visualizzazione (Viste, Componenti Custom).
    *   `it.univaq.brewhub.business`: Logica di dominio.
    *   `it.univaq.brewhub.dao`: Data Access Object (Interfacce e Implementazioni).
    *   `it.univaq.brewhub.model`: Classi del modello dati.
*   `src/test/java`: Test cases JUnit.
*   `src/main/resources`: Foglio di stile css

## 🎓 Contesto Accademico

Questo progetto è stato realizzato come parte del corso di **Metodi di Sviluppo Agile** presso l'**Università degli Studi dell'Aquila**. L'obiettivo è applicare metodologie agili e pratiche di clean code nello sviluppo di un'applicazione Java completa.


