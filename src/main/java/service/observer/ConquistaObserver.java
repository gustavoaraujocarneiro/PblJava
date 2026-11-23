package service.observer;

import model.Cena;
import model.Conquista;
import model.Jogo;

import java.util.Objects;

/** Verifica a conquista a cada cena e avisa (para persistir) só quando o estado dela muda. */
public class ConquistaObserver implements JogoObserver {
    private final Conquista conquista;
    private final Runnable aoMudar;

    public ConquistaObserver(Conquista conquista) {
        this(conquista, () -> { });
    }

    public ConquistaObserver(Conquista conquista, Runnable aoMudar) {
        this.conquista = conquista;
        this.aoMudar = aoMudar;
    }

    @Override
    public void aoEntrarNaCena(Jogo jogo, Cena cena) {
        String antes = conquista.getExplorador();
        conquista.exploradorCheck(jogo);
        if (!Objects.equals(antes, conquista.getExplorador())) {
            aoMudar.run();   // ex.: gravar conquistas.json
        }
    }
}