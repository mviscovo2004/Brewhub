package it.univaq.brewhub;

import java.time.LocalDateTime;

/**
 * Classe che rappresenta un Commento a un Post.
 * POJO puro: persistenza delegata a CommentoDAO.
 */
public class Commento {

	// Attributi
	/** Identificativo univoco del commento. */
	private int id; // ID database
	/** Utente autore del commento. */
	private Utente utente;
	/** Post a cui si riferisce il commento. */
	private Post post;
	/** Contenuto testuale del commento. */
	private String contenuto;
	/** Data e ora di creazione del commento. */
	private LocalDateTime dataCreazione = LocalDateTime.now();

	// Costruttore vuoto
	public Commento() {
	}

	// Costruttore completo
	/**
	 * Costruisce un nuovo Commento con i dettagli specificati.
	 *
	 * @param utente        L'utente che ha creato il commento.
	 * @param post          Il post commentato.
	 * @param contenuto     Il contenuto del commento.
	 * @param dataCreazione La data di creazione (se null, usa quella attuale).
	 */
	public Commento(Utente utente, Post post, String contenuto, LocalDateTime dataCreazione) {
		this.contenuto = contenuto;
		this.utente = utente;
		this.post = post;
		if (dataCreazione != null) {
			this.dataCreazione = dataCreazione;
		}
	}

	// --- GETTER & SETTER ---

	/**
	 * Restituisce l'ID univoco del commento.
	 *
	 * @return L'ID del commento.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Imposta l'ID univoco del commento.
	 *
	 * @param id L'ID del commento.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Restituisce il contenuto del commento.
	 *
	 * @return Il contenuto.
	 */
	public String getContenuto() {
		return contenuto;
	}

	/**
	 * Imposta il contenuto del commento.
	 *
	 * @param contenuto Il contenuto.
	 */
	public void setContenuto(String contenuto) {
		this.contenuto = contenuto;
	}

	/**
	 * Restituisce la data di creazione del commento.
	 *
	 * @return La data di creazione.
	 */
	public LocalDateTime getDataCreazione() {
		return dataCreazione;
	}

	/**
	 * Imposta la data di creazione del commento.
	 *
	 * @param dataCreazione La data di creazione.
	 */
	public void setDataCreazione(LocalDateTime dataCreazione) {
		this.dataCreazione = dataCreazione;
	}

	/**
	 * Restituisce il post associato al commento.
	 *
	 * @return Il post.
	 */
	public Post getPost() {
		return post;
	}

	/**
	 * Imposta il post associato al commento.
	 *
	 * @param post Il post.
	 */
	public void setPost(Post post) {
		this.post = post;
	}

	/**
	 * Restituisce l'utente che ha creato il commento.
	 *
	 * @return L'utente.
	 */
	public Utente getUtente() {
		return utente;
	}

	/**
	 * Imposta l'utente che ha creato il commento.
	 *
	 * @param utente L'utente.
	 */
	public void setUtente(Utente utente) {
		this.utente = utente;
	}
}
