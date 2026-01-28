package it.univaq.brewhub.model;

import java.time.LocalDateTime;

/**
 * Rappresenta una notifica inviata a un utente.
 */
public class Notifica {

    /** ID univoco della notifica. */
    private int id;

    /** Utente destinatario della notifica. */
    private Utente utente;

    /** Testo del messaggio di notifica. */
    private String messaggio;

    /** Stato di lettura. */
    private boolean letto;

    /** Data e ora di creazione. */
    private LocalDateTime dataCreazione;

    /**
     * Costruttore vuoto.
     */
    public Notifica() {
    }

    /**
     * Costruttore per nuova notifica.
     * 
     * @param utente    Destinatario.
     * @param messaggio Testo.
     */
    public Notifica(Utente utente, String messaggio) {
        this.utente = utente;
        this.messaggio = messaggio;
        this.letto = false;
        this.dataCreazione = LocalDateTime.now();
    }

    /**
     * Restituisce l'ID della notifica.
     * @return L'identificativo.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID della notifica.
     * @param id Il nuovo ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce l'utente destinatario.
     * @return L'oggetto {@link Utente}.
     */
    public Utente getUtente() {
        return utente;
    }

    /**
     * Imposta l'utente destinatario.
     * @param utente Il nuovo destinatario.
     */
    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    /**
     * Restituisce il messaggio della notifica.
     * @return Il testo.
     */
    public String getMessaggio() {
        return messaggio;
    }

    /**
     * Imposta il messaggio della notifica.
     * @param messaggio Il nuovo testo.
     */
    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    /**
     * Verifica se la notifica è stata letta.
     * @return true se letta, false altrimenti.
     */
    public boolean isLetto() {
        return letto;
    }

    /**
     * Imposta lo stato di lettura.
     * @param letto true se letta.
     */
    public void setLetto(boolean letto) {
        this.letto = letto;
    }

    /**
     * Restituisce la data di creazione.
     * @return {@link LocalDateTime} di creazione.
     */
    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    /**
     * Imposta la data di creazione.
     * @param dataCreazione La nuova data.
     */
    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }
}
