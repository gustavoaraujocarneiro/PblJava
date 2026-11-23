package service.condicao;

import model.Jogo;


@FunctionalInterface
public interface Condicao {
    boolean avaliar(Jogo jogo);
}
