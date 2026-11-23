package view.fx;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Conquista;

public class ConquistasTela {

    private final VBox root = new VBox();

    public ConquistasTela(Navegador nav) {
        Conquista conquista = nav.getPartida().getConquista();

        root.getStyleClass().add("tela-menu");
        root.setAlignment(Pos.CENTER);

        VBox painel = new VBox(18);
        painel.getStyleClass().add("painel-conteudo");
        painel.setAlignment(Pos.CENTER);
        painel.setMaxWidth(Region.USE_PREF_SIZE); // o cartão só ocupa o espaço do conteúdo

        Label titulo = new Label("CONQUISTAS");
        titulo.getStyleClass().add("titulo-tela");

        boolean exploradorDesbloqueada = "1".equals(conquista.getExplorador());
        String textoExplorador = exploradorDesbloqueada
                ? "1 - Explorador: visitou todas as salas do apartamento"
                : "1 - ??????";

        Label explorador = criarCardConquista(textoExplorador, exploradorDesbloqueada);

        Button voltar = new Button("Voltar");
        voltar.getStyleClass().add("botao-voltar");
        voltar.setOnAction(e -> nav.mostrarMenu());

        painel.getChildren().addAll(titulo, explorador, voltar);
        root.getChildren().add(painel);
    }

    private Label criarCardConquista(String texto, boolean desbloqueada) {
        Label card = new Label(texto);
        card.setWrapText(true);
        card.getStyleClass().add("card-conquista");
        if (!desbloqueada) {
            card.getStyleClass().add("card-conquista-bloqueada");
        }
        return card;
    }

    public Parent getRoot() {
        return root;
    }
}