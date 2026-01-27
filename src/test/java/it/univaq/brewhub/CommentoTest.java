package it.univaq.brewhub;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import it.univaq.brewhub.Post.TipoPost;

/**
 * Test unitari e di integrazione per la classe {@link Commento} e il suo DAO.
 */
public class CommentoTest extends BaseTest {

    /**
     * Verifica il costruttore e i metodi di accesso di Commento.
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
     * Verifica le operazioni CRUD sul database per i Commenti.
     * @throws SQLException Se si verifica un errore SQL.
     */
    @Test
    public void testMetodiDB() throws SQLException {
        Utente autore = new Utente("AutoreC", "CognomeC", "userCommento", "pw", Utente.TipoUtente.APPASSIONATO, null);
        try {
            utenteDAO.create(autore);
        } catch (SQLException e) {
        } 
        Post p = new Post();
        p.setAutore(autore);
        p.setTitolo("Post per Commenti");
        p.setContenuto("Contenuto");
        p.setTipo(TipoPost.TESTO);
        postDAO.create(p);
        assertTrue(p.getId() > 0, "Il post dovrebbe avere un ID dopo il salvataggio");
        Commento c = new Commento();
        c.setPost(p);
        c.setUtente(autore); 
        c.setContenuto("Primo Commento");
        c.setDataCreazione(LocalDateTime.now()); 
        commentoDAO.create(c);
        assertTrue(c.getId() > 0, "Il commento dovrebbe avere un ID dopo il salvataggio");
        List<Commento> commenti = commentoDAO.findByPost(p);
        assertFalse(commenti.isEmpty());
        boolean found = commenti.stream().anyMatch(comm -> comm.getContenuto().equals("Primo Commento"));
        assertTrue(found);
        commentoDAO.delete(c.getId());
        commenti = commentoDAO.findByPost(p);
        boolean foundAfterDelete = commenti.stream().anyMatch(comm -> comm.getId() == c.getId());
        assertFalse(foundAfterDelete, "Il commento non dovrebbe essere presente dopo l'eliminazione");
        postDAO.delete(p.getId());
        utenteDAO.delete(autore.getUsername());
    }
}