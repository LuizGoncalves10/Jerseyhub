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

public class PainelFuncionarios extends JPanel {
    private JTable tabelaFuncionarios;
    private JTextField campoBusca;
    private JLabel lblTotalFuncionarios;
    private static final String SQL_LISTA_FUNCIONARIOS = "SELECT f.id_funcionario AS ID, f.nome AS Nome, CASE WHEN f.cargo LIKE 'Vended%' THEN 'Vendedor' WHEN f.cargo LIKE 'Supervis%' THEN 'Supervisor' ELSE 'Gerente' END AS Cargo, COALESCE(sup.nome, 'Nenhum') AS Supervisor FROM Funcionario f LEFT JOIN Supervisao s ON f.id_funcionario = s.fk_id_supervisionado LEFT JOIN Funcionario sup ON s.fk_id_supervisor = sup.id_funcionario WHERE f.nome LIKE ? OR f.cargo LIKE ? ORDER BY f.nome";

    private static class ComboItem {
        private final int id;
        private final String nome;

        public ComboItem(int id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return nome;
        }
    }

    public PainelFuncionarios() {
        UIUtils.configurarPainelBase(this);

        JButton btnInserir = UIUtils.criarBotao("Inserir", UIUtils.COR_SUCESSO);
        JButton btnEditar = UIUtils.criarBotao("Editar", UIUtils.COR_EDICAO);
        JButton btnExcluir = UIUtils.criarBotao("Excluir", UIUtils.COR_PERIGO);

        btnInserir.addActionListener(e -> inserirFuncionario());
        btnEditar.addActionListener(e -> editarFuncionario());
        btnExcluir.addActionListener(e -> excluirFuncionario());

        campoBusca = UIUtils.criarCampoBusca("Buscar por nome ou cargo...");
        UIUtils.executarAoDigitar(campoBusca, this::atualizarListaFuncionarios);

        campoBusca.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tabelaFuncionarios != null) {
                    tabelaFuncionarios.clearSelection();
                }
            }
        });

        tabelaFuncionarios = UIUtils.criarTabela();
        lblTotalFuncionarios = UIUtils.criarContador();

        JScrollPane scrollPaneTabela = UIUtils.criarRolagem(tabelaFuncionarios);

        scrollPaneTabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabelaFuncionarios.clearSelection();
            }
        });

        scrollPaneTabela.getViewport().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabelaFuncionarios.clearSelection();
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabelaFuncionarios.clearSelection();
            }
        });

        add(UIUtils.criarBarraTopo("Gerenciamento de Funcionários", campoBusca, btnInserir, btnEditar, btnExcluir), BorderLayout.NORTH);
        add(scrollPaneTabela, BorderLayout.CENTER);
        add(lblTotalFuncionarios, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::atualizarListaFuncionarios);
        UIUtils.recarregarAoExibir(this, this::atualizarListaFuncionarios);
    }

    private void atualizarListaFuncionarios() {
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) return;
            try (PreparedStatement stmt = conexao.prepareStatement(SQL_LISTA_FUNCIONARIOS)) {
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

                tabelaFuncionarios.setModel(UIUtils.criarModeloSomenteLeitura(dados, colunas));
                lblTotalFuncionarios.setText(dados.size() + " funcionário(s) encontrado(s)");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar lista: " + ex.getMessage());
        }
    }

    private void carregarSupervisores(JComboBox<ComboItem> combo, int idIgnorar, String cargoFiltro) {
        combo.removeAllItems();
        combo.addItem(new ComboItem(0, "Nenhum"));

        if (cargoFiltro.equals("Nenhum")) {
            combo.setEnabled(false);
            return;
        }
        combo.setEnabled(true);

        String sql = "SELECT id_funcionario, nome FROM Funcionario WHERE id_funcionario != ? AND cargo LIKE ? ORDER BY nome";
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idIgnorar);
            stmt.setString(2, cargoFiltro + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                combo.addItem(new ComboItem(rs.getInt("id_funcionario"), rs.getString("nome")));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar supervisores: " + ex.getMessage());
        }
    }

    private void configurarComportamentoCargo(JComboBox<String> cbCargo, JComboBox<ComboItem> cbSupervisor, int idIgnorar) {
        cbCargo.addActionListener(e -> {
            String selecionado = cbCargo.getSelectedItem().toString();
            if (selecionado.equals("Gerente")) {
                carregarSupervisores(cbSupervisor, idIgnorar, "Nenhum");
            } else if (selecionado.equals("Supervisor")) {
                carregarSupervisores(cbSupervisor, idIgnorar, "Gerent");
            } else {
                carregarSupervisores(cbSupervisor, idIgnorar, "Supervis");
            }
        });
    }

    private JPanel criarPainelFormulario(JTextField txtNome, JComboBox<String> cbCargo, JComboBox<ComboItem> cbSupervisor) {
        JPanel painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BoxLayout(painelPrincipal, BoxLayout.Y_AXIS));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlLinha1 = new JPanel(new BorderLayout(5, 5));
        pnlLinha1.add(new JLabel("Nome:"), BorderLayout.WEST);
        pnlLinha1.add(txtNome, BorderLayout.CENTER);

        JPanel pnlLinha2 = new JPanel(new GridLayout(1, 4, 10, 10));
        pnlLinha2.add(new JLabel("Cargo:"));
        pnlLinha2.add(cbCargo);
        pnlLinha2.add(new JLabel("Supervisor Direto:"));
        pnlLinha2.add(cbSupervisor);

        painelPrincipal.add(pnlLinha1);
        painelPrincipal.add(Box.createRigidArea(new Dimension(0, 5)));
        painelPrincipal.add(pnlLinha2);

        return painelPrincipal;
    }

    private void inserirFuncionario() {
        JTextField txtNome = new JTextField(20);
        JComboBox<String> cbCargo = new JComboBox<>(new String[]{"Vendedor", "Supervisor", "Gerente"});
        JComboBox<ComboItem> cbSupervisor = new JComboBox<>();
        
        carregarSupervisores(cbSupervisor, 0, "Supervis");
        configurarComportamentoCargo(cbCargo, cbSupervisor, 0);

        JPanel painelForm = criarPainelFormulario(txtNome, cbCargo, cbSupervisor);

        int resultado = JOptionPane.showConfirmDialog(this, painelForm, "Inserir Novo Funcionário", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            String nome = txtNome.getText().trim();
            String cargo = cbCargo.getSelectedItem().toString();
            ComboItem supervisorSelecionado = (ComboItem) cbSupervisor.getSelectedItem();

            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, preencha o nome.");
                return;
            }

            try (Connection conexao = ConexaoBanco.conectar()) {
                conexao.setAutoCommit(false);
                String sqlFuncionario = "INSERT INTO Funcionario (nome, cargo) VALUES (?, ?)";
                
                try (PreparedStatement stmt = conexao.prepareStatement(sqlFuncionario, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, nome);
                    stmt.setString(2, cargo);
                    stmt.executeUpdate();

                    ResultSet rsKeys = stmt.getGeneratedKeys();
                    if (rsKeys.next()) {
                        int novoId = rsKeys.getInt(1);
                        int supervisorId = (supervisorSelecionado != null) ? supervisorSelecionado.getId() : 0;

                        if (supervisorId > 0) {
                            String sqlSupervisao = "INSERT INTO Supervisao (fk_id_supervisor, fk_id_supervisionado) VALUES (?, ?)";
                            try (PreparedStatement stmtSup = conexao.prepareStatement(sqlSupervisao)) {
                                stmtSup.setInt(1, supervisorId);
                                stmtSup.setInt(2, novoId);
                                stmtSup.executeUpdate();
                            }
                        }
                    }
                    conexao.commit();
                    JOptionPane.showMessageDialog(this, "Funcionário inserido com sucesso!");
                    atualizarListaFuncionarios();
                } catch (SQLException ex) {
                    conexao.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao inserir: " + ex.getMessage());
            }
        }
    }

    private String idDaLinhaSelecionada() {
        int linha = tabelaFuncionarios.getSelectedRow();
        if (linha == -1) return null;
        int colunaId = tabelaFuncionarios.getColumnModel().getColumnIndex("ID");
        return String.valueOf(tabelaFuncionarios.getValueAt(linha, colunaId));
    }

    private void editarFuncionario() {
        String idBusca = idDaLinhaSelecionada();
        if (idBusca == null) {
            idBusca = JOptionPane.showInputDialog(this, "Digite o ID do funcionário que deseja editar:");
        }
        if (idBusca == null || idBusca.trim().isEmpty()) return;

        int idFunc = Integer.parseInt(idBusca);

        String sqlBusca = "SELECT f.nome, f.cargo, s.fk_id_supervisor FROM Funcionario f LEFT JOIN Supervisao s ON f.id_funcionario = s.fk_id_supervisionado WHERE f.id_funcionario = ?";
        
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmtBusca = conexao.prepareStatement(sqlBusca)) {
            stmtBusca.setInt(1, idFunc);
            ResultSet rs = stmtBusca.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Erro: Funcionário com o ID informado não foi encontrado.");
                return;
            }

            String nomeAtual = rs.getString("nome");
            String cargoBanco = rs.getString("cargo").toLowerCase();
            String cargoAtual = "Gerente";
            
            if (cargoBanco.startsWith("vended")) {
                cargoAtual = "Vendedor";
            } else if (cargoBanco.startsWith("supervis")) {
                cargoAtual = "Supervisor";
            }
            
            int supervisorAtualId = rs.getInt("fk_id_supervisor");
            if (rs.wasNull()) supervisorAtualId = 0;

            JTextField txtNome = new JTextField(nomeAtual);
            JComboBox<String> cbCargo = new JComboBox<>(new String[]{"Vendedor", "Supervisor", "Gerente"});
            JComboBox<ComboItem> cbSupervisor = new JComboBox<>();
            
            if (cargoAtual.equals("Gerente")) {
                carregarSupervisores(cbSupervisor, idFunc, "Nenhum");
            } else if (cargoAtual.equals("Supervisor")) {
                carregarSupervisores(cbSupervisor, idFunc, "Gerent");
            } else {
                carregarSupervisores(cbSupervisor, idFunc, "Supervis");
            }

            configurarComportamentoCargo(cbCargo, cbSupervisor, idFunc);
            cbCargo.setSelectedItem(cargoAtual);
            
            for (int i = 0; i < cbSupervisor.getItemCount(); i++) {
                if (cbSupervisor.getItemAt(i).getId() == supervisorAtualId) {
                    cbSupervisor.setSelectedIndex(i);
                    break;
                }
            }

            JPanel painelForm = criarPainelFormulario(txtNome, cbCargo, cbSupervisor);

            Object[] options = {"Salvar Alterações", "Cancelar"};
            int resultado = JOptionPane.showOptionDialog(this, painelForm, "Editar Funcionário (ID: " + idFunc + ")", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (resultado == JOptionPane.OK_OPTION) {
                String nome = txtNome.getText().trim();
                String cargo = cbCargo.getSelectedItem().toString();
                ComboItem supervisorSelecionado = (ComboItem) cbSupervisor.getSelectedItem();

                if (nome.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Por favor, preencha o nome.");
                    return;
                }

                conexao.setAutoCommit(false);
                try {
                    String sqlUpdate = "UPDATE Funcionario SET nome = ?, cargo = ? WHERE id_funcionario = ?";
                    try (PreparedStatement stmtUpdate = conexao.prepareStatement(sqlUpdate)) {
                        stmtUpdate.setString(1, nome);
                        stmtUpdate.setString(2, cargo);
                        stmtUpdate.setInt(3, idFunc);
                        stmtUpdate.executeUpdate();
                    }

                    String sqlDeleteSup = "DELETE FROM Supervisao WHERE fk_id_supervisionado = ?";
                    try (PreparedStatement stmtDel = conexao.prepareStatement(sqlDeleteSup)) {
                        stmtDel.setInt(1, idFunc);
                        stmtDel.executeUpdate();
                    }

                    int novoSupervisorId = (supervisorSelecionado != null) ? supervisorSelecionado.getId() : 0;
                    if (novoSupervisorId > 0) {
                        String sqlInsertSup = "INSERT INTO Supervisao (fk_id_supervisor, fk_id_supervisionado) VALUES (?, ?)";
                        try (PreparedStatement stmtIns = conexao.prepareStatement(sqlInsertSup)) {
                            stmtIns.setInt(1, novoSupervisorId);
                            stmtIns.setInt(2, idFunc);
                            stmtIns.executeUpdate();
                        }
                    }

                    conexao.commit();
                    JOptionPane.showMessageDialog(this, "Funcionário atualizado com sucesso!");
                    atualizarListaFuncionarios();
                } catch (SQLException ex) {
                    conexao.rollback();
                    throw ex;
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro: O ID deve ser um número.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + ex.getMessage());
        }
    }

    private void excluirFuncionario() {
        String idBusca = idDaLinhaSelecionada();
        if (idBusca == null) {
            idBusca = JOptionPane.showInputDialog(this, "Digite o ID do funcionário que deseja excluir:");
        }
        if (idBusca == null || idBusca.trim().isEmpty()) return;

        String sqlBusca = "SELECT nome FROM Funcionario WHERE id_funcionario = ?";
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmtBusca = conexao.prepareStatement(sqlBusca)) {
            stmtBusca.setInt(1, Integer.parseInt(idBusca));
            ResultSet rs = stmtBusca.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Erro: Funcionário não encontrado.");
                return;
            }

            String nomeFuncionario = rs.getString("nome");
            int confirmacao = JOptionPane.showConfirmDialog(this, 
                "Deseja confirmar a exclusão do funcionário: " + nomeFuncionario + " (ID: " + idBusca + ")?", 
                "Confirmação de Exclusão", 
                JOptionPane.YES_NO_OPTION);

            if (confirmacao == JOptionPane.YES_OPTION) {
                String sqlDelete = "DELETE FROM Funcionario WHERE id_funcionario = ?";
                try (PreparedStatement stmtDelete = conexao.prepareStatement(sqlDelete)) {
                    stmtDelete.setInt(1, Integer.parseInt(idBusca));
                    stmtDelete.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Funcionário excluído com sucesso!");
                    atualizarListaFuncionarios();
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro: O ID deve ser um número inteiro.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir. Verifique se o funcionário não está atrelado a um pedido: " + ex.getMessage());
        }
    }
}