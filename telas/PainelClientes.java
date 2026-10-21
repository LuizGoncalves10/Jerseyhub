package telas;

import banco.ConexaoBanco;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Vector;

public class PainelClientes extends JPanel {
    private JTable tabelaClientes;
    private JTextField campoBusca;
    private JLabel lblTotalClientes;
    private static final String SQL_LISTA_CLIENTES = "SELECT id_cliente AS ID, nome AS Nome, cpf AS CPF, rua AS Rua, numero AS Numero, bairro AS Bairro, cep AS CEP FROM Cliente WHERE nome LIKE ? OR cpf LIKE ? ORDER BY nome";

    public PainelClientes() {
        UIUtils.configurarPainelBase(this);

        JButton btnInserirCliente = UIUtils.criarBotao("Inserir", UIUtils.COR_SUCESSO);
        JButton btnEditarCliente = UIUtils.criarBotao("Editar", UIUtils.COR_EDICAO);
        JButton btnExcluirCliente = UIUtils.criarBotao("Excluir", UIUtils.COR_PERIGO);

        btnInserirCliente.addActionListener(e -> inserirCliente());
        btnEditarCliente.addActionListener(e -> editarClientePorCpf());
        btnExcluirCliente.addActionListener(e -> excluirClientePorCpf());

        campoBusca = UIUtils.criarCampoBusca("Buscar por nome ou CPF...");
        UIUtils.executarAoDigitar(campoBusca, this::atualizarListaClientes);

        campoBusca.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tabelaClientes != null) {
                    tabelaClientes.clearSelection();
                }
            }
        });

        tabelaClientes = UIUtils.criarTabela();
        lblTotalClientes = UIUtils.criarContador();
        
        JScrollPane scrollPaneTabela = UIUtils.criarRolagem(tabelaClientes);
        
        scrollPaneTabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabelaClientes.clearSelection();
            }
        });
        
        scrollPaneTabela.getViewport().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabelaClientes.clearSelection();
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabelaClientes.clearSelection();
            }
        });

        add(UIUtils.criarBarraTopo("Gerenciamento de Clientes", campoBusca, btnInserirCliente, btnEditarCliente, btnExcluirCliente), BorderLayout.NORTH);
        add(scrollPaneTabela, BorderLayout.CENTER);
        add(lblTotalClientes, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::atualizarListaClientes);
        UIUtils.recarregarAoExibir(this, this::atualizarListaClientes);
    }

    private void atualizarListaClientes() {
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) return;
            try (PreparedStatement stmt = conexao.prepareStatement(SQL_LISTA_CLIENTES)) {
                String termo = "%" + campoBusca.getText().trim() + "%";
                stmt.setString(1, termo);
                stmt.setString(2, termo);
                ResultSet rs = stmt.executeQuery();

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
                lblTotalClientes.setText(dados.size() + " cliente(s) encontrado(s)");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar lista: " + ex.getMessage());
        }
    }

    private JPanel criarPainelFormulario(JTextField txtNome, JTextField txtCpf, JTextField txtRua, 
                                         JTextField txtBairro, JTextField txtNumero, JTextField txtCep) {
        JPanel painelPrincipal = new JPanel(new GridLayout(3, 1, 10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlLinha1 = new JPanel(new BorderLayout(5, 5));
        pnlLinha1.add(new JLabel("Nome:"), BorderLayout.WEST);
        pnlLinha1.add(txtNome, BorderLayout.CENTER);

        JPanel pnlLinha2 = new JPanel(new GridLayout(1, 4, 10, 10));
        pnlLinha2.add(new JLabel("CPF:"));
        pnlLinha2.add(txtCpf);
        pnlLinha2.add(new JLabel("CEP:"));
        pnlLinha2.add(txtCep);

        JPanel pnlLinha3 = new JPanel(new GridLayout(1, 4, 10, 10));
        pnlLinha3.add(new JLabel("Rua:"));
        pnlLinha3.add(txtRua);
        pnlLinha3.add(new JLabel("Número:"));
        pnlLinha3.add(txtNumero);

        JPanel pnlLinha4 = new JPanel(new GridLayout(1, 2, 10, 10));
        pnlLinha4.add(new JLabel("Bairro:"));
        pnlLinha4.add(txtBairro);

        painelPrincipal.setLayout(new BoxLayout(painelPrincipal, BoxLayout.Y_AXIS));
        painelPrincipal.add(pnlLinha1);
        painelPrincipal.add(Box.createRigidArea(new Dimension(0, 5)));
        painelPrincipal.add(pnlLinha2);
        painelPrincipal.add(Box.createRigidArea(new Dimension(0, 5)));
        painelPrincipal.add(pnlLinha3);
        painelPrincipal.add(Box.createRigidArea(new Dimension(0, 5)));
        painelPrincipal.add(pnlLinha4);

        return painelPrincipal;
    }

    private void inserirCliente() {
        JTextField txtNome = new JTextField(20);
        JTextField txtCpf = new JTextField();
        JTextField txtRua = new JTextField();
        JTextField txtBairro = new JTextField();
        JTextField txtNumero = new JTextField();
        JTextField txtCep = new JTextField();

        JPanel painelForm = criarPainelFormulario(txtNome, txtCpf, txtRua, txtBairro, txtNumero, txtCep);

        int resultado = JOptionPane.showConfirmDialog(this, painelForm, "Inserir Novo Cliente", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            String nome = txtNome.getText().trim();
            String cpf = txtCpf.getText().trim();
            String rua = txtRua.getText().trim();
            String bairro = txtBairro.getText().trim();
            String numeroStr = txtNumero.getText().trim();
            String cep = txtCep.getText().trim();

            if (nome.isEmpty() || cpf.isEmpty() || rua.isEmpty() || bairro.isEmpty() || numeroStr.isEmpty() || cep.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos obrigatórios.");
                return;
            }

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
                JOptionPane.showMessageDialog(this, "Cliente inserido com sucesso!");
                atualizarListaClientes();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Erro: O campo número deve conter apenas valores inteiros positivos.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao inserir: " + ex.getMessage());
            }
        }
    }

    private String cpfDaLinhaSelecionada() {
        int linha = tabelaClientes.getSelectedRow();
        if (linha == -1) return null;
        int colunaCpf = tabelaClientes.getColumnModel().getColumnIndex("CPF");
        return String.valueOf(tabelaClientes.getValueAt(linha, colunaCpf));
    }

    private void editarClientePorCpf() {
        String cpfBusca = cpfDaLinhaSelecionada();
        if (cpfBusca == null) {
            cpfBusca = JOptionPane.showInputDialog(this, "Digite o CPF do cliente que deseja editar:");
        }
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

            JTextField txtNome = new JTextField(rs.getString("nome"));
            JTextField txtCpf = new JTextField(rs.getString("cpf"));
            JTextField txtRua = new JTextField(rs.getString("rua"));
            JTextField txtBairro = new JTextField(rs.getString("bairro"));
            JTextField txtNumero = new JTextField(String.valueOf(rs.getInt("numero")));
            JTextField txtCep = new JTextField(rs.getString("cep"));

            JPanel painelForm = criarPainelFormulario(txtNome, txtCpf, txtRua, txtBairro, txtNumero, txtCep);

            Object[] options = {"Salvar Alterações", "Cancelar"};
            int resultado = JOptionPane.showOptionDialog(this, painelForm, "Editar Cliente (CPF: " + cpfBusca + ")", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (resultado == JOptionPane.OK_OPTION) {
                String nome = txtNome.getText().trim();
                String cpf = txtCpf.getText().trim();
                String rua = txtRua.getText().trim();
                String bairro = txtBairro.getText().trim();
                String numeroStr = txtNumero.getText().trim();
                String cep = txtCep.getText().trim();

                if (nome.isEmpty() || cpf.isEmpty() || rua.isEmpty() || bairro.isEmpty() || numeroStr.isEmpty() || cep.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos obrigatórios.");
                    return;
                }

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
            JOptionPane.showMessageDialog(this, "Erro: O campo número deve conter apenas valores inteiros positivos.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + ex.getMessage());
        }
    }

    private void excluirClientePorCpf() {
        String cpfBusca = cpfDaLinhaSelecionada();
        if (cpfBusca == null) {
            cpfBusca = JOptionPane.showInputDialog(this, "Digite o CPF do cliente que deseja excluir:");
        }
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