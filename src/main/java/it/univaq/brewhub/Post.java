package it.univaq.brewhub;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class Post {

    private Utente autore;

    private String titolo;

    private String contenuto;
    private TipoPost tipo;
    private LocalDateTime dataCreazione;
    private File media = null;
    private List<Utente> miPiace = new ArrayList<Utente>();

    private List<Commento> commenti = new ArrayList<Commento>();

    public enum TipoPost {
        TESTO("Testo"),
        FOTO("Foto"),
        VIDEO("Video");

        private final String label;

        TipoPost(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    // Costruttore principale
    public Post(String titolo, String contenuto, Utente autore, TipoPost tipo, File media) {
        this.titolo = titolo;
        this.contenuto = contenuto;
        this.autore = autore;
        this.tipo = tipo;
        this.media = media;
        this.dataCreazione = LocalDateTime.now();
    }

    // Costruttore vuoto per JSON
    public Post() {
    }

    // GETTER
    public Utente getAutore() {
        return autore;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getContenuto() {
        return contenuto;
    }

    public TipoPost getTipo() {
        return tipo;
    }

    public File getMedia() {
        return media;
    }

    public List<Utente> getMiPiace() {
        return miPiace;
    }

    public Utente getMiPiaceSingolo(int i) {
        return miPiace.get(i);
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    public List<Commento> getCommenti() {
        return commenti;
    }

    public Commento getCommentoSingolo(int i) {
        return commenti.get(i);
    }

    // --- SETTER ---
    public void setAutore(Utente autore) {
        this.autore = autore;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

    public void setTipo(TipoPost tipo) {
        this.tipo = tipo;
    }

    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public void setMedia(File media) {
        this.media = media;
    }

    public void setMiPiace(List<Utente> miPiace) {
        this.miPiace = miPiace;
    }

    public void setMiPiaceSingolo(int i, Utente utente) {
        miPiace.set(i, utente);
    }

    public void setCommenti(List<Commento> commenti) {
        this.commenti = commenti;
    }

    public void setCommentoSingolo(int i, Commento commento) {
        commenti.set(i, commento);
    }

    // --- METODO PER SALVARE IL POST NEL DATABASE ---
    public void salvaPost() throws SQLException {
        String sql = "INSERT INTO post(autore_username, titolo, contenuto, tipo, data_creazione, media_uri) VALUES(?,?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, this.autore.getUsername());
            pstmt.setString(2, this.titolo);
            pstmt.setString(3, this.contenuto);
            pstmt.setString(4, this.tipo.name());
            pstmt.setString(5, this.dataCreazione.toString());
            // Salva il percorso relativo del media (es. media/uuid.ext)
            pstmt.setString(6, this.media != null ? this.media.getPath().replace('\\', '/') : null);

            pstmt.executeUpdate();
        }
    }

    // --- METODO PER CARICARE TUTTI I POST DAL DATABASE ---
    public static List<Post> caricaTuttiPost() throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM post ORDER BY data_creazione DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Post post = new Post();
                
                // Carica l'autore dal database
                String usernameAutore = rs.getString("autore_username");
                Utente autore = new Utente();
                autore.setUsername(usernameAutore);
                post.setAutore(autore);
                
                post.setTitolo(rs.getString("titolo"));
                post.setContenuto(rs.getString("contenuto"));
                
                String tipoStr = rs.getString("tipo");
                System.out.println("DEBUG Post - Tipo letto dal DB: " + tipoStr);
                try {
                    post.setTipo(TipoPost.valueOf(tipoStr));
                } catch (IllegalArgumentException e) {
                    System.out.println("DEBUG Post - Errore nel parsing del tipo post: " + tipoStr);
                    throw e;
                }
                
                post.setDataCreazione(LocalDateTime.parse(rs.getString("data_creazione")));
                
                String mediaUri = rs.getString("media_uri");
                if (mediaUri != null) {
                    // Usa MediaManager per risolvere il percorso relativo
                    File mediaFile = MediaManager.getMediaFile(mediaUri);
                    post.setMedia(mediaFile);
                }
                
                posts.add(post);
            }
        }

        return posts;
    }

}
