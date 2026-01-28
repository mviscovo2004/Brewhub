package it.univaq.brewhub.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta un utente registrato nel sistema BrewHub.
 *
 * Questa classe è un POJO (Plain Old Java Object) che contiene le informazioni
 * anagrafiche,
 * le credenziali di accesso e le relazioni dell'utente (follower, following,
 * post salvati).
 *
 */
public class Utente {

  /** URI o percorso relativo dell'immagine del profilo dell'utente. */
  private String fotoProfilo;

  /** Nome dell'utente. */
  private String nome;

  /** Cognome dell'utente. */
  private String cognome;

  /** Username univoco che identifica l'utente nel sistema. */
  private String username;

  /**
   * Password in chiaro (utilizzata temporaneamente durante registrazione/login).
   */
  private String password;

  /** Hash della password (versione cifrata salvata nel database). */
  private String pwCrypto;

  /**
   * Enumerazione dei possibili ruoli o tipi di utente nel sistema.
   */
  public enum TipoUtente {
    /** Utente professionista del bar. */
    BARISTA("Barista"),
    /** Utente appassionato di caffè. */
    APPASSIONATO("Appassionato"),
    /** Azienda di torrefazione. */
    TORREFATTORE("Torrefattore"),
    /** Utente generico interessato. */
    CURIOSO("Curioso"),
    /** Amministratore del sistema. */
    ADMIN("Admin"),
    /** Utente non registrato o con accesso limitato. */
    OSPITE("Ospite");

    private final String label;

