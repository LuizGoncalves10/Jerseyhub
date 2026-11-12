package telas;

import banco.ConexaoBanco;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PainelEstatisticas extends JPanel {

    private static final Color[] PALETA = {
        Color.decode("#14B8A6"), Color.decode("#F59E0B"), Color.decode("#6366F1"),
        Color.decode("#EC4899"), Color.decode("#84CC16"), Color.decode("#06B6D4")
    };

    private static final String JUNCOES_VENDAS =
        "FROM Pedido p " +
        "JOIN Item_Pedido ip ON ip.fk_id_pedido = p.id_pedido " +
        "JOIN Camisa c ON c.id_camisa = ip.fk_id_camisa " +
        "LEFT JOIN Equipe e ON e.id_equipe = c.fk_id_equipe " +
        "WHERE p.data_compra BETWEEN ? AND ? AND (? = 0 OR e.equipe_tipo = ?) ";

    private JSpinner spinnerInicio;
    private JSpinner spinnerFim;
    private JComboBox<String> comboTipo;
    private boolean datasIniciadas = false;

    private JLabel lblTotalClientes;
    private JLabel lblTotalCamisas;
    private JLabel lblTotalEstoque;
    private JLabel lblTotalPedidos;
    private JLabel lblFaturamento;
    private JLabel lblTicketMedio;

    private JLabel lblMediaPreco;
    private JLabel lblMedianaPreco;
    private JLabel lblModaPreco;
    private JLabel lblVariancia;
    private JLabel lblDesvioPreco;
    private JLabel lblTamanhoModa;

    private JLabel lblMediaEstoque;
    private JLabel lblConfiancaEstoque;

    private BoxplotPanel graficoBoxplot;
    private ColumnChartPanel graficoHistograma;
    private PieChartPanel graficoVersoes;
    private LineChartPanel graficoSemanas;
    private BarChartPanel graficoFaturamento;
    private BarChartPanel graficoVendedores;
    private BarChartPanel graficoTamanhos;

    public PainelEstatisticas() {
        UIUtils.configurarPainelBase(this);

        JPanel painelConteudo = new JPanel();
        painelConteudo.setLayout(new BoxLayout(painelConteudo, BoxLayout.Y_AXIS));
        painelConteudo.setBackground(UIUtils.COR_FUNDO);
        painelConteudo.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        lblTotalClientes = criarLabelDestaque();
        lblTotalCamisas = criarLabelDestaque();
        lblTotalEstoque = criarLabelDestaque();
        lblTotalPedidos = criarLabelDestaque();
        lblFaturamento = criarLabelDestaque();
        lblTicketMedio = criarLabelDestaque();

        JPanel painelResumo = new JPanel(new GridLayout(2, 3, 16, 16));
        painelResumo.setOpaque(false);
        painelResumo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        painelResumo.add(criarCartao("Total de Clientes", lblTotalClientes, true));
        painelResumo.add(criarCartao("Modelos Cadastrados", lblTotalCamisas, true));
        painelResumo.add(criarCartao("Peças em Estoque", lblTotalEstoque, true));
        painelResumo.add(criarCartao("Pedidos no Período", lblTotalPedidos, true));
        painelResumo.add(criarCartao("Faturamento no Período", lblFaturamento, true));
        painelResumo.add(criarCartao("Ticket Médio no Período", lblTicketMedio, true));

        lblMediaPreco = criarLabelNormal();
        lblMedianaPreco = criarLabelNormal();
        lblModaPreco = criarLabelNormal();
        lblVariancia = criarLabelNormal();
        lblDesvioPreco = criarLabelNormal();
        lblTamanhoModa = criarLabelNormal();

        JPanel infoPrecos = new JPanel(new GridLayout(6, 1, 0, 6));
        infoPrecos.setOpaque(false);
        infoPrecos.add(lblMediaPreco);
        infoPrecos.add(lblMedianaPreco);
        infoPrecos.add(lblModaPreco);
        infoPrecos.add(lblVariancia);
        infoPrecos.add(lblDesvioPreco);
        infoPrecos.add(lblTamanhoModa);

        lblMediaEstoque = criarLabelNormal();
        lblConfiancaEstoque = criarLabelNormal();

        JPanel infoEstoque = new JPanel(new GridLayout(2, 1, 0, 8));
        infoEstoque.setOpaque(false);
        infoEstoque.add(lblMediaEstoque);
        infoEstoque.add(lblConfiancaEstoque);

        graficoBoxplot = new BoxplotPanel();
        graficoBoxplot.setPreferredSize(new Dimension(400, 220));
        graficoHistograma = new ColumnChartPanel();
        graficoVersoes = new PieChartPanel();
        graficoSemanas = new LineChartPanel();
        graficoFaturamento = new BarChartPanel(true, 140);
        graficoVendedores = new BarChartPanel(true, 140);
        graficoTamanhos = new BarChartPanel(false, 140);

        painelConteudo.add(criarBarraFiltros());
        painelConteudo.add(Box.createRigidArea(new Dimension(0, 16)));
        painelConteudo.add(painelResumo);
        painelConteudo.add(Box.createRigidArea(new Dimension(0, 16)));
        painelConteudo.add(criarLinhaDeCartoes(
            criarCartao("Comportamento dos Preços", infoPrecos, false),
            criarCartao("Análise de Estoque", infoEstoque, false), 260));
        painelConteudo.add(Box.createRigidArea(new Dimension(0, 16)));
        painelConteudo.add(criarLinhaDeCartoes(
            criarCartao("Distribuição de Preços (Boxplot)", graficoBoxplot, false),
            criarCartao("Histograma de Preços (nº de camisas por faixa)", graficoHistograma, false), 320));
        painelConteudo.add(Box.createRigidArea(new Dimension(0, 16)));
        painelConteudo.add(criarLinhaDeCartoes(
            criarCartao("Faturamento por Versão no Período", graficoVersoes, false),
            criarCartao("Faturamento por Semana no Período", graficoSemanas, false), 320));
        painelConteudo.add(Box.createRigidArea(new Dimension(0, 16)));
        painelConteudo.add(criarLinhaDeCartoes(
            criarCartao("Top 5: Faturamento por Equipe", graficoFaturamento, false),
            criarCartao("Top 5: Desempenho de Vendedores (todo o período)", graficoVendedores, false), 320));
        painelConteudo.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel cartaoTamanhos = criarCartao("Peças Vendidas por Tamanho no Período", graficoTamanhos, false);
        cartaoTamanhos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        painelConteudo.add(cartaoTamanhos);
        painelConteudo.add(Box.createVerticalGlue());

        JScrollPane rolagem = UIUtils.criarRolagem(painelConteudo);
        rolagem.setBorder(null);

        add(UIUtils.criarBarraTopo("Estatísticas do Negócio"), BorderLayout.NORTH);
        add(rolagem, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::atualizarEstatisticas);
        UIUtils.recarregarAoExibir(this, this::atualizarEstatisticas);
    }

    private JPanel criarBarraFiltros() {
        spinnerInicio = criarSpinnerData();
        spinnerFim = criarSpinnerData();
        comboTipo = new JComboBox<>(new String[]{"Todas as equipes", "Somente clubes", "Somente seleções"});
        comboTipo.setPreferredSize(new Dimension(180, 32));

        JButton btnAplicar = UIUtils.criarBotao("Aplicar", UIUtils.COR_SUCESSO);
        btnAplicar.addActionListener(e -> atualizarEstatisticas());

        JButton btnLimpar = UIUtils.criarBotao("Limpar", UIUtils.COR_SUCESSO);
        btnLimpar.addActionListener(e -> {
            datasIniciadas = false;
            comboTipo.setSelectedIndex(0);
            atualizarEstatisticas();
        });

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        barra.setOpaque(false);
        barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        barra.add(new JLabel("De:"));
        barra.add(spinnerInicio);
        barra.add(new JLabel("Até:"));
        barra.add(spinnerFim);
        barra.add(new JLabel("Equipes:"));
        barra.add(comboTipo);
        barra.add(btnAplicar);
        barra.add(btnLimpar);
        return barra;
    }

    private JSpinner criarSpinnerData() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(new java.util.Date(), null, null, Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setPreferredSize(new Dimension(120, 32));
        return spinner;
    }

    private JPanel criarLinhaDeCartoes(JComponent esquerdo, JComponent direito, int altura) {
        JPanel linha = new JPanel(new GridLayout(1, 2, 16, 0));
        linha.setOpaque(false);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, altura));
        linha.add(esquerdo);
        linha.add(direito);
        return linha;
    }

    private JLabel criarLabelDestaque() {
        JLabel lbl = new JLabel("-");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lbl.setForeground(UIUtils.COR_TEXTO);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    private JLabel criarLabelNormal() {
        JLabel lbl = new JLabel("-");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lbl.setForeground(UIUtils.COR_TEXTO);
        return lbl;
    }

    private JPanel criarCartao(String titulo, JComponent conteudo, boolean centrar) {
        JPanel cartao = new JPanel(new BorderLayout(0, 12));
        cartao.setBackground(UIUtils.COR_CARTAO);
        cartao.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.COR_BORDA),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(UIUtils.COR_TEXTO_SUAVE);
        if (centrar) {
            lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        }

        cartao.add(lblTitulo, BorderLayout.NORTH);
        cartao.add(conteudo, BorderLayout.CENTER);
        return cartao;
    }

    private java.sql.Date lerData(JSpinner spinner) {
        return new java.sql.Date(((java.util.Date) spinner.getValue()).getTime());
    }

    private void definirFiltrosVendas(PreparedStatement stmt, java.sql.Date inicio, java.sql.Date fim, int tipo) throws SQLException {
        stmt.setDate(1, inicio);
        stmt.setDate(2, fim);
        stmt.setInt(3, tipo);
        stmt.setInt(4, tipo);
    }

    private void atualizarEstatisticas() {
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) return;

            iniciarDatasNaPrimeiraCarga(conexao);

            java.sql.Date inicio = lerData(spinnerInicio);
            java.sql.Date fim = lerData(spinnerFim);
            if (inicio.after(fim)) {
                JOptionPane.showMessageDialog(this, "A data inicial não pode ser maior que a data final.");
                return;
            }
            int tipo = comboTipo.getSelectedIndex();

            carregarTotalClientes(conexao);
            carregarIndicadoresDeVendas(conexao, inicio, fim, tipo);
            carregarEstatisticasDoCatalogo(conexao, tipo);
            carregarGraficosDeVendas(conexao, inicio, fim, tipo);
            carregarGraficosDasViews(conexao, tipo);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar estatísticas: " + ex.getMessage());
        }
    }

    private void iniciarDatasNaPrimeiraCarga(Connection conexao) throws SQLException {
        if (datasIniciadas) return;

        try (PreparedStatement stmt = conexao.prepareStatement("SELECT MIN(data_compra) AS primeira, MAX(data_compra) AS ultima FROM Pedido");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && rs.getDate("primeira") != null) {
                spinnerInicio.setValue(rs.getDate("primeira"));
                spinnerFim.setValue(rs.getDate("ultima"));
            }
        }
        datasIniciadas = true;
    }

    private void carregarTotalClientes(Connection conexao) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement("SELECT COUNT(*) AS total FROM Cliente");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) lblTotalClientes.setText(String.valueOf(rs.getInt("total")));
        }
    }

    private void carregarIndicadoresDeVendas(Connection conexao, java.sql.Date inicio, java.sql.Date fim, int tipo) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT p.id_pedido) AS total_pedidos, COALESCE(SUM(ip.subtotal_item), 0) AS faturamento " + JUNCOES_VENDAS;

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            definirFiltrosVendas(stmt, inicio, fim, tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int totalPedidos = rs.getInt("total_pedidos");
                    double faturamento = rs.getDouble("faturamento");
                    double ticketMedio = totalPedidos > 0 ? faturamento / totalPedidos : 0;

                    lblTotalPedidos.setText(String.valueOf(totalPedidos));
                    lblFaturamento.setText(String.format("R$ %.2f", faturamento));
                    lblTicketMedio.setText(String.format("R$ %.2f", ticketMedio));
                }
            }
        }
    }

    private void carregarEstatisticasDoCatalogo(Connection conexao, int tipo) throws SQLException {
        List<Double> precos = new ArrayList<>();
        List<Integer> estoques = new ArrayList<>();
        Map<Double, Integer> freqPrecos = new TreeMap<>();
        Map<String, Integer> freqTamanhos = new TreeMap<>();

        String sql = "SELECT c.preco, c.quantidade_estoque, c.tamanho FROM Camisa c " +
                     "LEFT JOIN Equipe e ON e.id_equipe = c.fk_id_equipe WHERE (? = 0 OR e.equipe_tipo = ?)";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, tipo);
            stmt.setInt(2, tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double preco = rs.getDouble("preco");
                    int estoque = rs.getInt("quantidade_estoque");
                    String tamanho = rs.getString("tamanho");

                    precos.add(preco);
                    estoques.add(estoque);
                    freqPrecos.put(preco, freqPrecos.getOrDefault(preco, 0) + 1);
                    freqTamanhos.put(tamanho, freqTamanhos.getOrDefault(tamanho, 0) + 1);
                }
            }
        }

        if (precos.isEmpty()) {
            limparEstatisticasDoCatalogo();
            return;
        }

        java.util.Collections.sort(precos);
        int n = precos.size();

        double somaPrecos = 0;
        for (double preco : precos) somaPrecos += preco;
        double mediaPreco = somaPrecos / n;

        double modaPreco = precos.get(0);
        int maxFreq = 0;
        for (Map.Entry<Double, Integer> entrada : freqPrecos.entrySet()) {
            if (entrada.getValue() > maxFreq) {
                maxFreq = entrada.getValue();
                modaPreco = entrada.getKey();
            }
        }

        String modaTamanho = "";
        int maxFreqTamanho = 0;
        for (Map.Entry<String, Integer> entrada : freqTamanhos.entrySet()) {
            if (entrada.getValue() > maxFreqTamanho) {
                maxFreqTamanho = entrada.getValue();
                modaTamanho = entrada.getKey();
            }
        }

        double min = precos.get(0);
        double max = precos.get(n - 1);
        double q2 = calcularMediana(precos, 0, n - 1);
        double q1 = calcularMediana(precos, 0, n / 2 - 1);
        double q3 = calcularMediana(precos, n / 2 + (n % 2 == 0 ? 0 : 1), n - 1);

        double somaDiferencasPreco = 0;
        for (double preco : precos) {
            somaDiferencasPreco += Math.pow(preco - mediaPreco, 2);
        }
        double variancia = somaDiferencasPreco / n;
        double desvioPadraoPreco = Math.sqrt(variancia);

        int totalEstoque = 0;
        for (int estoque : estoques) totalEstoque += estoque;
        double mediaEstoque = (double) totalEstoque / n;

        double somaDiferencasEstoque = 0;
        for (int estoque : estoques) {
            somaDiferencasEstoque += Math.pow(estoque - mediaEstoque, 2);
        }
        double desvioPadraoEstoque = Math.sqrt(somaDiferencasEstoque / n);
        double margemErro = 1.96 * (desvioPadraoEstoque / Math.sqrt(n));

        lblTotalCamisas.setText(String.valueOf(n));
        lblTotalEstoque.setText(String.valueOf(totalEstoque));

        lblMediaPreco.setText(String.format("<html><b>Média:</b> R$ %.2f</html>", mediaPreco));
        lblMedianaPreco.setText(String.format("<html><b>Mediana:</b> R$ %.2f</html>", q2));
        lblModaPreco.setText(maxFreq > 1
            ? String.format("<html><b>Moda (Mais comum):</b> R$ %.2f</html>", modaPreco)
            : "<html><b>Moda:</b> sem repetição de preços</html>");
        lblVariancia.setText(String.format("<html><b>Variância:</b> %.2f</html>", variancia));
        lblDesvioPreco.setText(String.format("<html><b>Desvio Padrão:</b> R$ %.2f</html>", desvioPadraoPreco));
        lblTamanhoModa.setText(String.format("<html><b>Tamanho mais comum:</b> %s</html>", modaTamanho));

        lblMediaEstoque.setText(String.format("<html><b>Média de peças por modelo:</b> %.1f</html>", mediaEstoque));
        lblConfiancaEstoque.setText(String.format(
            "<html><b>Intervalo de Confiança (95%%):</b><br>A média populacional de estoque está entre<br><font color='%s'><b>%.1f e %.1f</b></font> unidades por modelo.</html>",
            UIUtils.hex(UIUtils.COR_SUCESSO), mediaEstoque - margemErro, mediaEstoque + margemErro
        ));

        graficoBoxplot.setValores(min, q1, q2, q3, max);
        montarHistograma(precos, min, max);
    }

    private void limparEstatisticasDoCatalogo() {
        lblTotalCamisas.setText("0");
        lblTotalEstoque.setText("0");
        lblMediaPreco.setText("-");
        lblMedianaPreco.setText("-");
        lblModaPreco.setText("-");
        lblVariancia.setText("-");
        lblDesvioPreco.setText("-");
        lblTamanhoModa.setText("-");
        lblMediaEstoque.setText("-");
        lblConfiancaEstoque.setText("-");
        graficoBoxplot.limpar();
        graficoHistograma.setDados(new ArrayList<>(), new ArrayList<>());
    }

    private void montarHistograma(List<Double> precosOrdenados, double min, double max) {
        int n = precosOrdenados.size();
        int classes = (int) Math.ceil(1 + Math.log(n) / Math.log(2));
        if (max == min) classes = 1;
        double larguraFaixa = (max - min) / classes;

        int[] contagem = new int[classes];
        for (double preco : precosOrdenados) {
            int faixa = larguraFaixa == 0 ? 0 : (int) ((preco - min) / larguraFaixa);
            if (faixa >= classes) faixa = classes - 1;
            contagem[faixa]++;
        }

        List<String> faixas = new ArrayList<>();
        List<Double> quantidades = new ArrayList<>();
        for (int i = 0; i < classes; i++) {
            faixas.add(String.format("%.0f-%.0f", min + i * larguraFaixa, min + (i + 1) * larguraFaixa));
            quantidades.add((double) contagem[i]);
        }
        graficoHistograma.setDados(faixas, quantidades);
    }

    private void carregarGraficosDeVendas(Connection conexao, java.sql.Date inicio, java.sql.Date fim, int tipo) throws SQLException {
        List<String> versoes = new ArrayList<>();
        List<Double> faturamentoVersoes = new ArrayList<>();
        String sqlVersoes = "SELECT c.versao AS versao, SUM(ip.subtotal_item) AS faturamento " + JUNCOES_VENDAS +
                            "GROUP BY c.versao ORDER BY faturamento DESC";
        try (PreparedStatement stmt = conexao.prepareStatement(sqlVersoes)) {
            definirFiltrosVendas(stmt, inicio, fim, tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    versoes.add(rs.getString("versao"));
                    faturamentoVersoes.add(rs.getDouble("faturamento"));
                }
            }
        }
        graficoVersoes.setDados(versoes, faturamentoVersoes);

        SimpleDateFormat formatoDia = new SimpleDateFormat("dd/MM");
        List<String> semanas = new ArrayList<>();
        List<Double> faturamentoSemanas = new ArrayList<>();
        String sqlSemanas = "SELECT YEARWEEK(p.data_compra, 1) AS semana, MIN(p.data_compra) AS inicio_semana, SUM(ip.subtotal_item) AS faturamento " +
                            JUNCOES_VENDAS + "GROUP BY YEARWEEK(p.data_compra, 1) ORDER BY semana";
        try (PreparedStatement stmt = conexao.prepareStatement(sqlSemanas)) {
            definirFiltrosVendas(stmt, inicio, fim, tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    semanas.add(formatoDia.format(rs.getDate("inicio_semana")));
                    faturamentoSemanas.add(rs.getDouble("faturamento"));
                }
            }
        }
        graficoSemanas.setDados(semanas, faturamentoSemanas);

        List<String> tamanhos = new ArrayList<>();
        List<Double> pecasVendidas = new ArrayList<>();
        String sqlTamanhos = "SELECT CONCAT('Tamanho ', c.tamanho) AS tamanho, SUM(ip.quantidade) AS pecas " +
                             JUNCOES_VENDAS + "GROUP BY c.tamanho ORDER BY pecas DESC";
        try (PreparedStatement stmt = conexao.prepareStatement(sqlTamanhos)) {
            definirFiltrosVendas(stmt, inicio, fim, tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tamanhos.add(rs.getString("tamanho"));
                    pecasVendidas.add(rs.getDouble("pecas"));
                }
            }
        }
        graficoTamanhos.setDados(tamanhos, pecasVendidas);
    }

    private void carregarGraficosDasViews(Connection conexao, int tipo) throws SQLException {
        List<String> equipes = new ArrayList<>();
        List<Double> faturamentos = new ArrayList<>();
        String sqlFaturamento = "SELECT Equipe, Faturamento_Total FROM vw_faturamento_por_equipe " +
                                "WHERE (? = 0 OR (? = 1 AND Categoria = 'Clube') OR (? = 2 AND Categoria <> 'Clube')) " +
                                "ORDER BY Faturamento_Total DESC LIMIT 5";
        try (PreparedStatement stmt = conexao.prepareStatement(sqlFaturamento)) {
            stmt.setInt(1, tipo);
            stmt.setInt(2, tipo);
            stmt.setInt(3, tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    equipes.add(rs.getString("Equipe"));
                    faturamentos.add(rs.getDouble("Faturamento_Total"));
                }
            }
        }
        graficoFaturamento.setDados(equipes, faturamentos);

        List<String> vendedores = new ArrayList<>();
        List<Double> vendas = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement("SELECT Vendedor, Valor_Total_Vendido FROM vw_desempenho_vendedores ORDER BY Valor_Total_Vendido DESC LIMIT 5");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                vendedores.add(rs.getString("Vendedor"));
                vendas.add(rs.getDouble("Valor_Total_Vendido"));
            }
        }
        graficoVendedores.setDados(vendedores, vendas);
    }

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

    private static String ajustarTexto(String texto, FontMetrics fm, int larguraMaxima) {
        if (fm.stringWidth(texto) <= larguraMaxima) return texto;
        String reduzido = texto;
        while (reduzido.length() > 1 && fm.stringWidth(reduzido + "...") > larguraMaxima) {
            reduzido = reduzido.substring(0, reduzido.length() - 1);
        }
        return reduzido + "...";
    }

    private class BoxplotPanel extends JPanel {
        private double min = 0, q1 = 0, q2 = 0, q3 = 0, max = 0;
        private boolean temDados = false;

        public BoxplotPanel() {
            setOpaque(false);
        }

        public void setValores(double min, double q1, double q2, double q3, double max) {
            this.min = min;
            this.q1 = q1;
            this.q2 = q2;
            this.q3 = q3;
            this.max = max;
            this.temDados = true;
            repaint();
        }

        public void limpar() {
            temDados = false;
            repaint();
        }

        private int mapX(double valor, int largura, int margem, double minEscala, double maxEscala) {
            if (maxEscala == minEscala) return largura / 2;
            return margem + (int) (((valor - minEscala) / (maxEscala - minEscala)) * (largura - 2 * margem));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!temDados) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int largura = getWidth();
            int altura = getHeight();
            int margem = 60;
            int centroY = altura / 2;
            int alturaCaixa = 60;

            double minEscala = min - (max - min) * 0.1;
            double maxEscala = max + (max - min) * 0.1;
            if (minEscala == maxEscala) {
                minEscala -= 10;
                maxEscala += 10;
            }

            int xMin = mapX(min, largura, margem, minEscala, maxEscala);
            int xQ1 = mapX(q1, largura, margem, minEscala, maxEscala);
            int xQ2 = mapX(q2, largura, margem, minEscala, maxEscala);
            int xQ3 = mapX(q3, largura, margem, minEscala, maxEscala);
            int xMax = mapX(max, largura, margem, minEscala, maxEscala);

            g2.setColor(UIUtils.COR_TEXTO);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(xMin, centroY, xQ1, centroY);
            g2.drawLine(xQ3, centroY, xMax, centroY);

            g2.drawLine(xMin, centroY - 15, xMin, centroY + 15);
            g2.drawLine(xMax, centroY - 15, xMax, centroY + 15);

            Color corCaixaFundo = new Color(UIUtils.COR_SUCESSO.getRed(), UIUtils.COR_SUCESSO.getGreen(), UIUtils.COR_SUCESSO.getBlue(), 80);
            g2.setColor(corCaixaFundo);
            g2.fillRect(xQ1, centroY - alturaCaixa / 2, xQ3 - xQ1, alturaCaixa);
            g2.setColor(UIUtils.COR_SUCESSO);
            g2.drawRect(xQ1, centroY - alturaCaixa / 2, xQ3 - xQ1, alturaCaixa);

            g2.setColor(UIUtils.COR_TEXTO);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(xQ2, centroY - alturaCaixa / 2, xQ2, centroY + alturaCaixa / 2);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();

            desenharTexto(g2, "Min: " + formatarMoeda(min), xMin, centroY + 40, fm);
            desenharTexto(g2, "Q1: " + formatarMoeda(q1), xQ1, centroY - 40, fm);
            desenharTexto(g2, "Med: " + formatarMoeda(q2), xQ2, centroY + 45, fm);
            desenharTexto(g2, "Q3: " + formatarMoeda(q3), xQ3, centroY - 40, fm);
            desenharTexto(g2, "Max: " + formatarMoeda(max), xMax, centroY + 40, fm);
        }

        private void desenharTexto(Graphics2D g2, String texto, int x, int y, FontMetrics fm) {
            int larguraTexto = fm.stringWidth(texto);
            g2.setColor(UIUtils.COR_TEXTO);
            g2.drawString(texto, x - larguraTexto / 2, y);
        }

        private String formatarMoeda(double valor) {
            return String.format("R$ %.2f", valor);
        }
    }

    private class BarChartPanel extends JPanel {
        private List<String> labels = new ArrayList<>();
        private List<Double> values = new ArrayList<>();
        private final boolean isCurrency;
        private final int labelWidth;

        public BarChartPanel(boolean isCurrency, int labelWidth) {
            this.isCurrency = isCurrency;
            this.labelWidth = labelWidth;
            setOpaque(false);
            setPreferredSize(new Dimension(400, 200));
        }

        public void setDados(List<String> labels, List<Double> values) {
            this.labels = labels;
            this.values = values;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (labels.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double maxVal = 0;
            for (double v : values) {
                if (v > maxVal) maxVal = v;
            }
            if (maxVal == 0) maxVal = 1;

            int width = getWidth();
            int height = getHeight();
            int barHeight = Math.max(8, Math.min(25, (height - 20) / labels.size() - 10));
            int startY = 20;
            int maxBarWidth = Math.max(40, width - labelWidth - 100);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            FontMetrics fm = g2.getFontMetrics();

            for (int i = 0; i < labels.size(); i++) {
                double val = values.get(i);
                int y = startY + i * (barHeight + 10);

                g2.setColor(UIUtils.COR_TEXTO);
                String rotulo = ajustarTexto(labels.get(i), fm, labelWidth - 15);
                g2.drawString(rotulo, 10, y + barHeight / 2 + fm.getAscent() / 2 - 2);

                int bWidth = (int) ((val / maxVal) * maxBarWidth);
                g2.setColor(UIUtils.COR_SUCESSO);
                g2.fillRoundRect(labelWidth, y, Math.max(bWidth, 5), barHeight, 8, 8);

                g2.setColor(UIUtils.COR_TEXTO_SUAVE);
                String valStr = isCurrency ? String.format("R$ %.2f", val) : String.format("%.0f", val);
                g2.drawString(valStr, labelWidth + Math.max(bWidth, 5) + 10, y + barHeight / 2 + fm.getAscent() / 2 - 2);
            }
        }
    }

    private class ColumnChartPanel extends JPanel {
        private List<String> labels = new ArrayList<>();
        private List<Double> values = new ArrayList<>();

        public ColumnChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(400, 220));
        }

        public void setDados(List<String> labels, List<Double> values) {
            this.labels = labels;
            this.values = values;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (labels.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double maxVal = 0;
            for (double v : values) {
                if (v > maxVal) maxVal = v;
            }
            if (maxVal == 0) maxVal = 1;

            int margemEsquerda = 20;
            int margemSuperior = 25;
            int margemInferior = 30;
            int larguraUtil = getWidth() - margemEsquerda * 2;
            int alturaUtil = getHeight() - margemSuperior - margemInferior;
            int larguraSlot = larguraUtil / labels.size();
            int larguraBarra = Math.max(6, (int) (larguraSlot * 0.8));
            int baseY = margemSuperior + alturaUtil;

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();

            g2.setColor(UIUtils.COR_BORDA);
            g2.drawLine(margemEsquerda, baseY, margemEsquerda + larguraUtil, baseY);

            for (int i = 0; i < labels.size(); i++) {
                double val = values.get(i);
                int alturaBarra = (int) ((val / maxVal) * alturaUtil);
                int x = margemEsquerda + i * larguraSlot + (larguraSlot - larguraBarra) / 2;
                int y = baseY - alturaBarra;

                g2.setColor(UIUtils.COR_SUCESSO);
                g2.fillRoundRect(x, y, larguraBarra, Math.max(alturaBarra, 2), 6, 6);

                String contagem = String.format("%.0f", val);
                g2.setColor(UIUtils.COR_TEXTO);
                g2.drawString(contagem, x + (larguraBarra - fm.stringWidth(contagem)) / 2, y - 5);

                String rotulo = ajustarTexto(labels.get(i), fm, larguraSlot);
                g2.setColor(UIUtils.COR_TEXTO_SUAVE);
                g2.drawString(rotulo, margemEsquerda + i * larguraSlot + (larguraSlot - fm.stringWidth(rotulo)) / 2, baseY + 18);
            }
        }
    }

    private class PieChartPanel extends JPanel {
        private List<String> labels = new ArrayList<>();
        private List<Double> values = new ArrayList<>();

        public PieChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(400, 220));
        }

        public void setDados(List<String> labels, List<Double> values) {
            this.labels = labels;
            this.values = values;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (labels.isEmpty()) return;

            double total = 0;
            for (double v : values) total += v;
            if (total <= 0) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int diametro = Math.max(40, Math.min(getHeight() - 20, getWidth() / 2 - 20));
            int x = 10;
            int y = (getHeight() - diametro) / 2;

            double anguloInicial = 90;
            for (int i = 0; i < values.size(); i++) {
                double extensao = values.get(i) / total * 360.0;
                g2.setColor(PALETA[i % PALETA.length]);
                g2.fill(new Arc2D.Double(x, y, diametro, diametro, anguloInicial, -extensao, Arc2D.PIE));
                anguloInicial -= extensao;
            }

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            FontMetrics fm = g2.getFontMetrics();
            int xLegenda = x + diametro + 24;
            int yLegenda = Math.max(20, getHeight() / 2 - labels.size() * 12);

            for (int i = 0; i < labels.size(); i++) {
                int linhaY = yLegenda + i * 26;
                g2.setColor(PALETA[i % PALETA.length]);
                g2.fillRoundRect(xLegenda, linhaY, 14, 14, 4, 4);

                double percentual = values.get(i) / total * 100.0;
                String texto = String.format("%s: R$ %.2f (%.1f%%)", labels.get(i), values.get(i), percentual);
                g2.setColor(UIUtils.COR_TEXTO);
                g2.drawString(ajustarTexto(texto, fm, Math.max(60, getWidth() - xLegenda - 28)), xLegenda + 22, linhaY + 12);
            }
        }
    }

    private class LineChartPanel extends JPanel {
        private List<String> labels = new ArrayList<>();
        private List<Double> values = new ArrayList<>();

        public LineChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(400, 220));
        }

        public void setDados(List<String> labels, List<Double> values) {
            this.labels = labels;
            this.values = values;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (labels.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double maxVal = 0;
            for (double v : values) {
                if (v > maxVal) maxVal = v;
            }
            if (maxVal == 0) maxVal = 1;
            double topoEscala = maxVal * 1.15;

            int margemEsquerda = 75;
            int margemDireita = 25;
            int margemSuperior = 20;
            int margemInferior = 35;
            int larguraUtil = getWidth() - margemEsquerda - margemDireita;
            int alturaUtil = getHeight() - margemSuperior - margemInferior;
            int baseY = margemSuperior + alturaUtil;

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();

            int divisoes = 4;
            for (int i = 0; i <= divisoes; i++) {
                int linhaY = baseY - i * alturaUtil / divisoes;
                g2.setColor(UIUtils.COR_BORDA);
                g2.drawLine(margemEsquerda, linhaY, margemEsquerda + larguraUtil, linhaY);

                String valorEixo = String.format("R$ %.0f", topoEscala * i / divisoes);
                g2.setColor(UIUtils.COR_TEXTO_SUAVE);
                g2.drawString(valorEixo, margemEsquerda - fm.stringWidth(valorEixo) - 8, linhaY + fm.getAscent() / 2 - 1);
            }

            int total = labels.size();
            int[] pontosX = new int[total];
            int[] pontosY = new int[total];
            for (int i = 0; i < total; i++) {
                pontosX[i] = total == 1 ? margemEsquerda + larguraUtil / 2
                                        : margemEsquerda + i * larguraUtil / (total - 1);
                pontosY[i] = baseY - (int) ((values.get(i) / topoEscala) * alturaUtil);
            }

            g2.setColor(UIUtils.COR_SUCESSO);
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawPolyline(pontosX, pontosY, total);

            int passoRotulo = Math.max(1, (int) Math.ceil(total / Math.max(1.0, larguraUtil / 55.0)));
            for (int i = 0; i < total; i++) {
                g2.setColor(UIUtils.COR_SUCESSO);
                g2.fillOval(pontosX[i] - 5, pontosY[i] - 5, 10, 10);

                if (i % passoRotulo == 0) {
                    g2.setColor(UIUtils.COR_TEXTO_SUAVE);
                    g2.drawString(labels.get(i), pontosX[i] - fm.stringWidth(labels.get(i)) / 2, baseY + 20);
                }

                if (total <= 8) {
                    String valorPonto = String.format("%.0f", values.get(i));
                    g2.setColor(UIUtils.COR_TEXTO);
                    g2.drawString(valorPonto, pontosX[i] - fm.stringWidth(valorPonto) / 2, pontosY[i] - 10);
                }
            }
        }
    }
}