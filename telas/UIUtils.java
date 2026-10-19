package telas;

import telas.componentes.CampoFormulario;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Vector;

/**
 * Fábrica dos componentes visuais usados em todas as telas (botões, campos, tabelas, cabeçalhos),
 * para que todas sigam o mesmo padrão definido em {@link Tema}.
 */
public final class UIUtils {

    private static final DecimalFormat FORMATO_DECIMAL =
        new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pt-BR")));

    private UIUtils() {
    }

    // ------------------------------------------------------------------ botões

    public static JButton criarBotaoPrimario(String texto) {
        return criarBotao(texto, Tema.PRIMARIA, Tema.PRIMARIA_HOVER, Tema.PRIMARIA_TEXTO, null);
    }

    public static JButton criarBotaoSecundario(String texto) {
        return criarBotao(texto, Tema.SUPERFICIE, Tema.CAMPO_FUNDO_HOVER, Tema.TEXTO, Tema.BORDA_FORTE);
    }

    public static JButton criarBotaoPerigo(String texto) {
        return criarBotao(texto, Tema.PERIGO, Tema.PERIGO_HOVER, Color.WHITE, null);
    }

    /** Botão de excluir sem fundo, para não competir com o "Salvar". */
    public static JButton criarBotaoPerigoDiscreto(String texto) {
        JButton botao = criarBotao(texto, Tema.SUPERFICIE, Tema.PERIGO_FUNDO, Tema.PERIGO, null);
        botao.setMargin(new Insets(8, 6, 8, 6));
        return botao;
    }

    private static JButton criarBotao(String texto, Color fundo, Color fundoHover, Color corTexto, Color borda) {
        JButton botao = new JButton(texto);
        botao.setFont(Tema.fonte(Tema.TAMANHO_TEXTO, Font.BOLD));
        botao.setMargin(new Insets(8, 16, 8, 16));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        String estiloBorda = borda == null
            ? "borderWidth: 0;"
            : "borderWidth: 1; borderColor: " + Tema.hex(borda) + "; hoverBorderColor: " + Tema.hex(Tema.TEXTO) + ";";
        botao.putClientProperty("FlatLaf.style",
            "arc: " + Tema.ARREDONDAMENTO + "; " + estiloBorda
            + " background: " + Tema.hex(fundo) + "; foreground: " + Tema.hex(corTexto) + ";"
            + " hoverBackground: " + Tema.hex(fundoHover) + "; pressedBackground: " + Tema.hex(fundoHover) + ";"
            + " focusedBackground: " + Tema.hex(fundo) + "; focusWidth: 2; innerFocusWidth: 0;"
            + " focusColor: " + Tema.hex(Tema.DESTAQUE) + ";");
        return botao;
    }

    // ------------------------------------------------------------------ campos de entrada

    public static JTextField criarCampoTexto(String textoAjuda) {
        JTextField campo = new JTextField();
        estilizarCampo(campo, textoAjuda);
        return campo;
    }

    /** Campo com máscara (CPF, CEP). O valor guardado no banco é só os dígitos. */
    public static JFormattedTextField criarCampoMascara(String mascara) {
        JFormattedTextField campo;
        try {
            MaskFormatter formatador = new MaskFormatter(mascara);
            formatador.setPlaceholderCharacter('_');
            formatador.setValueContainsLiteralCharacters(false);
            campo = new JFormattedTextField(formatador);
        } catch (java.text.ParseException ex) {
            campo = new JFormattedTextField();
        }
        // PERSIST mantém o que foi digitado mesmo incompleto, para a validação mostrar o erro
        campo.setFocusLostBehavior(JFormattedTextField.PERSIST);
        estilizarCampo(campo, null);
        campo.setFont(Tema.fonte(Tema.TAMANHO_TEXTO, Font.PLAIN));
        return campo;
    }

    public static JTextField criarCampoBusca(String textoAjuda) {
        JTextField campo = criarCampoTexto(textoAjuda);
        campo.putClientProperty("JTextField.showClearButton", true);
        campo.putClientProperty("JTextField.leadingIcon", new IconeLupa());
        return campo;
    }

