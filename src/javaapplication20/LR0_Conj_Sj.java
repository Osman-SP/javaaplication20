package javaapplication20;

import java.util.HashSet;

/**
 *
 * @author nsofi
 */
public class LR0_Conj_Sj {
    public int j;                   // número del estado LR(0)
    public HashSet<ItemLR0> Sj;     // conjunto de ítems del estado

    public LR0_Conj_Sj() {
        this.j = -1;
        this.Sj = new HashSet<>();  // sin comparador en Java
    }

    public LR0_Conj_Sj(int j) {
        this.j = j;
        this.Sj = new HashSet<>();
    }

    // Para agregar items
    public void agregarItem(ItemLR0 item) {
        Sj.add(item);
    }

    // Para verificar si un item ya existe
    public boolean contiene(ItemLR0 item) {
        return Sj.contains(item);
    }

    // Para obtener un nombre bonito del conjunto
    public String toString() {
        return "Estado " + j + ": " + Sj.toString();
    }
}
