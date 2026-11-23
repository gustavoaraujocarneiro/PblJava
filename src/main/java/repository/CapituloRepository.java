package repository;

import model.Capitulo;
import model.Cena;
import model.Opcao;
import model.Protagonista;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CapituloRepository {

    private static final String ARQUIVO_PADRAO = "historia.txt";
    private static final String NOME_DETETIVE_PADRAO = "Cesar"; // usado quando o protagonista não tem nomeDetetive


    public static class CenaExtras {
        private final List<String> diretivas = new ArrayList<>();
        private final Map<Integer, String> acoes = new HashMap<>(); // índice da opção -> ação

        public List<String> getDiretivas() { return diretivas; }

        public boolean tem(String chave) { return valor(chave) != null; }


        public String valor(String chave) {
            String prefixo = "/" + chave;
            for (String d : diretivas) {
                if (d.equals(prefixo)) return "";
                if (d.startsWith(prefixo + " ")) return d.substring(prefixo.length() + 1).trim();
            }
            return null;
        }


        public String getAcao(int indiceOpcao) { return acoes.get(indiceOpcao); }
    }

    // Linha de fala no texto da cena: "Jorge: ..." ou "Detetive: ..." (começa com letras e dois-pontos).
    // "[usuário]: ..." não casa (tem colchete), então o jogador nunca vira um personagem na tela.
    private static final Pattern FALA = Pattern.compile("^(\\p{L}+):");

    // O cenário "continua" de uma cena para a outra até aparecer outro /cenario no txt
    private String cenarioAtual;

    private final List<String> linhas;
    private final Map<Integer, Integer> inicioCapitulo = new HashMap<>(); // nº do capítulo -> linha do /inicio_capitulo
    private final Map<String, CenaExtras> extras = new HashMap<>();        // "capitulo:cena" -> extras

    public CapituloRepository() {
        this(ARQUIVO_PADRAO);
    }

    public CapituloRepository(String caminhoArquivo) {
        try {
            this.linhas = new ArrayList<>(Files.readAllLines(Paths.get(caminhoArquivo), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler " + caminhoArquivo, e);
        }
        if (!linhas.isEmpty() && linhas.get(0).startsWith("\uFEFF")) {
            linhas.set(0, linhas.get(0).substring(1)); // remove BOM
        }
        indexarCapitulos();
    }


    private void indexarCapitulos() {
        for (int i = 0; i < linhas.size(); i++) {
            String l = linhas.get(i);
            if (l.startsWith("/inicio_capitulo ")) {
                inicioCapitulo.put(Integer.parseInt(l.split(" ", 3)[1]), i);
            }
        }
    }

    public boolean existeCapitulo(int numeroCapitulo) {
        return inicioCapitulo.containsKey(numeroCapitulo);
    }

    public int quantidadeCapitulos() {
        return inicioCapitulo.size();
    }

    public CenaExtras getExtras(int numeroCapitulo, int idCena) {
        CenaExtras e = extras.get(numeroCapitulo + ":" + idCena);
        return e != null ? e : new CenaExtras();
    }

    public Capitulo buscarCapitulo(int numeroCapitulo, Protagonista protagonista) {
        Integer inicio = inicioCapitulo.get(numeroCapitulo);
        if (inicio == null) return null;

        String[] cabecalho = linhas.get(inicio).split(" ", 3);
        Capitulo capitulo = new Capitulo(numeroCapitulo, cabecalho.length > 2 ? cabecalho[2] : "");
        cenarioAtual = null; // cada capítulo começa sem cenário até o primeiro /cenario

        int i = inicio + 1;
        while (i < linhas.size()) {
            String linha = linhas.get(i);
            if (linha.startsWith("/final_capitulo")) break;   // fim do capítulo
            if (linha.startsWith("/cena ")) {
                i = lerCena(capitulo, numeroCapitulo, i, protagonista); // devolve a linha seguinte à cena
            } else {
                i++;
            }
        }
        return capitulo;
    }


    private int lerCena(Capitulo capitulo, int numeroCapitulo, int i, Protagonista protagonista) {
        int idCena = Integer.parseInt(linhas.get(i).substring("/cena ".length()).trim());
        i++;

        CenaExtras ex = new CenaExtras();
        while (linhas.get(i).startsWith("/") && !linhas.get(i).equals("/fim")) {
            ex.diretivas.add(linhas.get(i).trim());            // /hub, /item, /input, /condicao
            i++;
        }

        String textoCru = linhas.get(i++).replace("\\n", "\n");
        String texto = aplicarVariaveis(textoCru, protagonista);
        exigir(linhas.get(i).equals("/fim"), i, "esperava /fim");
        i++;

        Cena cena = new Cena(idCena, texto);
        preencherVisuais(cena, ex, textoCru);
        int indiceOpcao = 0;
        while (true) {
            String[] c = linhas.get(i).split("\\|", -1);       // texto|destino|autoestima|sanidade|confianca|acao
            exigir(c.length == 6, i, "opção deve ter 6 campos separados por |");

            int destino    = Integer.parseInt(c[1]);
            int autoestima = Integer.parseInt(c[2]);
            int sanidade   = Integer.parseInt(c[3]);
            int confianca  = Integer.parseInt(c[4]);

            // destino 0 = fim do capítulo
            // se este é o último capítulo encerra o jogo
            int proximaCena = destino;
            int proximoCapitulo = 0;
            if (destino == 0 && existeCapitulo(numeroCapitulo + 1)) {
                proximaCena = 1;
                proximoCapitulo = numeroCapitulo + 1;
            }

            cena.getOpcoes().add(new Opcao(aplicarVariaveis(c[0], protagonista), proximaCena,
                    confianca, autoestima, sanidade, proximoCapitulo));

            if (!c[5].isEmpty()) ex.acoes.put(indiceOpcao, c[5]);
            indiceOpcao++;
            i++;

            String separador = linhas.get(i++);
            if (separador.equals("/opt")) break;               // última opção da cena
            exigir(separador.equals(",,"), i - 1, "esperava ,, ou /opt");
        }

        capitulo.addCena(cena);
        extras.put(numeroCapitulo + ":" + idCena, ex);
        return i;
    }


    /**
     * Cenário, personagens em tela e item da cena.
     *  - /cenario nome  : muda o fundo (vale também para as cenas seguintes do capítulo)
     *  - /sprite a b    : força quem aparece na tela (ex.: /sprite sombra). Sem isso, aparecem
     *                     os personagens que falam no texto ("Jorge: ...").
     *  - /item nome     : o item que a cena entrega (a regra de entrada é que o adiciona ao inventário)
     */
    private void preencherVisuais(Cena cena, CenaExtras ex, String textoCru) {
        String novoCenario = ex.valor("cenario");
        if (novoCenario != null && !novoCenario.isBlank()) cenarioAtual = novoCenario;
        cena.setCenario(cenarioAtual);

        String forcados = ex.valor("sprite");
        if (forcados != null && !forcados.isBlank()) {
            for (String nome : forcados.trim().split("\\s+")) cena.getPersonagens().add(nome.toLowerCase());
        } else {
            for (String linha : textoCru.split("\n")) {
                Matcher m = FALA.matcher(linha);
                if (m.find()) {
                    String nome = m.group(1).toLowerCase();
                    if (!cena.getPersonagens().contains(nome)) cena.getPersonagens().add(nome);
                }
            }
        }

        String item = ex.valor("item");
        if (item != null && !item.isBlank()) cena.setItem(item);
    }

    private String aplicarVariaveis(String texto, Protagonista p) {
        if (p == null) return texto;
        String g = p.getGenero() == null ? "" : p.getGenero().trim().toLowerCase();
        boolean fem = g.startsWith("fem") || g.equals("f") || g.equals("mulher");
        String detetive = (p.getNomeDetetive() == null || p.getNomeDetetive().isBlank())
                ? NOME_DETETIVE_PADRAO : p.getNomeDetetive();
        return texto
                .replace("[usuário]", p.getNome())
                .replace("[detetive]", detetive)
                .replace("[gênero]", fem ? "mulher" : "homem")
                .replace("o(a) morador(a)", fem ? "a moradora" : "o morador")
                .replace("sozinho(a)", fem ? "sozinha" : "sozinho")
                .replace("um(a)", fem ? "uma" : "um");
    }

    private void exigir(boolean condicao, int indiceLinha, String mensagem) {
        if (!condicao) throw new IllegalStateException("historia.txt, linha " + (indiceLinha + 1) + ": " + mensagem);
    }
}