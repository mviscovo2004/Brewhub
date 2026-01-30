package it.univaq.brewhub.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe che rappresenta un Post all'interno della piattaforma BrewHub.
 * 
 * Un post è l'unità di contenuto principale e può essere di tipo Testo, Foto o
 * Video.
 * Ogni post è creato da un Autore e può appartenere a una Categoria.
 * Supporta interazioni sociali come "Mi Piace" e "Commenti".
 */
public class Post {

    /**
     * Identificativo univoco del post.
     */
    private int id;

    /**
     * L'utente che ha creato il post.
     */
    private Utente autore;

    /**
     * Il titolo del post.
     */
    private String titolo;

    /**
     * Il contenuto testuale o la descrizione del post.
     */
    private String contenuto;

    /**
     * La tipologia del post (es. TESTO, FOTO, VIDEO).
     */
    private TipoPost tipo;

    /**
     * Data e ora di creazione del post.
     */
    private LocalDateTime dataCreazione = LocalDateTime.now();

    /**
     * Percorso o URI del file multimediale associato (se presente).
     */
    private String media = null;

    /**
     * Categoria tematica a cui appartiene il post.
     */
    private Categoria categoria;

    /**
     * Lista degli utenti che hanno espresso apprezzamento ("Mi Piace") per il post.
     */
    private List<Utente> miPiace = new ArrayList<>();

    /**
     * Lista dei commenti lasciati dagli utenti su questo post.
     */
    private List<Commento> commenti = new ArrayList<>();

    /**
     * Enumerazione dei tipi di post supportati.
     */
    public enum TipoPost {
        /**
         * Post contenente solo testo.
         */
        TESTO("Testo"),

        /**
         * Post contenente un'immagine.
         */
        FOTO("Foto"),

        /**
         * Post contenente un video.
         */
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

    /**
     * Costruttore completo per la creazione di un nuovo post.
     * 
     * @param titolo    Il titolo del post.
     * @param contenuto Il contenuto testuale.
     * @param autore    L'utente che sta creando il post.
     * @param tipo      Il tipo di contenuto.
     * @param media     Il riferimento al file multimediale (opzionale).
     */
    public Post(String titolo, String contenuto, Utente autore, TipoPost tipo, String media) {
        this.titolo = titolo;
        this.contenuto = contenuto;
        this.autore = autore;
        this.tipo = tipo;
        this.media = media;
        this.dataCreazione = LocalDateTime.now();
    }

    /**
     * Costruttore predefinito.
     */
    public Post() {
    }

    /**
     * Restituisce un nuovo Builder per facilitare la creazione di oggetti Post.
     *
     * @return Un'istanza di {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder pattern per la classe Post.
     * Permette di costruire un post impostando i campi in modo fluente.
     */
    public static class Builder {
        private String titolo;
        private String contenuto;
        private Utente autore;
        private TipoPost tipo = TipoPost.TESTO;
        private String media;
        private Categoria categoria;

        public Builder withTitolo(String titolo) {
            this.titolo = titolo;
            return this;
        }

        public Builder withContenuto(String contenuto) {
            this.contenuto = contenuto;
            return this;
        }

        public Builder withAutore(Utente autore) {
            this.autore = autore;
            return this;
        }

        public Builder withTipo(TipoPost tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder withMedia(String media) {
            this.media = media;
            return this;
        }

        public Builder withCategoria(Categoria categoria) {
            this.categoria = categoria;
            return this;
        }

        public Post build() {
            Post p = new Post(titolo, contenuto, autore, tipo, media);
            if (categoria != null) {
                p.setCategoria(categoria);
            }
            return p;
        }
    }

    // --- Metodi Getter ---

    /**
     * Restituisce l'ID del post.
     *
     * @return L'identificativo.
     */
    public int getId() {
        return id;
    }

    /**
     * Restituisce l'autore del post.
     *
     * @return L'oggetto Utente.
     */
    public Utente getAutore() {
        return autore;
    }

