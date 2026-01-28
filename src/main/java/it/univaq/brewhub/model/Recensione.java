package it.univaq.brewhub.model;

/**
 * Rappresenta una recensione lasciata su un post.
 */
public class Recensione {

    private int id;
    private Post post;
    private Utente autore;
    private int voto;
    private String testo;
    private String dataCreazione;

    /**
     * Costruttore vuoto.
     */
    public Recensione() {
    }

    /**
     * Costruttore completo.
     * 
     * @param post          Il post recensito.
     * @param autore        L'autore della recensione.
     * @param voto          Il voto (da 1 a 5).
     * @param testo         Il commento testuale.
     * @param dataCreazione La data di creazione.
     */
    public Recensione(Post post, Utente autore, int voto, String testo, String dataCreazione) {
        this.post = post;
        this.autore = autore;
        this.voto = voto;
        this.testo = testo;
        this.dataCreazione = dataCreazione;
    }

    /**
     * Restituisce l'ID della recensione.
     * 
     * @return L'identificativo.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID della recensione.
     * 
     * @param id Il nuovo ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il post recensito.
     * 
     * @return L'oggetto {@link Post}.
     */
    public Post getPost() {
        return post;
    }

    /**
     * Imposta il post recensito.
     * 
     * @param post Il nuovo post.
     */
    public void setPost(Post post) {
        this.post = post;
    }

    /**
     * Restituisce l'autore della recensione.
     * 
     * @return L'oggetto {@link Utente}.
     */
    public Utente getAutore() {
        return autore;
    }

    /**
     * Imposta l'autore della recensione.
     * 
     * @param autore Il nuovo autore.
     */
    public void setAutore(Utente autore) {
        this.autore = autore;
    }

    /**
     * Restituisce il voto assegnato.
     * 
     * @return Il voto (intero).
     */
    public int getVoto() {
        return voto;
    }

    /**
     * Imposta il voto.
     * 
     * @param voto Il nuovo voto.
     */
    public void setVoto(int voto) {
        this.voto = voto;
    }

    /**
     * Restituisce il testo della recensione.
     * 
     * @return Il commento.
     */
    public String getTesto() {
        return testo;
    }

    /**
     * Imposta il testo della recensione.
     * 
     * @param testo Il nuovo commento.
     */
    public void setTesto(String testo) {
        this.testo = testo;
    }

    /**
     * Restituisce la data di creazione.
     * 
     * @return La data come stringa.
     */
    public String getDataCreazione() {
        return dataCreazione;
    }

    /**
     * Imposta la data di creazione.
     * 
     * @param dataCreazione La nuova data.
     */
    public void setDataCreazione(String dataCreazione) {
        this.dataCreazione = dataCreazione;
    }
}
