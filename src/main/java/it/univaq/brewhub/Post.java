package it.univaq.brewhub;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta un Post pubblicato sulla piattaforma BrewHub.
 * POJO puro: la persistenza è delegata a PostDAO.
 */
public class Post {

    /** Identificativo univoco del post. */
    private int id;
    /** Autore del post. */
    private Utente autore;
    /** Titolo del post. */
    private String titolo;
    /** Contenuto testuale del post. */
    private String contenuto;
    /** Tipo di contenuto del post (Testo, Foto, Video). */
    private TipoPost tipo;
    /** Data e ora di creazione del post. */
    private LocalDateTime dataCreazione = LocalDateTime.now();
    /** Percorso/URI del media associato (se presente). */
    private String media = null;
    /** Categoria del post (opzionale). */
    private Categoria categoria;

    /** Lista degli utenti che hanno messo "mi piace". */
    private List<Utente> miPiace = new ArrayList<>();
    /** Lista dei commenti associati al post. */
    private List<Commento> commenti = new ArrayList<>();

    /**
     * Enumerazione dei tipi di post disponibili.
     */
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

    /**
     * Costruisce un nuovo Post con i dettagli specificati.
     *
     * @param titolo    Il titolo del post.
     * @param contenuto Il contenuto del post.
     * @param autore    L'autore del post.
     * @param tipo      Il tipo del post.
     * @param media     L'URI del media (se presente).
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
     * Costruttore di default.
     */
    public Post() {
    }

    /**
     * Restituisce l'ID univoco del post.
     *
     * @return L'ID del post.
     */
    public int getId() {
        return id;
    }

    /**
     * Restituisce l'autore del post.
     *
     * @return L'autore.
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
     * @return Il contenuto.
     */
    public String getContenuto() {
        return contenuto;
    }

    /**
     * Restituisce il tipo del post.
     *
     * @return Il tipo del post.
     */
    public TipoPost getTipo() {
        return tipo;
    }

    /**
     * Restituisce l'URI del media associato al post.
     *
     * @return L'URI del media.
     */
    public String getMedia() {
        return media;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    /**
     * Restituisce la lista degli utenti che hanno messo "mi piace".
     *
     * @return La lista dei "mi piace".
     */
    public List<Utente> getMiPiace() {
        return miPiace;
    }

    /**
     * Restituisce un singolo utente che ha messo "mi piace" dato l'indice.
     *
     * @param i L'indice.
     * @return L'utente.
     */
    public Utente getMiPiaceSingolo(int i) {
        return miPiace.get(i);
    }

    /**
     * Restituisce la data di creazione del post.
     *
     * @return La data di creazione.
     */
    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    /**
     * Restituisce la lista dei commenti sul post.
     *
     * @return La lista dei commenti.
     */
    public List<Commento> getCommenti() {
        return commenti;
    }

    /**
     * Restituisce un singolo commento dato l'indice.
     *
     * @param i L'indice.
     * @return Il commento.
     */
    public Commento getCommentoSingolo(int i) {
        return commenti.get(i);
    }

    /**
     * Imposta l'ID univoco del post.
     *
     * @param id L'ID del post.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Imposta l'autore del post.
     *
     * @param autore L'autore.
     */
    public void setAutore(Utente autore) {
        this.autore = autore;
    }

    /**
     * Imposta il titolo del post.
     *
     * @param titolo Il titolo.
     */
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    /**
     * Imposta il contenuto del post.
     *
     * @param contenuto Il contenuto.
     */
    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

    /**
     * Imposta il tipo del post.
     *
     * @param tipo Il tipo del post.
     */
    public void setTipo(TipoPost tipo) {
        this.tipo = tipo;
    }

    /**
     * Imposta la data di creazione del post.
     *
     * @param dataCreazione La data di creazione.
     */
    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    /**
     * Imposta l'URI del media associato al post.
     *
     * @param media L'URI del media.
     */
    public void setMedia(String media) {
        this.media = media;
    }

    /**
     * Imposta la lista degli utenti che hanno messo "mi piace".
     *
     * @param miPiace La lista dei "mi piace".
     */
    public void setMiPiace(List<Utente> miPiace) {
        this.miPiace = miPiace;
    }

    /**
     * Imposta un singolo utente nella lista dei "mi piace" all'indice specificato.
     *
     * @param i      L'indice.
     * @param utente L'utente.
     */
    public void setMiPiaceSingolo(int i, Utente utente) {
        miPiace.set(i, utente);
    }

    /**
     * Imposta la lista dei commenti sul post.
     *
     * @param commenti La lista dei commenti.
     */
    public void setCommenti(List<Commento> commenti) {
        this.commenti = commenti;
    }

    /**
     * Imposta un singolo commento nella lista all'indice specificato.
     *
     * @param i        L'indice.
     * @param commento Il commento.
     */
    public void setCommentoSingolo(int i, Commento commento) {
        commenti.set(i, commento);
    }
}
