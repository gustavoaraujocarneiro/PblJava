package service.regra;

import model.Cena;


public class HubRegra extends RegraEntrada {
    @Override
    protected Cena tratar(Cena cena, ContextoEntrada ctx) {
        String destino = ctx.extras(cena).valor("hub");
        if (destino == null) return cena;

        int numero = ctx.capitulo().getNumero();
        for (int i = 0; i < cena.getOpcoes().size(); i++) {
            if (!ctx.jogo().fezEscolha(ContextoEntrada.chave(numero, cena.getIdCena(), i))) return cena;
        }
        return ctx.cena(Integer.parseInt(destino));
    }
}
