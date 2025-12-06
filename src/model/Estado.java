package model;

import java.util.ArrayList;
import java.util.List;

public class Estado extends Regiao {
    private String uf;
    private List<Municipio> municipios = new ArrayList<>();

    public Estado(String codigo, String nome, double areaKm2, String uf) {
        super(codigo, nome, areaKm2);
        this.uf = uf;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public List<Municipio> getMunicipios() {
        return municipios;
    }

    public void addMunicipio(Municipio m) {
        if (m == null) return;
        municipios.add(m);
        m.setEstado(this); // composição/associação
    }

    public long getPopulacaoTotalUltima() {
        long total = 0;
        if (!municipios.isEmpty()) {
            for (Municipio m : municipios) {
                Populacao p = m.getUltimaPopulacao();
                if (p != null) total += p.getHabitantes();
            }
            return total;
        }
        Populacao p = getUltimaPopulacao();
        return p == null ? 0 : p.getHabitantes();
    }

    @Override
    public String getTipo() {
        return "Estado";
    }

    @Override
    public String toString() {
        return super.toString() + "[uf='" + uf + "', municipios=" + municipios.size() + "]";
    }
}
