package telas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PainelLoja extends JPanel {

    public PainelLoja() {
        UIUtils.configurarPainelBase(this);

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

        painelOpcoes.add(cartaoClubes);
        painelOpcoes.add(cartaoSelecoes);

        painelConteudo.add(painelCabecalho, BorderLayout.NORTH);
        painelConteudo.add(painelOpcoes, BorderLayout.CENTER);

        add(UIUtils.criarBarraTopo("Loja Virtual"), BorderLayout.NORTH);
        add(painelConteudo, BorderLayout.CENTER);
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

            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(cartao, "Em breve: Catálogo de " + titulo);
            }
        });

        return cartao;
    }
}