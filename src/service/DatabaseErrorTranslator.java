package service;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DatabaseErrorTranslator {
    private static final Map<String, String> DATA_TRUNCATION_HINTS = Map.of(
            "sigla", "Valor muito longo para o campo 'sigla'. Informe apenas 2 letras (ex: 'SP')."
    );

    private static final Pattern COLUMN_PATTERN = Pattern.compile("column '([^']+)'", Pattern.CASE_INSENSITIVE);
    private static final Pattern DUPLICATE_ENTRY_PATTERN = Pattern.compile("Duplicate entry '([^']+)' for key '([^']+)'", Pattern.CASE_INSENSITIVE);

    private DatabaseErrorTranslator() {
    }

    public static String traduzir(String contexto, SQLException e) {
        String causa = traduzirCausa(e);
        if (contexto == null || contexto.isBlank()) {
            return causa;
        }
        return contexto.trim() + ": " + causa;
    }

    private static String traduzirCausa(SQLException e) {
        if (e == null) {
            return "motivo desconhecido.";
        }
        String sqlState = e.getSQLState();
        String mensagem = e.getMessage() == null ? "" : e.getMessage();
        String lower = mensagem.toLowerCase(Locale.ROOT);

        if (sqlState != null && sqlState.startsWith("08")) {
            return "Não foi possível conectar ao banco. Verifique se o MySQL está em execução e as credenciais estão corretas.";
        }
        if ("28000".equals(sqlState)) {
            return "A autenticação no banco foi recusada. Confirme usuário e senha configurados.";
        }
        if (lower.contains("communications link failure")) {
            return "A conexão com o banco foi interrompida. Tente novamente em instantes.";
        }
        if ("23000".equals(sqlState) || lower.contains("foreign key") || lower.contains("duplicate entry")) {
            return traduzirRestricao(mensagem, lower);
        }
        if ("22001".equals(sqlState) || lower.contains("data truncation")) {
            return traduzirTruncamento(mensagem);
        }
        return mensagem.isBlank() ? "motivo desconhecido." : mensagem.trim();
    }

    private static String traduzirRestricao(String mensagemOriginal, String lowerMessage) {
        Matcher duplicate = DUPLICATE_ENTRY_PATTERN.matcher(mensagemOriginal);
        if (duplicate.find()) {
            String valor = duplicate.group(1);
            String chave = duplicate.group(2);
            return String.format("Valor '%s' já existe (chave %s). Ajuste o dado antes de importar novamente.", valor, chave);
        }
        if (lowerMessage.contains("foreign key")) {
            return "Referência inválida: o registro aponta para um item que não existe no banco (verifique o código do estado/município).";
        }
        if (lowerMessage.contains("cannot be null")) {
            return "Um campo obrigatório foi enviado vazio. Preencha todas as colunas necessárias no CSV.";
        }
        return "A operação violou uma restrição do banco. Revise os dados informados.";
    }

    private static String traduzirTruncamento(String mensagem) {
        String coluna = extrairColuna(mensagem);
        if (coluna != null) {
            String dica = DATA_TRUNCATION_HINTS.get(coluna.toLowerCase(Locale.ROOT));
            if (dica != null) {
                return dica;
            }
            return "Valor muito longo para a coluna '" + coluna + "'. Reduza o texto para caber no limite.";
        }
        return "Valor muito longo para algum campo da tabela. Ajuste o tamanho do texto no CSV.";
    }

    private static String extrairColuna(String mensagem) {
        if (mensagem == null) {
            return null;
        }
        Matcher matcher = COLUMN_PATTERN.matcher(mensagem);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
