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

public class PainelCamisas extends JPanel {
    private JTable tabelaCamisas;
    private JTextField campoBusca;
    private JLabel lblTotalCamisas;
    private static final String SQL_LISTA_CAMISAS = "SELECT c.id_camisa AS ID, c.modelo AS Modelo, COALESCE(e.nome, 'Sem Equipe') AS Equipe, c.versao AS Versao, c.tamanho AS Tamanho, c.preco AS Preco, c.ano AS Ano, c.quantidade_estoque AS Estoque, fn_total_vendido_camisa(c.id_camisa) AS Total_Vendido FROM Camisa c LEFT JOIN Equipe e ON c.fk_id_equipe = e.id_equipe WHERE c.modelo LIKE ? OR COALESCE(e.nome, 'Sem Equipe') LIKE ? ORDER BY c.id_camisa";

    public PainelCamisas() {
        UIUtils.configurarPainelBase(this);

        JButton btnInserirCamisa = UIUtils.criarBotao("Inserir", UIUtils.COR_SUCESSO);
        JButton btnEditarCamisa = UIUtils.criarBotao("Editar", UIUtils.COR_EDICAO);
        JButton btnExcluirCamisa = UIUtils.criarBotao("Excluir", UIUtils.COR_PERIGO);
        JButton btnReajustar = UIUtils.criarBotao("Reajustar", UIUtils.COR_NEUTRA);
        JButton btnRemessa = UIUtils.criarBotao("Remessa", UIUtils.COR_NEUTRA);
        JButton btnHistorico = UIUtils.criarBotao("Histórico", UIUtils.COR_NEUTRA);

        btnInserirCamisa.addActionListener(e -> inserirCamisa());
        btnEditarCamisa.addActionListener(e -> editarCamisa());
        btnExcluirCamisa.addActionListener(e -> excluirCamisa());
        btnReajustar.addActionListener(e -> reajustarPrecosEquipe());
        btnRemessa.addActionListener(e -> distribuirRemessa());
        btnHistorico.addActionListener(e -> mostrarHistoricoCamisas());

        campoBusca = UIUtils.criarCampoBusca("Buscar por modelo ou equipe...");
        UIUtils.executarAoDigitar(campoBusca, this::atualizarListaCamisas);

        campoBusca.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tabelaCamisas != null) {
                    tabelaCamisas.clearSelection();
                }
            }
        });

        tabelaCamisas = UIUtils.criarTabela();
        lblTotalCamisas = UIUtils.criarContador();
        
        JScrollPane scrollPaneTabela = UIUtils.criarRolagem(tabelaCamisas);
        
        scrollPaneTabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabelaCamisas.clearSelection();
            }
        });
        
        scrollPaneTabela.getViewport().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabelaCamisas.clearSelection();
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabelaCamisas.clearSelection();
            }
        });

        add(UIUtils.criarBarraTopo("Gerenciamento de Camisas", campoBusca, btnInserirCamisa, btnEditarCamisa, btnExcluirCamisa, btnReajustar, btnRemessa, btnHistorico), BorderLayout.NORTH);
        add(scrollPaneTabela, BorderLayout.CENTER);
        add(lblTotalCamisas, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::atualizarListaCamisas);
        UIUtils.recarregarAoExibir(this, this::atualizarListaCamisas);
    }

    private void atualizarListaCamisas() {
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) return;
            try (PreparedStatement stmt = conexao.prepareStatement(SQL_LISTA_CAMISAS)) {
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

                tabelaCamisas.setModel(UIUtils.criarModeloSomenteLeitura(dados, colunas));
                lblTotalCamisas.setText(dados.size() + " camisa(s) encontrada(s)");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar lista: " + ex.getMessage());
        }
    }

    private JPanel criarPainelFormulario(JTextField txtModelo, JComboBox<String> cbVersao, JComboBox<String> cbTamanho, 
                                         JTextField txtPreco, JTextField txtAno, JTextField txtEstoque, JTextField txtIdEquipe) {
        JPanel painelPrincipal = new JPanel(new GridLayout(4, 1, 10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlLinha1 = new JPanel(new BorderLayout(5, 5));
        pnlLinha1.add(new JLabel("Modelo:"), BorderLayout.WEST);
        pnlLinha1.add(txtModelo, BorderLayout.CENTER);

        JPanel pnlLinha2 = new JPanel(new GridLayout(1, 4, 10, 10));
        pnlLinha2.add(new JLabel("Versão:"));
        pnlLinha2.add(cbVersao);
        pnlLinha2.add(new JLabel("Tamanho:"));
        pnlLinha2.add(cbTamanho);

        JPanel pnlLinha3 = new JPanel(new GridLayout(1, 4, 10, 10));
        pnlLinha3.add(new JLabel("Preço (R$):"));
        pnlLinha3.add(txtPreco);
        pnlLinha3.add(new JLabel("Ano:"));
        pnlLinha3.add(txtAno);

        JPanel pnlLinha4 = new JPanel(new GridLayout(1, 4, 10, 10));
        pnlLinha4.add(new JLabel("Estoque:"));
        pnlLinha4.add(txtEstoque);
        pnlLinha4.add(new JLabel("ID Equipe:"));
        pnlLinha4.add(txtIdEquipe);

        painelPrincipal.add(pnlLinha1);
        painelPrincipal.add(pnlLinha2);
        painelPrincipal.add(pnlLinha3);
        painelPrincipal.add(pnlLinha4);

        return painelPrincipal;
    }

    private void inserirCamisa() {
        JTextField txtModelo = new JTextField(20);
        JComboBox<String> cbVersao = new JComboBox<>(new String[]{"Torcedor", "Jogador"});
        JComboBox<String> cbTamanho = new JComboBox<>(new String[]{"P", "M", "G", "GG"});
        JTextField txtPreco = new JTextField();
        JTextField txtAno = new JTextField();
        JTextField txtEstoque = new JTextField();
        JTextField txtIdEquipe = new JTextField();

        JPanel painelForm = criarPainelFormulario(txtModelo, cbVersao, cbTamanho, txtPreco, txtAno, txtEstoque, txtIdEquipe);

        int resultado = JOptionPane.showConfirmDialog(this, painelForm, "Inserir Nova Camisa", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            String modelo = txtModelo.getText().trim();
            String versao = cbVersao.getSelectedItem().toString();
            String tamanho = cbTamanho.getSelectedItem().toString();
            String precoStr = txtPreco.getText().trim().replace(",", ".");
            String anoStr = txtAno.getText().trim();
            String estoqueStr = txtEstoque.getText().trim();
            String idEquipeStr = txtIdEquipe.getText().trim();

            if (modelo.isEmpty() || precoStr.isEmpty() || anoStr.isEmpty() || estoqueStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos obrigatórios.");
                return;
            }

            String sql = "INSERT INTO Camisa (modelo, versao, tamanho, preco, ano, quantidade_estoque, fk_id_equipe) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conexao = ConexaoBanco.conectar();
                 PreparedStatement stmt = conexao.prepareStatement(sql)) {
                stmt.setString(1, modelo);
                stmt.setString(2, versao);
                stmt.setString(3, tamanho);
                stmt.setDouble(4, Double.parseDouble(precoStr));
                stmt.setInt(5, Integer.parseInt(anoStr));
                stmt.setInt(6, Integer.parseInt(estoqueStr));
                
                if (idEquipeStr.isEmpty()) {
                    stmt.setNull(7, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(7, Integer.parseInt(idEquipeStr));
                }
                
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Camisa inserida com sucesso!");
                atualizarListaCamisas();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Erro: Certifique-se de que Preço, Ano, Estoque e Equipe contêm apenas números válidos.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao inserir: " + ex.getMessage());
            }
        }
    }

    private String idDaLinhaSelecionada() {
        int linha = tabelaCamisas.getSelectedRow();
        if (linha == -1) return null;
        int colunaId = tabelaCamisas.getColumnModel().getColumnIndex("ID");
        return String.valueOf(tabelaCamisas.getValueAt(linha, colunaId));
    }

    private void editarCamisa() {
        String idBusca = idDaLinhaSelecionada();
        if (idBusca == null) {
            idBusca = JOptionPane.showInputDialog(this, "Digite o ID da camisa que deseja editar:");
        }
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

            JTextField txtModelo = new JTextField(rs.getString("modelo"));
            JComboBox<String> cbVersao = new JComboBox<>(new String[]{"Torcedor", "Jogador"});
            cbVersao.setSelectedItem(rs.getString("versao"));
            JComboBox<String> cbTamanho = new JComboBox<>(new String[]{"P", "M", "G", "GG"});
            cbTamanho.setSelectedItem(rs.getString("tamanho"));
            JTextField txtPreco = new JTextField(String.valueOf(rs.getDouble("preco")));
            JTextField txtAno = new JTextField(String.valueOf(rs.getInt("ano")));
            JTextField txtEstoque = new JTextField(String.valueOf(rs.getInt("quantidade_estoque")));
            
            int idEquipeAtual = rs.getInt("fk_id_equipe");
            boolean equipeNula = rs.wasNull();
            JTextField txtIdEquipe = new JTextField(equipeNula ? "" : String.valueOf(idEquipeAtual));

            JPanel painelForm = criarPainelFormulario(txtModelo, cbVersao, cbTamanho, txtPreco, txtAno, txtEstoque, txtIdEquipe);

            Object[] options = {"Salvar Alterações", "Cancelar"};
            int resultado = JOptionPane.showOptionDialog(this, painelForm, "Editar Camisa (ID: " + idBusca + ")", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (resultado == JOptionPane.OK_OPTION) {
                String modelo = txtModelo.getText().trim();
                String versao = cbVersao.getSelectedItem().toString();
                String tamanho = cbTamanho.getSelectedItem().toString();
                String precoStr = txtPreco.getText().trim().replace(",", ".");
                String anoStr = txtAno.getText().trim();
                String estoqueStr = txtEstoque.getText().trim();
                String idEquipeStr = txtIdEquipe.getText().trim();

                if (modelo.isEmpty() || precoStr.isEmpty() || anoStr.isEmpty() || estoqueStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos obrigatórios.");
                    return;
                }

                String sqlUpdate = "UPDATE Camisa SET modelo = ?, versao = ?, tamanho = ?, preco = ?, ano = ?, quantidade_estoque = ?, fk_id_equipe = ? WHERE id_camisa = ?";
                try (PreparedStatement stmtUpdate = conexao.prepareStatement(sqlUpdate)) {
                    stmtUpdate.setString(1, modelo);
                    stmtUpdate.setString(2, versao);
                    stmtUpdate.setString(3, tamanho);
                    stmtUpdate.setDouble(4, Double.parseDouble(precoStr));
                    stmtUpdate.setInt(5, Integer.parseInt(anoStr));
                    stmtUpdate.setInt(6, Integer.parseInt(estoqueStr));
                    
                    if (idEquipeStr.isEmpty()) {
                        stmtUpdate.setNull(7, java.sql.Types.INTEGER);
                    } else {
                        stmtUpdate.setInt(7, Integer.parseInt(idEquipeStr));
                    }
                    
                    stmtUpdate.setInt(8, Integer.parseInt(idBusca));
                    stmtUpdate.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Camisa atualizada com sucesso!");
                    atualizarListaCamisas();
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro de formatação. Preço, Ano, Estoque, e ID precisam ser numéricos.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + ex.getMessage());
        }
    }

    private void excluirCamisa() {
        String idBusca = idDaLinhaSelecionada();
        if (idBusca == null) {
            idBusca = JOptionPane.showInputDialog(this, "Digite o ID da camisa que deseja excluir:");
        }
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
                    atualizarListaCamisas();
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro: O ID informado deve ser um número inteiro.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }

    private void reajustarPrecosEquipe() {
        JTextField txtIdEquipe = new JTextField();
        JTextField txtPercentual = new JTextField();

        JPanel painelForm = new JPanel(new GridLayout(2, 2, 10, 10));
        painelForm.add(new JLabel("ID da Equipe:"));
        painelForm.add(txtIdEquipe);
        painelForm.add(new JLabel("Percentual (%):"));
        painelForm.add(txtPercentual);

        int resultado = JOptionPane.showConfirmDialog(this, painelForm, "Reajustar Preços da Equipe",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) return;

        try (Connection conexao = ConexaoBanco.conectar();
             CallableStatement stmt = conexao.prepareCall("{CALL sp_reajustar_precos_equipe(?, ?)}")) {
            stmt.setInt(1, Integer.parseInt(txtIdEquipe.getText().trim()));
            stmt.setBigDecimal(2, new java.math.BigDecimal(txtPercentual.getText().trim().replace(",", ".")));

            int camisasReajustadas = 0;
            if (stmt.execute()) {
                try (ResultSet rs = stmt.getResultSet()) {
                    if (rs.next()) camisasReajustadas = rs.getInt("Camisas_Reajustadas");
                }
            }
            JOptionPane.showMessageDialog(this, camisasReajustadas + " camisa(s) reajustada(s). As mudanças foram registradas no Histórico.");
            atualizarListaCamisas();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro: informe apenas números válidos para a equipe e o percentual.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao reajustar: " + ex.getMessage());
        }
    }

    private void distribuirRemessa() {
        JTextField txtPecas = new JTextField();
        JTextField txtEstoqueMinimo = new JTextField();

        JPanel painelForm = new JPanel(new GridLayout(2, 2, 10, 10));
        painelForm.add(new JLabel("Peças da remessa:"));
        painelForm.add(txtPecas);
        painelForm.add(new JLabel("Estoque mínimo:"));
        painelForm.add(txtEstoqueMinimo);

        int resultado = JOptionPane.showConfirmDialog(this, painelForm, "Distribuir Remessa",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) return;

        try (Connection conexao = ConexaoBanco.conectar();
             CallableStatement stmt = conexao.prepareCall("{CALL sp_distribuir_remessa(?, ?, ?)}")) {
            stmt.setInt(1, Integer.parseInt(txtPecas.getText().trim()));
            stmt.setInt(2, Integer.parseInt(txtEstoqueMinimo.getText().trim()));
            stmt.registerOutParameter(3, Types.INTEGER);

            JTable tabelaResultado = UIUtils.criarTabela();
            if (stmt.execute()) {
                try (ResultSet rs = stmt.getResultSet()) {
                    tabelaResultado = criarTabelaDoResultado(rs);
                }
            }
            int pecasRestantes = stmt.getInt(3);

            mostrarTabela("Resultado da Remessa", "Peças restantes da remessa: " + pecasRestantes, tabelaResultado);
            atualizarListaCamisas();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro: informe apenas números inteiros para as peças e o estoque mínimo.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao distribuir remessa: " + ex.getMessage());
        }
    }

    private void mostrarHistoricoCamisas() {
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmt = conexao.prepareStatement("SELECT * FROM Log_Camisa ORDER BY id_log DESC");
             ResultSet rs = stmt.executeQuery()) {
            JTable tabelaLog = criarTabelaDoResultado(rs);
            mostrarTabela("Histórico de Alterações de Camisas", tabelaLog.getRowCount() + " alteração(ões) registrada(s) pelo trigger", tabelaLog);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar histórico: " + ex.getMessage());
        }
    }

    private JTable criarTabelaDoResultado(ResultSet rs) throws SQLException {
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

        JTable tabela = UIUtils.criarTabela();
        tabela.setModel(UIUtils.criarModeloSomenteLeitura(dados, colunas));
        return tabela;
    }

    private void mostrarTabela(String titulo, String resumo, JTable tabela) {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.add(new JLabel(resumo), BorderLayout.NORTH);
        painel.add(UIUtils.criarRolagem(tabela), BorderLayout.CENTER);
        painel.setPreferredSize(new Dimension(850, 320));
        JOptionPane.showMessageDialog(this, painel, titulo, JOptionPane.PLAIN_MESSAGE);
    }
}