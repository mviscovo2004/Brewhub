package it.univaq.brewhub.view.utils;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Classe di utilità per componenti UI comuni e operazioni ricorrenti nella
 * vista.
 */
public class UiUtils {

    /**
     * Crea un nodo UI (VBox) che rappresenta uno stato vuoto o un placeholder.
     *
     * @param text Il messaggio da visualizzare.
     * @return VBox configurato e centrato con il messaggio.
     */
    public static VBox createEmptyState(String text) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 14px; -fx-font-style: italic;");
        box.getChildren().add(lbl);
        return box;
    }

    /**
     * Apre un FileChooser per selezionare un'immagine.
     *
     * @param owner Lo stage proprietario.
     * @return Il file selezionato, oppure null se annullato.
     */
    public static File chooseImage(Stage owner) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleziona Immagine");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        return fc.showOpenDialog(owner);
    }
}
