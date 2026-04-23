package transmilenio;

import transmilenio.estrategia.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Contexto principal del sistema TransMilenio.
 *
 * Colecciones:
 *  - troncales : ArrayList<Troncal>          → orden de inserción, iteración secuencial
 *  - rutas     : TreeSet<Ruta>               → siempre ordenadas alfabéticamente (Servicio 2)
 *  - estaciones: HashMap<String, Estacion>   → búsqueda O(1) por nombre
 *
 * Patrón Estrategia: mejorRuta() delega en EstrategiaRuta intercambiable.
 */
public class SistemaTransmilenio {

    private final ArrayList<Troncal> troncales = new ArrayList<>();
    private final TreeSet<Ruta> rutas = new TreeSet<>();
    private final HashMap<String, Estacion> estaciones = new HashMap<>();
    private EstrategiaRuta estrategia;

    public SistemaTransmilenio() {
        this.estrategia = new RutaMenorTiempo(this);
    }

    // ── Carga del modelo ──────────────────────────────────────────────────────

    public void agregarEstacion(Estacion e) {
        estaciones.put(e.getNombre().toLowerCase(), e);
    }

    public void agregarRuta(Ruta r) {
        rutas.add(r);
    }

    public void agregarTroncal(Troncal t) {
        troncales.add(t);
    }

    public void setEstrategia(EstrategiaRuta e) {
        Objects.requireNonNull(e);
        this.estrategia = e;
    }

    // ── Servicio 1 ─ Tiempo de espera ─────────────────────────────────────────

    public int getTiempoEspera(String nombreEstacion) throws TransmilenioException {
        return buscarEstacion(nombreEstacion).getTiempoEspera();
    }

    // ── Servicio 2 ─ Rutas ordenadas alfabéticamente ──────────────────────────

    public TreeSet<Ruta> getRutasOrdenadas() {
        return rutas; // TreeSet ya está ordenado
    }

    // ── Servicio 3 ─ Número de paradas en una ruta dada ───────────────────────

    public int numeroParadas(String nombreRuta, String origen, String destino)
            throws TransmilenioException {
        Ruta ruta = buscarRuta(nombreRuta);
        Estacion eOrigen = buscarEstacion(origen);
        Estacion eDestino = buscarEstacion(destino);
        return ruta.numeroParadas(eOrigen, eDestino);
    }

    // ── Servicio 4 ─ Rutas directas (sin transbordo) ─────────────────────────

