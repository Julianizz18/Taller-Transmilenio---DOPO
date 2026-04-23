package transmilenio;

public class TransmilenioException extends Exception {

    public static final String ESTACION_NO_ENCONTRADA =
            "La estación '%s' no existe en el sistema.";
    public static final String RUTA_NO_ENCONTRADA =
            "La ruta '%s' no existe en el sistema.";
    public static final String TRONCAL_NO_ENCONTRADA =
            "La troncal '%s' no existe en el sistema.";
    public static final String ESTACION_NO_EN_RUTA =
            "La estación '%s' no pertenece a la ruta '%s'.";
    public static final String SIN_RUTA_DIRECTA =
            "No existe ruta directa entre '%s' y '%s'.";
    public static final String SIN_RUTA_CON_TRANSBORDO =
            "No existe ruta con transbordo entre '%s' y '%s'.";
    public static final String SIN_RUTA_POSIBLE =
            "No existe ninguna ruta posible entre '%s' y '%s'.";
    public static final String ARCHIVO_NO_ENCONTRADO =
            "El archivo '%s' no fue encontrado.";
    public static final String ARCHIVO_FORMATO_INVALIDO =
            "El archivo '%s' tiene formato inválido: %s";
    public static final String RUTA_YA_EXISTE =
            "La ruta '%s' ya existe en el sistema.";
    public static final String PLAN_INVALIDO =
            "El plan de ruta es inválido: %s";

    public TransmilenioException(String message) {
        super(message);
    }

    public TransmilenioException(String message, Throwable cause) {
        super(message, cause);
    }
}
