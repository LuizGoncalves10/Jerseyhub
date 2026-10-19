package telas;

import java.awt.*;
import java.io.File;
import java.io.InputStream;

/**
 * Identidade visual do Jerseyhub, num lugar só: cores, fontes e espaçamentos.
 *
 * A ideia vem das etiquetas de loja de camisas: papel branco da etiqueta,
 * faixa preta com o nome da loja, adesivo amarelo de preço para marcar o que
 * está selecionado e as fitinhas coloridas de tamanho (P, M, G, GG) que vêm
 * coladas nas camisas. Trocar uma cor aqui muda o sistema inteiro.
 */
public final class Tema {

    private Tema() {
    }

    // ------------------------------------------------------------------ cores de base
    /** Fundo das telas (cinza frio bem claro, para as superfícies brancas se destacarem). */
    public static final Color FUNDO = Color.decode("#EEF0F3");
    /** Superfície dos cartões, tabelas e formulários (o "papel da etiqueta"). */
    public static final Color SUPERFICIE = Color.decode("#FFFFFF");
    public static final Color SUPERFICIE_ALTERNADA = Color.decode("#F7F8FA");
    public static final Color BORDA = Color.decode("#DADDE3");
    public static final Color BORDA_FORTE = Color.decode("#B9BEC7");

    public static final Color TEXTO = Color.decode("#14161A");
    public static final Color TEXTO_SECUNDARIO = Color.decode("#5A6170");

    // ------------------------------------------------------------------ barra lateral (faixa preta da etiqueta)
    public static final Color LATERAL_FUNDO = Color.decode("#14161A");
    public static final Color LATERAL_TEXTO = Color.decode("#F4F5F7");
    public static final Color LATERAL_TEXTO_SUAVE = Color.decode("#9AA1AD");
    public static final Color LATERAL_ITEM_HOVER = Color.decode("#23262C");
    public static final Color LATERAL_ITEM_ATIVO = Color.decode("#2C3038");

    // ------------------------------------------------------------------ ações e seleção
    /** Botão principal: preto, como a impressão da etiqueta. */
    public static final Color PRIMARIA = Color.decode("#14161A");
    public static final Color PRIMARIA_HOVER = Color.decode("#2E323A");
    public static final Color PRIMARIA_TEXTO = Color.decode("#FFFFFF");
    /** Amarelo do adesivo de preço: marca a linha selecionada e a seção atual. */
    public static final Color DESTAQUE = Color.decode("#FFD23F");
    public static final Color SELECAO_LINHA = Color.decode("#FFF1B8");

    public static final Color CAMPO_FUNDO = Color.decode("#FFFFFF");
    public static final Color CAMPO_FUNDO_HOVER = Color.decode("#F1F3F6");

    // ------------------------------------------------------------------ estados
    public static final Color PERIGO = Color.decode("#C62E2E");
    public static final Color PERIGO_HOVER = Color.decode("#A82424");
    public static final Color PERIGO_FUNDO = Color.decode("#FDECEC");
    public static final Color PERIGO_TEXTO = Color.decode("#8E1C1C");
    public static final Color SUCESSO_FUNDO = Color.decode("#E3F5EA");
    public static final Color SUCESSO_TEXTO = Color.decode("#14632F");

    // ------------------------------------------------------------------ fitas de tamanho (P, M, G, GG)
    public static final Color TAMANHO_P = Color.decode("#0E9384");
    public static final Color TAMANHO_M = Color.decode("#2563EB");
    public static final Color TAMANHO_G = Color.decode("#E2620B");
    public static final Color TAMANHO_GG = Color.decode("#7C3AED");

    /** Cor da fitinha de cada tamanho; cinza se o valor não for um tamanho conhecido. */
    public static Color corDoTamanho(String tamanho) {
        if (tamanho == null) return TEXTO_SECUNDARIO;
        switch (tamanho) {
            case "P": return TAMANHO_P;
            case "M": return TAMANHO_M;
            case "G": return TAMANHO_G;
            case "GG": return TAMANHO_GG;
            default: return TEXTO_SECUNDARIO;
        }
    }

