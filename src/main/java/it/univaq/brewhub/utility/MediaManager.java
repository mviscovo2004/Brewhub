package it.univaq.brewhub.utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Manager per la gestione dei file multimediali (immagini).
 * Si occupa del salvataggio, recupero e organizzazione dei file nella cartella
 * locale dedicata ('media').
 * I file vengono rinominati con UUID per evitare collisioni.
 */
public class MediaManager {

    private static final String MEDIA_FOLDER = "media";

    /**
     * Inizializza la cartella 'media' nella root del progetto.
     * Se la cartella non esiste, viene creata.
     */
    public static void initMediaFolder() {
        try {
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
     * Copia un file selezionato dall'utente nella cartella gestita
     * dall'applicazione.
     * Il file viene rinominato con un UUID univoco preservando l'estensione
     * originale.
     *
     * @param file Il file sorgente.
     * @return Il percorso relativo (es. "/media/uuid.jpg") da salvare nel database,
     *         oppure null in caso di errore.
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

            // Esegue la copia
            Files.copy(sourcePath, destPath);

            // Restituisce il path relativo con formato standard (slash)
            String rel = "/" + MEDIA_FOLDER + "/" + fileName;
            return rel.replace('\\', '/');
        } catch (IOException e) {
            System.err.println("Errore durante la copia del file: " + e.getMessage());
            Log.error("Errore durante la copia del media", e);
            return null;
        }
    }

    /**
     * Estrae l'estensione da un nome file.
     *
     * @param fileName Il nome del file.
     * @return L'estensione (senza punto) in minuscolo, o stringa vuota se assente.
     */
    private static String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return lastIndex > 0 ? fileName.substring(lastIndex + 1).toLowerCase() : "";
    }

    /**
     * Risolve un percorso relativo (salvato nel DB) in un oggetto File fisico.
     *
     * @param relativePath Il percorso relativo (es. "/media/immagine.jpg").
     * @return L'oggetto File corrispondente, o null se il percorso è invalido o il
     *         file non esiste.
     */
    public static File getMediaFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        // Rimuove slash iniziale se presente
        String rel = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;

        // Risolve il path assoluto
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path mediaPath = projectRoot.resolve(rel);

        File file = mediaPath.toFile();
        if (file.exists()) {
            return file;
        }
        return null;
    }

    /**
     * Calcola il percorso relativo di un file che risiede già nella cartella media.
     * Utile quando l'utente seleziona un file che è già parte dell'archivio
     * dell'applicazione.
     *
     * @param file Il file fisico.
     * @return Il percorso relativo standardizzato, o null se il file è esterno alla
     *         cartella media.
     */
    public static String getRelativePath(File file) {
        if (file == null)
            return null;
        try {
            Path projectRoot = Paths.get("").toAbsolutePath();
            Path mediaFolder = projectRoot.resolve(MEDIA_FOLDER).toAbsolutePath();
            Path filePath = file.toPath().toAbsolutePath();

            // Verifica appartenenza alla cartella media
            if (filePath.startsWith(mediaFolder)) {
                Path rel = mediaFolder.relativize(filePath);
                return "/" + MEDIA_FOLDER + "/" + rel.toString().replace('\\', '/');
            }
        } catch (Exception e) {
            Log.error("Errore nel calcolo del path relativo", e);
        }
        return null;
    }
}
