package it.univaq.brewhub.view.components;

import it.univaq.brewhub.view.model.ChatSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

/**
 * Cella personalizzata per la visualizzazione delle sessioni di chat in una
 * ListView.
 * Gestisce la visualizzazione grafica (avatar, nome) e le azioni contestuali
 * (rinomina, abbandona, elimina).
 */
public class ChatListCell extends ListCell<ChatSession> {

    /**
     * Interfaccia per gestire le azioni dell'utente sulla cella della chat.
     */
    public interface ChatActionListener {
        /**
         * Chiamato quando l'utente richiede di rinominare un gruppo.
         * 
         * @param session La sessione di chat interessata.
         */
        void onRenameGroup(ChatSession session);

        /**
         * Chiamato quando l'utente richiede di abbandonare un gruppo.
         * 
         * @param session La sessione di chat interessata.
         */
        void onLeaveGroup(ChatSession session);

        /**
         * Chiamato quando l'utente richiede di eliminare un gruppo.
         * 
         * @param session La sessione di chat interessata.
         */
        void onDeleteGroup(ChatSession session);

        /**
         * Chiamato quando l'utente richiede di eliminare una chat privata.
         * 
         * @param session La sessione di chat interessata.
         */
        void onDeletePrivateChat(ChatSession session);
    }

    private final ChatActionListener listener;

    /**
     * Costruisce una nuova cella per la lista delle chat.
     *
     * @param listener Il listener per le azioni contestuali.
     */
    public ChatListCell(ChatActionListener listener) {
        this.listener = listener;
        getStyleClass().add("chat-conversation-item");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
        if (getGraphic() != null) {
            updateStyle(selected);
        }
    }

    /**
     * Aggiorna lo stile grafico della cella in base allo stato di selezione.
     *
     * @param selected true se la cella è selezionata, false altrimenti.
     */
    private void updateStyle(boolean selected) {
        if (selected) {
            getGraphic().setStyle("-fx-background-color: #D7CCC8; -fx-background-radius: 10;");
        } else {
            getGraphic().setStyle("-fx-background-color: transparent; -fx-background-radius: 10;");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateItem(ChatSession item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setStyle("-fx-background-color: transparent;");
            setContextMenu(null);
        } else {
            HBox cell = new HBox(12);
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setPadding(new Insets(8));
            Circle avatar = new Circle(20);
            avatar.setFill(javafx.scene.paint.Color.web(item.isGroup() ? "#5D4037" : "#8D6E63"));
            String initialChar = item.getLabel().length() > 0 ? item.getLabel().substring(0, 1).toUpperCase()
                    : "?";
            Label initial = new Label(initialChar);
            initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            StackPane avatarStack = new StackPane(avatar, initial);
            String displayName = item.getLabel();
            Label name = new Label(displayName);
            name.setStyle("-fx-font-weight: bold; -fx-text-fill: #3E2723; -fx-font-size: 14px;");
            cell.getChildren().addAll(avatarStack, name);
            setGraphic(cell);
            setText(null);
            updateStyle(isSelected());

            ContextMenu cm = new ContextMenu();
            if (item.isGroup()) {
                MenuItem renameItem = new MenuItem("Rinomina Gruppo");
                renameItem.setOnAction(e -> listener.onRenameGroup(item));
                MenuItem leaveItem = new MenuItem("Abbandona Gruppo");
                leaveItem.setOnAction(e -> listener.onLeaveGroup(item));
                MenuItem deleteItem = new MenuItem("Elimina Gruppo");
                deleteItem.setOnAction(e -> listener.onDeleteGroup(item));
                cm.getItems().addAll(renameItem, leaveItem, deleteItem);
            } else {
                MenuItem deleteItem = new MenuItem("Elimina Conversazione");
                deleteItem.setOnAction(e -> listener.onDeletePrivateChat(item));
                cm.getItems().add(deleteItem);
            }
            setContextMenu(cm);
        }
    }
}
