package indicadores.exceptions;

/**
 * Erro de programacao relacionado aos indicadores; reservado para estados realmente fatais.
 */
public class IndicadorError extends Error {
    public IndicadorError(String indicador, String mensagem) {
        super(montarMensagem(indicador, mensagem));
    }

    public IndicadorError(String indicador, String mensagem, Throwable causa) {
        super(montarMensagem(indicador, mensagem), causa);
    }

    private static String montarMensagem(String indicador, String mensagem) {
        StringBuilder sb = new StringBuilder();
        if (indicador != null && !indicador.isEmpty()) {
            sb.append("Indicador ").append(indicador);
        } else {
            sb.append("Indicador");
        }
        if (mensagem != null && !mensagem.isEmpty()) {
            sb.append(": ").append(mensagem);
        }
        return sb.toString();
    }

    public static IndicadorError configuracaoInconsistente(String indicador) {
        return new IndicadorError(indicador, "Configuracao inconsistente detectada");
    }

    public static IndicadorError resultadoInvalido(String indicador, double valorCalculado) {
        return new IndicadorError(indicador, "Resultado invalido calculado (valor=" + valorCalculado + ")");
    }
}
