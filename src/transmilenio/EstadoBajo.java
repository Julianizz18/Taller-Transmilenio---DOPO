package transmilenio;

public class EstadoBajo implements EstadoEstacion {

    @Override
    public int calcularTiempoDeEspera() {
        return 2; // minutos
    }

    @Override
    public String toString() {
        return "BAJO";
    }
}
