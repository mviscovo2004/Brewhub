package it.univaq.brewhub;

import java.time.LocalDateTime;

public class Commento {
	private Utente utente;
	private Post post;
	private String contenuto;
	private LocalDateTime dataCreazione=LocalDateTime.now();
	
	
	
	public Commento() {
		
	}
	
	public Commento(Utente utente,Post post,String contenuto,LocalDateTime dataCreazione) {
		this.contenuto=contenuto;
		this.utente=utente;
		this.post=post;
		this.dataCreazione=dataCreazione;
	}
	
	public String getContenuto() {
		return contenuto;
	}
	
	public LocalDateTime getDataCreazione() {
		return dataCreazione;
	}
	
	public Post getPost() {
		return post;
	}
	
	public Utente getUtente() {
		return utente;
	}
	
	public void setContenuto(String contenuto) {
		this.contenuto = contenuto;
	}
	
	public void setDataCreazione(LocalDateTime dataCreazione) {
		this.dataCreazione = dataCreazione;
	}
	
	public void setPost(Post post) {
		this.post = post;
	}
	
	public void setUtente(Utente utente) {
		this.utente = utente;
	}
}
