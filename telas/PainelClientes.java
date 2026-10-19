package telas;

import telas.componentes.CampoFormulario;

import javax.swing.*;
import java.sql.*;

/**
 * Cadastro de clientes: lista com busca por nome ou CPF e formulário fixo ao lado
 * para inserir, editar e excluir (tabela Cliente).
 */
public class PainelClientes extends PainelCadastro {

    private static final String SQL_LISTA_CLIENTES =
        "SELECT id_cliente AS `Nº`, nome AS Nome, cpf AS CPF, bairro AS Bairro, cep AS CEP "
        + "FROM Cliente WHERE nome LIKE ? OR cpf LIKE ? ORDER BY nome";
    private static final String SQL_BUSCA_CLIENTE =
        "SELECT nome, cpf, rua, numero, bairro, cep FROM Cliente WHERE id_cliente = ?";
    private static final String SQL_INSERE_CLIENTE =
        "INSERT INTO Cliente (nome, cpf, rua, bairro, numero, cep) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_ATUALIZA_CLIENTE =
        "UPDATE Cliente SET nome = ?, cpf = ?, rua = ?, bairro = ?, numero = ?, cep = ? WHERE id_cliente = ?";
    private static final String SQL_EXCLUI_CLIENTE =
        "DELETE FROM Cliente WHERE id_cliente = ?";

    private final JTextField campoNome = UIUtils.criarCampoTexto("Nome completo");
    private final JFormattedTextField campoCpf = UIUtils.criarCampoMascara("###.###.###-##");
    private final JTextField campoRua = UIUtils.criarCampoTexto("Rua, avenida…");
    private final JTextField campoNumero = UIUtils.criarCampoTexto("Nº");
    private final JTextField campoBairro = UIUtils.criarCampoTexto("Bairro");
    private final JFormattedTextField campoCep = UIUtils.criarCampoMascara("#####-###");

    private final CampoFormulario grupoNome = new CampoFormulario("Nome", campoNome);
    private final CampoFormulario grupoCpf = new CampoFormulario("CPF", campoCpf);
    private final CampoFormulario grupoRua = new CampoFormulario("Rua", campoRua);
    private final CampoFormulario grupoNumero = new CampoFormulario("Número", campoNumero);
    private final CampoFormulario grupoBairro = new CampoFormulario("Bairro", campoBairro);
    private final CampoFormulario grupoCep = new CampoFormulario("CEP", campoCep);

    public PainelClientes() {
        super("Cliente", "Clientes", false);
        montarTela("Clientes", "Clique em um cliente para editar ou excluir.");
    }

    @Override
    protected void montarCampos(JPanel painelCampos) {
        UIUtils.adicionarLinhaFormulario(painelCampos, 0, grupoNome);
        UIUtils.adicionarLinhaFormulario(painelCampos, 1, grupoCpf);
        UIUtils.adicionarLinhaFormulario(painelCampos, 2, grupoRua, grupoNumero, 3, 1);
        UIUtils.adicionarLinhaFormulario(painelCampos, 3, grupoBairro, grupoCep, 3, 2);
    }

    @Override
    protected void focarPrimeiroCampo() {
        SwingUtilities.invokeLater(campoNome::requestFocusInWindow);
    }

    @Override
    protected void limparCampos() {
        campoNome.setText("");
        campoCpf.setValue(null);
        campoRua.setText("");
        campoNumero.setText("");
        campoBairro.setText("");
        campoCep.setValue(null);
    }

    @Override
    protected void limparErros() {
        for (CampoFormulario grupo : new CampoFormulario[]{grupoNome, grupoCpf, grupoRua, grupoNumero, grupoBairro, grupoCep}) {
            grupo.limparErro();
        }
    }

