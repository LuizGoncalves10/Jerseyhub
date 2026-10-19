package telas.componentes;

import telas.Tema;

import javax.swing.*;
import java.awt.*;

/**
 * Faixa de aviso que aparece dentro da própria tela depois de salvar ou excluir,
 * no lugar das janelinhas "Sucesso!" do JOptionPane. Some sozinha depois de alguns segundos.
 */
public class AvisoStatus extends JLabel {

    private static final int TEMPO_VISIVEL_MS = 5000;
    private final Timer temporizadorOcultar;

    public AvisoStatus() {
        setOpaque(true);
        setVisible(false);
        setFont(Tema.fonte(Tema.TAMANHO_TEXTO, Font.PLAIN));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        temporizadorOcultar = new Timer(TEMPO_VISIVEL_MS, e -> setVisible(false));
        temporizadorOcultar.setRepeats(false);
    }

    public void mostrarSucesso(String mensagem) {
        mostrar(mensagem, Tema.SUCESSO_FUNDO, Tema.SUCESSO_TEXTO);
    }

    public void mostrarErro(String mensagem) {
        mostrar(mensagem, Tema.PERIGO_FUNDO, Tema.PERIGO_TEXTO);
    }

    public void ocultar() {
        temporizadorOcultar.stop();
        setVisible(false);
    }

    private void mostrar(String mensagem, Color fundo, Color texto) {
        // <html> faz a mensagem quebrar linha dentro da largura do formulário
        String mensagemSegura = mensagem.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        setText("<html>" + mensagemSegura + "</html>");
        setBackground(fundo);
        setForeground(texto);
        setVisible(true);
        temporizadorOcultar.restart();
    }
}
