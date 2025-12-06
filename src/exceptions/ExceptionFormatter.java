package exceptions;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class ExceptionFormatter {
    private ExceptionFormatter() {
    }

    public static String resumo(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        String nome = throwable.getClass().getSimpleName();
        String mensagem = throwable.getMessage();
        if ((throwable instanceof DashboardDataException || throwable instanceof ArquivoInvalidoException)
                && mensagem != null && !mensagem.trim().isEmpty()) {
            return mensagem.trim();
        }
        if (mensagem == null || mensagem.trim().isEmpty()) {
            return nome;
        }
        return nome + ": " + mensagem.trim();
    }

    public static String detalhar(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        if (throwable instanceof ArquivoInvalidoException) {
            return detalhar((ArquivoInvalidoException) throwable);
        }
        StringBuilder sb = new StringBuilder(resumo(throwable));
        Throwable causa = throwable.getCause();
        if (causa != null) {
            sb.append(" | causa: ").append(resumo(causa));
        }
        return sb.toString();
    }

    public static String detalhar(ArquivoInvalidoException ex) {
        if (ex == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(resumo(ex));
        String arquivoResumido = resumirCaminho(ex.getCaminhoArquivo());
        if (arquivoResumido != null && !arquivoResumido.isEmpty()) {
            sb.append(" | arquivo=").append(arquivoResumido);
        }
        if (ex.getLinhaArquivo() != null && ex.getLinhaArquivo() > 0) {
            sb.append(" | linha=").append(ex.getLinhaArquivo());
        }
        if (ex.getConteudoLinha() != null && !ex.getConteudoLinha().isEmpty()) {
            sb.append(" | trecho=").append('"').append(ex.getConteudoLinha()).append('"');
        }
        Throwable causa = ex.getCause();
        if (causa != null) {
            sb.append(" | causa: ").append(resumo(causa));
        }
        return sb.toString();
    }

    public static Throwable causaRaiz(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable atual = throwable;
        while (atual.getCause() != null && atual.getCause() != atual) {
            atual = atual.getCause();
        }
        return atual;
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
            // Mantem fallback textual abaixo
        }
        int barra = Math.max(caminhoArquivo.lastIndexOf('/'), caminhoArquivo.lastIndexOf('\\'));
        if (barra >= 0 && barra + 1 < caminhoArquivo.length()) {
            return caminhoArquivo.substring(barra + 1);
        }
        return caminhoArquivo.trim();
    }
}
