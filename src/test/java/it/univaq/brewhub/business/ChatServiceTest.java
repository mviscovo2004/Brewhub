package it.univaq.brewhub.business;

import it.univaq.brewhub.BaseTest;
import it.univaq.brewhub.model.Messaggio;
import it.univaq.brewhub.model.Utente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatServiceTest extends BaseTest {

    private ChatService chatService;

    @BeforeEach
    public void setUp() throws Exception {
        chatService = ChatService.getInstance();
        createTestUser("userA");
        createTestUser("userB");
    }

    private void createTestUser(String username) throws Exception {
        try {
            Utente u = new Utente("Test", "User", username, "pass", Utente.TipoUtente.APPASSIONATO, null);
            utenteDAO.create(u);
        } catch (Exception e) {
            // Ignore
        }
    }

    @Test
    public void testSendMessage() throws BusinessException {
        // Messaggio(String sender, String receiver, String contenuto, String timestamp)
        Messaggio m = new Messaggio("userA", "userB", "Hello World", "2023-10-27");
        chatService.sendMessage(m);

        List<Messaggio> chat = chatService.getPrivateMessages("userA", "userB");
        assertEquals(1, chat.size());
        assertEquals("Hello World", chat.get(0).getContenuto());
    }

    @Test
    public void testMarkAsRead() throws BusinessException {
        Messaggio m = new Messaggio("userA", "userB", "Unread", "2023-10-27");
        chatService.sendMessage(m);

        List<Messaggio> chat = chatService.getPrivateMessages("userA", "userB");
        assertFalse(chat.isEmpty());
        int msgId = chat.get(0).getId();

        chatService.markAsRead(msgId);
        // We assume it works if no exception. Verification would involve re-fetching.
        List<Messaggio> updated = chatService.getPrivateMessages("userA", "userB");
        assertTrue(updated.get(0).isLetto());
    }

    @Test
    public void testActiveConversations() throws BusinessException {
        Messaggio m = new Messaggio("userA", "userB", "Hi", "2023-10-27");
        chatService.sendMessage(m);

        List<String> active = chatService.getActiveConversations("userA");
        assertTrue(active.contains("userB"));
    }
}
