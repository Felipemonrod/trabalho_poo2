package service;

import dto.DashboardSnapshot;
import dto.EstadoPopulacao;
import dto.EstadoResumo;
import dto.FiltroResolvido;
import dto.MunicipioResumo;
import dto.PontoSerieTemporal;
import dto.RegiaoResumo;
import exceptions.DashboardDataException;
import repository.DashboardRepository;
import repository.EstadoRepository;
import repository.MunicipioRepository;
import repository.RegiaoRepository;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DashboardService {
    private final EstadoRepository estadoRepository = new EstadoRepository();
    private final MunicipioRepository municipioRepository = new MunicipioRepository();
    private final DashboardRepository dashboardRepository = new DashboardRepository();
    private final RegiaoRepository regiaoRepository = new RegiaoRepository();

    public List<EstadoResumo> listarEstados() {
        return listarEstadosPorRegiao(null);
    }

    public List<EstadoResumo> listarEstadosPorRegiao(Integer regiaoId) {
        try {
            return regiaoId == null ? estadoRepository.listarTodos() : estadoRepository.listarPorRegiao(regiaoId);
        } catch (SQLException e) {
            throw new DashboardDataException(DatabaseErrorTranslator.traduzir("Não foi possível listar os estados", e), e);
        }
    }

    public List<MunicipioResumo> listarMunicipios(Integer estadoId) {
        try {
            if (estadoId == null) {
                return municipioRepository.listarTodos();
            }
            return municipioRepository.listarPorEstado(estadoId);
        } catch (SQLException e) {
            throw new DashboardDataException(DatabaseErrorTranslator.traduzir("Não foi possível listar os municípios", e), e);
        }
    }

    public DashboardSnapshot carregarSnapshot(Integer estadoId, Integer municipioId, Integer ano, Integer regiaoId) {
        FiltroResolvido filtro = resolverFiltro(estadoId, municipioId, ano);
        List<EstadoPopulacao> estados = carregarPopulacaoEstados(filtro, ano, regiaoId);
        List<PontoSerieTemporal> serie = carregarSerieTemporal(filtro);

        String mensagem = montarMensagem(filtro.estado(), filtro.municipio());
        return new DashboardSnapshot(estados, serie, filtro.estado(), filtro.municipio(), mensagem);
    }

    public FiltroResolvido resolverFiltro(Integer estadoId, Integer municipioId, Integer ano) {
        if (estadoId == null && municipioId == null) {
            return new FiltroResolvido(null, null);
        }
        try {
            int anoResolvido = resolverAno(ano);
            MunicipioResumo municipioSelecionado = selecionarMunicipio(estadoId, municipioId, anoResolvido);
            EstadoResumo estadoSelecionado = selecionarEstado(estadoId, municipioSelecionado);
            return new FiltroResolvido(estadoSelecionado, municipioSelecionado);
        } catch (SQLException e) {
            throw new DashboardDataException(DatabaseErrorTranslator.traduzir("Não foi possível resolver o filtro selecionado", e), e);
        }
    }

    public List<EstadoPopulacao> carregarPopulacaoEstados(FiltroResolvido filtro, Integer ano, Integer regiaoId) {
        try {
            int anoResolvido = resolverAno(ano);
            Integer estadoId = filtro.estado() == null ? null : filtro.estado().id();
            Integer municipioId = filtro.municipio() == null ? null : filtro.municipio().id();
            return dashboardRepository.carregarPopulacaoEstados(estadoId, municipioId, anoResolvido, regiaoId);
        } catch (SQLException e) {
            throw new DashboardDataException(DatabaseErrorTranslator.traduzir("Não foi possível carregar os gráficos de estados", e), e);
        }
    }

    public List<PontoSerieTemporal> carregarSerieTemporal(FiltroResolvido filtro) {
        try {
            if (filtro.municipio() == null) {
                return Collections.emptyList();
            }
            return dashboardRepository.carregarSerieMunicipio(filtro.municipio().id());
        } catch (SQLException e) {
            throw new DashboardDataException(DatabaseErrorTranslator.traduzir("Não foi possível carregar a série do município", e), e);
        }
    }

    private MunicipioResumo selecionarMunicipio(Integer estadoId, Integer municipioId, int ano) throws SQLException {
        if (municipioId != null) {
            return municipioRepository.buscarPorId(municipioId).orElse(null);
        }
        if (estadoId != null) {
            List<MunicipioResumo> municipios = municipioRepository.listarPorEstado(estadoId);
            return municipios.isEmpty() ? null : municipios.get(0);
        }
        Optional<MunicipioResumo> top = dashboardRepository.buscarMunicipioMaisPopuloso(ano);
        return top.orElse(null);
    }

    private EstadoResumo selecionarEstado(Integer estadoId, MunicipioResumo municipioRelacionado) throws SQLException {
        if (estadoId != null) {
            return estadoRepository.buscarPorId(estadoId).orElse(null);
        }
        if (municipioRelacionado != null) {
            return estadoRepository.buscarPorId(municipioRelacionado.estadoId()).orElse(null);
        }
        List<EstadoResumo> estados = estadoRepository.listarTodos();
        return estados.isEmpty() ? null : estados.get(0);
    }

    private String montarMensagem(EstadoResumo estado, MunicipioResumo municipio) {
        if (estado == null && municipio == null) {
            return "Exibindo dados gerais (todos os estados)";
        }
        if (estado != null && municipio == null) {
            return "Exibindo dados do estado " + estado.sigla();
        }
        if (municipio != null && estado != null) {
            return "Exibindo série histórica de " + municipio.nome() + " (" + estado.sigla() + ")";
        }
        return "Exibindo dados filtrados";
    }

    public String mensagemParaFiltro(FiltroResolvido filtro) {
        return montarMensagem(filtro.estado(), filtro.municipio());
    }

    public List<Integer> listarAnosDisponiveis() {
        try {
            return dashboardRepository.listarAnosDisponiveis();
        } catch (SQLException e) {
            throw new DashboardDataException(DatabaseErrorTranslator.traduzir("Não foi possível listar os anos disponíveis", e), e);
        }
    }

    public int obterAnoPadrao() {
        List<Integer> anos = listarAnosDisponiveis();
        if (anos.isEmpty()) {
            throw new DashboardDataException("Nenhum ano disponível na base populacional", null);
        }
        return anos.get(anos.size() - 1);
    }

    public List<RegiaoResumo> listarRegioes() {
        try {
            return regiaoRepository.listarTodas();
        } catch (SQLException e) {
            throw new DashboardDataException(DatabaseErrorTranslator.traduzir("Não foi possível listar as regiões", e), e);
        }
    }

    private int resolverAno(Integer ano) {
        if (ano != null) {
            return ano;
        }
        List<Integer> anos = listarAnosDisponiveis();
        if (anos.isEmpty()) {
            throw new DashboardDataException("Nenhum ano disponível para consulta", null);
        }
        return anos.get(anos.size() - 1);
    }
}
