package it.univaq.brewhub;

import java.time.LocalDateTime;

/**
 * Rappresenta un commento lasciato da un utente su un post.
 */
public class Commento {

	/** Identificativo univoco del commento. */
	private int id; 

	/** L'autore del commento. */
	private Utente utente;

	/** Il post a cui il commento si riferisce. */
	private Post post;

	/** Il testo del commento. */
	private String contenuto;

	/** Data e ora di creazione. */
	private LocalDateTime dataCreazione = LocalDateTime.now();

	/**
	 * Costruttore vuoto.
	 */
	public Commento() {
	}

	/**
	 * Costruttore completo.
	 * 
	 * @param utente        L'autore.
	 * @param post          Il post target.
	 * @param contenuto     Il testo del commento.
	 * @param dataCreazione La data di creazione (opzionale, se null usa now()).
	 */
	public Commento(Utente utente, Post post, String contenuto, LocalDateTime dataCreazione) {
		this.contenuto = contenuto;
		this.utente = utente;
		this.post = post;
		if (dataCreazione != null) {
			this.dataCreazione = dataCreazione;
		}
	}

	/**
	 * Restituisce l'ID del commento.
	 * @return L'identificativo.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Imposta l'ID del commento.
	 * @param id Il nuovo ID.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Restituisce il contenuto del commento.
	 * @return Il testo.
	 */
	public String getContenuto() {
		return contenuto;
	}

	/**
	 * Imposta il contenuto del commento.
	 * @param contenuto Il nuovo testo.
	 */
	public void setContenuto(String contenuto) {
		this.contenuto = contenuto;
	}

	/**
	 * Restituisce la data di creazione.
	 * @return {@link LocalDateTime} di creazione.
	 */
	public LocalDateTime getDataCreazione() {
		return dataCreazione;
	}

	/**
	 * Imposta la data di creazione.
	 * @param dataCreazione La nuova data.
	 */
	public void setDataCreazione(LocalDateTime dataCreazione) {
		this.dataCreazione = dataCreazione;
	}

	/**
	 * Restituisce il post associato.
	 * @return L'oggetto {@link Post}.
	 */
	public Post getPost() {
		return post;
	}

	/**
	 * Imposta il post associato.
	 * @param post Il nuovo post.
	 */
	public void setPost(Post post) {
		this.post = post;
	}

	/**
	 * Restituisce l'autore del commento.
	 * @return L'oggetto {@link Utente}.
	 */
	public Utente getUtente() {
		return utente;
	}

	/**
	 * Imposta l'autore del commento.
	 * @param utente Il nuovo autore.
	 */
	public void setUtente(Utente utente) {
		this.utente = utente;
	}
}
