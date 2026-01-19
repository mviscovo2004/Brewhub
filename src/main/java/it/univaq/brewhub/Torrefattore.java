package it.univaq.brewhub;

/**
 * Classe che rappresenta un Torrefattore.
 * Estende la classe Utente aggiungendo dettagli specifici.
 */
public class Torrefattore extends Utente {

    private String nomeAzienda;
    private String partitaIva;
    private String indirizzo;
    private String descrizione;

    /**
     * Costruttore di default.
     */
    public Torrefattore() {
        super();
        this.setTipo(TipoUtente.TORREFATTORE);
    }

    /**
     * Costruttore completo.
     * 
     * @param nome        Il nome.
     * @param cognome     Il cognome.
     * @param username    Lo username.
     * @param password    La password.
     * @param fotoProfilo L'URI della foto profilo.
     * @param partitaIva  La Partita IVA.
     * @param indirizzo   L'indirizzo della torrefazione.
     * @param descrizione Una breve descrizione.
     * @param nomeAzienda Il nome dell'azienda.
     */
    public Torrefattore(String nome, String cognome, String username, String password, String fotoProfilo,
            String partitaIva, String indirizzo, String descrizione, String nomeAzienda) {
        super(nome, cognome, username, password, TipoUtente.TORREFATTORE, fotoProfilo);
        this.partitaIva = partitaIva;
        this.indirizzo = indirizzo;
        this.descrizione = descrizione;
        this.nomeAzienda = nomeAzienda;
    }

    public String getPartitaIva() {
        return partitaIva;
    }

    public void setPartitaIva(String partitaIva) {
        this.partitaIva = partitaIva;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getNomeAzienda() {
        return nomeAzienda;
    }

    public void setNomeAzienda(String nomeAzienda) {
        this.nomeAzienda = nomeAzienda;
    }
}
