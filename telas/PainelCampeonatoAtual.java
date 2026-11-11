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
            dados = getDadosBrasileirao();
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

    private Vector<Vector<Object>> getDadosBrasileirao() {
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