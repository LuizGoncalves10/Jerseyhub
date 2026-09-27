import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Dashboard extends JFrame {

    private JComboBox<String> comboConsultas;
    private JTable tabelaResultados;

    public Dashboard() {
        setTitle("Jerseyhub - Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane painelAbas = new JTabbedPane();

        JPanel painelClientes = new JPanel();
        painelClientes.add(new JLabel("Gerenciamento de Clientes:"));
        JButton btnInserirCliente = new JButton("Inserir Cliente");
        JButton btnEditarCliente = new JButton("Editar Cliente");
        JButton btnExcluirCliente = new JButton("Excluir Cliente");
        
        btnInserirCliente.addActionListener(e -> inserirCliente());
        btnEditarCliente.addActionListener(e -> editarClientePorCpf());
        btnExcluirCliente.addActionListener(e -> excluirClientePorCpf());

        painelClientes.add(btnInserirCliente);
        painelClientes.add(btnEditarCliente);
        painelClientes.add(btnExcluirCliente);

        JPanel painelCamisas = new JPanel();
        painelCamisas.add(new JLabel("Gerenciamento de Camisas:"));
        JButton btnInserirCamisa = new JButton("Inserir Camisa");
        JButton btnEditarCamisa = new JButton("Editar Camisa");
        JButton btnExcluirCamisa = new JButton("Excluir Camisa");
        
        btnInserirCamisa.addActionListener(e -> inserirCamisa());
        btnEditarCamisa.addActionListener(e -> editarCamisa());
        btnExcluirCamisa.addActionListener(e -> excluirCamisa());

        painelCamisas.add(btnInserirCamisa);
        painelCamisas.add(btnEditarCamisa);
        painelCamisas.add(btnExcluirCamisa);

        JPanel painelConsultas = new JPanel(new BorderLayout());
        JPanel menuConsultas = new JPanel();
        String[] opcoes = {"Selecione", "Maior Receita", "Acima da Media", "Relatorio", "Estoque"};
        comboConsultas = new JComboBox<>(opcoes);
        JButton btnRodarConsulta = new JButton("Executar");
        
        btnRodarConsulta.addActionListener(e -> executarConsulta());

        menuConsultas.add(new JLabel("Visualizacao:"));
        menuConsultas.add(comboConsultas);
        menuConsultas.add(btnRodarConsulta);
        tabelaResultados = new JTable();
        painelConsultas.add(menuConsultas, BorderLayout.NORTH);
        painelConsultas.add(new JScrollPane(tabelaResultados), BorderLayout.CENTER);

        JPanel painelEstatistica = new JPanel(new BorderLayout());
        JButton btnAtualizarEstatistica = new JButton("Atualizar Estatísticas");
        JTextArea areaEstatisticas = new JTextArea();
        areaEstatisticas.setEditable(false);
        areaEstatisticas.setFont(new Font("Monospaced", Font.BOLD, 14));
        areaEstatisticas.setMargin(new Insets(20, 20, 20, 20));
        
        btnAtualizarEstatistica.addActionListener(e -> atualizarEstatisticas(areaEstatisticas));
        
        painelEstatistica.add(btnAtualizarEstatistica, BorderLayout.NORTH);
        painelEstatistica.add(new JScrollPane(areaEstatisticas), BorderLayout.CENTER);

        painelAbas.addTab("Clientes", painelClientes);
        painelAbas.addTab("Camisas", painelCamisas);
        painelAbas.addTab("Consultas", painelConsultas);
        painelAbas.addTab("Estatisticas", painelEstatistica);

        add(painelAbas);
    }

    //crod de clientes
    private void inserirCliente() {
        String nome = JOptionPane.showInputDialog(this, "Nome:");
        String cpf = JOptionPane.showInputDialog(this, "CPF (11 digitos, apenas numeros):");
        String rua = JOptionPane.showInputDialog(this, "Rua:");
        String bairro = JOptionPane.showInputDialog(this, "Bairro:");
        String numeroStr = JOptionPane.showInputDialog(this, "Numero:");
        String cep = JOptionPane.showInputDialog(this, "CEP (8 digitos, apenas numeros):");

        if (nome != null && cpf != null && rua != null && bairro != null && numeroStr != null && cep != null) {
            String sql = "INSERT INTO Cliente (nome, cpf, rua, bairro, numero, cep) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conexao = ConexaoBanco.conectar();
                 PreparedStatement stmt = conexao.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.setString(2, cpf);
                stmt.setString(3, rua);
                stmt.setString(4, bairro);
                stmt.setInt(5, Integer.parseInt(numeroStr));
                stmt.setString(6, cep);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Sucesso!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Erro: O campo numero deve conter apenas valores inteiros positivos.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        }
    }

    private void editarClientePorCpf() {
        String cpfBusca = JOptionPane.showInputDialog(this, "Digite o CPF do cliente que deseja editar:");
        if (cpfBusca == null || cpfBusca.trim().isEmpty()) return;

        String sqlBusca = "SELECT * FROM Cliente WHERE cpf = ?";
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmtBusca = conexao.prepareStatement(sqlBusca)) {
            stmtBusca.setString(1, cpfBusca);
            ResultSet rs = stmtBusca.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Erro: Cliente com o CPF informado não foi encontrado.");
                return;
            }

            String nomeAtual = rs.getString("nome");
            String cpfAtual = rs.getString("cpf");
            String ruaAtual = rs.getString("rua");
            String bairroAtual = rs.getString("bairro");
            int numeroAtual = rs.getInt("numero");
            String cepAtual = rs.getString("cep");

            String nome = JOptionPane.showInputDialog(this, "Nome:", nomeAtual);
            String cpf = JOptionPane.showInputDialog(this, "CPF:", cpfAtual);
            String rua = JOptionPane.showInputDialog(this, "Rua:", ruaAtual);
            String bairro = JOptionPane.showInputDialog(this, "Bairro:", bairroAtual);
            String numeroStr = JOptionPane.showInputDialog(this, "Numero:", String.valueOf(numeroAtual));
            String cep = JOptionPane.showInputDialog(this, "CEP:", cepAtual);

            if (nome != null && cpf != null && rua != null && bairro != null && numeroStr != null && cep != null) {
                String sqlUpdate = "UPDATE Cliente SET nome = ?, cpf = ?, rua = ?, bairro = ?, numero = ?, cep = ? WHERE cpf = ?";
                try (PreparedStatement stmtUpdate = conexao.prepareStatement(sqlUpdate)) {
                    stmtUpdate.setString(1, nome);
                    stmtUpdate.setString(2, cpf);
                    stmtUpdate.setString(3, rua);
                    stmtUpdate.setString(4, bairro);
                    stmtUpdate.setInt(5, Integer.parseInt(numeroStr));
                    stmtUpdate.setString(6, cep);
                    stmtUpdate.setString(7, cpfBusca);
                    stmtUpdate.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Cliente atualizado com sucesso!");
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro: O campo numero deve conter apenas valores inteiros positivos.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + ex.getMessage());
        }
    }

    private void excluirClientePorCpf() {
        String cpfBusca = JOptionPane.showInputDialog(this, "Digite o CPF do cliente que deseja excluir:");
        if (cpfBusca == null || cpfBusca.trim().isEmpty()) return;

        String sqlBusca = "SELECT nome FROM Cliente WHERE cpf = ?";
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmtBusca = conexao.prepareStatement(sqlBusca)) {
            stmtBusca.setString(1, cpfBusca);
            ResultSet rs = stmtBusca.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Erro: Cliente com o CPF informado não foi encontrado.");
                return;
            }

            String nomeCliente = rs.getString("nome");
            int confirmacao = JOptionPane.showConfirmDialog(this, 
                "Deseja confirmar a exclusão do cliente: " + nomeCliente + " (CPF: " + cpfBusca + ")?", 
                "Confirmação de Exclusão", 
                JOptionPane.YES_NO_OPTION);

            if (confirmacao == JOptionPane.YES_OPTION) {
                String sqlDelete = "DELETE FROM Cliente WHERE cpf = ?";
                try (PreparedStatement stmtDelete = conexao.prepareStatement(sqlDelete)) {
                    stmtDelete.setString(1, cpfBusca);
                    stmtDelete.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Cliente excluído com sucesso!");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }

    //crod de camisas
    private void inserirCamisa() {
        String modelo = JOptionPane.showInputDialog(this, "Modelo:");
        String versao = JOptionPane.showInputDialog(this, "Versão (Torcedor ou Jogador):");
        String tamanho = JOptionPane.showInputDialog(this, "Tamanho (P, M, G, GG):");
        String precoStr = JOptionPane.showInputDialog(this, "Preço (ex: 250.00):");
        String anoStr = JOptionPane.showInputDialog(this, "Ano:");
        String estoqueStr = JOptionPane.showInputDialog(this, "Quantidade em Estoque:");
        String idEquipeStr = JOptionPane.showInputDialog(this, "ID da Equipe (deixe em branco se nulo):");

        if (modelo != null && versao != null && tamanho != null && precoStr != null && anoStr != null && estoqueStr != null) {
            String sql = "INSERT INTO Camisa (modelo, versao, tamanho, preco, ano, quantidade_estoque, fk_id_equipe) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conexao = ConexaoBanco.conectar();
                 PreparedStatement stmt = conexao.prepareStatement(sql)) {
                stmt.setString(1, modelo);
                stmt.setString(2, versao);
                stmt.setString(3, tamanho);
                stmt.setDouble(4, Double.parseDouble(precoStr));
                stmt.setInt(5, Integer.parseInt(anoStr));
                stmt.setInt(6, Integer.parseInt(estoqueStr));
                
                if (idEquipeStr == null || idEquipeStr.trim().isEmpty()) {
                    stmt.setNull(7, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(7, Integer.parseInt(idEquipeStr));
                }
                
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Camisa inserida com sucesso!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Erro: Certifique-se de que Preço, Ano, Estoque e Equipe são números válidos.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao inserir: " + ex.getMessage());
            }
        }
    }

    private void editarCamisa() {
        String idBusca = JOptionPane.showInputDialog(this, "Digite o ID da camisa que deseja editar:");
        if (idBusca == null || idBusca.trim().isEmpty()) return;

        String sqlBusca = "SELECT * FROM Camisa WHERE id_camisa = ?";
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmtBusca = conexao.prepareStatement(sqlBusca)) {
            stmtBusca.setInt(1, Integer.parseInt(idBusca));
            ResultSet rs = stmtBusca.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Erro: Camisa com o ID informado não foi encontrada.");
                return;
            }

            String modeloAtual = rs.getString("modelo");
            String versaoAtual = rs.getString("versao");
            String tamanhoAtual = rs.getString("tamanho");
            double precoAtual = rs.getDouble("preco");
            int anoAtual = rs.getInt("ano");
            int estoqueAtual = rs.getInt("quantidade_estoque");
            int idEquipeAtual = rs.getInt("fk_id_equipe");
            boolean equipeNula = rs.wasNull();

            String modelo = JOptionPane.showInputDialog(this, "Modelo:", modeloAtual);
            String versao = JOptionPane.showInputDialog(this, "Versão (Torcedor ou Jogador):", versaoAtual);
            String tamanho = JOptionPane.showInputDialog(this, "Tamanho (P, M, G, GG):", tamanhoAtual);
            String precoStr = JOptionPane.showInputDialog(this, "Preço:", String.valueOf(precoAtual));
            String anoStr = JOptionPane.showInputDialog(this, "Ano:", String.valueOf(anoAtual));
            String estoqueStr = JOptionPane.showInputDialog(this, "Quantidade em Estoque:", String.valueOf(estoqueAtual));
            String idEquipeStr = JOptionPane.showInputDialog(this, "ID da Equipe (deixe em branco se nulo):", equipeNula ? "" : String.valueOf(idEquipeAtual));

            if (modelo != null && versao != null && tamanho != null && precoStr != null && anoStr != null && estoqueStr != null) {
                String sqlUpdate = "UPDATE Camisa SET modelo = ?, versao = ?, tamanho = ?, preco = ?, ano = ?, quantidade_estoque = ?, fk_id_equipe = ? WHERE id_camisa = ?";
                try (PreparedStatement stmtUpdate = conexao.prepareStatement(sqlUpdate)) {
                    stmtUpdate.setString(1, modelo);
                    stmtUpdate.setString(2, versao);
                    stmtUpdate.setString(3, tamanho);
                    stmtUpdate.setDouble(4, Double.parseDouble(precoStr));
                    stmtUpdate.setInt(5, Integer.parseInt(anoStr));
                    stmtUpdate.setInt(6, Integer.parseInt(estoqueStr));
                    
                    if (idEquipeStr == null || idEquipeStr.trim().isEmpty()) {
                        stmtUpdate.setNull(7, java.sql.Types.INTEGER);
                    } else {
                        stmtUpdate.setInt(7, Integer.parseInt(idEquipeStr));
                    }
                    
                    stmtUpdate.setInt(8, Integer.parseInt(idBusca));
                    stmtUpdate.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Camisa atualizada com sucesso!");
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro de formatação. Preço, Ano, Estoque, e ID precisam ser numéricos.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + ex.getMessage());
        }
    }

    private void excluirCamisa() {
        String idBusca = JOptionPane.showInputDialog(this, "Digite o ID da camisa que deseja excluir:");
        if (idBusca == null || idBusca.trim().isEmpty()) return;

        String sqlBusca = "SELECT modelo, versao FROM Camisa WHERE id_camisa = ?";
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmtBusca = conexao.prepareStatement(sqlBusca)) {
            stmtBusca.setInt(1, Integer.parseInt(idBusca));
            ResultSet rs = stmtBusca.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Erro: Camisa com o ID informado não foi encontrada.");
                return;
            }

            String modeloCamisa = rs.getString("modelo");
            String versaoCamisa = rs.getString("versao");
            int confirmacao = JOptionPane.showConfirmDialog(this, 
                "Deseja confirmar a exclusão da camisa: " + modeloCamisa + " - " + versaoCamisa + " (ID: " + idBusca + ")?", 
                "Confirmação de Exclusão", 
                JOptionPane.YES_NO_OPTION);

            if (confirmacao == JOptionPane.YES_OPTION) {
                String sqlDelete = "DELETE FROM Camisa WHERE id_camisa = ?";
                try (PreparedStatement stmtDelete = conexao.prepareStatement(sqlDelete)) {
                    stmtDelete.setInt(1, Integer.parseInt(idBusca));
                    stmtDelete.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Camisa excluída com sucesso!");
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro: O ID informado deve ser um número inteiro.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }

    //consultas
    private void executarConsulta() {
        String selecao = (String) comboConsultas.getSelectedItem();
        String sql = "";

        if ("Maior Receita".equals(selecao)) {
            sql = "SELECT COALESCE(e.nome, 'Sem Equipe') AS Equipe, SUM(ip.subtotal_item) AS Receita_Total FROM Item_Pedido ip JOIN Camisa c ON ip.fk_id_camisa = c.id_camisa LEFT JOIN Equipe e ON c.fk_id_equipe = e.id_equipe GROUP BY e.nome ORDER BY Receita_Total DESC";
        } else if ("Acima da Media".equals(selecao)) {
            sql = "SELECT COALESCE(e.nome, 'Sem Equipe') AS Equipe, c.versao AS Versao, c.preco AS Preco FROM Camisa c LEFT JOIN Equipe e ON c.fk_id_equipe = e.id_equipe WHERE c.preco > (SELECT AVG(preco) FROM Camisa)";
        } else if ("Relatorio".equals(selecao)) {
            sql = "SELECT p.id_pedido AS Pedido, cl.nome AS Cliente, p.data_compra AS Data, p.valor_total AS Total FROM Pedido p JOIN Cliente cl ON p.fk_id_cliente = cl.id_cliente";
        } else if ("Estoque".equals(selecao)) {
            sql = "SELECT COALESCE(e.nome, 'Sem Equipe') AS Equipe, c.versao AS Versao, c.tamanho as Tamanho, c.quantidade_estoque AS Estoque FROM Camisa c LEFT JOIN Equipe e ON c.fk_id_equipe = e.id_equipe ORDER BY c.quantidade_estoque DESC";
        } else {
            JOptionPane.showMessageDialog(this, "Selecione uma consulta válida.");
            return;
        }

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            Vector<String> colunas = new Vector<>();
            
            for (int i = 1; i <= columnCount; i++) {
                colunas.add(metaData.getColumnName(i));
            }

            Vector<Vector<Object>> dados = new Vector<>();
            while (rs.next()) {
                Vector<Object> linha = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    linha.add(rs.getObject(i));
                }
                dados.add(linha);
            }

            tabelaResultados.setModel(new DefaultTableModel(dados, colunas));

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao executar consulta: " + ex.getMessage());
        }
    }

    //estatisticas
    private void atualizarEstatisticas(JTextArea areaEstatisticas) {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Dashboard().setVisible(true);
        });
    }
}