    @Override
    protected boolean validarCampos() {
        boolean valido = true;
        if (campoNome.getText().isBlank()) {
            grupoNome.mostrarErro("Informe o nome.");
            valido = false;
        }
        if (UIUtils.somenteDigitos(campoCpf.getText()).length() != 11) {
            grupoCpf.mostrarErro("O CPF precisa ter 11 dígitos.");
            valido = false;
        }
        if (campoRua.getText().isBlank()) {
            grupoRua.mostrarErro("Informe a rua.");
            valido = false;
        }
        Integer numero = UIUtils.lerInteiro(campoNumero.getText());
        if (numero == null || numero <= 0) {
            grupoNumero.mostrarErro("Número inválido.");
            valido = false;
        }
        if (campoBairro.getText().isBlank()) {
            grupoBairro.mostrarErro("Informe o bairro.");
            valido = false;
        }
        if (UIUtils.somenteDigitos(campoCep.getText()).length() != 8) {
            grupoCep.mostrarErro("O CEP tem 8 dígitos.");
            valido = false;
        }
        return valido;
    }

    @Override
    protected PreparedStatement prepararConsultaLista(Connection conexao, String filtro) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(SQL_LISTA_CLIENTES);
        consulta.setString(1, filtro);
        // O CPF é guardado só com números, então a busca também ignora pontos e traço.
        // Se a busca não tem nenhum dígito, o '' faz essa parte nunca combinar (senão '%%' traria todos).
        String digitosBusca = UIUtils.somenteDigitos(filtro);
        consulta.setString(2, digitosBusca.isEmpty() ? "" : "%" + digitosBusca + "%");
        return consulta;
    }

    @Override
    protected void ajustarColunas() {
        UIUtils.configurarColuna(tabela, 0, 56, SwingConstants.RIGHT);
        UIUtils.configurarColuna(tabela, 2, 130, SwingConstants.LEFT);
        UIUtils.configurarColuna(tabela, 4, 100, SwingConstants.LEFT);
    }

    @Override
    protected String carregarRegistro(Connection conexao, int id) throws SQLException {
        try (PreparedStatement consulta = conexao.prepareStatement(SQL_BUSCA_CLIENTE)) {
            consulta.setInt(1, id);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) return null;
                campoNome.setText(resultado.getString("nome"));
                campoCpf.setValue(resultado.getString("cpf"));
                campoRua.setText(resultado.getString("rua"));
                campoNumero.setText(String.valueOf(resultado.getInt("numero")));
                campoBairro.setText(resultado.getString("bairro"));
                campoCep.setValue(resultado.getString("cep"));
                return resultado.getString("nome");
            }
        }
    }

    @Override
    protected void inserir(Connection conexao) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERE_CLIENTE)) {
            preencherParametros(comando);
            comando.executeUpdate();
        }
    }

    @Override
    protected void atualizar(Connection conexao, int id) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZA_CLIENTE)) {
            preencherParametros(comando);
            comando.setInt(7, id);
            comando.executeUpdate();
        }
    }

    @Override
    protected void excluirRegistro(Connection conexao, int id) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_EXCLUI_CLIENTE)) {
            comando.setInt(1, id);
            comando.executeUpdate();
        }
    }

    @Override
    protected String traduzirErroEspecifico(SQLException ex) {
        String mensagem = ex.getMessage() == null ? "" : ex.getMessage();
        if (mensagem.contains("Duplicate entry")) {
            grupoCpf.mostrarErro("Já existe um cliente com este CPF.");
            return "Já existe um cliente cadastrado com este CPF.";
        }
        if (mensagem.contains("foreign key constraint fails")) {
            return "Este cliente tem pedidos registrados, então não pode ser excluído (ON DELETE RESTRICT).";
        }
        return null;
    }

    /** Os parâmetros 1 a 6 são os mesmos no INSERT e no UPDATE. */
    private void preencherParametros(PreparedStatement comando) throws SQLException {
        comando.setString(1, campoNome.getText().trim());
        comando.setString(2, UIUtils.somenteDigitos(campoCpf.getText()));
        comando.setString(3, campoRua.getText().trim());
        comando.setString(4, campoBairro.getText().trim());
        comando.setInt(5, UIUtils.lerInteiro(campoNumero.getText()));
        comando.setString(6, UIUtils.somenteDigitos(campoCep.getText()));
    }
}
