package it.univaq.brewhub.model;

/**
 * Modello specifico per l'utente di tipo Torrefattore.
 * Estende {@link Utente} aggiungendo dettagli aziendali.
 */
public class Torrefattore extends Utente {

    private String nomeAzienda;
    private String partitaIva;
    private String indirizzo;
    private String descrizione;

    /**
     * Costruttore vuoto.
     * Imposta automaticamente il tipo a TORREFATTORE.
     */
    public Torrefattore() {
        super();
        this.setTipo(TipoUtente.TORREFATTORE);
    }

    /**
     * Costruttore completo.
     * 
     * @param nome          Nome del referente.
     * @param cognome       Cognome del referente.
     * @param username      Username.
     * @param password      Password.
     * @param fotoProfilo   Foto profilo.
     * @param partitaIva    Partita IVA azienda.
     * @param indirizzo     Indirizzo azienda.
     * @param descrizione   Descrizione azienda.
     * @param nomeAzienda   Nome dell'azienda.
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
     * Restituisce la Partita IVA.
     * @return La Partita IVA.
     */
    public String getPartitaIva() {
        return partitaIva;
    }

    /**
     * Imposta la Partita IVA.
     * @param partitaIva La nuova Partita IVA.
     */
    public void setPartitaIva(String partitaIva) {
        this.partitaIva = partitaIva;
    }

    /**
     * Restituisce l'indirizzo dell'azienda.
     * @return L'indirizzo.
     */
    public String getIndirizzo() {
        return indirizzo;
    }

    /**
     * Imposta l'indirizzo dell'azienda.
     * @param indirizzo Il nuovo indirizzo.
     */
    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    /**
     * Restituisce la descrizione dell'azienda.
     * @return La descrizione.
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Imposta la descrizione dell'azienda.
     * @param descrizione La nuova descrizione.
     */
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * Restituisce il nome dell'azienda.
     * @return Il nome azienda.
     */
    public String getNomeAzienda() {
        return nomeAzienda;
    }

    /**
     * Imposta il nome dell'azienda.
     * @param nomeAzienda Il nuovo nome azienda.
     */
    public void setNomeAzienda(String nomeAzienda) {
        this.nomeAzienda = nomeAzienda;
    }
}
