package it.univaq.brewhub.dao;

import it.univaq.brewhub.Gruppo;
import java.util.List;

public interface GruppoDAO {
    int createGruppo(String nome, String creatore, List<String> membri);

    Gruppo getGruppo(int id);

    List<Gruppo> getGruppiUtente(String username);

    void addMembro(int idGruppo, String username);

    void removeMembro(int idGruppo, String username);
}
