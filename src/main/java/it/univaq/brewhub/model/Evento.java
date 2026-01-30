package it.univaq.brewhub.model;

/**
 * Classe che rappresenta un evento all'interno della piattaforma.
 * Gli eventi possono essere creati da utenti specifici (es. Torrefattori) e
 * contengono dettagli come data, luogo e numero di partecipanti.
 */
public class Evento {

    /**
     * Identificativo univoco dell'evento.
     */
    private int id;

    /**
     * Nome o titolo dell'evento.
     */
    private String nome;

    /**
     * Descrizione dettagliata dell'evento.
     */
    private String descrizione;

    /**
     * Data e ora dell'evento, memorizzata come stringa.
     */
    private String data;

    /**
     * Luogo in cui si svolge l'evento.
     */
    private String luogo;

    /**
     * Username dell'utente che organizza l'evento.
     */
    private String organizzatore;

    /**
     * Numero attuale di partecipanti iscritti all'evento.
     */
    private int partecipantiCount;

    /**
     * Costruttore predefinito.
     */
    public Evento() {
    }

    /**
     * Costruttore per creare un nuovo evento con le informazioni principali.
     * Il conteggio dei partecipanti viene inizializzato a 0.
     * 
     * @param nome          Il nome dell'evento.
     * @param descrizione   La descrizione dell'evento.
     * @param data          La data dell'evento (come stringa).
     * @param luogo         Il luogo dell'evento.
     * @param organizzatore L'username dell'organizzatore.
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
     *
     * @return L'identificativo dell'evento.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID dell'evento.
     *
     * @param id Il nuovo ID da assegnare.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il nome dell'evento.
     *
     * @return Il nome dell'evento.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Imposta il nome dell'evento.
     *
     * @param nome Il nuovo nome da assegnare.
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Restituisce la descrizione dell'evento.
     *
     * @return La descrizione.
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Imposta la descrizione dell'evento.
     *
     * @param descrizione La nuova descrizione da assegnare.
     */
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * Restituisce la data dell'evento.
     *
     * @return La stringa rappresentante la data.
     */
    public String getData() {
        return data;
    }

    /**
     * Imposta la data dell'evento.
     *
     * @param data La nuova data da assegnare.
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Restituisce il luogo dell'evento.
     *
     * @return Il luogo.
     */
    public String getLuogo() {
        return luogo;
    }

    /**
     * Imposta il luogo dell'evento.
     *
     * @param luogo Il nuovo luogo da assegnare.
     */
    public void setLuogo(String luogo) {
        this.luogo = luogo;
    }

    /**
     * Restituisce l'username dell'organizzatore.
     *
     * @return L'username dell'organizzatore.
     */
    public String getOrganizzatore() {
        return organizzatore;
    }

    /**
     * Imposta l'organizzatore dell'evento.
     *
     * @param organizzatore L'username del nuovo organizzatore.
     */
    public void setOrganizzatore(String organizzatore) {
        this.organizzatore = organizzatore;
    }

    /**
     * Restituisce il numero di partecipanti.
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
}
