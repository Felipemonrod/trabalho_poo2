package indicadores;

import indicadores.exceptions.IndicadorDadosInsuficientesException;
import indicadores.exceptions.IndicadorError;
import indicadores.exceptions.IndicadorException;
import model.Populacao;
import model.Regiao;

public class CrescimentoPopulacional implements Indicador {
    @Override
    public double calcular(Regiao r) throws IndicadorException {
        if (r == null) {
            throw IndicadorDadosInsuficientesException.regiaoNula(getNome());
        }
        Populacao ultimo = r.getUltimaPopulacao();
        Populacao penultimo = r.getPenultimaPopulacao();
        if (ultimo == null || penultimo == null) {
            throw IndicadorDadosInsuficientesException.semHistorico(getNome(), r.getCodigo());
        }
        if (penultimo.getHabitantes() <= 0) {
            throw IndicadorDadosInsuficientesException.valorInvalido(getNome(), r.getCodigo(), "habitantes");
        }
        double diff = (double) (ultimo.getHabitantes() - penultimo.getHabitantes());
        double percentual = (diff / penultimo.getHabitantes()) * 100.0;
        if (!Double.isFinite(percentual)) {
            throw IndicadorError.resultadoInvalido(getNome(), percentual);
        }
        return percentual;
    }

    @Override
    public String getNome() {
        return "Crescimento Populacional (%)";
    }
}
