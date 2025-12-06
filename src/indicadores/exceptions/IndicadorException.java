package indicadores.exceptions;

/**
 * Base para excecoes de calculo de indicadores demograficos.
 */
public class IndicadorException extends RuntimeException {
    private final String indicador;
    private final String codigoRegiao;

    public IndicadorException(String indicador, String mensagem) {
        this(indicador, null, mensagem, null);
    }

    public IndicadorException(String indicador, String codigoRegiao, String mensagem) {
        this(indicador, codigoRegiao, mensagem, null);
    }

    public IndicadorException(String indicador, String codigoRegiao, String mensagem, Throwable causa) {
        super(montarMensagem(indicador, codigoRegiao, mensagem), causa);
        this.indicador = indicador;
        this.codigoRegiao = codigoRegiao;
    }

    private static String montarMensagem(String indicador, String codigoRegiao, String mensagem) {
        StringBuilder sb = new StringBuilder();
        if (indicador != null && !indicador.isEmpty()) {
            sb.append("Indicador ").append(indicador);
        } else {
            sb.append("Indicador");
        }
        if (codigoRegiao != null && !codigoRegiao.isEmpty()) {
            sb.append(" [regiao=").append(codigoRegiao).append("]");
        }
        if (mensagem != null && !mensagem.isEmpty()) {
            sb.append(": ").append(mensagem);
        }
        return sb.toString();
    }

    public String getIndicador() {
        return indicador;
    }

    public String getCodigoRegiao() {
        return codigoRegiao;
    }
}
