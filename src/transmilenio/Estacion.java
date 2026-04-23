package transmilenio;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa una estación del sistema TransMilenio.
 * Delega el cálculo del tiempo de espera a su EstadoEstacion actual
 * (Patrón Estado): cambiar el estado cambia el comportamiento.
 */
public class Estacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nombre;
    private EstadoEstacion estado;

    public Estacion(String nombre, EstadoEstacion estado) {
        Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        Objects.requireNonNull(estado, "El estado no puede ser nulo");
        this.nombre = nombre;
        this.estado = estado;
    }

    /** Servicio 1: delega al estado actual (Patrón Estado). */
    public int getTiempoEspera() {
        return estado.calcularTiempoDeEspera();
    }

    /** Transición de estado. */
    public void setEstado(EstadoEstacion estado) {
        Objects.requireNonNull(estado);
        this.estado = estado;
    }

    public String getNombre() { return nombre; }
    public EstadoEstacion getEstado() { return estado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Estacion)) return false;
        return nombre.equalsIgnoreCase(((Estacion) o).nombre);
    }

    @Override
    public int hashCode() {
        return nombre.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Estacion{nombre='%s', estado=%s, espera=%d min}",
                nombre, estado, estado.calcularTiempoDeEspera());
    }
}
