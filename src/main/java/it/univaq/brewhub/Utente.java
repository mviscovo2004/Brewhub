package it.univaq.brewhub;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

public class Utente {

  private String fotoProfilo;

  private String nome;
  private String cognome;

  private String username;

  private String password;

  private String pwCrypto;

  public enum TipoUtente {
    BARISTA("Barista"),
    APPASSIONATO("Appassionato"),
    UTENTE_MEDIO("Utente medio"),
    ADMIN("Admin"),
    OSPITE("Ospite");

    String label;

    private TipoUtente(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  private TipoUtente tipo;

  private List<Post> archivio = new ArrayList<Post>();
  private List<Utente> follower = new ArrayList<Utente>();
  private List<Utente> following = new ArrayList<Utente>();

  // --- COSTRUTTORI ---
  // Costruttore vuoto per json
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
  public String getNome() {
    return nome;
  }

  public String getCognome() {
    return cognome;
  }

  public String getUsername() {
    return username;
  }

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

  public Utente getSingoloFollower(int i) {
    return follower.get(i);
  }

  public int getNumFollower() {
    return follower.size();
  }

  public List<Utente> getFollowing() {
    return following;
  }

  public Utente getSingoloFollowing(int i) {
    return following.get(i);
  }

  public int getNumFollowing() {
    return following.size();
  }

  public String getFotoProfilo() {
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

  public void setPassword(String password) {
    this.password = password;
  }

  public void setPasswordCrypto(String pwCrypto) {
    this.pwCrypto = pwCrypto;
  }

  public void setArchivio(List<Post> archivio) {
    this.archivio = archivio;
  }

  public void setSingoloPost(Post post, int i) {
    archivio.set(i, post);
  }

  public void setFollower(List<Utente> follower) {
    this.follower = follower;
  }

  public void setSingoloFollower(Utente utente, int i) {
    follower.set(i, utente);
  }

  public void setFollowing(List<Utente> following) {
    this.following = following;
  }

  public void setSingoloFollowing(Utente utente, int i) {
    following.set(i, utente);
  }

  public void setTipo(TipoUtente tipo) {
    this.tipo = tipo;
  }

  public void setFotoProfilo(String fotoProfilo) {
    this.fotoProfilo = fotoProfilo;
  }

  public void registraUtente(Utente u) throws SQLException {
    // Verifica se l'username è già registrato
    if (usernameEsiste(u.getUsername())) {
      throw new SQLException("Username già registrato");
    }

    String sql = "INSERT INTO utenti(username, nome, cognome, password_hash, tipo, foto_uri) VALUES(?,?,?,?,?,?)";

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, u.getUsername());
      pstmt.setString(2, u.getNome());
      pstmt.setString(3, u.getCognome());
      // Hashiamo la password prima di salvarla
      String hash = BCrypt.hashpw(u.getPassword(), BCrypt.gensalt());

      pstmt.setString(4, hash);
      pstmt.setString(5, u.getTipo().name());
      pstmt.setString(6, u.getFotoProfilo());

      pstmt.executeUpdate();
    }
  }

  // Metodo per controllare se un username esiste già nel database
  private boolean usernameEsiste(String username) throws SQLException {
    String sql = "SELECT COUNT(*) FROM utenti WHERE username = ?";

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        return rs.getInt(1) > 0;
      }
    }
    return false;
  }

  // Metodo per il LOGIN
  public Utente login(String username, String passwordInserita) {
    String sql = "SELECT * FROM utenti WHERE username = ?";

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        String hashSalvato = rs.getString("password_hash");

        // Verifica password con BCrypt
        if (hashSalvato != null && BCrypt.checkpw(passwordInserita, hashSalvato)) {

          // Ricostruiamo l'oggetto Utente dai dati del DB
          Utente u = new Utente();
          u.setUsername(rs.getString("username"));
          u.setNome(rs.getString("nome"));
          u.setCognome(rs.getString("cognome"));
          u.setPasswordCrypto(hashSalvato);

          String tipoStr = rs.getString("tipo");

          try {
            u.setTipo(TipoUtente.valueOf(tipoStr));
          } catch (IllegalArgumentException e) {
            throw e;
          }

          u.setFotoProfilo(rs.getString("foto_uri"));
          return u;
        }
      }
    } catch (SQLException e) {

      e.printStackTrace();
    }
    return null; // Login fallito
  }

  // In it/univaq/brewhub/Utente.java

  public void aggiornaProfilo() throws SQLException {
    // Se la password è stata modificata (quindi diversa dall'hash salvato), la
    // ri-hashiamo
    // Nota: assumiamo che se this.password è diverso da null e non inizia con
    // "$2a$", è una nuova password in chiaro
    String sql;
    boolean cambioPassword = this.password != null && !this.password.equals(this.pwCrypto);

    if (cambioPassword) {
      sql = "UPDATE utenti SET nome = ?, cognome = ?, foto_uri = ?, password_hash = ? WHERE username = ?";
    } else {
      sql = "UPDATE utenti SET nome = ?, cognome = ?, foto_uri = ? WHERE username = ?";
    }

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, this.nome);
      pstmt.setString(2, this.cognome);
      pstmt.setString(3, this.fotoProfilo);

      if (cambioPassword) {
        String hash = BCrypt.hashpw(this.password, BCrypt.gensalt());
        this.pwCrypto = hash; // Aggiorniamo l'hash in memoria
        pstmt.setString(4, hash);
        pstmt.setString(5, this.username);
      } else {
        pstmt.setString(4, this.username);
      }

      pstmt.executeUpdate();

    }
  }

  public void eliminaAccount() throws SQLException {
    String sql = "DELETE FROM utenti WHERE username = ?";

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, this.username);

      int affectedRows = pstmt.executeUpdate();
      if (affectedRows <= 0) {
        throw new SQLException("Impossibile eliminare l'account: utente non trovato.");
      }
    }
  }
}
