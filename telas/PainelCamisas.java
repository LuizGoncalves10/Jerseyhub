package telas;

import banco.ConexaoBanco;
import telas.componentes.CampoFormulario;
import telas.componentes.SeletorOpcoes;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Year;

/**
 * Catálogo de camisas: lista com busca por modelo ou equipe e formulário fixo ao lado
 * para inserir, editar e excluir (tabela Camisa).
 */
public class PainelCamisas extends PainelCadastro {

    private static final String SQL_LISTA_CAMISAS =
        "SELECT c.id_camisa AS `Nº`, CONCAT(COALESCE(e.nome, 'Sem equipe'), ' · ', c.modelo) AS Camisa, "
        + "c.versao AS `Versão`, c.tamanho AS Tam, c.ano AS Ano, c.preco AS `Preço (R$)`, c.quantidade_estoque AS Estoque "
        + "FROM Camisa c LEFT JOIN Equipe e ON c.fk_id_equipe = e.id_equipe "
        + "WHERE c.modelo LIKE ? OR e.nome LIKE ? ORDER BY c.id_camisa";
    private static final String SQL_LISTA_EQUIPES =
        "SELECT id_equipe, nome FROM Equipe ORDER BY nome";
    private static final String SQL_BUSCA_CAMISA =
        "SELECT c.modelo, c.versao, c.tamanho, c.preco, c.ano, c.quantidade_estoque, c.fk_id_equipe, e.nome AS nome_equipe "
        + "FROM Camisa c LEFT JOIN Equipe e ON c.fk_id_equipe = e.id_equipe WHERE c.id_camisa = ?";
    private static final String SQL_INSERE_CAMISA =
        "INSERT INTO Camisa (modelo, versao, tamanho, preco, ano, quantidade_estoque, fk_id_equipe) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_ATUALIZA_CAMISA =
        "UPDATE Camisa SET modelo = ?, versao = ?, tamanho = ?, preco = ?, ano = ?, quantidade_estoque = ?, fk_id_equipe = ? "
        + "WHERE id_camisa = ?";
    private static final String SQL_EXCLUI_CAMISA =
        "DELETE FROM Camisa WHERE id_camisa = ?";

    /** Item da lista de equipes; id null representa "Sem equipe" (fk_id_equipe = NULL). */
    private static class OpcaoEquipe {
        private final Integer idEquipe;
        private final String nome;

        OpcaoEquipe(Integer idEquipe, String nome) {
            this.idEquipe = idEquipe;
            this.nome = nome;
        }

        Integer idEquipe() {
            return idEquipe;
        }

        @Override
        public String toString() {
            return nome;
        }
    }

    private static final OpcaoEquipe SEM_EQUIPE = new OpcaoEquipe(null, "Sem equipe");

    /** Modelo e ano da camisa em edição, mostrados abaixo do nome da equipe no formulário. */
    private String detalheAtual = null;

    private final JComboBox<OpcaoEquipe> comboEquipe = new JComboBox<>();
    private final JTextField campoModelo = UIUtils.criarCampoTexto("Ex.: Principal, Reserva, Retrô");
    private final SeletorOpcoes seletorVersao = new SeletorOpcoes("Torcedor", "Jogador");
    private final SeletorOpcoes seletorTamanho = new SeletorOpcoes("P", "M", "G", "GG");
    private final JTextField campoPreco = UIUtils.criarCampoTexto("0,00");
    private final JTextField campoAno = UIUtils.criarCampoTexto(String.valueOf(Year.now().getValue()));
    private final JTextField campoEstoque = UIUtils.criarCampoTexto("0");

    private final CampoFormulario grupoEquipe = new CampoFormulario("Equipe", comboEquipe);
    private final CampoFormulario grupoModelo = new CampoFormulario("Modelo", campoModelo);
    private final CampoFormulario grupoVersao = new CampoFormulario("Versão", seletorVersao);
    private final CampoFormulario grupoTamanho = new CampoFormulario("Tamanho", seletorTamanho);
    private final CampoFormulario grupoPreco = new CampoFormulario("Preço (R$)", campoPreco);
    private final CampoFormulario grupoAno = new CampoFormulario("Ano", campoAno);
    private final CampoFormulario grupoEstoque = new CampoFormulario("Estoque (peças)", campoEstoque);

