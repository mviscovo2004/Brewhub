package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Categoria;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO (Data Access Object) per la gestione delle entità
 * {@link Categoria}.
 * Definisce i metodi CRUD e le operazioni di ricerca per le categorie dei post.
 */
public interface CategoriaDAO {

    /**
     * Persiste una nuova categoria nel database.
     *
     * @param c L'oggetto Categoria da salvare.
     * @throws SQLException Se si verifica un errore durante l'operazione SQL.
     */
    void create(Categoria c) throws SQLException;

    /**
     * Recupera l'elenco completo di tutte le categorie presenti nel sistema.
     *
     * @return Una lista di oggetti Categoria.
     * @throws SQLException Se si verifica un errore durante il recupero dei dati.
     */
    List<Categoria> findAll() throws SQLException;

    /**
     * Aggiorna i dati di una categoria esistente.
     *
     * @param c L'oggetto Categoria con i dati aggiornati.
     * @throws SQLException Se si verifica un errore durante l'aggiornamento.
     */
    void update(Categoria c) throws SQLException;

    /**
     * Elimina una categoria dal database identificandola tramite il suo ID.
     *
     * @param id L'identificativo univoco della categoria da eliminare.
     * @throws SQLException Se si verifica un errore durante l'eliminazione.
     */
    void delete(int id) throws SQLException;

    /**
     * Cerca una categoria specifica tramite il suo ID.
     *
     * @param id L'identificativo della categoria.
     * @return L'oggetto Categoria trovato, oppure null se non esiste.
     * @throws SQLException Se si verifica un errore durante la ricerca.
     */
    Categoria findById(int id) throws SQLException;

    /**
     * Cerca una categoria tramite il suo nome.
     *
     * @param name Il nome della categoria da cercare.
     * @return L'oggetto Categoria trovato, oppure null se non esiste.
     * @throws SQLException Se si verifica un errore durante la ricerca.
     */
    Categoria findByName(String name) throws SQLException;
}
