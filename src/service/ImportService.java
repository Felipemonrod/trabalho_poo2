package service;

import config.DatabaseConfig;
import dto.ImportResult;
import exceptions.ArquivoInvalidoException;
import exceptions.DashboardDataException;
import model.Estado;
import model.Municipio;
import model.Populacao;
import model.Regiao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportService {
    private static final String UPSERT_ESTADO =
            "INSERT INTO estado (nome, sigla, area_km2, codigo_ibge) VALUES (?,?,?,?) " +
            "ON DUPLICATE KEY UPDATE nome = VALUES(nome), area_km2 = VALUES(area_km2), codigo_ibge = VALUES(codigo_ibge)";
    private static final String SELECT_ESTADO_ID = "SELECT id FROM estado WHERE sigla = ?";

    private static final String UPSERT_MUNICIPIO =
            "INSERT INTO municipio (id_estado, nome, area_km2, codigo_ibge) VALUES (?,?,?,?) " +
            "ON DUPLICATE KEY UPDATE area_km2 = VALUES(area_km2), codigo_ibge = VALUES(codigo_ibge)";
    private static final String SELECT_MUNICIPIO_ID = "SELECT id FROM municipio WHERE id_estado = ? AND nome = ?";
    private static final String SELECT_MUNICIPIO_ID_POR_CODIGO = "SELECT id FROM municipio WHERE codigo_ibge = ?";

    private static final String UPSERT_POPULACAO =
            "INSERT INTO populacao (id_municipio, ano, habitantes, fonte) VALUES (?,?,?,?) " +
            "ON DUPLICATE KEY UPDATE habitantes = VALUES(habitantes), fonte = VALUES(fonte)";

    public ImportResult importarCsvParaBanco(String caminhoCsv) throws ArquivoInvalidoException {
        List<Regiao> regioes = Entrada.lerArquivo(caminhoCsv);
        if (regioes.isEmpty()) {
            return new ImportResult(0, 0, 0);
        }

        try (Connection conn = DatabaseConfig.getWriteConnection()) {
            conn.setAutoCommit(false);
            try {
                ImportadorContexto contexto = new ImportadorContexto(conn);
                int estados = 0;
                int municipios = 0;
                int populacoes = 0;

                // Primeiro estados
                for (Regiao regiao : regioes) {
                    if (regiao instanceof Estado estado) {
                        if (contexto.processarEstado(estado)) {
                            estados++;
                        }
                    }
                }

                // Depois municípios
                for (Regiao regiao : regioes) {
                    if (regiao instanceof Municipio municipio) {
                        ResultadoMunicipio resultado = contexto.processarMunicipio(municipio);
                        municipios += resultado.inseriuMunicipio() ? 1 : 0;
                        populacoes += resultado.populacoesInseridas();
                    }
                }

                conn.commit();
                return new ImportResult(estados, municipios, populacoes);
            } catch (SQLException e) {
                conn.rollback();
                throw new DashboardDataException(DatabaseErrorTranslator.traduzir("Falha ao importar dados no banco", e), e);
            }
        } catch (SQLException e) {
            throw new DashboardDataException(DatabaseErrorTranslator.traduzir("Erro de conexão com o banco para importação", e), e);
        }
    }

    private static class ImportadorContexto {
        private final Connection conn;
        private final Map<String, Integer> estadoIdPorSigla = new HashMap<>();
        private final Map<String, Integer> estadoIdPorCodigo = new HashMap<>();
        private final Map<String, Integer> municipioIdPorCodigo = new HashMap<>();

        ImportadorContexto(Connection conn) {
            this.conn = conn;
        }

        boolean processarEstado(Estado estado) throws SQLException {
            Integer codigoIbge = parseInteiro(estado.getCodigo());
            BigDecimal area = toBigDecimal(estado.getAreaKm2());
            try (PreparedStatement ps = conn.prepareStatement(UPSERT_ESTADO)) {
                ps.setString(1, estado.getNome());
                ps.setString(2, estado.getUf());
                if (area == null) {
                    ps.setNull(3, java.sql.Types.DECIMAL);
                } else {
                    ps.setBigDecimal(3, area);
                }
                if (codigoIbge == null) {
                    ps.setNull(4, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(4, codigoIbge);
                }
                ps.executeUpdate();
            }

            Integer estadoId = obterEstadoId(estado.getUf(), codigoIbge);
            if (estadoId != null) {
                if (estado.getUf() != null && !estado.getUf().isEmpty()) {
                    estadoIdPorSigla.put(estado.getUf(), estadoId);
                }
                String codigoNormalizado = normalizarCodigo(estado.getCodigo());
                if (codigoNormalizado != null) {
                    estadoIdPorCodigo.put(codigoNormalizado, estadoId);
                }
            }
            return true;
        }

        ResultadoMunicipio processarMunicipio(Municipio municipio) throws SQLException {
            Integer estadoId = resolverEstadoId(municipio);
            if (estadoId == null) {
                throw new SQLException("Estado não encontrado para o município " + municipio.getNome());
            }

            Integer codigoIbge = parseInteiro(municipio.getCodigo());
            BigDecimal area = toBigDecimal(municipio.getAreaKm2());

            try (PreparedStatement ps = conn.prepareStatement(UPSERT_MUNICIPIO)) {
                ps.setInt(1, estadoId);
                ps.setString(2, municipio.getNome());
                if (area == null) {
                    ps.setNull(3, java.sql.Types.DECIMAL);
                } else {
                    ps.setBigDecimal(3, area);
                }
                if (codigoIbge == null) {
                    ps.setNull(4, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(4, codigoIbge);
                }
                ps.executeUpdate();
            }

            Integer municipioId = obterMunicipioId(estadoId, municipio.getNome(), codigoIbge);
            if (municipioId == null) {
                throw new SQLException("Não foi possível obter ID do município " + municipio.getNome());
            }
            if (municipio.getCodigo() != null) {
                String codigoNormalizado = normalizarCodigo(municipio.getCodigo());
                if (codigoNormalizado != null) {
                    municipioIdPorCodigo.put(codigoNormalizado, municipioId);
                }
            }

            int populacoesInseridas = inserirHistoricoPopulacional(municipioId, municipio.getHistorico());
            return new ResultadoMunicipio(true, populacoesInseridas);
        }

        private Integer resolverEstadoId(Municipio municipio) throws SQLException {
            if (municipio.getEstado() != null && municipio.getEstado().getUf() != null) {
                Integer cached = estadoIdPorSigla.get(municipio.getEstado().getUf());
                if (cached != null) {
                    return cached;
                }
            }
            if (municipio.getCodigoEstado() != null) {
                Integer cached = estadoIdPorCodigo.get(normalizarCodigo(municipio.getCodigoEstado()));
                if (cached != null) {
                    return cached;
                }
            }

            if (municipio.getCodigoEstado() != null) {
                Integer codigo = parseInteiro(municipio.getCodigoEstado());
                if (codigo != null) {
                    Integer estadoId = obterEstadoId(null, codigo);
                    if (estadoId != null) {
                        return estadoId;
                    }
                }
            }

            if (municipio.getEstado() != null && municipio.getEstado().getUf() != null) {
                return obterEstadoId(municipio.getEstado().getUf(), null);
            }
            return null;
        }

        private Integer obterEstadoId(String sigla, Integer codigoIbge) throws SQLException {
            if (sigla != null) {
                Integer cached = estadoIdPorSigla.get(sigla);
                if (cached != null) {
                    return cached;
                }
            }
            if (codigoIbge != null) {
                Integer cached = estadoIdPorCodigo.get(String.valueOf(codigoIbge));
                if (cached != null) {
                    return cached;
                }
            }
            if (sigla != null) {
                try (PreparedStatement ps = conn.prepareStatement(SELECT_ESTADO_ID)) {
                    ps.setString(1, sigla);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                }
            }
            if (codigoIbge != null) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM estado WHERE codigo_ibge = ?")) {
                    ps.setInt(1, codigoIbge);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                }
            }
            return null;
        }

        private Integer obterMunicipioId(int estadoId, String nomeMunicipio, Integer codigoIbge) throws SQLException {
            if (codigoIbge != null) {
                Integer cached = municipioIdPorCodigo.get(String.valueOf(codigoIbge));
                if (cached != null) {
                    return cached;
                }
            }

            if (codigoIbge != null) {
                try (PreparedStatement ps = conn.prepareStatement(SELECT_MUNICIPIO_ID_POR_CODIGO)) {
                    ps.setInt(1, codigoIbge);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(SELECT_MUNICIPIO_ID)) {
                ps.setInt(1, estadoId);
                ps.setString(2, nomeMunicipio);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return null;
        }

        private int inserirHistoricoPopulacional(int municipioId, List<Populacao> historico) throws SQLException {
            if (historico == null || historico.isEmpty()) {
                return 0;
            }
            int total = 0;
            try (PreparedStatement ps = conn.prepareStatement(UPSERT_POPULACAO)) {
                for (Populacao p : historico) {
                    ps.setInt(1, municipioId);
                    ps.setInt(2, p.getAno());
                    ps.setLong(3, p.getHabitantes());
                    ps.setString(4, "CSV");
                    total += ps.executeUpdate();
                }
            }
            return total;
        }

        private Integer parseInteiro(String valor) {
            if (valor == null || valor.trim().isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(valor.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private String normalizarCodigo(String valor) {
            if (valor == null) {
                return null;
            }
            String digits = valor.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? null : digits;
        }

        private BigDecimal toBigDecimal(double valor) {
            if (Double.isNaN(valor) || Double.isInfinite(valor) || valor == 0) {
                return null;
            }
            return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
        }
    }

    private record ResultadoMunicipio(boolean inseriuMunicipio, int populacoesInseridas) {
    }
}
