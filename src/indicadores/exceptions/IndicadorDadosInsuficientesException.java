package indicadores.exceptions;

/**
 * Indica falta ou inconsistencia de dados para a avaliacao do indicador.
 */
public class IndicadorDadosInsuficientesException extends IndicadorException {
    public IndicadorDadosInsuficientesException(String indicador, String codigoRegiao, String mensagem) {
        super(indicador, codigoRegiao, mensagem);
    }

    public static IndicadorDadosInsuficientesException regiaoNula(String indicador) {
        return new IndicadorDadosInsuficientesException(indicador, null, "Regiao nao informada para o calculo");
    }

    public static IndicadorDadosInsuficientesException semHistorico(String indicador, String codigoRegiao) {
        return new IndicadorDadosInsuficientesException(indicador, codigoRegiao, "Historico de populacao insuficiente");
    }

    public static IndicadorDadosInsuficientesException valorInvalido(String indicador, String codigoRegiao, String campo) {
        String alvo = (campo == null || campo.isEmpty()) ? "campo desconhecido" : campo;
        return new IndicadorDadosInsuficientesException(indicador, codigoRegiao, "Valor invalido encontrado para " + alvo);
    }
}