    public List<Ruta> rutasSinTransbordo(String origen, String destino)
            throws TransmilenioException {
        Estacion eOrigen = buscarEstacion(origen);
        Estacion eDestino = buscarEstacion(destino);

        List<Ruta> directas = rutas.stream()
                .filter(r -> r.pasaPor(eOrigen) && r.pasaPor(eDestino))
                .sorted(Comparator
                        .comparingInt((Ruta r) -> {
                            try { return r.numeroParadas(eOrigen, eDestino); }
                            catch (TransmilenioException ex) { return Integer.MAX_VALUE; }
                        })
                        .thenComparing(Ruta::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        if (directas.isEmpty())
            throw new TransmilenioException(
                    String.format(TransmilenioException.SIN_RUTA_DIRECTA, origen, destino));

        return directas;
    }

    // ── Servicio 5 ─ Rutas con transbordo ────────────────────────────────────

    public List<Ruta> rutasConTransbordo(String origen, String destino)
            throws TransmilenioException {
        Estacion eOrigen = buscarEstacion(origen);
        Estacion eDestino = buscarEstacion(destino);

        // Recolectar pares (rutaA, rutaB) que se conectan por una estación intermedia
        record Par(Ruta r1, Ruta r2, int totalParadas) {}
        List<Par> pares = new ArrayList<>();

        for (Ruta rutaA : rutas) {
            if (!rutaA.pasaPor(eOrigen)) continue;
            for (Estacion transbordo : rutaA.getEstaciones()) {
                if (transbordo.equals(eOrigen)) continue;
                for (Ruta rutaB : rutas) {
                    if (rutaB.equals(rutaA)) continue;
                    if (!rutaB.pasaPor(transbordo) || !rutaB.pasaPor(eDestino)) continue;
                    if (rutaA.pasaPor(eDestino)) continue; // ya tiene ruta directa
                    try {
                        int p1 = rutaA.numeroParadas(eOrigen, transbordo);
                        int p2 = rutaB.numeroParadas(transbordo, eDestino);
                        pares.add(new Par(rutaA, rutaB, p1 + p2));
                    } catch (TransmilenioException ignored) {}
                }
            }
        }

        if (pares.isEmpty())
            throw new TransmilenioException(
                    String.format(TransmilenioException.SIN_RUTA_CON_TRANSBORDO, origen, destino));

        pares.sort(Comparator.comparingInt(Par::totalParadas)
                .thenComparing(p -> p.r1().getNombre(), String.CASE_INSENSITIVE_ORDER));

        // Retornamos las rutas de la primera etapa (sin duplicados), coherente con el diagrama
        List<Ruta> resultado = new ArrayList<>();
        for (Par p : pares) {
            if (!resultado.contains(p.r1())) resultado.add(p.r1());
            if (!resultado.contains(p.r2())) resultado.add(p.r2());
        }
        return resultado;
    }

    // ── Servicio 6 ─ Tiempo de recorrido de un plan ───────────────────────────

    /**
     * Plan: lista de pares {nombreEstacion, nombreRuta} donde el último tiene ruta null.
     * Ejemplo: [{"A1","Ruta1"},{"A2","Ruta1"},{"A3",null}]
     */
    public double calcularTiempoRecorrido(List<String[]> plan) throws TransmilenioException {
        if (plan == null || plan.size() < 2)
            throw new TransmilenioException(
                    String.format(TransmilenioException.PLAN_INVALIDO,
                            "debe tener al menos 2 pasos"));

        String[] ultimo = plan.get(plan.size() - 1);
        if (ultimo[1] != null)
            throw new TransmilenioException(
                    String.format(TransmilenioException.PLAN_INVALIDO,
                            "el último paso debe tener ruta null"));

        double total = 0.0;
        for (int i = 0; i < plan.size() - 1; i++) {
            String[] paso = plan.get(i);
            String[] siguiente = plan.get(i + 1);

            if (paso[1] == null)
                throw new TransmilenioException(
                        String.format(TransmilenioException.PLAN_INVALIDO,
                                "el paso " + i + " tiene ruta null pero no es el último"));

            Estacion estActual = buscarEstacion(paso[0]);
            Estacion estSiguiente = buscarEstacion(siguiente[0]);

            total += estActual.getTiempoEspera();
            total += calcularTiempoViaje(estActual, estSiguiente);
        }
        // Tiempo de espera en el destino final
        total += buscarEstacion(ultimo[0]).getTiempoEspera();
        return total;
    }

    // ── Servicio 7 ─ Mejor ruta (Patrón Estrategia) ──────────────────────────

    public Ruta mejorRuta(String origen, String destino) throws TransmilenioException {
        Estacion eOrigen = buscarEstacion(origen);
        Estacion eDestino = buscarEstacion(destino);
        return estrategia.calcularRuta(eOrigen, eDestino);
    }

    // ── Persistencia 1 ─ Importar ruta desde archivo ─────────────────────────

    public void importarRuta(String rutaArchivo) throws TransmilenioException {
        Path path = Path.of(rutaArchivo);
        if (!Files.exists(path))
            throw new TransmilenioException(
                    String.format(TransmilenioException.ARCHIVO_NO_ENCONTRADO, rutaArchivo));
        try {
            List<String> lineas = Files.readAllLines(path);
            if (lineas.isEmpty())
                throw new TransmilenioException(
                        String.format(TransmilenioException.ARCHIVO_FORMATO_INVALIDO,
                                rutaArchivo, "el archivo está vacío"));

            String nombreRuta = lineas.get(0).trim();
            if (rutas.stream().anyMatch(r -> r.getNombre().equalsIgnoreCase(nombreRuta)))
                throw new TransmilenioException(
                        String.format(TransmilenioException.RUTA_YA_EXISTE, nombreRuta));

            Ruta nueva = new Ruta(nombreRuta);
            for (int i = 1; i < lineas.size(); i++) {
                String nom = lineas.get(i).trim();
                if (!nom.isBlank()) nueva.agregarEstacion(buscarEstacion(nom));
            }
            agregarRuta(nueva);
        } catch (IOException e) {
            throw new TransmilenioException(
                    String.format(TransmilenioException.ARCHIVO_NO_ENCONTRADO, rutaArchivo), e);
        }
    }

    // ── Persistencia 2 ─ Exportar rutas directas ─────────────────────────────

    public void exportarRutasDirectas(String origen, String destino, String rutaArchivo)
            throws TransmilenioException {
        List<Ruta> directas = rutasSinTransbordo(origen, destino);
        List<String> nombres = directas.stream()
                .map(Ruta::getNombre)
                .collect(Collectors.toList());
        try {
            Files.write(Path.of(rutaArchivo), nombres);
        } catch (IOException e) {
            throw new TransmilenioException("Error al escribir archivo: " + rutaArchivo, e);
        }
    }

    // ── Persistencia 3 ─ Salvar troncal (serialización) ──────────────────────

    public void salvarTroncal(String nombreTroncal, String rutaArchivo)
            throws TransmilenioException {
        Troncal troncal = buscarTroncal(nombreTroncal);
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(rutaArchivo))) {
            oos.writeObject(troncal);
        } catch (IOException e) {
            throw new TransmilenioException(
                    "Error al guardar la troncal en: " + rutaArchivo, e);
        }
    }

