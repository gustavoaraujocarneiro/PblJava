package controller;

import view.MenuView;
import service.JogoService;


public class MenuController {
    private MenuView menuView;
    private JogoService jogoService;
    private JogoController jogoController;

    public MenuController(MenuView menuView, JogoService jogoService, JogoController jogoController) {
        this.menuView = menuView;
        this.jogoService = jogoService;
        this.jogoController = jogoController;
    }

    public void startMenu() {
        menuView.showMenu();
        String option = menuView.readOption();

        switch(option) {
            case "1":
                String nome = menuView.pedirNome();
                String genero = menuView.pedirGenero();

                if (genero.equals("1")){
                    genero = "Masculino";
                }
                else {
                    genero = "Feminino";
                }

                jogoService.newGame(nome, genero);
                jogoController.iniciarJogo();
                break;

            case "2":
                //Exibe créditos
                break;

            case "3":
                //Encerra o jogo
                break;
        }
    }
}
