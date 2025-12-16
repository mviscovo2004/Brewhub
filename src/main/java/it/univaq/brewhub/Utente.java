package it.univaq.brewhub;

// Importazioni librerie Java e classi del progetto
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

// Classe Utente che rappresenta un utente dell'applicazione
public class Utente {

  // Attributi dell'utente
  private String fotoProfilo;
  private String nome;
  private String cognome;
  private String username;
  private String password;
  private String pwCrypto;

  // Tipi di utente supportati
  public enum TipoUtente {

    // Definizione tipi utente
    BARISTA("Barista"),
    APPASSIONATO("Appassionato"),
    UTENTE_MEDIO("Utente medio"),
    ADMIN("Admin"),
    OSPITE("Ospite");

    String label;

    // Costruttore enum
    private TipoUtente(String label) {
      this.label = label;
    }

    // toString override per ottenere la rappresentazione testuale
    @Override
    public String toString() {
      return label;
    }
  }

  private TipoUtente tipo;

  // Liste per archivio post, follower e following
  private List<Post> archivio = new ArrayList<Post>();
  private List<Utente> follower = new ArrayList<Utente>();
  private List<Utente> following = new ArrayList<Utente>();

  // --- COSTRUTTORI ---
  // Costruttore vuoto per db
  public Utente() {

  }

  // Costruttore per ospite
  public Utente(String username) {
    this.username = username;
    this.password = null;
    this.tipo = TipoUtente.OSPITE;
  }

  // Costruttore per utente registrato/da registrare
  public Utente(String nome, String cognome, String username, String password, TipoUtente tipo, String fotoProfilo) {
    this.nome = nome;
    this.cognome = cognome;
    this.username = username;
    this.password = password;
    this.pwCrypto = BCrypt.hashpw(password, BCrypt.gensalt());
    this.tipo = tipo;
    this.fotoProfilo = fotoProfilo;
  }

  // --- GETTER ---
  // Ritorna il nome dell'utente
  public String getNome() {
    return nome;
  }

  // Ritorna il cognome dell'utente
  public String getCognome() {
    return cognome;
  }

  // Ritorna lo username dell'utente
  public String getUsername() {
    return username;
  }

  // Ritorna la password in chiaro dell'utente
  protected String getPassword() {
    return password;
  }

  // Ritorna la password criptata dell'utente
  public String getPasswordCrypto() {
    return pwCrypto;
  }

  // Ritorna il tipo di utente
  public TipoUtente getTipo() {
    return tipo;
  }

  // Ritorna l'archivio dei post dell'utente
  public List<Post> getArchivio() {
    return archivio;
  }

  // Ritorna un singolo post dall'archivio
  public Post getSingoloPost(int i) {
    return archivio.get(i);
  }

  // Ritorna il numero di post nell'archivio
  public int getNumPost() {
    return archivio.size();
  }

  // Ritorna la lista dei follower dell'utente
  public List<Utente> getFollower() {
    return follower;
  }

  // Ritorna un singolo follower
  public Utente getSingoloFollower(int i) {
    return follower.get(i);
  }

  // Ritorna il numero di follower
  public int getNumFollower() {
    return follower.size();
  }

  // Ritorna la lista degli utenti seguiti
  public List<Utente> getFollowing() {
    return following;
  }

  // Ritorna un singolo utente seguito
  public Utente getSingoloFollowing(int i) {
    return following.get(i);
  }

  // Ritorna il numero di utenti seguiti
  public int getNumFollowing() {
    return following.size();
  }

  // Ritorna il percorso della foto profilo
  public String getFotoProfilo() {
    return fotoProfilo;
  }

  // --- SETTER ---
  // Imposta il nome dell'utente
  public void setNome(String nome) {
    this.nome = nome;
  }

  // Imposta il cognome dell'utente
  public void setCognome(String cognome) {
    this.cognome = cognome;
  }

  // Imposta lo username dell'utente
  public void setUsername(String username) {
    this.username = username;
  }

  // Imposta la password in chiaro dell'utente
  public void setPassword(String password) {
    this.password = password;
  }

  // Imposta la password criptata dell'utente
  public void setPasswordCrypto(String pwCrypto) {
    this.pwCrypto = pwCrypto;
  }

  // Imposta l'archivio dei post dell'utente
  public void setArchivio(List<Post> archivio) {
    this.archivio = archivio;
  }

  // Imposta un singolo post nell'archivio
  public void setSingoloPost(Post post, int i) {
    archivio.set(i, post);
  }

  // Imposta la lista dei follower dell'utente
  public void setFollower(List<Utente> follower) {
    this.follower = follower;
  }

  // Imposta un singolo follower
  public void setSingoloFollower(Utente utente, int i) {
    follower.set(i, utente);
  }

  // Imposta la lista degli utenti seguiti
  public void setFollowing(List<Utente> following) {
    this.following = following;
  }

  // Imposta un singolo utente seguito
  public void setSingoloFollowing(Utente utente, int i) {
    following.set(i, utente);
  }

  // Imposta il tipo di utente
  public void setTipo(TipoUtente tipo) {
    this.tipo = tipo;
  }

  // Imposta il percorso della foto profilo
  public void setFotoProfilo(String fotoProfilo) {
    this.fotoProfilo = fotoProfilo;
  }

