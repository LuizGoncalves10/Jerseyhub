package telas;

import banco.ConexaoBanco;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PainelEstatisticas extends JPanel {
    
    private JLabel lblTotalClientes;
    private JLabel lblTotalCamisas;
    private JLabel lblTotalEstoque;
    
    private JLabel lblMediaPreco;
    private JLabel lblModaPreco;
    private JLabel lblDesvioPreco;
    private JLabel lblTamanhoModa;
    
    private JLabel lblMediaEstoque;
    private JLabel lblConfiancaEstoque;
    
    private BoxplotPanel painelGrafico;

    public PainelEstatisticas() {
        UIUtils.configurarPainelBase(this);

        JButton btnAtualizar = UIUtils.criarBotao("Atualizar Estatísticas", UIUtils.COR_SUCESSO);
        btnAtualizar.addActionListener(e -> atualizarEstatisticas());

        JPanel painelConteudo = new JPanel();
        painelConteudo.setLayout(new BoxLayout(painelConteudo, BoxLayout.Y_AXIS));
        painelConteudo.setBackground(UIUtils.COR_FUNDO);
        painelConteudo.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Indicadores principais
        JPanel painelResumo = new JPanel(new GridLayout(1, 3, 16, 0));
        painelResumo.setOpaque(false);
        painelResumo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        lblTotalClientes = criarLabelDestaque();
        lblTotalCamisas = criarLabelDestaque();
        lblTotalEstoque = criarLabelDestaque();
        
        painelResumo.add(criarCartao("Total de Clientes", lblTotalClientes, true));
        painelResumo.add(criarCartao("Modelos Cadastrados", lblTotalCamisas, true));
        painelResumo.add(criarCartao("Peças em Estoque", lblTotalEstoque, true));

        // Detalhamento analítico
        JPanel painelDetalhes = new JPanel(new GridLayout(1, 2, 16, 0));
        painelDetalhes.setOpaque(false);
        painelDetalhes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel infoPrecos = new JPanel(new GridLayout(4, 1, 0, 8));
        infoPrecos.setOpaque(false);
        lblMediaPreco = criarLabelNormal();
        lblModaPreco = criarLabelNormal();
        lblDesvioPreco = criarLabelNormal();
        lblTamanhoModa = criarLabelNormal();
        infoPrecos.add(lblMediaPreco);
        infoPrecos.add(lblModaPreco);
        infoPrecos.add(lblDesvioPreco);
        infoPrecos.add(lblTamanhoModa);

        JPanel infoEstoque = new JPanel(new GridLayout(2, 1, 0, 8));
        infoEstoque.setOpaque(false);
        lblMediaEstoque = criarLabelNormal();
        lblConfiancaEstoque = criarLabelNormal();
        infoEstoque.add(lblMediaEstoque);
        infoEstoque.add(lblConfiancaEstoque);

        painelDetalhes.add(criarCartao("Comportamento dos Preços", infoPrecos, false));
        painelDetalhes.add(criarCartao("Análise de Estoque", infoEstoque, false));

        // Visualização de distribuição
        painelGrafico = new BoxplotPanel();
        painelGrafico.setPreferredSize(new Dimension(800, 250));
        JPanel cartaoBoxplot = criarCartao("Distribuição de Preços (Boxplot)", painelGrafico, false);
        cartaoBoxplot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        painelConteudo.add(painelResumo);
        painelConteudo.add(Box.createRigidArea(new Dimension(0, 16)));
        painelConteudo.add(painelDetalhes);
        painelConteudo.add(Box.createRigidArea(new Dimension(0, 16)));
        painelConteudo.add(cartaoBoxplot);
        painelConteudo.add(Box.createVerticalGlue());

        JScrollPane rolagem = UIUtils.criarRolagem(painelConteudo);
        rolagem.setBorder(null);

        add(UIUtils.criarBarraTopo("Estatísticas do Negócio", btnAtualizar), BorderLayout.NORTH);
        add(rolagem, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::atualizarEstatisticas);
    }

    private JLabel criarLabelDestaque() {
        JLabel lbl = new JLabel("-");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 36));
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

    private void atualizarEstatisticas() {
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) return;

            try (PreparedStatement stmt = conexao.prepareStatement("SELECT COUNT(*) AS total FROM Cliente");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) lblTotalClientes.setText(String.valueOf(rs.getInt("total")));
            }

            try (PreparedStatement stmt = conexao.prepareStatement("SELECT COUNT(*) AS total FROM Camisa");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) lblTotalCamisas.setText(String.valueOf(rs.getInt("total")));
            }

            try (PreparedStatement stmt = conexao.prepareStatement("SELECT SUM(quantidade_estoque) AS total FROM Camisa");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) lblTotalEstoque.setText(String.valueOf(rs.getInt("total")));
            }

            List<Double> precos = new ArrayList<>();
            List<Integer> estoques = new ArrayList<>();
            Map<Double, Integer> freqPrecos = new HashMap<>();
            Map<String, Integer> freqTamanhos = new HashMap<>();
            
            try (PreparedStatement stmt = conexao.prepareStatement("SELECT preco, quantidade_estoque, tamanho FROM Camisa");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double p = rs.getDouble("preco");
                    int e = rs.getInt("quantidade_estoque");
                    String t = rs.getString("tamanho");
                    
                    precos.add(p);
                    estoques.add(e);
                    
                    freqPrecos.put(p, freqPrecos.getOrDefault(p, 0) + 1);
                    freqTamanhos.put(t, freqTamanhos.getOrDefault(t, 0) + 1);
                }
            }

            if (!precos.isEmpty()) {
                Collections.sort(precos);
                int n = precos.size();
                
                double somaPrecos = 0;
                for (double p : precos) somaPrecos += p;
                double mediaPreco = somaPrecos / n;
                
                double modaPreco = precos.get(0);
                int maxFreq = 0;
                for (Map.Entry<Double, Integer> entry : freqPrecos.entrySet()) {
                    if (entry.getValue() > maxFreq) {
                        maxFreq = entry.getValue();
                        modaPreco = entry.getKey();
                    }
                }
                
                String modaTamanho = "";
                int maxFreqT = 0;
                for (Map.Entry<String, Integer> entry : freqTamanhos.entrySet()) {
                    if (entry.getValue() > maxFreqT) {
                        maxFreqT = entry.getValue();
                        modaTamanho = entry.getKey();
                    }
                }

                double min = precos.get(0);
                double max = precos.get(n - 1);
                double q2 = calcularMediana(precos, 0, n - 1);
                double q1 = calcularMediana(precos, 0, n / 2 - 1);
                double q3 = calcularMediana(precos, n / 2 + (n % 2 == 0 ? 0 : 1), n - 1);

                double somaDiferencasPreco = 0;
                for (double p : precos) {
                    somaDiferencasPreco += Math.pow(p - mediaPreco, 2);
                }
                double desvioPadraoPreco = Math.sqrt(somaDiferencasPreco / n);

                lblMediaPreco.setText(String.format("<html><b>Média:</b> R$ %.2f</html>", mediaPreco));
                lblModaPreco.setText(String.format("<html><b>Moda (Mais comum):</b> R$ %.2f</html>", modaPreco));
                lblDesvioPreco.setText(String.format("<html><b>Desvio Padrão:</b> R$ %.2f</html>", desvioPadraoPreco));
                lblTamanhoModa.setText(String.format("<html><b>Tamanho mais comum:</b> %s</html>", modaTamanho));

                painelGrafico.setValores(min, q1, q2, q3, max);

                double somaEstoque = 0;
                for (int e : estoques) somaEstoque += e;
                double mediaEstoque = somaEstoque / n;
                
                double somaDiferencasEstoque = 0;
                for (int e : estoques) {
                    somaDiferencasEstoque += Math.pow(e - mediaEstoque, 2);
                }
                double desvioPadraoEstoque = Math.sqrt(somaDiferencasEstoque / n);
                
                double margemErro = 1.96 * (desvioPadraoEstoque / Math.sqrt(n));
                double limiteInferior = mediaEstoque - margemErro;
                double limiteSuperior = mediaEstoque + margemErro;

                lblMediaEstoque.setText(String.format("<html><b>Média de peças por modelo:</b> %.1f</html>", mediaEstoque));
                lblConfiancaEstoque.setText(String.format(
                    "<html><b>Intervalo de Confiança (95%%):</b><br>A média populacional de estoque está entre<br><font color='#2EA05F'><b>%.1f e %.1f</b></font> unidades por modelo.</html>", 
                    limiteInferior, limiteSuperior
                ));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar estatísticas: " + ex.getMessage());
        }
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
}