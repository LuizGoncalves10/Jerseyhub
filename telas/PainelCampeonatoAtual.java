package telas;

import javax.swing.*;
import java.awt.*;

public class PainelCampeonatoAtual extends JPanel {

    private CardLayout layoutPai;
    private JPanel painelPai;

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

        JLabel lblTitulo = new JLabel("Campeonato Atual");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(UIUtils.COR_TEXTO);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        topo.add(btnVoltar, BorderLayout.WEST);
        topo.add(lblTitulo, BorderLayout.CENTER);
        topo.add(Box.createRigidArea(new Dimension(150, 0)), BorderLayout.EAST);

        JPanel painelConteudoVazio = new JPanel();
        painelConteudoVazio.setOpaque(false);

        add(topo, BorderLayout.NORTH);
        add(painelConteudoVazio, BorderLayout.CENTER);
    }
}