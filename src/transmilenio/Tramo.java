package transmilenio;

import java.io.Serializable;
import java.util.Objects;

public class Tramo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Estacion origen;
    private final Estacion destino;
    private final double distancia;

    public Tramo(Estacion origen, Estacion destino, double distancia) {
        Objects.requireNonNull(origen);
        Objects.requireNonNull(destino);
        if (distancia <= 0) throw new IllegalArgumentException("La distancia debe ser positiva");
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
    }

    public Estacion getOrigen() { return origen; }
    public Estacion getDestino() { return destino; }
    public double getDistancia() { return distancia; }

    @Override
    public String toString() {
        return String.format("Tramo{%s -> %s, %.1f m}",
                origen.getNombre(), destino.getNombre(), distancia);
    }
}