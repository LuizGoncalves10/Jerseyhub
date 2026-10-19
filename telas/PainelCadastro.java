package telas;

import banco.ConexaoBanco;
import telas.componentes.AvisoStatus;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Base das telas de cadastro (Clientes e Camisas).
 *
 * Layout: busca + tabela à esquerda e um formulário fixo à direita.
 * - Clicar numa linha da tabela carrega o registro no formulário para edição (UPDATE).
 * - "Novo" limpa o formulário para cadastrar um registro novo (INSERT).
 * - "Excluir" pede confirmação dentro do próprio formulário (DELETE).
 *
 * Toda a comunicação com o banco continua sendo SQL puro via JDBC; esta classe só organiza a tela.
 * As subclasses dizem quais campos existem e quais comandos SQL rodar.
 */
public abstract class PainelCadastro extends JPanel {

    private static final int LARGURA_FORMULARIO = 350;
    private static final int ATRASO_BUSCA_MS = 250;

    private final String nomeEntidade;
    private final String nomeEntidadePlural;
    private final boolean entidadeFeminina;

    protected final JTable tabela = UIUtils.criarTabela();
    private final JTextField campoBusca;
    private final JLabel rotuloContagem = new JLabel();

    private final JLabel tituloFormulario = new JLabel();
    private final JLabel subtituloFormulario = new JLabel();
    private final AvisoStatus aviso = new AvisoStatus();
    private final JButton botaoSalvar;
    private final JButton botaoExcluir;
    private final CardLayout trocaAcoes = new CardLayout();
    private final JPanel painelAcoes = new JPanel(trocaAcoes);
    private final JLabel textoConfirmacao = new JLabel();

    /** null quando o formulário está cadastrando um registro novo. */
    private Integer idEmEdicao = null;
    /** Nome completo do registro em edição, usado na confirmação e no aviso de exclusão. */
    private String descricaoEmEdicao = "";
    private boolean carregandoSelecao = false;

    protected PainelCadastro(String nomeEntidade, String nomeEntidadePlural, boolean entidadeFeminina) {
        this.nomeEntidade = nomeEntidade;
        this.nomeEntidadePlural = nomeEntidadePlural;
        this.entidadeFeminina = entidadeFeminina;

        setLayout(new BorderLayout());
        setBackground(Tema.FUNDO);

        campoBusca = UIUtils.criarCampoBusca("Buscar " + nomeEntidadePlural.toLowerCase() + "…");
        botaoSalvar = UIUtils.criarBotaoPrimario("Salvar");
        botaoExcluir = UIUtils.criarBotaoPerigoDiscreto("Excluir " + (entidadeFeminina ? "esta " : "este ") + nomeEntidade.toLowerCase());
    }

    /**
     * Monta a tela. Fica fora do construtor porque depende dos campos criados pelas subclasses.
     * Cada subclasse chama este método no fim do próprio construtor.
     */
    protected void montarTela(String titulo, String descricao) {
        add(UIUtils.criarCabecalhoSecao(titulo, descricao, criarBotaoNovo()), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(Tema.ESPACO_GRANDE, 0));
        corpo.setOpaque(false);
        corpo.setBorder(BorderFactory.createEmptyBorder(0, Tema.MARGEM_TELA, Tema.MARGEM_TELA, Tema.MARGEM_TELA));
        corpo.add(criarAreaLista(), BorderLayout.CENTER);
        corpo.add(criarAreaFormulario(), BorderLayout.EAST);
        add(corpo, BorderLayout.CENTER);

        configurarEventos();
        prepararNovoRegistro();
        SwingUtilities.invokeLater(this::atualizarLista);
    }

    // ------------------------------------------------------------------ montagem da tela

    private JButton criarBotaoNovo() {
        JButton botaoNovo = UIUtils.criarBotaoPrimario("+ " + (entidadeFeminina ? "Nova " : "Novo ") + nomeEntidade.toLowerCase());
        botaoNovo.addActionListener(e -> {
            tabela.clearSelection();
            prepararNovoRegistro();
        });
        return botaoNovo;
    }

    private JPanel criarAreaLista() {
        JPanel areaLista = new JPanel(new BorderLayout(0, Tema.ESPACO_MEDIO));
        areaLista.setOpaque(false);

        rotuloContagem.setFont(Tema.fonte(Tema.TAMANHO_ROTULO, Font.PLAIN));
        rotuloContagem.setForeground(Tema.TEXTO_SECUNDARIO);

        JPanel barraBusca = new JPanel(new BorderLayout(Tema.ESPACO_MEDIO, 0));
        barraBusca.setOpaque(false);
        barraBusca.add(campoBusca, BorderLayout.CENTER);
        barraBusca.add(rotuloContagem, BorderLayout.EAST);

        areaLista.add(barraBusca, BorderLayout.NORTH);
        areaLista.add(UIUtils.criarRolagem(tabela), BorderLayout.CENTER);
        return areaLista;
    }

