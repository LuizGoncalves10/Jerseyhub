package telas;

import banco.ConexaoBanco;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Vector;

public class PainelLoja extends JPanel {

    private final CardLayout layoutCards = new CardLayout();
    private final JPanel painelCards = new JPanel(layoutCards);
    private JTable tabelaCamisas;
    private JLabel lblTituloTabela;

    public PainelLoja() {
        UIUtils.configurarPainelBase(this);
        setLayout(new BorderLayout());

        painelCards.setOpaque(false);
        painelCards.add(criarMenuInicial(), "MENU");
        painelCards.add(criarTelaSelecoes(), "SELECOES");
        painelCards.add(criarTelaCatalogo(), "CATALOGO");
        painelCards.add(new PainelLojaClubes(layoutCards, painelCards), "CLUBES");

        add(UIUtils.criarBarraTopo("Loja Virtual - Catálogo Interativo"), BorderLayout.NORTH);
        add(painelCards, BorderLayout.CENTER);
    }

    private JPanel criarMenuInicial() {
        JPanel painelConteudo = new JPanel(new BorderLayout(0, 40));
        painelConteudo.setOpaque(false);
        painelConteudo.setBorder(BorderFactory.createEmptyBorder(60, 60, 60, 60));

        JLabel lblTitulo = new JLabel("Bem-vindo ao Catálogo Jerseyhub");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitulo.setForeground(UIUtils.COR_TEXTO);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblSubtitulo = new JLabel("Explore nossa coleção e encontre a camisa perfeita para você");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblSubtitulo.setForeground(UIUtils.COR_TEXTO_SUAVE);
        lblSubtitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel painelCabecalho = new JPanel(new GridLayout(2, 1, 0, 10));
        painelCabecalho.setOpaque(false);
        painelCabecalho.add(lblTitulo);
        painelCabecalho.add(lblSubtitulo);

        JPanel painelOpcoes = new JPanel(new GridLayout(1, 2, 40, 0));
        painelOpcoes.setOpaque(false);

        JPanel cartaoClubes = criarCartaoCategoria("Clubes", "Camisas dos maiores times do mundo", "");
        JPanel cartaoSelecoes = criarCartaoCategoria("Seleções", "Camisas das seleções nacionais", "");

        cartaoClubes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                layoutCards.show(painelCards, "CLUBES");
            }
        });

        cartaoSelecoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                layoutCards.show(painelCards, "SELECOES");
            }
        });

        painelOpcoes.add(cartaoClubes);
        painelOpcoes.add(cartaoSelecoes);

        painelConteudo.add(painelCabecalho, BorderLayout.NORTH);
        painelConteudo.add(painelOpcoes, BorderLayout.CENTER);

        return painelConteudo;
    }

    private JPanel criarTelaSelecoes() {
        JPanel painel = new JPanel(new BorderLayout(0, 30));
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        
        JButton btnVoltar = UIUtils.criarBotao("Voltar ao Menu", Color.decode("#374151"));
        btnVoltar.addActionListener(e -> layoutCards.show(painelCards, "MENU"));
        
        JLabel lblTitulo = new JLabel("Escolha a Seleção");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(UIUtils.COR_TEXTO);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        topo.add(btnVoltar, BorderLayout.WEST);
        topo.add(lblTitulo, BorderLayout.CENTER);
        topo.add(Box.createRigidArea(new Dimension(150, 0)), BorderLayout.EAST);

        JPanel gridSelecoes = new JPanel(new GridLayout(2, 5, 20, 20));
        gridSelecoes.setOpaque(false);

        gridSelecoes.add(criarBotaoSelecao("Brasil", "BRA", Color.decode("#22C55E"), Color.WHITE));
        gridSelecoes.add(criarBotaoSelecao("Argentina", "ARG", Color.decode("#38BDF8"), Color.WHITE));
        gridSelecoes.add(criarBotaoSelecao("Portugal", "POR", Color.decode("#EF4444"), Color.WHITE));
        gridSelecoes.add(criarBotaoSelecao("Alemanha", "GER", Color.decode("#374151"), Color.WHITE));
        gridSelecoes.add(criarBotaoSelecao("Italia", "ITA", Color.decode("#3B82F6"), Color.WHITE));
        gridSelecoes.add(criarBotaoSelecao("Holanda", "NED", Color.decode("#F97316"), Color.WHITE));
        gridSelecoes.add(criarBotaoSelecao("Inglaterra", "ENG", Color.decode("#F3F4F6"), Color.decode("#1F2937")));
        gridSelecoes.add(criarBotaoSelecao("Franca", "FRA", Color.decode("#1D4ED8"), Color.WHITE));
        gridSelecoes.add(criarBotaoSelecao("Espanha", "ESP", Color.decode("#DC2626"), Color.decode("#FBBF24")));
        gridSelecoes.add(criarBotaoSelecao("Outros", "OUT", Color.decode("#64748B"), Color.WHITE));

        JPanel wrapperGrid = new JPanel(new BorderLayout());
        wrapperGrid.setOpaque(false);
        wrapperGrid.add(gridSelecoes, BorderLayout.NORTH);

        painel.add(topo, BorderLayout.NORTH);
        painel.add(wrapperGrid, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarTelaCatalogo() {
        JPanel painel = new JPanel(new BorderLayout(0, 20));
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);

        JButton btnVoltar = UIUtils.criarBotao("Voltar às Seleções", Color.decode("#374151"));
        btnVoltar.addActionListener(e -> layoutCards.show(painelCards, "SELECOES"));

        lblTituloTabela = new JLabel("Catálogo");
        lblTituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTituloTabela.setForeground(UIUtils.COR_TEXTO);
        lblTituloTabela.setHorizontalAlignment(SwingConstants.CENTER);

        topo.add(btnVoltar, BorderLayout.WEST);
        topo.add(lblTituloTabela, BorderLayout.CENTER);
        topo.add(Box.createRigidArea(new Dimension(150, 0)), BorderLayout.EAST);

        tabelaCamisas = UIUtils.criarTabela();
        JScrollPane scroll = UIUtils.criarRolagem(tabelaCamisas);

        painel.add(topo, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarCartaoCategoria(String titulo, String descricao, String icone) {
        JPanel cartao = new JPanel(new BorderLayout(0, 20));
        cartao.setBackground(UIUtils.COR_CARTAO);
        cartao.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.COR_BORDA),
            BorderFactory.createEmptyBorder(60, 40, 60, 40)
        ));
        cartao.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblIcone = new JLabel(icone);
        lblIcone.setFont(new Font("Segoe MDL2 Assets", Font.PLAIN, 72));
        lblIcone.setForeground(UIUtils.COR_SUCESSO);
        lblIcone.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(UIUtils.COR_TEXTO);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblDesc = new JLabel(descricao);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDesc.setForeground(UIUtils.COR_TEXTO_SUAVE);
        lblDesc.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel painelTextos = new JPanel(new GridLayout(2, 1, 0, 10));
        painelTextos.setOpaque(false);
        painelTextos.add(lblTitulo);
        painelTextos.add(lblDesc);

        cartao.add(lblIcone, BorderLayout.CENTER);
        cartao.add(painelTextos, BorderLayout.SOUTH);

        cartao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cartao.setBackground(UIUtils.COR_DESTAQUE_FUNDO);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cartao.setBackground(UIUtils.COR_CARTAO);
            }
        });

        return cartao;
    }

    private JPanel criarBotaoSelecao(String pais, String sigla, Color corCirculo, Color corSigla) {
        JPanel cartao = new JPanel(new BorderLayout());
        cartao.setBackground(UIUtils.COR_CARTAO);
        cartao.setBorder(BorderFactory.createLineBorder(UIUtils.COR_BORDA));
        cartao.setPreferredSize(new Dimension(140, 150));
        cartao.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel painelCirculo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = 70;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                g2.setColor(corCirculo);
                g2.fillOval(x, y, size, size);
                
                g2.setColor(corSigla);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (size - fm.stringWidth(sigla)) / 2;
                int ty = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(sigla, tx, ty);
                g2.dispose();
            }
        };
        painelCirculo.setOpaque(false);

        JLabel lblNome = new JLabel(pais, SwingConstants.CENTER);
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblNome.setForeground(UIUtils.COR_TEXTO);
        lblNome.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        cartao.add(painelCirculo, BorderLayout.CENTER);
        cartao.add(lblNome, BorderLayout.SOUTH);

        cartao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cartao.setBackground(UIUtils.COR_DESTAQUE_FUNDO);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cartao.setBackground(UIUtils.COR_CARTAO);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                carregarCamisas(pais);
            }
        });

        return cartao;
    }

    private void carregarCamisas(String pais) {
        lblTituloTabela.setText("Catálogo: " + pais);
        String sql;
        boolean isOutros = "Outros".equals(pais);

        if (isOutros) {
            sql = "SELECT e.nome AS Selecao, c.modelo AS Modelo, c.versao AS Versao, c.tamanho AS Tamanho, c.preco AS Preco, c.quantidade_estoque AS Estoque " +
                  "FROM Camisa c JOIN Equipe e ON c.fk_id_equipe = e.id_equipe " +
                  "WHERE e.equipe_tipo = 2 AND e.nome NOT IN ('Brasil', 'Argentina', 'Portugal', 'Alemanha', 'Italia', 'Holanda', 'Inglaterra', 'Franca', 'Espanha')";
        } else {
            sql = "SELECT e.nome AS Selecao, c.modelo AS Modelo, c.versao AS Versao, c.tamanho AS Tamanho, c.preco AS Preco, c.quantidade_estoque AS Estoque " +
                  "FROM Camisa c JOIN Equipe e ON c.fk_id_equipe = e.id_equipe " +
                  "WHERE e.equipe_tipo = 2 AND e.nome = ?";
        }

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            if (!isOutros) {
                stmt.setString(1, pais);
            }

            try (ResultSet rs = stmt.executeQuery()) {
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
                        Object valor = rs.getObject(i);
                        if (metaData.getColumnName(i).equalsIgnoreCase("preco") && valor != null) {
                            valor = String.format("R$ %.2f", ((Number) valor).doubleValue());
                        }
                        linha.add(valor);
                    }
                    dados.add(linha);
                }
                
                tabelaCamisas.setModel(UIUtils.criarModeloSomenteLeitura(dados, colunas));
            }

            layoutCards.show(painelCards, "CATALOGO");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar catálogo: " + ex.getMessage());
        }
    }
}