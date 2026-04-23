package transmilenio;

import java.io.Serializable;
import java.util.Objects;

public class Estacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nombre;
    private NivelOcupacion nivelOcupacion;

    public Estacion(String nombre, NivelOcupacion nivelOcupacion) {
        Objects.requireNonNull(nombre);
        Objects.requireNonNull(nivelOcupacion);
        this.nombre = nombre;
        this.nivelOcupacion = nivelOcupacion;
    }

    public int getTiempoEspera() {
        return nivelOcupacion.getTiempoEspera();
    }

    public void setNivelOcupacion(NivelOcupacion nivelOcupacion) {
        Objects.requireNonNull(nivelOcupacion);
        this.nivelOcupacion = nivelOcupacion;
    }

    public String getNombre() { return nombre; }
    public NivelOcupacion getNivelOcupacion() { return nivelOcupacion; }

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
        return String.format("Estacion{nombre='%s', ocupacion=%s, espera=%d min}",
                nombre, nivelOcupacion, nivelOcupacion.getTiempoEspera());
    }
}