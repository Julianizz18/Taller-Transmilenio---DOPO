package transmilenio.estrategia;

import transmilenio.Ruta;

public class PasoRuta {

    private final String nombreEstacion;
    private final Ruta ruta;

    public PasoRuta(String nombreEstacion, Ruta ruta) {
        this.nombreEstacion = nombreEstacion;
        this.ruta = ruta;
    }

    public String getNombreEstacion() { return nombreEstacion; }
    public Ruta getRuta() { return ruta; }

    @Override
    public String toString() {
        return String.format("{estacion='%s', ruta=%s}",
                nombreEstacion, ruta != null ? ruta.getNombre() : "null");
    }
}