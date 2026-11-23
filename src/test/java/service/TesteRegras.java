package service;

import model.Jogo;
import model.Protagonista;
import org.junit.jupiter.api.Test;
import repository.CapituloRepository;

import static org.junit.jupiter.api.Assertions.*;

public class TesteRegras {

    @Test
    public void testeIniciarNovoJogo() {
        CapituloRepository repo = new CapituloRepository();
        JogoService service = new JogoService(repo);

        service.newGame("Lucas", "Masculino");
        Jogo jogo = service.getJogoAtual();

        assertNotNull(jogo);
        assertEquals("Lucas", jogo.getProtagonista().getNome());
        assertEquals(1, jogo.getCapituloAtual().getNumero());
        assertEquals(1, jogo.getCenaAtual().getIdCena());
    }

    @Test
    public void testeAvancarCenaEAlterarAtributos() {
        CapituloRepository repo = new CapituloRepository();
        JogoService service = new JogoService(repo);
        service.newGame("Lucas", "Masculino");

        Jogo jogo = service.getJogoAtual();
        Protagonista protagonista = jogo.getProtagonista();

        int autoestimaInicial = protagonista.getAutoestima();
        int sanidadeInicial = protagonista.getSanidade();
        int confiancaInicial = jogo.getJorge().getConfianca();

        // Avança da Cena 1 para a Cena 2 escolhendo a opção 0
        boolean escolheu = service.processarEscolha(0, protagonista);

        assertTrue(escolheu);
        assertEquals(2, jogo.getCenaAtual().getIdCena());

        // Na opção 0 da Cena 1, os efeitos são 0|0|0, então os atributos se mantêm
        assertEquals(autoestimaInicial, protagonista.getAutoestima());
        assertEquals(sanidadeInicial, protagonista.getSanidade());
        assertEquals(confiancaInicial, jogo.getJorge().getConfianca());

        // Testa alteração real nos atributos aplicando efeitos diretamente
        protagonista.alterarAutoestima(-1);
        protagonista.alterarSanidade(-1);
        jogo.getJorge().alterarConfianca(-1);

        assertEquals(autoestimaInicial - 1, protagonista.getAutoestima());
        assertEquals(sanidadeInicial - 1, protagonista.getSanidade());
        assertEquals(confiancaInicial - 1, jogo.getJorge().getConfianca());
    }
}
