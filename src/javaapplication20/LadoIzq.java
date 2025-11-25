package javaapplication20;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LadoIzq {
    public SimbolG simIzq;
    public List<SimbolG> ladoDerecho;
    public int id;

    public LadoIzq() {
        this.simIzq = null;
        this.ladoDerecho = new ArrayList<>();
    }

    public LadoIzq(SimbolG simIzq, List<SimbolG> ladoDerecho) {
        if (simIzq == null) throw new IllegalArgumentException("simIzq no puede ser null");
        this.simIzq = simIzq;
        this.ladoDerecho = new ArrayList<>();
        if (ladoDerecho != null) this.ladoDerecho.addAll(ladoDerecho);
    }

    public LadoIzq(SimbolG simIzq) {
        this(simIzq, new ArrayList<>());
    }

    public void addAlFinal(SimbolG s) {
        ladoDerecho.add(s);
    }

    public void addAlInicio(SimbolG s) {
        ladoDerecho.add(0, s);
    }

    public boolean isLadoDerechoVacio() {
        return ladoDerecho.isEmpty();
    }

    public SimbolG primeraPosicion() {
        return ladoDerecho.isEmpty() ? null : ladoDerecho.get(0);
    }

    public int size() {
        return ladoDerecho.size();
    }

    @Override
    public String toString() {
        return (simIzq == null ? "null" : simIzq.nombSimb) + " -> " + ladoDerecho;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LadoIzq)) return false;
        LadoIzq l = (LadoIzq) o;
        return Objects.equals(simIzq, l.simIzq) &&
               Objects.equals(ladoDerecho, l.ladoDerecho);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simIzq, ladoDerecho);
    }
}
