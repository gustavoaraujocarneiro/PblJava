package view.fx;

import controller.PartidaController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Capitulo;
import model.Cena;
import model.Config;
import model.Fala;
import model.Jogo;
import model.Opcao;
import model.Protagonista;
import model.SaveData;
import java.io.InputStream;
import java.util.List;

public class JogoTela {

    private final Navegador nav;
    private final PartidaController partida;
    private final Config config;

    // Camadas visuais (Cenário e Sprite)
    private final StackPane root = new StackPane();
    private final ImageView cenarioView = new ImageView();
    private final ImageView spriteView = new ImageView();

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
        cenarioView.setPreserveRatio(false);
        cenarioView.fitWidthProperty().bind(root.widthProperty());
        cenarioView.fitHeightProperty().bind(root.heightProperty());

        // --- Camada 2: Sprite do personagem apoiada na base da tela ---
        spriteView.setPreserveRatio(true);
        spriteView.setFitHeight(480);
        HBox spriteBox = new HBox(spriteView);
        spriteBox.setAlignment(Pos.BOTTOM_CENTER);
        spriteBox.setMouseTransparent(true); // permite clicar através da sprite

        // --- Camada 3: Cabeçalho (HUD) ---
        capituloLabel.getStyleClass().add("hud-capitulo");
        statusLabel.getStyleClass().add("hud-status");
        botaoPausa.getStyleClass().add("botao-hud");
        botaoPausa.setOnAction(e -> abrirPausa());

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        HBox cabecalho = new HBox(16, capituloLabel, espaco, statusLabel, botaoPausa);
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

        root.getChildren().addAll(cenarioView, spriteBox, jogo, pausaBox);
    }

    // =====================================================================
    // Fluxo da cena
    // =====================================================================

    private void mostrarCena() {
        Jogo jogo = partida.getJogo();
        Cena cena = jogo.getCenaAtual();

        atualizarCabecalho(jogo);
        limparOpcoes();

        if (cena == null) {
            pararAnimacao();
            botaoPausa.setDisable(true);
            spriteView.setImage(null);
            textoLabel.setText("Fim de jogo!");
            Button voltar = new Button("Voltar ao menu");
            voltar.getStyleClass().addAll("botao-menu", "botao-centro");
            voltar.setOnAction(e -> nav.mostrarMenu());
            opcoesBox.getChildren().add(voltar);
            mostrarPainelOpcoes(true);
            return;
        }

        // Atualiza os visuais da cena (adapte os getters se o seu model Cena já tiver caminhos de imagem)
        atualizarVisuaisDaCena(jogo, cena);

        botaoPausa.setDisable(false);
        digitar(montarTexto(cena), () -> mostrarOpcoes(cena));
    }

    /**
     * Carrega o cenário e a sprite do personagem atual.
     * Caso o arquivo ainda não exista em resources, ignora sem travar o jogo.
     */
    private void atualizarVisuaisDaCena(Jogo jogo, Cena cena) {
        // Exemplo: busca "/images/backgrounds/cap1.jpg" ou um caminho vindo do model Cena
        String caminhoCenario = "/images/backgrounds/cap" + jogo.getCapituloAtual().getNumero() + ".jpg";
        carregarImagemSegura(cenarioView, caminhoCenario);

        // Se houver fala na cena, tenta carregar a sprite correspondente ao nome do personagem
        List<Fala> falas = cena.getFalas();
        if (!falas.isEmpty()) {
            String personagem = falas.get(0).getNomePersonagem().toLowerCase().trim();
            String caminhoSprite = "/images/sprites/" + personagem + ".png";
            carregarImagemSegura(spriteView, caminhoSprite);
        } else {
            spriteView.setImage(null);
        }
    }

    private void carregarImagemSegura(ImageView destino, String caminhoResource) {
        InputStream stream = getClass().getResourceAsStream(caminhoResource);
        if (stream != null) {
            destino.setImage(new Image(stream));
        }
    }

    private void atualizarCabecalho(Jogo jogo) {
        Capitulo capitulo = jogo.getCapituloAtual();
        Protagonista p = jogo.getProtagonista();
        capituloLabel.setText("Capítulo " + capitulo.getNumero() + " - " + capitulo.getTitulo());
        statusLabel.setText("Sanidade: " + p.getSanidade() + "   |   Autoestima: " + p.getAutoestima());
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

        for (int i = 0; i < opcoes.size(); i++) {
            int indice = i;
            Button botao = new Button(textoDoBotao(opcoes.get(i)));
            botao.getStyleClass().add("botao-escolha");
            botao.setWrapText(true);
            botao.setMaxWidth(Double.MAX_VALUE);
            botao.setOnAction(e -> {
                partida.escolher(indice);
                mostrarCena();
            });
            opcoesBox.getChildren().add(botao);
        }
        mostrarPainelOpcoes(true);
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