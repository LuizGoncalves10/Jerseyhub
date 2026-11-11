package telas;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Vector;

public class PainelCampeonatoAtual extends JPanel {

    private CardLayout layoutPai;
    private JPanel painelPai;
    private JPanel painelConteudo;
    private JLabel lblTitulo;
    private JComboBox<String> comboFase;
    private JTable tabelaClassificacao;

    public PainelCampeonatoAtual(CardLayout layoutPai, JPanel painelPai) {
        this.layoutPai = layoutPai;
        this.painelPai = painelPai;

        setOpaque(false);
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);

        JButton btnVoltar = UIUtils.criarBotao("Voltar aos Times", Color.decode("#374151"));
        btnVoltar.addActionListener(e -> layoutPai.show(painelPai, "GRID_TIMES"));

        lblTitulo = new JLabel("Campeonato Atual");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(UIUtils.COR_TEXTO);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        comboFase = new JComboBox<>();
        comboFase.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboFase.setPreferredSize(new Dimension(200, 35));
        comboFase.setVisible(false);
        comboFase.addActionListener(e -> {
            if (comboFase.getSelectedItem() != null) {
                atualizarTabela();
            }
        });
        
        JPanel painelFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelFiltro.setOpaque(false);
        painelFiltro.add(comboFase);

        topo.add(btnVoltar, BorderLayout.WEST);
        topo.add(lblTitulo, BorderLayout.CENTER);
        topo.add(painelFiltro, BorderLayout.EAST);

        painelConteudo = new JPanel(new BorderLayout());
        painelConteudo.setOpaque(false);

        tabelaClassificacao = criarTabelaClassificacao();
        JScrollPane scroll = UIUtils.criarRolagem(tabelaClassificacao);
        painelConteudo.add(scroll, BorderLayout.CENTER);

