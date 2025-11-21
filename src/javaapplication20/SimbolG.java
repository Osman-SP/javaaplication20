package javaapplication20;

import java.util.Objects;

public class SimbolG {
    public String nombSimb;
    public int token;
    public boolean esTerminal;

    private static final SimbolG EPS = new SimbolG("epsilon", -1, true);
    private static final SimbolG DOL = new SimbolG("$", -1, true);

    public SimbolG() {}

    public SimbolG(String nomb, int token, boolean esTerminal){
        this.nombSimb = nomb;
        this.token = token;
        this.esTerminal = esTerminal; // <-- corregido
    }

    public SimbolG(String nombSimb) {
        this(nombSimb, -1, true);
    }

    public static SimbolG nonTerminal(String nombre) {
        return new SimbolG(nombre, -1, false);
    }

    public static SimbolG EPSILON() {
        return EPS;
    }

    public static SimbolG DOLAR() {
        return DOL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimbolG)) return false;
        SimbolG s = (SimbolG) o;
        return Objects.equals(this.nombSimb, s.nombSimb);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombSimb);
    }

    @Override
    public String toString() {
        return nombSimb;
    }
}
