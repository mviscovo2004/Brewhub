package it.univaq.brewhub.model;

/**
 * Classe che rappresenta una categoria nel sistema BrewHub.
 * Le categorie sono utilizzate per raggruppare i post e facilitare la
 * navigazione.
 * Esempi di categorie possono essere "Torrefattori", "Miscele", ecc.
 */
public class Categoria {

    /**
     * Identificativo univoco della categoria.
     */
    private int id;

    /**
     * Nome visualizzato della categoria.
     */
    private String nome;

    /**
     * Riferimento iconico (es. emoji o percorso risorsa) associato alla categoria.
     */
    private String icona;

    /**
     * Costruttore predefinito.
     */
    public Categoria() {
    }

    /**
     * Costruttore che inizializza l'oggetto con un ID e un nome.
     *
     * @param id   L'ID univoco della categoria.
     * @param nome Il nome della categoria.
     */
    public Categoria(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    /**
     * Costruttore completo che inizializza tutti i campi della categoria.
     *
     * @param id    L'ID univoco della categoria.
     * @param nome  Il nome della categoria.
     * @param icona L'icona associata alla categoria.
     */
    public Categoria(int id, String nome, String icona) {
        this.id = id;
        this.nome = nome;
        this.icona = icona;
    }

    /**
     * Costruttore per creare una nuova categoria senza specificare l'ID (utile per
     * inserimenti).
     *
     * @param nome Il nome della categoria.
     */
    public Categoria(String nome) {
        this.nome = nome;
    }

    /**
     * Costruttore per creare una nuova categoria con nome e icona, senza ID.
     *
     * @param nome  Il nome della categoria.
     * @param icona L'icona associata alla categoria.
     */
    public Categoria(String nome, String icona) {
        this.nome = nome;
        this.icona = icona;
    }

    /**
     * Restituisce l'identificativo della categoria.
     *
     * @return L'ID della categoria.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'identificativo della categoria.
     *
     * @param id Il nuovo ID da assegnare.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il nome della categoria.
     *
     * @return Il nome della categoria.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Imposta il nome della categoria.
     *
     * @param nome Il nuovo nome da assegnare.
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Restituisce l'icona associata alla categoria.
     *
     * @return L'icona della categoria.
     */
    public String getIcona() {
        return icona;
    }

    /**
     * Imposta l'icona associata alla categoria.
     *
     * @param icona La nuova icona da assegnare.
     */
    public void setIcona(String icona) {
        this.icona = icona;
    }

    /**
     * Restituisce una rappresentazione in formato stringa dell'oggetto,
     * corrispondente al nome della categoria.
     *
     * @return Il nome della categoria.
     */
    @Override
    public String toString() {
        return nome;
    }
}
