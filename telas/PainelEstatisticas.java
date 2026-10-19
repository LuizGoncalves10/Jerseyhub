package telas;

import banco.ConexaoBanco;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Estatísticas do negócio (parte da disciplina de Estatística).
 *
 * Os dados brutos vêm do banco com SELECT simples; os cálculos (média, moda, desvio padrão,
 * quartis e intervalo de confiança de 95%) são feitos aqui no Java, como na primeira entrega.
 * Os gráficos são desenhados com Java2D, sem bibliotecas externas.
 */
public class PainelEstatisticas extends JPanel {

    private static final String[] ORDEM_TAMANHOS = {"P", "M", "G", "GG"};

    private final JLabel rotuloResumo = new JLabel(" ");

    private final JLabel valorMedia = criarValor();
    private final JLabel valorMediana = criarValor();
    private final JLabel valorModa = criarValor();
    private final JLabel valorDesvio = criarValor();
    private final JLabel valorMinimo = criarValor();
    private final JLabel valorMaximo = criarValor();
    private final JLabel valorQ1 = criarValor();
    private final JLabel valorQ3 = criarValor();

    private final JLabel valorMediaEstoque = criarValor();
    private final JLabel valorDesvioEstoque = criarValor();
    private final JLabel valorTamanhoModa = criarValor();
    private final JLabel textoIntervalo = new JLabel(" ");

    private final GraficoBoxplot graficoBoxplot = new GraficoBoxplot();
    private final GraficoTamanhos graficoTamanhos = new GraficoTamanhos();
    private final GraficoIntervalo graficoIntervalo = new GraficoIntervalo();

    public PainelEstatisticas() {
        setLayout(new BorderLayout());
        setBackground(Tema.FUNDO);

        JButton botaoAtualizar = UIUtils.criarBotaoSecundario("Recalcular");
        botaoAtualizar.addActionListener(e -> atualizarEstatisticas());
        add(UIUtils.criarCabecalhoSecao("Estatísticas",
            "Calculadas na hora a partir das tabelas Cliente e Camisa.", botaoAtualizar), BorderLayout.NORTH);

        JPanel conteudo = new PainelLarguraDaJanela(new GridBagLayout());
        conteudo.setOpaque(false);
        conteudo.setBorder(BorderFactory.createEmptyBorder(0, Tema.MARGEM_TELA, Tema.MARGEM_TELA, Tema.MARGEM_TELA));

        rotuloResumo.setFont(Tema.fonte(16f, Font.PLAIN));
        rotuloResumo.setForeground(Tema.TEXTO);

        GridBagConstraints posicao = new GridBagConstraints();
        posicao.gridx = 0;
        posicao.gridy = 0;
        posicao.gridwidth = 2;
        posicao.fill = GridBagConstraints.HORIZONTAL;
        posicao.weightx = 1;
        posicao.insets = new Insets(0, 0, Tema.ESPACO_GRANDE, 0);
        conteudo.add(rotuloResumo, posicao);

        posicao.gridy = 1;
        posicao.gridwidth = 1;
        posicao.weightx = 0.58;
        posicao.fill = GridBagConstraints.BOTH;
        posicao.insets = new Insets(0, 0, 0, Tema.ESPACO_GRANDE / 2);
        conteudo.add(comLarguraFlexivel(criarSecaoPrecos()), posicao);

        posicao.gridx = 1;
        posicao.weightx = 0.42;
        posicao.insets = new Insets(0, Tema.ESPACO_GRANDE / 2, 0, 0);
        conteudo.add(comLarguraFlexivel(criarSecaoEstoque()), posicao);

        // Espaço vazio que empurra as seções para cima quando a janela é alta
        posicao.gridx = 0;
        posicao.gridy = 2;
        posicao.weighty = 1;
        conteudo.add(Box.createGlue(), posicao);

        JScrollPane rolagem = new JScrollPane(conteudo);
        rolagem.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rolagem.setBorder(null);
        rolagem.getViewport().setBackground(Tema.FUNDO);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        add(rolagem, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::atualizarEstatisticas);
    }

