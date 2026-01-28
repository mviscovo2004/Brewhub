package it.univaq.brewhub.utility;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility per eseguire operazioni asincrone in JavaFX.
 * <p>
 * Fornisce metodi helper per eseguire task in background e aggiornare
 * l'UI nel thread JavaFX Application Thread.
 * </p>
 */
public class AsyncTaskHelper {

    /**
     * Esegue un'operazione in background e gestisce il risultato nell'UI thread.
     * 
     * @param <T>            Tipo del risultato
     * @param backgroundTask Operazione da eseguire in background
     * @param onSuccess      Callback eseguito in caso di successo (UI thread)
     * @param onError        Callback eseguito in caso di errore (UI thread)
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
     * Esegue un'operazione in background senza risultato.
     * 
     * @param backgroundTask Operazione da eseguire in background
     * @param onSuccess      Callback eseguito in caso di successo (UI thread)
     * @param onError        Callback eseguito in caso di errore (UI thread)
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
     * Esegue un'operazione in background con solo gestione successo.
     * Gli errori vengono loggati ma non gestiti.
     * 
     * @param <T>            Tipo del risultato
     * @param backgroundTask Operazione da eseguire in background
     * @param onSuccess      Callback eseguito in caso di successo (UI thread)
     */
    public static <T> void runAsync(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess) {

        runAsync(
                backgroundTask,
                onSuccess,
                error -> Log.error("Errore in operazione asincrona", error));
    }
}
