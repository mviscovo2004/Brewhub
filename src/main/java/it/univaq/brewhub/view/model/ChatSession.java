package it.univaq.brewhub.view.model;

/**
 * Rappresenta una sessione di chat nella vista (UI Model).
 * Può essere una chat singola (privata) o un gruppo.
 */
public class ChatSession {

    // Tipo di sessione: "SINGLE" o "GROUP"
    private final String type;
    // ID di riferimento: username dell'altro utente (se SINGLE) o ID del gruppo
    // (se GROUP)
    private final String identifier;
    // Etichetta visuale: Nome utente o Nome gruppo
    private String label;
    // Ultimo messaggio (anteprima)
    private String lastMessagePreview;

    /**
     * Costruttore privato. Usare i metodi statici factory.
     *
     * @param type       Tipo di chat.
     * @param identifier Identificativo.
     * @param label      Etichetta visuale.
     */
    private ChatSession(String type, String identifier, String label) {
        this.type = type;
        this.identifier = identifier;
        this.label = label;
        this.lastMessagePreview = "";
    }

    /**
     * Crea una sessione per una chat privata con un utente.
     *
     * @param otherUsername Username dell'interlocutore.
     * @return Nuova istanza di ChatSession.
     */
    public static ChatSession single(String otherUsername) {
        return new ChatSession("SINGLE", otherUsername, "@" + otherUsername);
    }

    /**
     * Crea una sessione per un gruppo.
     *
     * @param groupId   ID del gruppo.
     * @param groupName Nome del gruppo.
     * @return Nuova istanza di ChatSession.
     */
    public static ChatSession group(int groupId, String groupName) {
        return new ChatSession("GROUP", String.valueOf(groupId), groupName);
    }

    public String getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public boolean isGroup() {
        return "GROUP".equals(type);
    }

    @Override
    public String toString() {
        return label;
    }

    // Helper per ottenere l'ID gruppo come int se è un gruppo
    public int getGroupId() {
        if (isGroup()) {
            try {
                return Integer.parseInt(identifier);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}
