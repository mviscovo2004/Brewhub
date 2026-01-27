package it.univaq.brewhub.model;

/**
 * Rappresenta una sfida (contest) lanciata da un Torrefattore.
 */
public class Sfida {

    private int id;
    private String titolo;
    private String descrizione;
    private String premio;
    private String scadenza; 
    private String creatore; 
    private int partecipantiCount;
    private boolean isPartecipante; 

    /**
     * Costruttore vuoto.
     */
    public Sfida() {
    }

    /**
     * Costruttore completo.
     * 
     * @param titolo Titolo della sfida.
     * @param descrizione Descrizione e regole.
     * @param premio Premio in palio.
     * @param scadenza Data di scadenza.
     * @param creatore Username del creatore (Torrefattore).
     */
    public Sfida(String titolo, String descrizione, String premio, String scadenza, String creatore) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.premio = premio;
        this.scadenza = scadenza;
        this.creatore = creatore;
        this.partecipantiCount = 0;
    }

    /**
     * Restituisce l'ID della sfida.
     * @return L'identificativo.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID della sfida.
     * @param id Il nuovo ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il titolo della sfida.
     * @return Il titolo.
     */
    public String getTitolo() {
        return titolo;
    }

    /**
     * Imposta il titolo della sfida.
     * @param titolo Il nuovo titolo.
     */
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    /**
     * Restituisce la descrizione.
     * @return La descrizione.
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Imposta la descrizione.
     * @param descrizione La nuova descrizione.
     */
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * Restituisce il premio in palio.
     * @return Il premio.
     */
    public String getPremio() {
        return premio;
    }

    /**
     * Imposta il premio.
     * @param premio Il nuovo premio.
     */
    public void setPremio(String premio) {
        this.premio = premio;
    }

    /**
     * Restituisce la data di scadenza.
     * @return La scadenza come stringa.
     */
    public String getScadenza() {
        return scadenza;
    }

    /**
     * Imposta la data di scadenza.
     * @param scadenza La nuova scadenza.
     */
    public void setScadenza(String scadenza) {
        this.scadenza = scadenza;
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
     * Restituisce il numero di partecipanti.
     * @return Il conteggio.
     */
    public int getPartecipantiCount() {
        return partecipantiCount;
    }

    /**
     * Imposta il numero di partecipanti.
     * @param partecipantiCount Il nuovo conteggio.
     */
    public void setPartecipantiCount(int partecipantiCount) {
        this.partecipantiCount = partecipantiCount;
    }

    /**
     * Verifica se l'utente corrente partecipa alla sfida.
     * @return true se partecipante, false altrimenti.
     */
    public boolean isPartecipante() {
        return isPartecipante;
    }

    /**
     * Imposta lo stato di partecipazione dell'utente corrente.
     * @param isPartecipante true se partecipa.
     */
    public void setPartecipante(boolean isPartecipante) {
        this.isPartecipante = isPartecipante;
    }
}
