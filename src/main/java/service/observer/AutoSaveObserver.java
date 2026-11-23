package service.observer;

import model.Cena;
import model.Jogo;


public class AutoSaveObserver implements JogoObserver {
    private final Runnable salvar;

    public AutoSaveObserver(Runnable salvar) {
        this.salvar = salvar;
    }

    @Override
    public void aoEntrarNaCena(Jogo jogo, Cena cena) {
        salvar.run();
    }
}
