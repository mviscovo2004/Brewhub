package it.univaq.brewhub.business;

import it.univaq.brewhub.model.Categoria;
import it.univaq.brewhub.dao.CategoriaDAO;
import it.univaq.brewhub.dao.impl.CategoriaDAOImpl;
import it.univaq.brewhub.utility.Log;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Servizio per la gestione delle categorie dei post.
 * Implementa il pattern Singleton per garantire un'unica istanza del servizio.
 * Gestisce le operazioni CRUD sulle categorie interagendo con il DAO.
 */
public class CategoriaService {

    private static CategoriaService instance;
    private final CategoriaDAO categoriaDAO;

    /**
     * Costruttore privato. Inizializza il DAO delle categorie.
     */
    private CategoriaService() {
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    /**
     * Restituisce l'istanza singleton del servizio.
     *
     * @return L'istanza unica di CategoriaService.
     */
    public static synchronized CategoriaService getInstance() {
        if (instance == null) {
            instance = new CategoriaService();
        }
        return instance;
    }

    /**
     * Recupera tutte le categorie presenti nel sistema.
     *
     * @return Una lista di oggetti Categoria. In caso di errore, restituisce una
     *         lista vuota.
     */
    public List<Categoria> getAllCategories() {
        try {
            return categoriaDAO.findAll();
        } catch (SQLException e) {
            Log.error("Errore durante il recupero delle categorie", e);
            return Collections.emptyList();
        }
    }

    /**
     * Cerca una categoria specificando il suo ID.
     *
     * @param id L'identificativo della categoria.
     * @return L'oggetto Categoria trovato, oppure null in caso di errore o se non
     *         trovato.
     */
    public Categoria getCategoryById(int id) {
        try {
            return categoriaDAO.findById(id);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero della categoria con ID: " + id, e);
            return null;
        }
    }

    /**
     * Crea una nuova categoria nel sistema.
     *
     * @param categoria L'oggetto Categoria da creare.
     * @throws BusinessException Se il nome della categoria è vuoto, se la categoria
     *                           esiste già o se si verifica un errore tecnico.
     */
    public void createCategory(Categoria categoria) throws BusinessException {
        if (categoria == null || categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new BusinessException("Il nome della categoria è obbligatorio");
        }
        try {
            categoriaDAO.create(categoria);
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                throw new BusinessException("Esiste già una categoria con questo nome", e);
            }
            Log.error("Errore durante la creazione della categoria", e);
            throw new BusinessException("Impossibile creare la categoria", e);
        }
    }

    /**
     * Modifica una categoria esistente.
     *
     * @param categoria L'oggetto Categoria con i dati aggiornati.
     * @throws BusinessException Se il nome è invalido o se si verifica un errore
     *                           durante l'aggiornamento.
     */
    public void updateCategory(Categoria categoria) throws BusinessException {
        if (categoria == null || categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new BusinessException("Il nome della categoria è obbligatorio");
        }
        if (isSystemCategory(categoria.getNome())) {
            throw new BusinessException("Le categorie di sistema non possono essere modificate.");
        }
        try {
            categoriaDAO.update(categoria);
        } catch (SQLException e) {
            Log.error("Errore durante l'aggiornamento della categoria", e);
            throw new BusinessException("Impossibile aggiornare la categoria", e);
        }
    }

    /**
     * Elimina una categoria dal sistema in base al suo ID.
     *
     * @param id L'identificativo della categoria da cancellare.
     * @throws BusinessException Se si verifica un errore durante l'eliminazione.
     */
    public void deleteCategory(int id) throws BusinessException {
        Categoria cat = getCategoryById(id);
        if (cat != null && isSystemCategory(cat.getNome())) {
            throw new BusinessException("Le categorie di sistema non possono essere eliminate.");
        }
        try {
            categoriaDAO.delete(id);
        } catch (SQLException e) {
            Log.error("Errore durante l'eliminazione della categoria", e);
            throw new BusinessException("Impossibile eliminare la categoria", e);
        }
    }

    /**
     * Verifica se una categoria è una categoria di sistema predefinita.
     * Le categorie di sistema non possono essere modificate o eliminate.
     * 
     * @param name Nome della categoria.
     * @return true se è una categoria di sistema.
     */
    public boolean isSystemCategory(String name) {
        if (name == null)
            return false;
        String n = name.toLowerCase();
        return n.equals("torrefattori") || n.equals("miscele") || n.equals("guide");
    }
}
