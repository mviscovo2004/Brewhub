package it.univaq.brewhub;

// Importazioni librerie Java e classi del progetto

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Classe che rappresenta un Post nel social network
public class Post {

    // Attributi del Post
    private Utente autore;
    private String titolo;
    private String contenuto;
    private TipoPost tipo;
    private LocalDateTime dataCreazione;
    private String media = null;
    private List<Utente> miPiace = new ArrayList<>();
    private List<Commento> commenti = new ArrayList<>();

    // Enum per i tipi di Post
    public enum TipoPost {

        // Tipi di post supportati
        TESTO("Testo"),
        FOTO("Foto"),
        VIDEO("Video");

        private final String label;

        // Costruttore Enum
        TipoPost(String label) {
            this.label = label;
        }

        // toString per ottenere la rappresentazione testuale
        @Override
        public String toString() {
            return label;
        }
    }

    // --- COSTRUTTORI ---
    // Costruttore completo
    public Post(String titolo, String contenuto, Utente autore, TipoPost tipo, String media) {
        this.titolo = titolo;
        this.contenuto = contenuto;
        this.autore = autore;
        this.tipo = tipo;
        this.media = media;
        this.dataCreazione = LocalDateTime.now();
    }

    // Costruttore vuoto per DB
    public Post() {
    }

    // --- GETTER ---
    // Ritorna l'autore del post
    public Utente getAutore() {
        return autore;
    }

    // Ritorna il titolo del post
    public String getTitolo() {
        return titolo;
    }

    // Ritorna il contenuto del post
    public String getContenuto() {
        return contenuto;
    }

    // Ritorna il tipo del post
    public TipoPost getTipo() {
        return tipo;
    }

    // Ritorna il file multimediale associato al post
    public String getMedia() {
        return media;
    }

    // Ritorna la lista degli utenti che hanno messo "Mi Piace"
    public List<Utente> getMiPiace() {
        return miPiace;
    }

    // Ritorna un singolo utente che ha messo "Mi Piace" in base all'indice
    public Utente getMiPiaceSingolo(int i) {
        return miPiace.get(i);
    }

    // Ritorna la data di creazione del post
    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    // Ritorna la lista dei commenti associati al post
    public List<Commento> getCommenti() {
        return commenti;
    }

    // Ritorna un singolo commento in base all'indice
    public Commento getCommentoSingolo(int i) {
        return commenti.get(i);
    }

    // --- SETTER ---
    // Imposta l'autore del post
    public void setAutore(Utente autore) {
        this.autore = autore;
    }

    // Imposta il titolo del post
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    // Imposta il contenuto del post
    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

    // Imposta il tipo del post
    public void setTipo(TipoPost tipo) {
        this.tipo = tipo;
    }

    // Imposta la data di creazione del post
    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    // Imposta il file multimediale associato al post
    public void setMedia(String media) {
        this.media = media;
    }

    // Imposta la lista degli utenti che hanno messo "Mi Piace"
    public void setMiPiace(List<Utente> miPiace) {
        this.miPiace = miPiace;
    }

    // Imposta un singolo utente che ha messo "Mi Piace" in base all'indice
    public void setMiPiaceSingolo(int i, Utente utente) {
        miPiace.set(i, utente);
    }

    // Imposta la lista dei commenti associati al post
    public void setCommenti(List<Commento> commenti) {
        this.commenti = commenti;
    }

    // Imposta un singolo commento in base all'indice
    public void setCommentoSingolo(int i, Commento commento) {
        commenti.set(i, commento);
    }

    // --- METODI DATBASE ---
    // Metodo per salvare il post nel database
    public void salvaPost() throws SQLException {

        // Query SQL per inserimento post
        String sql = "INSERT INTO post(autore_username, titolo, contenuto, tipo, data_creazione, media_uri) VALUES(?,?,?,?,?,?)";

        // Esegui l'inserimento nel database
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Imposta i parametri della query
            pstmt.setString(1, this.autore.getUsername());
            pstmt.setString(2, this.titolo);
            pstmt.setString(3, this.contenuto);
            pstmt.setString(4, this.tipo.name());
            pstmt.setString(5, this.dataCreazione.toString());

            // Salva il percorso relativo del media (es. media/uuid.ext)
            pstmt.setString(6, this.media != null ? this.media.replace('\\', '/') : null);

            // Esegui l'inserimento
            pstmt.executeUpdate();
        }
    }

    // Metodo statico per caricare tutti i post dal database
    public static List<Post> caricaTuttiPost() throws SQLException {

        // Lista per memorizzare i post caricati
        List<Post> posts = new ArrayList<>();

        // Query SQL per selezionare tutti i post ordinati per data creazione decrescente
        String sql = "SELECT * FROM post ORDER BY data_creazione DESC";

        // Esegui la query e carica i post
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            // Itera sui risultati della query
            while (rs.next()) {

                // Crea un nuovo oggetto Post
                Post post = new Post();
                
                // Carica l'autore dal database
                String usernameAutore = rs.getString("autore_username");

                // Imposta i campi del post
                Utente autore = new Utente();
                autore.setUsername(usernameAutore);
                post.setAutore(autore);
                post.setTitolo(rs.getString("titolo"));
                post.setContenuto(rs.getString("contenuto"));
                String tipoStr = rs.getString("tipo");
                
                // Parsing sicuro del tipo di post
                try {

                    // Converte la stringa in TipoPost
                    post.setTipo(TipoPost.valueOf(tipoStr));
                } catch (IllegalArgumentException e) {
                    // Tipo non valido, gestisci l'errore
                    throw e;
                }
                
                // Imposta la data di creazione
                post.setDataCreazione(LocalDateTime.parse(rs.getString("data_creazione")));
                
                // Carica il file multimediale se presente
                String mediaUri = rs.getString("media_uri");

                // Se è presente un media URI
                if (mediaUri != null) {

                    post.setMedia(mediaUri);
                }
                
                // Aggiungi il post alla lista
                posts.add(post);
            }
        }

        // Ritorna la lista dei post caricati
        return posts;
    }

}
