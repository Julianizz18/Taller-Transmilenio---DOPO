package transmilenio;

import java.io.Serializable;
import java.util.*;

public class Troncal implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final double velocidadPromedio;
    private final LinkedList<Tramo> tramos;
    private final LinkedList<Estacion> estaciones;

    public Troncal(String nombre, double velocidadPromedio) {
        Objects.requireNonNull(nombre);
        if (velocidadPromedio <= 0) throw new IllegalArgumentException("La velocidad debe ser positiva");
        this.nombre = nombre;
        this.velocidadPromedio = velocidadPromedio;
        this.tramos = new LinkedList<>();
        this.estaciones = new LinkedList<>();
    }

    public void agregarTramo(Tramo tramo) {
        Objects.requireNonNull(tramo);
        if (tramos.isEmpty()) {
            estaciones.add(tramo.getOrigen());
        }
        estaciones.add(tramo.getDestino());
        tramos.add(tramo);
    }

    public double tiempoRecorrido(double distanciaMetros) {
        return distanciaMetros / velocidadPromedio;
    }

    public String getNombre() { return nombre; }
    public double getVelocidadPromedio() { return velocidadPromedio; }
    public List<Tramo> getTramos() { return Collections.unmodifiableList(tramos); }
    public List<Estacion> getEstaciones() { return Collections.unmodifiableList(estaciones); }

    @Override
    public String toString() {
        return String.format("Troncal{nombre='%s', vel=%.1f m/min, estaciones=%d}",
                nombre, velocidadPromedio, estaciones.size());
    }
}