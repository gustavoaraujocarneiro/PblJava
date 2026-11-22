package service.regra;

import model.Capitulo;
import model.Cena;
import model.Jogo;
import repository.CapituloRepository;
import repository.CapituloRepository.CenaExtras;


public class ContextoEntrada {
    private static final int LIMITE_DESVIOS = 50;

    private final Jogo jogo;
    private final CapituloRepository repositorio;
    private final RegraEntrada inicioDaCadeia;
    private int desvios = 0;

    public ContextoEntrada(Jogo jogo, CapituloRepository repositorio, RegraEntrada inicioDaCadeia) {
        this.jogo = jogo;
        this.repositorio = repositorio;
        this.inicioDaCadeia = inicioDaCadeia;
    }

    public Jogo jogo() { return jogo; }

    public Capitulo capitulo() { return jogo.getCapituloAtual(); }

    public Cena cena(int idCena) { return capitulo().getProximaCena(idCena); }

    public CenaExtras extras(Cena cena) { return repositorio.getExtras(capitulo().getNumero(), cena.getIdCena()); }

    /** Chave usada em escolhasFeitas: "capitulo:cena:opcao". */
    public static String chave(int capitulo, int idCena, int indiceOpcao) {
        return capitulo + ":" + idCena + ":" + indiceOpcao;
    }

    /** Quando uma regra desvia para outra cena, a cadeia recomeça por ela. */
    Cena recomecar(Cena cena) {
        if (++desvios > LIMITE_DESVIOS) throw new IllegalStateException("Loop de desvios no txt (cena " + (cena == null ? "?" : cena.getIdCena()) + ")");
        return inicioDaCadeia.entrar(cena, this);
    }
}
