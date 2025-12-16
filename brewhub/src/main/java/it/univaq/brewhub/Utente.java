package it.univaq.brewhub;

import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.jsondb.JsonDBTemplate;
import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import io.jsondb.annotation.Secret;
import javafx.scene.image.Image;

@Document(collection = "utente", schemaVersion = "1.0")
public class Utente {
	private String dbFilesLocation="src/main/resources";
	private String BaseScanPackage="it.univaq.brewhub";
	private JsonDBTemplate json=new JsonDBTemplate(dbFilesLocation, BaseScanPackage);
	
	private Image fotoProfilo;

    private String nome;
    private String cognome;
    
    @Id
    private String username;
    
    @Secret
    private String password;
    
    @Secret
    private String pwCrypto;

    public enum TipoUtente {
        BARISTA("Barista"),
        APPASSIONATO("Appassionato"),
        UTENTE_MEDIO("Utente medio"),
        ADMIN("Admin"),
        OSPITE("Ospite");
    	
    	String label;
    	
    	private TipoUtente(String label) {
			this.label=label;
		}

		@Override
    	public String toString() {
    		return label;
    	}
    }

    private TipoUtente tipo;
    
    private List<Post> archivio=new ArrayList<Post>();
    private List<Utente> follower=new ArrayList<Utente>();
    private List<Utente> following=new ArrayList<Utente>();
    
    
    
    // --- COSTRUTTORI ---
    //Costruttore vuoto per json
    public Utente() {
    	
    }
    
    //Costruttore per ospite
    public Utente(String username) {
    	this.username=username;
    	this.password=null;
    	this.tipo=TipoUtente.OSPITE;
    }
    
    //Costruttore per utente registrato/da registrare
    public Utente(String nome,String cognome,String username,String password,TipoUtente tipo,Image fotoProfilo) {
    	this.nome=nome;
    	this.cognome=cognome;
    	this.username=username;
    	this.pwCrypto=BCrypt.hashpw(password, BCrypt.gensalt());
    	this.tipo=tipo;
    	this.fotoProfilo=fotoProfilo;
    }
    
    
    
    // --- GETTER ---
    public String getNome() {
    	return nome;
    }
    
    public String getCognome() {
    	return cognome;
    }
    
    public String getUsername() {
		return username;
	}
    
    @JsonIgnore
    public String getPassword() {
		return password;
	}
    
    public String getPasswordCrypto() {
		return pwCrypto;
	}
    
    public TipoUtente getTipo() {
		return tipo;
	}
    
    public List<Post> getArchivio() {
		return archivio;
	}
    
    public Post getSingoloPost(int i) {
    	return archivio.get(i);
    }
    
    public List<Utente> getFollower() {
		return follower;
	}
    
    @JsonIgnore
    public Utente getSingoloFollower(int i) {
    	return follower.get(i);
    }
    
    @JsonIgnore
    public int getNumFollower() {
    	return follower.size();
    }
    
    public List<Utente> getFollowing() {
		return following;
	}
    
    @JsonIgnore
    public Utente getSingoloFollowing(int i){
    	return following.get(i);
     }
    
    @JsonIgnore
    public int getNumFollowing() {
    	return following.size();
    }
    
    public Image getFotoProfilo() {
		return fotoProfilo;
	}
    
    
    
    // --- SETTER ---
    public void setNome(String nome) {
		this.nome = nome;
	}
    
    public void setCognome(String cognome) {
		this.cognome = cognome;
	}
    
    public void setUsername(String username) {
		this.username = username;
	}
    
    @JsonIgnore
    public void setPassword(String password) {
		this.password = password;
	}
    
    public void setPasswordCrypto(String pwCrypto) {
		this.pwCrypto = pwCrypto;
	}
    
    public void setArchivio(List<Post> archivio) {
		this.archivio = archivio;
	}
    
    @JsonIgnore
    public void setSingoloPost(Post post,int i) {
    	archivio.set(i, post);
    }
    
    public void setFollower(List<Utente> follower) {
		this.follower = follower;
	}
    
    @JsonIgnore
    public void setSingoloFollower(Utente utente,int i) {
    	follower.set(i, utente);
    }
    
    public void setFollowing(List<Utente> following) {
		this.following = following;
	}
    
    @JsonIgnore
    public void setSingoloFollowing(Utente utente,int i) {
    	following.set(i, utente);
    }
    
    public void setTipo(TipoUtente tipo) {
		this.tipo = tipo;
	}
    
    public void setFotoProfilo(Image fotoProfilo) {
		this.fotoProfilo = fotoProfilo;
	}
    
    
    
    // --- GESTIONE DATI ---
    public void creaCollezione() {
    	if(!json.collectionExists(Utente.class)) {
    		json.createCollection(Utente.class);
    	}
    }
    
    public void salvaJson(Utente utente) {
    	json.insert(utente);
    }
    
    public void modificaJson(Utente utente) {
    	json.upsert(utente);
    }
    
    public void rimuoviJson(Utente utente) {
    	json.remove(utente, Utente.class);
    }
    
    public List<Utente> leggiJson(){
    	return json.findAll(Utente.class);
    }
    
    public Utente leggiSingoloJson(String username) {
    	return json.findById(username, Utente.class);
    }
    
    public void backupJson() {
    	json.backup("src/main/resources/backup");
    }
    
    public void ripristinoJson() {
    	json.restore("src/main/resources/backup", false);
    }
    

}
