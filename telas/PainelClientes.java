package telas;

import banco.ConexaoBanco;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class PainelClientes extends JPanel {
    private JTable tabelaClientes;
    private static final String SQL_LISTA_CLIENTES = "SELECT id_cliente AS ID, nome AS Nome, cpf AS CPF, rua AS Rua, numero AS Numero, bairro AS Bairro, cep AS CEP FROM Cliente ORDER BY nome";

    public PainelClientes() {
        UIUtils.configurarPainelBase(this);

        JButton btnInserirCliente = UIUtils.criarBotao("+ Inserir Cliente", UIUtils.COR_SUCESSO);
        JButton btnEditarCliente = UIUtils.criarBotao("Editar Cliente", UIUtils.COR_EDICAO);
        JButton btnExcluirCliente = UIUtils.criarBotao("Excluir Cliente", UIUtils.COR_PERIGO);
        JButton btnAtualizarClientes = UIUtils.criarBotao("Atualizar", UIUtils.COR_NEUTRA);

        btnInserirCliente.addActionListener(e -> inserirCliente());
        btnEditarCliente.addActionListener(e -> editarClientePorCpf());
        btnExcluirCliente.addActionListener(e -> excluirClientePorCpf());
        btnAtualizarClientes.addActionListener(e -> atualizarListaClientes());

        tabelaClientes = UIUtils.criarTabela();
        add(UIUtils.criarBarraTopo("Gerenciamento de Clientes", btnInserirCliente, btnEditarCliente, btnExcluirCliente, btnAtualizarClientes), BorderLayout.NORTH);
        add(UIUtils.criarRolagem(tabelaClientes), BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::atualizarListaClientes);
    }

    private void atualizarListaClientes() {
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) return;
            try (PreparedStatement stmt = conexao.prepareStatement(SQL_LISTA_CLIENTES);
                 ResultSet rs = stmt.executeQuery()) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                Vector<String> colunas = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    colunas.add(metaData.getColumnLabel(i));
                }

                Vector<Vector<Object>> dados = new Vector<>();
                while (rs.next()) {
                    Vector<Object> linha = new Vector<>();
                    for (int i = 1; i <= columnCount; i++) {
                        linha.add(rs.getObject(i));
                    }
                    dados.add(linha);
                }

                tabelaClientes.setModel(UIUtils.criarModeloSomenteLeitura(dados, colunas));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar lista: " + ex.getMessage());
        }
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
                atualizarListaClientes();
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
                    atualizarListaClientes();
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
                    atualizarListaClientes();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }
}