    // ------------------------------------------------------------------ montagem

    private JPanel criarSecaoPrecos() {
        JPanel secao = criarSecao("Preço das camisas", "Distribuição dos preços de todos os modelos (boxplot).");

        graficoBoxplot.setPreferredSize(new Dimension(320, 200));
        graficoBoxplot.setMinimumSize(new Dimension(320, 200));

        JPanel medidas = new JPanel(new GridLayout(2, 4, Tema.ESPACO_MEDIO, Tema.ESPACO_MEDIO));
        medidas.setOpaque(false);
        medidas.add(criarMedida("Média", valorMedia));
        medidas.add(criarMedida("Mediana (Q2)", valorMediana));
        medidas.add(criarMedida("Moda", valorModa));
        medidas.add(criarMedida("Desvio padrão", valorDesvio));
        medidas.add(criarMedida("Mínimo", valorMinimo));
        medidas.add(criarMedida("1º quartil (Q1)", valorQ1));
        medidas.add(criarMedida("3º quartil (Q3)", valorQ3));
        medidas.add(criarMedida("Máximo", valorMaximo));

        JPanel corpo = new JPanel(new BorderLayout(0, Tema.ESPACO_GRANDE));
        corpo.setOpaque(false);
        corpo.add(graficoBoxplot, BorderLayout.CENTER);
        corpo.add(medidas, BorderLayout.SOUTH);
        secao.add(corpo, BorderLayout.CENTER);
        return secao;
    }

    private JPanel criarSecaoEstoque() {
        JPanel secao = criarSecao("Estoque", "Peças por modelo e quantos modelos há de cada tamanho.");

        graficoTamanhos.setPreferredSize(new Dimension(240, 140));
        graficoTamanhos.setMinimumSize(new Dimension(240, 140));
        graficoIntervalo.setPreferredSize(new Dimension(240, 70));
        graficoIntervalo.setMinimumSize(new Dimension(240, 70));

        JPanel medidas = new JPanel(new GridLayout(1, 3, Tema.ESPACO_MEDIO, 0));
        medidas.setOpaque(false);
        medidas.add(criarMedida("Média por modelo", valorMediaEstoque));
        medidas.add(criarMedida("Desvio padrão", valorDesvioEstoque));
        medidas.add(criarMedida("Tamanho mais comum", valorTamanhoModa));

        JLabel tituloIntervalo = new JLabel("Intervalo de confiança de 95% da média");
        tituloIntervalo.setFont(Tema.fonte(Tema.TAMANHO_TEXTO, Font.BOLD));
        tituloIntervalo.setForeground(Tema.TEXTO);
        textoIntervalo.setFont(Tema.fonte(Tema.TAMANHO_ROTULO, Font.PLAIN));
        textoIntervalo.setForeground(Tema.TEXTO_SECUNDARIO);

        JPanel blocoIntervalo = new JPanel(new BorderLayout(0, 4));
        blocoIntervalo.setOpaque(false);
        blocoIntervalo.add(tituloIntervalo, BorderLayout.NORTH);
        blocoIntervalo.add(graficoIntervalo, BorderLayout.CENTER);
        blocoIntervalo.add(textoIntervalo, BorderLayout.SOUTH);

        JLabel tituloTamanhos = new JLabel("Modelos por tamanho");
        tituloTamanhos.setFont(Tema.fonte(Tema.TAMANHO_TEXTO, Font.BOLD));
        tituloTamanhos.setForeground(Tema.TEXTO);
        JPanel blocoTamanhos = new JPanel(new BorderLayout(0, Tema.ESPACO_PEQUENO));
        blocoTamanhos.setOpaque(false);
        blocoTamanhos.add(tituloTamanhos, BorderLayout.NORTH);
        blocoTamanhos.add(graficoTamanhos, BorderLayout.CENTER);

        JPanel corpo = new JPanel();
        corpo.setOpaque(false);
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        for (JComponent bloco : new JComponent[]{medidas, blocoTamanhos, blocoIntervalo}) {
            bloco.setAlignmentX(LEFT_ALIGNMENT);
            corpo.add(bloco);
            corpo.add(Box.createVerticalStrut(Tema.ESPACO_GRANDE + 4));
        }
        secao.add(corpo, BorderLayout.CENTER);
        return secao;
    }

