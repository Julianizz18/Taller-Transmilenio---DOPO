package transmilenio;

import java.io.Serializable;
import java.util.*;

/**
 * Corredor lineal del sistema TransMilenio.
 * Usa ArrayList para tramos y estaciones (acceso por índice necesario
 * en calcularTiempoEntre).
 */
public class Troncal implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final double velocidadPromedio; // metros/minuto
    private final ArrayList<Estacion> estaciones;
    private final ArrayList<Tramo> tramos;

    public Troncal(String nombre, double velocidadPromedio) {
        Objects.requireNonNull(nombre);
        if (velocidadPromedio <= 0)
            throw new IllegalArgumentException("La velocidad debe ser positiva");
        this.nombre = nombre;
        this.velocidadPromedio = velocidadPromedio;
        this.estaciones = new ArrayList<>();
        this.tramos = new ArrayList<>();
    }

    public void agregarEstacion(Estacion e) {
        Objects.requireNonNull(e);
        estaciones.add(e);
    }

    public void agregarTramo(Tramo t) {
        Objects.requireNonNull(t);
        tramos.add(t);
    }

    /**
     * Calcula el tiempo de viaje entre dos estaciones de esta troncal.
     * Suma las distancias de los tramos intermedios y divide por la velocidad.
     *
     * @return tiempo en minutos, o -1 si alguna estación no está en la troncal
     */
    public double calcularTiempoEntre(Estacion e1, Estacion e2) {
        int io = indexOfEstacion(e1.getNombre());
        int id = indexOfEstacion(e2.getNombre());
        if (io == -1 || id == -1) return -1;

        int ini = Math.min(io, id);
        int fin = Math.max(io, id);
        double dist = 0;
        for (int i = ini; i < fin && i < tramos.size(); i++) {
            dist += tramos.get(i).getDistancia();
        }
        return dist / velocidadPromedio;
    }

    private int indexOfEstacion(String nombre) {
        for (int i = 0; i < estaciones.size(); i++) {
            if (estaciones.get(i).getNombre().equalsIgnoreCase(nombre)) return i;
        }
        return -1;
    }

    public String getNombre() { return nombre; }
    public double getVelocidadPromedio() { return velocidadPromedio; }
    public ArrayList<Estacion> getEstaciones() { return estaciones; }
    public ArrayList<Tramo> getTramos() { return tramos; }

    @Override
    public String toString() {
        return String.format("Troncal{nombre='%s', vel=%.1f m/min, estaciones=%d}",
                nombre, velocidadPromedio, estaciones.size());
    }
}
