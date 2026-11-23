package view.fx;

import controller.PartidaController;
import java.util.Objects;
import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.text.Font;

public class Navegador {

    private final Scene scene;
    private final PartidaController partida;

    public Navegador(Stage stage, PartidaController partida) {
        this.partida = partida;
        this.scene = new Scene(new StackPane(), 1000, 650);

        Font.loadFont(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/fonts/SpecialElite-Regular.ttf"),
                        "Fonte SpecialElite-Regular.ttf não encontrada!"
                ),
                48
        );

        // Carrega os dois arquivos CSS na Scene (valem para todas as telas)
        String cssMenu = Objects.requireNonNull(
                getClass().getResource("/css/menu.css"),
                "Arquivo /css/menu.css não encontrado!"
        ).toExternalForm();

        String cssJogo = Objects.requireNonNull(
                getClass().getResource("/css/jogo.css"),
                "Arquivo /css/jogo.css não encontrado!"
        ).toExternalForm();

        this.scene.getStylesheets().addAll(cssMenu, cssJogo);

        stage.setScene(scene);
        stage.setMinWidth(820);
        stage.setMinHeight(560);
    }

    public PartidaController getPartida() {
        return partida;
    }

    private void trocar(Parent novoRoot) {
        novoRoot.setOpacity(0.0);
        scene.setRoot(novoRoot);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), novoRoot);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public void mostrarMenu() {
        trocar(new MenuTela(this).getRoot());
    }

    public void mostrarNovoJogo() {
        trocar(new NovoJogoTela(this).getRoot());
    }

    public void mostrarJogo() {
        trocar(new JogoTela(this).getRoot());
    }

    public void mostrarCarregar() {
        trocar(new CarregarTela(this).getRoot());
    }

    public void mostrarOpcoes() {
        trocar(new OpcoesTela(this).getRoot());
    }

    public void mostrarConquistas() {
        trocar(new ConquistasTela(this).getRoot());
    }
}