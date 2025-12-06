package repository;

import config.DatabaseConfig;
import dto.EstadoPopulacao;
import dto.MunicipioResumo;
import dto.PontoSerieTemporal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DashboardRepository {
    private static final String POPULACAO_ESTADO_SQL =
            "SELECT e.nome AS estado, e.sigla AS sigla, COALESCE(r.nome, 'Sem região') AS regiao, p.ano AS ano, " +
            "SUM(p.habitantes) AS habitantes " +
            "FROM estado e " +
            "JOIN municipio m ON m.id_estado = e.id " +
            "JOIN populacao p ON p.id_municipio = m.id " +
            "LEFT JOIN regiao r ON r.id = e.id_regiao " +
            "WHERE p.ano = ? " +
            "AND (? IS NULL OR e.id = ?) " +
            "AND (? IS NULL OR m.id = ?) " +
            "AND (? IS NULL OR r.id = ?) " +
            "GROUP BY e.nome, e.sigla, regiao, p.ano " +
            "ORDER BY habitantes DESC";

    private static final String SERIE_MUNICIPIO_SQL =
            "SELECT ano, habitantes FROM populacao WHERE id_municipio = ? ORDER BY ano";

    private static final String MUNICIPIO_TOP_SQL =
            "SELECT m.id, m.nome, m.id_estado, SUM(p.habitantes) AS total " +
            "FROM municipio m " +
            "JOIN populacao p ON p.id_municipio = m.id " +
            "WHERE p.ano = ? " +
            "GROUP BY m.id, m.nome, m.id_estado " +
            "ORDER BY total DESC " +
            "LIMIT 1";

    private static final String LISTAR_ANOS_SQL =
            "SELECT DISTINCT ano FROM populacao ORDER BY ano";

    public List<EstadoPopulacao> carregarPopulacaoEstados(Integer estadoId,
                                                          Integer municipioId,
                                                          int ano,
                                                          Integer regiaoId) throws SQLException {
        try (Connection conn = DatabaseConfig.getReadOnlyConnection();
             PreparedStatement ps = conn.prepareStatement(POPULACAO_ESTADO_SQL)) {
            ps.setInt(1, ano);
            if (estadoId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, estadoId);
                ps.setInt(3, estadoId);
            }
            if (municipioId == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, municipioId);
                ps.setInt(5, municipioId);
            }
            if (regiaoId == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
                ps.setNull(7, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, regiaoId);
                ps.setInt(7, regiaoId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<EstadoPopulacao> data = new ArrayList<>();
                while (rs.next()) {
                    data.add(new EstadoPopulacao(
                            rs.getString("estado"),
                            rs.getString("sigla"),
                            rs.getString("regiao"),
                            rs.getInt("ano"),
                            rs.getLong("habitantes")));
                }
                return data;
            }
        }
    }

    public List<PontoSerieTemporal> carregarSerieMunicipio(int municipioId) throws SQLException {
        try (Connection conn = DatabaseConfig.getReadOnlyConnection();
             PreparedStatement ps = conn.prepareStatement(SERIE_MUNICIPIO_SQL)) {
            ps.setInt(1, municipioId);
            try (ResultSet rs = ps.executeQuery()) {
                List<PontoSerieTemporal> pontos = new ArrayList<>();
                while (rs.next()) {
                    pontos.add(new PontoSerieTemporal(rs.getInt("ano"), rs.getLong("habitantes")));
                }
                return pontos;
            }
        }
    }

    public Optional<MunicipioResumo> buscarMunicipioMaisPopuloso(int ano) throws SQLException {
        try (Connection conn = DatabaseConfig.getReadOnlyConnection();
             PreparedStatement ps = conn.prepareStatement(MUNICIPIO_TOP_SQL)) {
            ps.setInt(1, ano);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new MunicipioResumo(rs.getInt("id"), rs.getString("nome"), rs.getInt("id_estado")));
                }
                return Optional.empty();
            }
        }
    }

    public List<Integer> listarAnosDisponiveis() throws SQLException {
        try (Connection conn = DatabaseConfig.getReadOnlyConnection();
             PreparedStatement ps = conn.prepareStatement(LISTAR_ANOS_SQL);
             ResultSet rs = ps.executeQuery()) {
            List<Integer> anos = new ArrayList<>();
            while (rs.next()) {
                anos.add(rs.getInt("ano"));
            }
            return anos;
        }
    }
}




