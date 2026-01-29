package it.univaq.brewhub.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che rappresenta un utente all'interno del sistema BrewHub.
 * Contiene le informazioni personali, le credenziali e le relazioni sociali
 * (follower, following).
 */
public class Utente {

  /**
   * URI dell'immagine profilo dell'utente.
   */
  private String fotoProfilo;

  /**
   * Nome dell'utente.
   */
  private String nome;

  /**
   * Cognome dell'utente.
   */
  private String cognome;

  /**
   * Username univoco per l'accesso e l'identificazione nel sistema.
   */
  private String username;

  /**
   * Password in formato testo (utilizzata in fase di input).
   */
  private String password;

  /**
   * Hash crittografico della password (per il salvataggio sicuro).
   */
  private String pwCrypto;

  /**
   * Enumerazione dei ruoli disponibili per gli utenti.
   */
  public enum TipoUtente {
    /** Professionista Barista */
    BARISTA("Barista"),
    /** Appassionato di caffè */
    APPASSIONATO("Appassionato"),
    /** Azienda Torrefattore */
    TORREFATTORE("Torrefattore"),
    /** Utente curioso */
    CURIOSO("Curioso"),
    /** Amministratore */
    ADMIN("Admin"),
    /** Utente ospite */
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

  /**
   * Ruolo dell'utente nel sistema.
   */
  private TipoUtente tipo;

  /**
   * Lista dei post salvati dall'utente.
   */
  private List<Post> archivio = new ArrayList<>();

  /**
   * Lista degli utenti che seguono questo utente.
   */
  private List<Utente> follower = new ArrayList<>();

  /**
   * Lista degli utenti seguiti da questo utente.
   */
  private List<Utente> following = new ArrayList<>();

  /**
   * Costruttore predefinito.
   */
  public Utente() {
  }

  /**
   * Costruttore per utente ospite.
   * 
   * @param username L'username temporaneo.
   */
  public Utente(String username) {
    this.username = username;
    this.password = null;
    this.tipo = TipoUtente.OSPITE;
  }

  /**
   * Costruttore completo per creare un nuovo utente.
   * 
   * @param nome        Il nome.
   * @param cognome     Il cognome.
   * @param username    Lo username.
   * @param password    La password.
   * @param tipo        Il ruolo.
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
   * Restituisce un Builder per la creazione fluente di oggetti Utente.
   *
   * @return Un'istanza di {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Pattern Builder per facilitare la creazione di oggetti Utente complessi.
   */
  public static class Builder {
    private String fotoProfilo;
    private String nome;
    private String cognome;
    private String username;
    private String password;
    private TipoUtente tipo = TipoUtente.APPASSIONATO; // Valore di default

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

  // --- Getter ---

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
   * Restituisce la password in chiaro dell'utente.
   *
   * @return La password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Restituisce la password in formato hash cifrato.
   *
   * @return La password cifrata.
   */
  public String getPasswordCrypto() {
    return pwCrypto;
  }

  /**
   * Restituisce il ruolo dell'utente.
   *
   * @return Il tipo di utente.
   */
  public TipoUtente getTipo() {
    return tipo;
  }

  /**
   * Restituisce la lista dei post salvati.
   *
   * @return La lista dell'archivio.
   */
  public List<Post> getArchivio() {
    return archivio;
  }

  /**
   * Restituisce un singolo post dall'archivio.
   *
   * @param i L'indice del post.
   * @return Il post.
   */
  public Post getSingoloPost(int i) {
    return archivio.get(i);
  }

  /**
   * Restituisce il numero di post salvati.
   *
   * @return La dimensione dell'archivio.
   */
  public int getNumPost() {
    return archivio.size();
  }

  /**
   * Restituisce la lista dei follower.
   *
   * @return La lista dei follower.
   */
  public List<Utente> getFollower() {
    return follower;
  }

  /**
   * Restituisce un singolo follower.
   *
   * @param i L'indice.
   * @return L'utente follower.
   */
  public Utente getSingoloFollower(int i) {
    return follower.get(i);
  }

  /**
   * Restituisce il numero di follower.
   *
   * @return La dimensione della lista follower.
   */
  public int getNumFollower() {
    return follower.size();
  }

  /**
   * Restituisce la lista degli utenti seguiti (following).
   *
   * @return La lista following.
   */
  public List<Utente> getFollowing() {
    return following;
  }

  /**
   * Restituisce un singolo utente seguito.
   *
   * @param i L'indice.
   * @return L'utente seguito.
   */
  public Utente getSingoloFollowing(int i) {
    return following.get(i);
  }

  /**
   * Restituisce il numero di utenti seguiti.
   *
   * @return La dimensione della lista following.
   */
  public int getNumFollowing() {
    return following.size();
  }

  /**
   * Restituisce l'URI della foto profilo.
   *
   * @return Il percorso della foto.
   */
  public String getFotoProfilo() {
    return fotoProfilo;
  }

  // --- Setter ---

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
   * Imposta la password dell'utente.
   *
   * @param password La nuova password.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Imposta l'hash della password.
   *
   * @param pwCrypto La password cifrata.
   */
  public void setPasswordCrypto(String pwCrypto) {
    this.pwCrypto = pwCrypto;
  }

  /**
   * Imposta la lista dei post salvati.
   *
   * @param archivio La nuova lista.
   */
  public void setArchivio(List<Post> archivio) {
    this.archivio = archivio;
  }

  /**
   * Sostituisce un post nell'archivio.
   *
   * @param post Il nuovo post.
   * @param i    L'indice.
   */
  public void setSingoloPost(Post post, int i) {
    archivio.set(i, post);
  }

  /**
   * Imposta la lista dei follower.
   *
   * @param follower La nuova lista.
   */
  public void setFollower(List<Utente> follower) {
    this.follower = follower;
  }

  /**
   * Sostituisce un follower nella lista.
   *
   * @param utente L'utente.
   * @param i      L'indice.
   */
  public void setSingoloFollower(Utente utente, int i) {
    follower.set(i, utente);
  }

  /**
   * Imposta la lista dei following.
   *
   * @param following La nuova lista.
   */
  public void setFollowing(List<Utente> following) {
    this.following = following;
  }

  /**
   * Sostituisce un following nella lista.
   *
   * @param utente L'utente.
   * @param i      L'indice.
   */
  public void setSingoloFollowing(Utente utente, int i) {
    following.set(i, utente);
  }

  /**
   * Imposta il ruolo dell'utente.
   *
   * @param tipo Il nuovo tipo.
   */
  public void setTipo(TipoUtente tipo) {
    this.tipo = tipo;
  }

  /**
   * Imposta l'URI della foto profilo.
   *
   * @param fotoProfilo Il nuovo percorso.
   */
  public void setFotoProfilo(String fotoProfilo) {
    this.fotoProfilo = fotoProfilo;
  }
}
