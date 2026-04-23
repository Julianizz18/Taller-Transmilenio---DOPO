package transmilenio;

public class EstadoMedio implements EstadoEstacion {

    @Override
    public int calcularTiempoDeEspera() {
        return 5; // minutos
    }

    @Override
    public String toString() {
        return "MEDIO";
    }
}
