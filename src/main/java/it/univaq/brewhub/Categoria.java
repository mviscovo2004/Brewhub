package it.univaq.brewhub;

/**
 * Modello che rappresenta una Categoria di post.
 */
public class Categoria {
    private int id;
    private String nome;
    private String icona;

    public Categoria() {
    }

    public Categoria(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Categoria(int id, String nome, String icona) {
        this.id = id;
        this.nome = nome;
        this.icona = icona;
    }

    public Categoria(String nome) {
        this.nome = nome;
    }

    public Categoria(String nome, String icona) {
        this.nome = nome;
        this.icona = icona;
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

    public String getIcona() {
        return icona;
    }

    public void setIcona(String icona) {
        this.icona = icona;
    }

    @Override
    public String toString() {
        return nome;
    }
}
