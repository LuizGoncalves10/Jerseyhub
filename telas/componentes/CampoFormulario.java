package telas.componentes;

import telas.Tema;

import javax.swing.*;
import java.awt.*;

/**
 * Um campo de formulário completo: rótulo em cima, componente de entrada no meio
 * e uma linha de erro embaixo, que só aparece quando a validação falha.
 */
public class CampoFormulario extends JPanel {

    private final JComponent entrada;
    private final JLabel rotuloErro;

    public CampoFormulario(String rotulo, JComponent entrada) {
        this.entrada = entrada;
        setOpaque(false);
        setLayout(new BorderLayout(0, 6));

        JLabel rotuloCampo = new JLabel(rotulo);
        rotuloCampo.setFont(Tema.fonte(Tema.TAMANHO_ROTULO, Font.BOLD));
        rotuloCampo.setForeground(Tema.TEXTO_SECUNDARIO);
        rotuloCampo.setLabelFor(entrada);

        rotuloErro = new JLabel(" ");
        rotuloErro.setFont(Tema.fonte(Tema.TAMANHO_ROTULO, Font.PLAIN));
        rotuloErro.setForeground(Tema.PERIGO);

        add(rotuloCampo, BorderLayout.NORTH);
        add(entrada, BorderLayout.CENTER);
        add(rotuloErro, BorderLayout.SOUTH);
    }

    public JComponent getEntrada() {
        return entrada;
    }

    /** Mostra a mensagem embaixo do campo e pinta a borda de vermelho (FlatLaf "outline"). */
    public void mostrarErro(String mensagem) {
        rotuloErro.setText(mensagem);
        entrada.putClientProperty("JComponent.outline", "error");
    }

    public void limparErro() {
        rotuloErro.setText(" ");
        entrada.putClientProperty("JComponent.outline", null);
    }
}
