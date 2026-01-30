package it.univaq.brewhub.view.components;

import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

/**
 * Componente UI personalizzato che combina un PasswordField con un TextField
 * e un pulsante toggle per mostrare/nascondere la password in chiaro.
 */
public class PasswordFieldWithToggler extends StackPane {
    private final PasswordField passwordField;
    private final TextField textField;
    private final Button toggleButton;
    private boolean isPasswordVisible = false;

    /**
     * Costruisce il componente con il testo placeholder specificato.
     *
     * @param promptText Il testo da mostrare quando il campo è vuoto.
     */
    public PasswordFieldWithToggler(String promptText) {
        this.getStyleClass().add("password-toggler-container");
        this.setAlignment(Pos.CENTER_RIGHT);
        passwordField = new PasswordField();
        passwordField.setPromptText(promptText);
        passwordField.getStyleClass().add("password-field");
        passwordField.setMaxWidth(Double.MAX_VALUE);
        textField = new TextField();
        textField.setPromptText(promptText);
        textField.getStyleClass().add("text-field");
        textField.getStyleClass().add("plain-text-mode");
        textField.setManaged(false);
        textField.setVisible(false);
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.textProperty().bindBidirectional(passwordField.textProperty());
        toggleButton = new Button("👁");
        toggleButton.getStyleClass().add("password-toggle-btn");
        toggleButton.setFocusTraversable(false);
        toggleButton.setMaxWidth(30);
        toggleButton.setMaxHeight(30);
        StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggleButton, new javafx.geometry.Insets(0, 10, 0, 0));
        toggleButton.setOnAction(e -> toggleVisibility());
        this.getChildren().addAll(passwordField, textField, toggleButton);
    }

    /**
     * Alterna la visibilità della password tra caratteri nascosti e testo in
     * chiaro.
     */
    private void toggleVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            textField.setManaged(true);
            textField.setVisible(true);
            passwordField.setManaged(false);
            passwordField.setVisible(false);
            toggleButton.setText("🔒");
        } else {
            textField.setManaged(false);
            textField.setVisible(false);
            passwordField.setManaged(true);
            passwordField.setVisible(true);
            toggleButton.setText("👁");
        }
    }

    /**
     * Restituisce il testo contenuto nel campo password.
     *
     * @return La password inserita.
     */
    public String getText() {
        return passwordField.getText();
    }

    /**
     * Imposta il testo del campo password.
     *
     * @param text La password da impostare.
     */
    public void setText(String text) {
        passwordField.setText(text);
    }

    /**
     * Restituisce la proprietà osservabile del testo.
     *
     * @return La StringProperty del testo.
     */
    public StringProperty textProperty() {
        return passwordField.textProperty();
    }

    /**
     * Restituisce la proprietà osservabile del prompt text.
     *
     * @return La StringProperty del prompt text.
     */
    public StringProperty promptTextProperty() {
        return passwordField.promptTextProperty();
    }
}
