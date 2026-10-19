package telas.componentes;

import telas.Tema;

import javax.swing.*;
import java.awt.*;

/**
 * Grupo de botões lado a lado em que só um fica marcado (ex.: P / M / G / GG).
 * Usado nos campos cujo valor é limitado por um CHECK no banco, para o usuário
 * não conseguir digitar algo que o banco recusaria.
 */
public class SeletorOpcoes extends JPanel {

    private final ButtonGroup grupo = new ButtonGroup();
    private final JToggleButton[] botoes;

    public SeletorOpcoes(String... opcoes) {
        setOpaque(false);
        setLayout(new GridLayout(1, opcoes.length, 4, 0));
        botoes = new JToggleButton[opcoes.length];
        for (int i = 0; i < opcoes.length; i++) {
            JToggleButton botao = new JToggleButton(opcoes[i]);
            botao.setFont(Tema.fonte(Tema.TAMANHO_TEXTO, Font.BOLD));
            botao.setFocusable(true);
            botao.setMargin(new Insets(9, 8, 9, 8));
            botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            botao.putClientProperty("FlatLaf.style",
                "arc: " + Tema.ARREDONDAMENTO + "; borderWidth: 1; focusWidth: 1; innerFocusWidth: 0;"
                + " background: " + Tema.hex(Tema.CAMPO_FUNDO) + "; foreground: " + Tema.hex(Tema.TEXTO_SECUNDARIO) + ";"
                + " selectedBackground: " + Tema.hex(Tema.PRIMARIA) + "; selectedForeground: " + Tema.hex(Tema.PRIMARIA_TEXTO) + ";"
                + " hoverBackground: " + Tema.hex(Tema.CAMPO_FUNDO_HOVER) + ";");
            grupo.add(botao);
            add(botao);
            botoes[i] = botao;
        }
    }

    /** Troca a cor de uma opção quando marcada (usado para pintar cada tamanho com a cor da sua fitinha). */
    public void colorirOpcao(String opcao, Color corMarcada) {
        for (JToggleButton botao : botoes) {
            if (botao.getText().equals(opcao)) {
                String estiloAtual = (String) botao.getClientProperty("FlatLaf.style");
                botao.putClientProperty("FlatLaf.style", estiloAtual + " selectedBackground: " + Tema.hex(corMarcada) + ";");
            }
        }
    }

    /** Devolve o texto da opção marcada, ou null se nenhuma estiver marcada. */
    public String getSelecionado() {
        for (JToggleButton botao : botoes) {
            if (botao.isSelected()) return botao.getText();
        }
        return null;
    }

    public void setSelecionado(String valor) {
        grupo.clearSelection();
        for (JToggleButton botao : botoes) {
            if (botao.getText().equals(valor)) botao.setSelected(true);
        }
    }

    public void limpar() {
        grupo.clearSelection();
    }
}
