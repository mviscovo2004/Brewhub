package it.univaq.brewhub.model;

import java.util.List;

/**
 * Rappresenta un gruppo di utenti per chat o attività comuni.
 */
public class Gruppo {

    /** ID univoco del gruppo. */
    private int id;

    /** Nome del gruppo. */
    private String nome;

    /** Username del creatore del gruppo. */
    private String creatore;

    /** Lista degli username dei membri. */
    private List<String> membri; 

    /**
     * Costruttore vuoto.
     */
    public Gruppo() {
    }

    /**
     * Costruttore completo.
     * 
     * @param id ID del gruppo.
     * @param nome Nome del gruppo.
     * @param creatore Username del creatore.
     */
    public Gruppo(int id, String nome, String creatore) {
        this.id = id;
        this.nome = nome;
        this.creatore = creatore;
    }

    /**
     * Restituisce l'ID del gruppo.
     * @return L'identificativo.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID del gruppo.
     * @param id Il nuovo ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il nome del gruppo.
     * @return Il nome.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Imposta il nome del gruppo.
     * @param nome Il nuovo nome.
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Restituisce l'username del creatore.
     * @return L'username.
     */
    public String getCreatore() {
        return creatore;
    }

    /**
     * Imposta l'username del creatore.
     * @param creatore Il nuovo creatore.
     */
    public void setCreatore(String creatore) {
        this.creatore = creatore;
    }

    /**
     * Restituisce la lista dei membri (username).
     * @return Lista di stringhe.
     */
    public List<String> getMembri() {
        return membri;
    }

    /**
     * Imposta la lista dei membri.
     * @param membri La nuova lista di username.
     */
    public void setMembri(List<String> membri) {
        this.membri = membri;
    }
}
