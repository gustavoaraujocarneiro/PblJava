package controller;

import model.Config;
import model.Conquista;
import model.Jogo;
import model.SaveData;
import repository.SaveRepository;
import service.JogoService;

/**
 * Reúne as operações do jogo que as telas JavaFX precisam chamar.
 *
 * IMPORTANTE: esta classe NÃO importa nada do JavaFX e NÃO usa System.out/Scanner.
 * Ela só decide e devolve resultados (true/false, objetos). Quem mostra algo na tela
 * é sempre a view. É isso que mantém a separação do MVC.
 */
public class PartidaController {

    private final JogoService jogoService;
    private final SaveRepository save;
    private final Config config;
    private final Conquista conquista;

    public PartidaController(JogoService jogoService, SaveRepository save,
                             Config config, Conquista conquista) {
        this.jogoService = jogoService;
        this.save = save;
        this.config = config;
        this.conquista = conquista;
    }

    // ---------- Acesso aos dados ----------

    public Jogo getJogo() {
        return jogoService.getJogoAtual();
    }

    public Config getConfig() {
        return config;
    }

    public Conquista getConquista() {
        return conquista;
    }

    // ---------- Início de partida ----------

    public void novoJogo(String nome, String genero) {
        jogoService.newGame(nome, genero);
        conquista.setVisit(); // antes isso ficava no início do iniciarJogo()
    }

    /** Carrega o save do slot ("auto", "1", "2" ou "3"). Devolve false se não deu certo. */
    public boolean carregarSlot(String slot) {
        SaveData data = lerSlot(slot);
        if (data == null || !jogoService.carregarSave(data)) {
            return false;
        }
        conquista.setVisit();
        return true;
    }

    // ---------- Durante o jogo ----------

    /**
     * Faz o que o laço while do JogoController fazia a cada volta:
     * processa a escolha, confere a conquista, grava e faz o save automático.
     * Devolve false se o índice da opção for inválido.
     */
    public boolean escolher(int indiceOpcao) {
        Jogo jogo = jogoService.getJogoAtual();

        boolean valida = jogoService.processarEscolha(indiceOpcao, jogo.getProtagonista());
        if (!valida) {
            return false;
        }

        conquista.exploradorCheck(jogo);
        save.writeFile("saves/conquistas.json", conquista);

        if (jogo.getCenaAtual() != null && "1".equals(config.getsave())) {
            save.writeFile(JogoController.caminhoSlot("auto"), jogoService.criarSave());
        }
        return true;
    }

    // ---------- Saves ----------

    /** Salva a partida atual no slot informado. Devolve false se falhar. */
    public boolean salvarNoSlot(String slot) {
        return save.writeFile(JogoController.caminhoSlot(slot), jogoService.criarSave());
    }

    /** Existe um arquivo para esse slot? (pode estar corrompido) */
    public boolean slotTemArquivo(String slot) {
        return save.existe(JogoController.caminhoSlot(slot));
    }

    /** Lê o save do slot. Devolve null se o slot estiver vazio ou o arquivo estiver corrompido. */
    public SaveData lerSlot(String slot) {
        String caminho = JogoController.caminhoSlot(slot);
        if (!save.existe(caminho)) {
            return null;
        }
        return save.readFile(caminho, SaveData.class, null);
    }

    // ---------- Configurações ----------

    public void salvarConfig() {
        save.writeFile("saves/config.json", config);
    }
}
