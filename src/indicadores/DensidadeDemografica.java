package indicadores;

import indicadores.exceptions.IndicadorDadosInsuficientesException;
import indicadores.exceptions.IndicadorError;
import indicadores.exceptions.IndicadorException;
import model.Populacao;
import model.Regiao;

public class DensidadeDemografica implements Indicador {
    @Override
    public double calcular(Regiao r) throws IndicadorException {
        if (r == null) {
            throw IndicadorDadosInsuficientesException.regiaoNula(getNome());
        }
        Populacao p = r.getUltimaPopulacao();
        if (p == null) {
            throw IndicadorDadosInsuficientesException.semHistorico(getNome(), r.getCodigo());
        }
        if (r.getAreaKm2() <= 0) {
            throw IndicadorDadosInsuficientesException.valorInvalido(getNome(), r.getCodigo(), "areaKm2");
        }
        if (p.getHabitantes() <= 0) {
            throw IndicadorDadosInsuficientesException.valorInvalido(getNome(), r.getCodigo(), "habitantes");
        }

        double densidade = (double) p.getHabitantes() / r.getAreaKm2();
        if (!Double.isFinite(densidade)) {
            throw IndicadorError.resultadoInvalido(getNome(), densidade);
        }
        return densidade;
    }

    @Override
    public String getNome() {
        return "Densidade Demográfica (hab/km²)";
    }
}
