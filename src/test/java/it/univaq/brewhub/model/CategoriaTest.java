package it.univaq.brewhub.model;

import it.univaq.brewhub.BaseTest;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Test unitari e di integrazione per la classe {@link Categoria} e il suo DAO.
 */
public class CategoriaTest extends BaseTest {

    /**
     * Verifica i costruttori, getter e setter del modello Categoria.
     */
    @Test
    public void testCostruttoriGetterSetter() {
        Categoria c = new Categoria();
        c.setId(1);
        c.setNome("Metodi");
        c.setIcona("icon.png");
        assertEquals(1, c.getId());
        assertEquals("Metodi", c.getNome());
        assertEquals("icon.png", c.getIcona());
        assertEquals("Metodi", c.toString());

        Categoria c2 = new Categoria(2, "Strumenti");
        assertEquals(2, c2.getId());
        assertEquals("Strumenti", c2.getNome());

        Categoria c3 = new Categoria(3, "Chicchi", "bean.png");
        assertEquals(3, c3.getId());
        assertEquals("Chicchi", c3.getNome());
        assertEquals("bean.png", c3.getIcona());

        Categoria c4 = new Categoria("Ricette");
        assertEquals("Ricette", c4.getNome());

        Categoria c5 = new Categoria("Bar", "bar.png");
        assertEquals("Bar", c5.getNome());
        assertEquals("bar.png", c5.getIcona());
    }

    /**
     * Verifica le operazioni CRUD sul database per le Categorie.
     * 
     * @throws SQLException se si verifica un errore SQL.
     */
    @Test
    public void testMetodiDB() throws SQLException {
        String catName = "Test Cat " + System.currentTimeMillis();
        Categoria c = new Categoria(catName, "test.png");
        categoriaDAO.create(c);

        Categoria retrieved = categoriaDAO.findByName(catName);
        assertNotNull(retrieved, "Dovrebbe trovare la categoria appena creata");
        assertEquals("test.png", retrieved.getIcona());

        int id = retrieved.getId();
        assertTrue(id > 0, "L'ID dovrebbe essere > 0");

        Categoria byId = categoriaDAO.findById(id);
        assertNotNull(byId);
        assertEquals(catName, byId.getNome());

        String newName = catName + " Upd";
        retrieved.setNome(newName);
        categoriaDAO.update(retrieved);

        Categoria updated = categoriaDAO.findById(id);
        assertEquals(newName, updated.getNome());

        List<Categoria> all = categoriaDAO.findAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(cat -> cat.getNome().equals(newName)));

        categoriaDAO.delete(id);
        assertNull(categoriaDAO.findById(id), "Dovrebbe restituire null dopo la cancellazione");
    }
}
