package repository;

import config.DatabaseConfig;
import dto.MunicipioResumo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MunicipioRepository {
    private static final String LISTAR_TODOS = "SELECT id, nome, id_estado FROM municipio ORDER BY nome";
    private static final String LISTAR_POR_ESTADO = "SELECT id, nome, id_estado FROM municipio WHERE id_estado = ? ORDER BY nome";
    private static final String BUSCAR_POR_ID = "SELECT id, nome, id_estado FROM municipio WHERE id = ?";

    public List<MunicipioResumo> listarTodos() throws SQLException {
        try (Connection conn = DatabaseConfig.getReadOnlyConnection();
             PreparedStatement ps = conn.prepareStatement(LISTAR_TODOS);
             ResultSet rs = ps.executeQuery()) {
            List<MunicipioResumo> municipios = new ArrayList<>();
            while (rs.next()) {
                municipios.add(map(rs));
            }
            return municipios;
        }
    }

    public List<MunicipioResumo> listarPorEstado(int estadoId) throws SQLException {
        try (Connection conn = DatabaseConfig.getReadOnlyConnection();
             PreparedStatement ps = conn.prepareStatement(LISTAR_POR_ESTADO)) {
            ps.setInt(1, estadoId);
            try (ResultSet rs = ps.executeQuery()) {
                List<MunicipioResumo> municipios = new ArrayList<>();
                while (rs.next()) {
                    municipios.add(map(rs));
                }
                return municipios;
            }
        }
    }

    public Optional<MunicipioResumo> buscarPorId(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getReadOnlyConnection();
             PreparedStatement ps = conn.prepareStatement(BUSCAR_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        }
    }

    private MunicipioResumo map(ResultSet rs) throws SQLException {
        return new MunicipioResumo(rs.getInt("id"), rs.getString("nome"), rs.getInt("id_estado"));
    }
}
