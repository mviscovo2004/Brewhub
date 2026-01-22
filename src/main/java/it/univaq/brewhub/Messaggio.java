package it.univaq.brewhub;

/**
 * Modello che rappresenta un messaggio privato tra due utenti.
 */
public class Messaggio {

    private int id;
    private String sender;
    private String receiver;
    private String contenuto;
    private String timestamp; // Formato ISO o semplice stringa per SQLite
    private boolean letto;
    private Integer idGruppo; // Null if private chat

    public Messaggio() {
    }

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

    public Messaggio(String sender, String receiver, String contenuto, String timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.contenuto = contenuto;
        this.timestamp = timestamp;
        this.letto = false;
        this.idGruppo = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContenuto() {
        return contenuto;
    }

    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getIdGruppo() {
        return idGruppo;
    }

    public void setIdGruppo(Integer idGruppo) {
        this.idGruppo = idGruppo;
    }

    public boolean isLetto() {
        return letto;
    }

    public void setLetto(boolean letto) {
        this.letto = letto;
    }
}
