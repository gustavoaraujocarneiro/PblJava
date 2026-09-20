package view;

import model.Fala;
import model.Opcao;
import model.Protagonista;

import java.util.List;
import java.util.Scanner;


public class TerminalView {
    private Scanner scanner;

    public TerminalView() {
        this.scanner = new Scanner(System.in);
    }

    public void exibirStatusProtagonista(Protagonista protagonista) {
        System.out.println(
                "\n--- [ Sanidade: " + protagonista.getSanidade()
                + " ] --- [ Autoestima: " + protagonista.getAutoestima()
                + " ] ---"
        );
    }

    public void mostrarCena(String textoNarrativo, List<Fala> falas) {
        System.out.println("\n" + textoNarrativo);

        for (Fala fala : falas) {
            System.out.println(fala.getNomePersonagem() + ": " + fala.getTexto());
        }
    }

    public void mostrarOpcoes(List<Opcao> opcoes) {
        System.out.println("\n");
        for (int i = 0; i < opcoes.size(); i++) {
            System.out.println((i + 1) + ". " + opcoes.get(i).getTextoOpcao());
        }
        System.out.print("\n");
    }

    public int lerOpcao() {
        try {
            return Integer.parseInt(scanner.nextLine());
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }

    public void exibirMensagem(String mensagem) {
        System.out.println("\n" + mensagem);
    }
}
