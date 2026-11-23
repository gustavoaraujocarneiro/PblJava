package service.observer;

import model.Cena;
import model.Jogo;

/** Salva automaticamente ao entrar numa cena, a partir da primeira escolha feita. */
public class AutoSaveObserver implements JogoObserver {
    private final Runnable salvar;

    public AutoSaveObserver(Runnable salvar) {
        this.salvar = salvar;
    }

    @Override
    public void aoEntrarNaCena(Jogo jogo, Cena cena) {
        if (jogo.getEscolhasFeitas().isEmpty()) return;   // cena inicial: ainda não houve escolha
        salvar.run();
    }
}