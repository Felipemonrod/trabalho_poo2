package dto;

public record EstadoResumo(int id, String nome, String sigla) {
    @Override
    public String toString() {
        return sigla + " - " + nome;
    }
}