    // ------------------------------------------------------------------ espaçamentos e formas
    public static final int ESPACO_PEQUENO = 8;
    public static final int ESPACO_MEDIO = 12;
    public static final int ESPACO_GRANDE = 20;
    public static final int MARGEM_TELA = 28;
    public static final int ARREDONDAMENTO = 8;

    // ------------------------------------------------------------------ tipografia
    public static final float TAMANHO_ROTULO = 12.5f;
    public static final float TAMANHO_TEXTO = 14f;
    public static final float TAMANHO_TITULO_CARTAO = 22f;
    public static final float TAMANHO_TITULO_TELA = 34f;
    public static final float TAMANHO_NUMERO_GRANDE = 48f;

    private static final Font BARLOW_REGULAR = carregarFonte("Barlow-Regular.ttf", "SansSerif", Font.PLAIN);
    private static final Font BARLOW_MEDIA = carregarFonte("Barlow-Medium.ttf", "SansSerif", Font.PLAIN);
    private static final Font BARLOW_SEMINEGRITO = carregarFonte("Barlow-SemiBold.ttf", "SansSerif", Font.BOLD);
    private static final Font CONDENSADA_SEMINEGRITO = carregarFonte("BarlowCondensed-SemiBold.ttf", "SansSerif", Font.BOLD);
    private static final Font CONDENSADA_NEGRITO = carregarFonte("BarlowCondensed-Bold.ttf", "SansSerif", Font.BOLD);

    /** Fonte de interface (Barlow). Font.BOLD usa o peso semi-negrito, que lê melhor em tela. */
    public static Font fonte(float tamanho, int estilo) {
        Font base = (estilo & Font.BOLD) != 0 ? BARLOW_SEMINEGRITO : BARLOW_REGULAR;
        return base.deriveFont(tamanho);
    }

    /** Peso médio, para cabeçalhos de tabela e itens de menu. */
    public static Font fonteMedia(float tamanho) {
        return BARLOW_MEDIA.deriveFont(tamanho);
    }

    /** Fonte condensada das etiquetas, para títulos e números grandes. */
    public static Font fonteTitulo(float tamanho) {
        return CONDENSADA_NEGRITO.deriveFont(tamanho);
    }

    public static Font fonteTituloLeve(float tamanho) {
        return CONDENSADA_SEMINEGRITO.deriveFont(tamanho);
    }

    /** Fonte monoespaçada para mostrar o SQL das consultas. */
    public static Font fonteCodigo(float tamanho) {
        return new Font(Font.MONOSPACED, Font.PLAIN, Math.round(tamanho));
    }

    public static String hex(Color cor) {
        return String.format("#%02X%02X%02X", cor.getRed(), cor.getGreen(), cor.getBlue());
    }

    /**
     * Carrega um arquivo .ttf da pasta "fontes" do projeto (ou do classpath).
     * Se não achar, usa a fonte padrão do sistema para o programa nunca deixar de abrir.
     */
    private static Font carregarFonte(String arquivo, String familiaReserva, int estiloReserva) {
        try (InputStream entrada = abrirArquivoFonte(arquivo)) {
            if (entrada != null) {
                Font fonte = Font.createFont(Font.TRUETYPE_FONT, entrada);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fonte);
                return fonte;
            }
        } catch (Exception ex) {
            System.out.println("Não foi possível carregar a fonte " + arquivo + ": " + ex.getMessage());
        }
        return new Font(familiaReserva, estiloReserva, 14);
    }

    private static InputStream abrirArquivoFonte(String arquivo) throws Exception {
        File arquivoLocal = new File("fontes", arquivo);
        if (arquivoLocal.exists()) return new java.io.FileInputStream(arquivoLocal);
        return Tema.class.getResourceAsStream("/fontes/" + arquivo);
    }
}
