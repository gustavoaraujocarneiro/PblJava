package view.fx;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Carrega as imagens do jogo (cenários, personagens e itens) de src/main/resources/images.
 *
 * Se o arquivo não existir, devolve null em vez de dar erro: a tela decide o que fazer
 * (por exemplo, simplesmente não desenhar o personagem). Cada imagem é lida uma vez só
 * e guardada em um Map (cache), porque as mesmas imagens se repetem de cena em cena.
 */
public class Recursos {

    // Nome do item no historia.txt -> nome do arquivo em /images/itens/ (sem a extensão).
    // Item novo no jogo: coloque o PNG em images/itens e acrescente uma linha aqui.
    private static final Map<String, String> ARQUIVO_DO_ITEM = Map.of(
            "Comprimidos antidepressivos", "frasco",
            "Porta-retrato da vítima", "retrato"
    );

    private static final Map<String, Image> cache = new HashMap<>();

    private Recursos() { }

    /** Fundo da cena: images/backgrounds/{nome}.jpg  (ex.: "sala", "bar", "corpo"). */
    public static Image cenario(String nome) {
        return carregar("/images/backgrounds/" + nome + ".jpg");
    }

    /** Personagem: images/sprites/{nome}.png  (ex.: "detetive", "jorge", "sombra"). */
    public static Image personagem(String nome) {
        return carregar("/images/sprites/" + nome + ".png");
    }

    /** Ícone do item, procurado pelo nome do item no historia.txt. Null se não houver imagem. */
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
