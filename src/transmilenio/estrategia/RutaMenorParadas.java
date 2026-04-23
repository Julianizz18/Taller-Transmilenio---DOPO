package transmilenio.estrategia;

import transmilenio.*;

/**
 * Estrategia concreta: elige la ruta directa con menor número de paradas.
 */
public class RutaMenorParadas implements EstrategiaRuta {

    private final SistemaTransmilenio sistema;

    public RutaMenorParadas(SistemaTransmilenio sistema) {
        this.sistema = sistema;
    }

    @Override
    public Ruta calcularRuta(Estacion origen, Estacion destino)
            throws TransmilenioException {

        Ruta mejor = null;
        int menosParadas = Integer.MAX_VALUE;

        for (Ruta ruta : sistema.getRutas()) {
            if (!ruta.pasaPor(origen) || !ruta.pasaPor(destino)) continue;
            try {
                int paradas = ruta.numeroParadas(origen, destino);
                if (paradas < menosParadas) {
                    menosParadas = paradas;
                    mejor = ruta;
                }
            } catch (TransmilenioException ignored) {}
        }

        if (mejor == null)
            throw new TransmilenioException(
                    String.format(TransmilenioException.SIN_RUTA_POSIBLE,
                            origen.getNombre(), destino.getNombre()));
        return mejor;
    }
}