    TipoUtente(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  /** Il ruolo dell'utente nel sistema. */
  private TipoUtente tipo;

  /** Lista dei post salvati nell'archivio personale dell'utente. */
  private List<Post> archivio = new ArrayList<>();

  /** Lista degli utenti che seguono questo utente. */
  private List<Utente> follower = new ArrayList<>();

  /** Lista degli utenti seguiti da questo utente. */
  private List<Utente> following = new ArrayList<>();

  /**
   * Costruttore vuoto predefinito.
   */
  public Utente() {
  }

  /**
   * Costruttore per creare un utente ospite o temporaneo.
   * 
   * @param username Lo username dell'utente.
   */
  public Utente(String username) {
    this.username = username;
    this.password = null;
    this.tipo = TipoUtente.OSPITE;
  }

  /**
   * Costruttore completo per inizializzare un nuovo utente.
   * 
   * @param nome        Il nome dell'utente.
   * @param cognome     Il cognome dell'utente.
   * @param username    Lo username univoco.
   * @param password    La password in chiaro.
   * @param tipo        Il ruolo dell'utente.
   * @param fotoProfilo L'URI della foto profilo.
   */
  public Utente(String nome, String cognome, String username, String password, TipoUtente tipo, String fotoProfilo) {
    this.nome = nome;
    this.cognome = cognome;
    this.username = username;
    this.password = password;
    this.tipo = tipo;
    this.fotoProfilo = fotoProfilo;
  }

  /**
   * Restituisce un nuovo Builder per creare istanze di Utente in modo fluente.
   * 
   * @return Un'istanza di {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Pattern Builder per la classe Utente.
   * Permette una costruzione più leggibile e flessibile dell'oggetto.
   */
  public static class Builder {
    private String fotoProfilo;
    private String nome;
    private String cognome;
    private String username;
    private String password;
    private TipoUtente tipo = TipoUtente.APPASSIONATO; // Default

    public Builder withFotoProfilo(String fotoProfilo) {
      this.fotoProfilo = fotoProfilo;
      return this;
    }

    public Builder withNome(String nome) {
      this.nome = nome;
      return this;
    }

    public Builder withCognome(String cognome) {
      this.cognome = cognome;
      return this;
    }

    public Builder withUsername(String username) {
      this.username = username;
      return this;
    }

    public Builder withPassword(String password) {
      this.password = password;
      return this;
    }

    public Builder withTipo(TipoUtente tipo) {
      this.tipo = tipo;
      return this;
    }

    public Utente build() {
      return new Utente(nome, cognome, username, password, tipo, fotoProfilo);
    }
  }

  // --- GETTER ---

  /**
   * Restituisce il nome dell'utente.
   * 
   * @return Il nome.
   */
  public String getNome() {
    return nome;
  }

  /**
   * Restituisce il cognome dell'utente.
   * 
   * @return Il cognome.
   */
  public String getCognome() {
    return cognome;
  }

  /**
   * Restituisce lo username dell'utente.
   * 
   * @return Lo username.
   */
  public String getUsername() {
    return username;
  }

  /**
   * Restituisce la password in chiaro.
   * 
   * @return La password in chiaro.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Restituisce l'hash cifrato della password.
   * 
   * @return La password cifrata.
   */
  public String getPasswordCrypto() {
    return pwCrypto;
  }

  /**
   * Restituisce il tipo (ruolo) dell'utente.
   * 
   * @return Il {@link TipoUtente}.
   */
  public TipoUtente getTipo() {
    return tipo;
  }

  /**
   * Restituisce la lista completa dei post salvati.
   * 
   * @return Lista di {@link Post}.
   */
  public List<Post> getArchivio() {
    return archivio;
  }

  /**
   * Ottiene un singolo post dall'archivio in base all'indice.
   * 
   * @param i L'indice del post.
   * @return Il post corrispondente.
   */
  public Post getSingoloPost(int i) {
    return archivio.get(i);
  }

  /**
   * Restituisce il numero totale di post salvati.
   * 
   * @return Il conteggio dei post.
   */
  public int getNumPost() {
    return archivio.size();
  }

  /**
   * Restituisce la lista dei follower.
   * 
   * @return Lista di {@link Utente}.
   */
  public List<Utente> getFollower() {
    return follower;
  }

  /**
   * Ottiene un singolo follower in base all'indice.
   * 
   * @param i L'indice.
   * @return L'utente follower.
   */
  public Utente getSingoloFollower(int i) {
    return follower.get(i);
  }

  /**
   * Restituisce il numero totale di follower.
   * 
   * @return Il conteggio dei follower.
   */
  public int getNumFollower() {
    return follower.size();
  }

  /**
   * Restituisce la lista degli utenti seguiti (following).
   * 
   * @return Lista di {@link Utente}.
   */
  public List<Utente> getFollowing() {
    return following;
  }

  /**
   * Ottiene un singolo utente seguito in base all'indice.
   * 
   * @param i L'indice.
   * @return L'utente seguito.
   */
  public Utente getSingoloFollowing(int i) {
    return following.get(i);
  }

  /**
   * Restituisce il numero totale di utenti seguiti.
   * 
   * @return Il conteggio dei following.
   */
  public int getNumFollowing() {
    return following.size();
  }

  /**
   * Restituisce il percorso della foto profilo.
   * 
   * @return URI o path della foto.
   */
  public String getFotoProfilo() {
    return fotoProfilo;
  }

  // --- SETTER ---

  /**
   * Imposta il nome dell'utente.
   * 
   * @param nome Il nuovo nome.
   */
  public void setNome(String nome) {
    this.nome = nome;
  }

  /**
   * Imposta il cognome dell'utente.
   * 
   * @param cognome Il nuovo cognome.
   */
  public void setCognome(String cognome) {
    this.cognome = cognome;
  }

  /**
   * Imposta lo username dell'utente.
   * 
   * @param username Il nuovo username.
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Imposta la password in chiaro.
   * 
   * @param password La nuova password.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Imposta la password cifrata.
   * 
   * @param pwCrypto L'hash della password.
   */
  public void setPasswordCrypto(String pwCrypto) {
    this.pwCrypto = pwCrypto;
  }

  /**
   * Imposta l'elenco dei post salvati.
   * 
   * @param archivio La nuova lista di post.
   */
  public void setArchivio(List<Post> archivio) {
    this.archivio = archivio;
  }

  /**
   * Aggiorna un post specifico nell'archivio.
   * 
   * @param post Il nuovo post.
   * @param i    L'indice da aggiornare.
   */
  public void setSingoloPost(Post post, int i) {
    archivio.set(i, post);
  }

  /**
   * Imposta la lista dei follower.
   * 
   * @param follower La nuova lista di follower.
   */
  public void setFollower(List<Utente> follower) {
    this.follower = follower;
  }

  /**
   * Aggiorna un follower specifico nella lista.
   * 
   * @param utente L'utente da inserire.
   * @param i      L'indice da aggiornare.
   */
  public void setSingoloFollower(Utente utente, int i) {
    follower.set(i, utente);
  }

  /**
   * Imposta la lista degli utenti seguiti.
   * 
   * @param following La nuova lista di following.
   */
  public void setFollowing(List<Utente> following) {
    this.following = following;
  }

  /**
   * Aggiorna un utente seguito specifico nella lista.
   * 
   * @param utente L'utente da inserire.
   * @param i      L'indice da aggiornare.
   */
  public void setSingoloFollowing(Utente utente, int i) {
    following.set(i, utente);
  }

  /**
   * Imposta il tipo (ruolo) dell'utente.
   * 
   * @param tipo Il nuovo {@link TipoUtente}.
   */
  public void setTipo(TipoUtente tipo) {
    this.tipo = tipo;
  }

  /**
   * Imposta la foto profilo.
   * 
   * @param fotoProfilo Il percorso della nuova foto.
   */
  public void setFotoProfilo(String fotoProfilo) {
    this.fotoProfilo = fotoProfilo;
  }
}
