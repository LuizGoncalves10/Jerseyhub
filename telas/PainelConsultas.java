package telas;

import banco.ConexaoBanco;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Consultas avançadas (Etapa 03): a lista à esquerda traz as 9 consultas do projeto.
 * Ao escolher uma, a tela mostra o que ela responde, o SQL exato que vai ao banco e o resultado.
 */
public class PainelConsultas extends JPanel {

    /** Uma consulta do projeto: nome curto, pergunta que ela responde, conceitos de SQL usados e o comando. */
    private static class Consulta {
        final String titulo;
        final String pergunta;
        final String conceitos;
        final String sql;

        Consulta(String titulo, String pergunta, String conceitos, String sql) {
            this.titulo = titulo;
            this.pergunta = pergunta;
            this.conceitos = conceitos;
            this.sql = sql;
        }
    }

    // O SQL é o mesmo do arquivo consultas_etapa3.sql, só quebrado em linhas para ficar legível na tela.
    private static final Consulta[] CONSULTAS = {
        new Consulta("Clientes TOP",
            "Quais clientes gastaram, no total, mais do que o valor médio de um pedido?",
            "JOIN · GROUP BY · HAVING · subconsulta",
            "SELECT cl.nome AS Cliente, SUM(p.valor_total) AS Total_Gasto\n"
            + "FROM Cliente cl\n"
            + "JOIN Pedido p ON cl.id_cliente = p.fk_id_cliente\n"
            + "GROUP BY cl.id_cliente, cl.nome\n"
            + "HAVING SUM(p.valor_total) > (SELECT AVG(valor_total) FROM Pedido)"),
        new Consulta("Acima da média",
            "Quais camisas custam mais do que a média do catálogo?",
            "LEFT JOIN · COALESCE · subconsulta",
            "SELECT COALESCE(e.nome, 'Sem Equipe') AS Equipe, c.versao AS Versao, c.preco AS Preco\n"
            + "FROM Camisa c\n"
            + "LEFT JOIN Equipe e ON c.fk_id_equipe = e.id_equipe\n"
            + "WHERE c.preco > (SELECT AVG(preco) FROM Camisa)"),
        new Consulta("Relatório de pedidos",
            "Todos os pedidos, com o cliente, a data e o valor total.",
            "INNER JOIN",
            "SELECT p.id_pedido AS Pedido, cl.nome AS Cliente, p.data_compra AS Data, p.valor_total AS Total\n"
            + "FROM Pedido p\n"
            + "JOIN Cliente cl ON p.fk_id_cliente = cl.id_cliente"),
        new Consulta("Situação do estoque",
            "Quantas peças há de cada camisa, da maior quantidade para a menor?",
            "LEFT JOIN · COALESCE · ORDER BY",
            "SELECT COALESCE(e.nome, 'Sem Equipe') AS Equipe, c.versao AS Versao, c.tamanho AS Tamanho,\n"
            + "       c.quantidade_estoque AS Estoque\n"
            + "FROM Camisa c\n"
            + "LEFT JOIN Equipe e ON c.fk_id_equipe = e.id_equipe\n"
            + "ORDER BY c.quantidade_estoque DESC"),
        new Consulta("Clientes frequentes",
            "Quem fez mais de 3 pedidos?",
            "JOIN · COUNT · GROUP BY · HAVING",
            "SELECT cl.nome AS Cliente, COUNT(p.id_pedido) AS Total_Pedidos\n"
            + "FROM Cliente cl\n"
            + "JOIN Pedido p ON cl.id_cliente = p.fk_id_cliente\n"
            + "GROUP BY cl.id_cliente, cl.nome\n"
            + "HAVING COUNT(p.id_pedido) > 3\n"
            + "ORDER BY Total_Pedidos DESC"),
        new Consulta("Camisas sem venda",
            "Quais camisas nunca apareceram em nenhum pedido?",
            "Subconsulta com NOT IN",
            "SELECT c.modelo AS Modelo, c.versao AS Versao, c.preco AS Preco\n"
            + "FROM Camisa c\n"
            + "WHERE c.id_camisa NOT IN (SELECT fk_id_camisa FROM Item_Pedido)"),
        new Consulta("Ticket médio",
            "Qual o valor médio dos pedidos de cada cliente?",
            "JOIN · AVG · GROUP BY",
            "SELECT cl.nome AS Cliente, AVG(p.valor_total) AS Ticket_Medio\n"
            + "FROM Cliente cl\n"
            + "JOIN Pedido p ON cl.id_cliente = p.fk_id_cliente\n"
            + "GROUP BY cl.id_cliente, cl.nome\n"
            + "ORDER BY Ticket_Medio DESC"),
        new Consulta("Camisa mais cara por equipe",
            "Qual é a camisa mais cara de cada equipe?",
            "JOIN · subconsulta correlacionada · MAX",
            "SELECT e.nome AS Equipe, c.modelo AS Modelo, c.preco AS Preco\n"
            + "FROM Camisa c\n"
            + "JOIN Equipe e ON c.fk_id_equipe = e.id_equipe\n"
            + "WHERE c.preco = (SELECT MAX(preco) FROM Camisa c2 WHERE c2.fk_id_equipe = e.id_equipe)"),
        new Consulta("Clientes sem compras",
            "Quais clientes cadastrados nunca fizeram um pedido?",
            "Subconsulta com NOT IN",
            "SELECT cl.nome AS Cliente, cl.cpf AS CPF\n"
            + "FROM Cliente cl\n"
            + "WHERE cl.id_cliente NOT IN (SELECT fk_id_cliente FROM Pedido)"),
    };

