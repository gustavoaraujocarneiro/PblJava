package service.observer;

import model.Cena;
import model.Jogo;
import model.Opcao;


public interface JogoObserver {

    // Disparado depois que uma cena passou a ser a cena atual
    default void aoEntrarNaCena(Jogo jogo, Cena cena) { }

    //Disparado quando o jogador escolhe uma opção
    default void aoEscolher(Jogo jogo, Cena cena, int indiceOpcao, Opcao opcao) { }

    //Disparado quando a história termina
    default void aoFimDeJogo(Jogo jogo) { }
}
