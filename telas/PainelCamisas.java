package telas;

import banco.ConexaoBanco;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class PainelCamisas extends JPanel {
    private JTable tabelaCamisas;
    private JTextField campoBusca;
    private JLabel lblTotalCamisas;
    private static final String SQL_LISTA_CAMISAS = "SELECT c.id_camisa AS ID, c.modelo AS Modelo, COALESCE(e.nome, 'Sem Equipe') AS Equipe, c.versao AS Versao, c.tamanho AS Tamanho, c.preco AS Preco, c.ano AS Ano, c.quantidade_estoque AS Estoque FROM Camisa c LEFT JOIN Equipe e ON c.fk_id_equipe = e.id_equipe WHERE c.modelo LIKE ? OR COALESCE(e.nome, 'Sem Equipe') LIKE ? ORDER BY c.id_camisa";

    public PainelCamisas() {
        UIUtils.configurarPainelBase(this);

        JButton btnInserirCamisa = UIUtils.criarBotao("Inserir", UIUtils.COR_SUCESSO);
        JButton btnEditarCamisa = UIUtils.criarBotao("Editar", UIUtils.COR_EDICAO);
        JButton btnExcluirCamisa = UIUtils.criarBotao("Excluir", UIUtils.COR_PERIGO);

        btnInserirCamisa.addActionListener(e -> inserirCamisa());
        btnEditarCamisa.addActionListener(e -> editarCamisa());
        btnExcluirCamisa.addActionListener(e -> excluirCamisa());

        campoBusca = UIUtils.criarCampoBusca("Buscar por modelo ou equipe...");
        UIUtils.executarAoDigitar(campoBusca, this::atualizarListaCamisas);

        tabelaCamisas = UIUtils.criarTabela();
        lblTotalCamisas = UIUtils.criarContador();
        add(UIUtils.criarBarraTopo("Gerenciamento de Camisas", campoBusca, btnInserirCamisa, btnEditarCamisa, btnExcluirCamisa), BorderLayout.NORTH);
        add(UIUtils.criarRolagem(tabelaCamisas), BorderLayout.CENTER);
        add(lblTotalCamisas, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::atualizarListaCamisas);
        UIUtils.recarregarAoExibir(this, this::atualizarListaCamisas);
    }

    private void atualizarListaCamisas() {
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) return;
            try (PreparedStatement stmt = conexao.prepareStatement(SQL_LISTA_CAMISAS)) {
                //busca vazia vira "%%", que traz todas as camisas
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
                atualizarListaCamisas();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Erro: Certifique-se de que Preço, Ano, Estoque e Equipe são números válidos.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao inserir: " + ex.getMessage());
            }
        }
    }

    //ID da linha selecionada na tabela, ou null se nenhuma linha estiver selecionada
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
}