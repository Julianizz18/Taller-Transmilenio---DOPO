package transmilenio;

import java.io.Serializable;
import java.util.*;

public class Ruta implements Serializable, Comparable<Ruta> {

    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final ArrayList<Estacion> estaciones;

    public Ruta(String nombre) {
        Objects.requireNonNull(nombre);
        this.nombre = nombre;
        this.estaciones = new ArrayList<>();
    }

    public void agregarEstacion(Estacion estacion) {
        Objects.requireNonNull(estacion);
        estaciones.add(estacion);
    }

    public int numeroPararadas(String nombreOrigen, String nombreDestino)
            throws TransMilenioException {
        int idxOrigen = indexOf(nombreOrigen);
        int idxDestino = indexOf(nombreDestino);
        if (idxOrigen == -1)
            throw new TransMilenioException(
                    String.format(TransMilenioException.ESTACION_NO_EN_RUTA, nombreOrigen, nombre));
        if (idxDestino == -1)
            throw new TransMilenioException(
                    String.format(TransMilenioException.ESTACION_NO_EN_RUTA, nombreDestino, nombre));
        return Math.abs(idxDestino - idxOrigen);
    }

    public boolean conecta(String nombreOrigen, String nombreDestino) {
        return indexOf(nombreOrigen) != -1 && indexOf(nombreDestino) != -1;
    }

    private int indexOf(String nombre) {
        for (int i = 0; i < estaciones.size(); i++) {
            if (estaciones.get(i).getNombre().equalsIgnoreCase(nombre)) return i;
        }
        return -1;
    }

    public String getNombre() { return nombre; }
    public List<Estacion> getEstaciones() { return Collections.unmodifiableList(estaciones); }

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