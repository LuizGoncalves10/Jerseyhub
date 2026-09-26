import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Dashboard extends JFrame {

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
        painelCamisas.add(btnInserirCamisa);
        painelCamisas.add(btnEditarCamisa);
        painelCamisas.add(btnExcluirCamisa);

        JPanel painelConsultas = new JPanel(new BorderLayout());
        JPanel menuConsultas = new JPanel();
        String[] opcoes = {"Selecione", "Maior Receita", "Acima da Media", "Relatorio", "Estoque"};
        JComboBox<String> comboConsultas = new JComboBox<>(opcoes);
        JButton btnRodarConsulta = new JButton("Executar");
        menuConsultas.add(new JLabel("Visualizacao:"));
        menuConsultas.add(comboConsultas);
        menuConsultas.add(btnRodarConsulta);
        JTable tabelaResultados = new JTable(10, 4);
        painelConsultas.add(menuConsultas, BorderLayout.NORTH);
        painelConsultas.add(new JScrollPane(tabelaResultados), BorderLayout.CENTER);

        JPanel painelEstatistica = new JPanel(new BorderLayout());
        JLabel labelGrafico = new JLabel("Grafico de Estatistica vira aqui", SwingConstants.CENTER);
        painelEstatistica.add(labelGrafico, BorderLayout.CENTER);

        painelAbas.addTab("Clientes", painelClientes);
        painelAbas.addTab("Camisas", painelCamisas);
        painelAbas.addTab("Consultas", painelConsultas);
        painelAbas.addTab("Estatisticas", painelEstatistica);

        add(painelAbas);
    }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Dashboard().setVisible(true);
        });
    }
}