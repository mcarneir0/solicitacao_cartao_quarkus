package br.edu.fiap.cartoes.repository;

import br.edu.fiap.cartoes.entity.TipoCartao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC: use AgroalDataSource, PreparedStatement e try-with-resources.
 * Não chamar controller nem gerar token nesta camada. Esta é a única implementação JDBC obrigatória.
 */
@ApplicationScoped
public class TipoCartaoRepositoryJdbc implements ConsultaTiposDisponiveis {
    // TODO: implementar a interface depois de declarar suas assinaturas.

    private final DataSource dataSource;

    @Inject
    public TipoCartaoRepositoryJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<TipoCartao> listarDisponiveisPorNome() {
        String sql = """
                        SELECT id, nome, descricao, disponivel
                        FROM tipos_cartao
                        WHERE disponivel = true
                        ORDER BY nome ASC
                    """;
        List<TipoCartao> cartoes = new ArrayList<>();

        try(
                Connection c = dataSource.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while(rs.next()) cartoes.add(map(rs));
            return cartoes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Centraliza a conversão de uma linha SQL para o modelo Java. */
    private TipoCartao map(ResultSet rs) throws SQLException {
        return new TipoCartao(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("descricao"),
                rs.getBoolean("disponivel")
        );
    }
}
