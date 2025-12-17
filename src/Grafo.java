import java.util.*;

public class Grafo {

    private final Map<String, Integer> indicePorId = new LinkedHashMap<>();
    private final List<String> idPorIndice = new ArrayList<>();
    private final List<Arista> aristas = new ArrayList<>();
    private final Map<Integer, List<Arista>> aristasSalientes = new HashMap<>();

    public int agregarVertice(String id) {
        id = id.trim();
        Integer indice = indicePorId.get(id);
        if (indice != null) return indice;

        int nuevoIndice = idPorIndice.size();
        indicePorId.put(id, nuevoIndice);
        idPorIndice.add(id);
        aristasSalientes.put(nuevoIndice, new ArrayList<>());
        return nuevoIndice;
    }

    public boolean agregarArista(String idOrigen, String idDestino, double peso) {
        int origen = agregarVertice(idOrigen);
        int destino = agregarVertice(idDestino);
        if (origen == -1 || destino == -1) return false;

        Arista arista = new Arista(origen, destino, peso);
        aristas.add(arista);

        List<Arista> lista = aristasSalientes.get(origen);
        if (lista == null) {
            lista = new ArrayList<>();
            aristasSalientes.put(origen, lista);
        }
        lista.add(arista);

        return true;
    }

    public boolean agregarAristaNoDirigida(String idA, String idB, double peso) {
        boolean a = agregarArista(idA, idB, peso);
        boolean b = agregarArista(idB, idA, peso);
        return a && b;
    }


    public int obtenerIndiceDe(String id) {
        Integer indice = indicePorId.get(id.trim());
        return (indice == null) ? -1 : indice;
    }

    public String obtenerIdDe(int indice) {
        if (indice < 0 || indice >= idPorIndice.size()) return null;
        return idPorIndice.get(indice);
    }


    public int obtenerCantidadVertices() {
        return idPorIndice.size();
    }
    public List<Arista> obtenerAristas() {
        return Collections.unmodifiableList(aristas);
    }
}
