package service.observer;

import model.Cena;
import model.Conquista;
import model.Jogo;


public class ConquistaObserver implements JogoObserver {
    private final Conquista conquista;

    public ConquistaObserver(Conquista conquista) {
        this.conquista = conquista;
    }

    @Override
    public void aoEntrarNaCena(Jogo jogo, Cena cena) {
        conquista.exploradorCheck(jogo);
    }
}
