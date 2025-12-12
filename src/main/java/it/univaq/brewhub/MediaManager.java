package it.univaq.brewhub;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class MediaManager {
    private static final String MEDIA_FOLDER = "media";

    /**
     * Crea la cartella media se non esiste
     */
    public static void initMediaFolder() {
        try {
            Path mediaPath = Paths.get(MEDIA_FOLDER);
            if (!Files.exists(mediaPath)) {
                Files.createDirectories(mediaPath);
                System.out.println("DEBUG - Cartella media creata");
            }
        } catch (IOException e) {
            System.out.println("DEBUG - Errore nella creazione della cartella media: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Copia un file nella cartella media e ritorna il percorso relativo
     * @param file File da copiare
     * @return Percorso relativo del file nella cartella media
     */
    public static String copyMediaToFolder(File file) {
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
            System.out.println("DEBUG - Media copiato: " + fileName);

                // Ritorna il percorso relativo usando slash e senza prefisso di disco
                String rel = "/" + MEDIA_FOLDER + "/" + fileName;
                return rel.replace('\\', '/');
        } catch (IOException e) {
            System.out.println("DEBUG - Errore nella copia del media: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Ottiene l'estensione del file
     */
    private static String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return lastIndex > 0 ? fileName.substring(lastIndex + 1).toLowerCase() : "";
    }

    /**
     * Ritorna il percorso assoluto di un media data la path relativa
     */
    public static File getMediaFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        // Accetta sia "media/xxx" che "/media/xxx"
        String rel = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path mediaPath = projectRoot.resolve(rel);
        File file = mediaPath.toFile();
        if (file.exists()) {
            return file;
        }
        return null;
    }

    /**
     * Ritorna il percorso relativo (con leading slash) se il file si trova nella cartella media
     */
    public static String getRelativePath(File file) {
        if (file == null) return null;
        try {
            Path projectRoot = Paths.get("").toAbsolutePath();
            Path mediaFolder = projectRoot.resolve(MEDIA_FOLDER).toAbsolutePath();
            Path filePath = file.toPath().toAbsolutePath();
            if (filePath.startsWith(mediaFolder)) {
                Path rel = mediaFolder.relativize(filePath);
                return "/" + MEDIA_FOLDER + "/" + rel.toString().replace('\\', '/');
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
