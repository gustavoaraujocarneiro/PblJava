package service.regra;

import model.Cena;
import service.condicao.Condicoes;


public class CondicaoRegra extends service.regra.RegraEntrada {
    @Override
    protected Cena tratar(Cena cena, service.regra.ContextoEntrada ctx) {
        String nome = ctx.extras(cena).valor("condicao");
        if (nome == null || Condicoes.avaliar(nome, ctx.jogo())) return cena;
        return ctx.cena(cena.getOpcoes().get(0).getProximaCenaId());
    }
}
