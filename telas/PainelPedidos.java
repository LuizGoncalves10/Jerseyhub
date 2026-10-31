package telas;

import banco.ConexaoBanco;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PainelPedidos extends JPanel {
    private JTable tabelaPedidos;
    private JTextField campoBusca;
    private JLabel lblTotalPedidos;
    private static final String SQL_LISTA_PEDIDOS = "SELECT p.id_pedido AS ID, c.nome AS Cliente, f.nome AS Vendedor, p.data_compra AS Data, p.valor_total AS Total FROM Pedido p JOIN Cliente c ON p.fk_id_cliente = c.id_cliente JOIN Funcionario f ON p.fk_id_funcionario = f.id_funcionario WHERE c.nome LIKE ? OR f.nome LIKE ? ORDER BY p.id_pedido DESC";

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

    private static class ComboTime {
        private final int id;
        private final String nome;

        public ComboTime(int id, String nome) {
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

    private static class ComboCamisa {
        private final int id;
        private final String descricao;
        private final double preco;

        public ComboCamisa(int id, String descricao, double preco) {
            this.id = id;
            this.descricao = descricao;
            this.preco = preco;
        }

        public int getId() {
            return id;
        }

        public double getPreco() {
            return preco;
        }

        @Override
        public String toString() {
            return descricao;
        }
    }

    private static class LinhaItemPedido {
        public JComboBox<ComboTime> cbTime;
        public JComboBox<String> cbAno;
        public JComboBox<String> cbModelo;
        public JComboBox<ComboCamisa> cbCamisaFinal;
        public JTextField txtQuantidade;

        public LinhaItemPedido(JComboBox<ComboTime> cbTime, JComboBox<String> cbAno, JComboBox<String> cbModelo, JComboBox<ComboCamisa> cbCamisaFinal, JTextField txtQuantidade) {
            this.cbTime = cbTime;
            this.cbAno = cbAno;
            this.cbModelo = cbModelo;
            this.cbCamisaFinal = cbCamisaFinal;
            this.txtQuantidade = txtQuantidade;
        }
    }

    public PainelPedidos() {
        UIUtils.configurarPainelBase(this);

        JButton btnInserir = UIUtils.criarBotao("Inserir", UIUtils.COR_SUCESSO);
        JButton btnEditar = UIUtils.criarBotao("Editar", UIUtils.COR_EDICAO);
        JButton btnExcluir = UIUtils.criarBotao("Excluir", UIUtils.COR_PERIGO);

        btnInserir.addActionListener(e -> inserirPedido());
        btnEditar.addActionListener(e -> editarPedido());
        btnExcluir.addActionListener(e -> excluirPedido());

        campoBusca = UIUtils.criarCampoBusca("Buscar por cliente ou vendedor...");
        UIUtils.executarAoDigitar(campoBusca, this::atualizarListaPedidos);

        campoBusca.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tabelaPedidos != null) {
                    tabelaPedidos.clearSelection();
                }
            }
        });

        tabelaPedidos = UIUtils.criarTabela();
        lblTotalPedidos = UIUtils.criarContador();

        JScrollPane scrollPaneTabela = UIUtils.criarRolagem(tabelaPedidos);

        scrollPaneTabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabelaPedidos.clearSelection();
            }
        });

        scrollPaneTabela.getViewport().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabelaPedidos.clearSelection();
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabelaPedidos.clearSelection();
            }
        });

        add(UIUtils.criarBarraTopo("Gerenciamento de Pedidos", campoBusca, btnInserir, btnEditar, btnExcluir), BorderLayout.NORTH);
        add(scrollPaneTabela, BorderLayout.CENTER);
        add(lblTotalPedidos, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::atualizarListaPedidos);
        UIUtils.recarregarAoExibir(this, this::atualizarListaPedidos);
    }

    private void atualizarListaPedidos() {
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) return;
            try (PreparedStatement stmt = conexao.prepareStatement(SQL_LISTA_PEDIDOS)) {
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

                tabelaPedidos.setModel(UIUtils.criarModeloSomenteLeitura(dados, colunas));
                lblTotalPedidos.setText(dados.size() + " pedido(s) encontrado(s)");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar lista: " + ex.getMessage());
        }
    }

    private Vector<ComboItem> buscarClientes() {
        Vector<ComboItem> clientes = new Vector<>();
        String sql = "SELECT id_cliente, nome, cpf FROM Cliente ORDER BY nome";
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                clientes.add(new ComboItem(rs.getInt("id_cliente"), rs.getString("nome") + " (" + rs.getString("cpf") + ")"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return clientes;
    }

    private Vector<ComboItem> buscarFuncionarios() {
        Vector<ComboItem> funcionarios = new Vector<>();
        String sql = "SELECT id_funcionario, nome, cargo FROM Funcionario ORDER BY nome";
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                funcionarios.add(new ComboItem(rs.getInt("id_funcionario"), rs.getString("nome") + " - " + rs.getString("cargo")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return funcionarios;
    }

    private Vector<ComboTime> buscarTimes() {
        Vector<ComboTime> times = new Vector<>();
        String sql = "SELECT id_equipe, nome FROM Equipe ORDER BY nome";
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                times.add(new ComboTime(rs.getInt("id_equipe"), rs.getString("nome")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return times;
    }

    private void atualizarAnosParaTime(JComboBox<ComboTime> cbTime, JComboBox<String> cbAno) {
        cbAno.removeAllItems();
        ComboTime timeSel = (ComboTime) cbTime.getSelectedItem();
        if (timeSel == null) return;

        String sql = "SELECT DISTINCT ano FROM Camisa WHERE fk_id_equipe = ? ORDER BY ano DESC";
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, timeSel.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cbAno.addItem(String.valueOf(rs.getInt("ano")));
            }
            cbAno.setSelectedIndex(-1);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void atualizarModelosParaTimeEAno(JComboBox<ComboTime> cbTime, JComboBox<String> cbAno, JComboBox<String> cbModelo) {
        cbModelo.removeAllItems();
        ComboTime timeSel = (ComboTime) cbTime.getSelectedItem();
        String anoSel = (String) cbAno.getSelectedItem();
        if (timeSel == null || anoSel == null) return;

        String sql = "SELECT DISTINCT modelo FROM Camisa WHERE fk_id_equipe = ? AND ano = ? ORDER BY modelo";
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, timeSel.getId());
            stmt.setInt(2, Integer.parseInt(anoSel));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cbModelo.addItem(rs.getString("modelo"));
            }
            cbModelo.setSelectedIndex(-1);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void atualizarCamisasFinais(JComboBox<ComboTime> cbTime, JComboBox<String> cbAno, JComboBox<String> cbModelo, JComboBox<ComboCamisa> cbCamisaFinal) {
        cbCamisaFinal.removeAllItems();
        ComboTime timeSel = (ComboTime) cbTime.getSelectedItem();
        String anoSel = (String) cbAno.getSelectedItem();
        String modeloSel = (String) cbModelo.getSelectedItem();
        if (timeSel == null || anoSel == null || modeloSel == null) return;

        String sql = "SELECT id_camisa, versao, tamanho, preco FROM Camisa WHERE fk_id_equipe = ? AND ano = ? AND modelo = ?";
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, timeSel.getId());
            stmt.setInt(2, Integer.parseInt(anoSel));
            stmt.setString(3, modeloSel);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String desc = rs.getString("versao") + " - Tam: " + rs.getString("tamanho") + " (R$ " + rs.getDouble("preco") + ")";
                cbCamisaFinal.addItem(new ComboCamisa(rs.getInt("id_camisa"), desc, rs.getDouble("preco")));
            }
            cbCamisaFinal.setSelectedIndex(-1);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void adicionarNovaLinhaCamisa(JPanel painelItens, List<LinhaItemPedido> linhasAtivas) {
        JPanel painelLinha = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        Vector<ComboTime> times = buscarTimes();
        JComboBox<ComboTime> cbTime = new JComboBox<>(times);
        cbTime.setPreferredSize(new Dimension(150, 25));

        JComboBox<String> cbAno = new JComboBox<>();
        cbAno.setPreferredSize(new Dimension(80, 25));

        JComboBox<String> cbModelo = new JComboBox<>();
        cbModelo.setPreferredSize(new Dimension(120, 25));

        JComboBox<ComboCamisa> cbCamisaFinal = new JComboBox<>();
        cbCamisaFinal.setPreferredSize(new Dimension(280, 25));

        JTextField txtQuantidade = new JTextField("", 3);
        JButton btnRemover = new JButton("X");
        btnRemover.setBackground(UIUtils.COR_PERIGO);
        btnRemover.setForeground(Color.WHITE);
        btnRemover.setFocusPainted(false);
        btnRemover.setPreferredSize(new Dimension(45, 25));

        cbTime.addActionListener(e -> {
            cbAno.removeAllItems();
            cbModelo.removeAllItems();
            cbCamisaFinal.removeAllItems();
            if (cbTime.getSelectedItem() != null) {
                atualizarAnosParaTime(cbTime, cbAno);
            }
        });

        cbAno.addActionListener(e -> {
            cbModelo.removeAllItems();
            cbCamisaFinal.removeAllItems();
            if (cbAno.getSelectedItem() != null) {
                atualizarModelosParaTimeEAno(cbTime, cbAno, cbModelo);
            }
        });

        cbModelo.addActionListener(e -> {
            cbCamisaFinal.removeAllItems();
            if (cbModelo.getSelectedItem() != null) {
                atualizarCamisasFinais(cbTime, cbAno, cbModelo, cbCamisaFinal);
            }
        });

        cbTime.setSelectedIndex(-1);

        LinhaItemPedido linha = new LinhaItemPedido(cbTime, cbAno, cbModelo, cbCamisaFinal, txtQuantidade);
        linhasAtivas.add(linha);

        btnRemover.addActionListener(e -> {
            painelItens.remove(painelLinha);
            linhasAtivas.remove(linha);
            painelItens.revalidate();
            painelItens.repaint();
        });

        painelLinha.add(new JLabel("Time:"));
        painelLinha.add(cbTime);
        painelLinha.add(new JLabel("Temp:"));
        painelLinha.add(cbAno);
        painelLinha.add(new JLabel("Mod:"));
        painelLinha.add(cbModelo);
        painelLinha.add(new JLabel("Versão:"));
        painelLinha.add(cbCamisaFinal);
        painelLinha.add(new JLabel("Qtd:"));
        painelLinha.add(txtQuantidade);
        painelLinha.add(btnRemover);

        painelItens.add(painelLinha);
        painelItens.revalidate();
        painelItens.repaint();
    }

    private void selecionarComboItemPorId(JComboBox<ComboItem> combo, int idProcurado) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getId() == idProcurado) {
                combo.setSelectedIndex(i);
                break;
            }
        }
    }

    private String idDaLinhaSelecionada() {
        int linha = tabelaPedidos.getSelectedRow();
        if (linha == -1) return null;
        int colunaId = tabelaPedidos.getColumnModel().getColumnIndex("ID");
        return String.valueOf(tabelaPedidos.getValueAt(linha, colunaId));
    }

    private void inserirPedido() {
        Vector<ComboItem> clientes = buscarClientes();
        Vector<ComboItem> funcionarios = buscarFuncionarios();
        Vector<ComboTime> times = buscarTimes();

        if (clientes.isEmpty() || funcionarios.isEmpty() || times.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Certifique-se de que há clientes, funcionários e equipes cadastradas.");
            return;
        }

        JComboBox<ComboItem> cbCliente = new JComboBox<>(clientes);
        cbCliente.setSelectedIndex(-1);
        
        JComboBox<ComboItem> cbFuncionario = new JComboBox<>(funcionarios);
        cbFuncionario.setSelectedIndex(-1);
        
        JTextField txtData = new JTextField("");

        JPanel painelTop = new JPanel(new GridLayout(3, 2, 10, 10));
        painelTop.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        painelTop.add(new JLabel("Cliente:"));
        painelTop.add(cbCliente);
        painelTop.add(new JLabel("Vendedor Responsável:"));
        painelTop.add(cbFuncionario);
        painelTop.add(new JLabel("Data do Pedido (AAAA-MM-DD):"));
        painelTop.add(txtData);

        JPanel painelItensLista = new JPanel();
        painelItensLista.setLayout(new BoxLayout(painelItensLista, BoxLayout.Y_AXIS));
        JScrollPane scrollItens = new JScrollPane(painelItensLista);
        scrollItens.setPreferredSize(new Dimension(950, 300));

        List<LinhaItemPedido> linhasAtivas = new ArrayList<>();
        adicionarNovaLinhaCamisa(painelItensLista, linhasAtivas);

        JButton btnAdicionarCamisa = new JButton("Adicionar Mais uma Camisa");
        btnAdicionarCamisa.addActionListener(e -> adicionarNovaLinhaCamisa(painelItensLista, linhasAtivas));

        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.add(painelTop, BorderLayout.NORTH);
        painelPrincipal.add(scrollItens, BorderLayout.CENTER);
        painelPrincipal.add(btnAdicionarCamisa, BorderLayout.SOUTH);

        boolean salvo = false;
        while (!salvo) {
            int resultado = JOptionPane.showConfirmDialog(this, painelPrincipal, "Criar Novo Pedido", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (resultado == JOptionPane.OK_OPTION) {
                if (cbCliente.getSelectedItem() == null || cbFuncionario.getSelectedItem() == null || txtData.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Por favor, preencha o Cliente, Vendedor Responsável e a Data do Pedido.");
                    continue;
                }

                if (linhasAtivas.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "O pedido precisa ter pelo menos uma camisa.");
                    continue;
                }

                boolean itensValidos = true;
                for (LinhaItemPedido linha : linhasAtivas) {
                    if (linha.cbCamisaFinal.getSelectedItem() == null || linha.txtQuantidade.getText().trim().isEmpty()) {
                        itensValidos = false;
                        break;
                    }
                }

                if (!itensValidos) {
                    JOptionPane.showMessageDialog(this, "Por favor, selecione a camisa final e informe a quantidade em todas as linhas.");
                    continue;
                }

                ComboItem clienteSelecionado = (ComboItem) cbCliente.getSelectedItem();
                ComboItem funcionarioSelecionado = (ComboItem) cbFuncionario.getSelectedItem();

                try (Connection conexao = ConexaoBanco.conectar()) {
                    conexao.setAutoCommit(false);
                    try {
                        Date dataPedido = Date.valueOf(txtData.getText().trim());
                        double valorTotalPedido = 0.0;

                        for (LinhaItemPedido linha : linhasAtivas) {
                            ComboCamisa camisa = (ComboCamisa) linha.cbCamisaFinal.getSelectedItem();
                            int qtd = Integer.parseInt(linha.txtQuantidade.getText().trim());
                            if (qtd <= 0) {
                                throw new NumberFormatException("Quantidade menor ou igual a zero.");
                            }
                            valorTotalPedido += (camisa.getPreco() * qtd);
                        }

                        String sqlPedido = "INSERT INTO Pedido (valor_total, data_compra, fk_id_cliente, fk_id_funcionario) VALUES (?, ?, ?, ?)";
                        int idPedidoGerado = -1;

                        try (PreparedStatement stmtPedido = conexao.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                            stmtPedido.setDouble(1, valorTotalPedido);
                            stmtPedido.setDate(2, dataPedido);
                            stmtPedido.setInt(3, clienteSelecionado.getId());
                            stmtPedido.setInt(4, funcionarioSelecionado.getId());
                            stmtPedido.executeUpdate();

                            ResultSet rsKeys = stmtPedido.getGeneratedKeys();
                            if (rsKeys.next()) {
                                idPedidoGerado = rsKeys.getInt(1);
                            }
                        }

                        String sqlItem = "INSERT INTO Item_Pedido (quantidade, subtotal_item, fk_id_pedido, fk_id_camisa) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement stmtItem = conexao.prepareStatement(sqlItem)) {
                            for (LinhaItemPedido linha : linhasAtivas) {
                                ComboCamisa camisa = (ComboCamisa) linha.cbCamisaFinal.getSelectedItem();
                                int qtd = Integer.parseInt(linha.txtQuantidade.getText().trim());
                                double subtotal = camisa.getPreco() * qtd;

                                stmtItem.setInt(1, qtd);
                                stmtItem.setDouble(2, subtotal);
                                stmtItem.setInt(3, idPedidoGerado);
                                stmtItem.setInt(4, camisa.getId());
                                stmtItem.addBatch();
                            }
                            stmtItem.executeBatch();
                        }

                        conexao.commit();
                        JOptionPane.showMessageDialog(this, "Pedido criado com sucesso!");
                        atualizarListaPedidos();
                        salvo = true;

                    } catch (NumberFormatException ex) {
                        conexao.rollback();
                        JOptionPane.showMessageDialog(this, "Erro: As quantidades devem ser números inteiros maiores que zero.");
                    } catch (IllegalArgumentException ex) {
                        conexao.rollback();
                        JOptionPane.showMessageDialog(this, "Erro: Formato de data inválido. Use AAAA-MM-DD.");
                    } catch (SQLException ex) {
                        conexao.rollback();
                        JOptionPane.showMessageDialog(this, "Erro ao registrar o pedido no banco: " + ex.getMessage());
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erro geral de conexão: " + ex.getMessage());
                }
            } else {
                break;
            }
        }
    }

    private void editarPedido() {
        String idBusca = idDaLinhaSelecionada();
        if (idBusca == null) {
            idBusca = JOptionPane.showInputDialog(this, "Digite o ID do pedido que deseja editar:");
        }
        if (idBusca == null || idBusca.trim().isEmpty()) return;

        int idPedido = Integer.parseInt(idBusca);

        try (Connection conexao = ConexaoBanco.conectar()) {
            String sqlBuscaPedido = "SELECT data_compra, fk_id_cliente, fk_id_funcionario FROM Pedido WHERE id_pedido = ?";
            PreparedStatement stmtBusca = conexao.prepareStatement(sqlBuscaPedido);
            stmtBusca.setInt(1, idPedido);
            ResultSet rsPedido = stmtBusca.executeQuery();

            if (!rsPedido.next()) {
                JOptionPane.showMessageDialog(this, "Erro: Pedido não encontrado.");
                return;
            }

            Date dataAtual = rsPedido.getDate("data_compra");
            int idClienteAtual = rsPedido.getInt("fk_id_cliente");
            int idFuncionarioAtual = rsPedido.getInt("fk_id_funcionario");

            Vector<ComboItem> clientes = buscarClientes();
            Vector<ComboItem> funcionarios = buscarFuncionarios();

            JComboBox<ComboItem> cbCliente = new JComboBox<>(clientes);
            selecionarComboItemPorId(cbCliente, idClienteAtual);

            JComboBox<ComboItem> cbFuncionario = new JComboBox<>(funcionarios);
            selecionarComboItemPorId(cbFuncionario, idFuncionarioAtual);

            JTextField txtData = new JTextField(dataAtual.toString());

            JPanel painelTop = new JPanel(new GridLayout(3, 2, 10, 10));
            painelTop.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
            painelTop.add(new JLabel("Cliente:"));
            painelTop.add(cbCliente);
            painelTop.add(new JLabel("Vendedor Responsável:"));
            painelTop.add(cbFuncionario);
            painelTop.add(new JLabel("Data do Pedido (AAAA-MM-DD):"));
            painelTop.add(txtData);

            JPanel painelItensLista = new JPanel();
            painelItensLista.setLayout(new BoxLayout(painelItensLista, BoxLayout.Y_AXIS));
            JScrollPane scrollItens = new JScrollPane(painelItensLista);
            scrollItens.setPreferredSize(new Dimension(950, 300));

            List<LinhaItemPedido> linhasAtivas = new ArrayList<>();

            String sqlBuscaItens = "SELECT i.fk_id_camisa, i.quantidade, c.fk_id_equipe, c.ano, c.modelo FROM Item_Pedido i JOIN Camisa c ON i.fk_id_camisa = c.id_camisa WHERE i.fk_id_pedido = ?";
            PreparedStatement stmtBuscaItens = conexao.prepareStatement(sqlBuscaItens);
            stmtBuscaItens.setInt(1, idPedido);
            ResultSet rsItens = stmtBuscaItens.executeQuery();

            while (rsItens.next()) {
                adicionarNovaLinhaCamisa(painelItensLista, linhasAtivas);
                LinhaItemPedido ultimaLinha = linhasAtivas.get(linhasAtivas.size() - 1);
                
                int equipeId = rsItens.getInt("fk_id_equipe");
                for (int i = 0; i < ultimaLinha.cbTime.getItemCount(); i++) {
                    if (ultimaLinha.cbTime.getItemAt(i).getId() == equipeId) {
                        ultimaLinha.cbTime.setSelectedIndex(i);
                        break;
                    }
                }
                
                String anoStr = String.valueOf(rsItens.getInt("ano"));
                ultimaLinha.cbAno.setSelectedItem(anoStr);

                String modeloStr = rsItens.getString("modelo");
                ultimaLinha.cbModelo.setSelectedItem(modeloStr);

                int camisaId = rsItens.getInt("fk_id_camisa");
                for (int i = 0; i < ultimaLinha.cbCamisaFinal.getItemCount(); i++) {
                    if (ultimaLinha.cbCamisaFinal.getItemAt(i).getId() == camisaId) {
                        ultimaLinha.cbCamisaFinal.setSelectedIndex(i);
                        break;
                    }
                }

                ultimaLinha.txtQuantidade.setText(String.valueOf(rsItens.getInt("quantidade")));
            }

            JButton btnAdicionarCamisa = new JButton("Adicionar Mais uma Camisa");
            btnAdicionarCamisa.addActionListener(e -> adicionarNovaLinhaCamisa(painelItensLista, linhasAtivas));

            JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
            painelPrincipal.add(painelTop, BorderLayout.NORTH);
            painelPrincipal.add(scrollItens, BorderLayout.CENTER);
            painelPrincipal.add(btnAdicionarCamisa, BorderLayout.SOUTH);

            boolean salvo = false;
            while (!salvo) {
                int resultado = JOptionPane.showConfirmDialog(this, painelPrincipal, "Editar Pedido (ID: " + idPedido + ")", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (resultado == JOptionPane.OK_OPTION) {
                    if (cbCliente.getSelectedItem() == null || cbFuncionario.getSelectedItem() == null || txtData.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Por favor, preencha o Cliente, Vendedor Responsável e a Data do Pedido.");
                        continue;
                    }

                    if (linhasAtivas.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "O pedido precisa ter pelo menos uma camisa.");
                        continue;
                    }

                    boolean itensValidos = true;
                    for (LinhaItemPedido linha : linhasAtivas) {
                        if (linha.cbCamisaFinal.getSelectedItem() == null || linha.txtQuantidade.getText().trim().isEmpty()) {
                            itensValidos = false;
                            break;
                        }
                    }

                    if (!itensValidos) {
                        JOptionPane.showMessageDialog(this, "Por favor, selecione a camisa final e informe a quantidade em todas as linhas.");
                        continue;
                    }

                    ComboItem novoCliente = (ComboItem) cbCliente.getSelectedItem();
                    ComboItem novoFuncionario = (ComboItem) cbFuncionario.getSelectedItem();

                    try {
                        conexao.setAutoCommit(false);
                        Date novaDataPedido = Date.valueOf(txtData.getText().trim());
                        double novoValorTotal = 0.0;

                        for (LinhaItemPedido linha : linhasAtivas) {
                            ComboCamisa camisa = (ComboCamisa) linha.cbCamisaFinal.getSelectedItem();
                            int qtd = Integer.parseInt(linha.txtQuantidade.getText().trim());
                            if (qtd <= 0) {
                                throw new NumberFormatException("Quantidade menor ou igual a zero.");
                            }
                            novoValorTotal += (camisa.getPreco() * qtd);
                        }

                        String sqlUpdatePedido = "UPDATE Pedido SET valor_total = ?, data_compra = ?, fk_id_cliente = ?, fk_id_funcionario = ? WHERE id_pedido = ?";
                        try (PreparedStatement stmtUpdate = conexao.prepareStatement(sqlUpdatePedido)) {
                            stmtUpdate.setDouble(1, novoValorTotal);
                            stmtUpdate.setDate(2, novaDataPedido);
                            stmtUpdate.setInt(3, novoCliente.getId());
                            stmtUpdate.setInt(4, novoFuncionario.getId());
                            stmtUpdate.setInt(5, idPedido);
                            stmtUpdate.executeUpdate();
                        }

                        String sqlDeleteItens = "DELETE FROM Item_Pedido WHERE fk_id_pedido = ?";
                        try (PreparedStatement stmtDelete = conexao.prepareStatement(sqlDeleteItens)) {
                            stmtDelete.setInt(1, idPedido);
                            stmtDelete.executeUpdate();
                        }

                        String sqlInsertItens = "INSERT INTO Item_Pedido (quantidade, subtotal_item, fk_id_pedido, fk_id_camisa) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement stmtInsertItem = conexao.prepareStatement(sqlInsertItens)) {
                            for (LinhaItemPedido linha : linhasAtivas) {
                                ComboCamisa camisa = (ComboCamisa) linha.cbCamisaFinal.getSelectedItem();
                                int qtd = Integer.parseInt(linha.txtQuantidade.getText().trim());
                                double subtotal = camisa.getPreco() * qtd;

                                stmtInsertItem.setInt(1, qtd);
                                stmtInsertItem.setDouble(2, subtotal);
                                stmtInsertItem.setInt(3, idPedido);
                                stmtInsertItem.setInt(4, camisa.getId());
                                stmtInsertItem.addBatch();
                            }
                            stmtInsertItem.executeBatch();
                        }

                        conexao.commit();
                        JOptionPane.showMessageDialog(this, "Pedido atualizado com sucesso!");
                        atualizarListaPedidos();
                        salvo = true;

                    } catch (NumberFormatException ex) {
                        conexao.rollback();
                        JOptionPane.showMessageDialog(this, "Erro: As quantidades devem ser números inteiros maiores que zero.");
                    } catch (IllegalArgumentException ex) {
                        conexao.rollback();
                        JOptionPane.showMessageDialog(this, "Erro: Formato de data inválido. Use AAAA-MM-DD.");
                    } catch (SQLException ex) {
                        conexao.rollback();
                        JOptionPane.showMessageDialog(this, "Erro ao atualizar o pedido no banco: " + ex.getMessage());
                    }
                } else {
                    break;
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro: O ID deve ser um número inteiro.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro geral de conexão ao tentar editar: " + ex.getMessage());
        }
    }

    private void excluirPedido() {
        String idBusca = idDaLinhaSelecionada();
        if (idBusca == null) {
            idBusca = JOptionPane.showInputDialog(this, "Digite o ID do pedido que deseja excluir:");
        }
        if (idBusca == null || idBusca.trim().isEmpty()) return;

        int idPedido = Integer.parseInt(idBusca);

        String sqlBusca = "SELECT valor_total FROM Pedido WHERE id_pedido = ?";
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmtBusca = conexao.prepareStatement(sqlBusca)) {
            stmtBusca.setInt(1, idPedido);
            ResultSet rs = stmtBusca.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Erro: Pedido não encontrado.");
                return;
            }

            int confirmacao = JOptionPane.showConfirmDialog(this, 
                "Deseja confirmar a exclusão definitiva do pedido ID: " + idPedido + "?\nIsso também removerá todos os itens vinculados a ele.", 
                "Confirmação de Exclusão", 
                JOptionPane.YES_NO_OPTION);

            if (confirmacao == JOptionPane.YES_OPTION) {
                String sqlDelete = "DELETE FROM Pedido WHERE id_pedido = ?";
                try (PreparedStatement stmtDelete = conexao.prepareStatement(sqlDelete)) {
                    stmtDelete.setInt(1, idPedido);
                    stmtDelete.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Pedido excluído com sucesso!");
                    atualizarListaPedidos();
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro: O ID deve ser um numérico.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }
}