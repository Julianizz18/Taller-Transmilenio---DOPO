package transmilenio.estrategia;

import transmilenio.*;

import java.util.*;

/**
 * Estrategia concreta: elige la ruta directa que minimiza el tiempo total
 * (espera + viaje). Si no hay ruta directa, lanza excepción.
 */
public class RutaMenorTiempo implements EstrategiaRuta {

    private final SistemaTransmilenio sistema;

    public RutaMenorTiempo(SistemaTransmilenio sistema) {
        this.sistema = sistema;
    }

    @Override
    public Ruta calcularRuta(Estacion origen, Estacion destino)
            throws TransmilenioException {

        Ruta mejor = null;
        double menorTiempo = Double.MAX_VALUE;

        for (Ruta ruta : sistema.getRutas()) {
            if (!ruta.pasaPor(origen) || !ruta.pasaPor(destino)) continue;

            // Tiempo = espera en origen + viaje + espera en destino
            double tiempoViaje = sistema.calcularTiempoViaje(origen, destino);
            double tiempo = origen.getTiempoEspera() + tiempoViaje + destino.getTiempoEspera();

            if (tiempo < menorTiempo) {
                menorTiempo = tiempo;
                mejor = ruta;
            }
        }

        if (mejor == null)
            throw new TransmilenioException(
                    String.format(TransmilenioException.SIN_RUTA_POSIBLE,
                            origen.getNombre(), destino.getNombre()));
        return mejor;
    }
}
