package transmilenio.estrategia;

import transmilenio.*;
import java.util.*;

public class EstrategiaMenorTiempo implements EstrategiaRecorrido {

    private final TransMilenio sistema;

    public EstrategiaMenorTiempo(TransMilenio sistema) {
        this.sistema = sistema;
    }

    @Override
    public List<PasoRuta> calcularMejorPlan(String nombreOrigen, String nombreDestino)
            throws TransMilenioException {

        sistema.buscarEstacion(nombreOrigen);
        sistema.buscarEstacion(nombreDestino);

        if (nombreOrigen.equalsIgnoreCase(nombreDestino)) {
            return List.of(new PasoRuta(nombreOrigen, null));
        }

        // Dijkstra
        Map<String, Double> distMin = new HashMap<>();
        Map<String, String> estacionPrev = new HashMap<>();
        Map<String, Ruta> rutaPrev = new HashMap<>();

        PriorityQueue<NodoCosto> pq =
                new PriorityQueue<>(Comparator.comparingDouble(n -> n.costo));
        pq.add(new NodoCosto(nombreOrigen, 0.0));
        distMin.put(nombreOrigen.toLowerCase(), 0.0);

        while (!pq.isEmpty()) {
            NodoCosto actual = pq.poll();
            if (actual.estacion.equalsIgnoreCase(nombreDestino)) break;

            double mejorConocido = distMin.getOrDefault(
                    actual.estacion.toLowerCase(), Double.MAX_VALUE);
            if (actual.costo > mejorConocido) continue;

            for (Ruta ruta : sistema.getRutas()) {
                List<Estacion> paradas = ruta.getEstaciones();
                int idxActual = indexOfIgnoreCase(paradas, actual.estacion);
                if (idxActual == -1) continue;

                for (int delta : new int[]{-1, 1}) {
                    int idxVecino = idxActual + delta;
                    if (idxVecino < 0 || idxVecino >= paradas.size()) continue;
                    String estVecino = paradas.get(idxVecino).getNombre();

                    double espera = sistema.buscarEstacionSilencioso(actual.estacion)
                            .map(e -> (double) e.getTiempoEspera()).orElse(5.0);
                    double viaje = tiempoViaje(ruta, actual.estacion, estVecino);
                    double nuevoCosto = actual.costo + espera + viaje;

                    String key = estVecino.toLowerCase();
                    if (nuevoCosto < distMin.getOrDefault(key, Double.MAX_VALUE)) {
                        distMin.put(key, nuevoCosto);
                        estacionPrev.put(key, actual.estacion);
                        rutaPrev.put(key, ruta);
                        pq.add(new NodoCosto(estVecino, nuevoCosto));
                    }
                }
            }
        }

        String keyDest = nombreDestino.toLowerCase();
        if (!distMin.containsKey(keyDest))
            throw new TransMilenioException(
                    String.format(TransMilenioException.SIN_PLAN_POSIBLE,
                            nombreOrigen, nombreDestino));

        // Reconstruir camino
        LinkedList<PasoRuta> plan = new LinkedList<>();
        plan.addFirst(new PasoRuta(nombreDestino, null));
        String cursor = nombreDestino;
        while (!cursor.equalsIgnoreCase(nombreOrigen)) {
            String key = cursor.toLowerCase();
            plan.addFirst(new PasoRuta(estacionPrev.get(key), rutaPrev.get(key)));
            cursor = estacionPrev.get(key);
        }
        return plan;
    }

    private double tiempoViaje(Ruta ruta, String origen, String destino) {
        for (Troncal troncal : sistema.getTroncales()) {
            List<Estacion> est = troncal.getEstaciones();
            int io = indexOfIgnoreCase(est, origen);
            int id = indexOfIgnoreCase(est, destino);
            if (io != -1 && id != -1) {
                int ini = Math.min(io, id), fin = Math.max(io, id);
                double dist = 0;
                List<Tramo> tramos = troncal.getTramos();
                for (int i = ini; i < fin && i < tramos.size(); i++)
                    dist += tramos.get(i).getDistancia();
                return troncal.tiempoRecorrido(dist);
            }
        }
        try { return 3.0 * ruta.numeroPararadas(origen, destino); }
        catch (TransMilenioException e) { return 3.0; }
    }

    private int indexOfIgnoreCase(List<Estacion> list, String nombre) {
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).getNombre().equalsIgnoreCase(nombre)) return i;
        return -1;
    }

    private static class NodoCosto {
        final String estacion;
        final double costo;
        NodoCosto(String estacion, double costo) {
            this.estacion = estacion;
            this.costo = costo;
        }
    }
}