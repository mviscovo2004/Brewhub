package it.univaq.brewhub.model;

import java.time.LocalDateTime;

/**
 * Classe che rappresenta una notifica destinata a un utente del sistema.
 * Le notifiche informano gli utenti di attività rilevanti (es. like, commenti,
 * nuovi follower).
 */
public class Notifica {

    /**
     * Identificativo univoco della notifica.
     */
    private int id;

    /**
     * L'utente destinatario della notifica.
     */
    private Utente utente;

    /**
     * Il testo del messaggio di notifica.
     */
    private String messaggio;

    /**
     * Flag che indica se la notifica è stata visualizzata dall'utente.
     */
    private boolean letto;

    /**
     * Data e ora in cui la notifica è stata generata.
     */
    private LocalDateTime dataCreazione;

    /**
     * Costruttore predefinito.
     */
    public Notifica() {
    }

    /**
     * Costruttore per creare una nuova notifica da inviare.
     * Imposta di default lo stato 'letto' a false e la data di creazione al momento
     * attuale.
     * 
     * @param utente    L'utente destinatario della notifica.
     * @param messaggio Il contenuto testuale della notifica.
     */
    public Notifica(Utente utente, String messaggio) {
        this.utente = utente;
        this.messaggio = messaggio;
        this.letto = false;
        this.dataCreazione = LocalDateTime.now();
    }

    /**
     * Restituisce l'ID della notifica.
     *
     * @return L'identificativo.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID della notifica.
     *
     * @param id Il nuovo ID da assegnare.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce l'utente a cui è destinata la notifica.
     *
     * @return L'oggetto Utente destinatario.
     */
    public Utente getUtente() {
        return utente;
    }

    /**
     * Imposta l'utente destinatario della notifica.
     *
     * @param utente Il nuovo destinatario.
     */
    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    /**
     * Restituisce il messaggio della notifica.
     *
     * @return Il testo della notifica.
     */
    public String getMessaggio() {
        return messaggio;
    }

    /**
     * Imposta il messaggio della notifica.
     *
     * @param messaggio Il nuovo testo.
     */
    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    /**
     * Verifica se la notifica è già stata letta.
     *
     * @return true se la notifica è letta, false altrimenti.
     */
    public boolean isLetto() {
        return letto;
    }

    /**
     * Modifica lo stato di lettura della notifica.
     *
     * @param letto true per segnare come letta.
     */
    public void setLetto(boolean letto) {
        this.letto = letto;
    }

    /**
     * Restituisce la data e ora di creazione della notifica.
     *
     * @return L'oggetto LocalDateTime di creazione.
     */
    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    /**
     * Imposta la data e ora di creazione della notifica.
     *
     * @param dataCreazione La nuova data di creazione.
     */
    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }
}
