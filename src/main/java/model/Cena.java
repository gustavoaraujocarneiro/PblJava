package model;

import java.util.ArrayList;
import java.util.List;


public class Cena {
    private int idCena;
    private String textoNarrativo;
    private List<Fala> falas;
    private List<Opcao> opcoes; // 1 possui N

    // Informações visuais (vêm do historia.txt; a interface gráfica usa, o terminal ignora)
    private String cenario;                                // nome do fundo, ex.: "sala" (null = sem fundo)
    private List<String> personagens = new ArrayList<>();  // quem aparece na tela, ex.: "detetive", "jorge"
    private String item;                                   // item que o jogador ganha nesta cena (ou null)

    public Cena(int idCena, String textoNarrativo) {
        this.idCena = idCena;
        this.textoNarrativo = textoNarrativo;
        this.falas = new ArrayList<>();
        this.opcoes = new ArrayList<>();
    }

    public String getTextoNarrativo() { return textoNarrativo; }
    public List<Fala> getFalas() { return falas; }
    public List<Opcao> getOpcoes() { return opcoes; }
    public int getIdCena() { return idCena; }

    public String getCenario() { return cenario; }
    public void setCenario(String cenario) { this.cenario = cenario; }

    public List<String> getPersonagens() { return personagens; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }
}
