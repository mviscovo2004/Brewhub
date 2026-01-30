package it.univaq.brewhub.utility;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test unitari per la classe {@link MediaManager}.
 * Verifica l'inizializzazione della cartella media, la copia di file
 * multimediali e il recupero dei file.
 */
public class MediaManagerTest {
    private static final String MEDIA_FOLDER_NAME = "media";

    /**
     * Verifica che la cartella dei media venga creata correttamente.
     */
    @Test
    public void testInitMediaFolder() {
        MediaManager.initMediaFolder();
        File mediaDir = new File(MEDIA_FOLDER_NAME);
        assertTrue(mediaDir.exists() && mediaDir.isDirectory());
    }

    /**
     * Verifica la copia di un file multimediale nella cartella dedicata.
     * 
     * @param tempDir directory temporanea fornita da JUnit.
     * @throws IOException se si verifica un errore durante le operazioni I/O.
     */
    @Test
    public void testCopyMediaToFolder(@TempDir Path tempDir) throws IOException {
        Path sourcePath = tempDir.resolve("test_image.png");
        Files.createFile(sourcePath);
        File sourceFile = sourcePath.toFile();
        String resultPath = MediaManager.copyMediaToFolder(sourceFile);
        assertNotNull(resultPath);
        assertTrue(resultPath.startsWith("/" + MEDIA_FOLDER_NAME + "/"));
        assertTrue(resultPath.endsWith(".png"));
        String relPathClean = resultPath.startsWith("/") ? resultPath.substring(1) : resultPath;
        File destFile = new File(relPathClean);
        assertTrue(destFile.exists());
        destFile.delete();
    }

    /**
     * Verifica il recupero di un file multimediale esistente.
     */
    @Test
    public void testGetMediaFile() {
        String filename = "test_retrieve_" + System.currentTimeMillis() + ".txt";
        File mediaDir = new File(MEDIA_FOLDER_NAME);
        if (!mediaDir.exists())
            mediaDir.mkdir();
        File testFile = new File(mediaDir, filename);
        try {
            if (testFile.createNewFile()) {
                String relativePath = "/" + MEDIA_FOLDER_NAME + "/" + filename;
                File retrieved = MediaManager.getMediaFile(relativePath);
                assertNotNull(retrieved);
                assertTrue(retrieved.exists());
                assertEquals(testFile.getAbsolutePath(), retrieved.getAbsolutePath());
                testFile.delete();
            }
        } catch (IOException e) {
            fail("Errore creazione file test: " + e.getMessage());
        }
    }
}
