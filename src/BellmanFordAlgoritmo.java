import java.util.*;
public class BellmanFordAlgoritmo {

    public Resultado ejecutar(Grafo grafo, String idOrigen) {
        Resultado r = new Resultado();
        r.grafo = grafo;

        if (grafo == null) {
            r.ok = false;
            r.mensaje = "Grafo null";
            return r;
        }

        int origen = grafo.obtenerIndiceDe(idOrigen);
        if (origen == -1) {
            r.ok = false;
            r.mensaje = "Origen invalido o no existe";
            return r;
        }

        return ejecutar(grafo, origen);
    }

    public Resultado ejecutar(Grafo grafo, int indiceOrigen) {
        Resultado r = new Resultado();
        r.grafo = grafo;

        if (grafo == null) {
            r.ok = false;
            r.mensaje = "Grafo null";
            return r;
        }

        int n = grafo.obtenerCantidadVertices();
        if (n <= 0) {
            r.ok = false;
            r.mensaje = "Grafo sin vertices";
            return r;
        }

        if (indiceOrigen < 0 || indiceOrigen >= n) {
            r.ok = false;
            r.mensaje = "Indice de origen invalido";
            return r;
        }

        r.ok = true;
        r.mensaje = "OK";
        r.origen = indiceOrigen;

        r.distancias = new ArrayList<>(n);
        r.predecesor = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            r.distancias.add(Double.POSITIVE_INFINITY);
            r.predecesor.add(-1);
        }
        r.distancias.set(indiceOrigen, 0.0);

        List<Arista> aristas = grafo.obtenerAristas();

        for (int i = 0; i < n - 1; i++) {
            boolean cambio = false;

            for (Arista a : aristas) {
                int u = a.getOrigen();
                int v = a.getDestino();
                double w = a.getPeso();

                double du = r.distancias.get(u);
                if (Double.isInfinite(du)) continue;

                double nueva = du + w;
                double dv = r.distancias.get(v);

                if (nueva < dv) {
                    r.distancias.set(v, nueva);
                    r.predecesor.set(v, u);
                    cambio = true;
                }
            }

            if (!cambio) break;
        }

        r.cicloNegativo = false;
        for (Arista a : aristas) {
            int u = a.getOrigen();
            int v = a.getDestino();
            double w = a.getPeso();

            double du = r.distancias.get(u);
            if (Double.isInfinite(du)) continue;

            double nueva = du + w;
            double dv = r.distancias.get(v);

            if (nueva < dv) {
                r.cicloNegativo = true;
                break;
            }
        }

        return r;
    }

    public static final class Resultado {
        private boolean ok;
        private String mensaje;

        private Grafo grafo;
        private int origen;

        private List<Double> distancias;
        private List<Integer> predecesor;

        private boolean cicloNegativo;

        public boolean ok() { return ok; }
        public String mensaje() { return mensaje; }
        public boolean tieneCicloNegativo() { return cicloNegativo; }
        public int getOrigen() { return origen; }

        public double obtenerDistanciaA(String idDestino) {
            if (!ok || grafo == null || distancias == null) return Double.POSITIVE_INFINITY;
            int d = grafo.obtenerIndiceDe(idDestino);
            if (d == -1) return Double.POSITIVE_INFINITY;
            return distancias.get(d);
        }

        public double obtenerDistanciaA(int indiceDestino) {
            if (!ok || distancias == null) return Double.POSITIVE_INFINITY;
            if (indiceDestino < 0 || indiceDestino >= distancias.size()) return Double.POSITIVE_INFINITY;
            return distancias.get(indiceDestino);
        }

        public List<String> obtenerRutaA(String idDestino) {
            if (!ok || grafo == null) return Collections.emptyList();
            int d = grafo.obtenerIndiceDe(idDestino);
            if (d == -1) return Collections.emptyList();
            return obtenerRutaA(d);
        }

        public List<String> obtenerRutaA(int indiceDestino) {
            if (!ok || grafo == null || distancias == null || predecesor == null) return Collections.emptyList();
            if (indiceDestino < 0 || indiceDestino >= distancias.size()) return Collections.emptyList();
            if (Double.isInfinite(distancias.get(indiceDestino))) return Collections.emptyList();

            List<Integer> rutaIndices = new ArrayList<>();
            Set<Integer> visitados = new HashSet<>();

            int actual = indiceDestino;
            while (actual != -1) {
                if (!visitados.add(actual)) return Collections.emptyList();
                rutaIndices.add(actual);
                if (actual == origen) break;
                actual = predecesor.get(actual);
            }

            if (rutaIndices.isEmpty() || rutaIndices.get(rutaIndices.size() - 1) != origen) {
                return Collections.emptyList();
            }

            Collections.reverse(rutaIndices);

            List<String> ruta = new ArrayList<>();
            for (int idx : rutaIndices) {
                String id = grafo.obtenerIdDe(idx);
                if (id == null) return Collections.emptyList();
                ruta.add(id);
            }
            return ruta;
        }
    }
}
