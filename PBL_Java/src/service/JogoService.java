package service;

import model.Jogo;
import model.Capitulo;
import model.Cena;
import model.Opcao;
import model.Protagonista;
import repository.CapituloRepository;


public class JogoService {
    private Jogo jogoAtual;
    private CapituloRepository capituloRepository;

    public JogoService(CapituloRepository capituloRepository) {
        this.capituloRepository = capituloRepository;
    }

    public void newGame(String nome, String genero) {
        Protagonista protagonista = new Protagonista(nome, genero);

        Capitulo cap1 = capituloRepository.buscarCapitulo(1); // Busca do banco/repositório

        this.jogoAtual = new Jogo(protagonista, cap1);
        this.jogoAtual.setCenaAtual(cap1.getProximaCena(1)); // Seta a cena inicial
    }

    public Jogo getJogoAtual() {
        return this.jogoAtual;
    }

    public boolean processarEscolha(int indexOpcao) {
        Cena cenaAtual = jogoAtual.getCenaAtual();

        if (indexOpcao >= 0 && indexOpcao < cenaAtual.getOpcoes().size()) {
            Opcao opt = cenaAtual.getOpcoes().get(indexOpcao);

            jogoAtual.getProtagonista().alterarSanidade(opt.getEfeitoSanidade());

            // Aqui métodos para alterar a afinidade dos NPCs
            // jogoAtual.adicionarEscolha(opt.getTextoOpcao()); // Para salvar a escolha

            int idProximaCena = opt.getProximaCenaId();

            if (idProximaCena <= 0) {
                jogoAtual.setCenaAtual(null); // Encerra o jogo
            }
            else {
                jogoAtual.setCenaAtual(jogoAtual.getCapituloAtual().getProximaCena(idProximaCena));
            }

            return true;
        }

        return false;
    }
}
