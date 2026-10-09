import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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

    //cores da interface (tema escuro)
    private static final Color COR_PRIMARIA = Color.decode("#14532D");
    private static final Color COR_SUCESSO = Color.decode("#2EA05F");
    private static final Color COR_EDICAO = Color.decode("#2F6FD0");
    private static final Color COR_PERIGO = Color.decode("#C93C37");
    private static final Color COR_NEUTRA = Color.decode("#484F58");
    private static final Color COR_FUNDO = Color.decode("#16191D");
    private static final Color COR_CARTAO = Color.decode("#1F2328");
    private static final Color COR_BORDA = Color.decode("#2D333B");
    private static final Color COR_TEXTO = Color.decode("#E6EDF3");
    private static final Color COR_TEXTO_SUAVE = Color.decode("#8B949E");
    private static final Color COR_LINHA_ALTERNADA = Color.decode("#23282E");
    private static final Color COR_SELECAO = Color.decode("#1C4A30");
    private static final Color COR_DESTAQUE_FUNDO = Color.decode("#183326");

    private static final String SQL_LISTA_CLIENTES = "SELECT id_cliente AS ID, nome AS Nome, cpf AS CPF, rua AS Rua, numero AS Numero, bairro AS Bairro, cep AS CEP FROM Cliente ORDER BY nome";
    private static final String SQL_LISTA_CAMISAS = "SELECT c.id_camisa AS ID, c.modelo AS Modelo, COALESCE(e.nome, 'Sem Equipe') AS Equipe, c.versao AS Versao, c.tamanho AS Tamanho, c.preco AS Preco, c.ano AS Ano, c.quantidade_estoque AS Estoque FROM Camisa c LEFT JOIN Equipe e ON c.fk_id_equipe = e.id_equipe ORDER BY c.id_camisa";

    private JComboBox<String> comboConsultas;
    private JTable tabelaResultados;
    private JTable tabelaClientes;
    private JTable tabelaCamisas;
    private JLabel lblDescricaoConsulta;
    private JLabel lblTotalRegistros;

    public Dashboard() {
        setTitle("Jerseyhub - Dashboard");
        setSize(1000, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane painelAbas = new JTabbedPane();

        JPanel painelClientes = criarPainelBase();
        JButton btnInserirCliente = criarBotao("+ Inserir Cliente", COR_SUCESSO);
        JButton btnEditarCliente = criarBotao("Editar Cliente", COR_EDICAO);
        JButton btnExcluirCliente = criarBotao("Excluir Cliente", COR_PERIGO);
        JButton btnAtualizarClientes = criarBotao("Atualizar", COR_NEUTRA);

        btnInserirCliente.addActionListener(e -> inserirCliente());
        btnEditarCliente.addActionListener(e -> editarClientePorCpf());
        btnExcluirCliente.addActionListener(e -> excluirClientePorCpf());
        btnAtualizarClientes.addActionListener(e -> atualizarListaClientes());

        tabelaClientes = criarTabela();
        painelClientes.add(criarBarraTopo("Gerenciamento de Clientes",
            btnInserirCliente, btnEditarCliente, btnExcluirCliente, btnAtualizarClientes), BorderLayout.NORTH);
        painelClientes.add(criarRolagem(tabelaClientes), BorderLayout.CENTER);

        JPanel painelCamisas = criarPainelBase();
        JButton btnInserirCamisa = criarBotao("+ Inserir Camisa", COR_SUCESSO);
        JButton btnEditarCamisa = criarBotao("Editar Camisa", COR_EDICAO);
        JButton btnExcluirCamisa = criarBotao("Excluir Camisa", COR_PERIGO);
        JButton btnAtualizarCamisas = criarBotao("Atualizar", COR_NEUTRA);

        btnInserirCamisa.addActionListener(e -> inserirCamisa());
        btnEditarCamisa.addActionListener(e -> editarCamisa());
        btnExcluirCamisa.addActionListener(e -> excluirCamisa());
        btnAtualizarCamisas.addActionListener(e -> atualizarListaCamisas());

        tabelaCamisas = criarTabela();
        painelCamisas.add(criarBarraTopo("Gerenciamento de Camisas",
            btnInserirCamisa, btnEditarCamisa, btnExcluirCamisa, btnAtualizarCamisas), BorderLayout.NORTH);
        painelCamisas.add(criarRolagem(tabelaCamisas), BorderLayout.CENTER);

        JPanel painelConsultas = criarPainelBase();
        String[] opcoes = {
            "Selecione",
            "Clientes TOP",
            "Acima da Media",
            "Relatorio",
            "Estoque",
            "Clientes Frequentes",
            "Camisas Sem Venda",
            "Ticket Medio",
            "Camisa Mais Cara",
            "Clientes Sem Compras"
        };
        comboConsultas = new JComboBox<>(opcoes);
        comboConsultas.setPreferredSize(new Dimension(220, 32));
        JButton btnRodarConsulta = criarBotao("Executar", COR_SUCESSO);

        btnRodarConsulta.addActionListener(e -> executarConsulta());
        comboConsultas.addActionListener(e -> atualizarDescricaoConsulta());

        JPanel menuConsultas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        menuConsultas.setOpaque(false);
        menuConsultas.add(new JLabel("Visualizacao:"));
        menuConsultas.add(comboConsultas);
        menuConsultas.add(btnRodarConsulta);

        JPanel linhaConsultas = new JPanel(new BorderLayout());
        linhaConsultas.setOpaque(false);
        linhaConsultas.add(criarTituloSecao("Consultas Avançadas"), BorderLayout.WEST);
        linhaConsultas.add(menuConsultas, BorderLayout.EAST);

        lblDescricaoConsulta = new JLabel();
        lblDescricaoConsulta.setOpaque(true);
        lblDescricaoConsulta.setBackground(COR_DESTAQUE_FUNDO);
        lblDescricaoConsulta.setForeground(COR_TEXTO);
        lblDescricaoConsulta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, COR_SUCESSO),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        atualizarDescricaoConsulta();

        JPanel topoConsultas = new JPanel(new BorderLayout(0, 8));
        topoConsultas.setOpaque(false);
        topoConsultas.add(linhaConsultas, BorderLayout.NORTH);
        topoConsultas.add(lblDescricaoConsulta, BorderLayout.SOUTH);

        tabelaResultados = criarTabela();
        lblTotalRegistros = new JLabel(" ");
        lblTotalRegistros.setForeground(COR_TEXTO_SUAVE);

        painelConsultas.add(topoConsultas, BorderLayout.NORTH);
        painelConsultas.add(criarRolagem(tabelaResultados), BorderLayout.CENTER);
        painelConsultas.add(lblTotalRegistros, BorderLayout.SOUTH);

        JPanel painelEstatistica = criarPainelBase();
        JButton btnAtualizarEstatistica = criarBotao("Atualizar Estatísticas", COR_SUCESSO);
        JTextArea areaEstatisticas = new JTextArea();
        areaEstatisticas.setEditable(false);
        areaEstatisticas.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaEstatisticas.setMargin(new Insets(16, 20, 16, 20));
        areaEstatisticas.setBackground(COR_CARTAO);
        areaEstatisticas.setForeground(COR_TEXTO);

        btnAtualizarEstatistica.addActionListener(e -> atualizarEstatisticas(areaEstatisticas));

        painelEstatistica.add(criarBarraTopo("Estatísticas do Negócio", btnAtualizarEstatistica), BorderLayout.NORTH);
        painelEstatistica.add(criarRolagem(areaEstatisticas), BorderLayout.CENTER);

        painelAbas.addTab("Clientes", painelClientes);
        painelAbas.addTab("Camisas", painelCamisas);
        painelAbas.addTab("Consultas", painelConsultas);
        painelAbas.addTab("Estatisticas", painelEstatistica);

        JPanel painelRaiz = new JPanel(new BorderLayout());
        painelRaiz.add(criarCabecalho(), BorderLayout.NORTH);
        painelRaiz.add(painelAbas, BorderLayout.CENTER);
        add(painelRaiz);

        //carrega os dados assim que a janela abre
        SwingUtilities.invokeLater(() -> {
            atualizarListaClientes();
            atualizarListaCamisas();
            atualizarEstatisticas(areaEstatisticas);
        });
    }

    //componentes visuais
    private static void configurarAparencia() {
        //tema escuro do FlatLaf com verde como cor de destaque
        FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#2EA05F"));
        FlatDarkLaf.setup();

        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("TabbedPane.tabHeight", 40);
        UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("TabbedPane.background", COR_FUNDO);
        UIManager.put("Panel.background", COR_FUNDO);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(COR_PRIMARIA);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JLabel titulo = new JLabel("Jerseyhub");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Gestão de camisas, clientes e estoque");
        subtitulo.setForeground(new Color(190, 230, 205));

        cabecalho.add(titulo, BorderLayout.NORTH);
        cabecalho.add(subtitulo, BorderLayout.SOUTH);
        return cabecalho;
    }

    private JPanel criarPainelBase() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(COR_FUNDO);
        painel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return painel;
    }

    private JLabel criarTituloSecao(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(COR_TEXTO);
        return titulo;
    }

    private JPanel criarBarraTopo(String titulo, JButton... botoes) {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(criarTituloSecao(titulo), BorderLayout.WEST);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        painelBotoes.setOpaque(false);
        for (JButton botao : botoes) painelBotoes.add(botao);
        barra.add(painelBotoes, BorderLayout.EAST);
        return barra;
    }

    private JButton criarBotao(String texto, Color cor) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 13));
        botao.setMargin(new Insets(7, 16, 7, 16));
        botao.setFocusable(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        //o FlatLaf arredonda os cantos; aqui so definimos as cores (normal, mouse em cima e clicado)
        botao.putClientProperty("FlatLaf.style",
            "background: " + hex(cor) + "; foreground: #FFFFFF; borderWidth: 0; focusWidth: 0;"
            + " hoverBackground: " + hex(cor.brighter()) + "; pressedBackground: " + hex(cor.darker()));
        return botao;
    }

    private static String hex(Color cor) {
        return String.format("#%02X%02X%02X", cor.getRed(), cor.getGreen(), cor.getBlue());
    }

    private JScrollPane criarRolagem(JComponent componente) {
        JScrollPane rolagem = new JScrollPane(componente);
        rolagem.setBorder(BorderFactory.createLineBorder(COR_BORDA));
        rolagem.getViewport().setBackground(COR_CARTAO);
        return rolagem;
    }

    private JTable criarTabela() {
        JTable tabela = new JTable();
        tabela.setRowHeight(32);
        tabela.setShowVerticalLines(false);
        tabela.setShowHorizontalLines(true);
        tabela.setGridColor(COR_BORDA);
        tabela.setFillsViewportHeight(true);
        tabela.setBackground(COR_CARTAO);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 36));

        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean selecionada, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, selecionada, foco, linha, coluna);
                if (selecionada) setBackground(COR_SELECAO);
                else setBackground(linha % 2 == 0 ? COR_CARTAO : COR_LINHA_ALTERNADA);
                setForeground(COR_TEXTO);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return this;
            }
        });

        tabela.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean selecionada, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, selecionada, foco, linha, coluna);
                setBackground(COR_CARTAO);
                setForeground(COR_TEXTO_SUAVE);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, COR_BORDA),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                return this;
            }
        });
        return tabela;
    }

    private DefaultTableModel criarModeloSomenteLeitura(Vector<Vector<Object>> dados, Vector<String> colunas) {
        return new DefaultTableModel(dados, colunas) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
    }

    //listas das abas clientes e camisas
    private void atualizarListaClientes() {
        carregarLista(tabelaClientes, SQL_LISTA_CLIENTES);
    }

    private void atualizarListaCamisas() {
        carregarLista(tabelaCamisas, SQL_LISTA_CAMISAS);
    }

    private void carregarLista(JTable tabela, String sql) {
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) return;
            try (PreparedStatement stmt = conexao.prepareStatement(sql);
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

                tabela.setModel(criarModeloSomenteLeitura(dados, colunas));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar lista: " + ex.getMessage());
        }
    }

    private void atualizarDescricaoConsulta() {
        String selecao = (String) comboConsultas.getSelectedItem();
        String descricao;

        if ("Clientes TOP".equals(selecao)) {
            descricao = "JOIN + GROUP BY + HAVING com subconsulta: clientes cujo gasto total supera o valor médio de um pedido.";
        } else if ("Acima da Media".equals(selecao)) {
            descricao = "LEFT JOIN + COALESCE + subconsulta: camisas com preço acima da média do catálogo.";
        } else if ("Relatorio".equals(selecao)) {
            descricao = "INNER JOIN: todos os pedidos com o nome do cliente, a data e o valor total.";
        } else if ("Estoque".equals(selecao)) {
            descricao = "LEFT JOIN + COALESCE + ORDER BY: estoque de cada camisa, do maior para o menor.";
        } else if ("Clientes Frequentes".equals(selecao)) {
            descricao = "JOIN + COUNT + GROUP BY + HAVING: quantidade de pedidos feitos por cliente.";
        } else if ("Camisas Sem Venda".equals(selecao)) {
            descricao = "Subconsulta com NOT IN: camisas que nunca apareceram em um pedido.";
        } else if ("Ticket Medio".equals(selecao)) {
            descricao = "JOIN + AVG + GROUP BY: valor médio por pedido de cada cliente.";
        } else if ("Camisa Mais Cara".equals(selecao)) {
            descricao = "Subconsulta correlacionada: a camisa mais cara de cada equipe.";
        } else if ("Clientes Sem Compras".equals(selecao)) {
            descricao = "Subconsulta com NOT IN: clientes cadastrados que nunca fizeram um pedido.";
        } else {
            descricao = "Selecione uma consulta e clique em Executar.";
        }

        lblDescricaoConsulta.setText(descricao);
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
                atualizarListaCamisas();
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
                    atualizarListaCamisas();
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

        if ("Clientes TOP".equals(selecao)) {
            sql = "SELECT cl.nome AS Cliente, SUM(p.valor_total) AS Total_Gasto FROM Cliente cl JOIN Pedido p ON cl.id_cliente = p.fk_id_cliente GROUP BY cl.id_cliente, cl.nome HAVING SUM(p.valor_total) > (SELECT AVG(valor_total) FROM Pedido)";
        } else if ("Acima da Media".equals(selecao)) {
            sql = "SELECT COALESCE(e.nome, 'Sem Equipe') AS Equipe, c.versao AS Versao, c.preco AS Preco FROM Camisa c LEFT JOIN Equipe e ON c.fk_id_equipe = e.id_equipe WHERE c.preco > (SELECT AVG(preco) FROM Camisa)";
        } else if ("Relatorio".equals(selecao)) {
            sql = "SELECT p.id_pedido AS Pedido, cl.nome AS Cliente, p.data_compra AS Data, p.valor_total AS Total FROM Pedido p JOIN Cliente cl ON p.fk_id_cliente = cl.id_cliente";
        } else if ("Estoque".equals(selecao)) {
            sql = "SELECT COALESCE(e.nome, 'Sem Equipe') AS Equipe, c.versao AS Versao, c.tamanho as Tamanho, c.quantidade_estoque AS Estoque FROM Camisa c LEFT JOIN Equipe e ON c.fk_id_equipe = e.id_equipe ORDER BY c.quantidade_estoque DESC";
        } else if ("Clientes Frequentes".equals(selecao)) {
            sql = "SELECT cl.nome AS Cliente, COUNT(p.id_pedido) AS Total_Pedidos FROM Cliente cl JOIN Pedido p ON cl.id_cliente = p.fk_id_cliente GROUP BY cl.id_cliente, cl.nome HAVING COUNT(p.id_pedido) > 3 ORDER BY Total_Pedidos DESC";
        } else if ("Camisas Sem Venda".equals(selecao)) {
            sql = "SELECT c.modelo AS Modelo, c.versao AS Versao, c.preco AS Preco FROM Camisa c WHERE c.id_camisa NOT IN (SELECT fk_id_camisa FROM Item_Pedido)";
        } else if ("Ticket Medio".equals(selecao)) {
            sql = "SELECT cl.nome AS Cliente, AVG(p.valor_total) AS Ticket_Medio FROM Cliente cl JOIN Pedido p ON cl.id_cliente = p.fk_id_cliente GROUP BY cl.id_cliente, cl.nome ORDER BY Ticket_Medio DESC";
        } else if ("Camisa Mais Cara".equals(selecao)) {
            sql = "SELECT e.nome AS Equipe, c.modelo AS Modelo, c.preco AS Preco FROM Camisa c JOIN Equipe e ON c.fk_id_equipe = e.id_equipe WHERE c.preco = (SELECT MAX(preco) FROM Camisa c2 WHERE c2.fk_id_equipe = e.id_equipe)";
        } else if ("Clientes Sem Compras".equals(selecao)) {
            sql = "SELECT cl.nome AS Cliente, cl.cpf AS CPF FROM Cliente cl WHERE cl.id_cliente NOT IN (SELECT fk_id_cliente FROM Pedido)";
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

            tabelaResultados.setModel(criarModeloSomenteLeitura(dados, colunas));
            lblTotalRegistros.setText(dados.size() + " registro(s) encontrado(s)");

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
            configurarAparencia();
            new Dashboard().setVisible(true);
        });
    }
}