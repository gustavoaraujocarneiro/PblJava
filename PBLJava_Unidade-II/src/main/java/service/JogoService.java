package service;

import model.*;
import repository.CapituloRepository;
import repository.CapituloRepository.CenaExtras;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class JogoService {
    private static final int CONFIANCA_ALTA = 2; // limite de "/condicao confianca_jorge_alta" (defina o valor real)

    private Jogo jogoAtual;
    private CapituloRepository capituloRepository;

    public JogoService(CapituloRepository capituloRepository) {
        this.capituloRepository = capituloRepository;
    }

    public void newGame(String nome, String genero) {
        Protagonista protagonista = new Protagonista(nome, genero, "Cesar"); // ALTERADO: antes era ""

        Capitulo cap1 = capituloRepository.buscarCapitulo(1, protagonista);

        this.jogoAtual = new Jogo(protagonista, cap1);
        entrarNaCena(cap1.getProximaCena(1)); // ALTERADO: aplica /condicao, /hub e /item da cena inicial
    }

    public Jogo getJogoAtual() {
        return this.jogoAtual;
    }

    public SaveData criarSave() {
        Protagonista p = jogoAtual.getProtagonista();
        return new SaveData(p.getNome(), p.getGenero(), p.getNomeDetetive(),
                p.getSanidade(), p.getAutoestima(),
                new ArrayList<>(p.getinventario().listarItens()),
                jogoAtual.getCapituloAtual().getNumero(),
                jogoAtual.getCenaAtual().getIdCena(),
                new HashSet<>(jogoAtual.getEscolhasFeitas()),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                jogoAtual.getJorge().getConfianca());
    }

    public boolean carregarSave(SaveData data) {
        if (data == null || data.nome() == null || data.nome().isBlank()
                || data.genero() == null || data.genero().isBlank()) {
            return false;
        }
        try {
            return montarJogo(data);
        } catch (RuntimeException e) {
            System.err.println("Erro ao restaurar o save: " + e.getMessage());
            return false;
        }
    }

    public boolean montarJogo(SaveData data) {

        Protagonista p = new Protagonista(data.nome(), data.genero(), data.nomeDetetive());
        p.setSanidade(data.sanidade());
        p.setAutoestima(data.autoestima());

        if (data.itens() != null) {
            for (String item : data.itens()) {
                p.getinventario().adicionarItem(item);
            }
        }

        Capitulo cap = capituloRepository.buscarCapitulo(data.capituloId(), p);
        if (cap == null)
            return false;

        Cena cena = cap.getProximaCena(data.cenaId());
        if (cena == null)
            return false;

        Jogo jogo = new Jogo(p, cap);
        jogo.setCenaAtual(cena);

        Set<String> escolhasDoSave = data.escolhasFeitas();

        if (escolhasDoSave != null) {
            jogo.getEscolhasFeitas().addAll(escolhasDoSave);
        }

        jogo.getJorge().alterarConfianca(data.confiancaJorge());

        this.jogoAtual = jogo;
        return true;
    }

    public boolean processarEscolha(int indexOpcao, Protagonista protagonista) {
        Cena cenaAtual = jogoAtual.getCenaAtual();

        if (cenaAtual == null || indexOpcao < 0 || indexOpcao >= cenaAtual.getOpcoes().size()) {
            return false;
        }

        Opcao opt = cenaAtual.getOpcoes().get(indexOpcao);
        Protagonista p = jogoAtual.getProtagonista();
        int numeroCapitulo = jogoAtual.getCapituloAtual().getNumero();

        p.alterarAutoestima(opt.getEfeitoIntimidade());
        p.alterarSanidade(opt.getEfeitoSanidade());
        jogoAtual.getJorge().alterarConfianca(opt.getEfeitoConfianca());


        jogoAtual.adicionarEscolha(chave(numeroCapitulo, cenaAtual.getIdCena(), indexOpcao));

        int idProximaCena = opt.getProximaCenaId();
        int idProximoCapitulo = opt.getProximoCapituloId();

        if (idProximoCapitulo > 0) {
            Capitulo novoCapitulo = capituloRepository.buscarCapitulo(idProximoCapitulo, p);
            if (novoCapitulo == null) {
                jogoAtual.setCenaAtual(null);
                return true;
            }
            jogoAtual.setCapituloAtual(novoCapitulo);
        }

        if (idProximaCena <= 0) {
            jogoAtual.setCenaAtual(null); // Encerra o jogo
        } else {
            entrarNaCena(jogoAtual.getCapituloAtual().getProximaCena(idProximaCena));
        }
        return true;
    }
    private void entrarNaCena(Cena cena) {
        Capitulo capitulo = jogoAtual.getCapituloAtual();
        int numeroCapitulo = capitulo.getNumero();

        while (cena != null) {
            CenaExtras ex = capituloRepository.getExtras(numeroCapitulo, cena.getIdCena());

            String condicao = ex.valor("condicao");
            if (condicao != null && !condicaoVerdadeira(condicao)) {
                cena = capitulo.getProximaCena(cena.getOpcoes().get(0).getProximaCenaId());
                continue;
            }

            String hub = ex.valor("hub");
            if (hub != null && hubCompleto(numeroCapitulo, cena)) {
                cena = capitulo.getProximaCena(Integer.parseInt(hub));
                continue;
            }
            break;
        }

        jogoAtual.setCenaAtual(cena);

        if (cena != null) {
            String item = capituloRepository.getExtras(numeroCapitulo, cena.getIdCena()).valor("item");
            Inventario inventario = jogoAtual.getProtagonista().getinventario();
            if (item != null && !inventario.listarItens().contains(item)) {
                inventario.adicionarItem(item);
            }
        }
    }

    private boolean hubCompleto(int numeroCapitulo, Cena cena) {
        for (int i = 0; i < cena.getOpcoes().size(); i++) {
            if (!jogoAtual.fezEscolha(chave(numeroCapitulo, cena.getIdCena(), i))) return false;
        }
        return true;
    }

    private boolean condicaoVerdadeira(String condicao) {
        if (condicao.equals("confianca_jorge_alta")) return jogoAtual.getJorge().getConfianca() >= CONFIANCA_ALTA;
        return false;
    }

    private String chave(int capitulo, int idCena, int indiceOpcao) {
        return capitulo + ":" + idCena + ":" + indiceOpcao;
    }
}