import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import telas.*;
import javax.swing.*;
import java.awt.*;
import java.util.Collections;

public class Dashboard extends JFrame {

    private static final Color COR_SIDEBAR = Color.decode("#0B1F1E");
    private static final Color COR_SIDEBAR_ATIVO = Color.decode("#134E4A");
    private static final Color COR_SIDEBAR_TEXTO = Color.decode("#99F6E4");

    private final CardLayout trocaDeTelas = new CardLayout();
    private final JPanel painelConteudo = new JPanel(trocaDeTelas);
    private final ButtonGroup grupoMenu = new ButtonGroup();

    public Dashboard() {
        setTitle("Jerseyhub - Dashboard");
        setSize(1000, 680);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        painelConteudo.add(new PainelLoja(), "Loja");
        painelConteudo.add(new PainelClientes(), "Clientes");
        painelConteudo.add(new PainelCamisas(), "Camisas");
        painelConteudo.add(new PainelFuncionarios(), "Funcionarios");
        painelConteudo.add(new PainelPedidos(), "Pedidos");
        painelConteudo.add(new PainelConsultas(), "Consultas");
        painelConteudo.add(new PainelEstatisticas(), "Estatisticas");

        JPanel painelRaiz = new JPanel(new BorderLayout());
        painelRaiz.add(criarSidebar(), BorderLayout.WEST);
        painelRaiz.add(painelConteudo, BorderLayout.CENTER);
        add(painelRaiz);
    }

    private static void configurarAparencia() {
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", UIUtils.hex(UIUtils.COR_SUCESSO)));
        FlatDarkLaf.setup();

        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("Panel.background", UIUtils.COR_FUNDO);
    }

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
        sidebar.add(criarItemMenu("Loja", "shopping-bag.png"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(criarItemMenu("Clientes", "users.png"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(criarItemMenu("Camisas", "shirt.png"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(criarItemMenu("Funcionarios", "user-cog.png"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(criarItemMenu("Pedidos", "package.png"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(criarItemMenu("Consultas", "search.png"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(criarItemMenu("Estatisticas", "chart-bar.png"));
        sidebar.add(Box.createVerticalGlue());

        for (Component componente : sidebar.getComponents()) {
            if (componente instanceof JComponent) {
                ((JComponent) componente).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
        }

        grupoMenu.getElements().nextElement().setSelected(true);
        return sidebar;
    }

    private JToggleButton criarItemMenu(String nomeTela, String icone) {
        JToggleButton item = new JToggleButton(nomeTela);
        item.setIcon(carregarIcone(icone, 18, 18));
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

    private ImageIcon carregarIcone(String nomeArquivo, int largura, int altura) {
        String caminhoRelativo = "assets/icons/" + nomeArquivo;

        java.net.URL recurso = Dashboard.class.getClassLoader().getResource(caminhoRelativo);
        if (recurso != null) {
            Image imagem = new ImageIcon(recurso).getImage().getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
            return new ImageIcon(imagem);
        }

        java.io.File diretorioAtual = new java.io.File(System.getProperty("user.dir"));
        java.io.File[] candidatos = {
            new java.io.File(diretorioAtual, caminhoRelativo),
            new java.io.File(diretorioAtual, "Jerseyhub/" + caminhoRelativo),
            diretorioAtual.getParentFile() != null
                ? new java.io.File(diretorioAtual.getParentFile(), "Jerseyhub/" + caminhoRelativo)
                : null
        };

        for (java.io.File arquivo : candidatos) {
            if (arquivo != null && arquivo.isFile()) {
                Image imagem = new ImageIcon(arquivo.getAbsolutePath()).getImage()
                    .getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
                return new ImageIcon(imagem);
            }
        }

        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configurarAparencia();
            new Dashboard().setVisible(true);
        });
    }
}