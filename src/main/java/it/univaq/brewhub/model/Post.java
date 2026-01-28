package it.univaq.brewhub.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta un Post creato da un utente nel social network.
 *
 * Un post è l'elemento fondamentale di condivisione in BrewHub.
 * Può contenere testo, foto o video e funge da aggregatore per interazioni
 * sociali
 * come "Mi Piace" e commenti.
 *
 * Ogni post è associato a un {@link Utente} autore e opzionalmente a una
 * {@link Categoria} tematica.
 *
 */
public class Post {

    /** Identificativo univoco del post nel database (Primary Key). */
    private int id;

    /** L'autore che ha creato il post. */
    private Utente autore;

    /** Titolo del post, obbligatorio per la pubblicazione. */
    private String titolo;

    /** Contenuto testuale o descrizione del post. */
    private String contenuto;

    /**
     * Tipologia del post (Testo, Foto, Video) che determina come viene renderizzato
     * nella UI.
     */
    private TipoPost tipo;

    /**
     * Data e ora di creazione del post. Di default impostata al momento
     * dell'istanziazione.
     */
    private LocalDateTime dataCreazione = LocalDateTime.now();

    /**
     * Percorso (locale o URI) del file media associato.
     * È null se il tipo di post è {@link TipoPost#TESTO}.
     */
    private String media = null;

    /** Categoria di appartenenza del post (es. "Torrefattori", "Miscele"). */
    private Categoria categoria;

    /**
     * Lista degli utenti che hanno messo "Mi Piace".
     * Viene popolata pigramente (Lazy Loading) dal DAO solo quando necessario.
     */
    private List<Utente> miPiace = new ArrayList<>();

    /**
     * Lista dei commenti associati al post.
     * Solitamente caricati insieme al post per la visualizzazione nel feed.
     */
    private List<Commento> commenti = new ArrayList<>();

    /**
     * Enumerazione per i tipi di post supportati dal sistema.
     */
    public enum TipoPost {
        /** Post contenente solo titolo e descrizione testuale. */
        TESTO("Testo"),
        /** Post contenente un'immagine allegata. */
        FOTO("Foto"),
        /** Post contenente un video allegato. */
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
     * Costruttore completo per creare un nuovo post.
     * 
     * @param titolo    Il titolo del post.
     * @param contenuto Il contenuto o descrizione.
     * @param autore    L'utente autore.
     * @param tipo      Il tipo di post.
     * @param media     Il percorso del media (può essere null per i post di testo).
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
     * Costruttore vuoto (Bean).
     */
    public Post() {
    }

    /**
     * Restituisce un nuovo Builder per creare istanze di Post in modo fluente.
     * 
     * @return Un'istanza di {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Pattern Builder per la classe Post.
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

    // --- GETTER ---

    /**
     * Restituisce l'ID del post.
     * 
     * @return L'identificativo intero.
     */
    public int getId() {
        return id;
    }

    /**
     * Restituisce l'autore del post.
     * 
     * @return L'oggetto {@link Utente} autore.
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
     * @return Il {@link TipoPost}.
     */
    public TipoPost getTipo() {
        return tipo;
    }

    /**
     * Restituisce il percorso del media allegato.
     * 
     * @return L'URI del media o null se non presente.
     */
    public String getMedia() {
        return media;
    }

    /**
     * Restituisce la categoria del post.
     * 
     * @return L'oggetto {@link Categoria} o null se non assegnata.
     */
    public Categoria getCategoria() {
        return categoria;
    }

    /**
     * Restituisce la lista di utenti che hanno messo like.
     * 
     * @return Lista di {@link Utente}.
     */
    public List<Utente> getMiPiace() {
        return miPiace;
    }

    /**
     * Restituisce un singolo utente che ha messo like in base all'indice.
     * 
     * @param i L'indice nella lista dei like.
     * @return L'utente.
     */
    public Utente getMiPiaceSingolo(int i) {
        return miPiace.get(i);
    }

    /**
     * Restituisce la data e ora di creazione.
     * 
     * @return {@link LocalDateTime} di creazione.
     */
    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    /**
     * Restituisce la lista dei commenti.
     * 
     * @return Lista di {@link Commento}.
     */
    public List<Commento> getCommenti() {
        return commenti;
    }

    /**
     * Restituisce un singolo commento in base all'indice.
     * 
     * @param i L'indice nella lista dei commenti.
     * @return Il {@link Commento}.
     */
    public Commento getCommentoSingolo(int i) {
        return commenti.get(i);
    }

    // --- SETTER ---

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
     * Imposta la data di creazione.
     * 
     * @param dataCreazione La nuova data.
     */
    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    /**
     * Imposta il media allegato.
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
     * Imposta la lista dei like.
     * 
     * @param miPiace La nuova lista di utenti.
     */
    public void setMiPiace(List<Utente> miPiace) {
        this.miPiace = miPiace;
    }

    /**
     * Aggiorna un singolo like nella lista.
     * 
     * @param i      L'indice da aggiornare.
     * @param utente L'utente da inserire.
     */
    public void setMiPiaceSingolo(int i, Utente utente) {
        miPiace.set(i, utente);
    }

    /**
     * Imposta la lista dei commenti.
     * 
     * @param commenti La nuova lista di commenti.
     */
    public void setCommenti(List<Commento> commenti) {
        this.commenti = commenti;
    }

    /**
     * Aggiorna un singolo commento nella lista.
     * 
     * @param i        L'indice da aggiornare.
     * @param commento Il nuovo commento.
     */
    public void setCommentoSingolo(int i, Commento commento) {
        commenti.set(i, commento);
    }
}
