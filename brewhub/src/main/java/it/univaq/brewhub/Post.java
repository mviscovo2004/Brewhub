package it.univaq.brewhub;

import java.io.File;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import it.univaq.brewhub.Post;

@Document(collection = "post",schemaVersion = "1.0")
public class Post {

	@Id
    private Utente autore;
    
    @Id
    private String titolo;
    
    private String contenuto;
    private TipoPost tipo;
    private LocalDateTime dataCreazione;
    private File media=null;
    private List<Utente> miPiace=new ArrayList<Utente>();

    private List<Commento> commenti = new ArrayList<Commento>();

    public enum TipoPost {
        TESTO("Testo"),
        FOTO("Foto"),
        VIDEO("Video");

        private final String label;

        TipoPost(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    // Costruttore principale
    public Post(String titolo,String contenuto, Utente autore, TipoPost tipo, File media) {
    	this.titolo=titolo;
        this.contenuto = contenuto;
        this.autore = autore;
        this.tipo = tipo;
        this.media=media;
        this.dataCreazione = LocalDateTime.now();
    }
   


    // Costruttore vuoto per JSON
    public Post() {}

    // GETTER
    public Utente getAutore() {
        return autore;
    }
    
    public String getTitolo() {
		return titolo;
	}

    public String getContenuto() {
        return contenuto;
    }

    public TipoPost getTipo() {
        return tipo;
    }
    
    public File getMedia() {
    	return media;
    }

    public List<Utente> getMiPiace() {
		return miPiace;
	}
    
    @JsonIgnore
    public Utente getMiPiaceSingolo(int i) {
    	return miPiace.get(i);
    }
    
    
    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    public List<Commento> getCommenti() {
        return commenti;
    }

    @JsonIgnore
    public Commento getCommentoSingolo(int i) {
    	return commenti.get(i);
    }
    
    // --- SETTER ---
    public void setAutore(Utente autore) {
        this.autore = autore;
    }

    public void setTitolo(String titolo) {
		this.titolo = titolo;
	}
    
    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

    public void setTipo(TipoPost tipo) {
        this.tipo = tipo;
    }

    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }
    
    public void setMedia(File media) {
    	this.media = media;
    }
    
    public void setMiPiace(List<Utente> miPiace) {
		this.miPiace = miPiace;
	}
    
    @JsonIgnore
    public void setMiPiaceSingolo(int i,Utente utente) {
    	miPiace.set(i, utente);
    }
    
   public void setCommenti(List<Commento> commenti) {
	   this.commenti = commenti;
   }
   
   @JsonIgnore
   public void setCommentoSingolo(int i, Commento commento) {
	   commenti.set(i, commento);
   }
    
   
   
}