  // --- METODI DATABASE ---
  // Registra un nuovo utente nel database
  public void registraUtente(Utente u) throws SQLException {

    // Controlla se lo username esiste già
    if (usernameEsiste(u.getUsername())) {
      // Lancia eccezione se esiste
      throw new SQLException("Username già registrato");
    }

    // Query SQL per inserimento nuovo utente
    String sql = "INSERT INTO utenti(username, nome, cognome, password_hash, tipo, foto_uri) VALUES(?,?,?,?,?,?)";

    // Esegui l'inserimento nel database
    try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

      // Imposta i parametri della query
      pstmt.setString(1, u.getUsername());
      pstmt.setString(2, u.getNome());
      pstmt.setString(3, u.getCognome());

      // Hash della password
      String hash = BCrypt.hashpw(u.getPassword(), BCrypt.gensalt());

      // Continua a impostare i parametri
      pstmt.setString(4, hash);
      pstmt.setString(5, u.getTipo().name());
      pstmt.setString(6, u.getFotoProfilo());

      // Esegui l'update
      pstmt.executeUpdate();
    }
  }

  // Controlla se uno username esiste già nel database
  private boolean usernameEsiste(String username) throws SQLException {

    // Query SQL per controllo esistenza username
    String sql = "SELECT COUNT(*) FROM utenti WHERE username = ?";

    // Esegui la query
    try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

      // Imposta il parametro della query
      pstmt.setString(1, username);

      // Esegui la query
      ResultSet rs = pstmt.executeQuery();

      // Controlla il risultato
      if (rs.next()) {
        // Ritorna true se esiste, false altrimenti
        return rs.getInt(1) > 0;
      }
    }
    return false;
  }

  // Effettua il login di un utente con username e password
  public Utente login(String username, String passwordInserita) {

    // Query SQL per recupero utente
    String sql = "SELECT * FROM utenti WHERE username = ?";

    // Esegui la query
    try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

      // Imposta il parametro della query
      pstmt.setString(1, username);

      // Esegui la query
      ResultSet rs = pstmt.executeQuery();

      // Verifica se l'utente esiste e la password è corretta
      if (rs.next()) {

        // Recupera l'hash della password salvata
        String hashSalvato = rs.getString("password_hash");

        // Confronta la password inserita con l'hash salvato
        if (hashSalvato != null && BCrypt.checkpw(passwordInserita, hashSalvato)) {

          // Crea l'oggetto Utente
          Utente u = new Utente();
          u.setUsername(rs.getString("username"));
          u.setNome(rs.getString("nome"));
          u.setCognome(rs.getString("cognome"));
          u.setPasswordCrypto(hashSalvato);

          // Imposta il tipo di utente
          String tipoStr = rs.getString("tipo");

          // Gestione tipo utente non valido
          try {
            u.setTipo(TipoUtente.valueOf(tipoStr));
          } catch (IllegalArgumentException e) {
            throw e;
          }

          // Imposta la foto profilo
          u.setFotoProfilo(rs.getString("foto_uri"));

          // Ritorna l'utente autenticato
          return u;
        }
      }
    } catch (SQLException e) {
      // Gestione eccezione SQL
      e.printStackTrace();
    }

    // Ritorna null se login fallito
    return null;
  }

  // Aggiorna le informazioni del profilo utente nel database
  public void aggiornaProfilo() throws SQLException {

    // Query SQL per aggiornamento profilo
    String sql;

    // Verifica se la password è stata cambiata
    boolean cambioPassword = this.password != null && !this.password.equals(this.pwCrypto);

    // Costruisci la query in base al cambio password
    if (cambioPassword) {

      // Aggiorna anche la password
      sql = "UPDATE utenti SET nome = ?, cognome = ?, foto_uri = ?, password_hash = ? WHERE username = ?";
    } else {

      // Aggiorna senza cambiare la password
      sql = "UPDATE utenti SET nome = ?, cognome = ?, foto_uri = ? WHERE username = ?";
    }

    // Esegui l'aggiornamento nel database
    try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

      // Imposta i parametri della query
      pstmt.setString(1, this.nome);
      pstmt.setString(2, this.cognome);
      pstmt.setString(3, this.fotoProfilo);

      // Gestione cambio password
      if (cambioPassword) {
        // Hash della nuova password
        String hash = BCrypt.hashpw(this.password, BCrypt.gensalt());
        this.pwCrypto = hash; 

        // Imposta il parametro con la password
        pstmt.setString(4, hash);
        pstmt.setString(5, this.username);
      } else {

        // Imposta il parametro senza password
        pstmt.setString(4, this.username);
      }

      // Esegui l'update
      pstmt.executeUpdate();

    }
  }

  // Elimina l'account utente dal database
  public void eliminaAccount() throws SQLException {

    // Query SQL per eliminazione utente
    String sql = "DELETE FROM utenti WHERE username = ?";

    // Esegui l'eliminazione nel database
    try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

      // Imposta il parametro della query
      pstmt.setString(1, this.username);

      // Esegui l'update e controlla il risultato
      int affectedRows = pstmt.executeUpdate();
      if (affectedRows <= 0) {

        // Nessuna riga eliminata, utente non trovato
        throw new SQLException("Impossibile eliminare l'account: utente non trovato.");
      }
    }
  }
}
