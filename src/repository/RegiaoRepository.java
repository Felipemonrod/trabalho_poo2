package repository;

import config.DatabaseConfig;
import dto.RegiaoResumo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegiaoRepository {
    private static final String LISTAR_SQL = "SELECT id, nome FROM regiao ORDER BY nome";
    private static final String BUSCAR_POR_ID = "SELECT id, nome FROM regiao WHERE id = ?";

    public List<RegiaoResumo> listarTodas() throws SQLException {
        try (Connection conn = DatabaseConfig.getReadOnlyConnection();
             PreparedStatement ps = conn.prepareStatement(LISTAR_SQL);
             ResultSet rs = ps.executeQuery()) {
            List<RegiaoResumo> regioes = new ArrayList<>();
            while (rs.next()) {
                regioes.add(new RegiaoResumo(rs.getInt("id"), rs.getString("nome")));
            }
            return regioes;
        }
    }

    public Optional<RegiaoResumo> buscarPorId(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getReadOnlyConnection();
             PreparedStatement ps = conn.prepareStatement(BUSCAR_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new RegiaoResumo(rs.getInt("id"), rs.getString("nome")));
                }
                return Optional.empty();
            }
        }
    }
}
