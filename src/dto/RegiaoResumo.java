package dto;

public record RegiaoResumo(int id, String nome) {
    @Override
    public String toString() {
        return nome;
    }
}