    private JComponent criarAreaFormulario() {
        JPanel formulario = UIUtils.criarPainelSuperficie();
        formulario.setLayout(new BorderLayout(0, Tema.ESPACO_GRANDE));
        formulario.setBorder(BorderFactory.createCompoundBorder(
            formulario.getBorder(),
            BorderFactory.createEmptyBorder(Tema.ESPACO_GRANDE, Tema.ESPACO_GRANDE, Tema.ESPACO_GRANDE, Tema.ESPACO_GRANDE)));

        tituloFormulario.setFont(Tema.fonte(Tema.TAMANHO_TITULO_CARTAO, Font.BOLD));
        tituloFormulario.setForeground(Tema.TEXTO);
        subtituloFormulario.setFont(Tema.fonte(Tema.TAMANHO_ROTULO, Font.PLAIN));
        subtituloFormulario.setForeground(Tema.TEXTO_SECUNDARIO);

        JPanel topoFormulario = new JPanel(new GridLayout(2, 1, 0, 2));
        topoFormulario.setOpaque(false);
        topoFormulario.add(tituloFormulario);
        topoFormulario.add(subtituloFormulario);

        JPanel camposFormulario = new JPanel(new GridBagLayout());
        camposFormulario.setOpaque(false);
        montarCampos(camposFormulario);

        JPanel rodapeFormulario = new JPanel(new BorderLayout(0, Tema.ESPACO_MEDIO));
        rodapeFormulario.setOpaque(false);
        rodapeFormulario.add(aviso, BorderLayout.NORTH);
        rodapeFormulario.add(criarPainelAcoes(), BorderLayout.SOUTH);

        JPanel conteudoRolavel = new JPanel(new BorderLayout());
        conteudoRolavel.setOpaque(false);
        conteudoRolavel.add(camposFormulario, BorderLayout.NORTH);

        JScrollPane rolagemCampos = new JScrollPane(conteudoRolavel);
        rolagemCampos.setBorder(null);
        rolagemCampos.setOpaque(false);
        rolagemCampos.getViewport().setOpaque(false);
        rolagemCampos.getVerticalScrollBar().setUnitIncrement(16);

        formulario.add(topoFormulario, BorderLayout.NORTH);
        formulario.add(rolagemCampos, BorderLayout.CENTER);
        formulario.add(rodapeFormulario, BorderLayout.SOUTH);
        formulario.setPreferredSize(new Dimension(LARGURA_FORMULARIO, 0));
        return formulario;
    }

    /** Duas "cartas": os botões normais e a confirmação de exclusão, que troca de lugar com eles. */
    private JPanel criarPainelAcoes() {
        painelAcoes.setOpaque(false);

        JButton botaoLimpar = UIUtils.criarBotaoSecundario("Limpar");
        botaoLimpar.addActionListener(e -> {
            if (idEmEdicao == null) limparCampos();
            else carregarParaEdicao(idEmEdicao);
            limparErros();
        });
        botaoSalvar.addActionListener(e -> salvar());
        botaoExcluir.addActionListener(e -> pedirConfirmacaoExclusao());

        // Salvar e Limpar dividem a largura; Excluir fica numa linha própria, discreto, só na edição
        JPanel acoesPadrao = new JPanel(new BorderLayout(0, Tema.ESPACO_PEQUENO));
        acoesPadrao.setOpaque(false);
        JPanel botoesPrincipais = new JPanel(new GridLayout(1, 2, Tema.ESPACO_PEQUENO, 0));
        botoesPrincipais.setOpaque(false);
        botoesPrincipais.add(botaoLimpar);
        botoesPrincipais.add(botaoSalvar);
        JPanel linhaExcluir = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaExcluir.setOpaque(false);
        linhaExcluir.add(botaoExcluir);
        acoesPadrao.add(linhaExcluir, BorderLayout.NORTH);
        acoesPadrao.add(botoesPrincipais, BorderLayout.SOUTH);

        JPanel acoesConfirmacao = new JPanel(new BorderLayout(0, Tema.ESPACO_PEQUENO));
        acoesConfirmacao.setOpaque(false);
        textoConfirmacao.setFont(Tema.fonte(Tema.TAMANHO_TEXTO, Font.PLAIN));
        textoConfirmacao.setForeground(Tema.TEXTO);
        JButton botaoCancelarExclusao = UIUtils.criarBotaoSecundario("Cancelar");
        JButton botaoConfirmarExclusao = UIUtils.criarBotaoPerigo("Sim, excluir");
        botaoCancelarExclusao.addActionListener(e -> trocaAcoes.show(painelAcoes, "padrao"));
        botaoConfirmarExclusao.addActionListener(e -> excluir());
        JPanel botoesConfirmacao = new JPanel(new GridLayout(1, 2, Tema.ESPACO_PEQUENO, 0));
        botoesConfirmacao.setOpaque(false);
        botoesConfirmacao.add(botaoCancelarExclusao);
        botoesConfirmacao.add(botaoConfirmarExclusao);
        acoesConfirmacao.add(textoConfirmacao, BorderLayout.NORTH);
        acoesConfirmacao.add(botoesConfirmacao, BorderLayout.SOUTH);

        painelAcoes.add(acoesPadrao, "padrao");
        painelAcoes.add(acoesConfirmacao, "confirmacao");
        return painelAcoes;
    }

