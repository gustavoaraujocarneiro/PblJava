package view.fx;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Recursos {

    private static final Map<String, String> ARQUIVO_DO_ITEM = Map.of(
            "Comprimidos antidepressivos", "frasco",
            "Porta-retrato da vítima", "retrato"
    );

    private static final Map<String, Image> cache = new HashMap<>();

    private Recursos() { }

    public static Image cenario(String nome) {
        return carregar("/images/backgrounds/" + nome + ".jpg");
    }

    public static Image personagem(String nome) {
        return carregar("/images/sprites/" + nome + ".png");
    }

    public static Image item(String nomeDoItem) {
        String arquivo = ARQUIVO_DO_ITEM.get(nomeDoItem);
        return arquivo == null ? null : carregar("/images/itens/" + arquivo + ".png");
    }

    private static Image carregar(String caminho) {
        if (cache.containsKey(caminho)) {
            return cache.get(caminho);
        }
        Image imagem = null;
        try (InputStream stream = Recursos.class.getResourceAsStream(caminho)) {
            if (stream != null) {
                imagem = new Image(stream);
            }
        } catch (Exception e) {
            System.err.println("Não foi possível ler a imagem " + caminho + ": " + e.getMessage());
        }
        cache.put(caminho, imagem); // guarda até o null, para não tentar de novo a cada cena
        return imagem;
    }
}
