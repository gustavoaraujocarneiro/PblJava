package view.fx;

import controller.PartidaController;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.SaveData;

public class CarregarTela {

    private final VBox root = new VBox();

    public CarregarTela(Navegador nav) {
        PartidaController partida = nav.getPartida();

        root.getStyleClass().add("tela-menu");
        root.setAlignment(Pos.CENTER);

        VBox painel = new VBox(12);
        painel.getStyleClass().add("painel-conteudo");
        painel.setAlignment(Pos.CENTER);
        painel.setMaxWidth(Region.USE_PREF_SIZE);

        Label titulo = new Label("CARREGAR JOGO");
        titulo.getStyleClass().add("titulo-tela");

        // Altura mínima fixa: a mensagem de erro aparece sem empurrar os botões
        Label mensagem = new Label();
        mensagem.getStyleClass().add("texto-erro");
        mensagem.setMinHeight(22);

        painel.getChildren().add(titulo);

        String[] slots = {"auto", "1", "2", "3"};
        for (String slot : slots) {
            String rotulo = slot.equals("auto") ? "Automático" : "Slot " + slot;

            SaveData data = partida.lerSlot(slot);
            Button botao = new Button(Formatador.descreverSlot(rotulo, data, partida.slotTemArquivo(slot)));
            botao.getStyleClass().add("botao-slot");
            botao.setDisable(data == null);

            botao.setOnAction(e -> {
                if (partida.carregarSlot(slot)) {
                    nav.mostrarJogo();
                } else {
                    mensagem.setText("Save corrompido ou incompatível.");
                }
            });
            painel.getChildren().add(botao);
        }

        Button voltar = new Button("Voltar");
        voltar.getStyleClass().add("botao-voltar");
        voltar.setOnAction(e -> nav.mostrarMenu());

        painel.getChildren().addAll(mensagem, voltar);
        root.getChildren().add(painel);
    }

    public Parent getRoot() {
        return root;
    }
}