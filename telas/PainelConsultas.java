package telas;

import banco.ConexaoBanco;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class PainelConsultas extends JPanel {
    private JComboBox<String> comboConsultas;
    private JTable tabelaResultados;
    private JLabel lblDescricaoConsulta;
    private JLabel lblTotalRegistros;
    private JTextField campoFiltro;
    private TableRowSorter<TableModel> ordenador;

    public PainelConsultas() {
        UIUtils.configurarPainelBase(this);

        String[] opcoes = {
            "Selecione", "Clientes TOP", "Acima da Media", "Relatorio",
            "Estoque", "Clientes Frequentes", "Camisas Sem Venda",
            "Ticket Medio", "Camisa Mais Cara", "Clientes Sem Compras",
            "Funcionarios Sem Vendas", "Historico Completo", 
            "Supervisores Ativos", "Maior Venda",
            "View Faturamento", "View Vendedores"
        };
        comboConsultas = new JComboBox<>(opcoes);
        comboConsultas.setPreferredSize(new Dimension(220, 32));
        campoFiltro = UIUtils.criarCampoBusca("Filtrar resultado...");
        campoFiltro.setPreferredSize(new Dimension(200, 32));
        UIUtils.executarAoDigitar(campoFiltro, this::filtrarResultados);
        JButton btnRodarConsulta = UIUtils.criarBotao("Executar", UIUtils.COR_SUCESSO);

        btnRodarConsulta.addActionListener(e -> executarConsulta());
        comboConsultas.addActionListener(e -> atualizarDescricaoConsulta());

        JPanel menuConsultas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        menuConsultas.setOpaque(false);
        menuConsultas.add(new JLabel("Visualizacao:"));
        menuConsultas.add(comboConsultas);
        menuConsultas.add(campoFiltro);
        menuConsultas.add(btnRodarConsulta);

        JPanel linhaConsultas = new JPanel(new BorderLayout());
        linhaConsultas.setOpaque(false);
        linhaConsultas.add(UIUtils.criarTituloSecao("Consultas Avançadas"), BorderLayout.WEST);
        linhaConsultas.add(menuConsultas, BorderLayout.EAST);

        lblDescricaoConsulta = new JLabel();
        lblDescricaoConsulta.setOpaque(true);
        lblDescricaoConsulta.setBackground(UIUtils.COR_DESTAQUE_FUNDO);
        lblDescricaoConsulta.setForeground(UIUtils.COR_TEXTO);
        lblDescricaoConsulta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, UIUtils.COR_SUCESSO),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        atualizarDescricaoConsulta();

        JPanel topoConsultas = new JPanel(new BorderLayout(0, 8));
        topoConsultas.setOpaque(false);
        topoConsultas.add(linhaConsultas, BorderLayout.NORTH);
        topoConsultas.add(lblDescricaoConsulta, BorderLayout.SOUTH);

        tabelaResultados = UIUtils.criarTabela();
        lblTotalRegistros = new JLabel(" ");
        lblTotalRegistros.setForeground(UIUtils.COR_TEXTO_SUAVE);

        add(topoConsultas, BorderLayout.NORTH);
        add(UIUtils.criarRolagem(tabelaResultados), BorderLayout.CENTER);
        add(lblTotalRegistros, BorderLayout.SOUTH);
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
        } else if ("Funcionarios Sem Vendas".equals(selecao)) {
            descricao = "Anti Join (LEFT JOIN com IS NULL): funcionários cadastrados que nunca realizaram uma venda.";
        } else if ("Historico Completo".equals(selecao)) {
            descricao = "Full Outer Join (via UNION): lista simultânea de todos os funcionários e todos os pedidos.";
        } else if ("Supervisores Ativos".equals(selecao)) {
            descricao = "Subconsulta (IN): lista os funcionários que ocupam posições de supervisão.";
        } else if ("Maior Venda".equals(selecao)) {
            descricao = "Subconsulta (MAX): exibe o vendedor responsável pelo pedido de maior valor transacionado.";
        } else if ("View Faturamento".equals(selecao)) {
            descricao = "View (vw_faturamento_por_equipe) com 3 JOINs: camisas vendidas e faturamento total de cada equipe.";
        } else if ("View Vendedores".equals(selecao)) {
            descricao = "View (vw_desempenho_vendedores) com 3 JOINs: pedidos, clientes atendidos e valor total vendido por vendedor.";
        } else {
            descricao = "Selecione uma consulta e clique em Executar.";
        }

        lblDescricaoConsulta.setText(descricao);
    }

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
        } else if ("Funcionarios Sem Vendas".equals(selecao)) {
            sql = "SELECT f.nome AS Funcionario, f.cargo AS Cargo FROM Funcionario f LEFT JOIN Pedido p ON f.id_funcionario = p.fk_id_funcionario WHERE p.id_pedido IS NULL";
        } else if ("Historico Completo".equals(selecao)) {
            sql = "SELECT f.nome AS Funcionario, p.id_pedido AS Pedido, p.valor_total AS Valor FROM Funcionario f LEFT JOIN Pedido p ON f.id_funcionario = p.fk_id_funcionario UNION SELECT f.nome AS Funcionario, p.id_pedido AS Pedido, p.valor_total AS Valor FROM Funcionario f RIGHT JOIN Pedido p ON f.id_funcionario = p.fk_id_funcionario";
        } else if ("Supervisores Ativos".equals(selecao)) {
            sql = "SELECT f.nome AS Funcionario, f.cargo AS Cargo FROM Funcionario f WHERE f.id_funcionario IN (SELECT fk_id_supervisor FROM Supervisao)";
        } else if ("Maior Venda".equals(selecao)) {
            sql = "SELECT f.nome AS Vendedor, p.id_pedido AS Pedido, p.valor_total AS Total_Venda FROM Pedido p JOIN Funcionario f ON p.fk_id_funcionario = f.id_funcionario WHERE p.valor_total = (SELECT MAX(valor_total) FROM Pedido)";
        } else if ("View Faturamento".equals(selecao)) {
            sql = "SELECT * FROM vw_faturamento_por_equipe";
        } else if ("View Vendedores".equals(selecao)) {
            sql = "SELECT * FROM vw_desempenho_vendedores";
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

            tabelaResultados.setModel(UIUtils.criarModeloSomenteLeitura(dados, colunas));
            campoFiltro.setText("");
            ordenador = new TableRowSorter<>(tabelaResultados.getModel());
            tabelaResultados.setRowSorter(ordenador);
            filtrarResultados();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao executar consulta: " + ex.getMessage());
        }
    }

    private void filtrarResultados() {
        if (ordenador == null) return;
        String termo = campoFiltro.getText().trim();
        ordenador.setRowFilter(termo.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(termo)));
        lblTotalRegistros.setText(tabelaResultados.getRowCount() + " registro(s) encontrado(s)");
    }
}