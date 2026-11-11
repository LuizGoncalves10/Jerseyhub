package telas;

import banco.ConexaoBanco;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Vector;

public class PainelLojaClubes extends JPanel {

    private final CardLayout layoutCards = new CardLayout();
    private final JPanel painelCards = new JPanel(layoutCards);
    
    private JTable tabelaCamisas;
    private JLabel lblTituloTabela;
    private JLabel lblTituloLigas;
    private JLabel lblTituloTimes;
    
    private JPanel painelGridLigas;
    private JPanel painelGridTimes;

    private CardLayout layoutMenuPrincipal;
    private JPanel painelMenuPrincipal;
    
    private String ligaAtual = "";
    private String origemCatalogo = "GRID_TIMES";
    
    private PainelCampeonatoAtual painelCampeonatoAtual;

    public PainelLojaClubes(CardLayout layoutMenuPrincipal, JPanel painelMenuPrincipal) {
        this.layoutMenuPrincipal = layoutMenuPrincipal;
        this.painelMenuPrincipal = painelMenuPrincipal;

        setOpaque(false);
        setLayout(new BorderLayout());

        painelCampeonatoAtual = new PainelCampeonatoAtual(layoutCards, painelCards);

        painelCards.setOpaque(false);
        painelCards.add(criarTelaPaises(), "GRID_PAISES");
        painelCards.add(criarTelaLigas(), "GRID_LIGAS");
        painelCards.add(criarTelaTimes(), "GRID_TIMES");
        painelCards.add(criarTelaCatalogo(), "CATALOGO_CLUBES");
        painelCards.add(painelCampeonatoAtual, "CAMPEONATO_ATUAL");

        add(painelCards, BorderLayout.CENTER);
    }

