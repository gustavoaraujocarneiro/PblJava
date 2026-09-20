package repository;

import model.Capitulo;
import model.Cena;
import model.Opcao;


public class CapituloRepository {

    public Capitulo buscarCapitulo(int numeroCapitulo) {
        if (numeroCapitulo == 1) {
            Capitulo cap1 = new Capitulo(1, "Prólogo");

            Cena cena1 = new Cena(1, "Você acorda em um quarto escuro. O ar é frio e você não lembra como chegou aqui.");
            cena1.getOpcoes().add(new Opcao("Tentar tatear as paredes procurando um interruptor.", 2, 0, 0, 0));
            cena1.getOpcoes().add(new Opcao("Gritar por socorro.", 3, 0, 0, -10)); // Perde 10 de sanidade

            Cena cena2 = new Cena(2, "Você encontra o interruptor. A luz revela uma porta entreaberta.");
            cena2.getOpcoes().add(new Opcao("Sair pela porta.", 0, 0, 0, 0)); // ID 0 termina o jogo

            Cena cena3 = new Cena(3, "O eco do seu grito se perde no escuro... Algo se move no canto da sala.");
            cena3.getOpcoes().add(new Opcao("Ficar quieto e esperar.", 0, 0, 0, -20)); // ID 0 termina o jogo

            cap1.addCena(cena1);
            cap1.addCena(cena2);
            cap1.addCena(cena3);

            return cap1;
        }

        else if (numeroCapitulo == 2) {
            Capitulo cap2 = new Capitulo(2, "Titulo 2");
            //Criação das cenas

            return cap2;
        }

        else if (numeroCapitulo == 3) {
            Capitulo cap3 = new Capitulo(3, "Titulo 3");
            //Criação das cenas

            return cap3;
        }

        return null;
    }
}
