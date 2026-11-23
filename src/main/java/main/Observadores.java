package main;

import controller.JogoController;
import model.Config;
import model.Conquista;
import repository.SaveRepository;
import service.JogoService;
import service.observer.AutoSaveObserver;
import service.observer.ConquistaObserver;


public final class Observadores {
    private Observadores() { }

    public static void registrar(JogoService jogoService, Conquista conquista, SaveRepository save, Config config) {
        jogoService.adicionarObservador(new ConquistaObserver(conquista,
                () -> save.writeFile("saves/conquistas.json", conquista)));

        jogoService.adicionarObservador(new AutoSaveObserver(() -> {
            if ("1".equals(config.getsave())) {
                save.writeFile(JogoController.caminhoSlot("auto"), jogoService.criarSave());
            }
        }));
    }
}