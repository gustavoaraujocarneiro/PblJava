package view.fx;

import model.SaveData;

/** Textos usados em mais de uma tela (tela de carregar e tela de salvar). */
public class Formatador {

    private Formatador() { }

    /** Monta a linha que descreve um slot de save. */
    public static String descreverSlot(String rotulo, SaveData data, boolean arquivoExiste) {
        if (data == null) {
            if (arquivoExiste) {
                return rotulo + " - (arquivo corrompido)";
            }
            return rotulo + " - (vazio)";
        }
        return rotulo + " - " + data.nome()
                + " | Capítulo " + data.capituloId()
                + " | " + data.dataHora();
    }
}
