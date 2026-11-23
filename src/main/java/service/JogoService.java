package service;

import model.*;
import repository.CapituloRepository;
import service.observer.JogoObserver;
import service.regra.CondicaoRegra;
import service.regra.ContextoEntrada;
import service.regra.HubRegra;
import service.regra.ItemRegra;
import service.regra.RegraEntrada;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;


public class JogoService {
    private Jogo jogoAtual;
    private CapituloRepository capituloRepository;

    private final List<JogoObserver> observadores = new ArrayList<>();   // OBSERVER
    private final RegraEntrada regrasDeEntrada = montarRegras();          // CHAIN OF RESPONSIBILITY

    public JogoService(CapituloRepository capituloRepository) {
        this.capituloRepository = capituloRepository;
    }


    private static RegraEntrada montarRegras() {
        RegraEntrada inicio = new CondicaoRegra();
        inicio.depois(new HubRegra()).depois(new ItemRegra());
        return inicio;
    }


    public void adicionarObservador(JogoObserver observador) {
        observadores.add(observador);
    }

    public void removerObservador(JogoObserver observador) {
        observadores.remove(observador);
    }

    private void notificar(Consumer<JogoObserver> evento) {
        for (JogoObserver o : new ArrayList<>(observadores)) {
            try {
                evento.accept(o);
            } catch (RuntimeException e) {
                System.err.println("Observador " + o.getClass().getSimpleName() + " falhou: " + e);
            }
        }
    }

    public void newGame(String nome, String genero) {
        Protagonista protagonista = new Protagonista(nome, genero, "Cesar");

        Capitulo cap1 = capituloRepository.buscarCapitulo(1, protagonista);

        this.jogoAtual = new Jogo(protagonista, cap1);
        entrarNaCena(cap1.getProximaCena(1));
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
        jogo.setCenaAtual(cena); // sem entrarNaCena: item e desvios já foram aplicados antes do save

        Set<String> escolhasDoSave = data.escolhasFeitas();

        if (escolhasDoSave != null) {
            jogo.getEscolhasFeitas().addAll(escolhasDoSave);
        }

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

        // Efeitos
        p.alterarAutoestima(opt.getEfeitoIntimidade());
        p.alterarSanidade(opt.getEfeitoSanidade());
        jogoAtual.getJorge().alterarConfianca(opt.getEfeitoConfianca());

        jogoAtual.adicionarEscolha(ContextoEntrada.chave(numeroCapitulo, cenaAtual.getIdCena(), indexOpcao));
        notificar(o -> o.aoEscolher(jogoAtual, cenaAtual, indexOpcao, opt));

        int idProximaCena = opt.getProximaCenaId();
        int idProximoCapitulo = opt.getProximoCapituloId();

        if (idProximoCapitulo > 0) {
            Capitulo novoCapitulo = capituloRepository.buscarCapitulo(idProximoCapitulo, p);
            if (novoCapitulo == null) {
                encerrarJogo();
                return true;
            }
            jogoAtual.setCapituloAtual(novoCapitulo);
        }

        if (idProximaCena <= 0) {
            encerrarJogo();
        } else {
            entrarNaCena(jogoAtual.getCapituloAtual().getProximaCena(idProximaCena));
        }
        return true;
    }

    private void entrarNaCena(Cena cena) {
        ContextoEntrada contexto = new ContextoEntrada(jogoAtual, capituloRepository, regrasDeEntrada);
        Cena destino = regrasDeEntrada.entrar(cena, contexto);

        if (destino == null) {
            encerrarJogo();
            return;
        }
        jogoAtual.setCenaAtual(destino);
        notificar(o -> o.aoEntrarNaCena(jogoAtual, destino));
    }

    private void encerrarJogo() {
        jogoAtual.setCenaAtual(null);
        notificar(o -> o.aoFimDeJogo(jogoAtual));
    }
}