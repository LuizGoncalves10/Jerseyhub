import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import telas.*;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;

/**
 * Janela principal do Jerseyhub.
 *
 * À esquerda fica a barra de navegação (faixa preta) com as quatro áreas do sistema;
 * à direita, a área de trabalho mostra a tela escolhida (CardLayout).
 */
public class Dashboard extends JFrame {

    private static final int LARGURA_LATERAL = 220;

    private final CardLayout trocaTelas = new CardLayout();
    private final JPanel areaTrabalho = new JPanel(trocaTelas);
    private final ButtonGroup grupoNavegacao = new ButtonGroup();
    private final JPanel listaNavegacao = new JPanel();

    public Dashboard() {
        setTitle("Jerseyhub");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1180, 700));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        areaTrabalho.setBackground(Tema.FUNDO);

        listaNavegacao.setLayout(new BoxLayout(listaNavegacao, BoxLayout.Y_AXIS));
        listaNavegacao.setOpaque(false);

        adicionarTela("Camisas", "Catálogo e estoque", new PainelCamisas());
        adicionarTela("Clientes", "Cadastro de clientes", new PainelClientes());
        adicionarTela("Consultas", "Relatórios em SQL", new PainelConsultas());
        adicionarTela("Estatísticas", "Preços e estoque", new PainelEstatisticas());

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(criarBarraLateral(), BorderLayout.WEST);
        raiz.add(areaTrabalho, BorderLayout.CENTER);
        setContentPane(raiz);

        // Começa na primeira tela
        ((AbstractButton) listaNavegacao.getComponent(0)).doClick();
    }

    private JPanel criarBarraLateral() {
        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setBackground(Tema.LATERAL_FUNDO);
        lateral.setPreferredSize(new Dimension(LARGURA_LATERAL, 0));
        lateral.setBorder(BorderFactory.createEmptyBorder(24, 12, 20, 12));

        JLabel marca = new JLabel("JERSEYHUB");
        marca.setFont(Tema.fonteTitulo(28f));
        marca.setForeground(Tema.LATERAL_TEXTO);
        marca.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        JLabel subtitulo = new JLabel("Loja de camisas");
        subtitulo.setFont(Tema.fonte(Tema.TAMANHO_ROTULO, Font.PLAIN));
        subtitulo.setForeground(Tema.LATERAL_TEXTO_SUAVE);
        subtitulo.setBorder(BorderFactory.createEmptyBorder(0, 12, 28, 0));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(marca, BorderLayout.NORTH);
        topo.add(subtitulo, BorderLayout.SOUTH);

        JLabel rodape = new JLabel("<html>Banco: MySQL · jerseyhub<br>Projeto de Banco de Dados</html>");
        rodape.setFont(Tema.fonte(11.5f, Font.PLAIN));
        rodape.setForeground(Tema.LATERAL_TEXTO_SUAVE);
        rodape.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        lateral.add(topo, BorderLayout.NORTH);
        lateral.add(listaNavegacao, BorderLayout.CENTER);
        lateral.add(rodape, BorderLayout.SOUTH);
        return lateral;
    }

    private void adicionarTela(String nome, String descricao, JComponent tela) {
        areaTrabalho.add(tela, nome);

        ItemNavegacao item = new ItemNavegacao(nome, descricao);
        item.addActionListener(e -> trocaTelas.show(areaTrabalho, nome));
        grupoNavegacao.add(item);
        listaNavegacao.add(item);
        listaNavegacao.add(Box.createVerticalStrut(4));
    }

    /**
     * Item da barra lateral: nome da tela e uma linha de descrição.
     * O item ativo ganha fundo mais claro e a faixa amarela à esquerda.
     */
    private static class ItemNavegacao extends JToggleButton {
        private static final int ALTURA = 58;
        private final String descricao;
        private boolean mouseEmCima = false;

        ItemNavegacao(String nome, String descricao) {
            super(nome);
            this.descricao = descricao;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, ALTURA));
            setPreferredSize(new Dimension(LARGURA_LATERAL - 24, ALTURA));
            getAccessibleContext().setAccessibleDescription(descricao);
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    mouseEmCima = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    mouseEmCima = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean ativo = isSelected();
            if (ativo || mouseEmCima) {
                g2.setColor(ativo ? Tema.LATERAL_ITEM_ATIVO : Tema.LATERAL_ITEM_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Tema.ARREDONDAMENTO, Tema.ARREDONDAMENTO);
            }
            if (ativo) {
                g2.setColor(Tema.DESTAQUE);
                g2.fillRoundRect(0, 12, 4, getHeight() - 24, 4, 4);
            }
            if (isFocusOwner()) {
                g2.setColor(Tema.DESTAQUE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Tema.ARREDONDAMENTO, Tema.ARREDONDAMENTO);
            }

            g2.setFont(Tema.fonte(15f, Font.BOLD));
            g2.setColor(ativo ? Tema.LATERAL_TEXTO : new Color(0xD5D9E0));
            g2.drawString(getText(), 16, 25);

            g2.setFont(Tema.fonte(Tema.TAMANHO_ROTULO, Font.PLAIN));
            g2.setColor(Tema.LATERAL_TEXTO_SUAVE);
            g2.drawString(descricao, 16, 44);
            g2.dispose();
        }
    }

    private static void configurarAparencia() {
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", Tema.hex(Tema.PRIMARIA)));
        FlatLightLaf.setup();

        UIManager.put("defaultFont", Tema.fonte(Tema.TAMANHO_TEXTO, Font.PLAIN));
        UIManager.put("Panel.background", Tema.FUNDO);
        UIManager.put("Component.arc", Tema.ARREDONDAMENTO);
        UIManager.put("Button.arc", Tema.ARREDONDAMENTO);
        UIManager.put("TextComponent.arc", Tema.ARREDONDAMENTO);
        UIManager.put("Component.focusColor", Tema.DESTAQUE);
        UIManager.put("Component.borderColor", Tema.BORDA_FORTE);
        UIManager.put("Component.focusedBorderColor", Tema.TEXTO);
        UIManager.put("Component.error.borderColor", Tema.PERIGO);
        UIManager.put("Component.error.focusedBorderColor", Tema.PERIGO);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("Table.selectionBackground", Tema.SELECAO_LINHA);
        UIManager.put("Table.selectionForeground", Tema.TEXTO);
        UIManager.put("TextComponent.selectionBackground", Tema.SELECAO_LINHA);
        UIManager.put("TextComponent.selectionForeground", Tema.TEXTO);
        UIManager.put("List.selectionBackground", Tema.SELECAO_LINHA);
        UIManager.put("List.selectionForeground", Tema.TEXTO);
        UIManager.put("ComboBox.selectionBackground", Tema.SELECAO_LINHA);
        UIManager.put("ComboBox.selectionForeground", Tema.TEXTO);
        UIManager.put("ToolTip.background", Tema.LATERAL_FUNDO);
        UIManager.put("ToolTip.foreground", Tema.LATERAL_TEXTO);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configurarAparencia();
            new Dashboard().setVisible(true);
        });
    }
}
