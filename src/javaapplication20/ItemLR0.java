package javaapplication20;

/**
 *
 * @author nsofi
 */
public class ItemLR0 {
    public int numRegla;
    public int posPunto;

    public ItemLR0() {
        this.numRegla = -1;
        this.posPunto = -1;
    }

    public ItemLR0(int numRegla, int posPunto) {
        this.numRegla = numRegla;
        this.posPunto = posPunto;
    }

    // Para permitir que Java use igualdad nativa correctamente
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemLR0 item = (ItemLR0) o;
        return numRegla == item.numRegla && posPunto == item.posPunto;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(numRegla, posPunto);
    }
}
