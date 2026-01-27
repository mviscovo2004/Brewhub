package it.univaq.brewhub;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import it.univaq.brewhub.utility.Log;

/**
 * Gestore per i file multimediali (immagini, video) dell'applicazione.
 * <p>Si occupa di salvare, recuperare e gestire i percorsi dei file nella cartella locale 'media'.</p>
 */
public class MediaManager {

    private static final String MEDIA_FOLDER = "media";

    /**
     * Inizializza la cartella dei media se non esiste.
     */
    public static void initMediaFolder() {
        try {
            // Crea la directory 'media' nella root del progetto se assente
            Path mediaPath = Paths.get(MEDIA_FOLDER);
            if (!Files.exists(mediaPath)) {
                Files.createDirectories(mediaPath);
            }
        } catch (IOException e) {
            System.err.println("Errore durante creazione cartella media: " + e.getMessage());
            Log.error("Errore durante la creazione cartella media", e);
        }
    }

    /**
     * Copia un file selezionato nella cartella gestita dall'applicazione.
     * Genera un nome file univoco (UUID) per evitare conflitti.
     * 
     * @param file Il file sorgente selezionato dall'utente.
     * @return Il percorso relativo (es. "/media/uuid.jpg") o null se errore.
     */
    public static String copyMediaToFolder(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            // Genera nome univoco preservando l'estensione
            String extension = getFileExtension(file.getName());
            String fileName = UUID.randomUUID().toString() + (extension.isEmpty() ? "" : "." + extension);
            
            Path sourcePath = file.toPath();
            Path destPath = Paths.get(MEDIA_FOLDER, fileName);
            
            // Esegue la copia fisica
            Files.copy(sourcePath, destPath);
            
            // Standardizza i percorsi con forward slash per compatibilità DB/UI su OS diversi
            String rel = "/" + MEDIA_FOLDER + "/" + fileName;
            return rel.replace('\\', '/');
        } catch (IOException e) {
            System.err.println("Errore durante la copia del file: " + e.getMessage());
            Log.error("Errore durante la copia del media", e);
            return null;
        }
    }

    private static String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return lastIndex > 0 ? fileName.substring(lastIndex + 1).toLowerCase() : "";
    }

    /**
     * Recupera l'oggetto File fisico dato un percorso relativo.
     * 
     * @param relativePath Il percorso salvato nel DB (es. "/media/abc.jpg").
     * @return L'oggetto {@link File} se esiste, altrimenti null.
     */
    public static File getMediaFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        // Rimuovi slash iniziale se presente per permettere il resolve corretto
        String rel = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        
        // Risolvi il path relativo rispetto alla working directory corrente
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path mediaPath = projectRoot.resolve(rel);
        
        File file = mediaPath.toFile();
        if (file.exists()) {
            return file;
        }
        return null;
    }

    /**
     * Calcola il percorso relativo di un file che si trova già nella cartella media.
     * Utile se si seleziona un file già presente.
     * 
     * @param file Il file fisico.
     * @return Il percorso relativo o null se il file è esterno.
     */
    public static String getRelativePath(File file) {
        if (file == null)
            return null;
        try {
            Path projectRoot = Paths.get("").toAbsolutePath();
            Path mediaFolder = projectRoot.resolve(MEDIA_FOLDER).toAbsolutePath();
            Path filePath = file.toPath().toAbsolutePath();
            
            // Controlla se il file è effettivamente dentro la cartella media
            if (filePath.startsWith(mediaFolder)) {
                Path rel = mediaFolder.relativize(filePath);
                return "/" + MEDIA_FOLDER + "/" + rel.toString().replace('\\', '/');
            }
        } catch (Exception e) {
            Log.error("Errore calcolo path relativo", e);
        }
        return null;
    }
}