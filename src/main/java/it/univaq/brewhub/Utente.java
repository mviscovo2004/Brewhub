package it.univaq.brewhub;

import java.util.ArrayList;
import java.util.List;

/**
 * Modello che rappresenta l'Utente nel sistema.
 * POJO puro: la logica di persistenza è delegata a UtenteDAO.
 */
public class Utente {

  // Attributi anagrafici e credenziali
  /** Foto del profilo dell'utente (URI/Path). */
  private String fotoProfilo;
  /** Nome dell'utente. */
  private String nome;
  /** Cognome dell'utente. */
  private String cognome;
  /** Username univoco dell'utente. */
  private String username;
  /** Password dell'utente (in chiaro, usata solo transitoriamente). */
  private String password;
  /** Hash della password (salvata nel DB). */
  private String pwCrypto;

  /**
   * Enumerazione dei tipi di utente disponibili nel sistema.
   */
  public enum TipoUtente {
    BARISTA("Barista"),
    APPASSIONATO("Appassionato"),
    TORREFATTORE("Torrefattore"),
    UTENTE_MEDIO("Utente medio"),
    ADMIN("Admin"),
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

  private TipoUtente tipo;

  // Liste per le relazioni e i contenuti
  private List<Post> archivio = new ArrayList<>();
  private List<Utente> follower = new ArrayList<>();
  private List<Utente> following = new ArrayList<>();

  /**
   * Costruttore di default.
   */
  public Utente() {
  }

  /**
   * Costruttore per utente ospite o temporaneo.
   *
   * @param username Lo username.
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
   * @param password    La password in chiaro.
   * @param tipo        Il tipo di utente.
   * @param fotoProfilo L'URI della foto profilo.
   */
  public Utente(String nome, String cognome, String username, String password, TipoUtente tipo, String fotoProfilo) {
    this.nome = nome;
    this.cognome = cognome;
    this.username = username;
    this.password = password;
    // L'hash verrà ricalcolato dal DAO se necessario, o qui se vogliamo mantenere
    // logica domain.
    // Per coerenza con DAOImpl, lasciamo che il campo pwCrypto gestisca l'hash.
    this.tipo = tipo;
    this.fotoProfilo = fotoProfilo;
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

  // Rendo pubblico getPassword per permettere al DAO di leggerla
  /**
   * Restituisce la password in chiaro.
   *
   * @return La password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Restituisce l'hash della password cifrata.
   *
   * @return L'hash della password.
   */
  public String getPasswordCrypto() {
    return pwCrypto;
  }

  /**
   * Restituisce il tipo di utente.
   *
   * @return Il tipo di utente.
   */
  public TipoUtente getTipo() {
    return tipo;
  }

  /**
   * Restituisce la lista dei post nell'archivio dell'utente.
   *
   * @return La lista dei post.
   */
  public List<Post> getArchivio() {
    return archivio;
  }

  /**
   * Restituisce un singolo post dall'archivio dato l'indice.
   *
   * @param i L'indice.
   * @return Il post.
   */
  public Post getSingoloPost(int i) {
    return archivio.get(i);
  }

  /**
   * Restituisce il numero di post nell'archivio.
   *
   * @return Il numero di post.
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
   * Restituisce un singolo follower dato l'indice.
   *
   * @param i L'indice.
   * @return Il follower.
   */
  public Utente getSingoloFollower(int i) {
    return follower.get(i);
  }

  /**
   * Restituisce il numero di follower.
   *
   * @return Il numero di follower.
   */
  public int getNumFollower() {
    return follower.size();
  }

  /**
   * Restituisce la lista degli utenti seguiti (following).
   *
   * @return La lista dei following.
   */
  public List<Utente> getFollowing() {
    return following;
  }

  /**
   * Restituisce un singolo utente seguito dato l'indice.
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
   * @return Il numero di following.
   */
  public int getNumFollowing() {
    return following.size();
  }

  /**
   * Restituisce l'URI della foto profilo.
   *
   * @return L'URI della foto profilo.
   */
  public String getFotoProfilo() {
    return fotoProfilo;
  }

  // --- SETTER ---

  /**
   * Imposta il nome dell'utente.
   *
   * @param nome Il nome.
   */
  public void setNome(String nome) {
    this.nome = nome;
  }

  /**
   * Imposta il cognome dell'utente.
   *
   * @param cognome Il cognome.
   */
  public void setCognome(String cognome) {
    this.cognome = cognome;
  }

  /**
   * Imposta lo username dell'utente.
   *
   * @param username Lo username.
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Imposta la password in chiaro.
   *
   * @param password La password.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Imposta l'hash della password cifrata.
   *
   * @param pwCrypto L'hash della password.
   */
  public void setPasswordCrypto(String pwCrypto) {
    this.pwCrypto = pwCrypto;
  }

  /**
   * Imposta l'archivio dei post dell'utente.
   *
   * @param archivio La lista dei post.
   */
  public void setArchivio(List<Post> archivio) {
    this.archivio = archivio;
  }

  /**
   * Imposta un singolo post nell'archivio all'indice specificato.
   *
   * @param post Il post.
   * @param i    L'indice.
   */
  public void setSingoloPost(Post post, int i) {
    archivio.set(i, post);
  }

  /**
   * Imposta la lista dei follower.
   *
   * @param follower La lista dei follower.
   */
  public void setFollower(List<Utente> follower) {
    this.follower = follower;
  }

  /**
   * Imposta un singolo follower nella lista all'indice specificato.
   *
   * @param utente Il follower.
   * @param i      L'indice.
   */
  public void setSingoloFollower(Utente utente, int i) {
    follower.set(i, utente);
  }

  /**
   * Imposta la lista degli utenti seguiti.
   *
   * @param following La lista dei following.
   */
  public void setFollowing(List<Utente> following) {
    this.following = following;
  }

  /**
   * Imposta un singolo utente seguito nella lista all'indice specificato.
   *
   * @param utente L'utente seguito.
   * @param i      L'indice.
   */
  public void setSingoloFollowing(Utente utente, int i) {
    following.set(i, utente);
  }

  /**
   * Imposta il tipo di utente.
   *
   * @param tipo Il tipo di utente.
   */
  public void setTipo(TipoUtente tipo) {
    this.tipo = tipo;
  }

  /**
   * Imposta l'URI della foto profilo.
   *
   * @param fotoProfilo L'URI della foto profilo.
   */
  public void setFotoProfilo(String fotoProfilo) {
    this.fotoProfilo = fotoProfilo;
  }
}
