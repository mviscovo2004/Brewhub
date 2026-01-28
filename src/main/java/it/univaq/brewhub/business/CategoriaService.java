package it.univaq.brewhub.business;

import it.univaq.brewhub.model.Categoria;
import it.univaq.brewhub.dao.CategoriaDAO;
import it.univaq.brewhub.dao.impl.CategoriaDAOImpl;
import it.univaq.brewhub.utility.Log;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service Layer per la gestione delle Categorie.
 * <p>
 * Centralizza la logica relativa alle categorie dei post.
 * Implementa il pattern Singleton.
 * </p>
 */
public class CategoriaService {

    private static CategoriaService instance;
    private final CategoriaDAO categoriaDAO;

    private CategoriaService() {
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    public static synchronized CategoriaService getInstance() {
        if (instance == null) {
            instance = new CategoriaService();
        }
        return instance;
    }

    /**
     * Recupera tutte le categorie disponibili.
     * 
     * @return Lista di categorie.
     */
    public List<Categoria> getAllCategories() {
        try {
            return categoriaDAO.findAll();
        } catch (SQLException e) {
            Log.error("Errore recupero categorie", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera una categoria per ID.
     * 
     * @param id ID della categoria.
     * @return La categoria trovata o null.
     */
    public Categoria getCategoryById(int id) {
        try {
            return categoriaDAO.findById(id);
        } catch (SQLException e) {
            Log.error("Errore recupero categoria per ID: " + id, e);
            return null;
        }
    }

    /**
     * Crea una nuova categoria.
     * 
     * @param categoria La categoria da creare.
     * @throws BusinessException Se la categoria esiste già o errore tecnico.
     */
    public void createCategory(Categoria categoria) throws BusinessException {
        if (categoria == null || categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new BusinessException("Il nome della categoria è obbligatorio");
        }
        try {
            categoriaDAO.create(categoria);
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                throw new BusinessException("Categoria già esistente", e);
            }
            Log.error("Errore creazione categoria", e);
            throw new BusinessException("Impossibile creare la categoria", e);
        }
    }

    /**
     * Aggiorna una categoria esistente.
     * 
     * @param categoria La categoria da aggiornare.
     * @throws BusinessException Se si verifica un errore.
     */
    public void updateCategory(Categoria categoria) throws BusinessException {
        if (categoria == null || categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new BusinessException("Il nome della categoria è obbligatorio");
        }
        try {
            categoriaDAO.update(categoria);
        } catch (SQLException e) {
            Log.error("Errore aggiornamento categoria", e);
            throw new BusinessException("Impossibile aggiornare la categoria", e);
        }
    }

    /**
     * Elimina una categoria.
     * 
     * @param id ID della categoria da eliminare.
     * @throws BusinessException Se si verifica un errore.
     */
    public void deleteCategory(int id) throws BusinessException {
        try {
            categoriaDAO.delete(id);
        } catch (SQLException e) {
            Log.error("Errore eliminazione categoria", e);
            throw new BusinessException("Impossibile eliminare la categoria", e);
        }
    }
}
