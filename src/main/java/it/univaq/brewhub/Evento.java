package it.univaq.brewhub;

/**
 * Classe che rappresenta un evento creato da un Torrefattore.
 */
public class Evento {

    private int id;
    private String nome;
    private String descrizione;
    private String data;
    private String luogo;
    private String organizzatore; // Username del Torrefattore
    private int partecipantiCount;

    public Evento() {
    }

    public Evento(String nome, String descrizione, String data, String luogo, String organizzatore) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.data = data;
        this.luogo = luogo;
        this.organizzatore = organizzatore;
        this.partecipantiCount = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getLuogo() {
        return luogo;
    }

    public void setLuogo(String luogo) {
        this.luogo = luogo;
    }

    public String getOrganizzatore() {
        return organizzatore;
    }

    public void setOrganizzatore(String organizzatore) {
        this.organizzatore = organizzatore;
    }

    public int getPartecipantiCount() {
        return partecipantiCount;
    }

    public void setPartecipantiCount(int partecipantiCount) {
        this.partecipantiCount = partecipantiCount;
    }
}
