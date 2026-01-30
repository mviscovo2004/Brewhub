package it.univaq.brewhub.model;

import java.time.LocalDateTime;

/**
 * Classe che modella un commento effettuato da un utente su un post.
 */
public class Commento {

	/**
	 * Identificativo univoco del commento.
	 */
	private int id;

	/**
	 * L'utente che ha scritto il commento.
	 */
	private Utente utente;

	/**
	 * Il post associato al commento.
	 */
	private Post post;

	/**
	 * Il contenuto testuale del commento.
	 */
	private String contenuto;

	/**
	 * Data e ora in cui il commento è stato creato.
	 * Di default è inizializzato al momento della creazione dell'oggetto.
	 */
	private LocalDateTime dataCreazione = LocalDateTime.now();

	/**
	 * Costruttore predefinito.
	 */
	public Commento() {
	}

	/**
	 * Costruttore per creare un nuovo commento con tutti i dettagli.
	 * 
	 * @param utente        L'autore del commento.
	 * @param post          Il post a cui il commento si riferisce.
	 * @param contenuto     Il testo del commento.
	 * @param dataCreazione La data di creazione. Se null, viene mantenuta quella
	 *                      attuale.
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
	 *
	 * @return L'identificativo del commento.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Imposta l'ID del commento.
	 *
	 * @param id Il nuovo identificativo da assegnare.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Restituisce il contenuto testuale del commento.
	 *
	 * @return Il testo del commento.
	 */
	public String getContenuto() {
		return contenuto;
	}

	/**
	 * Imposta il contenuto testuale del commento.
	 *
	 * @param contenuto Il nuovo testo da assegnare.
	 */
	public void setContenuto(String contenuto) {
		this.contenuto = contenuto;
	}

	/**
	 * Restituisce la data e l'ora di creazione del commento.
	 *
	 * @return La data di creazione.
	 */
	public LocalDateTime getDataCreazione() {
		return dataCreazione;
	}

	/**
	 * Imposta la data e l'ora di creazione del commento.
	 *
	 * @param dataCreazione La nuova data di creazione.
	 */
	public void setDataCreazione(LocalDateTime dataCreazione) {
		this.dataCreazione = dataCreazione;
	}

	/**
	 * Restituisce il post a cui è associato il commento.
	 *
	 * @return L'oggetto Post associato.
	 */
	public Post getPost() {
		return post;
	}

	/**
	 * Imposta il post a cui è associato il commento.
	 *
	 * @param post Il nuovo post da associare.
	 */
	public void setPost(Post post) {
		this.post = post;
	}

	/**
	 * Restituisce l'utente autore del commento.
	 *
	 * @return L'utente che ha scritto il commento.
	 */
	public Utente getUtente() {
		return utente;
	}

	/**
	 * Imposta l'utente autore del commento.
	 *
	 * @param utente Il nuovo autore da assegnare.
	 */
	public void setUtente(Utente utente) {
		this.utente = utente;
	}
}
