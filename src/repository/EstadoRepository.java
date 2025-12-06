package repository;

import config.DatabaseConfig;
import dto.EstadoResumo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EstadoRepository {
    private static final String LISTAR_SQL = "SELECT id, nome, sigla FROM estado ORDER BY nome";
    private static final String LISTAR_POR_REGIAO_SQL = "SELECT id, nome, sigla FROM estado WHERE id_regiao = ? ORDER BY nome";
    private static final String BUSCAR_POR_ID = "SELECT id, nome, sigla FROM estado WHERE id = ?";
    private static final String BUSCAR_POR_MUNICIPIO = "SELECT e.id, e.nome, e.sigla FROM estado e JOIN municipio m ON m.id_estado = e.id WHERE m.id = ?";

    public List<EstadoResumo> listarTodos() throws SQLException {
        try (Connection conn = DatabaseConfig.getReadOnlyConnection();
             PreparedStatement ps = conn.prepareStatement(LISTAR_SQL);
             ResultSet rs = ps.executeQuery()) {
            List<EstadoResumo> estados = new ArrayList<>();
            while (rs.next()) {
                estados.add(new EstadoResumo(rs.getInt("id"), rs.getString("nome"), rs.getString("sigla")));
            }
            return estados;
        }
    }

    public Optional<EstadoResumo> buscarPorId(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getReadOnlyConnection();
             PreparedStatement ps = conn.prepareStatement(BUSCAR_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new EstadoResumo(rs.getInt("id"), rs.getString("nome"), rs.getString("sigla")));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<EstadoResumo> buscarPorMunicipio(int municipioId) throws SQLException {
        try (Connection conn = DatabaseConfig.getReadOnlyConnection();
             PreparedStatement ps = conn.prepareStatement(BUSCAR_POR_MUNICIPIO)) {
            ps.setInt(1, municipioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new EstadoResumo(rs.getInt("id"), rs.getString("nome"), rs.getString("sigla")));
                }
                return Optional.empty();
            }
        }
    }

    public List<EstadoResumo> listarPorRegiao(Integer regiaoId) throws SQLException {
        if (regiaoId == null) {
            return listarTodos();
        }
        try (Connection conn = DatabaseConfig.getReadOnlyConnection();
             PreparedStatement ps = conn.prepareStatement(LISTAR_POR_REGIAO_SQL)) {
            ps.setInt(1, regiaoId);
            try (ResultSet rs = ps.executeQuery()) {
                List<EstadoResumo> estados = new ArrayList<>();
                while (rs.next()) {
                    estados.add(new EstadoResumo(rs.getInt("id"), rs.getString("nome"), rs.getString("sigla")));
                }
                return estados;
            }
        }
    }
}
