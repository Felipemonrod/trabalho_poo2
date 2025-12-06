package dto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record DashboardSnapshot(List<EstadoPopulacao> estados,
                                List<PontoSerieTemporal> serieTemporal,
                                EstadoResumo estadoSelecionado,
                                MunicipioResumo municipioSelecionado,
                                String mensagem) {

    public DashboardSnapshot {
        estados = estados == null ? Collections.emptyList() : List.copyOf(estados);
        serieTemporal = serieTemporal == null ? Collections.emptyList() : List.copyOf(serieTemporal);
    }

    public Optional<EstadoResumo> estadoOpt() {
        return Optional.ofNullable(estadoSelecionado);
    }

    public Optional<MunicipioResumo> municipioOpt() {
        return Optional.ofNullable(municipioSelecionado);
    }
}