    /**
     * Restituisce il titolo del post.
     *
     * @return Il titolo.
     */
    public String getTitolo() {
        return titolo;
    }

    /**
     * Restituisce il contenuto del post.
     *
     * @return Il testo del contenuto.
     */
    public String getContenuto() {
        return contenuto;
    }

    /**
     * Restituisce il tipo di post.
     *
     * @return Il valore dell'enumerazione TipoPost.
     */
    public TipoPost getTipo() {
        return tipo;
    }

    /**
     * Restituisce il percorso del media allegato.
     *
     * @return L'URI o path del media.
     */
    public String getMedia() {
        return media;
    }

    /**
     * Restituisce la categoria a cui appartiene il post.
     *
     * @return L'oggetto Categoria.
     */
    public Categoria getCategoria() {
        return categoria;
    }

    /**
     * Restituisce la lista degli utenti che hanno messo "Mi Piace".
     *
     * @return Lista di Utenti.
     */
    public List<Utente> getMiPiace() {
        return miPiace;
    }

    /**
     * Restituisce un utente specifico dalla lista dei "Mi Piace".
     *
     * @param i L'indice nella lista.
     * @return L'oggetto Utente.
     */
    public Utente getMiPiaceSingolo(int i) {
        return miPiace.get(i);
    }

    /**
     * Restituisce la data e ora di creazione del post.
     *
     * @return L'oggetto LocalDateTime.
     */
    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    /**
     * Restituisce la lista dei commenti al post.
     *
     * @return Lista di Commenti.
     */
    public List<Commento> getCommenti() {
        return commenti;
    }

    /**
     * Restituisce un commento specifico dalla lista.
     *
     * @param i L'indice nella lista.
     * @return L'oggetto Commento.
     */
    public Commento getCommentoSingolo(int i) {
        return commenti.get(i);
    }

    // --- Metodi Setter ---

    /**
     * Imposta l'ID del post.
     *
     * @param id Il nuovo ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Imposta l'autore del post.
     *
     * @param autore Il nuovo autore.
     */
    public void setAutore(Utente autore) {
        this.autore = autore;
    }

    /**
     * Imposta il titolo del post.
     *
     * @param titolo Il nuovo titolo.
     */
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    /**
     * Imposta il contenuto del post.
     *
     * @param contenuto Il nuovo contenuto.
     */
    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

    /**
     * Imposta il tipo di post.
     *
     * @param tipo Il nuovo tipo.
     */
    public void setTipo(TipoPost tipo) {
        this.tipo = tipo;
    }

    /**
     * Imposta la data di creazione del post.
     *
     * @param dataCreazione La nuova data.
     */
    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    /**
     * Imposta il media allegato al post.
     *
     * @param media Il percorso del nuovo media.
     */
    public void setMedia(String media) {
        this.media = media;
    }

    /**
     * Imposta la categoria del post.
     *
     * @param categoria La nuova categoria.
     */
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    /**
     * Imposta la lista degli utenti che hanno messo "Mi Piace".
     *
     * @param miPiace La nuova lista.
     */
    public void setMiPiace(List<Utente> miPiace) {
        this.miPiace = miPiace;
    }

    /**
     * Sostituisce un utente nella lista dei "Mi Piace" in una specifica posizione.
     *
     * @param i      L'indice.
     * @param utente Il nuovo utente.
     */
    public void setMiPiaceSingolo(int i, Utente utente) {
        miPiace.set(i, utente);
    }

    /**
     * Imposta la lista dei commenti.
     *
     * @param commenti La nuova lista.
     */
    public void setCommenti(List<Commento> commenti) {
        this.commenti = commenti;
    }

    /**
     * Sostituisce un commento nella lista in una specifica posizione.
     *
     * @param i        L'indice.
     * @param commento Il nuovo commento.
     */
    public void setCommentoSingolo(int i, Commento commento) {
        commenti.set(i, commento);
    }
}
