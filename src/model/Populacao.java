package model;

public class Populacao {
    private int ano;
    private long habitantes;

    public Populacao(int ano, long habitantes) {
        this.ano = ano;
        this.habitantes = habitantes;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public long getHabitantes() {
        return habitantes;
    }

    public void setHabitantes(long habitantes) {
        this.habitantes = habitantes;
    }

    @Override
    public String toString() {
        return "Populacao{" + "ano=" + ano + ", habitantes=" + habitantes + '}';
    }
}
