package telas;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class PainelLojaClubes extends JPanel {

    private final CardLayout layoutCards = new CardLayout();
    private final JPanel painelCards = new JPanel(layoutCards);
    private JTable tabelaCamisas;
    private JLabel lblTituloTabela;
    private CardLayout layoutPai;
    private JPanel painelPai;

    public PainelLojaClubes(CardLayout layoutPai, JPanel painelPai) {
        this.layoutPai = layoutPai;
        this.painelPai = painelPai;

        setOpaque(false);
        setLayout(new BorderLayout());

        painelCards.setOpaque(false);
        painelCards.add(criarTelaClubes(), "GRID_CLUBES");
        painelCards.add(criarTelaCatalogo(), "CATALOGO_CLUBES");

        add(painelCards, BorderLayout.CENTER);
    }

    private JPanel criarTelaClubes() {
        JPanel painel = new JPanel(new BorderLayout(0, 30));
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        
        JButton btnVoltar = UIUtils.criarBotao("Voltar ao Menu", Color.decode("#374151"));
        btnVoltar.addActionListener(e -> layoutPai.show(painelPai, "MENU"));
        
        JLabel lblTitulo = new JLabel("Escolha o Clube");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(UIUtils.COR_TEXTO);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        topo.add(btnVoltar, BorderLayout.WEST);
        topo.add(lblTitulo, BorderLayout.CENTER);
        topo.add(Box.createRigidArea(new Dimension(150, 0)), BorderLayout.EAST);

        JPanel gridClubes = new JPanel(new GridLayout(0, 5, 20, 20));
        gridClubes.setOpaque(false);

        JPanel wrapperGrid = new JPanel(new BorderLayout());
        wrapperGrid.setOpaque(false);
        wrapperGrid.add(gridClubes, BorderLayout.NORTH);

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

        JButton btnVoltar = UIUtils.criarBotao("Voltar aos Clubes", Color.decode("#374151"));
        btnVoltar.addActionListener(e -> layoutCards.show(painelCards, "GRID_CLUBES"));

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
}