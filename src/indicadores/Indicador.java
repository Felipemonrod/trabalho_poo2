package indicadores;

import indicadores.exceptions.IndicadorException;
import model.Regiao;

public interface Indicador {
    double calcular(Regiao r) throws IndicadorException;
    String getNome();
}
