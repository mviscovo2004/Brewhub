package it.univaq.brewhub.model;

/**
 * Classe che rappresenta un messaggio all'interno del sistema di chat.
 * Un messaggio può essere diretto a un singolo utente (chat privata)
 * o a un gruppo (chat di gruppo).
 */
public class Messaggio {

    /**
     * Identificativo univoco del messaggio.
     */
    private int id;

    /**
     * Username dell'utente mittente.
     */
    private String sender;

    /**
     * Username dell'utente destinatario.
     * È valorizzato solo se il messaggio è privato, altrimenti è null.
     */
    private String receiver;

    /**
     * Il contenuto testuale del messaggio.
     */
    private String contenuto;

    /**
     * Timestamp che indica quando il messaggio è stato inviato (formato stringa).
     */
    private String timestamp;

    /**
     * Flag che indica se il messaggio è stato letto dal destinatario.
     */
    private boolean letto;

    /**
     * Identificativo del gruppo di destinazione.
     * È valorizzato solo se il messaggio è inviato in una chat di gruppo,
     * altrimenti è null.
     */
    private Integer idGruppo;

    /**
     * Costruttore predefinito.
     */
    public Messaggio() {
    }

    /**
     * Costruttore completo per inizializzare un messaggio recuperato dal database.
     * 
     * @param id        L'ID del messaggio.
     * @param sender    Il mittente.
     * @param receiver  Il destinatario (o null per gruppi).
     * @param contenuto Il testo del messaggio.
     * @param timestamp L'orario di invio.
     * @param letto     Stato di lettura.
     * @param idGruppo  L'ID del gruppo (o null per privati).
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
     * Costruttore semplificato per creare nuovi messaggi privati da inviare.
     * Imposta di default 'letto' a false e 'idGruppo' a null.
     * 
     * @param sender    Il mittente.
     * @param receiver  Il destinatario.
     * @param contenuto Il testo del messaggio.
     * @param timestamp L'orario di invio.
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
     *
     * @return L'identificativo.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID del messaggio.
     *
     * @param id Il nuovo ID da assegnare.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce lo username del mittente.
     *
     * @return Il mittente.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Imposta il mittente del messaggio.
     *
     * @param sender Lo username del nuovo mittente.
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Restituisce lo username del destinatario (per messaggi privati).
     *
     * @return Il destinatario o null se è un messaggio di gruppo.
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * Imposta il destinatario del messaggio.
     *
     * @param receiver Lo username del destinatario.
     */
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    /**
     * Restituisce il contenuto testuale del messaggio.
     *
     * @return Il testo del messaggio.
     */
    public String getContenuto() {
        return contenuto;
    }

    /**
     * Imposta il contenuto del messaggio.
     *
     * @param contenuto Il nuovo testo.
     */
    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

    /**
     * Restituisce il timestamp di invio del messaggio.
     *
     * @return La data/ora di invio.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Imposta il timestamp di invio.
     *
     * @param timestamp La nuova data/ora.
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Restituisce l'ID del gruppo di destinazione.
     *
     * @return L'ID del gruppo o null se è un messaggio privato.
     */
    public Integer getIdGruppo() {
        return idGruppo;
    }

    /**
     * Imposta l'ID del gruppo di destinazione.
     *
     * @param idGruppo Il nuovo ID gruppo.
     */
    public void setIdGruppo(Integer idGruppo) {
        this.idGruppo = idGruppo;
    }

    /**
     * Verifica se il messaggio è stato contrassegnato come letto.
     *
     * @return true se il messaggio è stato letto, altrimenti false.
     */
    public boolean isLetto() {
        return letto;
    }

    /**
     * Imposta lo stato di lettura del messaggio.
     *
     * @param letto true per segnare come letto, false altrimenti.
     */
    public void setLetto(boolean letto) {
        this.letto = letto;
    }
}
