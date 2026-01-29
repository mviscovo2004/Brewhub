package it.univaq.brewhub.model;

import java.util.List;

/**
 * Classe che rappresenta un gruppo di utenti.
 * I gruppi permettono agli utenti di aggregarsi per conversazioni (chat di
 * gruppo)
 * o altre attività collaborative.
 */
public class Gruppo {

    /**
     * Identificativo univoco del gruppo.
     */
    private int id;

    /**
     * Nome del gruppo.
     */
    private String nome;

    /**
     * Username dell'utente che ha creato il gruppo.
     */
    private String creatore;

    /**
     * Lista degli username degli utenti che fanno parte del gruppo.
     */
    private List<String> membri;

    /**
     * Costruttore predefinito.
     */
    public Gruppo() {
    }

    /**
     * Costruttore per creare un gruppo con informazioni di base.
     * 
     * @param id       L'ID del gruppo.
     * @param nome     Il nome del gruppo.
     * @param creatore L'username del creatore.
     */
    public Gruppo(int id, String nome, String creatore) {
        this.id = id;
        this.nome = nome;
        this.creatore = creatore;
    }

    /**
     * Restituisce l'ID del gruppo.
     *
     * @return L'identificativo del gruppo.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID del gruppo.
     *
     * @param id Il nuovo ID da assegnare.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il nome del gruppo.
     *
     * @return Il nome del gruppo.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Imposta il nome del gruppo.
     *
     * @param nome Il nuovo nome da assegnare.
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Restituisce l'username del creatore del gruppo.
     *
     * @return L'username del creatore.
     */
    public String getCreatore() {
        return creatore;
    }

    /**
     * Imposta l'username del creatore del gruppo.
     *
     * @param creatore Il nuovo username del creatore.
     */
    public void setCreatore(String creatore) {
        this.creatore = creatore;
    }

    /**
     * Restituisce la lista dei membri del gruppo.
     *
     * @return Una lista di stringhe contenente gli username dei membri.
     */
    public List<String> getMembri() {
        return membri;
    }

    /**
     * Imposta la lista dei membri del gruppo.
     *
     * @param membri La nuova lista di username dei membri.
     */
    public void setMembri(List<String> membri) {
        this.membri = membri;
    }
}
