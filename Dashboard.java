import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import telas.*;
import javax.swing.*;
import java.awt.*;
import java.util.Collections;

public class Dashboard extends JFrame {

    //cores da barra lateral
    private static final Color COR_SIDEBAR = Color.decode("#0F1E16");
    private static final Color COR_SIDEBAR_ATIVO = Color.decode("#1C4A30");
    private static final Color COR_SIDEBAR_TEXTO = Color.decode("#A7D7B8");

    private final CardLayout trocaDeTelas = new CardLayout();
    private final JPanel painelConteudo = new JPanel(trocaDeTelas);
    private final ButtonGroup grupoMenu = new ButtonGroup();

    public Dashboard() {
        setTitle("Jerseyhub - Dashboard");
        setSize(1000, 680);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        //cada tela fica em um "cartao"; o menu lateral escolhe qual cartao aparece
        painelConteudo.add(new PainelClientes(), "Clientes");
        painelConteudo.add(new PainelCamisas(), "Camisas");
        painelConteudo.add(new PainelConsultas(), "Consultas");
        painelConteudo.add(new PainelEstatisticas(), "Estatisticas");

        JPanel painelRaiz = new JPanel(new BorderLayout());
        painelRaiz.add(criarSidebar(), BorderLayout.WEST);
        painelRaiz.add(painelConteudo, BorderLayout.CENTER);
        add(painelRaiz);
    }

    private static void configurarAparencia() {
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#2EA05F"));
        FlatDarkLaf.setup();

        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("Panel.background", UIUtils.COR_FUNDO);
    }

    //barra lateral
    private JPanel criarSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 14, 24, 14));

        JLabel titulo = new JLabel("<html>Jersey<span style='color:" + UIUtils.hex(UIUtils.COR_SUCESSO) + "'>hub</span></html>");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(Color.WHITE);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel subtitulo = new JLabel("Loja de camisas");
        subtitulo.setForeground(COR_SIDEBAR_TEXTO);
        subtitulo.setBorder(BorderFactory.createEmptyBorder(0, 10, 28, 20));

        sidebar.add(titulo);
        sidebar.add(subtitulo);
        sidebar.add(criarItemMenu("Clientes", ""));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(criarItemMenu("Camisas", ""));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(criarItemMenu("Consultas", ""));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(criarItemMenu("Estatisticas", ""));
        sidebar.add(Box.createVerticalGlue());

        for (Component componente : sidebar.getComponents()) {
            ((JComponent) componente).setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        //comeca mostrando a primeira tela
        grupoMenu.getElements().nextElement().setSelected(true);
        return sidebar;
    }

    private JToggleButton criarItemMenu(String nomeTela, String icone) {
        JToggleButton item = new JToggleButton(nomeTela);
        item.setIcon(new IconeFonte(icone));
        item.setIconTextGap(12);
        item.setHorizontalAlignment(SwingConstants.LEFT);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        item.setMargin(new Insets(9, 12, 9, 12));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        item.setFocusable(false);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.putClientProperty("FlatLaf.style",
            "background: " + UIUtils.hex(COR_SIDEBAR) + "; foreground: " + UIUtils.hex(COR_SIDEBAR_TEXTO) + ";"
            + " selectedBackground: " + UIUtils.hex(COR_SIDEBAR_ATIVO) + "; selectedForeground: #FFFFFF;"
            + " hoverBackground: " + UIUtils.hex(COR_SIDEBAR_ATIVO) + "; borderWidth: 0; focusWidth: 0; arc: 10");

        item.addActionListener(e -> trocaDeTelas.show(painelConteudo, nomeTela));
        item.addItemListener(e -> item.setFont(item.getFont().deriveFont(item.isSelected() ? Font.BOLD : Font.PLAIN)));
        grupoMenu.add(item);
        return item;
    }

    //desenha um icone usando a fonte de simbolos do Windows (Segoe MDL2 Assets)
    private static class IconeFonte implements Icon {
        private final String simbolo;

        IconeFonte(String simbolo) {
            this.simbolo = simbolo;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(new Font("Segoe MDL2 Assets", Font.PLAIN, 16));
            g2.setColor(c.getForeground());
            g2.drawString(simbolo, x, y + 15);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 18; }

        @Override
        public int getIconHeight() { return 18; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configurarAparencia();
            new Dashboard().setVisible(true);
        });
    }
}
