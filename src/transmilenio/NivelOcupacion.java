package transmilenio;

public enum NivelOcupacion {
    ALTO(10),
    MEDIO(5),
    BAJO(2);

    private final int tiempoEspera;

    NivelOcupacion(int tiempoEspera) {
        this.tiempoEspera = tiempoEspera;
    }

    public int getTiempoEspera() {
        return tiempoEspera;
    }
}