    public Troncal cargarTroncal(String rutaArchivo) throws TransmilenioException {
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(rutaArchivo))) {
            return (Troncal) ois.readObject();
        } catch (FileNotFoundException e) {
            throw new TransmilenioException(
                    String.format(TransmilenioException.ARCHIVO_NO_ENCONTRADO, rutaArchivo), e);
        } catch (IOException | ClassNotFoundException e) {
            throw new TransmilenioException(
                    String.format(TransmilenioException.ARCHIVO_FORMATO_INVALIDO,
                            rutaArchivo, e.getMessage()), e);
        }
    }

    // ── Helpers públicos (usados por las estrategias) ─────────────────────────

    public Estacion buscarEstacion(String nombre) throws TransmilenioException {
        Estacion e = estaciones.get(nombre.toLowerCase());
        if (e == null)
            throw new TransmilenioException(
                    String.format(TransmilenioException.ESTACION_NO_ENCONTRADA, nombre));
        return e;
    }

    public ArrayList<Troncal> getTroncales() { return troncales; }
    public TreeSet<Ruta> getRutas() { return rutas; }
    public HashMap<String, Estacion> getEstaciones() { return estaciones; }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private Ruta buscarRuta(String nombre) throws TransmilenioException {
        return rutas.stream()
                .filter(r -> r.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseThrow(() -> new TransmilenioException(
                        String.format(TransmilenioException.RUTA_NO_ENCONTRADA, nombre)));
    }

    private Troncal buscarTroncal(String nombre) throws TransmilenioException {
        return troncales.stream()
                .filter(t -> t.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseThrow(() -> new TransmilenioException(
                        String.format(TransmilenioException.TRONCAL_NO_ENCONTRADA, nombre)));
    }

    public double calcularTiempoViaje(Estacion origen, Estacion destino) {
        for (Troncal t : troncales) {
            double tiempo = t.calcularTiempoEntre(origen, destino);
            if (tiempo >= 0) return tiempo;
        }
        return 3.0; // fallback: 3 min por tramo desconocido
    }
}
