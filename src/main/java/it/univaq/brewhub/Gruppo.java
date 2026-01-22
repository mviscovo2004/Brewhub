package it.univaq.brewhub;

import java.util.List;

public class Gruppo {
    private int id;
    private String nome;
    private String creatore;
    private List<String> membri; // Optional, loaded on demand

    public Gruppo() {
    }

    public Gruppo(int id, String nome, String creatore) {
        this.id = id;
        this.nome = nome;
        this.creatore = creatore;
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

    public String getCreatore() {
        return creatore;
    }

    public void setCreatore(String creatore) {
        this.creatore = creatore;
    }

    public List<String> getMembri() {
        return membri;
    }

    public void setMembri(List<String> membri) {
        this.membri = membri;
    }
}
