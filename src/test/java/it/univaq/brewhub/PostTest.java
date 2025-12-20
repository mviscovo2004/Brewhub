package it.univaq.brewhub;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import it.univaq.brewhub.Post.TipoPost;

public class PostTest {

    public void testCostruttoreTesto() {
        String titolo = "Test post";
        String contenuto = "Contenuto test post";
        Utente u = new Utente();
        TipoPost tipo = TipoPost.TESTO;
        String media = null;

        Post p = new Post(titolo, contenuto, u, tipo, media);

        assertEquals(titolo, p.getTitolo(), "Lo titolo del post non corrisponde");
        assertEquals(contenuto, p.getContenuto(), "Lo contenuto del post non corrisponde");
        assertEquals(u, p.getAutore(), "L'autore del post non corrisponde");
        assertEquals(tipo, p.getTipo(), "Il tipo del post non corrisponde");
        assertNull(p.getMedia(), "La media del post deve essere null");
    }

    @Test
    public void testCostruttoreFoto() {
        String titolo = "Test post";
        String contenuto = "Contenuto test post";
        Utente u = new Utente();
        TipoPost tipo = TipoPost.FOTO;
        String foto = "media/foto.jpg";

        Post p = new Post(titolo, contenuto, u, tipo, foto);

        assertEquals(titolo, p.getTitolo(), "Lo titolo del post non corrisponde");
        assertEquals(contenuto, p.getContenuto(), "Lo contenuto del post non corrisponde");
        assertEquals(u, p.getAutore(), "L'autore del post non corrisponde");
        assertEquals(tipo, p.getTipo(), "Il tipo del post non corrisponde");
        assertEquals(foto, p.getMedia(), "La foto del post non corrisponde");

    }

    public void testCostruttoreVideo() {
        String titolo = "Test post";
        String contenuto = "Contenuto test post";
        Utente u = new Utente();
        TipoPost tipo = TipoPost.VIDEO;
        String video = "media/video.mp4";

        Post p = new Post(titolo, contenuto, u, tipo, video);

        assertEquals(titolo, p.getTitolo(), "Lo titolo del post non corrisponde");
        assertEquals(contenuto, p.getContenuto(), "Lo contenuto del post non corrisponde");
        assertEquals(u, p.getAutore(), "L'autore del post non corrisponde");
        assertEquals(tipo, p.getTipo(), "Il tipo del post non corrisponde");
        assertEquals(video, p.getMedia(), "Il video del post non corrisponde");
    }

    public void testSetterGetter() {
        Post p = new Post();

        p.setTitolo("Test post");
        assertEquals("Test post", p.getTitolo());

        p.setContenuto("Contenuto test post");
        assertEquals("Contenuto test post", p.getContenuto());

        p.setAutore(new Utente());
        assertEquals(new Utente(), p.getAutore());

        p.setTipo(TipoPost.TESTO);
        assertEquals(TipoPost.TESTO, p.getTipo());

        p.setMedia("media/foto.jpg");
        assertEquals("media/foto.jpg", p.getMedia());

        p.setMiPiace(new ArrayList<Utente>());
        assertEquals(new ArrayList<Utente>(), p.getMiPiace());

        p.setCommenti(new ArrayList<Commento>());
        assertEquals(new ArrayList<Commento>(), p.getCommenti());

        p.setDataCreazione(LocalDateTime.now());
        assertEquals(LocalDateTime.now(), p.getDataCreazione());

        p.setMiPiaceSingolo(0, new Utente());
        assertEquals(new Utente(), p.getMiPiaceSingolo(0));

        p.setCommentoSingolo(0, new Commento());
        assertEquals(new Commento(), p.getCommentoSingolo(0));
    }

    @Test
    public void testMetodiDB() throws SQLException {
        Post p = new Post();
        p.salvaPost();

        List<Post> post = Post.caricaTuttiPost();
        assertEquals(1, post.size());

        p.eliminaPost();

        post = Post.caricaTuttiPost();
        assertEquals(0, post.size());

    }

    @BeforeAll
    public void init() {

    }

}