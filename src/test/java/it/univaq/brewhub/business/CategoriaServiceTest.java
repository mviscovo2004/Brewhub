package it.univaq.brewhub.business;

import it.univaq.brewhub.BaseTest;
import it.univaq.brewhub.model.Categoria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CategoriaServiceTest extends BaseTest {

    private CategoriaService categoriaService;

    @BeforeEach
    public void setUp() throws Exception {
        categoriaService = CategoriaService.getInstance();
    }

    @Test
    public void testCreateAndGetCategories() throws BusinessException {
        Categoria c = new Categoria("TestCategory", "Icon");
        categoriaService.createCategory(c);

        List<Categoria> list = categoriaService.getAllCategories();
        boolean found = list.stream().anyMatch(cat -> "TestCategory".equals(cat.getNome()));
        assertTrue(found);
    }

    @Test
    public void testUpdateCategory() throws BusinessException {
        Categoria c = new Categoria("ToUpdate", "Icon");
        categoriaService.createCategory(c);

        List<Categoria> list = categoriaService.getAllCategories();
        Categoria created = list.stream().filter(cat -> "ToUpdate".equals(cat.getNome())).findFirst().orElse(null);
        assertNotNull(created);

        created.setNome("UpdatedName");
        categoriaService.updateCategory(created);

        Categoria updated = categoriaService.getCategoryById(created.getId());
        assertEquals("UpdatedName", updated.getNome());
    }

    @Test
    public void testDeleteCategory() throws BusinessException {
        Categoria c = new Categoria("ToDelete", "Icon");
        categoriaService.createCategory(c);

        List<Categoria> list = categoriaService.getAllCategories();
        Categoria created = list.stream().filter(cat -> "ToDelete".equals(cat.getNome())).findFirst().orElse(null);
        assertNotNull(created);

        categoriaService.deleteCategory(created.getId());

        Categoria deleted = categoriaService.getCategoryById(created.getId());
        assertNull(deleted);
    }

    @Test
    public void testCreateDuplicateCategory() throws BusinessException {
        Categoria c1 = new Categoria("UniqueCat", "Icon");
        categoriaService.createCategory(c1);

        Categoria c2 = new Categoria("UniqueCat", "Icon2");
        assertThrows(BusinessException.class, () -> {
            categoriaService.createCategory(c2);
        });
    }

    @Test
    public void testInvalidCategory() {
        assertThrows(BusinessException.class, () -> categoriaService.createCategory(null));
        assertThrows(BusinessException.class, () -> categoriaService.createCategory(new Categoria(null, "icon")));
    }
}
