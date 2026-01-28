package it.univaq.brewhub.model;

/**
 * Rappresenta un messaggio scambiato tra utenti (chat privata o di gruppo).
 */
public class Messaggio {

    /** Identificativo univoco del messaggio. */
    private int id;

    /** Username del mittente. */
    private String sender;

    /** Username del destinatario (null se è un messaggio di gruppo). */
    private String receiver;

    /** Contenuto del messaggio. */
    private String contenuto;

    /** Timestamp di invio come stringa. */
    private String timestamp; 

    /** Stato di lettura del messaggio. */
    private boolean letto;

    /** ID del gruppo di destinazione (null se è un messaggio privato). */
    private Integer idGruppo; 

    /**
     * Costruttore vuoto.
     */
    public Messaggio() {
    }

    /**
     * Costruttore completo (usato tipicamente dal DAO).
     * 
     * @param id        ID messaggio.
     * @param sender    Mittente.
     * @param receiver  Destinatario.
     * @param contenuto Testo.
     * @param timestamp Orario.
     * @param letto     Stato lettura.
     * @param idGruppo  ID Gruppo.
     */
    public Messaggio(int id, String sender, String receiver, String contenuto, String timestamp, boolean letto,
            Integer idGruppo) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.contenuto = contenuto;
        this.timestamp = timestamp;
        this.letto = letto;
        this.idGruppo = idGruppo;
    }

    /**
     * Costruttore semplificato per la creazione di nuovi messaggi privati.
     * 
     * @param sender    Mittente.
     * @param receiver  Destinatario.
     * @param contenuto Testo.
     * @param timestamp Orario.
     */
    public Messaggio(String sender, String receiver, String contenuto, String timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.contenuto = contenuto;
        this.timestamp = timestamp;
        this.letto = false;
        this.idGruppo = null;
    }

    /**
     * Restituisce l'ID del messaggio.
     * @return L'identificativo.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID del messaggio.
     * @param id Il nuovo ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il mittente.
     * @return Username del mittente.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Imposta il mittente.
     * @param sender Username del mittente.
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Restituisce il destinatario (se messaggio privato).
     * @return Username del destinatario o null.
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * Imposta il destinatario.
     * @param receiver Username del destinatario.
     */
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    /**
     * Restituisce il contenuto del messaggio.
     * @return Il testo.
     */
    public String getContenuto() {
        return contenuto;
    }

    /**
     * Imposta il contenuto del messaggio.
     * @param contenuto Il nuovo testo.
     */
    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

    /**
     * Restituisce il timestamp di invio.
     * @return La data/ora come stringa.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Imposta il timestamp.
     * @param timestamp La nuova data/ora.
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Restituisce l'ID del gruppo (se messaggio di gruppo).
     * @return ID gruppo o null.
     */
    public Integer getIdGruppo() {
        return idGruppo;
    }

    /**
     * Imposta l'ID del gruppo.
     * @param idGruppo Il nuovo ID gruppo.
     */
    public void setIdGruppo(Integer idGruppo) {
        this.idGruppo = idGruppo;
    }

    /**
     * Verifica se il messaggio è stato letto.
     * @return true se letto, false altrimenti.
     */
    public boolean isLetto() {
        return letto;
    }

    /**
     * Imposta lo stato di lettura.
     * @param letto true se letto.
     */
    public void setLetto(boolean letto) {
        this.letto = letto;
    }
}
