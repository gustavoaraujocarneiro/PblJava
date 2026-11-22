package model;

public class Personagem {
    private String nome;
    private int confianca = 0;
    private int intimidade = 0;

    public Personagem(String nome) {
        this.nome = nome;
    }
    public int getConfianca() { return confianca; }
    public void alterarConfianca(int valor) { this.confianca += valor; }
}
