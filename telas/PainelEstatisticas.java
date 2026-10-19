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
    private JTextArea areaEstatisticas;

    public PainelEstatisticas() {
        UIUtils.configurarPainelBase(this);

        JButton btnAtualizarEstatistica = UIUtils.criarBotao("Atualizar Estatísticas", UIUtils.COR_SUCESSO);
        areaEstatisticas = new JTextArea();
        areaEstatisticas.setEditable(false);
        areaEstatisticas.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaEstatisticas.setMargin(new Insets(16, 20, 16, 20));
        areaEstatisticas.setBackground(UIUtils.COR_CARTAO);
        areaEstatisticas.setForeground(UIUtils.COR_TEXTO);

        btnAtualizarEstatistica.addActionListener(e -> atualizarEstatisticas());

        add(UIUtils.criarBarraTopo("Estatísticas do Negócio", btnAtualizarEstatistica), BorderLayout.NORTH);
        add(UIUtils.criarRolagem(areaEstatisticas), BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::atualizarEstatisticas);
    }

    private void atualizarEstatisticas() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ESTATÍSTICAS DO SISTEMA JERSEYHUB ===\n\n");

        try (Connection conexao = ConexaoBanco.conectar()) {
            
            try (PreparedStatement stmt = conexao.prepareStatement("SELECT COUNT(*) AS total FROM Cliente");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) sb.append("Total de Clientes Cadastrados: ").append(rs.getInt("total")).append("\n");
            }

            try (PreparedStatement stmt = conexao.prepareStatement("SELECT COUNT(*) AS total FROM Camisa");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) sb.append("Modelos de Camisas Cadastrados: ").append(rs.getInt("total")).append("\n");
            }

            try (PreparedStatement stmt = conexao.prepareStatement("SELECT SUM(quantidade_estoque) AS total FROM Camisa");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) sb.append("Total de Peças em Estoque: ").append(rs.getInt("total")).append("\n\n");
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

                sb.append("--- COMPORTAMENTO DOS PREÇOS ---\n");
                sb.append(String.format("Média de Preço: R$ %.2f\n", mediaPreco));
                sb.append(String.format("Moda de Preço (Mais comum): R$ %.2f\n", modaPreco));
                sb.append(String.format("Desvio Padrão: R$ %.2f\n", desvioPadraoPreco));
                sb.append("Tamanho na Moda (Mais cadastrado): ").append(modaTamanho).append("\n\n");
                
                sb.append("--- DADOS PARA BOXPLOT (PREÇOS) ---\n");
                sb.append(String.format("Mínimo: R$ %.2f\n", min));
                sb.append(String.format("1º Quartil (Q1): R$ %.2f\n", q1));
                sb.append(String.format("Mediana (Q2): R$ %.2f\n", q2));
                sb.append(String.format("3º Quartil (Q3): R$ %.2f\n", q3));
                sb.append(String.format("Máximo: R$ %.2f\n\n", max));

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

                sb.append("--- INTERVALO DE CONFIANÇA DO ESTOQUE ---\n");
                sb.append(String.format("Média de peças por modelo: %.1f\n", mediaEstoque));
                sb.append(String.format("Com 95%% de confiança, a média populacional de estoque\n"));
                sb.append(String.format("está entre %.1f e %.1f unidades por modelo.\n", limiteInferior, limiteSuperior));
            } else {
                sb.append("Não há camisas cadastradas para gerar cálculos estatísticos.\n");
            }

        } catch (SQLException ex) {
            sb.append("Erro ao carregar estatísticas: ").append(ex.getMessage());
        }

        areaEstatisticas.setText(sb.toString());
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
}