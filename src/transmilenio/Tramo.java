package transmilenio;

import java.io.Serializable;
import java.util.Objects;

/**
 * Segmento entre dos estaciones consecutivas dentro de una troncal.
 */
public class Tramo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Estacion inicio;
    private final Estacion fin;
    private final double distancia; // metros

    public Tramo(Estacion inicio, Estacion fin, double distancia) {
        Objects.requireNonNull(inicio);
        Objects.requireNonNull(fin);
        if (distancia <= 0) throw new IllegalArgumentException("Distancia debe ser positiva");
        this.inicio = inicio;
        this.fin = fin;
        this.distancia = distancia;
    }

    public Estacion getInicio() { return inicio; }
    public Estacion getFin()    { return fin; }
    public double getDistancia(){ return distancia; }

    @Override
    public String toString() {
        return String.format("Tramo{%s -> %s, %.1f m}",
                inicio.getNombre(), fin.getNombre(), distancia);
    }
}
