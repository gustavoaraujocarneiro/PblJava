package service.condicao;

import model.Jogo;

import java.util.HashMap;
import java.util.Map;

/** Registro que liga o nome usado no txt à estratégia correspondente. */
public final class Condicoes {
    private static final Map<String, Condicao> REGISTRO = new HashMap<>();

    static {
        registrar("confianca_jorge_alta", new service.condicao.ConfiancaJorgeAlta());
        // Novas condições entram aqui, sem mexer no resto. Como Condicao é funcional, pode ser lambda:
        // registrar("sanidade_baixa", j -> j.getProtagonista().getSanidade() < 50);
    }

    private Condicoes() { }

    public static void registrar(String nome, Condicao condicao) {
        REGISTRO.put(nome, condicao);
    }

    public static boolean avaliar(String nome, Jogo jogo) {
        Condicao c = REGISTRO.get(nome);
        if (c == null) throw new IllegalArgumentException("Condição desconhecida no txt: " + nome);
        return c.avaliar(jogo);
    }
}