    public PainelCamisas() {
        super("Camisa", "Camisas", true);
        UIUtils.configurarCombo(comboEquipe);
        UIUtils.prefixarCampo(campoPreco, "R$");
        for (String tamanho : new String[]{"P", "M", "G", "GG"}) {
            seletorTamanho.colorirOpcao(tamanho, Tema.corDoTamanho(tamanho));
        }
        carregarEquipes();
        montarTela("Camisas", "Catálogo e estoque. Clique em uma camisa para editar ou excluir.");
    }

    /** Preenche o combo de equipes direto da tabela Equipe, em vez de pedir o ID digitado. */
    private void carregarEquipes() {
        comboEquipe.removeAllItems();
        comboEquipe.addItem(SEM_EQUIPE);
        try (Connection conexao = ConexaoBanco.conectar()) {
            if (conexao == null) return;
            try (PreparedStatement consulta = conexao.prepareStatement(SQL_LISTA_EQUIPES);
                 ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    comboEquipe.addItem(new OpcaoEquipe(resultado.getInt("id_equipe"), resultado.getString("nome")));
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao carregar equipes: " + ex.getMessage());
        }
    }

    @Override
    protected void montarCampos(JPanel painelCampos) {
        UIUtils.adicionarLinhaFormulario(painelCampos, 0, grupoEquipe);
        UIUtils.adicionarLinhaFormulario(painelCampos, 1, grupoModelo);
        UIUtils.adicionarLinhaFormulario(painelCampos, 2, grupoVersao);
        UIUtils.adicionarLinhaFormulario(painelCampos, 3, grupoTamanho);
        UIUtils.adicionarLinhaFormulario(painelCampos, 4, grupoPreco, grupoAno, 1, 1);
        UIUtils.adicionarLinhaFormulario(painelCampos, 5, grupoEstoque);
    }

    @Override
    protected void focarPrimeiroCampo() {
        SwingUtilities.invokeLater(comboEquipe::requestFocusInWindow);
    }

    @Override
    protected void limparCampos() {
        comboEquipe.setSelectedItem(SEM_EQUIPE);
        campoModelo.setText("");
        seletorVersao.limpar();
        seletorTamanho.limpar();
        campoPreco.setText("");
        campoAno.setText(String.valueOf(Year.now().getValue()));
        campoEstoque.setText("");
    }

    @Override
    protected void limparErros() {
        for (CampoFormulario grupo : new CampoFormulario[]{grupoEquipe, grupoModelo, grupoVersao, grupoTamanho, grupoPreco, grupoAno, grupoEstoque}) {
            grupo.limparErro();
        }
    }

    @Override
    protected boolean validarCampos() {
        boolean valido = true;
        if (campoModelo.getText().isBlank()) {
            grupoModelo.mostrarErro("Informe o modelo.");
            valido = false;
        }
        if (seletorVersao.getSelecionado() == null) {
            grupoVersao.mostrarErro("Escolha a versão.");
            valido = false;
        }
        if (seletorTamanho.getSelecionado() == null) {
            grupoTamanho.mostrarErro("Escolha o tamanho.");
            valido = false;
        }
        BigDecimal preco = UIUtils.lerDecimal(campoPreco.getText());
        if (preco == null || preco.signum() <= 0) {
            grupoPreco.mostrarErro("Preço maior que zero.");
            valido = false;
        }
        Integer ano = UIUtils.lerInteiro(campoAno.getText());
        if (ano == null || ano < 1900 || ano > 2100) {
            grupoAno.mostrarErro("Entre 1900 e 2100.");
            valido = false;
        }
        Integer estoque = UIUtils.lerInteiro(campoEstoque.getText());
        if (estoque == null || estoque < 0) {
            grupoEstoque.mostrarErro("Use 0 ou mais peças.");
            valido = false;
        }
        return valido;
    }

    @Override
    protected PreparedStatement prepararConsultaLista(Connection conexao, String filtro) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(SQL_LISTA_CAMISAS);
        consulta.setString(1, filtro);
        consulta.setString(2, filtro);
        return consulta;
    }

