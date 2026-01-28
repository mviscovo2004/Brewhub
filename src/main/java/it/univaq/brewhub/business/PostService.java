package it.univaq.brewhub.business;

import it.univaq.brewhub.model.Post;
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
    private final it.univaq.brewhub.dao.CommentoDAO commentoDAO;
    private final it.univaq.brewhub.dao.RecensioneDAO recensioneDAO;

    private PostService() {
        this.postDAO = new PostDAOImpl();
        this.commentoDAO = new it.univaq.brewhub.dao.impl.CommentoDAOImpl();
        this.recensioneDAO = new it.univaq.brewhub.dao.impl.RecensioneDAOImpl();
    }

    public static synchronized PostService getInstance() {
        if (instance == null) {
            instance = new PostService();
        }
        return instance;
    }

    /**
     * Pubblica un nuovo post.
     * 
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
     * 
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
     * 
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

    /**
     * Recupera il feed personalizzato per un utente.
     * 
     * @param username Username dell'utente.
     * @return Lista di post del feed personalizzato.
     */
    public List<Post> getFeedForUser(String username) {
        try {
            return postDAO.findFeedForUser(username);
        } catch (SQLException e) {
            Log.error("Errore recupero feed personalizzato", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera i post piaciuti da un utente.
     * 
     * @param username Username dell'utente.
     * @return Lista di post piaciuti.
     */
    public List<Post> getLikedPosts(String username) {
        try {
            return postDAO.findLikedBy(username);
        } catch (SQLException e) {
            Log.error("Errore recupero post piaciuti", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera i post di una categoria.
     * 
     * @param categoryId ID della categoria.
     * @return Lista di post della categoria.
     */
    public List<Post> getPostsByCategory(int categoryId) {
        try {
            return postDAO.findByCategory(categoryId);
        } catch (SQLException e) {
            Log.error("Errore recupero post per categoria", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera i post per tipo di utente.
     * 
     * @param userType Tipo di utente (es. TORREFATTORE).
     * @return Lista di post.
     */
    public List<Post> getPostsByUserType(String userType) {
        try {
            return postDAO.findByUserType(userType);
        } catch (SQLException e) {
            Log.error("Errore recupero post per tipo utente", e);
            return Collections.emptyList();
        }
    }

    /**
     * Cerca post per query.
     * 
     * @param query Testo di ricerca.
     * @return Lista di post trovati.
     */
    public List<Post> searchPosts(String query) {
        try {
            return postDAO.search(query);
        } catch (SQLException e) {
            Log.error("Errore ricerca post", e);
            return Collections.emptyList();
        }
    }

    /**
     * Conta il totale dei post.
     * 
     * @return Numero totale di post.
     */
    public int getTotalPostsCount() {
        try {
            return postDAO.countAll();
        } catch (SQLException e) {
            Log.error("Errore conteggio post", e);
            return 0;
        }
    }

    /**
     * Conta i post delle ultime 24 ore.
     * 
     * @return Numero di post delle ultime 24h.
     */
    public int getPostsLast24hCount() {
        try {
            return postDAO.countPostsLast24h();
        } catch (SQLException e) {
            Log.error("Errore conteggio post 24h", e);
            return 0;
        }
    }

    /**
     * Recupera un post per ID.
     * 
     * @param id ID del post.
     * @return Il post trovato o null.
     */
    public Post getPostById(int id) {
        try {
            return postDAO.findById(id);
        } catch (SQLException e) {
            Log.error("Errore recupero post per id", e);
            return null;
        }
    }

    /**
     * Aggiunge un "Mi Piace" a un post.
     * 
     * @param postId   ID del post.
     * @param username Username dell'utente.
     * @throws BusinessException Se si verifica un errore.
     */
    public void addLike(int postId, String username) throws BusinessException {
        try {
            postDAO.addLike(postId, username);
        } catch (SQLException e) {
            Log.error("Errore aggiunta like", e);
            throw new BusinessException("Impossibile mettere like", e);
        }
    }

    /**
     * Rimuove un "Mi Piace" da un post.
     * 
     * @param postId   ID del post.
     * @param username Username dell'utente.
     * @throws BusinessException Se si verifica un errore.
     */
    public void removeLike(int postId, String username) throws BusinessException {
        try {
            postDAO.removeLike(postId, username);
        } catch (SQLException e) {
            Log.error("Errore rimozione like", e);
            throw new BusinessException("Impossibile rimuovere like", e);
        }
    }

    /**
     * Verifica se un utente ha messo "Mi Piace".
     * 
     * @param postId   ID post.
     * @param username Username.
     * @return true se like esiste.
     */
    public boolean isLiked(int postId, String username) {
        try {
            return postDAO.isLiked(postId, username);
        } catch (SQLException e) {
            Log.error("Errore verifica like", e);
            return false;
        }
    }

    /**
     * Conta i "Mi Piace" di un post.
     * 
     * @param postId ID post.
     * @return Numero di like.
     */
    public int getLikesCount(int postId) {
        try {
            return postDAO.getLikesCount(postId);
        } catch (SQLException e) {
            Log.error("Errore conteggio like", e);
            return 0;
        }
    }

    // ==================== COMMENTI ====================

    public void addComment(it.univaq.brewhub.model.Commento commento) throws BusinessException {
        if (commento == null || commento.getContenuto() == null || commento.getContenuto().isBlank()) {
            throw new BusinessException("Il commento non può essere vuoto");
        }
        try {
            commentoDAO.create(commento);
        } catch (SQLException e) {
            Log.error("Errore creazione commento", e);
            throw new BusinessException("Impossibile pubblicare il commento", e);
        }
    }

    public void deleteComment(int commentId) throws BusinessException {
        try {
            commentoDAO.delete(commentId);
        } catch (SQLException e) {
            Log.error("Errore eliminazione commento", e);
            throw new BusinessException("Impossibile eliminare il commento", e);
        }
    }

    public void updateComment(it.univaq.brewhub.model.Commento commento) throws BusinessException {
        try {
            commentoDAO.update(commento);
        } catch (SQLException e) {
            Log.error("Errore aggiornamento commento", e);
            throw new BusinessException("Impossibile modificare il commento", e);
        }
    }

    public List<it.univaq.brewhub.model.Commento> getComments(int postId) {
        try {
            return commentoDAO.findByPostId(postId);
        } catch (SQLException e) {
            Log.error("Errore recupero commenti", e);
            return Collections.emptyList();
        }
    }

    // ==================== RECENSIONI ====================

    public void addReview(it.univaq.brewhub.model.Recensione recensione) throws BusinessException {
        if (recensione == null || recensione.getVoto() < 1 || recensione.getVoto() > 5) {
            throw new BusinessException("Voto non valido (1-5)");
        }
        try {
            recensioneDAO.create(recensione);
        } catch (SQLException e) {
            Log.error("Errore creazione recensione", e);
            throw new BusinessException("Impossibile pubblicare la recensione", e);
        }
    }

    public List<it.univaq.brewhub.model.Recensione> getReviews(int postId) {
        try {
            return recensioneDAO.findByPost(postId);
        } catch (SQLException e) {
            Log.error("Errore recupero recensioni", e);
            return Collections.emptyList();
        }
    }

    public double getAverageRating(int postId) {
        try {
            return recensioneDAO.getAverageRating(postId);
        } catch (SQLException e) {
            Log.error("Errore calcolo media voti", e);
            return 0.0;
        }
    }

    public boolean hasUserReviewed(int postId, String username) {
        try {
            return recensioneDAO.hasUserReviewed(postId, username);
        } catch (SQLException e) {
            Log.error("Errore verifica recensione utente", e);
            return false;
        }
    }

    public void deletePost(int id) throws BusinessException {
        try {
            postDAO.delete(id);
        } catch (SQLException e) {
            Log.error("Errore eliminazione post", e);
            throw new BusinessException("Impossibile eliminare il post", e);
        }
    }

    public List<Post> getPostsByAuthor(String username) {
        try {
            return postDAO.findByAuthor(username);
        } catch (SQLException e) {
            Log.error("Errore recupero post autore", e);
            return Collections.emptyList();
        }
    }
}