    private void configurarEventos() {
        // Ao clicar numa linha, o registro vai para o formulário
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || carregandoSelecao) return;
            int linhaSelecionada = tabela.getSelectedRow();
            if (linhaSelecionada < 0) return;
            Object id = tabela.getModel().getValueAt(tabela.convertRowIndexToModel(linhaSelecionada), 0);
            carregarParaEdicao(((Number) id).intValue());
        });

        // A busca roda um pouco depois da última tecla, para não consultar o banco a cada letra
        Timer temporizadorBusca = new Timer(ATRASO_BUSCA_MS, e -> atualizarLista());
        temporizadorBusca.setRepeats(false);
        campoBusca.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { temporizadorBusca.restart(); }
            public void removeUpdate(DocumentEvent e) { temporizadorBusca.restart(); }
            public void changedUpdate(DocumentEvent e) { temporizadorBusca.restart(); }
        });
    }

    // ------------------------------------------------------------------ fluxo do formulário

    private void prepararNovoRegistro() {
        idEmEdicao = null;
        limparCampos();
        limparErros();
        aviso.ocultar();
        tituloFormulario.setText((entidadeFeminina ? "Nova " : "Novo ") + nomeEntidade.toLowerCase());
        subtituloFormulario.setText("Preencha os dados e clique em Cadastrar.");
        botaoSalvar.setText("Cadastrar");
        botaoExcluir.setVisible(false);
        trocaAcoes.show(painelAcoes, "padrao");
        focarPrimeiroCampo();
    }

    private void carregarParaEdicao(int id) {
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) {
                aviso.mostrarErro("Não foi possível conectar ao banco de dados.");
                return;
            }
            String descricao = carregarRegistro(conexao, id);
            if (descricao == null) {
                aviso.mostrarErro(nomeEntidade + " não encontrad" + (entidadeFeminina ? "a" : "o") + ". A lista foi atualizada.");
                atualizarLista();
                return;
            }
            idEmEdicao = id;
            limparErros();
            aviso.ocultar();
            tituloFormulario.setText(descricao);
            String detalhe = detalheDoRegistro();
            descricaoEmEdicao = descricao + (detalhe == null ? "" : " · " + detalhe);
            subtituloFormulario.setText((detalhe == null ? "" : detalhe + "  ·  ") + "Editando " + nomeEntidade.toLowerCase() + " nº " + id);
            botaoSalvar.setText("Salvar alterações");
            botaoExcluir.setVisible(true);
            trocaAcoes.show(painelAcoes, "padrao");
        } catch (SQLException ex) {
            aviso.mostrarErro("Erro ao carregar: " + ex.getMessage());
        }
    }

    private void salvar() {
        limparErros();
        if (!validarCampos()) {
            aviso.mostrarErro("Corrija os campos marcados em vermelho.");
            return;
        }
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) {
                aviso.mostrarErro("Não foi possível conectar ao banco de dados.");
                return;
            }
            if (idEmEdicao == null) {
                inserir(conexao);
                atualizarLista();
                prepararNovoRegistro();
                aviso.mostrarSucesso(nomeEntidade + " cadastrad" + (entidadeFeminina ? "a" : "o") + " com sucesso.");
            } else {
                int idSalvo = idEmEdicao;
                atualizar(conexao, idSalvo);
                atualizarLista();
                selecionarNaTabela(idSalvo);
                carregarParaEdicao(idSalvo);
                aviso.mostrarSucesso("Alterações salvas.");
            }
        } catch (SQLException ex) {
            aviso.mostrarErro(traduzirErroBanco(ex));
        }
    }

    private void pedirConfirmacaoExclusao() {
        if (idEmEdicao == null) return;
        textoConfirmacao.setText("<html>Excluir <b>" + escaparHtml(descricaoEmEdicao) + "</b>? Essa ação não pode ser desfeita.</html>");
        trocaAcoes.show(painelAcoes, "confirmacao");
    }

    private void excluir() {
        if (idEmEdicao == null) return;
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) {
                aviso.mostrarErro("Não foi possível conectar ao banco de dados.");
                return;
            }
            String descricaoExcluida = descricaoEmEdicao;
            excluirRegistro(conexao, idEmEdicao);
            tabela.clearSelection();
            atualizarLista();
            prepararNovoRegistro();
            aviso.mostrarSucesso(descricaoExcluida + " foi excluíd" + (entidadeFeminina ? "a" : "o") + ".");
        } catch (SQLException ex) {
            trocaAcoes.show(painelAcoes, "padrao");
            aviso.mostrarErro(traduzirErroBanco(ex));
        }
    }

    /** Recarrega a tabela com o filtro da busca, mantendo a linha selecionada quando possível. */
    protected void atualizarLista() {
        String filtro = "%" + campoBusca.getText().trim() + "%";
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) {
                rotuloContagem.setText("Sem conexão com o banco");
                return;
            }
            try (PreparedStatement consulta = prepararConsultaLista(conexao, filtro)) {
                carregandoSelecao = true;
                int totalLinhas = UIUtils.preencherTabela(tabela, consulta);
                ajustarColunas();
                rotuloContagem.setText(totalLinhas + " " + (totalLinhas == 1 ? nomeEntidade.toLowerCase() : nomeEntidadePlural.toLowerCase()));
                if (idEmEdicao != null) selecionarNaTabela(idEmEdicao);
            } finally {
                carregandoSelecao = false;
            }
        } catch (SQLException ex) {
            aviso.mostrarErro("Erro ao carregar a lista: " + ex.getMessage());
        }
    }

    private void selecionarNaTabela(int id) {
        for (int linha = 0; linha < tabela.getRowCount(); linha++) {
            Object valor = tabela.getModel().getValueAt(tabela.convertRowIndexToModel(linha), 0);
            if (((Number) valor).intValue() == id) {
                boolean estavaCarregando = carregandoSelecao;
                carregandoSelecao = true;
                tabela.setRowSelectionInterval(linha, linha);
                tabela.scrollRectToVisible(tabela.getCellRect(linha, 0, true));
                carregandoSelecao = estavaCarregando;
                return;
            }
        }
    }

    private String traduzirErroBanco(SQLException ex) {
        String mensagemTraduzida = traduzirErroEspecifico(ex);
        if (mensagemTraduzida != null) return mensagemTraduzida;
        String mensagem = ex.getMessage() == null ? "" : ex.getMessage();
        if (mensagem.contains("Check constraint")) {
            return "O banco recusou algum valor fora das regras (CHECK). Revise os campos.";
        }
        return "Erro no banco de dados: " + mensagem;
    }

    private static String escaparHtml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ------------------------------------------------------------------ o que cada tela define

    /** Linha extra opcional mostrada abaixo do título durante a edição. */
    protected String detalheDoRegistro() {
        return null;
    }

    /** Adiciona os campos de entrada ao painel (GridBagLayout). */
    protected abstract void montarCampos(JPanel painelCampos);

    /** Coloca o foco no primeiro campo do formulário. */
    protected abstract void focarPrimeiroCampo();

    /** Esvazia todos os campos do formulário. */
    protected abstract void limparCampos();

    /** Remove as mensagens de erro de todos os campos. */
    protected abstract void limparErros();

    /** Confere os campos antes de enviar ao banco; marca os inválidos e devolve false se houver algum. */
    protected abstract boolean validarCampos();

    /** SELECT da listagem, com o filtro da busca já no formato '%texto%'. */
    protected abstract PreparedStatement prepararConsultaLista(Connection conexao, String filtro) throws SQLException;

    /** Ajusta largura e alinhamento das colunas depois que a tabela é recarregada. */
    protected abstract void ajustarColunas();

    /** Busca o registro pelo id, preenche os campos e devolve um título curto para ele (ou null se não existir). */
    protected abstract String carregarRegistro(Connection conexao, int id) throws SQLException;

    /** INSERT com os valores do formulário. */
    protected abstract void inserir(Connection conexao) throws SQLException;

    /** UPDATE do registro com os valores do formulário. */
    protected abstract void atualizar(Connection conexao, int id) throws SQLException;

    /** DELETE do registro. */
    protected abstract void excluirRegistro(Connection conexao, int id) throws SQLException;

    /** Mensagem amigável para erros conhecidos desta tabela (CPF repetido, chave estrangeira...), ou null. */
    protected abstract String traduzirErroEspecifico(SQLException ex);
}
