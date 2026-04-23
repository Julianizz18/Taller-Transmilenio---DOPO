package transmilenio.estrategia;

import transmilenio.TransMilenioException;
import java.util.List;

public interface EstrategiaRecorrido {

    List<PasoRuta> calcularMejorPlan(String nombreOrigen, String nombreDestino)
            throws TransMilenioException;
}