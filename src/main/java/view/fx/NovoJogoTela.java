package view.fx;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Pede o nome e o gênero do protagonista e começa uma partida nova. */
public class NovoJogoTela {

    private final VBox root = new VBox();

    public NovoJogoTela(Navegador nav) {
        root.getStyleClass().add("tela-menu");
        root.setAlignment(Pos.CENTER);

        VBox painel = new VBox(20);
        painel.getStyleClass().add("painel-conteudo");
        painel.setAlignment(Pos.CENTER);
        painel.setMaxWidth(Region.USE_PREF_SIZE); // o cartão só ocupa o espaço do conteúdo

        Label titulo = new Label("NOVO JOGO");
        titulo.getStyleClass().add("titulo-tela");

        Label perguntaNome = new Label("Qual é o seu nome?");
        perguntaNome.getStyleClass().add("texto-padrao");

        TextField campoNome = new TextField();
        campoNome.setPromptText("Digite o seu nome");
        campoNome.setMaxWidth(320);

        Label perguntaGenero = new Label("Qual o gênero do seu personagem?");
        perguntaGenero.getStyleClass().add("texto-padrao");

        ToggleGroup grupoGenero = new ToggleGroup();
        RadioButton masculino = new RadioButton("Masculino");
        RadioButton feminino = new RadioButton("Feminino");
        masculino.setToggleGroup(grupoGenero);
        feminino.setToggleGroup(grupoGenero);
        masculino.setSelected(true);

        HBox linhaGenero = new HBox(25, masculino, feminino);
        linhaGenero.setAlignment(Pos.CENTER);

        Label erro = new Label();
        erro.getStyleClass().add("texto-erro");

        Button comecar = new Button("Começar");
        comecar.getStyleClass().addAll("botao-menu", "botao-centro");
        comecar.setOnAction(e -> {
            String nome = campoNome.getText().trim();
            if (nome.isEmpty()) {
                erro.setText("Digite um nome para continuar.");
                return;
            }
            String genero = masculino.isSelected() ? "Masculino" : "Feminino";
            nav.getPartida().novoJogo(nome, genero);
            nav.mostrarJogo();
        });

        campoNome.setOnAction(e -> comecar.fire());

        Button voltar = new Button("Voltar");
        voltar.getStyleClass().add("botao-voltar");
        voltar.setOnAction(e -> nav.mostrarMenu());

        VBox blocoNome = new VBox(8, perguntaNome, campoNome);
        blocoNome.setAlignment(Pos.CENTER);
        VBox blocoGenero = new VBox(8, perguntaGenero, linhaGenero);
        blocoGenero.setAlignment(Pos.CENTER);

        painel.getChildren().addAll(titulo, blocoNome, blocoGenero, erro, comecar, voltar);

        root.getChildren().add(painel);
    }

    public Parent getRoot() {
        return root;
    }
}