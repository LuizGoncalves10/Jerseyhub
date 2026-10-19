package telas;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

public class UIUtils {
    public static final Color COR_PRIMARIA = Color.decode("#14532D");
    public static final Color COR_SUCESSO = Color.decode("#2EA05F");
    public static final Color COR_EDICAO = Color.decode("#2F6FD0");
    public static final Color COR_PERIGO = Color.decode("#C93C37");
    public static final Color COR_NEUTRA = Color.decode("#484F58");
    public static final Color COR_FUNDO = Color.decode("#16191D");
    public static final Color COR_CARTAO = Color.decode("#1F2328");
    public static final Color COR_BORDA = Color.decode("#2D333B");
    public static final Color COR_TEXTO = Color.decode("#E6EDF3");
    public static final Color COR_TEXTO_SUAVE = Color.decode("#8B949E");
    public static final Color COR_LINHA_ALTERNADA = Color.decode("#23282E");
    public static final Color COR_SELECAO = Color.decode("#1C4A30");
    public static final Color COR_DESTAQUE_FUNDO = Color.decode("#183326");

    public static void configurarPainelBase(JPanel painel) {
        painel.setLayout(new BorderLayout(0, 12));
        painel.setBackground(COR_FUNDO);
        painel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }

    public static JLabel criarTituloSecao(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(COR_TEXTO);
        return titulo;
    }

    public static JPanel criarBarraTopo(String titulo, JButton... botoes) {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(criarTituloSecao(titulo), BorderLayout.WEST);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        painelBotoes.setOpaque(false);
        for (JButton botao : botoes) painelBotoes.add(botao);
        barra.add(painelBotoes, BorderLayout.EAST);
        return barra;
    }

    public static JButton criarBotao(String texto, Color cor) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 13));
        botao.setMargin(new Insets(7, 16, 7, 16));
        botao.setFocusable(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.putClientProperty("FlatLaf.style",
            "background: " + hex(cor) + "; foreground: #FFFFFF; borderWidth: 0; focusWidth: 0;"
            + " hoverBackground: " + hex(cor.brighter()) + "; pressedBackground: " + hex(cor.darker()));
        return botao;
    }

    public static String hex(Color cor) {
        return String.format("#%02X%02X%02X", cor.getRed(), cor.getGreen(), cor.getBlue());
    }

    public static JScrollPane criarRolagem(JComponent componente) {
        JScrollPane rolagem = new JScrollPane(componente);
        rolagem.setBorder(BorderFactory.createLineBorder(COR_BORDA));
        rolagem.getViewport().setBackground(COR_CARTAO);
        return rolagem;
    }

    public static JTable criarTabela() {
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

    public static DefaultTableModel criarModeloSomenteLeitura(Vector<Vector<Object>> dados, Vector<String> colunas) {
        return new DefaultTableModel(dados, colunas) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
    }
}