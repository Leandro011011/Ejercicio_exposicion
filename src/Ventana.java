import javax.swing.*;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Ventana extends JFrame {

    private JPanel panelPrincipal;
    private JTextField txtOrigen;
    private JTextField txtDestino;
    private JButton btnCalcular;
    private JLabel lblSeleccion;
    private JLabel lblRuta;
    private JTextArea txtPorqueNo;
    private RutaVisualizar panelDibujo;

    private Grafo grafoGlobal;
    private Grafo grafoRuta1;
    private Grafo grafoRuta2;
    private Grafo grafoRuta3;

    private final Map<String, String> aliasNodos = new HashMap<>();

    public Ventana() {

        panelPrincipal = new JPanel(new BorderLayout());
        setContentPane(panelPrincipal);

        panelDibujo = new RutaVisualizar();
        panelPrincipal.add(panelDibujo, BorderLayout.CENTER);

        JPanel panelDerecha = new JPanel();
        panelDerecha.setLayout(new BoxLayout(panelDerecha, BoxLayout.Y_AXIS));

        panelDerecha.add(new JLabel("Origen"));
        txtOrigen = new JTextField();
        panelDerecha.add(txtOrigen);

        panelDerecha.add(new JLabel("Destino"));
        txtDestino = new JTextField();
        panelDerecha.add(txtDestino);

        btnCalcular = new JButton("Calcular ruta mas corta");
        panelDerecha.add(btnCalcular);

        lblSeleccion = new JLabel(" ");
        panelDerecha.add(lblSeleccion);

        lblRuta = new JLabel(" ");
        panelDerecha.add(lblRuta);

        panelDerecha.add(new JLabel("Por que no las otras rutas"));
        txtPorqueNo = new JTextArea(12, 22);
        txtPorqueNo.setLineWrap(true);
        txtPorqueNo.setWrapStyleWord(true);
        panelDerecha.add(new JScrollPane(txtPorqueNo));

        panelPrincipal.add(panelDerecha, BorderLayout.EAST);

        construirGrafos();

        btnCalcular.addActionListener(e -> calcular());

        txtOrigen.setText("Quito");
        txtDestino.setText("Guayaquil");
        calcular();
    }

    private void construirGrafos() {
        grafoGlobal = new Grafo();
        grafoRuta1 = new Grafo();
        grafoRuta2 = new Grafo();
        grafoRuta3 = new Grafo();

        registrarNodo("Quito");
        registrarNodo("Santo Domingo");
        registrarNodo("Quevedo");
        registrarNodo("Guayaquil");
        registrarNodo("Ambato");
        registrarNodo("Riobamba");

        agregarNoDirigida(grafoRuta1, "Quito", "Ambato", 150);
        agregarNoDirigida(grafoRuta1, "Ambato", "Riobamba", 55);
        agregarNoDirigida(grafoRuta1, "Riobamba", "Guayaquil", 270);

        agregarNoDirigida(grafoRuta1, "Quito", "Santo Domingo", 200);
        agregarNoDirigida(grafoRuta1, "Santo Domingo", "Quevedo", 200);
        agregarNoDirigida(grafoRuta1, "Quevedo", "Guayaquil", 250);

        agregarNoDirigida(grafoRuta2, "Quito", "Santo Domingo", 130);
        agregarNoDirigida(grafoRuta2, "Santo Domingo", "Quevedo", 115);
        agregarNoDirigida(grafoRuta2, "Quevedo", "Guayaquil", 180);

        agregarNoDirigida(grafoRuta2, "Quito", "Ambato", 220);
        agregarNoDirigida(grafoRuta2, "Ambato", "Riobamba", 120);
        agregarNoDirigida(grafoRuta2, "Riobamba", "Guayaquil", 340);

        agregarNoDirigida(grafoRuta3, "Quito", "Santo Domingo", 130);
        agregarNoDirigida(grafoRuta3, "Santo Domingo", "Quevedo", 115);
        agregarNoDirigida(grafoRuta3, "Quevedo", "Ambato", 170);
        agregarNoDirigida(grafoRuta3, "Ambato", "Riobamba", 55);
        agregarNoDirigida(grafoRuta3, "Riobamba", "Guayaquil", 270);

        copiarAristas(grafoRuta1, grafoGlobal);
        copiarAristas(grafoRuta2, grafoGlobal);
        copiarAristas(grafoRuta3, grafoGlobal);

        panelDibujo.setGrafo(grafoGlobal);
    }

    private void copiarAristas(Grafo desde, Grafo hacia) {
        for (Arista a : desde.obtenerAristas()) {
            String o = desde.obtenerIdDe(a.getOrigen());
            String d = desde.obtenerIdDe(a.getDestino());
            if (o == null || d == null) continue;
            hacia.agregarArista(o, d, a.getPeso());
        }
    }

    private void agregarNoDirigida(Grafo g, String a, String b, double km) {
        g.agregarAristaNoDirigida(a, b, km);
    }

    private void registrarNodo(String nombre) {
        aliasNodos.put(normalizar(nombre), nombre);
    }

    private void calcular() {
        lblSeleccion.setText(" ");
        lblRuta.setText(" ");
        txtPorqueNo.setText("");

        String idOrigen = resolverNodo(txtOrigen.getText());
        String idDestino = resolverNodo(txtDestino.getText());

        if (idOrigen == null || idDestino == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Nodo no reconocido.\n\n");
            sb.append("Disponibles:\n");
            for (String v : aliasNodos.values()) sb.append("- ").append(v).append("\n");
            lblSeleccion.setText("Revisa origen y destino");
            panelDibujo.limpiarRuta();
            txtPorqueNo.setText(sb.toString());
            return;
        }

        BellmanFordAlgoritmo bf = new BellmanFordAlgoritmo();

        EvalRuta e1 = evaluarSubgrafo("Ruta 1", grafoRuta1, bf, idOrigen, idDestino);
        EvalRuta e2 = evaluarSubgrafo("Ruta 2", grafoRuta2, bf, idOrigen, idDestino);
        EvalRuta e3 = evaluarSubgrafo("Ruta 3", grafoRuta3, bf, idOrigen, idDestino);

        EvalRuta seleccionada = seleccionarMejor(e1, e2, e3);

        if (seleccionada == null || !seleccionada.tieneRuta) {
            lblSeleccion.setText("No hay ruta en Ruta 1, 2 o 3 para ese origen y destino");
            lblRuta.setText(" ");
            panelDibujo.limpiarRuta();

            StringBuilder sb = new StringBuilder();
            sb.append("Comparacion por rutas:\n\n");
            sb.append(resumenComparacion(e1, null)).append("\n");
            sb.append(resumenComparacion(e2, null)).append("\n");
            sb.append(resumenComparacion(e3, null)).append("\n");
            txtPorqueNo.setText(sb.toString());
            return;
        }

        panelDibujo.setGrafo(grafoGlobal);
        panelDibujo.setRutaPorIds(seleccionada.ruta);

        lblSeleccion.setText("Se selecciono " + seleccionada.nombre);
        lblRuta.setText("Ruta: " + String.join(" -> ", seleccionada.ruta) + " | Distancia: " + formatearKm(seleccionada.distanciaKm) + " km");

        StringBuilder sb = new StringBuilder();
        sb.append("Comparacion por rutas:\n\n");
        sb.append(resumenComparacion(e1, seleccionada)).append("\n");
        sb.append(resumenComparacion(e2, seleccionada)).append("\n");
        sb.append(resumenComparacion(e3, seleccionada)).append("\n");
        txtPorqueNo.setText(sb.toString());
    }

    private EvalRuta evaluarSubgrafo(String nombre, Grafo g, BellmanFordAlgoritmo bf, String idOrigen, String idDestino) {
        EvalRuta ev = new EvalRuta();
        ev.nombre = nombre;

        int iO = g.obtenerIndiceDe(idOrigen);
        int iD = g.obtenerIndiceDe(idDestino);

        if (iO == -1 && iD == -1) { ev.mensaje = "no contiene origen ni destino"; return ev; }
        if (iO == -1) { ev.mensaje = "no contiene el origen"; return ev; }
        if (iD == -1) { ev.mensaje = "no contiene el destino"; return ev; }

        BellmanFordAlgoritmo.Resultado res = bf.ejecutar(g, idOrigen);
        if (res == null || !res.ok()) { ev.mensaje = "no se pudo ejecutar"; return ev; }
        if (res.tieneCicloNegativo()) { ev.mensaje = "ciclo negativo"; return ev; }

        double d = res.obtenerDistanciaA(idDestino);
        List<String> r = res.obtenerRutaA(idDestino);

        if (r == null || r.isEmpty() || Double.isInfinite(d)) {
            ev.mensaje = "no conecta origen -> destino";
            return ev;
        }

        ev.tieneRuta = true;
        ev.distanciaKm = d;
        ev.ruta = r;
        ev.mensaje = "ok";
        return ev;
    }

    private EvalRuta seleccionarMejor(EvalRuta e1, EvalRuta e2, EvalRuta e3) {
        List<EvalRuta> lista = new ArrayList<>();
        lista.add(e1);
        lista.add(e2);
        lista.add(e3);

        EvalRuta mejor = null;
        for (EvalRuta e : lista) {
            if (e == null || !e.tieneRuta) continue;
            if (mejor == null || e.distanciaKm + 1e-9 < mejor.distanciaKm) mejor = e;
        }
        return mejor;
    }

    private String resumenComparacion(EvalRuta e, EvalRuta sel) {
        if (e == null) return "Ruta ?: no existe";
        if (!e.tieneRuta) return e.nombre + ": " + e.mensaje;

        if (sel == null || !sel.tieneRuta) {
            return e.nombre + ": ruta posible (" + formatearKm(e.distanciaKm) + " km) -> " + String.join(" -> ", e.ruta);
        }

        if (e.nombre.equals(sel.nombre)) {
            return e.nombre + ": seleccionada (" + formatearKm(e.distanciaKm) + " km) -> " + String.join(" -> ", e.ruta);
        }

        double diff = e.distanciaKm - sel.distanciaKm;
        if (Math.abs(diff) < 1e-9) {
            return e.nombre + ": no se eligio porque empata en distancia (" + formatearKm(e.distanciaKm) + " km) -> " + String.join(" -> ", e.ruta);
        }
        if (diff > 0) {
            return e.nombre + ": no se eligio por ser mas larga (" + formatearKm(e.distanciaKm) + " km), diferencia +" + formatearKm(diff) + " km -> " + String.join(" -> ", e.ruta);
        }
        return e.nombre + ": mas corta que la seleccionada (" + formatearKm(e.distanciaKm) + " km), diferencia " + formatearKm(diff) + " km -> " + String.join(" -> ", e.ruta);
    }

    private String resolverNodo(String entrada) {
        String n = normalizar(entrada);
        if (n.isEmpty()) return null;

        if (n.equals("santodomingo")) n = "santo domingo";
        if (n.equals("sto domingo")) n = "santo domingo";
        if (n.equals("sd")) n = "santo domingo";

        String id = aliasNodos.get(n);
        if (id != null) return id;

        for (String k : aliasNodos.keySet()) {
            if (k.contains(n)) return aliasNodos.get(k);
        }
        return null;
    }

    private String normalizar(String s) {
        if (s == null) return "";
        s = s.trim().toLowerCase();
        return s;
    }

    private String formatearKm(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-9) return String.valueOf((long) Math.rint(v));
        return String.format(Locale.US, "%.1f", v);
    }

    private static final class EvalRuta {
        String nombre;
        boolean tieneRuta;
        double distanciaKm;
        List<String> ruta;
        String mensaje;
    }

    public static void main(String[] args) {
        JFrame ventana = new JFrame("Rutas predefinidas - Bellman Ford");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(1100, 700);
        ventana.setLocationRelativeTo(null);
        Ventana contenido = new Ventana();
        ventana.setContentPane(contenido.getContentPane());
        ventana.setVisible(true);
    }
}