    /** Coloca um texto fixo antes do valor, por exemplo "R$" no campo de preço. */
    public static void prefixarCampo(JTextField campo, String prefixo) {
        JLabel rotuloPrefixo = new JLabel(prefixo);
        rotuloPrefixo.setFont(Tema.fonte(Tema.TAMANHO_TEXTO, Font.BOLD));
        rotuloPrefixo.setForeground(Tema.TEXTO_SECUNDARIO);
        rotuloPrefixo.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        campo.putClientProperty("JTextField.leadingComponent", rotuloPrefixo);
    }

    public static void configurarCombo(JComboBox<?> combo) {
        combo.setFont(Tema.fonte(Tema.TAMANHO_TEXTO, Font.PLAIN));
        combo.setMaximumRowCount(14);
        combo.putClientProperty("FlatLaf.style", "arc: " + Tema.ARREDONDAMENTO + "; padding: 8,6,8,6;"
            + " background: " + Tema.hex(Tema.CAMPO_FUNDO) + "; focusedBorderColor: " + Tema.hex(Tema.TEXTO) + ";");
    }

    private static void estilizarCampo(JTextField campo, String textoAjuda) {
        campo.setFont(Tema.fonte(Tema.TAMANHO_TEXTO, Font.PLAIN));
        if (textoAjuda != null) campo.putClientProperty("JTextField.placeholderText", textoAjuda);
        campo.putClientProperty("FlatLaf.style", "arc: " + Tema.ARREDONDAMENTO + ";"
            + " margin: 8,10,8,10; background: " + Tema.hex(Tema.CAMPO_FUNDO) + ";"
            + " focusedBorderColor: " + Tema.hex(Tema.TEXTO) + "; placeholderForeground: #7C8494;");
    }

    // ------------------------------------------------------------------ leitura de valores digitados

