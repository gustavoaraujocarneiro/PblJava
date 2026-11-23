package view.fx;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MenuTela {

    private final VBox root = new VBox(15);

    public MenuTela(Navegador nav) {
        // 1. Aplica o fundo e o recuo lateral no container principal
        root.getStyleClass().add("tela-menu");
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(0, 0, 0, 80));

        Label titulo = new Label("Incidente Lobo");
        titulo.getStyleClass().add("titulo-jogo");

        Button novo = criarBotao("Novo Jogo");
        Button carregar = criarBotao("Carregar Jogo");
        Button opcoes = criarBotao("Opções");
        Button conquistas = criarBotao("Conquistas");
        Button sair = criarBotao("Sair");

        novo.setOnAction(e -> nav.mostrarNovoJogo());
        carregar.setOnAction(e -> nav.mostrarCarregar());
        opcoes.setOnAction(e -> nav.mostrarOpcoes());
        conquistas.setOnAction(e -> nav.mostrarConquistas());
        sair.setOnAction(e -> Platform.exit());

        root.getChildren().addAll(titulo, novo, carregar, opcoes, conquistas, sair);
    }

    private Button criarBotao(String texto) {
        Button botao = new Button(texto);
        // 3. Todos os botões criados já recebem o estilo do menu
        botao.getStyleClass().add("botao-menu");
        return botao;
    }

    public Parent getRoot() {
        return root;
    }
}