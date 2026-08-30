package it.univaq.brewhub.business;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import it.univaq.brewhub.dao.PostDAO;
import it.univaq.brewhub.dao.impl.PostDAOImpl;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.utility.Log;

/**
 * Service Layer per la gestione dei Post e delle relative interazioni (Like,
 * Commenti, Recensioni).
 * Centralizza tutta la logica di business riguardante i contenuti pubblicati
 * dagli utenti.
 * Implementa il pattern Singleton.
 */
public class PostService {

    private static PostService instance;
    private final PostDAO postDAO;
    private final it.univaq.brewhub.dao.CommentoDAO commentoDAO;
    private final it.univaq.brewhub.dao.RecensioneDAO recensioneDAO;

    /**
     * Costruttore privato che inizializza i DAO necessari.
     */
    private PostService() {
        this.postDAO = new PostDAOImpl();
        this.commentoDAO = new it.univaq.brewhub.dao.impl.CommentoDAOImpl();
        this.recensioneDAO = new it.univaq.brewhub.dao.impl.RecensioneDAOImpl();
    }

    /**
     * Restituisce l'istanza singleton di PostService.
     *
     * @return L'istanza unica del servizio.
     */
    public static synchronized PostService getInstance() {
        if (instance == null) {
            instance = new PostService();
        }
        return instance;
    }

    /**
     * Pubblica un nuovo post nel sistema.
     *
     * @param post L'oggetto Post da creare.
     * @throws BusinessException Se il titolo è vuoto o si verifica un errore
     *                           durante il salvataggio.
     */
    public void createPost(Post post) throws BusinessException {
        if (post.getTitolo() == null || post.getTitolo().isBlank()) {
            throw new BusinessException("Il titolo del post è obbligatorio.");
        }
        try {
            postDAO.create(post);
        } catch (SQLException e) {
            Log.error("Errore durante la creazione del post", e);
            throw new BusinessException("Impossibile pubblicare il post.", e);
        }
    }

    /**
 * Modifica un post esistente
 *
 * @param post                  L'oggetto Post contenente i dati aggiornati.
 * @throws BusinessException    nel caso  post non valido o si verifica un errore
 *                              durante l'aggiornamento.
 */
public void updatePost(Post post) throws BusinessException {
    if (post == null) {
        throw new BusinessException("Il post da modificare non può essere nullo.");
    }

    if (post.getId() <= 0) {
        throw new BusinessException("ID del post non valido.");
    }

    if (post.getTitolo() == null || post.getTitolo().isBlank()) {
        throw new BusinessException("Il titolo del post è obbligatorio.");
    }

    try {
        postDAO.update(post);
    } catch (SQLException e) {
        Log.error("Errore durante la modifica del post", e);
        throw new BusinessException("Impossibile modificare il post.", e);
    }
}