    /**
     * Faz a seção dividir a largura só pelos pesos do GridBagLayout (58% / 42%),
     * em vez de pela largura "preferida" do conteúdo de cada uma.
     */
    private static JPanel comLarguraFlexivel(JPanel secao) {
        JPanel envoltorio = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, secao.getPreferredSize().height);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        envoltorio.setOpaque(false);
        envoltorio.add(secao, BorderLayout.CENTER);
        return envoltorio;
    }

    private JPanel criarSecao(String titulo, String descricao) {
        JPanel secao = UIUtils.criarPainelSuperficie();
        secao.setLayout(new BorderLayout(0, Tema.ESPACO_GRANDE));
        secao.setBorder(BorderFactory.createCompoundBorder(secao.getBorder(),
            BorderFactory.createEmptyBorder(Tema.ESPACO_GRANDE, Tema.ESPACO_GRANDE + 4, Tema.ESPACO_GRANDE, Tema.ESPACO_GRANDE + 4)));

        JLabel rotuloTitulo = new JLabel(titulo);
        rotuloTitulo.setFont(Tema.fonteTitulo(Tema.TAMANHO_TITULO_CARTAO + 2));
        rotuloTitulo.setForeground(Tema.TEXTO);
        JLabel rotuloDescricao = new JLabel(descricao);
        rotuloDescricao.setFont(Tema.fonte(Tema.TAMANHO_ROTULO, Font.PLAIN));
        rotuloDescricao.setForeground(Tema.TEXTO_SECUNDARIO);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(rotuloTitulo, BorderLayout.NORTH);
        topo.add(rotuloDescricao, BorderLayout.SOUTH);
        secao.add(topo, BorderLayout.NORTH);
        return secao;
    }

    private static JLabel criarValor() {
        JLabel valor = new JLabel("–");
        valor.setFont(Tema.fonteTitulo(23f));
        valor.setForeground(Tema.TEXTO);
        return valor;
    }

    private static JPanel criarMedida(String nome, JLabel valor) {
        JLabel rotuloNome = new JLabel(nome);
        rotuloNome.setFont(Tema.fonte(Tema.TAMANHO_ROTULO, Font.PLAIN));
        rotuloNome.setForeground(Tema.TEXTO_SECUNDARIO);
        JPanel medida = new JPanel(new BorderLayout(0, 0));
        medida.setOpaque(false);
        medida.add(rotuloNome, BorderLayout.NORTH);
        medida.add(valor, BorderLayout.CENTER);
        return medida;
    }

    // ------------------------------------------------------------------ cálculos

    private void atualizarEstatisticas() {
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) {
                rotuloResumo.setText("Não foi possível conectar ao banco de dados.");
                return;
            }

            int totalClientes = contar(conexao, "SELECT COUNT(*) AS total FROM Cliente");
            int totalCamisas = contar(conexao, "SELECT COUNT(*) AS total FROM Camisa");
            int totalPecas = contar(conexao, "SELECT SUM(quantidade_estoque) AS total FROM Camisa");
            rotuloResumo.setText("<html><b>" + totalClientes + "</b> clientes cadastrados  ·  <b>" + totalCamisas
                + "</b> modelos de camisa  ·  <b>" + String.format("%,d", totalPecas).replace(',', '.')
                + "</b> peças em estoque</html>");

            List<Double> precos = new ArrayList<>();
            List<Integer> estoques = new ArrayList<>();
            Map<Double, Integer> frequenciaPrecos = new HashMap<>();
            Map<String, Integer> frequenciaTamanhos = new HashMap<>();

            try (PreparedStatement consulta = conexao.prepareStatement("SELECT preco, quantidade_estoque, tamanho FROM Camisa");
                 ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    double preco = resultado.getDouble("preco");
                    int estoque = resultado.getInt("quantidade_estoque");
                    String tamanho = resultado.getString("tamanho");

                    precos.add(preco);
                    estoques.add(estoque);
                    frequenciaPrecos.put(preco, frequenciaPrecos.getOrDefault(preco, 0) + 1);
                    frequenciaTamanhos.put(tamanho, frequenciaTamanhos.getOrDefault(tamanho, 0) + 1);
                }
            }

            if (precos.isEmpty()) {
                rotuloResumo.setText("Ainda não há camisas cadastradas para calcular as estatísticas.");
                return;
            }

            // --- Preços: centralização, dispersão e posição
            Collections.sort(precos);
            int quantidade = precos.size();

            double somaPrecos = 0;
            for (double preco : precos) somaPrecos += preco;
            double mediaPreco = somaPrecos / quantidade;

            double modaPreco = precos.get(0);
            int maiorFrequencia = 0;
            for (Map.Entry<Double, Integer> entrada : frequenciaPrecos.entrySet()) {
                if (entrada.getValue() > maiorFrequencia) {
                    maiorFrequencia = entrada.getValue();
                    modaPreco = entrada.getKey();
                }
            }

            String modaTamanho = "";
            int maiorFrequenciaTamanho = 0;
            for (Map.Entry<String, Integer> entrada : frequenciaTamanhos.entrySet()) {
                if (entrada.getValue() > maiorFrequenciaTamanho) {
                    maiorFrequenciaTamanho = entrada.getValue();
                    modaTamanho = entrada.getKey();
                }
            }

            double minimo = precos.get(0);
            double maximo = precos.get(quantidade - 1);
            double q2 = calcularMediana(precos, 0, quantidade - 1);
            double q1 = calcularMediana(precos, 0, quantidade / 2 - 1);
            double q3 = calcularMediana(precos, quantidade / 2 + (quantidade % 2 == 0 ? 0 : 1), quantidade - 1);

            double somaDiferencasPreco = 0;
            for (double preco : precos) somaDiferencasPreco += Math.pow(preco - mediaPreco, 2);
            double desvioPadraoPreco = Math.sqrt(somaDiferencasPreco / quantidade);

            valorMedia.setText(formatarMoeda(mediaPreco));
            valorMediana.setText(formatarMoeda(q2));
            valorModa.setText(formatarMoeda(modaPreco));
            valorDesvio.setText(formatarMoeda(desvioPadraoPreco));
            valorMinimo.setText(formatarMoeda(minimo));
            valorMaximo.setText(formatarMoeda(maximo));
            valorQ1.setText(formatarMoeda(q1));
            valorQ3.setText(formatarMoeda(q3));
            graficoBoxplot.setValores(minimo, q1, q2, q3, maximo, mediaPreco);

            // --- Estoque: média, desvio padrão e intervalo de confiança de 95% (z = 1,96)
            double somaEstoque = 0;
            for (int estoque : estoques) somaEstoque += estoque;
            double mediaEstoque = somaEstoque / quantidade;

            double somaDiferencasEstoque = 0;
            for (int estoque : estoques) somaDiferencasEstoque += Math.pow(estoque - mediaEstoque, 2);
            double desvioPadraoEstoque = Math.sqrt(somaDiferencasEstoque / quantidade);

            double margemErro = 1.96 * (desvioPadraoEstoque / Math.sqrt(quantidade));
            double limiteInferior = mediaEstoque - margemErro;
            double limiteSuperior = mediaEstoque + margemErro;

            valorMediaEstoque.setText(formatarNumero(mediaEstoque) + " pç");
            valorDesvioEstoque.setText(formatarNumero(desvioPadraoEstoque) + " pç");
            valorTamanhoModa.setText(modaTamanho);
            valorTamanhoModa.setForeground(Tema.corDoTamanho(modaTamanho));

            int minimoEstoque = Collections.min(estoques);
            int maximoEstoque = Collections.max(estoques);
            graficoIntervalo.setValores(minimoEstoque, maximoEstoque, mediaEstoque, limiteInferior, limiteSuperior);
            textoIntervalo.setText("Com 95% de confiança, a média de peças por modelo está entre "
                + formatarNumero(limiteInferior) + " e " + formatarNumero(limiteSuperior)
                + " (margem de erro ± " + formatarNumero(margemErro) + ").");

            Map<String, Integer> tamanhosOrdenados = new LinkedHashMap<>();
            for (String tamanho : ORDEM_TAMANHOS) tamanhosOrdenados.put(tamanho, frequenciaTamanhos.getOrDefault(tamanho, 0));
            graficoTamanhos.setFrequencias(tamanhosOrdenados);

        } catch (SQLException ex) {
            rotuloResumo.setText("Erro ao carregar estatísticas: " + ex.getMessage());
        }
    }

    private static int contar(Connection conexao, String sql) throws SQLException {
        try (PreparedStatement consulta = conexao.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            return resultado.next() ? resultado.getInt("total") : 0;
        }
    }

    /** Mediana do trecho [inicio, fim] de uma lista já ordenada (usada para Q1, Q2 e Q3). */
    private double calcularMediana(List<Double> valores, int inicio, int fim) {
        if (inicio > fim || inicio < 0 || fim >= valores.size()) return 0.0;
        int tamanho = fim - inicio + 1;
        int meio = inicio + tamanho / 2;
        if (tamanho % 2 == 0) {
            return (valores.get(meio - 1) + valores.get(meio)) / 2.0;
        } else {
            return valores.get(meio);
        }
    }

    private static String formatarMoeda(double valor) {
        return "R$ " + formatarNumero2(valor);
    }

    private static String formatarNumero2(double valor) {
        return String.format(java.util.Locale.forLanguageTag("pt-BR"), "%,.2f", valor);
    }

    private static String formatarNumero(double valor) {
        return String.format(java.util.Locale.forLanguageTag("pt-BR"), "%,.1f", valor);
    }

    private static void ativarSuavizacao(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /** Painel que acompanha a largura da janela dentro da rolagem (só rola na vertical). */
    private static class PainelLarguraDaJanela extends JPanel implements Scrollable {
        PainelLarguraDaJanela(LayoutManager layout) {
            super(layout);
        }

        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public int getScrollableUnitIncrement(Rectangle area, int orientacao, int direcao) {
            return 16;
        }

        public int getScrollableBlockIncrement(Rectangle area, int orientacao, int direcao) {
            return area.height;
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public boolean getScrollableTracksViewportHeight() {
            return getParent() != null && getParent().getHeight() > getPreferredSize().height;
        }
    }

    // ------------------------------------------------------------------ gráficos

    /** Boxplot horizontal dos preços, com eixo em reais e a média marcada como um losango. */
    private static class GraficoBoxplot extends JComponent {
        private double minimo, q1, q2, q3, maximo, media;
        private boolean temDados = false;

        void setValores(double minimo, double q1, double q2, double q3, double maximo, double media) {
            this.minimo = minimo;
            this.q1 = q1;
            this.q2 = q2;
            this.q3 = q3;
            this.maximo = maximo;
            this.media = media;
            this.temDados = true;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!temDados) return;
            Graphics2D g2 = (Graphics2D) g.create();
            ativarSuavizacao(g2);

            int margemLateral = 36;
            int largura = getWidth() - 2 * margemLateral;
            int centroY = getHeight() / 2 - 12;
            int alturaCaixa = 56;

            // Escala com folga dos dois lados, em passos "redondos" (10, 20, 25, 50...) de reais
            double passo = escolherPasso((maximo - minimo) * 1.4);
            double inicioEscala = Math.floor((minimo - passo / 2) / passo) * passo;
            double fimEscala = Math.ceil((maximo + passo / 2) / passo) * passo;
            java.util.function.DoubleToIntFunction posicaoX =
                valor -> margemLateral + (int) Math.round((valor - inicioEscala) / (fimEscala - inicioEscala) * largura);

            int xMin = posicaoX.applyAsInt(minimo);
            int xQ1 = posicaoX.applyAsInt(q1);
            int xQ2 = posicaoX.applyAsInt(q2);
            int xQ3 = posicaoX.applyAsInt(q3);
            int xMax = posicaoX.applyAsInt(maximo);
            int xMedia = posicaoX.applyAsInt(media);

            // Eixo com marcas
            int eixoY = getHeight() - 26;
            g2.setColor(Tema.BORDA);
            g2.drawLine(margemLateral, eixoY, margemLateral + largura, eixoY);
            g2.setFont(Tema.fonte(11.5f, Font.PLAIN));
            FontMetrics medidas = g2.getFontMetrics();
            for (double valor = inicioEscala; valor <= fimEscala + 0.001; valor += passo) {
                int x = posicaoX.applyAsInt(valor);
                g2.setColor(Tema.BORDA);
                g2.drawLine(x, eixoY, x, eixoY + 4);
                g2.drawLine(x, 8, x, eixoY);
                String texto = "R$ " + Math.round(valor);
                g2.setColor(Tema.TEXTO_SECUNDARIO);
                g2.drawString(texto, x - medidas.stringWidth(texto) / 2, eixoY + 18);
            }

            // Bigodes (mínimo até Q1 e Q3 até máximo)
            g2.setColor(Tema.TEXTO);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(xMin, centroY, xQ1, centroY);
            g2.drawLine(xQ3, centroY, xMax, centroY);
            g2.drawLine(xMin, centroY - 14, xMin, centroY + 14);
            g2.drawLine(xMax, centroY - 14, xMax, centroY + 14);

            // Caixa de Q1 a Q3 (amarelo do adesivo de preço) e linha da mediana
            int larguraCaixa = Math.max(xQ3 - xQ1, 3);
            g2.setColor(Tema.DESTAQUE);
            g2.fillRoundRect(xQ1, centroY - alturaCaixa / 2, larguraCaixa, alturaCaixa, 6, 6);
            g2.setColor(Tema.TEXTO);
            g2.drawRoundRect(xQ1, centroY - alturaCaixa / 2, larguraCaixa, alturaCaixa, 6, 6);
            g2.setStroke(new BasicStroke(3f));
            g2.drawLine(xQ2, centroY - alturaCaixa / 2, xQ2, centroY + alturaCaixa / 2);

            // Média como losango
            int raio = 6;
            Polygon losango = new Polygon(
                new int[]{xMedia, xMedia + raio, xMedia, xMedia - raio},
                new int[]{centroY - raio, centroY, centroY + raio, centroY}, 4);
            g2.setColor(Tema.SUPERFICIE);
            g2.fillPolygon(losango);
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(Tema.TEXTO);
            g2.drawPolygon(losango);

            // Legenda curta
            g2.setFont(Tema.fonte(Tema.TAMANHO_ROTULO, Font.PLAIN));
            g2.setColor(Tema.TEXTO_SECUNDARIO);
            String legenda = "Caixa: Q1 a Q3  ·  linha: mediana  ·  losango: média";
            g2.drawString(legenda, margemLateral, 18);
            g2.dispose();
        }
    }

    /** Escolhe um intervalo "redondo" para as marcas do eixo, buscando umas 5 divisões. */
    private static double escolherPasso(double amplitude) {
        double[] candidatos = {1, 2, 5, 10, 20, 25, 50, 100, 200, 250, 500, 1000};
        for (double candidato : candidatos) {
            if (amplitude / candidato <= 6) return candidato;
        }
        return 1000;
    }

    /** Barras horizontais com quantos modelos existem em cada tamanho, nas cores das fitas de tamanho. */
    private static class GraficoTamanhos extends JComponent {
        private Map<String, Integer> frequencias = new LinkedHashMap<>();

        void setFrequencias(Map<String, Integer> frequencias) {
            this.frequencias = frequencias;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (frequencias.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            ativarSuavizacao(g2);

            int maior = 1;
            for (int valor : frequencias.values()) maior = Math.max(maior, valor);

            int alturaLinha = getHeight() / frequencias.size();
            int inicioBarra = 48;
            int larguraMaxima = getWidth() - inicioBarra - 40;
            int linha = 0;
            for (Map.Entry<String, Integer> entrada : frequencias.entrySet()) {
                int centroY = linha * alturaLinha + alturaLinha / 2;
                UIUtils.desenharFitaTamanho(g2, entrada.getKey(), 18, centroY);

                int larguraBarra = (int) Math.round((double) entrada.getValue() / maior * larguraMaxima);
                g2.setColor(Tema.SUPERFICIE_ALTERNADA);
                g2.fillRoundRect(inicioBarra, centroY - 8, larguraMaxima, 16, 4, 4);
                g2.setColor(Tema.corDoTamanho(entrada.getKey()));
                if (larguraBarra > 0) g2.fillRoundRect(inicioBarra, centroY - 8, larguraBarra, 16, 4, 4);

                g2.setFont(Tema.fonte(Tema.TAMANHO_TEXTO, Font.BOLD));
                g2.setColor(Tema.TEXTO);
                String texto = String.valueOf(entrada.getValue());
                g2.drawString(texto, inicioBarra + larguraMaxima + 10, centroY + 5);
                linha++;
            }
            g2.dispose();
        }
    }

    /** Reta numérica do estoque (mínimo a máximo) com a média e o intervalo de confiança marcados. */
    private static class GraficoIntervalo extends JComponent {
        private double minimo, maximo, media, inferior, superior;
        private boolean temDados = false;

        void setValores(double minimo, double maximo, double media, double inferior, double superior) {
            this.minimo = minimo;
            this.maximo = maximo;
            this.media = media;
            this.inferior = inferior;
            this.superior = superior;
            this.temDados = true;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!temDados) return;
            Graphics2D g2 = (Graphics2D) g.create();
            ativarSuavizacao(g2);

            int margem = 24;
            int largura = getWidth() - 2 * margem;
            int centroY = getHeight() / 2 - 6;
            double faixa = Math.max(maximo - minimo, 1);
            java.util.function.DoubleToIntFunction posicaoX =
                valor -> margem + (int) Math.round((valor - minimo) / faixa * largura);

            g2.setColor(Tema.BORDA);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(margem, centroY, margem + largura, centroY);

            int xInferior = posicaoX.applyAsInt(inferior);
            int xSuperior = posicaoX.applyAsInt(superior);
            int xMedia = posicaoX.applyAsInt(media);
            g2.setColor(Tema.DESTAQUE);
            g2.fillRoundRect(xInferior, centroY - 9, Math.max(xSuperior - xInferior, 4), 18, 6, 6);
            g2.setColor(Tema.TEXTO);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(xInferior, centroY - 9, Math.max(xSuperior - xInferior, 4), 18, 6, 6);
            g2.fillOval(xMedia - 4, centroY - 4, 8, 8);

            g2.setFont(Tema.fonte(11.5f, Font.PLAIN));
            g2.setColor(Tema.TEXTO_SECUNDARIO);
            FontMetrics medidas = g2.getFontMetrics();
            String textoMinimo = (int) minimo + " pç";
            String textoMaximo = (int) maximo + " pç";
            g2.drawString(textoMinimo, margem, centroY + 28);
            g2.drawString(textoMaximo, margem + largura - medidas.stringWidth(textoMaximo), centroY + 28);
            g2.dispose();
        }
    }
}
