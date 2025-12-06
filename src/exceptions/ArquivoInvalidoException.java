package exceptions;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ArquivoInvalidoException extends Exception {
    private final String caminhoArquivo;
    private final Integer linhaArquivo;
    private final String conteudoLinha;

    public ArquivoInvalidoException(String message) {
        this(message, null, null, null, null);
    }

    public ArquivoInvalidoException(String message, Throwable cause) {
        this(message, null, null, null, cause);
    }

    public ArquivoInvalidoException(String message, String caminhoArquivo) {
        this(message, caminhoArquivo, null, null, null);
    }

    public ArquivoInvalidoException(String message, String caminhoArquivo, Integer linhaArquivo, String conteudoLinha) {
        this(message, caminhoArquivo, linhaArquivo, conteudoLinha, null);
    }

    public ArquivoInvalidoException(String message, String caminhoArquivo, Integer linhaArquivo, String conteudoLinha, Throwable cause) {
        super(montarMensagem(message, caminhoArquivo, linhaArquivo), cause);
        this.caminhoArquivo = caminhoArquivo;
        this.linhaArquivo = linhaArquivo;
        this.conteudoLinha = sanitizarConteudo(conteudoLinha);
    }

    private static String montarMensagem(String mensagemOriginal, String caminhoArquivo, Integer linhaArquivo) {
        String texto = (mensagemOriginal == null || mensagemOriginal.trim().isEmpty()) ? "Arquivo invalido" : mensagemOriginal.trim();
        StringBuilder sb = new StringBuilder(texto);
        String caminhoResumido = resumirCaminho(caminhoArquivo);
        boolean temCaminho = caminhoResumido != null && !caminhoResumido.trim().isEmpty();
        boolean temLinha = linhaArquivo != null && linhaArquivo > 0;
        if (temCaminho || temLinha) {
            sb.append(" (");
            if (temCaminho) {
                sb.append(caminhoResumido);
            }
            if (temLinha) {
                if (temCaminho) {
                    sb.append(":");
                }
                sb.append(linhaArquivo);
            }
            sb.append(")");
        }
        return sb.toString();
    }

    private static String sanitizarConteudo(String conteudoLinha) {
        if (conteudoLinha == null) {
            return null;
        }
        String texto = conteudoLinha.trim();
        if (texto.length() > 200) {
            return texto.substring(0, 197) + "...";
        }
        return texto;
    }

    private static String resumirCaminho(String caminhoArquivo) {
        if (caminhoArquivo == null || caminhoArquivo.trim().isEmpty()) {
            return null;
        }
        try {
            Path path = Paths.get(caminhoArquivo.trim());
            Path fileName = path.getFileName();
            if (fileName != null) {
                return fileName.toString();
            }
        } catch (Exception ignored) {
            // Mantem fallback abaixo
        }
        int barra = Math.max(caminhoArquivo.lastIndexOf('/'), caminhoArquivo.lastIndexOf('\\'));
        if (barra >= 0 && barra + 1 < caminhoArquivo.length()) {
            return caminhoArquivo.substring(barra + 1);
        }
        return caminhoArquivo.trim();
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public Integer getLinhaArquivo() {
        return linhaArquivo;
    }

    public String getConteudoLinha() {
        return conteudoLinha;
    }

    public IllegalArgumentException asIllegalArgumentException() {
        return new IllegalArgumentException(getMessage(), this);
    }

    public IllegalStateException asIllegalStateException() {
        return new IllegalStateException(getMessage(), this);
    }

    public Error asFatalError() {
        return new Error(getMessage(), this);
    }

    public static ArquivoInvalidoException formatoNaoReconhecido(String caminhoArquivo, int linhaArquivo, String conteudoLinha) {
        return new ArquivoInvalidoException("Formato nao reconhecido para a linha do arquivo", caminhoArquivo, linhaArquivo, conteudoLinha);
    }

    public static ArquivoInvalidoException campoObrigatorioAusente(String caminhoArquivo, int linhaArquivo, String nomeCampo) {
        String detalhe = "Campo obrigatorio ausente: " + (nomeCampo == null ? "desconhecido" : nomeCampo);
        return new ArquivoInvalidoException(detalhe, caminhoArquivo, linhaArquivo, null);
    }

    public static ArquivoInvalidoException valorInvalido(String caminhoArquivo, int linhaArquivo, String nomeCampo, String valorInformado, String expectativa) {
        StringBuilder detalhe = new StringBuilder("Valor invalido para o campo ");
        detalhe.append(nomeCampo == null ? "desconhecido" : ("'" + nomeCampo + "'"));
        if (valorInformado != null && !valorInformado.isEmpty()) {
            detalhe.append(". Recebido: '").append(valorInformado).append("'");
        }
        if (expectativa != null && !expectativa.isBlank()) {
            detalhe.append(". Esperado: ").append(expectativa);
        }
        return new ArquivoInvalidoException(detalhe.toString(), caminhoArquivo, linhaArquivo, valorInformado);
    }

    public static ArquivoInvalidoException delimitadorInvalido(String caminhoArquivo, int linhaArquivo, String esperado, String encontrado) {
        String detalhe = String.format("Delimitador invalido. Esperado '%s' mas encontrado '%s'", esperado, encontrado);
        return new ArquivoInvalidoException(detalhe, caminhoArquivo, linhaArquivo, null);
    }

    public static ArquivoInvalidoException erroLeitura(String caminhoArquivo, Throwable causa) {
        return new ArquivoInvalidoException("Falha ao acessar arquivo", caminhoArquivo, null, null, causa);
    }
}