    public static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    /** Converte o texto em inteiro; devolve null se não for um número inteiro válido. */
    public static Integer lerInteiro(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException | NullPointerException ex) {
            return null;
        }
    }

    /** Aceita "249,90", "249.90" e "1.249,90". Devolve null se não for um número. */
    public static BigDecimal lerDecimal(String texto) {
        if (texto == null || texto.isBlank()) return null;
        String limpo = texto.trim().replace(" ", "");
        if (limpo.contains(",")) limpo = limpo.replace(".", "").replace(",", ".");
        try {
            return new BigDecimal(limpo);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String formatarDecimal(BigDecimal valor) {
        return valor == null ? "" : FORMATO_DECIMAL.format(valor);
    }

    /** Mostra CPF como 000.000.000-00 e CEP como 00000-000; o banco continua guardando só dígitos. */
    private static Object formatarDocumento(String nomeColuna, Object valor) {
        if (!(valor instanceof String)) return valor;
        String texto = (String) valor;
        if (nomeColuna.equalsIgnoreCase("CPF") && texto.matches("\\d{11}")) {
            return texto.substring(0, 3) + "." + texto.substring(3, 6) + "." + texto.substring(6, 9) + "-" + texto.substring(9);
        }
        if (nomeColuna.equalsIgnoreCase("CEP") && texto.matches("\\d{8}")) {
            return texto.substring(0, 5) + "-" + texto.substring(5);
        }
        return valor;
    }

    // ------------------------------------------------------------------ formulário

    /** Uma linha do formulário com um único campo ocupando toda a largura. */
    public static void adicionarLinhaFormulario(JPanel painel, int linha, CampoFormulario campo) {
        GridBagConstraints posicao = posicaoBase(linha);
        posicao.gridwidth = 2;
        painel.add(campo, posicao);
    }

    /** Uma linha com dois campos lado a lado; os pesos definem quanto da largura cada um ocupa. */
    public static void adicionarLinhaFormulario(JPanel painel, int linha, CampoFormulario campoEsquerda,
                                                CampoFormulario campoDireita, double pesoEsquerda, double pesoDireita) {
        GridBagConstraints esquerda = posicaoBase(linha);
        esquerda.weightx = pesoEsquerda;
        esquerda.insets = new Insets(0, 0, Tema.ESPACO_PEQUENO / 2, Tema.ESPACO_MEDIO / 2);
        painel.add(campoEsquerda, esquerda);

        GridBagConstraints direita = posicaoBase(linha);
        direita.gridx = 1;
        direita.weightx = pesoDireita;
        direita.insets = new Insets(0, Tema.ESPACO_MEDIO / 2, Tema.ESPACO_PEQUENO / 2, 0);
        painel.add(campoDireita, direita);
    }

    private static GridBagConstraints posicaoBase(int linha) {
        GridBagConstraints posicao = new GridBagConstraints();
        posicao.gridx = 0;
        posicao.gridy = linha;
        posicao.weightx = 1;
        posicao.fill = GridBagConstraints.HORIZONTAL;
        posicao.anchor = GridBagConstraints.NORTHWEST;
        posicao.insets = new Insets(0, 0, Tema.ESPACO_PEQUENO / 2, 0);
        return posicao;
    }

    // ------------------------------------------------------------------ estrutura das telas

    /** Cabeçalho de cada tela: título grande, uma linha de explicação e uma ação à direita. */
    public static JPanel criarCabecalhoSecao(String titulo, String descricao, JComponent acao) {
        JPanel cabecalho = new JPanel(new BorderLayout(Tema.ESPACO_GRANDE, 0));
        cabecalho.setOpaque(false);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(Tema.MARGEM_TELA - 4, Tema.MARGEM_TELA, Tema.ESPACO_GRANDE, Tema.MARGEM_TELA));

        JLabel rotuloTitulo = new JLabel(titulo);
        rotuloTitulo.setFont(Tema.fonteTitulo(Tema.TAMANHO_TITULO_TELA));
        rotuloTitulo.setForeground(Tema.TEXTO);

        JLabel rotuloDescricao = new JLabel(descricao);
        rotuloDescricao.setFont(Tema.fonte(Tema.TAMANHO_TEXTO, Font.PLAIN));
        rotuloDescricao.setForeground(Tema.TEXTO_SECUNDARIO);

        JPanel textos = new JPanel(new BorderLayout(0, 0));
        textos.setOpaque(false);
        textos.add(rotuloTitulo, BorderLayout.NORTH);
        textos.add(rotuloDescricao, BorderLayout.SOUTH);

        cabecalho.add(textos, BorderLayout.CENTER);
        if (acao != null) {
            JPanel caixaAcao = new JPanel(new GridBagLayout());
            caixaAcao.setOpaque(false);
            caixaAcao.add(acao);
            cabecalho.add(caixaAcao, BorderLayout.EAST);
        }
        return cabecalho;
    }

    /** Painel branco com borda fina: a "etiqueta" onde ficam formulários e cartões. */
    public static JPanel criarPainelSuperficie() {
        JPanel painel = new JPanel();
        painel.setBackground(Tema.SUPERFICIE);
        painel.setBorder(new BordaArredondada(Tema.BORDA, Tema.ARREDONDAMENTO + 2));
        return painel;
    }

    public static JScrollPane criarRolagem(JComponent componente) {
        JScrollPane rolagem = new JScrollPane(componente);
        rolagem.setBorder(new BordaArredondada(Tema.BORDA, Tema.ARREDONDAMENTO + 2));
        rolagem.getViewport().setBackground(Tema.SUPERFICIE);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        return rolagem;
    }

    // ------------------------------------------------------------------ tabelas

    public static JTable criarTabela() {
        JTable tabela = new JTable();
        tabela.setRowHeight(36);
        tabela.setFont(Tema.fonte(Tema.TAMANHO_TEXTO, Font.PLAIN));
        tabela.setShowVerticalLines(false);
        tabela.setShowHorizontalLines(true);
        tabela.setGridColor(Tema.SUPERFICIE_ALTERNADA);
        tabela.setIntercellSpacing(new Dimension(0, 1));
        tabela.setFillsViewportHeight(true);
        tabela.setBackground(Tema.SUPERFICIE);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tabela.setDefaultRenderer(Object.class, new RenderizadorCelula());
        tabela.setDefaultRenderer(Number.class, new RenderizadorCelula());
        tabela.setDefaultRenderer(BigDecimal.class, new RenderizadorCelula());

        tabela.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean selecionada, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, selecionada, foco, linha, coluna);
                setBackground(Tema.SUPERFICIE);
                setForeground(Tema.TEXTO_SECUNDARIO);
                setFont(Tema.fonteMedia(Tema.TAMANHO_ROTULO));
                setText(valor == null ? "" : valor.toString().toUpperCase());
                Object alinhamento = t.getColumnModel().getColumn(coluna).getIdentifier();
                setHorizontalAlignment(alinhamento instanceof Integer ? (Integer) alinhamento : SwingConstants.LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDA),
                    BorderFactory.createEmptyBorder(0, 14, 0, 14)));
                return this;
            }
        });
        return tabela;
    }

    /**
     * Executa o SELECT e joga o resultado na tabela, usando os apelidos (AS) das colunas como títulos.
     * Devolve quantas linhas vieram. O SQL continua sendo escrito à mão em cada tela.
     */
    public static int preencherTabela(JTable tabela, PreparedStatement consulta) throws SQLException {
        try (ResultSet resultado = consulta.executeQuery()) {
            ResultSetMetaData metaDados = resultado.getMetaData();
            int totalColunas = metaDados.getColumnCount();

            Vector<String> colunas = new Vector<>();
            Class<?>[] tiposColunas = new Class<?>[totalColunas];
            for (int i = 1; i <= totalColunas; i++) {
                colunas.add(metaDados.getColumnLabel(i));
                tiposColunas[i - 1] = Object.class;
            }

            Vector<Vector<Object>> linhas = new Vector<>();
            while (resultado.next()) {
                Vector<Object> linha = new Vector<>();
                for (int i = 1; i <= totalColunas; i++) {
                    Object valor = resultado.getObject(i);
                    if (valor != null && tiposColunas[i - 1] == Object.class) tiposColunas[i - 1] = valor.getClass();
                    linha.add(valor);
                }
                linhas.add(linha);
            }

            DefaultTableModel modelo = new DefaultTableModel(linhas, colunas) {
                @Override
                public boolean isCellEditable(int linha, int coluna) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int coluna) {
                    return tiposColunas[coluna];
                }
            };
            tabela.setModel(modelo);
            tabela.setRowSorter(new TableRowSorter<>(modelo));

            // Alinha números à direita e desenha a fitinha colorida na coluna de tamanho
            for (int i = 0; i < totalColunas; i++) {
                TableColumn coluna = tabela.getColumnModel().getColumn(i);
                String titulo = colunas.get(i);
                if (titulo.equalsIgnoreCase("Tam") || titulo.equalsIgnoreCase("Tamanho")) {
                    coluna.setCellRenderer(new RenderizadorTamanho());
                    coluna.setIdentifier(SwingConstants.CENTER);
                } else if (Number.class.isAssignableFrom(tiposColunas[i])) {
                    coluna.setIdentifier(SwingConstants.RIGHT);
                } else {
                    coluna.setIdentifier(SwingConstants.LEFT);
                }
            }
            return linhas.size();
        }
    }

    /** Fixa a largura de uma coluna e o alinhamento do conteúdo e do título. */
    public static void configurarColuna(JTable tabela, int indice, int largura, int alinhamento) {
        if (indice >= tabela.getColumnCount()) return;
        TableColumn coluna = tabela.getColumnModel().getColumn(indice);
        coluna.setPreferredWidth(largura);
        coluna.setMaxWidth(largura + 40);
        if (!(coluna.getCellRenderer() instanceof RenderizadorTamanho)) coluna.setIdentifier(alinhamento);
    }

    /** Células com faixas alternadas, seleção amarela e números formatados no padrão brasileiro. */
    private static class RenderizadorCelula extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean selecionada, boolean foco, int linha, int coluna) {
            Object valorExibido = valor instanceof BigDecimal ? formatarDecimal((BigDecimal) valor)
                : valor instanceof Double ? formatarDecimal(BigDecimal.valueOf((Double) valor))
                : formatarDocumento(t.getColumnName(coluna), valor);
            super.getTableCellRendererComponent(t, valorExibido, selecionada, false, linha, coluna);
            setBackground(selecionada ? Tema.SELECAO_LINHA : (linha % 2 == 0 ? Tema.SUPERFICIE : Tema.SUPERFICIE_ALTERNADA));
            setForeground(Tema.TEXTO);
            setFont(Tema.fonte(Tema.TAMANHO_TEXTO, Font.PLAIN));
            Object alinhamento = t.getColumnModel().getColumn(coluna).getIdentifier();
            setHorizontalAlignment(alinhamento instanceof Integer ? (Integer) alinhamento : SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            return this;
        }
    }

    /** A fitinha de tamanho: um rótulo colorido (P, M, G, GG) como o adesivo colado na camisa. */
    public static class RenderizadorTamanho extends JComponent implements javax.swing.table.TableCellRenderer {
        private String tamanho = "";
        private Color fundo = Tema.SUPERFICIE;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean selecionada, boolean foco, int linha, int coluna) {
            tamanho = valor == null ? "" : valor.toString();
            fundo = selecionada ? Tema.SELECAO_LINHA : (linha % 2 == 0 ? Tema.SUPERFICIE : Tema.SUPERFICIE_ALTERNADA);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(fundo);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (!tamanho.isEmpty()) desenharFitaTamanho(g2, tamanho, getWidth() / 2, getHeight() / 2);
            g2.dispose();
        }
    }

    /** Desenha a fitinha de tamanho centralizada em (centroX, centroY). Usada na tabela e nas estatísticas. */
    public static void desenharFitaTamanho(Graphics2D g2, String tamanho, int centroX, int centroY) {
        g2.setFont(Tema.fonteTitulo(15f));
        FontMetrics medidas = g2.getFontMetrics();
        int largura = Math.max(30, medidas.stringWidth(tamanho) + 16);
        int altura = 22;
        int x = centroX - largura / 2;
        int y = centroY - altura / 2;
        g2.setColor(Tema.corDoTamanho(tamanho));
        g2.fillRoundRect(x, y, largura, altura, 4, 4);
        g2.setColor(Color.WHITE);
        g2.drawString(tamanho, centroX - medidas.stringWidth(tamanho) / 2, centroY + (medidas.getAscent() - medidas.getDescent()) / 2);
    }

    // ------------------------------------------------------------------ pequenos desenhos

    /** Painel com fundo de cantos arredondados (usado no bloco escuro do SQL). */
    public static class PainelArredondado extends JPanel {
        private final int raio;

        public PainelArredondado(LayoutManager layout, Color fundo, int raio) {
            super(layout);
            this.raio = raio;
            setBackground(fundo);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), raio, raio);
            g2.dispose();
        }
    }

    /** Borda de 1px com cantos arredondados, desenhada à mão para não depender de bibliotecas. */
    public static class BordaArredondada extends javax.swing.border.AbstractBorder {
        private final Color cor;
        private final int raio;

        public BordaArredondada(Color cor, int raio) {
            this.cor = cor;
            this.raio = raio;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int largura, int altura) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(cor);
            g2.drawRoundRect(x, y, largura - 1, altura - 1, raio, raio);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
    }

    /** Ícone de lupa do campo de busca, desenhado com Java2D. */
    private static class IconeLupa implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Tema.TEXTO_SECUNDARIO);
            g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(x + 2, y + 2, 9, 9);
            g2.drawLine(x + 10, y + 10, x + 14, y + 14);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }
}
