package transmilenio;

public class EstadoAlto implements EstadoEstacion {

    @Override
    public int calcularTiempoDeEspera() {
        return 10; // minutos
    }

    @Override
    public String toString() {
        return "ALTO";
    }
}
