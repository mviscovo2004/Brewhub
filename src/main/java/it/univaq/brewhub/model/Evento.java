package it.univaq.brewhub.model;

/**
 * Rappresenta un evento organizzato (es. degustazione, corso).
 */
public class Evento {

    /** ID univoco dell'evento. */
    private int id;

    /** Nome dell'evento. */
    private String nome;

    /** Descrizione dettagliata. */
    private String descrizione;

    /** Data dell'evento (formato stringa). */
    private String data;

    /** Luogo dell'evento. */
    private String luogo;

    /** Username dell'organizzatore. */
    private String organizzatore; 

    /** Numero di partecipanti confermati. */
    private int partecipantiCount;

    /**
     * Costruttore vuoto.
     */
    public Evento() {
    }

    /**
     * Costruttore completo.
     * 
     * @param nome Nome evento.
     * @param descrizione Descrizione.
     * @param data Data evento.
     * @param luogo Luogo evento.
     * @param organizzatore Username organizzatore.
     */
    public Evento(String nome, String descrizione, String data, String luogo, String organizzatore) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.data = data;
        this.luogo = luogo;
        this.organizzatore = organizzatore;
        this.partecipantiCount = 0;
    }

    /**
     * Restituisce l'ID dell'evento.
     * @return L'identificativo.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID dell'evento.
     * @param id Il nuovo ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il nome dell'evento.
     * @return Il nome.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Imposta il nome dell'evento.
     * @param nome Il nuovo nome.
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Restituisce la descrizione dell'evento.
     * @return La descrizione.
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Imposta la descrizione dell'evento.
     * @param descrizione La nuova descrizione.
     */
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * Restituisce la data dell'evento.
     * @return La data come stringa.
     */
    public String getData() {
        return data;
    }

    /**
     * Imposta la data dell'evento.
     * @param data La nuova data.
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Restituisce il luogo dell'evento.
     * @return Il luogo.
     */
    public String getLuogo() {
        return luogo;
    }

    /**
     * Imposta il luogo dell'evento.
     * @param luogo Il nuovo luogo.
     */
    public void setLuogo(String luogo) {
        this.luogo = luogo;
    }

    /**
     * Restituisce l'username dell'organizzatore.
     * @return L'username.
     */
    public String getOrganizzatore() {
        return organizzatore;
    }

    /**
     * Imposta l'organizzatore.
     * @param organizzatore L'username dell'organizzatore.
     */
    public void setOrganizzatore(String organizzatore) {
        this.organizzatore = organizzatore;
    }

    /**
     * Restituisce il numero di partecipanti.
     * @return Il conteggio partecipanti.
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
}