    private JPanel criarTelaPaises() {
        JPanel painel = new JPanel(new BorderLayout(0, 30));
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        
        JButton btnVoltar = UIUtils.criarBotao("Voltar ao Menu", Color.decode("#374151"));
        btnVoltar.addActionListener(e -> layoutMenuPrincipal.show(painelMenuPrincipal, "MENU"));
        
        JLabel lblTitulo = new JLabel("Escolha o País / Região");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(UIUtils.COR_TEXTO);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        topo.add(btnVoltar, BorderLayout.WEST);
        topo.add(lblTitulo, BorderLayout.CENTER);
        topo.add(Box.createRigidArea(new Dimension(150, 0)), BorderLayout.EAST);

        JPanel gridPaises = new JPanel(new GridLayout(2, 5, 20, 20));
        gridPaises.setOpaque(false);

        gridPaises.add(criarBotaoAvatar("Brasil", "BRA", Color.decode("#22C55E"), Color.WHITE, "PAIS"));
        gridPaises.add(criarBotaoAvatar("Argentina", "ARG", Color.decode("#38BDF8"), Color.WHITE, "PAIS"));
        gridPaises.add(criarBotaoAvatar("Italia", "ITA", Color.decode("#3B82F6"), Color.WHITE, "PAIS"));
        gridPaises.add(criarBotaoAvatar("Alemanha", "GER", Color.decode("#374151"), Color.WHITE, "PAIS"));
        gridPaises.add(criarBotaoAvatar("Espanha", "ESP", Color.decode("#DC2626"), Color.decode("#FBBF24"), "PAIS"));
        gridPaises.add(criarBotaoAvatar("Inglaterra", "ENG", Color.decode("#F3F4F6"), Color.decode("#1F2937"), "PAIS"));
        gridPaises.add(criarBotaoAvatar("Franca", "FRA", Color.decode("#1D4ED8"), Color.WHITE, "PAIS"));
        gridPaises.add(criarBotaoAvatar("EUA", "EUA", Color.decode("#E11D48"), Color.WHITE, "PAIS"));
        gridPaises.add(criarBotaoAvatar("Arabia Saudita", "KSA", Color.decode("#16A34A"), Color.WHITE, "PAIS"));
        gridPaises.add(criarBotaoAvatar("Outros", "OUT", Color.decode("#64748B"), Color.WHITE, "PAIS"));

        JPanel wrapperGrid = new JPanel(new BorderLayout());
        wrapperGrid.setOpaque(false);
        wrapperGrid.add(gridPaises, BorderLayout.NORTH);

        painel.add(topo, BorderLayout.NORTH);
        painel.add(wrapperGrid, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarTelaLigas() {
        JPanel painel = new JPanel(new BorderLayout(0, 30));
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        
        JButton btnVoltar = UIUtils.criarBotao("Voltar aos Países", Color.decode("#374151"));
        btnVoltar.addActionListener(e -> layoutCards.show(painelCards, "GRID_PAISES"));
        
        lblTituloLigas = new JLabel("Escolha a Liga");
        lblTituloLigas.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTituloLigas.setForeground(UIUtils.COR_TEXTO);
        lblTituloLigas.setHorizontalAlignment(SwingConstants.CENTER);

        topo.add(btnVoltar, BorderLayout.WEST);
        topo.add(lblTituloLigas, BorderLayout.CENTER);
        topo.add(Box.createRigidArea(new Dimension(150, 0)), BorderLayout.EAST);

        painelGridLigas = new JPanel(new GridLayout(0, 5, 20, 20));
        painelGridLigas.setOpaque(false);

        JPanel wrapperGrid = new JPanel(new BorderLayout());
        wrapperGrid.setOpaque(false);
        wrapperGrid.add(painelGridLigas, BorderLayout.NORTH);

        JScrollPane scroll = UIUtils.criarRolagem(wrapperGrid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        painel.add(topo, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarTelaTimes() {
        JPanel painel = new JPanel(new BorderLayout(0, 30));
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        
        JButton btnVoltar = UIUtils.criarBotao("Voltar", Color.decode("#374151"));
        btnVoltar.addActionListener(e -> {
            if ("Outros".equals(ligaAtual)) {
                layoutCards.show(painelCards, "GRID_PAISES");
            } else {
                layoutCards.show(painelCards, "GRID_LIGAS");
            }
        });
        
        lblTituloTimes = new JLabel("Escolha o Time");
        lblTituloTimes.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTituloTimes.setForeground(UIUtils.COR_TEXTO);
        lblTituloTimes.setHorizontalAlignment(SwingConstants.CENTER);

        topo.add(btnVoltar, BorderLayout.WEST);
        topo.add(lblTituloTimes, BorderLayout.CENTER);
        topo.add(Box.createRigidArea(new Dimension(150, 0)), BorderLayout.EAST);

        painelGridTimes = new JPanel(new GridLayout(0, 5, 20, 20));
        painelGridTimes.setOpaque(false);

        JPanel wrapperGrid = new JPanel(new BorderLayout());
        wrapperGrid.setOpaque(false);
        wrapperGrid.add(painelGridTimes, BorderLayout.NORTH);
        
        JScrollPane scroll = UIUtils.criarRolagem(wrapperGrid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        painel.add(topo, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarTelaCatalogo() {
        JPanel painel = new JPanel(new BorderLayout(0, 20));
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);

        JButton btnVoltar = UIUtils.criarBotao("Voltar", Color.decode("#374151"));
        btnVoltar.addActionListener(e -> layoutCards.show(painelCards, origemCatalogo));

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

    private JPanel criarBotaoAvatar(String rotulo, String sigla, Color corCirculo, Color corSigla, String acao) {
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

        JLabel lblNome = new JLabel(rotulo, SwingConstants.CENTER);
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNome.setForeground(UIUtils.COR_TEXTO);
        lblNome.setBorder(BorderFactory.createEmptyBorder(0, 5, 15, 5));

        cartao.add(painelCirculo, BorderLayout.CENTER);
        cartao.add(lblNome, BorderLayout.SOUTH);

        cartao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { cartao.setBackground(UIUtils.COR_DESTAQUE_FUNDO); }
            @Override
            public void mouseExited(MouseEvent e) { cartao.setBackground(UIUtils.COR_CARTAO); }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (acao.equals("PAIS")) {
                    if (rotulo.equals("Outros")) {
                        origemCatalogo = "GRID_PAISES";
                        carregarCamisasOutros();
                    } else {
                        mostrarLigas(rotulo);
                    }
                }
                else if (acao.equals("LIGA")) mostrarTimes(rotulo);
                else if (acao.equals("TIME")) {
                    origemCatalogo = "GRID_TIMES";
                    carregarCamisas(rotulo);
                }
                else if (acao.equals("CAMPEONATO_ATUAL")) {
                    painelCampeonatoAtual.carregarCampeonato(ligaAtual);
                    layoutCards.show(painelCards, "CAMPEONATO_ATUAL");
                }
            }
        });

        return cartao;
    }

    private void mostrarLigas(String pais) {
        lblTituloLigas.setText("Ligas: " + pais);
        painelGridLigas.removeAll();

        switch (pais) {
            case "Brasil":
                painelGridLigas.add(criarBotaoAvatar("Brasileirão Assaí", "1", Color.decode("#FBBF24"), Color.decode("#1F2937"), "LIGA"));
                painelGridLigas.add(criarBotaoAvatar("Brasileirão Série B", "2", Color.decode("#22C55E"), Color.WHITE, "LIGA"));
                break;
            case "Inglaterra":
                painelGridLigas.add(criarBotaoAvatar("Premier League", "1", Color.decode("#A855F7"), Color.WHITE, "LIGA"));
                painelGridLigas.add(criarBotaoAvatar("Championship", "2", Color.decode("#3B82F6"), Color.WHITE, "LIGA"));
                break;
            case "Espanha":
                painelGridLigas.add(criarBotaoAvatar("La Liga", "1", Color.decode("#EF4444"), Color.WHITE, "LIGA"));
                break;
            case "Italia":
                painelGridLigas.add(criarBotaoAvatar("Serie A", "1", Color.decode("#2563EB"), Color.WHITE, "LIGA"));
                break;
            case "Alemanha":
                painelGridLigas.add(criarBotaoAvatar("Bundesliga", "1", Color.decode("#DC2626"), Color.WHITE, "LIGA"));
                break;
            case "Franca":
                painelGridLigas.add(criarBotaoAvatar("Ligue 1", "1", Color.decode("#1D4ED8"), Color.WHITE, "LIGA"));
                break;
            case "Argentina":
                painelGridLigas.add(criarBotaoAvatar("Liga Profesional", "1", Color.decode("#38BDF8"), Color.WHITE, "LIGA"));
                break;
            case "EUA":
                painelGridLigas.add(criarBotaoAvatar("MLS", "1", Color.decode("#E11D48"), Color.WHITE, "LIGA"));
                break;
            case "Arabia Saudita":
                painelGridLigas.add(criarBotaoAvatar("Saudi Pro League", "1", Color.decode("#16A34A"), Color.WHITE, "LIGA"));
                break;
        }

        painelGridLigas.revalidate();
        painelGridLigas.repaint();
        layoutCards.show(painelCards, "GRID_LIGAS");
    }

    private void mostrarTimes(String liga) {
        ligaAtual = liga;
        lblTituloTimes.setText("Equipes: " + liga);
        painelGridTimes.removeAll();

        painelGridTimes.add(criarBotaoAvatar("Campeonato Atual", "★", Color.decode("#EAB308"), Color.WHITE, "CAMPEONATO_ATUAL"));

        String sql = "";
        
        if (liga.equals("Brasileirão Assaí")) {
            sql = "SELECT e.nome FROM Equipe e JOIN Clube c ON e.id_equipe = c.fk_id_equipe WHERE c.liga = 'Campeonato Brasileiro' AND e.nome IN ('Flamengo', 'Palmeiras', 'Sao Paulo', 'Corinthians', 'Gremio', 'Internacional', 'Vasco da Gama', 'Fluminense', 'Cruzeiro', 'Atletico Mineiro', 'Bahia', 'Vitoria', 'Santos') ORDER BY e.nome";
        } else if (liga.equals("Brasileirão Série B")) {
            sql = "SELECT e.nome FROM Equipe e JOIN Clube c ON e.id_equipe = c.fk_id_equipe WHERE c.liga = 'Campeonato Brasileiro' AND e.nome IN ('Sport Recife', 'Nautico') ORDER BY e.nome";
        } else if (liga.equals("Liga Profesional")) {
            sql = "SELECT e.nome FROM Equipe e JOIN Clube c ON e.id_equipe = c.fk_id_equipe WHERE c.liga = 'Liga Profesional Argentina' ORDER BY e.nome";
        } else {
            sql = "SELECT e.nome FROM Equipe e JOIN Clube c ON e.id_equipe = c.fk_id_equipe WHERE c.liga = ? ORDER BY e.nome";
        }

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            if (!liga.equals("Brasileirão Assaí") && !liga.equals("Brasileirão Série B") && !liga.equals("Liga Profesional")) {
                stmt.setString(1, liga);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String nome = rs.getString("nome");
                    String sigla = nome.length() >= 3 ? nome.substring(0, 3).toUpperCase() : nome.toUpperCase();
                    painelGridTimes.add(criarBotaoAvatar(nome, sigla, UIUtils.COR_SUCESSO, Color.WHITE, "TIME"));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar times: " + ex.getMessage());
        }

        painelGridTimes.revalidate();
        painelGridTimes.repaint();
        layoutCards.show(painelCards, "GRID_TIMES");
    }

    private void carregarCamisas(String time) {
        lblTituloTabela.setText("Catálogo: " + time);
        
        String sql = "SELECT e.nome AS Equipe, c.modelo AS Modelo, c.versao AS Versao, c.tamanho AS Tamanho, c.preco AS Preco, c.quantidade_estoque AS Estoque " +
                     "FROM Camisa c JOIN Equipe e ON c.fk_id_equipe = e.id_equipe " +
                     "WHERE e.equipe_tipo = 1 AND e.nome = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, time);

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

            layoutCards.show(painelCards, "CATALOGO_CLUBES");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar catálogo: " + ex.getMessage());
        }
    }

    private void carregarCamisasOutros() {
        lblTituloTabela.setText("Catálogo: Outros Clubes");
        
        String sql = "SELECT e.nome AS Equipe, c.modelo AS Modelo, c.versao AS Versao, c.tamanho AS Tamanho, c.preco AS Preco, c.quantidade_estoque AS Estoque " +
                     "FROM Camisa c " +
                     "JOIN Equipe e ON c.fk_id_equipe = e.id_equipe " +
                     "JOIN Clube cl ON e.id_equipe = cl.fk_id_equipe " +
                     "WHERE e.equipe_tipo = 1 AND (" +
                     "cl.liga NOT IN ('Campeonato Brasileiro', 'Premier League', 'Championship', 'La Liga', 'Serie A', 'Bundesliga', 'Ligue 1', 'Liga Profesional Argentina', 'MLS', 'Saudi Pro League') " +
                     "OR (cl.liga = 'Campeonato Brasileiro' AND e.nome NOT IN ('Flamengo', 'Palmeiras', 'Sao Paulo', 'Corinthians', 'Gremio', 'Internacional', 'Vasco da Gama', 'Fluminense', 'Cruzeiro', 'Atletico Mineiro', 'Bahia', 'Vitoria', 'Santos', 'Sport Recife', 'Nautico'))" +
                     ") ORDER BY e.nome";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

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

            layoutCards.show(painelCards, "CATALOGO_CLUBES");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar catálogo: " + ex.getMessage());
        }
    }
}