package it.univaq.brewhub;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;
import it.univaq.brewhub.dao.impl.CategoriaDAOImpl;

public class CategoriaTest {

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

    @Test
    public void testMetodiDB() throws SQLException {
        it.univaq.brewhub.DatabaseManager.init();
        CategoriaDAOImpl dao = new CategoriaDAOImpl();

        String catName = "Test Cat " + System.currentTimeMillis();

        // Create
        Categoria c = new Categoria(catName, "test.png");
        dao.create(c);

        // Verify creation (findByName)
        Categoria retrieved = dao.findByName(catName);
        assertNotNull(retrieved, "Dovrebbe trovare la categoria appena creata");
        assertEquals("test.png", retrieved.getIcona());
        int id = retrieved.getId();
        assertTrue(id > 0, "L'ID dovrebbe essere > 0");

        // FindById
        Categoria byId = dao.findById(id);
        assertNotNull(byId);
        assertEquals(catName, byId.getNome());

        // Update
        String newName = catName + " Upd";
        retrieved.setNome(newName);
        dao.update(retrieved);
        Categoria updated = dao.findById(id);
        assertEquals(newName, updated.getNome());

        // FindAll
        List<Categoria> all = dao.findAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(cat -> cat.getNome().equals(newName)));

        // Delete
        dao.delete(id);
        assertNull(dao.findById(id), "Dovrebbe restituire null dopo la cancellazione");
    }
}
