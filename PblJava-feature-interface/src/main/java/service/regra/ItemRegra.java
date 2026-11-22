package service.regra;

import model.Cena;
import model.Inventario;

public class ItemRegra extends RegraEntrada {
    @Override
    protected Cena tratar(Cena cena, ContextoEntrada ctx) {
        String item = ctx.extras(cena).valor("item");
        if (item != null) {
            Inventario inv = ctx.jogo().getProtagonista().getinventario();
            if (!inv.listarItens().contains(item)) inv.adicionarItem(item);
        }
        return cena;
    }
}
