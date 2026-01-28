package it.univaq.brewhub.model;

/**
 * Rappresenta una categoria per classificare i post.
 * <p>
 * Le categorie aiutano a organizzare i contenuti (es. "Torrefattori", "Eventi").
 * Ogni categoria ha un nome univoco e un'icona opzionale.
 * </p>
 */
public class Categoria {

    /** Identificativo univoco della categoria. */
    private int id;
    
    /** Nome della categoria (es. "Miscele"). */
    private String nome;
    
    /** Identificativo o percorso dell'icona associata. */
    private String icona;

    /**
     * Costruttore vuoto.
     */
    public Categoria() {
    }

    /**
     * Costruttore con ID e nome.
     * @param id Identificativo.
     * @param nome Nome della categoria.
     */
    public Categoria(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    /**
     * Costruttore completo.
     * @param id Identificativo.
     * @param nome Nome della categoria.
     * @param icona Icona della categoria.
     */
    public Categoria(int id, String nome, String icona) {
        this.id = id;
        this.nome = nome;
        this.icona = icona;
    }

    /**
     * Costruttore per nuova categoria (senza ID).
     * @param nome Nome della categoria.
     */
    public Categoria(String nome) {
        this.nome = nome;
    }

    /**
     * Costruttore per nuova categoria con icona.
     * @param nome Nome della categoria.
     * @param icona Icona della categoria.
     */
    public Categoria(String nome, String icona) {
        this.nome = nome;
        this.icona = icona;
    }

    /**
     * Restituisce l'ID della categoria.
     * @return L'identificativo.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID della categoria.
     * @param id Il nuovo identificativo.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il nome della categoria.
     * @return Il nome.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Imposta il nome della categoria.
     * @param nome Il nuovo nome.
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Restituisce l'icona della categoria.
     * @return La stringa dell'icona.
     */
    public String getIcona() {
        return icona;
    }

    /**
     * Imposta l'icona della categoria.
     * @param icona La nuova icona.
     */
    public void setIcona(String icona) {
        this.icona = icona;
    }

    /**
     * Restituisce la rappresentazione a stringa (il nome).
     * @return Il nome della categoria.
     */
    @Override
    public String toString() {
        return nome;
    }
}
