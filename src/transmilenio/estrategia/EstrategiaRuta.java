package transmilenio.estrategia;

import transmilenio.Estacion;
import transmilenio.Ruta;
import transmilenio.TransmilenioException;

/**
 * Patrón Estrategia: interfaz para algoritmos de búsqueda de la mejor ruta.
 * Permite intercambiar el criterio (menor tiempo, menor paradas) sin
 * modificar SistemaTransmilenio.
 */
public interface EstrategiaRuta {
    Ruta calcularRuta(Estacion origen, Estacion destino) throws TransmilenioException;
}
