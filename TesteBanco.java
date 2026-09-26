import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TesteBanco {
    
    public static void main(String[] args) {
        Connection conexao = ConexaoBanco.conectar();

        if (conexao != null) {
            String sql = "SELECT id_cliente, nome, cpf, bairro FROM Cliente;";

            try (PreparedStatement stmt = conexao.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int idCliente = rs.getInt("id_cliente");
                    String nome = rs.getString("nome");
                    String cpf = rs.getString("cpf");
                    String bairro = rs.getString("bairro");

                    System.out.println("ID: " + idCliente + " | Nome: " + nome + " | CPF: " + cpf + " | Bairro: " + bairro);
                }

            } catch (SQLException e) {
                System.out.println("Erro ao executar a consulta: " + e.getMessage());
            } finally {
                try {
                    conexao.close(); 
                } catch (SQLException e) {
                    System.out.println("Erro ao fechar a conexão: " + e.getMessage());
                }
            }
        }
    }
}