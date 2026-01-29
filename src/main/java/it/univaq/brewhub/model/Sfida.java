package it.univaq.brewhub.model;

/**
 * Classe che rappresenta una "Sfida" (o contest) lanciata all'interno della
 * piattaforma.
 * Le sfide sono tipicamente create dai Torrefattori e prevedono premi per i
 * partecipanti.
 */
public class Sfida {

    /**
     * Identificativo univoco della sfida.
     */
    private int id;

    /**
     * Titolo della sfida.
     */
    private String titolo;

    /**
     * Descrizione dettagliata della sfida e delle sue regole.
     */
    private String descrizione;

    /**
     * Descrizione del premio in palio.
     */
    private String premio;

    /**
     * Data di scadenza della sfida (formato stringa).
     */
    private String scadenza;

    /**
     * Username del creatore della sfida (solitamente un Torrefattore).
     */
    private String creatore;

    /**
     * Numero attuale di partecipanti alla sfida.
     */
    private int partecipantiCount;

    /**
     * Flag transitorio usato nella UI per indicare se l'utente corrente sta
     * partecipando.
     */
    private boolean isPartecipante;

    /**
     * Costruttore predefinito.
     */
    public Sfida() {
    }

    /**
     * Costruttore completo per creare una nuova sfida.
     * Inizializza il numero di partecipanti a 0.
     * 
     * @param titolo      Il titolo della sfida.
     * @param descrizione La descrizione e le regole.
     * @param premio      Il premio in palio.
     * @param scadenza    La data di scadenza.
     * @param creatore    L'username dell'organizzatore.
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
     *
     * @return L'identificativo.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID della sfida.
     *
     * @param id Il nuovo ID da assegnare.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il titolo della sfida.
     *
     * @return Il titolo.
     */
    public String getTitolo() {
        return titolo;
    }

    /**
     * Imposta il titolo della sfida.
     *
     * @param titolo Il nuovo titolo.
     */
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    /**
     * Restituisce la descrizione della sfida.
     *
     * @return La descrizione.
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Imposta la descrizione della sfida.
     *
     * @param descrizione La nuova descrizione.
     */
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * Restituisce il premio in palio.
     *
     * @return La descrizione del premio.
     */
    public String getPremio() {
        return premio;
    }

    /**
     * Imposta il premio della sfida.
     *
     * @param premio Il nuovo premio.
     */
    public void setPremio(String premio) {
        this.premio = premio;
    }

    /**
     * Restituisce la data di scadenza della sfida.
     *
     * @return La data di scadenza.
     */
    public String getScadenza() {
        return scadenza;
    }

    /**
     * Imposta la data di scadenza.
     *
     * @param scadenza La nuova data.
     */
    public void setScadenza(String scadenza) {
        this.scadenza = scadenza;
    }

    /**
     * Restituisce l'username del creatore della sfida.
     *
     * @return L'username del creatore.
     */
    public String getCreatore() {
        return creatore;
    }

    /**
     * Imposta l'username del creatore della sfida.
     *
     * @param creatore Il nuovo username.
     */
    public void setCreatore(String creatore) {
        this.creatore = creatore;
    }

    /**
     * Restituisce il numero di partecipanti attuali.
     *
     * @return Il conteggio dei partecipanti.
     */
    public int getPartecipantiCount() {
        return partecipantiCount;
    }

    /**
     * Imposta il numero di partecipanti.
     *
     * @param partecipantiCount Il nuovo conteggio.
     */
    public void setPartecipantiCount(int partecipantiCount) {
        this.partecipantiCount = partecipantiCount;
    }

    /**
     * Verifica se l'utente corrente partecipa a questa sfida.
     * Metodo di utilità per la UI.
     *
     * @return true se l'utente partecipa, false altrimenti.
     */
    public boolean isPartecipante() {
        return isPartecipante;
    }

    /**
     * Imposta lo stato di partecipazione dell'utente corrente.
     *
     * @param isPartecipante true se l'utente partecipa.
     */
    public void setPartecipante(boolean isPartecipante) {
        this.isPartecipante = isPartecipante;
    }
}
