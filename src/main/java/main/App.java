package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.PartidaController;
import javafx.application.Application;
import javafx.stage.Stage;
import model.Config;
import model.Conquista;
import repository.CapituloRepository;
import repository.SaveRepository;
import service.JogoService;
import view.fx.Navegador;


public class App extends Application {

    // O JavaFX chama este método quando o programa abre. Ele substitui o main() para a interface.
    @Override
    public void start(Stage stage) {
        // Mesma montagem que a Main fazia
        ObjectMapper mapper = new ObjectMapper();
        SaveRepository save = new SaveRepository(mapper);
        Config config = save.readFile("saves/config.json", Config.class, new Config("0", "0", "0"));
        Conquista conquista = save.readFile("saves/conquistas.json", Conquista.class, new Conquista("0"));

        CapituloRepository capituloRepo = new CapituloRepository();
        JogoService jogoService = new JogoService(capituloRepo);
        Observadores.registrar(jogoService, conquista, save, config);
        PartidaController partida = new PartidaController(jogoService, save, config, conquista);

        Navegador navegador = new Navegador(stage, partida);
        navegador.mostrarMenu();

        stage.setTitle("Jogo Interativo");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args); // inicia o JavaFX, que depois chama start()
    }
}