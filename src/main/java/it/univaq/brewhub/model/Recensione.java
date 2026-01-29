package it.univaq.brewhub.model;

/**
 * Classe che rappresenta una recensione (voto e commento) lasciata su un post.
 * Le recensioni permettono di valutare contenuti come miscele o eventi.
 */
public class Recensione {

    /**
     * Identificativo univoco della recensione.
     */
    private int id;

    /**
     * Il post oggetto della recensione.
     */
    private Post post;

    /**
     * L'utente che ha scritto la recensione.
     */
    private Utente autore;

    /**
     * Il voto assegnato (tipicamente in una scala da 1 a 5).
     */
    private int voto;

    /**
     * Il testo opzionale della recensione.
     */
    private String testo;

    /**
     * La data di creazione della recensione (formato stringa).
     */
    private String dataCreazione;

    /**
     * Costruttore predefinito.
     */
    public Recensione() {
    }

    /**
     * Costruttore completo per creare una recensione.
     * 
     * @param post          Il post a cui si riferisce la recensione.
     * @param autore        L'utente autore della recensione.
     * @param voto          Il voto numerico assegnato.
     * @param testo         Il commento testuale.
     * @param dataCreazione La data di creazione come stringa.
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
     * @param id Il nuovo ID da assegnare.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il post recensito.
     *
     * @return L'oggetto Post.
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
     * @return L'oggetto Utente.
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
     * @return Il valore del voto.
     */
    public int getVoto() {
        return voto;
    }

    /**
     * Imposta il voto della recensione.
     *
     * @param voto Il nuovo voto.
     */
    public void setVoto(int voto) {
        this.voto = voto;
    }

    /**
     * Restituisce il testo della recensione.
     *
     * @return Il commento testuale.
     */
    public String getTesto() {
        return testo;
    }

    /**
     * Imposta il testo della recensione.
     *
     * @param testo Il nuovo testo.
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
