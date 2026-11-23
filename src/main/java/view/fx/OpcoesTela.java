package view.fx;

import controller.PartidaController;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Config;

public class OpcoesTela {

    private final VBox root = new VBox();

    public OpcoesTela(Navegador nav) {
        PartidaController partida = nav.getPartida();
        Config config = partida.getConfig();

        root.getStyleClass().add("tela-menu");
        root.setAlignment(Pos.CENTER);

        VBox painel = new VBox(20);
        painel.getStyleClass().add("painel-conteudo");
        painel.setAlignment(Pos.CENTER);
        painel.setMaxWidth(Region.USE_PREF_SIZE); // o cartão só ocupa o espaço do conteúdo

        Label titulo = new Label("OPÇÕES");
        titulo.getStyleClass().add("titulo-tela");

        CheckBox saveAutomatico = new CheckBox("Save automático");
        saveAutomatico.setSelected("1".equals(config.getsave()));
        saveAutomatico.setOnAction(e -> {
            config.changeSave(saveAutomatico.isSelected() ? "1" : "0");
            partida.salvarConfig();
        });

        CheckBox textoRapido = new CheckBox("Texto rápido (sem animação de digitação)");
        textoRapido.setSelected("1".equals(config.getIntervalo()));
        textoRapido.setOnAction(e -> {
            config.changeIntervalo(textoRapido.isSelected() ? "1" : "0");
            partida.salvarConfig();
        });

        VBox listaOpcoes = new VBox(14, saveAutomatico, textoRapido);
        listaOpcoes.setAlignment(Pos.CENTER_LEFT);
        listaOpcoes.setMaxWidth(420);

        Button voltar = new Button("Voltar");
        voltar.getStyleClass().add("botao-voltar");
        voltar.setOnAction(e -> nav.mostrarMenu());

        painel.getChildren().addAll(titulo, listaOpcoes, voltar);
        root.getChildren().add(painel);
    }

    public Parent getRoot() {
        return root;
    }
}