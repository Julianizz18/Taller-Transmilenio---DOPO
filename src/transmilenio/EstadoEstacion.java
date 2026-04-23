package transmilenio;

/**
 * Patrón Estado: interfaz que representa el estado de ocupación de una estación.
 * Cada implementación encapsula el comportamiento del tiempo de espera.
 */
public interface EstadoEstacion {
    int calcularTiempoDeEspera();
}
