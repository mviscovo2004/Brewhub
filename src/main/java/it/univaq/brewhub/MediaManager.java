package it.univaq.brewhub;

// Importazioni librerie Java
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

// Gestione dei file media (immagini, video) per i post
public class MediaManager {

    // Cartella dedicata ai media
    private static final String MEDIA_FOLDER = "media";

    // Inizializza la cartella media all'avvio dell'applicazione
    public static void initMediaFolder() {

        // Crea la cartella se non esiste
        try {
            Path mediaPath = Paths.get(MEDIA_FOLDER);

            // Crea la cartella media se non esiste
            if (!Files.exists(mediaPath)) {

                Files.createDirectories(mediaPath);
            }
        } catch (IOException e) {
            // Gestione errore creazione cartella
            e.printStackTrace();
        }
    }

    // Copia un file media nella cartella dedicata e ritorna il percorso relativo
    public static String copyMediaToFolder(File file) {

        // Controllo file valido
        if (file == null || !file.exists()) {
            return null;
        }

        // Copia il file nella cartella media
        try {

            // Crea il nome univoco del file
            String extension = getFileExtension(file.getName());
            String fileName = UUID.randomUUID().toString() + (extension.isEmpty() ? "" : "." + extension);
            Path sourcePath = file.toPath();
            Path destPath = Paths.get(MEDIA_FOLDER, fileName);

            // Copia il file
            Files.copy(sourcePath, destPath);

            // Ritorna il percorso relativo usando slash e senza prefisso di disco
            String rel = "/" + MEDIA_FOLDER + "/" + fileName;
            return rel.replace('\\', '/');
        } catch (IOException e) {
           
            // Gestione errore copia file
            e.printStackTrace();
            return null;
        }
    }

    // Ottiene l'estensione del file
    private static String getFileExtension(String fileName) {
        
        // Trova l'ultimo punto nel nome del file
        int lastIndex = fileName.lastIndexOf('.');
        return lastIndex > 0 ? fileName.substring(lastIndex + 1).toLowerCase() : "";
    }

    // Ritorna il file media dato il percorso relativo
    public static File getMediaFile(String relativePath) {

        // Controllo percorso valido
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }

        // Accetta sia "media/xxx" che "/media/xxx"
        String rel = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path mediaPath = projectRoot.resolve(rel);
        File file = mediaPath.toFile();

        // Controlla se il file esiste
        if (file.exists()) {
            return file;
        }
        return null;
    }

    // Ritorna il percorso relativo di un file media
    public static String getRelativePath(File file) {

        // Controllo file valido
        if (file == null)
            return null;

        // Calcola il percorso relativo rispetto alla cartella media
        try {
            Path projectRoot = Paths.get("").toAbsolutePath();
            Path mediaFolder = projectRoot.resolve(MEDIA_FOLDER).toAbsolutePath();
            Path filePath = file.toPath().toAbsolutePath();

            // Verifica che il file sia nella cartella media
            if (filePath.startsWith(mediaFolder)) {

                // Calcola il percorso relativo
                Path rel = mediaFolder.relativize(filePath);
                return "/" + MEDIA_FOLDER + "/" + rel.toString().replace('\\', '/');
            }
        } catch (Exception e) {
            
            // Gestione errore calcolo percorso
            e.printStackTrace();
        }
        return null;
    }
}