        add(topo, BorderLayout.NORTH);
        add(painelConteudo, BorderLayout.CENTER);
    }

    public void carregarCampeonato(String liga) {
        lblTitulo.setText("Classificação: " + liga);
        
        comboFase.setVisible(false);
        comboFase.removeAllItems();

        if (liga.equals("MLS")) {
            comboFase.addItem("Conferência Leste");
            comboFase.addItem("Conferência Oeste");
            comboFase.setVisible(true);
        } else if (liga.equals("Liga Profesional")) {
            comboFase.addItem("Grupo A");
            comboFase.addItem("Grupo B");
            comboFase.setVisible(true);
        }

        atualizarTabela();
    }

    private void atualizarTabela() {
        String titulo = lblTitulo.getText();
        String fase = (comboFase.isVisible() && comboFase.getSelectedItem() != null) ? (String) comboFase.getSelectedItem() : "";
        
        Vector<String> colunas = new Vector<>();
        colunas.add("Pos");
        colunas.add("Clube");
        colunas.add("Pts");
        colunas.add("PJ");
        colunas.add("VIT");
        colunas.add("E");
        colunas.add("DER");
        colunas.add("GM");
        colunas.add("GC");
        colunas.add("SG");

        Vector<Vector<Object>> dados = new Vector<>();

        if (titulo.contains("Brasileirão Assaí")) {
            dados = getDadosBrasileiraoAssai();
        } else if (titulo.contains("Brasileirão Série B")) {
            dados = getDadosSerieB();
        } else if (titulo.contains("Premier League")) {
            dados = getDadosPremierLeague();
        } else if (titulo.contains("Championship")) {
            dados = getDadosChampionship();
        } else if (titulo.contains("La Liga")) {
            dados = getDadosLaLiga();
        } else if (titulo.contains("Serie A")) {
            dados = getDadosSerieA();
        } else if (titulo.contains("Bundesliga")) {
            dados = getDadosBundesliga();
        } else if (titulo.contains("Ligue 1")) {
            dados = getDadosLigue1();
        } else if (titulo.contains("Saudi Pro League")) {
            dados = getDadosSaudi();
        } else if (titulo.contains("MLS")) {
            if ("Conferência Leste".equals(fase)) {
                dados = getDadosMLSLeste();
            } else {
                dados = getDadosMLSOeste();
            }
        } else if (titulo.contains("Liga Profesional")) {
            if ("Grupo A".equals(fase)) {
                dados = getDadosArgentinaGrupoA();
            } else {
                dados = getDadosArgentinaGrupoB();
            }
        }

        DefaultTableModel modelo = new DefaultTableModel(dados, colunas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaClassificacao.setModel(modelo);
        configurarColunasTabela();
    }

    private JTable criarTabelaClassificacao() {
        JTable tabela = new JTable();
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabela.setForeground(UIUtils.COR_TEXTO);
        tabela.setBackground(UIUtils.COR_FUNDO);
        tabela.setRowHeight(35);
        tabela.setShowGrid(false);
        tabela.setShowHorizontalLines(true);
        tabela.setGridColor(UIUtils.COR_BORDA);
        tabela.setSelectionBackground(UIUtils.COR_DESTAQUE_FUNDO);
        tabela.setSelectionForeground(UIUtils.COR_TEXTO);
        
        JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cabecalho.setForeground(UIUtils.COR_TEXTO_SUAVE);
        cabecalho.setBackground(UIUtils.COR_CARTAO);
        cabecalho.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.COR_BORDA));
        cabecalho.setPreferredSize(new Dimension(100, 40));
        
        return tabela;
    }

    private void configurarColunasTabela() {
        tabelaClassificacao.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelaClassificacao.getColumnModel().getColumn(1).setPreferredWidth(300);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        DefaultTableCellRenderer boldCenterRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                return c;
            }
        };
        boldCenterRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < tabelaClassificacao.getColumnCount(); i++) {
            if (i == 0 || i == 2) {
                tabelaClassificacao.getColumnModel().getColumn(i).setCellRenderer(boldCenterRenderer);
            } else if (i != 1) {
                tabelaClassificacao.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    private Vector<Vector<Object>> getDadosBrasileiraoAssai() {
        Vector<Vector<Object>> d = new Vector<>();
        d.add(criarLinha(1, "Flamengo", 76, 34, 23, 7, 4, 71, 29, 42));
        d.add(criarLinha(2, "Palmeiras", 73, 34, 21, 10, 3, 62, 23, 39));
        d.add(criarLinha(3, "Cruzeiro", 60, 34, 18, 6, 10, 55, 48, 7));
        d.add(criarLinha(4, "Fluminense", 57, 34, 16, 9, 9, 55, 42, 13));
        d.add(criarLinha(5, "Athletico-PR", 54, 34, 15, 9, 10, 51, 43, 8));
        d.add(criarLinha(6, "Bahia", 53, 34, 14, 11, 9, 50, 45, 5));
        d.add(criarLinha(7, "Atlético-MG", 50, 34, 14, 8, 12, 44, 40, 4));
        d.add(criarLinha(8, "Bragantino", 47, 34, 13, 8, 13, 44, 39, 5));
        d.add(criarLinha(9, "Santos", 47, 34, 12, 11, 11, 50, 49, 1));
        d.add(criarLinha(10, "São Paulo", 44, 34, 12, 8, 14, 39, 39, 0));
        d.add(criarLinha(11, "Botafogo", 42, 34, 11, 9, 14, 46, 53, -7));
        d.add(criarLinha(12, "Coritiba", 42, 34, 11, 9, 14, 40, 51, -11));
        d.add(criarLinha(13, "Vasco da Gama", 41, 33, 11, 8, 14, 43, 50, -7));
        d.add(criarLinha(14, "Mirassol", 40, 34, 10, 10, 14, 39, 48, -9));
        d.add(criarLinha(15, "EC Vitória", 38, 34, 10, 8, 16, 35, 51, -16));
        d.add(criarLinha(16, "Internacional", 38, 34, 9, 11, 14, 38, 42, -4));
        d.add(criarLinha(17, "Corinthians", 37, 34, 9, 10, 15, 36, 42, -6));
        d.add(criarLinha(18, "Grêmio", 37, 34, 9, 10, 15, 38, 47, -9));
        d.add(criarLinha(19, "Remo", 29, 34, 6, 11, 17, 37, 57, -20));
        d.add(criarLinha(20, "Chapecoense", 21, 33, 4, 9, 20, 33, 68, -35));
        return d;
    }

    private Vector<Vector<Object>> getDadosSerieB() {
        Vector<Vector<Object>> d = new Vector<>();
        d.add(criarLinha(1, "Novorizontino", 70, 37, 20, 10, 7, 66, 32, 34));
        d.add(criarLinha(2, "Juventude", 68, 37, 19, 11, 7, 49, 26, 23));
        d.add(criarLinha(3, "Vila Nova", 66, 37, 19, 9, 9, 53, 38, 15));
        d.add(criarLinha(4, "Criciúma", 62, 37, 18, 8, 11, 38, 31, 7));
        d.add(criarLinha(5, "Fortaleza", 62, 37, 16, 14, 7, 44, 33, 11));
        d.add(criarLinha(6, "CRB", 58, 37, 17, 7, 13, 57, 51, 6));
        d.add(criarLinha(7, "Goiás", 58, 37, 17, 7, 13, 39, 41, -2));
        d.add(criarLinha(8, "Atlético-GO", 58, 37, 15, 13, 9, 45, 35, 10));
        d.add(criarLinha(9, "Sport Recife", 55, 37, 14, 13, 10, 50, 42, 8));
        d.add(criarLinha(10, "Operário", 54, 37, 15, 9, 13, 49, 50, -1));
        d.add(criarLinha(11, "São Bernardo", 52, 37, 14, 10, 13, 52, 42, 10));
        d.add(criarLinha(12, "Botafogo SP", 51, 37, 14, 9, 14, 45, 39, 6));
        d.add(criarLinha(13, "Náutico", 49, 38, 13, 10, 15, 41, 49, -8));
        d.add(criarLinha(14, "Cuiabá", 47, 37, 11, 14, 12, 34, 32, 2));
        d.add(criarLinha(15, "Athletic", 47, 37, 11, 14, 12, 44, 44, 0));
        d.add(criarLinha(16, "Ceará SC", 43, 37, 11, 10, 16, 38, 47, -9));
        d.add(criarLinha(17, "Avaí", 40, 37, 11, 7, 19, 35, 47, -12));
        d.add(criarLinha(18, "Londrina", 32, 38, 8, 8, 22, 45, 59, -14));
        d.add(criarLinha(19, "América-MG", 29, 37, 8, 5, 24, 27, 55, -28));
        d.add(criarLinha(20, "Ponte Preta", 15, 37, 3, 6, 28, 21, 79, -58));
        return d;
    }

    private Vector<Vector<Object>> getDadosPremierLeague() {
        Vector<Vector<Object>> d = new Vector<>();
        d.add(criarLinha(1, "Arsenal", 27, 10, 9, 0, 1, 15, 5, 10));
        d.add(criarLinha(2, "Brighton", 22, 10, 7, 1, 2, 26, 7, 19));
        d.add(criarLinha(3, "City", 21, 10, 7, 0, 3, 21, 13, 8));
        d.add(criarLinha(4, "Liverpool", 18, 10, 5, 3, 2, 14, 10, 4));
        d.add(criarLinha(5, "Chelsea", 16, 10, 5, 1, 4, 19, 16, 3));
        d.add(criarLinha(6, "Everton", 16, 10, 4, 4, 2, 12, 10, 2));
        d.add(criarLinha(7, "Bournemouth", 15, 10, 4, 3, 3, 16, 14, 2));
        d.add(criarLinha(8, "Brentford", 14, 10, 3, 5, 2, 16, 11, 5));
        d.add(criarLinha(9, "Leeds", 14, 10, 3, 5, 2, 10, 8, 2));
        d.add(criarLinha(10, "Aston Villa", 13, 10, 3, 4, 3, 14, 17, -3));
        d.add(criarLinha(11, "Spurs", 12, 10, 3, 3, 4, 10, 11, -1));
        d.add(criarLinha(12, "Newcastle", 12, 10, 3, 3, 4, 13, 16, -3));
        d.add(criarLinha(13, "Hull City", 11, 10, 2, 5, 3, 11, 11, 0));
        d.add(criarLinha(14, "Ipswich Town", 11, 10, 3, 2, 5, 15, 20, -5));
        d.add(criarLinha(15, "Fulham", 10, 10, 2, 4, 4, 10, 12, -2));
        d.add(criarLinha(16, "Nottingham Forest", 9, 10, 2, 3, 5, 9, 13, -4));
        d.add(criarLinha(17, "Coventry City", 9, 10, 3, 0, 7, 10, 19, -9));
        d.add(criarLinha(18, "Manchester United", 8, 10, 1, 5, 4, 14, 19, -5));
        d.add(criarLinha(19, "Sunderland", 8, 10, 2, 2, 6, 8, 17, -9));
        d.add(criarLinha(20, "Crystal Palace", 7, 10, 2, 1, 7, 10, 24, -14));
        return d;
    }

    private Vector<Vector<Object>> getDadosChampionship() {
        Vector<Vector<Object>> d = new Vector<>();
        d.add(criarLinha(1, "Birmingham", 29, 15, 8, 5, 2, 28, 14, 14));
        d.add(criarLinha(2, "Middlesbrough", 29, 15, 8, 5, 2, 30, 19, 11));
        d.add(criarLinha(3, "Stoke City", 29, 15, 9, 2, 4, 24, 18, 6));
        d.add(criarLinha(4, "Wolves", 28, 15, 8, 4, 3, 26, 18, 8));
        d.add(criarLinha(5, "West Bromwich", 28, 15, 8, 4, 3, 23, 15, 8));
        d.add(criarLinha(6, "Swansea City", 26, 15, 8, 2, 5, 22, 18, 4));
        d.add(criarLinha(7, "Southampton", 24, 15, 8, 4, 3, 29, 15, 14));
        d.add(criarLinha(8, "QPR", 24, 15, 6, 6, 3, 24, 18, 6));
        d.add(criarLinha(9, "West Ham", 23, 15, 6, 5, 4, 37, 23, 14));
        d.add(criarLinha(10, "Sheffield United", 22, 15, 6, 4, 5, 21, 19, 2));
        d.add(criarLinha(11, "Portsmouth", 22, 15, 6, 4, 5, 20, 18, 2));
        d.add(criarLinha(12, "Norwich", 21, 15, 7, 0, 8, 32, 24, 8));
        d.add(criarLinha(13, "Blackburn Rovers", 21, 15, 6, 3, 6, 22, 23, -1));
        d.add(criarLinha(14, "Cardiff", 18, 15, 4, 6, 5, 27, 27, 0));
        d.add(criarLinha(15, "Wrexham", 18, 15, 4, 6, 5, 18, 20, -2));
        d.add(criarLinha(16, "Millwall", 18, 15, 5, 3, 7, 23, 27, -4));
        d.add(criarLinha(17, "Bristol City", 18, 15, 5, 3, 7, 16, 24, -8));
        d.add(criarLinha(18, "Bolton Wanderers", 17, 15, 5, 2, 8, 20, 29, -9));
        d.add(criarLinha(19, "Charlton", 17, 15, 4, 5, 6, 13, 23, -10));
        d.add(criarLinha(20, "Watford", 15, 15, 4, 3, 8, 13, 23, -10));
        d.add(criarLinha(21, "Lincoln City", 15, 15, 4, 3, 8, 15, 26, -11));
        d.add(criarLinha(22, "Burnley", 10, 15, 1, 7, 7, 22, 31, -9));
        d.add(criarLinha(23, "Derby County", 10, 15, 2, 4, 9, 14, 30, -16));
        d.add(criarLinha(24, "Preston", 8, 15, 2, 2, 11, 13, 30, -17));
        return d;
    }

    private Vector<Vector<Object>> getDadosLaLiga() {
        Vector<Vector<Object>> d = new Vector<>();
        d.add(criarLinha(1, "Barcelona", 33, 12, 11, 0, 1, 48, 13, 35));
        d.add(criarLinha(2, "Real Madrid", 30, 12, 10, 0, 2, 31, 10, 21));
        d.add(criarLinha(3, "Atlético Madrid", 28, 12, 9, 1, 2, 23, 10, 13));
        d.add(criarLinha(4, "Betis", 26, 12, 8, 2, 2, 20, 15, 5));
        d.add(criarLinha(5, "Real Sociedad", 22, 12, 7, 1, 4, 21, 20, 1));
        d.add(criarLinha(6, "Sevilla", 18, 12, 5, 3, 4, 17, 19, -2));
        d.add(criarLinha(7, "Ath. Bilbao", 16, 11, 4, 4, 3, 15, 11, 4));
        d.add(criarLinha(8, "Villarreal", 15, 12, 4, 3, 5, 17, 15, 2));
        d.add(criarLinha(9, "La Coruña", 15, 12, 3, 6, 3, 15, 14, 1));
        d.add(criarLinha(10, "Racing", 14, 12, 4, 2, 6, 17, 30, -13));
        d.add(criarLinha(11, "Celta de Vigo", 13, 12, 2, 7, 3, 10, 10, 0));
        d.add(criarLinha(12, "Alavés", 13, 12, 3, 4, 5, 15, 17, -2));
        d.add(criarLinha(13, "Espanyol", 12, 12, 3, 3, 6, 13, 14, -1));
        d.add(criarLinha(14, "Rayo Vallecano", 12, 11, 3, 3, 5, 15, 21, -6));
        d.add(criarLinha(15, "Getafe", 12, 12, 3, 3, 6, 8, 18, -10));
        d.add(criarLinha(16, "Osasuna", 12, 12, 3, 3, 6, 12, 23, -11));
        d.add(criarLinha(17, "Levante", 11, 12, 2, 5, 5, 16, 21, -5));
        d.add(criarLinha(18, "Elche", 10, 12, 2, 4, 6, 14, 20, -6));
        d.add(criarLinha(19, "Valencia", 9, 12, 2, 3, 7, 8, 20, -12));
        d.add(criarLinha(20, "Málaga", 5, 12, 0, 5, 7, 9, 23, -14));
        return d;
    }

    private Vector<Vector<Object>> getDadosSerieA() {
        Vector<Vector<Object>> d = new Vector<>();
        d.add(criarLinha(1, "Lazio", 29, 11, 9, 2, 0, 25, 9, 16));
        d.add(criarLinha(2, "Inter", 28, 11, 9, 1, 1, 28, 13, 15));
        d.add(criarLinha(3, "Roma", 27, 11, 8, 3, 0, 31, 10, 21));
        d.add(criarLinha(4, "Milan", 24, 11, 7, 3, 1, 23, 13, 10));
        d.add(criarLinha(5, "Como", 23, 11, 7, 2, 2, 18, 10, 8));
        d.add(criarLinha(6, "Frosinone", 20, 11, 6, 2, 3, 24, 15, 9));
        d.add(criarLinha(7, "Juventus", 18, 11, 5, 3, 3, 15, 12, 3));
        d.add(criarLinha(8, "Bologna", 17, 11, 5, 2, 4, 16, 14, 2));
        d.add(criarLinha(9, "Cagliari", 16, 11, 5, 1, 5, 10, 15, -5));
        d.add(criarLinha(10, "Napoli", 15, 11, 4, 3, 4, 18, 14, 4));
        d.add(criarLinha(11, "Torino", 14, 11, 4, 2, 5, 15, 17, -2));
        d.add(criarLinha(12, "Atalanta", 14, 11, 4, 2, 5, 17, 20, -3));
        d.add(criarLinha(13, "Genoa", 13, 11, 4, 1, 6, 11, 16, -5));
        d.add(criarLinha(14, "Lecce", 10, 11, 3, 1, 7, 12, 22, -10));
        d.add(criarLinha(15, "Fiorentina", 9, 11, 2, 3, 6, 17, 25, -8));
        d.add(criarLinha(16, "Parma", 8, 11, 2, 2, 7, 9, 17, -8));
        d.add(criarLinha(17, "Sassuolo", 8, 11, 2, 2, 7, 16, 29, -13));
        d.add(criarLinha(18, "A.C. Monza", 7, 11, 2, 1, 8, 17, 25, -8));
        d.add(criarLinha(19, "Venezia", 6, 11, 2, 0, 9, 8, 21, -13));
        d.add(criarLinha(20, "Udinese", 5, 11, 1, 2, 8, 12, 25, -13));
        return d;
    }

    private Vector<Vector<Object>> getDadosBundesliga() {
        Vector<Vector<Object>> d = new Vector<>();
        d.add(criarLinha(1, "Bayern", 21, 9, 6, 3, 0, 29, 9, 20));
        d.add(criarLinha(2, "Borussia", 21, 9, 6, 3, 0, 16, 7, 9));
        d.add(criarLinha(3, "Freiburg", 19, 9, 6, 1, 2, 20, 10, 10));
        d.add(criarLinha(4, "RB Leipzig", 18, 9, 6, 0, 3, 27, 13, 14));
        d.add(criarLinha(5, "Leverkusen", 16, 9, 5, 1, 3, 20, 17, 3));
        d.add(criarLinha(6, "Werder Bremen", 15, 9, 4, 3, 2, 17, 15, 2));
        d.add(criarLinha(7, "Elversberg", 13, 9, 4, 1, 4, 17, 18, -1));
        d.add(criarLinha(8, "VfB Stuttgart", 12, 9, 4, 0, 5, 15, 15, 0));
        d.add(criarLinha(9, "Hoffenheim", 12, 9, 4, 0, 5, 16, 17, -1));
        d.add(criarLinha(10, "Eintracht", 12, 9, 3, 3, 3, 15, 20, -5));
        d.add(criarLinha(11, "Hamburg", 12, 9, 4, 0, 5, 10, 20, -10));
        d.add(criarLinha(12, "Mainz 05", 10, 9, 3, 1, 5, 18, 19, -1));
        d.add(criarLinha(13, "Paderborn 07", 10, 9, 2, 4, 3, 8, 11, -3));
        d.add(criarLinha(14, "Augsburg", 9, 9, 2, 3, 4, 20, 20, 0));
        d.add(criarLinha(15, "Colônia", 9, 9, 2, 3, 4, 10, 14, -4));
        d.add(criarLinha(16, "Union Berlin", 7, 9, 1, 4, 4, 7, 21, -14));
        d.add(criarLinha(17, "Schalke", 6, 9, 1, 3, 5, 5, 11, -6));
        d.add(criarLinha(18, "Mönchengladbach", 4, 9, 1, 1, 7, 12, 25, -13));
        return d;
    }

    private Vector<Vector<Object>> getDadosLigue1() {
        Vector<Vector<Object>> d = new Vector<>();
        d.add(criarLinha(1, "PSG", 23, 10, 7, 2, 1, 22, 10, 12));
        d.add(criarLinha(2, "LOSC", 21, 10, 6, 3, 1, 14, 7, 7));
        d.add(criarLinha(3, "Paris FC", 20, 10, 6, 2, 2, 17, 10, 7));
        d.add(criarLinha(4, "Olympique", 18, 10, 6, 0, 4, 22, 14, 8));
        d.add(criarLinha(5, "Lyon", 17, 10, 5, 2, 3, 15, 9, 6));
        d.add(criarLinha(6, "Nice", 17, 10, 5, 2, 3, 15, 12, 3));
        d.add(criarLinha(7, "Mônaco", 16, 10, 4, 4, 2, 13, 11, 2));
        d.add(criarLinha(8, "Rennes", 16, 10, 5, 1, 4, 21, 21, 0));
        d.add(criarLinha(9, "Stade Brestois", 15, 10, 4, 3, 3, 18, 12, 6));
        d.add(criarLinha(10, "Lorient-Bretagne", 15, 10, 4, 3, 3, 9, 8, 1));
        d.add(criarLinha(11, "Lens", 13, 10, 4, 1, 5, 17, 15, 2));
        d.add(criarLinha(12, "Toulouse", 13, 10, 3, 4, 3, 15, 14, 1));
        d.add(criarLinha(13, "RC Strasbourg", 10, 10, 3, 1, 6, 18, 22, -4));
        d.add(criarLinha(14, "Auxerre", 10, 10, 3, 1, 6, 13, 24, -11));
        d.add(criarLinha(15, "Le Mans Foot...", 8, 10, 1, 5, 4, 12, 19, -7));
        d.add(criarLinha(16, "Angers", 8, 10, 2, 2, 6, 9, 19, -10));
        d.add(criarLinha(17, "Troyes", 8, 10, 2, 2, 6, 7, 17, -10));
        d.add(criarLinha(18, "Le Havre AC", 2, 10, 0, 2, 8, 9, 22, -13));
        return d;
    }

    private Vector<Vector<Object>> getDadosSaudi() {
        Vector<Vector<Object>> d = new Vector<>();
        d.add(criarLinha(1, "Al-Hilal", 30, 11, 10, 0, 1, 32, 9, 23));
        d.add(criarLinha(2, "Al Quadisiya", 28, 12, 9, 1, 2, 32, 12, 20));
        d.add(criarLinha(3, "Al-Nassr", 28, 11, 9, 1, 1, 28, 11, 17));
        d.add(criarLinha(4, "Al-Ittihad", 24, 11, 7, 3, 1, 21, 11, 10));
        d.add(criarLinha(5, "Neom", 22, 12, 7, 1, 4, 18, 10, 8));
        d.add(criarLinha(6, "Al-Ahli Saudi", 21, 11, 7, 0, 4, 28, 19, 9));
        d.add(criarLinha(7, "Al-Ettifaq", 19, 12, 5, 4, 3, 18, 17, 1));
        d.add(criarLinha(8, "Al Draih", 15, 12, 4, 3, 5, 10, 13, -3));
        d.add(criarLinha(9, "Al Khaleej", 15, 12, 3, 6, 3, 14, 19, -5));
        d.add(criarLinha(10, "Al-Shabab", 14, 11, 3, 5, 3, 20, 15, 5));
        d.add(criarLinha(11, "Al Kholood", 12, 11, 3, 3, 5, 17, 26, -9));
        d.add(criarLinha(12, "Al-Riyadh", 12, 11, 3, 3, 5, 11, 25, -14));
        d.add(criarLinha(13, "Al Feiha", 11, 12, 2, 5, 5, 16, 20, -4));
        d.add(criarLinha(14, "Al Hazm", 10, 12, 2, 4, 6, 14, 20, -6));
        d.add(criarLinha(15, "Al Faisaly", 10, 12, 2, 4, 6, 13, 24, -11));
        d.add(criarLinha(16, "Al Fateh", 6, 12, 1, 3, 8, 10, 25, -15));
        d.add(criarLinha(17, "Al Taawon", 5, 12, 0, 5, 7, 6, 20, -14));
        d.add(criarLinha(18, "Abha", 3, 11, 0, 3, 8, 7, 19, -12));
        return d;
    }

    private Vector<Vector<Object>> getDadosMLSLeste() {
        Vector<Vector<Object>> d = new Vector<>();
        d.add(criarLinha(1, "Nashville SC", 77, 34, 23, 8, 3, 71, 25, 46));
        d.add(criarLinha(2, "Inter Miami", 60, 34, 16, 12, 6, 80, 59, 21));
        d.add(criarLinha(3, "New England", 58, 34, 18, 4, 12, 55, 47, 8));
        d.add(criarLinha(4, "Chicago Fire", 55, 34, 16, 7, 11, 64, 54, 10));
        d.add(criarLinha(5, "Charlotte FC", 54, 34, 15, 9, 10, 66, 51, 15));
        d.add(criarLinha(6, "Philadelphia Union", 50, 34, 14, 8, 12, 66, 52, 14));
        d.add(criarLinha(7, "Columbus Crew", 47, 34, 14, 5, 15, 59, 52, 7));
        d.add(criarLinha(8, "Orlando City", 46, 34, 13, 7, 14, 63, 78, -15));
        d.add(criarLinha(9, "Cincinnati", 46, 34, 12, 10, 12, 67, 75, -8));
        d.add(criarLinha(10, "NY Red Bulls", 42, 34, 12, 6, 16, 48, 62, -14));
        d.add(criarLinha(11, "New York City FC", 42, 34, 10, 12, 12, 49, 45, 4));
        d.add(criarLinha(12, "Toronto", 39, 34, 9, 12, 13, 52, 67, -15));
        d.add(criarLinha(13, "Atlanta United", 33, 34, 9, 6, 19, 43, 60, -17));
        d.add(criarLinha(14, "DC United", 33, 34, 7, 12, 15, 39, 61, -22));
        d.add(criarLinha(15, "Montréal", 26, 34, 6, 8, 20, 37, 69, -32));
        return d;
    }

    private Vector<Vector<Object>> getDadosMLSOeste() {
        Vector<Vector<Object>> d = new Vector<>();
        d.add(criarLinha(1, "Whitecaps", 64, 34, 19, 7, 8, 74, 34, 40));
        d.add(criarLinha(2, "FC Dallas", 58, 34, 16, 10, 8, 62, 51, 11));
        d.add(criarLinha(3, "St. Louis City SC", 54, 34, 15, 9, 10, 59, 48, 11));
        d.add(criarLinha(4, "San José", 52, 34, 14, 10, 10, 57, 51, 6));
        d.add(criarLinha(5, "Houston Dynamo", 51, 34, 14, 9, 11, 43, 43, 0));
        d.add(criarLinha(6, "LAFC", 47, 34, 12, 11, 11, 52, 40, 12));
        d.add(criarLinha(7, "Portland Timbers", 46, 34, 13, 7, 14, 62, 61, 1));
        d.add(criarLinha(8, "Seattle Sounders", 46, 34, 12, 10, 12, 44, 46, -2));
        d.add(criarLinha(9, "Real Salt Lake", 45, 34, 13, 6, 15, 53, 52, 1));
        d.add(criarLinha(10, "Minnesota United", 44, 34, 11, 11, 12, 56, 58, -2));
        d.add(criarLinha(11, "LA Galaxy", 44, 34, 11, 11, 12, 50, 57, -7));
        d.add(criarLinha(12, "Colorado Rapids", 43, 34, 12, 7, 15, 47, 50, -3));
        d.add(criarLinha(13, "San Diego FC", 38, 34, 9, 11, 14, 57, 63, -6));
        d.add(criarLinha(14, "Austin", 34, 34, 8, 10, 16, 42, 65, -23));
        d.add(criarLinha(15, "Sporting KC", 26, 34, 7, 5, 22, 40, 81, -41));
        return d;
    }

    private Vector<Vector<Object>> getDadosArgentinaGrupoA() {
        Vector<Vector<Object>> d = new Vector<>();
        d.add(criarLinha(1, "Estudiantes", 31, 16, 9, 4, 3, 19, 7, 12));
        d.add(criarLinha(2, "Boca Juniors", 30, 16, 8, 6, 2, 22, 9, 13));
        d.add(criarLinha(3, "Vélez Sársfield", 28, 16, 7, 7, 2, 18, 12, 6));
        d.add(criarLinha(4, "Talleres", 26, 16, 7, 5, 4, 17, 13, 4));
        d.add(criarLinha(5, "Independiente", 24, 16, 6, 6, 4, 24, 20, 4));
        d.add(criarLinha(6, "Lanús", 24, 16, 6, 6, 4, 18, 15, 3));
        d.add(criarLinha(7, "San Lorenzo", 22, 16, 5, 7, 4, 14, 14, 0));
        d.add(criarLinha(8, "Unión de Santa Fe", 21, 16, 5, 6, 5, 24, 20, 4));
        d.add(criarLinha(9, "Instituto", 21, 16, 6, 3, 7, 17, 17, 0));
        d.add(criarLinha(10, "Defensa y Justicia", 19, 16, 4, 7, 5, 18, 21, -3));
        d.add(criarLinha(11, "Gimnasia Mendoza", 19, 16, 5, 4, 7, 14, 22, -8));
        d.add(criarLinha(12, "Platense", 16, 16, 3, 7, 6, 10, 15, -5));
        d.add(criarLinha(13, "Central Córdoba", 16, 16, 4, 4, 8, 11, 21, -10));
        d.add(criarLinha(14, "Newell's Old Boys", 15, 16, 3, 6, 7, 15, 27, -12));
        d.add(criarLinha(15, "Deportivo Riestra", 11, 16, 1, 8, 7, 5, 12, -7));
        return d;
    }

    private Vector<Vector<Object>> getDadosArgentinaGrupoB() {
        Vector<Vector<Object>> d = new Vector<>();
        d.add(criarLinha(1, "Independiente R.", 34, 16, 10, 4, 2, 29, 15, 14));
        d.add(criarLinha(2, "River Plate", 29, 16, 9, 2, 5, 22, 12, 10));
        d.add(criarLinha(3, "Argentinos Juniors", 29, 16, 8, 5, 3, 17, 13, 4));
        d.add(criarLinha(4, "Rosario", 28, 16, 8, 4, 4, 20, 16, 4));
        d.add(criarLinha(5, "Belgrano", 26, 16, 7, 5, 4, 17, 13, 4));
        d.add(criarLinha(6, "Gimnasia", 26, 16, 8, 2, 6, 19, 19, 0));
        d.add(criarLinha(7, "Huracán", 22, 16, 5, 7, 4, 17, 13, 4));
        d.add(criarLinha(8, "Racing", 21, 16, 5, 6, 5, 17, 15, 2));
        d.add(criarLinha(9, "Barracas Central", 21, 16, 5, 6, 5, 15, 15, 0));
        d.add(criarLinha(10, "Tigre", 20, 16, 4, 8, 4, 18, 15, 3));
        d.add(criarLinha(11, "Sarmiento", 19, 16, 6, 1, 9, 13, 20, -7));
        d.add(criarLinha(12, "Banfield", 18, 16, 5, 3, 8, 17, 19, -2));
        d.add(criarLinha(13, "Tucuman", 14, 16, 3, 5, 8, 15, 20, -5));
        d.add(criarLinha(14, "Aldosivi", 8, 16, 0, 8, 8, 6, 19, -13));
        d.add(criarLinha(15, "Asociación Atlética", 5, 16, 1, 2, 13, 5, 24, -19));
        return d;
    }

    private Vector<Object> criarLinha(int pos, String clube, int pts, int pj, int vit, int e, int der, int gm, int gc, int sg) {
        Vector<Object> linha = new Vector<>();
        linha.add(pos);
        linha.add(clube);
        linha.add(pts);
        linha.add(pj);
        linha.add(vit);
        linha.add(e);
        linha.add(der);
        linha.add(gm);
        linha.add(gc);
        linha.add(sg);
        return linha;
    }
}