import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import telas.*;
import javax.swing.*;
import java.awt.*;
import java.util.Collections;

public class Dashboard extends JFrame {

    public Dashboard() {
        setTitle("Jerseyhub - Dashboard");
        setSize(1000, 680);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane painelAbas = new JTabbedPane();
        
        painelAbas.addTab("Clientes", new PainelClientes());
        painelAbas.addTab("Camisas", new PainelCamisas());
        painelAbas.addTab("Consultas", new PainelConsultas());
        painelAbas.addTab("Estatisticas", new PainelEstatisticas());

        JPanel painelRaiz = new JPanel(new BorderLayout());
        painelRaiz.add(criarCabecalho(), BorderLayout.NORTH);
        painelRaiz.add(painelAbas, BorderLayout.CENTER);
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
        UIManager.put("TabbedPane.tabHeight", 40);
        UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("TabbedPane.background", UIUtils.COR_FUNDO);
        UIManager.put("Panel.background", UIUtils.COR_FUNDO);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(UIUtils.COR_PRIMARIA);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configurarAparencia();
            new Dashboard().setVisible(true);
        });
    }
}