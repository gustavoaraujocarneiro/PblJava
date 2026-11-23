package service.condicao;

import model.Jogo;

public class ConfiancaJorgeAlta implements Condicao {
    private static final int LIMITE = 2; // defina o valor real

    @Override
    public boolean avaliar(Jogo jogo) {
        return jogo.getJorge().getConfianca() >= LIMITE;
    }
}