    @Override
    protected void ajustarColunas() {
        UIUtils.configurarColuna(tabela, 0, 56, SwingConstants.RIGHT);
        UIUtils.configurarColuna(tabela, 2, 90, SwingConstants.LEFT);
        UIUtils.configurarColuna(tabela, 3, 56, SwingConstants.CENTER);
        UIUtils.configurarColuna(tabela, 4, 64, SwingConstants.RIGHT);
        UIUtils.configurarColuna(tabela, 5, 100, SwingConstants.RIGHT);
        UIUtils.configurarColuna(tabela, 6, 84, SwingConstants.RIGHT);
    }

    @Override
    protected String carregarRegistro(Connection conexao, int id) throws SQLException {
        try (PreparedStatement consulta = conexao.prepareStatement(SQL_BUSCA_CAMISA)) {
            consulta.setInt(1, id);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) return null;

                int idEquipe = resultado.getInt("fk_id_equipe");
                boolean semEquipe = resultado.wasNull();
                selecionarEquipe(semEquipe ? null : idEquipe);

                campoModelo.setText(resultado.getString("modelo"));
                seletorVersao.setSelecionado(resultado.getString("versao"));
                seletorTamanho.setSelecionado(resultado.getString("tamanho"));
                campoPreco.setText(UIUtils.formatarDecimal(resultado.getBigDecimal("preco")));
                campoAno.setText(String.valueOf(resultado.getInt("ano")));
                campoEstoque.setText(String.valueOf(resultado.getInt("quantidade_estoque")));

                detalheAtual = resultado.getString("modelo") + " " + resultado.getInt("ano");
                return semEquipe ? "Sem equipe" : resultado.getString("nome_equipe");
            }
        }
    }

    @Override
    protected String detalheDoRegistro() {
        return detalheAtual;
    }

    private void selecionarEquipe(Integer idEquipe) {
        for (int i = 0; i < comboEquipe.getItemCount(); i++) {
            OpcaoEquipe opcao = comboEquipe.getItemAt(i);
            if (java.util.Objects.equals(opcao.idEquipe(), idEquipe)) {
                comboEquipe.setSelectedIndex(i);
                return;
            }
        }
        comboEquipe.setSelectedItem(SEM_EQUIPE);
    }

    @Override
    protected void inserir(Connection conexao) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERE_CAMISA)) {
            preencherParametros(comando);
            comando.executeUpdate();
        }
    }

    @Override
    protected void atualizar(Connection conexao, int id) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZA_CAMISA)) {
            preencherParametros(comando);
            comando.setInt(8, id);
            comando.executeUpdate();
        }
    }

    @Override
    protected void excluirRegistro(Connection conexao, int id) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_EXCLUI_CAMISA)) {
            comando.setInt(1, id);
            comando.executeUpdate();
        }
    }

    @Override
    protected String traduzirErroEspecifico(SQLException ex) {
        String mensagem = ex.getMessage() == null ? "" : ex.getMessage();
        if (mensagem.contains("foreign key constraint fails")) {
            return "Esta camisa já aparece em pedidos, então não pode ser excluída (ON DELETE RESTRICT). "
                + "Se ela saiu de linha, deixe o estoque em 0.";
        }
        return null;
    }

    /** Os parâmetros 1 a 7 são os mesmos no INSERT e no UPDATE. */
    private void preencherParametros(PreparedStatement comando) throws SQLException {
        comando.setString(1, campoModelo.getText().trim());
        comando.setString(2, seletorVersao.getSelecionado());
        comando.setString(3, seletorTamanho.getSelecionado());
        comando.setBigDecimal(4, UIUtils.lerDecimal(campoPreco.getText()));
        comando.setInt(5, UIUtils.lerInteiro(campoAno.getText()));
        comando.setInt(6, UIUtils.lerInteiro(campoEstoque.getText()));

        OpcaoEquipe equipe = (OpcaoEquipe) comboEquipe.getSelectedItem();
        if (equipe == null || equipe.idEquipe() == null) {
            comando.setNull(7, Types.INTEGER);
        } else {
            comando.setInt(7, equipe.idEquipe());
        }
    }
}
