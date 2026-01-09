package it.univaq.brewhub;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import it.univaq.brewhub.dao.impl.CommentoDAOImpl;
import it.univaq.brewhub.dao.impl.PostDAOImpl;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.Post.TipoPost;

/**
 * Classe di test per la classe Commento.
 * Verifica il funzionamento dei costruttori, dei getter/setter e dei metodi di
 * persistenza.
 */
public class CommentoTest {

    /**
     * Test del costruttore completo e dei metodi getter di base.
     * Verifica che l'oggetto Commento venga inizializzato con i valori corretti.
     */
    @Test
    public void testCostruttoreCommento() {
        Utente u = new Utente();
        Post p = new Post();
        String contenuto = "Bel post!";
        LocalDateTime now = LocalDateTime.now();

        Commento c = new Commento(u, p, contenuto, now);

        assertEquals(u, c.getUtente());
        assertEquals(p, c.getPost());
        assertEquals(contenuto, c.getContenuto());
        assertEquals(now, c.getDataCreazione());
    }

    /**
     * Test di integrazione con il Database per i Commenti.
     * Copre il ciclo di vita completo:
     * 1. Registrazione di un utente e creazione di un post (prerequisiti).
     * 2. Salvataggio del commento.
     * 3. Verifica dell'ID generato.
     * 4. Caricamento dei commenti per il post.
     * 5. Eliminazione del commento.
     * 
     * @throws SQLException In caso di errori SQL durante le operazioni.
     */
    @Test
    public void testMetodiDB() throws SQLException {
        UtenteDAOImpl utenteDAO = new UtenteDAOImpl();
        PostDAOImpl postDAO = new PostDAOImpl();
        CommentoDAOImpl commentoDAO = new CommentoDAOImpl();

        // 1. Setup Utente
        Utente autore = new Utente("AutoreC", "CognomeC", "userCommento", "pw", Utente.TipoUtente.APPASSIONATO, null);
        try {
            utenteDAO.create(autore);
        } catch (SQLException e) {
        } // Ignora se esiste

        // 2. Setup Post (deve essere salvato per avere un ID)
        Post p = new Post();
        p.setAutore(autore);
        p.setTitolo("Post per Commenti");
        p.setContenuto("Contenuto");
        p.setTipo(TipoPost.TESTO);
        postDAO.create(p);
        assertTrue(p.getId() > 0, "Il post dovrebbe avere un ID dopo il salvataggio");

        // 3. Creazione e Salvataggio Commento
        Commento c = new Commento();
        c.setPost(p);
        c.setUtente(autore); // Commenta lo stesso autore per semplicità
        c.setContenuto("Primo Commento");
        c.setDataCreazione(LocalDateTime.now()); // Set data

        commentoDAO.create(c);
        assertTrue(c.getId() > 0, "Il commento dovrebbe avere un ID dopo il salvataggio");

        // 4. Caricamento Commenti
        List<Commento> commenti = commentoDAO.findByPost(p);
        assertFalse(commenti.isEmpty());
        // Potrebbero esserci altri commenti vecchi
        boolean found = commenti.stream().anyMatch(comm -> comm.getContenuto().equals("Primo Commento"));
        assertTrue(found);

        // 5. Eliminazione Commento
        commentoDAO.delete(c.getId());

        commenti = commentoDAO.findByPost(p);
        boolean foundAfterDelete = commenti.stream().anyMatch(comm -> comm.getId() == c.getId());
        assertFalse(foundAfterDelete, "Il commento non dovrebbe essere presente dopo l'eliminazione");

        // Cleanup finale
        postDAO.delete(p.getId());
        utenteDAO.delete(autore.getUsername());
    }
}