    private final JList<Consulta> listaConsultas = new JList<>(CONSULTAS);
    private final JLabel rotuloTitulo = new JLabel();
    private final JLabel rotuloPergunta = new JLabel();
    private final JTextArea areaSql = new JTextArea();
    private final JTable tabelaResultados = UIUtils.criarTabela();
    private final JLabel rotuloTotal = new JLabel(" ");
    private final CardLayout trocaResultado = new CardLayout();
    private final JPanel areaResultado = new JPanel(trocaResultado);
    private final JLabel mensagemVazia = new JLabel();

    public PainelConsultas() {
        setLayout(new BorderLayout());
        setBackground(Tema.FUNDO);

        add(UIUtils.criarCabecalhoSecao("Consultas",
            "Nove consultas do projeto, com o SQL que é enviado ao banco.", null), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(Tema.ESPACO_GRANDE, 0));
        corpo.setOpaque(false);
        corpo.setBorder(BorderFactory.createEmptyBorder(0, Tema.MARGEM_TELA, Tema.MARGEM_TELA, Tema.MARGEM_TELA));
        corpo.add(criarListaConsultas(), BorderLayout.WEST);
        corpo.add(criarAreaDetalhe(), BorderLayout.CENTER);
        add(corpo, BorderLayout.CENTER);

        listaConsultas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaConsultas.getSelectedValue() != null) {
                executarConsulta(listaConsultas.getSelectedValue());
            }
        });
        SwingUtilities.invokeLater(() -> listaConsultas.setSelectedIndex(0));
    }

    private JComponent criarListaConsultas() {
        listaConsultas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaConsultas.setBackground(Tema.SUPERFICIE);
        listaConsultas.setFixedCellHeight(64);
        listaConsultas.setCellRenderer(new RenderizadorConsulta());

        JScrollPane rolagem = UIUtils.criarRolagem(listaConsultas);
        rolagem.setPreferredSize(new Dimension(300, 0));
        return rolagem;
    }

    private JComponent criarAreaDetalhe() {
        JPanel detalhe = new JPanel(new BorderLayout(0, Tema.ESPACO_MEDIO));
        detalhe.setOpaque(false);

        rotuloTitulo.setFont(Tema.fonteTitulo(Tema.TAMANHO_TITULO_CARTAO + 4));
        rotuloTitulo.setForeground(Tema.TEXTO);
        rotuloPergunta.setFont(Tema.fonte(15f, Font.PLAIN));
        rotuloPergunta.setForeground(Tema.TEXTO_SECUNDARIO);

        JPanel textos = new JPanel(new BorderLayout(0, 2));
        textos.setOpaque(false);
        textos.add(rotuloTitulo, BorderLayout.NORTH);
        textos.add(rotuloPergunta, BorderLayout.SOUTH);

        // O bloco de SQL usa a faixa preta da identidade, como um "cupom" impresso do comando
        areaSql.setEditable(false);
        areaSql.setLineWrap(true);
        areaSql.setWrapStyleWord(true);
        areaSql.setFont(Tema.fonteCodigo(13.5f));
        areaSql.setBackground(Tema.LATERAL_FUNDO);
        areaSql.setForeground(Tema.LATERAL_TEXTO);
        areaSql.setCaretColor(Tema.DESTAQUE);
        areaSql.setSelectionColor(Tema.LATERAL_ITEM_ATIVO);
        areaSql.setSelectedTextColor(Tema.DESTAQUE);
        areaSql.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        areaSql.putClientProperty("FlatLaf.style", "background: " + Tema.hex(Tema.LATERAL_FUNDO) + ";");

        areaSql.setOpaque(false);
        JPanel blocoSql = new UIUtils.PainelArredondado(new BorderLayout(), Tema.LATERAL_FUNDO, Tema.ARREDONDAMENTO + 4);
        blocoSql.add(areaSql, BorderLayout.CENTER);

        JPanel topo = new JPanel(new BorderLayout(0, Tema.ESPACO_MEDIO));
        topo.setOpaque(false);
        topo.add(textos, BorderLayout.NORTH);
        topo.add(blocoSql, BorderLayout.CENTER);

        mensagemVazia.setHorizontalAlignment(SwingConstants.CENTER);
        mensagemVazia.setFont(Tema.fonte(15f, Font.PLAIN));
        mensagemVazia.setForeground(Tema.TEXTO_SECUNDARIO);
        JPanel painelVazio = UIUtils.criarPainelSuperficie();
        painelVazio.setLayout(new BorderLayout());
        painelVazio.add(mensagemVazia, BorderLayout.CENTER);

        areaResultado.setOpaque(false);
        areaResultado.add(UIUtils.criarRolagem(tabelaResultados), "tabela");
        areaResultado.add(painelVazio, "vazio");

        rotuloTotal.setFont(Tema.fonte(Tema.TAMANHO_ROTULO, Font.PLAIN));
        rotuloTotal.setForeground(Tema.TEXTO_SECUNDARIO);

        detalhe.add(topo, BorderLayout.NORTH);
        detalhe.add(areaResultado, BorderLayout.CENTER);
        detalhe.add(rotuloTotal, BorderLayout.SOUTH);
        return detalhe;
    }

    private void executarConsulta(Consulta consulta) {
        rotuloTitulo.setText(consulta.titulo);
        rotuloPergunta.setText(consulta.pergunta);
        areaSql.setText(consulta.sql);

        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) {
                mostrarVazio("Não foi possível conectar ao banco de dados.");
                return;
            }
            try (PreparedStatement comando = conexao.prepareStatement(consulta.sql)) {
                int total = UIUtils.preencherTabela(tabelaResultados, comando);
                if (total == 0) {
                    mostrarVazio("A consulta rodou, mas nenhum registro atende a essa condição.");
                } else {
                    trocaResultado.show(areaResultado, "tabela");
                    rotuloTotal.setText(total + (total == 1 ? " registro encontrado" : " registros encontrados"));
                }
            }
        } catch (SQLException ex) {
            mostrarVazio("Erro ao executar a consulta: " + ex.getMessage());
        }
    }

    private void mostrarVazio(String mensagem) {
        mensagemVazia.setText(mensagem);
        trocaResultado.show(areaResultado, "vazio");
        rotuloTotal.setText(" ");
    }

    /** Cada item da lista mostra o nome da consulta e os conceitos de SQL que ela usa. */
    private static class RenderizadorConsulta extends JPanel implements ListCellRenderer<Consulta> {
        private final JLabel titulo = new JLabel();
        private final JLabel conceitos = new JLabel();

        RenderizadorConsulta() {
            setLayout(new BorderLayout(0, 2));
            setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 14));
            titulo.setFont(Tema.fonte(15f, Font.BOLD));
            titulo.setForeground(Tema.TEXTO);
            conceitos.setFont(Tema.fonte(Tema.TAMANHO_ROTULO, Font.PLAIN));
            conceitos.setForeground(Tema.TEXTO_SECUNDARIO);
            add(titulo, BorderLayout.NORTH);
            add(conceitos, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Consulta> lista, Consulta consulta, int indice,
                                                      boolean selecionado, boolean comFoco) {
            titulo.setText(consulta.titulo);
            conceitos.setText(consulta.conceitos);
            setBackground(selecionado ? Tema.SELECAO_LINHA : Tema.SUPERFICIE);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Tema.SUPERFICIE_ALTERNADA);
            g.fillRect(0, getHeight() - 1, getWidth(), 1);
        }
    }
}
