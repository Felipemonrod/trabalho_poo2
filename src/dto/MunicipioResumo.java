package dto;

public record MunicipioResumo(int id, String nome, int estadoId) {
    @Override
    public String toString() {
        return nome;
    }
}