    /**
     * Recupera il feed principale contenente tutti i post.
     *
     * @return Una lista di tutti i post.
     */
    public List<Post> getAllPosts() {
        try {
            return postDAO.findAll();
        } catch (SQLException e) {
            Log.error("Errore durante il recupero del feed", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera i post popolari (es. quelli con più like).
     *
     * @return Una lista dei post più popolari.
     */
    public List<Post> getPopularPosts() {
        try {
            return postDAO.findPopular();
        } catch (SQLException e) {
            Log.error("Errore durante il recupero dei post popolari", e);
            return Collections.emptyList();
        }
    }

    /**
     * Genera un feed personalizzato per un utente specifico (es. post degli utenti
     * seguiti).
     *
     * @param username L'username dell'utente.
     * @return Una lista di post personalizzata.
     */
    public List<Post> getFeedForUser(String username) {
        try {
            return postDAO.findFeedForUser(username);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero del feed personalizzato", e);
            return Collections.emptyList();
        }
    }

    /**
     * Restituisce la lista dei post a cui l'utente ha messo "Mi Piace".
     *
     * @param username L'username dell'utente.
     * @return Una lista di post piaciuti.
     */
    public List<Post> getLikedPosts(String username) {
        try {
            return postDAO.findLikedBy(username);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero dei post piaciuti", e);
            return Collections.emptyList();
        }
    }

    /**
     * Filtra i post per una specifica categoria.
     *
     * @param categoryId L'ID della categoria.
     * @return Una lista di post appartenenti alla categoria.
     */
    public List<Post> getPostsByCategory(int categoryId) {
        try {
            return postDAO.findByCategory(categoryId);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero dei post per categoria", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera i post creati da una specifica tipologia di utente (es. solo
     * Torrefattori).
     *
     * @param userType Il tipo di utente (stringa).
     * @return Una lista di post filtrati per tipo autore.
     */
    public List<Post> getPostsByUserType(String userType) {
        try {
            return postDAO.findByUserType(userType);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero dei post per tipo utente", e);
            return Collections.emptyList();
        }
    }

    /**
     * Esegue una ricerca testuale tra i post (titolo, contenuto).
     *
     * @param query La stringa da cercare.
     * @return Una lista di post che corrispondono alla ricerca.
     */
    public List<Post> searchPosts(String query) {
        try {
            return postDAO.search(query);
        } catch (SQLException e) {
            Log.error("Errore durante la ricerca dei post", e);
            return Collections.emptyList();
        }
    }

    /**
     * Calcola il numero totale di post presenti nel sistema.
     *
     * @return Il numero totale di post.
     */
    public int getTotalPostsCount() {
        try {
            return postDAO.countAll();
        } catch (SQLException e) {
            Log.error("Errore durante il conteggio dei post", e);
            return 0;
        }
    }

    /**
     * Calcola il numero di post pubblicati nelle ultime 24 ore.
     *
     * @return Il numero di post recenti.
     */
    public int getPostsLast24hCount() {
        try {
            return postDAO.countPostsLast24h();
        } catch (SQLException e) {
            Log.error("Errore durante il conteggio dei post delle ultime 24h", e);
            return 0;
        }
    }

    /**
     * Recupera un singolo post tramite il suo ID.
     *
     * @param id L'ID del post.
     * @return L'oggetto Post, o null se non trovato.
     */
    public Post getPostById(int id) {
        try {
            return postDAO.findById(id);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero del post per ID", e);
            return null;
        }
    }

    /**
     * Aggiunge un "Mi Piace" a un post da parte di un utente.
     *
     * @param postId   L'ID del post.
     * @param username L'username dell'utente.
     * @throws BusinessException Se si verifica un errore durante l'operazione.
     */
    public void addLike(int postId, String username) throws BusinessException {
        try {
            postDAO.addLike(postId, username);
        } catch (SQLException e) {
            Log.error("Errore durante l'aggiunta del like", e);
            throw new BusinessException("Impossibile mettere like", e);
        }
    }

    /**
     * Rimuove un "Mi Piace" precedentemente assegnato.
     *
     * @param postId   L'ID del post.
     * @param username L'username dell'utente.
     * @throws BusinessException Se si verifica un errore durante l'operazione.
     */
    public void removeLike(int postId, String username) throws BusinessException {
        try {
            postDAO.removeLike(postId, username);
        } catch (SQLException e) {
            Log.error("Errore durante la rimozione del like", e);
            throw new BusinessException("Impossibile rimuovere like", e);
        }
    }

    /**
     * Verifica se un utente ha già messo "Mi Piace" a un post.
     *
     * @param postId   L'ID del post.
     * @param username L'username dell'utente.
     * @return true se il like è presente, false altrimenti.
     */
    public boolean isLiked(int postId, String username) {
        try {
            return postDAO.isLiked(postId, username);
        } catch (SQLException e) {
            Log.error("Errore durante la verifica del like", e);
            return false;
        }
    }

    /**
     * Restituisce il numero totale di "Mi Piace" per un post.
     *
     * @param postId L'ID del post.
     * @return Il numero di like.
     */
    public int getLikesCount(int postId) {
        try {
            return postDAO.getLikesCount(postId);
        } catch (SQLException e) {
            Log.error("Errore durante il conteggio dei like", e);
            return 0;
        }
    }

    // ==================== COMMENTI ====================

    /**
     * Aggiunge un commento a un post.
     *
     * @param commento L'oggetto Commento da aggiungere.
     * @throws BusinessException Se il contenuto è vuoto o si verifica un errore.
     */
    public void addComment(it.univaq.brewhub.model.Commento commento) throws BusinessException {
        if (commento == null || commento.getContenuto() == null || commento.getContenuto().isBlank()) {
            throw new BusinessException("Il commento non può essere vuoto");
        }
        try {
            commentoDAO.create(commento);
        } catch (SQLException e) {
            Log.error("Errore durante la creazione del commento", e);
            throw new BusinessException("Impossibile pubblicare il commento", e);
        }
    }

    /**
     * Elimina un commento esistente.
     *
     * @param commentId L'ID del commento.
     * @throws BusinessException Se si verifica un errore.
     */
    public void deleteComment(int commentId) throws BusinessException {
        try {
            commentoDAO.delete(commentId);
        } catch (SQLException e) {
            Log.error("Errore durante l'eliminazione del commento", e);
            throw new BusinessException("Impossibile eliminare il commento", e);
        }
    }

    /**
     * Modifica il testo di un commento esistente.
     *
     * @param commento L'oggetto Commento con i dati aggiornati.
     * @throws BusinessException Se si verifica un errore.
     */
    public void updateComment(it.univaq.brewhub.model.Commento commento) throws BusinessException {
        try {
            commentoDAO.update(commento);
        } catch (SQLException e) {
            Log.error("Errore durante l'aggiornamento del commento", e);
            throw new BusinessException("Impossibile modificare il commento", e);
        }
    }

    /**
     * Recupera tutti i commenti associati a un post.
     *
     * @param postId L'ID del post.
     * @return Una lista di commenti.
     */
    public List<it.univaq.brewhub.model.Commento> getComments(int postId) {
        try {
            return commentoDAO.findByPostId(postId);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero dei commenti", e);
            return Collections.emptyList();
        }
    }

    // ==================== RECENSIONI ====================

    /**
     * Aggiunge una recensione a un post.
     *
     * @param recensione L'oggetto Recensione.
     * @throws BusinessException Se il voto non è valido (1-5) o si verifica un
     *                           errore.
     */
    public void addReview(it.univaq.brewhub.model.Recensione recensione) throws BusinessException {
        if (recensione == null || recensione.getVoto() < 1 || recensione.getVoto() > 5) {
            throw new BusinessException("Voto non valido (deve essere compreso tra 1 e 5)");
        }
        try {
            recensioneDAO.create(recensione);
        } catch (SQLException e) {
            Log.error("Errore durante la creazione della recensione", e);
            throw new BusinessException("Impossibile pubblicare la recensione", e);
        }
    }

    /**
    * Modifica di una recensione esistente.
    *
    * @param recensione recensione contenente voto e testo aggiornati.
    * @throws BusinessException Se la recensione non è valida o si verifica un errore.
    */
    public void updateReview(it.univaq.brewhub.model.Recensione recensione) throws BusinessException {
        if (recensione == null) {
            throw new BusinessException("La recensione da modificare non può essere nulla.");
        }

        if (recensione.getId() <= 0) {
            throw new BusinessException("ID della recensione non valido.");
        }

        if (recensione.getVoto() < 1 || recensione.getVoto() > 5) {
            throw new BusinessException("Voto non valido (deve essere compreso tra 1 e 5).");
        }

        try {
            recensioneDAO.update(recensione);
        } catch (SQLException e) {
            Log.error("Errore durante la modifica della recensione", e);
            throw new BusinessException("Impossibile modificare la recensione.", e);
        }
    }

    /**
     * Recupera tutte le recensioni di un post.
     *
     * @param postId L'ID del post.
     * @return Una lista di recensioni.
     */
    public List<it.univaq.brewhub.model.Recensione> getReviews(int postId) {
        try {
            return recensioneDAO.findByPost(postId);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero delle recensioni", e);
            return Collections.emptyList();
        }
    }

    /**
     * Calcola la media dei voti delle recensioni per un post.
     *
     * @param postId L'ID del post.
     * @return La media dei voti.
     */
    public double getAverageRating(int postId) {
        try {
            return recensioneDAO.getAverageRating(postId);
        } catch (SQLException e) {
            Log.error("Errore durante il calcolo della media voti", e);
            return 0.0;
        }
    }

    /**
     * Verifica se un utente ha già recensito un determinato post.
     *
     * @param postId   L'ID del post.
     * @param username L'username dell'utente.
     * @return true se la recensione esiste, false altrimenti.
     */
    public boolean hasUserReviewed(int postId, String username) {
        try {
            return recensioneDAO.hasUserReviewed(postId, username);
        } catch (SQLException e) {
            Log.error("Errore durante la verifica della recensione utente", e);
            return false;
        }
    }

    /**
     * Elimina un post dal sistema.
     *
     * @param id L'ID del post da eliminare.
     * @throws BusinessException Se si verifica un errore durante l'operazione.
     */
    public void deletePost(int id) throws BusinessException {
        try {
            postDAO.delete(id);
        } catch (SQLException e) {
            Log.error("Errore durante l'eliminazione del post", e);
            throw new BusinessException("Impossibile eliminare il post", e);
        }
    }

    /**
     * Recupera tutti i post creati da uno specifico autore.
     *
     * @param username L'username dell'autore.
     * @return Una lista di post.
     */
    public List<Post> getPostsByAuthor(String username) {
        try {
            return postDAO.findByAuthor(username);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero dei post dell'autore", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera il feed dei post degli utenti seguiti.
     *
     * @param username L'username dell'utente corrente.
     * @return Una lista di post.
     */
    public List<Post> getPostsFromFollowed(String username) {
        return getFeedForUser(username);
    }

    /**
     * Recupera tutti i post pubblicati dai Torrefattori.
     *
     * @return Una lista di post.
     */
    public List<Post> getPostsByTorrefattori() {
        return getPostsByUserType("TORREFATTORE");
    }

    /**
     * Recupera i post salvati nell'archivio personale di un utente.
     *
     * @param username L'username dell'utente.
     * @return Una lista di post salvati.
     */
    public List<Post> getSavedPosts(String username) {
        try {
            return postDAO.findSavedBy(username);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero dei post salvati", e);
            return Collections.emptyList();
        }
    }
}
