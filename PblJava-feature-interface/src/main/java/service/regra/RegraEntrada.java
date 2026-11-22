package service.regra;

import model.Cena;


public abstract class RegraEntrada {
    private RegraEntrada proxima;

    public RegraEntrada depois(RegraEntrada proxima) {
        this.proxima = proxima;
        return proxima;
    }

    public final Cena entrar(Cena cena, ContextoEntrada ctx) {
        if (cena == null) return null;
        Cena resultado = tratar(cena, ctx);
        if (resultado != cena) return ctx.recomecar(resultado);   // desvio (ou null = fim)
        return proxima == null ? cena : proxima.entrar(cena, ctx);
    }

    protected abstract Cena tratar(Cena cena, ContextoEntrada ctx);
}
