package it.univaq.brewhub.UI.components;

import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.scene.layout.StackPane;

/**
 * A custom component that combines a PasswordField with a toggle button to
 * show/hide the password.
 */
public class PasswordFieldWithToggler extends StackPane {

    private final PasswordField passwordField;
    private final TextField textField;
    private final Button toggleButton;
    private boolean isPasswordVisible = false;

    public PasswordFieldWithToggler(String promptText) {
        this.getStyleClass().add("password-toggler-container");
        this.setAlignment(Pos.CENTER_RIGHT);

        // Initialize fields
        passwordField = new PasswordField();
        passwordField.setPromptText(promptText);
        passwordField.getStyleClass().add("password-field");
        // Ensure the password field grows to fill the stack pane
        passwordField.setMaxWidth(Double.MAX_VALUE);

        textField = new TextField();
        textField.setPromptText(promptText);
        textField.getStyleClass().add("text-field");
        textField.setManaged(false);
        textField.setVisible(false);
        // Ensure the text field grows similarly
        textField.setMaxWidth(Double.MAX_VALUE);

        // Synchronize text
        textField.textProperty().bindBidirectional(passwordField.textProperty());

        // Create toggle button
        toggleButton = new Button("👁"); // Eye icon
        toggleButton.getStyleClass().add("password-toggle-btn");
        toggleButton.setFocusTraversable(false);
        toggleButton.setMaxWidth(30);
        toggleButton.setMaxHeight(30);

        // Positioning the button inside the field area
        // We use StackPane alignment. The fields fill the pane. The button is aligned
        // right.
        StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggleButton, new javafx.geometry.Insets(0, 10, 0, 0)); // Padding from right

        toggleButton.setOnAction(e -> toggleVisibility());

        this.getChildren().addAll(passwordField, textField, toggleButton);
    }

    private void toggleVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            textField.setManaged(true);
            textField.setVisible(true);
            passwordField.setManaged(false);
            passwordField.setVisible(false);
            toggleButton.setText("🔒"); // Lock/Hide icon
        } else {
            textField.setManaged(false);
            textField.setVisible(false);
            passwordField.setManaged(true);
            passwordField.setVisible(true);
            toggleButton.setText("👁"); // Show icon
        }
    }

    public String getText() {
        return passwordField.getText();
    }

    public void setText(String text) {
        passwordField.setText(text);
    }

    public StringProperty textProperty() {
        return passwordField.textProperty();
    }

    public StringProperty promptTextProperty() {
        return passwordField.promptTextProperty();
    }

    /*
     * Expose style class list of the inner fields to allow external styling if
     * needed.
     * Note: Generally better to style the container or use specific CSS selectors.
     */
}
