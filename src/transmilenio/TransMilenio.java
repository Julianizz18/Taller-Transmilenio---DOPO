package transmilenio;

import transmilenio.estrategia.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class TransMilenio {

    private final Map<String, Estacion> estaciones = new HashMap<>();
    private final TreeMap<String, Ruta> rutas = new TreeMap<>();
    private final LinkedHashMap<String, Troncal> troncales = new LinkedHashMap<>();
    private EstrategiaRecorrido estrategiaRecorrido;

    public TransMilenio() {
        this.estrategiaRecorrido = new EstrategiaMenorTiempo(this);
    }

    // ── Carga del modelo ──────────────────────────────────────────────────────

    public void agregarEstacion(Estacion estacion) {
        estaciones.put(estacion.getNombre().toLowerCase(), estacion);
    }

    public void agregarRuta(Ruta ruta) {
        rutas.put(ruta.getNombre().toLowerCase(), ruta);
    }

    public void agregarTroncal(Troncal troncal) {
        troncales.put(troncal.getNombre().toLowerCase(), troncal);
    }

    public void setEstrategiaRecorrido(EstrategiaRecorrido estrategia) {
        Objects.requireNonNull(estrategia);
        this.estrategiaRecorrido = estrategia;
    }

    // ── Servicio 1 ────────────────────────────────────────────────────────────

    public int tiempoEsperaEstacion(String nombreEstacion) throws TransMilenioException {
        return buscarEstacion(nombreEstacion).getTiempoEspera();
    }

    // ── Servicio 2 ────────────────────────────────────────────────────────────

    public List<String> nombresRutasOrdenadas() {
        return rutas.values().stream()
                .map(Ruta::getNombre)
                .collect(Collectors.toUnmodifiableList());
    }

    // ── Servicio 3 ────────────────────────────────────────────────────────────

    public int numeroPararadas(String nombreRuta, String nombreOrigen, String nombreDestino)
            throws TransMilenioException {
        return buscarRuta(nombreRuta).numeroPararadas(nombreOrigen, nombreDestino);
    }

    // ── Servicio 4 ────────────────────────────────────────────────────────────

    public List<String> rutasDirectas(String nombreOrigen, String nombreDestino)
            throws TransMilenioException {
        buscarEstacion(nombreOrigen);
        buscarEstacion(nombreDestino);

        List<Ruta> directas = rutas.values().stream()
                .filter(r -> r.conecta(nombreOrigen, nombreDestino))
                .collect(Collectors.toList());

        if (directas.isEmpty())
            throw new TransMilenioException(
                    String.format(TransMilenioException.SIN_RUTA_DIRECTA, nombreOrigen, nombreDestino));

        directas.sort(Comparator
                .comparingInt((Ruta r) -> {
                    try { return r.numeroPararadas(nombreOrigen, nombreDestino); }
                    catch (TransMilenioException e) { return Integer.MAX_VALUE; }
                })
                .thenComparing(Ruta::getNombre, String.CASE_INSENSITIVE_ORDER));

        return directas.stream().map(Ruta::getNombre).collect(Collectors.toList());
    }

    // ── Servicio 5 ────────────────────────────────────────────────────────────

    public List<String> rutasConTransbordo(String nombreOrigen, String nombreDestino)
            throws TransMilenioException {
        buscarEstacion(nombreOrigen);
        buscarEstacion(nombreDestino);

        List<String> resultado = new ArrayList<>();

        for (Ruta rutaA : rutas.values()) {
            if (!contieneEstacion(rutaA, nombreOrigen)) continue;
            for (Estacion transbordo : rutaA.getEstaciones()) {
                String nomT = transbordo.getNombre();
                if (nomT.equalsIgnoreCase(nombreOrigen)) continue;
                for (Ruta rutaB : rutas.values()) {
                    if (rutaB.equals(rutaA)) continue;
                    if (!rutaB.conecta(nomT, nombreDestino)) continue;
                    if (rutaA.conecta(nombreOrigen, nombreDestino)) continue;
                    try {
                        int p1 = rutaA.numeroPararadas(nombreOrigen, nomT);
                        int p2 = rutaB.numeroPararadas(nomT, nombreDestino);
                        resultado.add(rutaA.getNombre() + " → " + rutaB.getNombre()
                                + "  (" + (p1 + p2) + " paradas)");
                    } catch (TransMilenioException ignored) {}
                }
            }
        }

        if (resultado.isEmpty())
            throw new TransMilenioException(
                    String.format(TransMilenioException.SIN_RUTA_CON_TRANSBORDO,
                            nombreOrigen, nombreDestino));

        Collections.sort(resultado);
        return resultado.stream().distinct().collect(Collectors.toList());
    }

    // ── Servicio 6 ────────────────────────────────────────────────────────────

    public double tiempoRecorridoPlan(List<PasoRuta> plan) throws TransMilenioException {
        if (plan == null || plan.size() < 2)
            throw new TransMilenioException(
                    String.format(TransMilenioException.PLAN_INVALIDO,
                            "debe tener al menos 2 pasos"));
        if (plan.get(plan.size() - 1).getRuta() != null)
            throw new TransMilenioException(
                    String.format(TransMilenioException.PLAN_INVALIDO,
                            "el último paso debe tener ruta null"));

        double total = 0.0;
        for (int i = 0; i < plan.size() - 1; i++) {
            PasoRuta paso = plan.get(i);
            PasoRuta siguiente = plan.get(i + 1);
            if (paso.getRuta() == null)
                throw new TransMilenioException(
                        String.format(TransMilenioException.PLAN_INVALIDO,
                                "paso " + i + " tiene ruta null pero no es el último"));
            total += buscarEstacion(paso.getNombreEstacion()).getTiempoEspera();
            total += calcularTiempoViaje(paso.getRuta(),
                    paso.getNombreEstacion(), siguiente.getNombreEstacion());
        }
        total += buscarEstacion(plan.get(plan.size() - 1).getNombreEstacion()).getTiempoEspera();
        return total;
    }

    // ── Servicio 7 ────────────────────────────────────────────────────────────

    public List<PasoRuta> mejorPlanRecorrido(String nombreOrigen, String nombreDestino)
            throws TransMilenioException {
        return estrategiaRecorrido.calcularMejorPlan(nombreOrigen, nombreDestino);
    }
}