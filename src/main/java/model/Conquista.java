package model;

import repository.SaveRepository;

import java.util.ArrayList;
import java.util.List;

public class Conquista {
    private String explorador;
    private Integer visit = 0; // começa em 0 para não dar NullPointerException
    private List<Integer> rooms = new ArrayList<>(List.of(10, 11, 12, 13, 16));

    public Conquista(){}

    public Conquista(String explorador){
        this.explorador = explorador;
    }

    // Chamado no início de cada partida (novo jogo ou carregar save):
    // zera a contagem e a lista de salas ainda não visitadas.
    public void setVisit() {
        this.visit = 0;
        this.rooms = new ArrayList<>(List.of(10, 11, 12, 13, 16));
    }

    public String getExplorador() {
        return explorador;
    }

    public void exploradorCheck(Jogo jogoAtual){
        if (jogoAtual.getCenaAtual() != null && jogoAtual.getCapituloAtual() != null) {
            if (jogoAtual.getCapituloAtual().getNumero() == 2) {
                if (rooms.contains(jogoAtual.getCenaAtual().getIdCena())) {
                    this.visit += 1;
                    rooms.remove((Object)jogoAtual.getCenaAtual().getIdCena());
                }
            }
        }
        if (visit >= 5){
            this.explorador = "1";
        }
    }
}