package model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class Regiao {
    private String codigo;
    private String nome;
    private double areaKm2;
    private LinkedList<Populacao> historico = new LinkedList<>();

    public Regiao() {}

    public Regiao(String codigo, String nome, double areaKm2) {
        this.codigo = codigo;
        this.nome = nome;
        this.areaKm2 = areaKm2;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getAreaKm2() {
        return areaKm2;
    }

    public void setAreaKm2(double areaKm2) {
        this.areaKm2 = areaKm2;
    }

    public void addPopulacao(Populacao p) {
        if (p != null) {
            historico.add(p);
        }
    }

    public List<Populacao> getHistorico() {
        return Collections.unmodifiableList(historico);
    }

    public Populacao getUltimaPopulacao() {
        return historico.isEmpty() ? null : historico.getLast();
    }

    public Populacao getPenultimaPopulacao() {
        return historico.size() < 2 ? null : historico.get(historico.size() - 2);
    }

    public abstract String getTipo();

    @Override
    public String toString() {
        return getTipo() + "{" + "codigo='" + codigo + '\'' + ", nome='" + nome + '\'' + ", areaKm2=" + areaKm2 + '}';
    }
}
