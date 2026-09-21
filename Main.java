import view.MenuView;
import view.TerminalView;
import repository.CapituloRepository;
import service.JogoService;
import controller.JogoController;
import controller.MenuController;


public class Main {

    public static void main(String[] args) {
        TerminalView terminalView = new TerminalView();
        MenuView menuView = new MenuView();

        CapituloRepository capituloRepo = new CapituloRepository();

        JogoService jogoService = new JogoService(capituloRepo);

        JogoController jogoController = new JogoController(terminalView, jogoService);
        MenuController menuController = new MenuController(menuView, jogoService, jogoController);

        menuController.startMenu();
    }
}
