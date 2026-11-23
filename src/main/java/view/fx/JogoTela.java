package view.fx;

import controller.PartidaController;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import model.Capitulo;
import model.Cena;
import model.Config;
import model.Fala;
import model.Jogo;
import model.Opcao;
import model.Protagonista;
import model.SaveData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JogoTela {

    private final Navegador nav;
    private final PartidaController partida;
    private final Config config;

    // Espaço que o cabeçalho (em cima) e a caixa de diálogo (embaixo) ocupam.
    // Os personagens ficam na faixa livre entre os dois.
    private static final double RESERVA_TOPO = 56;
    private static final double RESERVA_BASE = 245;

    // Camadas visuais (cenário, personagens e item)
    private final StackPane root = new StackPane();
    private final ImageView cenarioView = new ImageView();
    private final ImageView personagemA = new ImageView(); // sozinho = centro; com outro = esquerda
    private final ImageView personagemB = new ImageView(); // direita
    private final HBox personagensBox = new HBox();
    private final Region espacoEntrePersonagens = new Region();
    private String cenarioMostrado = null;                 // evita recarregar o mesmo fundo

    private final VBox itemBox = new VBox(8);              // "Novo item" que aparece ao pegar um objeto
    private final ImageView itemView = new ImageView();
    private final Label itemNomeLabel = new Label();
    private final HBox inventarioBox = new HBox(6);        // ícones dos itens no cabeçalho
    private final Set<String> itensVistos = new HashSet<>(); // itens que o jogador já viu ganhar

    // Componentes da interface
    private final Label capituloLabel = new Label();
    private final Label statusLabel = new Label();
    private final Label textoLabel = new Label();
    private final Label dicaLabel = new Label("(clique na caixa de texto para pular a animação)");
    private final VBox opcoesBox = new VBox(8);
    private final VBox pausaBox = new VBox(14);
    private final Button botaoPausa = new Button("Pausa");

    // Controle do texto gradual
    private Timeline animacao;
    private boolean digitando = false;
    private String textoCompleto = "";
    private Runnable aoTerminar;

    public JogoTela(Navegador nav) {
        this.nav = nav;
        this.partida = nav.getPartida();
        this.config = partida.getConfig();

        // Um save carregado já traz itens: esses não contam como "novo item" na tela
        itensVistos.addAll(partida.getJogo().getProtagonista().getinventario().listarItens());

        montarLayout();
        mostrarCena();
    }

    public Parent getRoot() {
        return root;
    }

    // =====================================================================
    // Montagem do layout (feita uma vez)
    // =====================================================================

    private void montarLayout() {
        root.getStyleClass().add("tela-jogo");

        // --- Camada 1: Cenário ocupando 100% da tela ---
        // A imagem é cortada (viewport) para preencher a janela SEM esticar nem achatar.
        cenarioView.setPreserveRatio(false);
        cenarioView.setSmooth(false); // pixel art: ampliar sem borrar
        cenarioView.fitWidthProperty().bind(root.widthProperty());
        cenarioView.fitHeightProperty().bind(root.heightProperty());
        root.widthProperty().addListener((obs, antigo, novo) -> ajustarCorteDoCenario());
        root.heightProperty().addListener((obs, antigo, novo) -> ajustarCorteDoCenario());

        // --- Camada 2: personagens, na faixa livre entre o cabeçalho e a caixa de diálogo ---
        configurarPersonagem(personagemA);
        configurarPersonagem(personagemB);
        HBox.setHgrow(espacoEntrePersonagens, Priority.ALWAYS);
        personagensBox.setAlignment(Pos.BOTTOM_CENTER);
        personagensBox.setPadding(new Insets(0, 110, 0, 110));
        personagensBox.setMouseTransparent(true); // permite clicar através dos personagens
        StackPane.setMargin(personagensBox, new Insets(RESERVA_TOPO, 0, RESERVA_BASE, 0));

        // --- Camada 3: item recém-encontrado (começa escondido) ---
        itemView.setPreserveRatio(true);
        itemView.setFitHeight(110);
        Label itemTitulo = new Label("NOVO ITEM");
        itemTitulo.getStyleClass().add("item-titulo");
        itemNomeLabel.getStyleClass().add("item-nome");
        itemBox.getChildren().addAll(itemTitulo, itemView, itemNomeLabel);
        itemBox.getStyleClass().add("item-card");
        itemBox.setAlignment(Pos.CENTER);
        itemBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE); // o cartão só ocupa o conteúdo
        itemBox.setMouseTransparent(true);
        itemBox.setVisible(false);
        StackPane.setAlignment(itemBox, Pos.CENTER);
        StackPane.setMargin(itemBox, new Insets(RESERVA_TOPO, 0, RESERVA_BASE, 0));

        // --- Camada 3: Cabeçalho (HUD) ---
        capituloLabel.getStyleClass().add("hud-capitulo");
        statusLabel.getStyleClass().add("hud-status");
        botaoPausa.getStyleClass().add("botao-hud");
        botaoPausa.setOnAction(e -> abrirPausa());

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        inventarioBox.setAlignment(Pos.CENTER);

        HBox cabecalho = new HBox(16, capituloLabel, espaco, inventarioBox, statusLabel, botaoPausa);
        cabecalho.getStyleClass().add("barra-hud");
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        // --- Caixa de diálogo + Opções na parte inferior ---
        textoLabel.setWrapText(true);
        textoLabel.setMaxWidth(Double.MAX_VALUE);
        textoLabel.getStyleClass().add("texto-dialogo");

        VBox caixaTexto = new VBox(textoLabel);
        caixaTexto.setPadding(new Insets(5));

        ScrollPane rolagem = new ScrollPane(caixaTexto);
        rolagem.setFitToWidth(true);
        rolagem.getStyleClass().add("rolagem-dialogo");
        rolagem.setPrefHeight(165);
        rolagem.setMaxHeight(200);
        rolagem.setOnMouseClicked(e -> {
            if (digitando) {
                finalizarTexto();
            }
        });

        // Painel de opções: separado da caixa de texto, para o texto não mudar de tamanho
        // quando as opções aparecem. Começa escondido e só aparece quando há botões.
        opcoesBox.setAlignment(Pos.CENTER);
        opcoesBox.getStyleClass().add("painel-opcoes");
        mostrarPainelOpcoes(false);

        // Dica de "clique para pular": o espaço dela é sempre reservado (só alterna visível/invisível)
        dicaLabel.getStyleClass().add("dica-pular");
        mostrarDica(false);

        // Caixa de diálogo: só o texto da cena e a dica
        VBox caixaDialogo = new VBox(6, rolagem, dicaLabel);
        caixaDialogo.getStyleClass().add("caixa-dialogo");

        // Parte de baixo da tela: opções (em cima) + caixa de diálogo (embaixo)
        VBox areaInferior = new VBox(12, opcoesBox, caixaDialogo);
        BorderPane.setMargin(areaInferior, new Insets(0, 30, 20, 30));

        BorderPane jogo = new BorderPane();
        jogo.setTop(cabecalho);
        jogo.setBottom(areaInferior);

        // --- Camada 4: Painel de pausa (começa invisível) ---
        pausaBox.getStyleClass().add("overlay-pausa");
        pausaBox.setAlignment(Pos.CENTER);
        pausaBox.setMaxWidth(Double.MAX_VALUE);
        pausaBox.setMaxHeight(Double.MAX_VALUE);
        pausaBox.setVisible(false);

        root.getChildren().addAll(cenarioView, personagensBox, itemBox, jogo, pausaBox);
    }

    // =====================================================================
    // Fluxo da cena
    // =====================================================================

    private void mostrarCena() {
        Jogo jogo = partida.getJogo();
        Cena cena = jogo.getCenaAtual();

        itemBox.setVisible(false); // o cartão de item da cena anterior some
        limparOpcoes();

        if (cena == null) {
            atualizarCabecalho(jogo, null);
            pararAnimacao();
            botaoPausa.setDisable(true);
            mostrarPersonagens(new ArrayList<>());
            textoLabel.setText("Fim de jogo!");
            Button voltar = new Button("Voltar ao menu");
            voltar.getStyleClass().addAll("botao-menu", "botao-centro");
            voltar.setOnAction(e -> nav.mostrarMenu());
            opcoesBox.getChildren().add(voltar);
            mostrarPainelOpcoes(true);
            return;
        }

        // Item novo: só aparece no inventário e no cartão quando o texto terminar de aparecer
        String itemDaCena = cena.getItem();
        String itemNovo = (itemDaCena != null && !itensVistos.contains(itemDaCena)) ? itemDaCena : null;

        atualizarCabecalho(jogo, itemNovo);
        atualizarVisuaisDaCena(cena);

        botaoPausa.setDisable(false);
        digitar(montarTexto(cena), () -> {
            mostrarOpcoes(cena);
            revelarItem(itemNovo, jogo);
        });
    }

    // =====================================================================
    // Imagens: cenário, personagens e item
    // =====================================================================

    /** Troca o fundo e os personagens de acordo com o que o historia.txt definiu para a cena. */
    private void atualizarVisuaisDaCena(Cena cena) {
        mostrarCenario(cena.getCenario());

        List<String> nomes = new ArrayList<>();
        for (String nome : cena.getPersonagens()) {
            // só entra quem tem imagem; quem não tem (ex.: o jogador) simplesmente não aparece
            if (Recursos.personagem(nome) != null && !nomes.contains(nome)) {
                nomes.add(nome);
            }
        }
        // Com dois personagens, o detetive fica sempre à esquerda
        if (nomes.size() > 1 && nomes.contains("detetive")) {
            nomes.remove("detetive");
            nomes.add(0, "detetive");
        }
        mostrarPersonagens(nomes);
    }

    private void mostrarCenario(String nome) {
        if (nome == null) {
            cenarioView.setImage(null); // cena sem fundo definido: aparece a cor escura do CSS
            cenarioMostrado = null;
            return;
        }
        if (nome.equals(cenarioMostrado)) {
            return; // mesmo fundo da cena anterior: não recarrega
        }
        cenarioView.setImage(Recursos.cenario(nome)); // null se o arquivo não existir
        cenarioMostrado = nome;
        ajustarCorteDoCenario();
    }

    /**
     * Faz a imagem de fundo "cobrir" a janela sem distorcer: mostra só o pedaço central dela
     * que tem a mesma proporção da janela (como o background-size: cover do CSS da web).
     */
    private void ajustarCorteDoCenario() {
        Image imagem = cenarioView.getImage();
        double largura = root.getWidth();
        double altura = root.getHeight();
        if (imagem == null || largura <= 0 || altura <= 0) {
            cenarioView.setViewport(null);
            return;
        }

        double proporcaoJanela = largura / altura;
        double corteLargura;
        double corteAltura;
        if (imagem.getWidth() / imagem.getHeight() > proporcaoJanela) {
            corteAltura = imagem.getHeight();                 // imagem mais larga: corta os lados
            corteLargura = corteAltura * proporcaoJanela;
        } else {
            corteLargura = imagem.getWidth();                 // imagem mais alta: corta em cima e embaixo
            corteAltura = corteLargura / proporcaoJanela;
        }
        cenarioView.setViewport(new Rectangle2D(
                (imagem.getWidth() - corteLargura) / 2,
                (imagem.getHeight() - corteAltura) / 2,
                corteLargura, corteAltura));
    }

    private void configurarPersonagem(ImageView view) {
        view.setPreserveRatio(true);
        // altura = faixa livre da tela (entre o cabeçalho e a caixa de diálogo), de 120 a 520 px
        view.fitHeightProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(520, Math.max(120, root.getHeight() - RESERVA_TOPO - RESERVA_BASE - 10)),
                root.heightProperty()));
    }

    /** 0 nomes = ninguém; 1 = no centro; 2 = um de cada lado. */
    private void mostrarPersonagens(List<String> nomes) {
        personagensBox.getChildren().clear();
        if (nomes.size() >= 1) {
            personagemA.setImage(Recursos.personagem(nomes.get(0)));
            personagensBox.getChildren().add(personagemA);
        }
        if (nomes.size() >= 2) {
            personagemB.setImage(Recursos.personagem(nomes.get(1)));
            personagensBox.getChildren().addAll(espacoEntrePersonagens, personagemB);
        }
    }

    /** Ao terminar o texto de uma cena que dá um item novo: mostra o cartão e põe o ícone no cabeçalho. */
    private void revelarItem(String item, Jogo jogo) {
        if (item == null) {
            return;
        }
        itensVistos.add(item);
        atualizarCabecalho(jogo, null); // agora o item entra no inventário do cabeçalho

        Image icone = Recursos.item(item);
        itemView.setImage(icone);
        itemView.setVisible(icone != null);
        itemView.setManaged(icone != null);
        itemNomeLabel.setText(item);
        itemBox.setVisible(true);

        FadeTransition aparecer = new FadeTransition(Duration.millis(450), itemBox);
        aparecer.setFromValue(0.0);
        aparecer.setToValue(1.0);
        aparecer.play();
    }

    // =====================================================================
    // Cabeçalho (capítulo, inventário, status)
    // =====================================================================

    /** itemOculto = item que ainda não deve aparecer no inventário (null = mostra todos). */
    private void atualizarCabecalho(Jogo jogo, String itemOculto) {
        Capitulo capitulo = jogo.getCapituloAtual();
        Protagonista p = jogo.getProtagonista();
        capituloLabel.setText("Capítulo " + capitulo.getNumero() + " - " + capitulo.getTitulo());
        statusLabel.setText("Sanidade: " + p.getSanidade() + "   |   Autoestima: " + p.getAutoestima());

        inventarioBox.getChildren().clear();
        for (String item : p.getinventario().listarItens()) {
            if (item.equals(itemOculto)) {
                continue;
            }
            inventarioBox.getChildren().add(criarIconeDoItem(item));
        }
    }

    private StackPane criarIconeDoItem(String item) {
        StackPane slot = new StackPane();
        slot.getStyleClass().add("slot-item");

        Image icone = Recursos.item(item);
        if (icone != null) {
            ImageView view = new ImageView(icone);
            view.setPreserveRatio(true);
            view.setFitHeight(34);
            slot.getChildren().add(view);
        } else {
            slot.getChildren().add(new Label(item)); // item sem imagem: mostra o nome
        }
        Tooltip.install(slot, new Tooltip(item));    // passar o mouse mostra o nome
        return slot;
    }

    private String montarTexto(Cena cena) {
        StringBuilder texto = new StringBuilder(cena.getTextoNarrativo());
        for (Fala fala : cena.getFalas()) {
            texto.append("\n\n").append(fala.getNomePersonagem()).append(": ").append(fala.getTexto());
        }
        return texto.toString();
    }

    private void mostrarOpcoes(Cena cena) {
        opcoesBox.getChildren().clear();
        List<Opcao> opcoes = cena.getOpcoes();

        if (opcoes.size() == 1) {
            // Só "Continuar": um botão ocupando a largura toda
            opcoesBox.getChildren().add(criarBotaoOpcao(opcoes.get(0), 0));
        } else {
            GridPane grade = new GridPane();
            grade.setHgap(8);
            grade.setVgap(8);

            // Duas colunas, cada uma com 50% da largura
            for (int c = 0; c < 2; c++) {
                ColumnConstraints coluna = new ColumnConstraints();
                coluna.setPercentWidth(50);
                grade.getColumnConstraints().add(coluna);
            }

            for (int i = 0; i < opcoes.size(); i++) {
                Button botao = criarBotaoOpcao(opcoes.get(i), i);
                // Se a quantidade for ímpar, o último botão ocupa as duas colunas
                boolean ultimoSozinho = (i == opcoes.size() - 1) && opcoes.size() % 2 == 1;
                // add(nó, coluna, linha, quantas colunas ocupa, quantas linhas ocupa)
                grade.add(botao, i % 2, i / 2, ultimoSozinho ? 2 : 1, 1);
            }
            opcoesBox.getChildren().add(grade);
        }
        mostrarPainelOpcoes(true);
    }

    private Button criarBotaoOpcao(Opcao opcao, int indice) {
        Button botao = new Button(textoDoBotao(opcao));
        botao.getStyleClass().add("botao-escolha");
        botao.setWrapText(true);
        botao.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // preenche a célula; botões da mesma linha ficam com a mesma altura
        botao.setOnAction(e -> {
            partida.escolher(indice);
            mostrarCena();
        });
        return botao;
    }

    private String textoDoBotao(Opcao opcao) {
        String texto = opcao.getTextoOpcao();
        if (texto.toLowerCase().contains("pressione 1")) {
            return "Continuar ▶";
        }
        return texto;
    }

    // O painel de opções só aparece (e só ocupa espaço) quando há botões dentro dele
    private void limparOpcoes() {
        opcoesBox.getChildren().clear();
        mostrarPainelOpcoes(false);
    }

    private void mostrarPainelOpcoes(boolean visivel) {
        opcoesBox.setVisible(visivel);
        opcoesBox.setManaged(visivel); // não gerenciado = não ocupa espaço no layout
    }

    // Só alterna visível/invisível: o espaço da dica continua reservado, a caixa não "pula"
    private void mostrarDica(boolean visivel) {
        dicaLabel.setVisible(visivel);
    }

    // =====================================================================
    // Texto gradual
    // =====================================================================

    private void digitar(String texto, Runnable aoTerminar) {
        pararAnimacao();
        this.textoCompleto = texto;
        this.aoTerminar = aoTerminar;

        if (texto.isEmpty() || !"0".equals(config.getIntervalo())) {
            finalizarTexto();
            return;
        }

        digitando = true;
        textoLabel.setText("");
        mostrarDica(true);

        int[] letras = {0};
        animacao = new Timeline(new KeyFrame(Duration.millis(15), e -> {
            letras[0]++;
            textoLabel.setText(texto.substring(0, letras[0]));
        }));
        animacao.setCycleCount(texto.length());
        animacao.setOnFinished(e -> finalizarTexto());
        animacao.play();
    }

    private void finalizarTexto() {
        pararAnimacao();
        mostrarDica(false);
        textoLabel.setText(textoCompleto);
        aoTerminar.run();
    }

    private void pararAnimacao() {
        if (animacao != null) {
            animacao.stop();
        }
        digitando = false;
    }

    // =====================================================================
    // Pausa e save
    // =====================================================================

    private void abrirPausa() {
        if (digitando && animacao != null) {
            animacao.pause();
        }
        mostrarMenuPausa();
        pausaBox.setVisible(true);
    }

    private void fecharPausa() {
        pausaBox.setVisible(false);
        if (digitando && animacao != null && animacao.getStatus() == Animation.Status.PAUSED) {
            animacao.play();
        }
    }

    private void mostrarMenuPausa() {
        Label titulo = criarTituloPausa("PAUSA");

        Button salvar = criarBotaoPausa("Salvar jogo");
        salvar.setOnAction(e -> mostrarSlotsSalvar(""));

        Button voltar = criarBotaoPausa("Voltar ao jogo");
        voltar.setOnAction(e -> fecharPausa());

        Button sair = criarBotaoPausa("Sair para o menu");
        sair.setOnAction(e -> {
            pararAnimacao();
            nav.mostrarMenu();
        });

        pausaBox.getChildren().setAll(titulo, salvar, voltar, sair);
    }

    private void mostrarSlotsSalvar(String mensagem) {
        Label titulo = criarTituloPausa("SALVAR JOGO");
        pausaBox.getChildren().setAll(titulo);

        for (int i = 1; i <= 3; i++) {
            String slot = String.valueOf(i);
            SaveData data = partida.lerSlot(slot);
            String texto = Formatador.descreverSlot("Slot " + slot, data, partida.slotTemArquivo(slot));

            Button botao = new Button(texto);
            botao.getStyleClass().add("botao-slot");
            botao.setOnAction(e -> salvarNoSlot(slot));
            pausaBox.getChildren().add(botao);
        }

        // Altura mínima fixa: a mensagem aparece sem empurrar os botões
        Label resultado = new Label(mensagem);
        resultado.getStyleClass().add("texto-padrao");
        resultado.setMinHeight(26);

        Button voltar = new Button("Voltar");
        voltar.getStyleClass().add("botao-voltar");
        voltar.setOnAction(e -> mostrarMenuPausa());

        pausaBox.getChildren().addAll(resultado, voltar);
    }

    private void salvarNoSlot(String slot) {
        if (partida.slotTemArquivo(slot)) {
            Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                    "O slot " + slot + " já tem um save. Deseja sobrescrever?",
                    ButtonType.YES, ButtonType.NO);
            alerta.setHeaderText(null);
            ButtonType resposta = alerta.showAndWait().orElse(ButtonType.NO);
            if (resposta != ButtonType.YES) {
                mostrarSlotsSalvar("Save cancelado.");
                return;
            }
        }

        if (partida.salvarNoSlot(slot)) {
            mostrarSlotsSalvar("Jogo salvo no slot " + slot + "!");
        } else {
            mostrarSlotsSalvar("Falha ao salvar.");
        }
    }

    private Label criarTituloPausa(String texto) {
        Label titulo = new Label(texto);
        titulo.getStyleClass().add("titulo-tela");
        return titulo;
    }

    private Button criarBotaoPausa(String texto) {
        Button botao = new Button(texto);
        botao.getStyleClass().addAll("botao-menu", "botao-largo", "botao-centro");
        return botao;
    }
}