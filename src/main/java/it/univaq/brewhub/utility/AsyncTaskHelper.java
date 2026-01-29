package it.univaq.brewhub.utility;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Classe di utilità per la gestione della concorrenza in JavaFX.
 * Permette di eseguire operazioni lunghe in un thread di background per non
 * bloccare
 * l'interfaccia utente (UI Thread), e di gestire i risultati o gli errori
 * nuovamente nel thread principale dell'UI.
 */
public class AsyncTaskHelper {

    /**
     * Esegue un task asincrono che restituisce un risultato.
     *
     * @param <T>            Il tipo del risultato restituito dal task.
     * @param backgroundTask La logica da eseguire in un thread separato
     *                       (background).
     * @param onSuccess      Il callback da eseguire nel thread UI se l'operazione
     *                       ha successo.
     * @param onError        Il callback da eseguire nel thread UI se si verifica
     *                       un'eccezione.
     */
    public static <T> void runAsync(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError) {

        Task<T> task = new Task<T>() {
            @Override
            protected T call() throws Exception {
                return backgroundTask.get();
            }
        };

        task.setOnSucceeded(event -> {
            try {
                T result = task.getValue();
                Platform.runLater(() -> onSuccess.accept(result));
            } catch (Exception e) {
                Platform.runLater(() -> onError.accept(e));
            }
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> onError.accept(exception));
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Esegue un task asincrono che non restituisce alcun risultato (void).
     *
     * @param backgroundTask La logica da eseguire in background.
     * @param onSuccess      Il callback da eseguire nel thread UI al termine
     *                       dell'operazione.
     * @param onError        Il callback da eseguire nel thread UI in caso di
     *                       errore.
     */
    public static void runAsync(
            Runnable backgroundTask,
            Runnable onSuccess,
            Consumer<Throwable> onError) {

        runAsync(
                () -> {
                    backgroundTask.run();
                    return null;
                },
                result -> onSuccess.run(),
                onError);
    }

    /**
     * Esegue un task asincrono gestendo solo il caso di successo.
     * Gli errori vengono loggati ma non gestiti esplicitamente dal chiamante.
     *
     * @param <T>            Il tipo del risultato.
     * @param backgroundTask La logica da eseguire in background.
     * @param onSuccess      Il callback da eseguire nel thread UI in caso di
     *                       successo.
     */
    public static <T> void runAsync(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess) {

        runAsync(
                backgroundTask,
                onSuccess,
                error -> Log.error("Errore generico in operazione asincrona", error));
    }
}
