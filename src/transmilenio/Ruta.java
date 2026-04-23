package transmilenio;

import java.io.Serializable;
import java.util.*;

/**
 * Ruta del sistema TransMilenio.
 * Mantiene la lista ordenada de estaciones donde para.
 */
public class Ruta implements Serializable, Comparable<Ruta> {

    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final ArrayList<Estacion> estaciones;

    public Ruta(String nombre) {
        Objects.requireNonNull(nombre);
        this.nombre = nombre;
        this.estaciones = new ArrayList<>();
    }

    public void agregarEstacion(Estacion e) {
        Objects.requireNonNull(e);
        estaciones.add(e);
    }

    /** Retorna true si la ruta para en la estación dada. */
    public boolean pasaPor(Estacion e) {
        return estaciones.stream()
                .anyMatch(est -> est.getNombre().equalsIgnoreCase(e.getNombre()));
    }

    /**
     * Número de paradas (tramos) entre origen y destino en esta ruta.
     * @throws TransmilenioException si alguna estación no está en la ruta
     */
    public int numeroParadas(Estacion origen, Estacion destino)
            throws TransmilenioException {
        int io = indexOf(origen.getNombre());
        int id = indexOf(destino.getNombre());
        if (io == -1)
            throw new TransmilenioException(
                    String.format(TransmilenioException.ESTACION_NO_EN_RUTA,
                            origen.getNombre(), nombre));
        if (id == -1)
            throw new TransmilenioException(
                    String.format(TransmilenioException.ESTACION_NO_EN_RUTA,
                            destino.getNombre(), nombre));
        return Math.abs(id - io);
    }

    private int indexOf(String nombre) {
        for (int i = 0; i < estaciones.size(); i++) {
            if (estaciones.get(i).getNombre().equalsIgnoreCase(nombre)) return i;
        }
        return -1;
    }

    public String getNombre() { return nombre; }
    public ArrayList<Estacion> getEstaciones() { return estaciones; }

    @Override
    public int compareTo(Ruta otra) {
        return this.nombre.compareToIgnoreCase(otra.nombre);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ruta)) return false;
        return nombre.equalsIgnoreCase(((Ruta) o).nombre);
    }

    @Override
    public int hashCode() { return nombre.toLowerCase().hashCode(); }

    @Override
    public String toString() {
        return String.format("Ruta{nombre='%s', paradas=%d}", nombre, estaciones.size());
    }
}
