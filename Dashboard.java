import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Dashboard().setVisible(true);
        });
    }
}