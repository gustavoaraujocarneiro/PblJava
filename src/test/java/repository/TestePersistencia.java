package repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Config;
import model.SaveData;
import org.junit.jupiter.api.Test;
import service.JogoService;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class TestePersistencia {

    @Test
    public void testeGravarELerConfigJson() {
        SaveRepository saveRepo = new SaveRepository(new ObjectMapper());
        String caminhoTeste = "saves/config_teste.json";

        Config configOriginal = new Config("1", "1", "1");
        boolean gravou = saveRepo.writeFile(caminhoTeste, configOriginal);

        assertTrue(gravou);
        assertTrue(saveRepo.existe(caminhoTeste));

        Config configLida = saveRepo.readFile(caminhoTeste, Config.class, new Config("0", "0", "0"));
        assertEquals("1", configLida.getsave());
        assertEquals("1", configLida.getIntervalo());

        // Limpa o ficheiro temporário criado pelo teste
        new File(caminhoTeste).delete();
    }

    @Test
    public void testeSalvarECarregarPartida() {
        SaveRepository saveRepo = new SaveRepository(new ObjectMapper());
        CapituloRepository capRepo = new CapituloRepository();
        String caminhoSlotTeste = "saves/slot_teste.json";

        // Inicia um jogo e avança uma cena
        JogoService jogoOriginal = new JogoService(capRepo);
        jogoOriginal.newGame("Marina", "Feminino");
        jogoOriginal.processarEscolha(0, jogoOriginal.getJogoAtual().getProtagonista());

        // Grava o save no disco
        SaveData dadosParaSalvar = jogoOriginal.criarSave();
        assertTrue(saveRepo.writeFile(caminhoSlotTeste, dadosParaSalvar));

        // Lê o save do disco e carrega numa nova instância de JogoService
        SaveData dadosLidos = saveRepo.readFile(caminhoSlotTeste, SaveData.class, null);
        assertNotNull(dadosLidos);

        JogoService jogoCarregado = new JogoService(capRepo);
        assertTrue(jogoCarregado.carregarSave(dadosLidos));
        assertEquals("Marina", jogoCarregado.getJogoAtual().getProtagonista().getNome());
        assertEquals(2, jogoCarregado.getJogoAtual().getCenaAtual().getIdCena());

        // Limpa o ficheiro temporário criado pelo teste
        new File(caminhoSlotTeste).delete();
    }
}