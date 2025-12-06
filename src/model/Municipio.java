package model;

public class Municipio extends Regiao {
    private String codigoEstado;
    private Estado estado; // associação

    public Municipio(String codigo, String nome, double areaKm2, String codigoEstado) {
        super(codigo, nome, areaKm2);
        this.codigoEstado = codigoEstado;
    }

    public String getCodigoEstado() {
        return codigoEstado;
    }

    public void setCodigoEstado(String codigoEstado) {
        this.codigoEstado = codigoEstado;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    @Override
    public String getTipo() {
        return "Municipio";
    }

    @Override
    public String toString() {
        return super.toString() + "[codigoEstado='" + codigoEstado + "']";
    }
}
