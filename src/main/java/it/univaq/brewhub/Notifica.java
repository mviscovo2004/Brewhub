package it.univaq.brewhub;

import java.time.LocalDateTime;

public class Notifica {
    private int id;
    private Utente utente;
    private String messaggio;
    private boolean letto;
    private LocalDateTime dataCreazione;

    public Notifica() {
    }

    public Notifica(Utente utente, String messaggio) {
        this.utente = utente;
        this.messaggio = messaggio;
        this.letto = false;
        this.dataCreazione = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    public boolean isLetto() {
        return letto;
    }

    public void setLetto(boolean letto) {
        this.letto = letto;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }
}
