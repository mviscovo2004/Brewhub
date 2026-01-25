package it.univaq.brewhub.model;

/**
 * Classe che rappresenta una sfida creata da un Torrefattore.
 */
public class Sfida {

    private int id;
    private String titolo;
    private String descrizione;
    private String premio;
    private String scadenza; // Formato YYYY-MM-DD
    private String creatore; // Username del Torrefattore
    private int partecipantiCount;
    private boolean isPartecipante; // Campo di comodo per la UI

    public Sfida() {
    }

    public Sfida(String titolo, String descrizione, String premio, String scadenza, String creatore) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.premio = premio;
        this.scadenza = scadenza;
        this.creatore = creatore;
        this.partecipantiCount = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getPremio() {
        return premio;
    }

    public void setPremio(String premio) {
        this.premio = premio;
    }

    public String getScadenza() {
        return scadenza;
    }

    public void setScadenza(String scadenza) {
        this.scadenza = scadenza;
    }

    public String getCreatore() {
        return creatore;
    }

    public void setCreatore(String creatore) {
        this.creatore = creatore;
    }

    public int getPartecipantiCount() {
        return partecipantiCount;
    }

    public void setPartecipantiCount(int partecipantiCount) {
        this.partecipantiCount = partecipantiCount;
    }

    public boolean isPartecipante() {
        return isPartecipante;
    }

    public void setPartecipante(boolean isPartecipante) {
        this.isPartecipante = isPartecipante;
    }
}
