package it.univaq.brewhub;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.UUID;
import it.univaq.brewhub.utility.Log;

/**
 * Gestione dei file media (immagini, video) per i post.
 * Si occupa di salvare, copiare e recuperare i file dalla directory dedicata
 * "media".
 */
public class MediaManager {

    // Cartella dedicata ai media
    /** Nome della cartella dedicata ai media. */
    private static final String MEDIA_FOLDER = "media";

    /**
     * Inizializza la cartella media all'avvio dell'applicazione.
     * Se la cartella non esiste, viene creata.
     */
    public static void initMediaFolder() {
        try {
            Path mediaPath = Paths.get(MEDIA_FOLDER);

            // Crea la cartella media se non esiste
            if (!Files.exists(mediaPath)) {
                Files.createDirectories(mediaPath);
            }
        } catch (IOException e) {
            System.err.println("Errore durante creazione cartella media: " + e.getMessage());
            Log.error("Errore durante la copia del media", e);
        }
    }

    /**
     * Copia un file media selezionato dall'utente nella cartella interna
     * dell'applicazione.
     * Genera un nome univoco per evitare conflitti.
     *
     * @param file Il file sorgente selezionato.
     * @return String Il percorso relativo del file copiato (es. "/media/uuid.jpg"),
     *         o null in caso di errore.
     */
    public static String copyMediaToFolder(File file) {
        // Controllo file valido
        if (file == null || !file.exists()) {
            return null;
        }

        try {
            // Crea il nome univoco del file
            String extension = getFileExtension(file.getName());
            String fileName = UUID.randomUUID().toString() + (extension.isEmpty() ? "" : "." + extension);
            Path sourcePath = file.toPath();
            Path destPath = Paths.get(MEDIA_FOLDER, fileName);

            // Copia il file
            Files.copy(sourcePath, destPath);

            // Ritorna il percorso relativo usando slash e senza prefisso di disco per
            // portabilità
            String rel = "/" + MEDIA_FOLDER + "/" + fileName;
            return rel.replace('\\', '/');
        } catch (IOException e) {
            System.err.println("Errore durante la copia del file: " + e.getMessage());
            Log.error("Errore durante eliminazione file", e);
            return null;
        }
    }

    /**
     * Estrae l'estensione da un nome file.
     *
     * @param fileName Il nome del file.
     * @return L'estensione (senza punto) o stringa vuota se non presente.
     */
    private static String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return lastIndex > 0 ? fileName.substring(lastIndex + 1).toLowerCase() : "";
    }

    /**
     * Recupera un oggetto File a partire dal percorso relativo memorizzato nel DB.
     *
     * @param relativePath Il percorso relativo (es. "media/foto.jpg" o
     *                     "/media/foto.jpg").
     * @return File L'oggetto File corrispondente, o null se non trovato o path
     *         invalido.
     */
    public static File getMediaFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }

        // Accetta sia "media/xxx" che "/media/xxx" rimuovendo lo slash iniziale se
        // presente
        String rel = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path mediaPath = projectRoot.resolve(rel);
        File file = mediaPath.toFile();

        // Controlla se il file esiste fisicamente
        if (file.exists()) {
            return file;
        }
        return null;
    }

    /**
     * Calcola il percorso relativo di un file media rispetto alla root del
     * progetto.
     * Utile per verificare se un file è già nella cartella gestita.
     *
     * @param file Il file di cui calcolare il path.
     * @return String Il percorso relativo o null se il file non è nella cartella
     *         media.
     */
    public static String getRelativePath(File file) {
        if (file == null)
            return null;

        try {
            Path projectRoot = Paths.get("").toAbsolutePath();
            Path mediaFolder = projectRoot.resolve(MEDIA_FOLDER).toAbsolutePath();
            Path filePath = file.toPath().toAbsolutePath();

            // Verifica che il file sia effettivamente dentro la cartella media
            if (filePath.startsWith(mediaFolder)) {
                // Calcola il percorso relativo
                Path rel = mediaFolder.relativize(filePath);
                return "/" + MEDIA_FOLDER + "/" + rel.toString().replace('\\', '/');
            }
        } catch (Exception e) {
            Log.error("Errore caricamento immagine default", e);
        }
        return null;
    }
}
