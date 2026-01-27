package it.univaq.brewhub.business;

import it.univaq.brewhub.Post;
import it.univaq.brewhub.dao.PostDAO;
import it.univaq.brewhub.dao.impl.PostDAOImpl;
import it.univaq.brewhub.utility.Log;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service Layer per la gestione dei Post.
 * <p>
 * Centralizza la logica relativa ai contenuti, feed e interazioni (like).
 * Implementa il pattern Singleton.
 * </p>
 */
public class PostService {

    private static PostService instance;
    private final PostDAO postDAO;

    private PostService() {
        this.postDAO = new PostDAOImpl();
    }

    public static synchronized PostService getInstance() {
        if (instance == null) {
            instance = new PostService();
        }
        return instance;
    }

    /**
     * Pubblica un nuovo post.
     * @param post Il post da creare.
     * @throws BusinessException Se mancano dati o errore tecnico.
     */
    public void createPost(Post post) throws BusinessException {
        if (post.getTitolo() == null || post.getTitolo().isBlank()) {
            throw new BusinessException("Il titolo del post è obbligatorio.");
        }
        try {
            postDAO.create(post);
        } catch (SQLException e) {
            Log.error("Errore creazione post", e);
            throw new BusinessException("Impossibile pubblicare il post.", e);
        }
    }

    /**
     * Recupera il feed principale.
     * @return Lista di post.
     */
    public List<Post> getAllPosts() {
        try {
            return postDAO.findAll();
        } catch (SQLException e) {
            Log.error("Errore recupero feed", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera i post popolari.
     * @return Lista di post popolari.
     */
    public List<Post> getPopularPosts() {
        try {
            return postDAO.findPopular();
        } catch (SQLException e) {
            Log.error("Errore recupero popolari", e);
            return Collections.emptyList();
        }
    }
}
