package it.univaq.brewhub.model;

/**
 * Modello specifico per l'utente di tipo 'Torrefattore'.
 * Estende la classe base {@link Utente} aggiungendo informazioni aziendali
 * come Partita IVA, indirizzo, nome azienda e descrizione.
 */
public class Torrefattore extends Utente {

    /**
     * Nome ufficiale dell'azienda di torrefazione.
     */
    private String nomeAzienda;

    /**
     * Partita IVA dell'azienda.
     */
    private String partitaIva;

    /**
     * Indirizzo fisico della sede aziendale.
     */
    private String indirizzo;

    /**
     * Descrizione dell'azienda e delle sue attività.
     */
    private String descrizione;

    /**
     * Costruttore predefinito.
     * Inizializza automaticamente il tipo di utente a
     * {@link TipoUtente#TORREFATTORE}.
     */
    public Torrefattore() {
        super();
        this.setTipo(TipoUtente.TORREFATTORE);
    }

    /**
     * Costruttore completo per creare un nuovo profilo Torrefattore.
     * 
     * @param nome        Nome del referente (personale).
     * @param cognome     Cognome del referente (personale).
     * @param username    Username di accesso.
     * @param password    Password di accesso.
     * @param fotoProfilo URI della foto profilo.
     * @param partitaIva  Partita IVA dell'azienda.
     * @param indirizzo   Indirizzo della sede.
     * @param descrizione Descrizione aziendale.
     * @param nomeAzienda Nome dell'azienda.
     */
    public Torrefattore(String nome, String cognome, String username, String password, String fotoProfilo,
            String partitaIva, String indirizzo, String descrizione, String nomeAzienda) {
        super(nome, cognome, username, password, TipoUtente.TORREFATTORE, fotoProfilo);
        this.partitaIva = partitaIva;
        this.indirizzo = indirizzo;
        this.descrizione = descrizione;
        this.nomeAzienda = nomeAzienda;
    }

    /**
     * Restituisce la Partita IVA dell'azienda.
     *
     * @return La Partita IVA.
     */
    public String getPartitaIva() {
        return partitaIva;
    }

    /**
     * Imposta la Partita IVA dell'azienda.
     *
     * @param partitaIva La nuova Partita IVA.
     */
    public void setPartitaIva(String partitaIva) {
        this.partitaIva = partitaIva;
    }

    /**
     * Restituisce l'indirizzo della sede aziendale.
     *
     * @return L'indirizzo.
     */
    public String getIndirizzo() {
        return indirizzo;
    }

    /**
     * Imposta l'indirizzo della sede aziendale.
     *
     * @param indirizzo Il nuovo indirizzo.
     */
    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    /**
     * Restituisce la descrizione dell'azienda.
     *
     * @return Il testo della descrizione.
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Imposta la descrizione dell'azienda.
     *
     * @param descrizione La nuova descrizione.
     */
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * Restituisce il nome dell'azienda.
     *
     * @return Il nome dell'azienda.
     */
    public String getNomeAzienda() {
        return nomeAzienda;
    }

    /**
     * Imposta il nome dell'azienda.
     *
     * @param nomeAzienda Il nuovo nome azienda.
     */
    public void setNomeAzienda(String nomeAzienda) {
        this.nomeAzienda = nomeAzienda;
    }
}
