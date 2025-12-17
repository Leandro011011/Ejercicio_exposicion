import javax.swing.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RutaVisualizar extends JPanel {

    private Grafo grafo;
    private final Map<Integer, Point2D.Double> posicionPorIndice = new HashMap<>();
    private final List<Integer> rutaIndices = new ArrayList<>();
    private final Set<String> aristasEnRuta = new HashSet<>();

    private int radioNodo = 34;

    private int iteracionesFuerza = 280;
    private double kAtraccion = 0.012;
    private double kRepulsion = 26000.0;
    private double amortiguacion = 0.86;

    private Font fuenteNodo = new Font("SansSerif", Font.PLAIN, 15);

    public RutaVisualizar() {
        setOpaque(true);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recalcularPosiciones();
                repaint();
            }
        });
    }

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
        rutaIndices.clear();
        aristasEnRuta.clear();
        recalcularPosiciones();
        repaint();
    }

    public void limpiarRuta() {
        rutaIndices.clear();
        aristasEnRuta.clear();
        repaint();
    }

    public void setRutaPorIds(List<String> rutaIds) {
        rutaIndices.clear();
        aristasEnRuta.clear();

        if (grafo == null || rutaIds == null || rutaIds.isEmpty()) {
            repaint();
            return;
        }

        List<Integer> indices = new ArrayList<>();
        for (String id : rutaIds) {
            int idx = grafo.obtenerIndiceDe(id);
            if (idx == -1) {
                indices.clear();
                break;
            }
            indices.add(idx);
        }

        if (indices.isEmpty()) {
            repaint();
            return;
        }

        rutaIndices.addAll(indices);

        for (int i = 0; i < rutaIndices.size() - 1; i++) {
            int u = rutaIndices.get(i);
            int v = rutaIndices.get(i + 1);
            aristasEnRuta.add(claveArista(u, v));
        }

        repaint();
    }

    public void recalcularPosiciones() {
        posicionPorIndice.clear();
        if (grafo == null) return;

        int n = grafo.obtenerCantidadVertices();
        if (n <= 0) return;

        int w = Math.max(getWidth(), 1);
        int h = Math.max(getHeight(), 1);

        double margen = Math.max(110, radioNodo * 4.6);
        double minX = margen;
        double minY = margen;
        double maxX = w - margen;
        double maxY = h - margen;

        if (maxX <= minX) maxX = minX + 1;
        if (maxY <= minY) maxY = minY + 1;

        double cx = (minX + maxX) / 2.0;
        double cy = (minY + maxY) / 2.0;
        double r = Math.min(maxX - minX, maxY - minY) * 0.44;
        r = Math.max(r, 170);

        for (int i = 0; i < n; i++) {
            double ang = (2.0 * Math.PI * i) / n;
            double x = cx + r * Math.cos(ang);
            double y = cy + r * Math.sin(ang);
            posicionPorIndice.put(i, new Point2D.Double(x, y));
        }

        List<Point2D.Double> velocidad = new ArrayList<>();
        for (int i = 0; i < n; i++) velocidad.add(new Point2D.Double(0, 0));

        List<Arista> aristas = grafo.obtenerAristas();

        for (int it = 0; it < iteracionesFuerza; it++) {
            List<Point2D.Double> fuerza = new ArrayList<>();
            for (int i = 0; i < n; i++) fuerza.add(new Point2D.Double(0, 0));

            for (int i = 0; i < n; i++) {
                Point2D.Double pi = posicionPorIndice.get(i);
                for (int j = i + 1; j < n; j++) {
                    Point2D.Double pj = posicionPorIndice.get(j);

                    double dx = pi.x - pj.x;
                    double dy = pi.y - pj.y;
                    double dist2 = dx * dx + dy * dy + 0.01;
                    double dist = Math.sqrt(dist2);

                    double rep = kRepulsion / dist2;
                    double fx = (dx / dist) * rep;
                    double fy = (dy / dist) * rep;

                    fuerza.get(i).x += fx;
                    fuerza.get(i).y += fy;
                    fuerza.get(j).x -= fx;
                    fuerza.get(j).y -= fy;
                }
            }

            for (Arista a : aristas) {
                int u = a.getOrigen();
                int v = a.getDestino();
                if (u == v) continue;

                Point2D.Double pu = posicionPorIndice.get(u);
                Point2D.Double pv = posicionPorIndice.get(v);

                double dx = pv.x - pu.x;
                double dy = pv.y - pu.y;
                double dist = Math.sqrt(dx * dx + dy * dy) + 0.01;

                double objetivo = radioNodo * 6.2;
                double atr = kAtraccion * (dist - objetivo);
                double fx = (dx / dist) * atr;
                double fy = (dy / dist) * atr;

                fuerza.get(u).x += fx;
                fuerza.get(u).y += fy;
                fuerza.get(v).x -= fx;
                fuerza.get(v).y -= fy;
            }

            for (int i = 0; i < n; i++) {
                Point2D.Double v = velocidad.get(i);
                Point2D.Double f = fuerza.get(i);

                v.x = (v.x + f.x) * amortiguacion;
                v.y = (v.y + f.y) * amortiguacion;

                double maxV = 24.0;
                if (v.x > maxV) v.x = maxV;
                if (v.x < -maxV) v.x = -maxV;
                if (v.y > maxV) v.y = maxV;
                if (v.y < -maxV) v.y = -maxV;

                Point2D.Double p = posicionPorIndice.get(i);
                p.x += v.x;
                p.y += v.y;

                if (p.x < minX) p.x = minX;
                if (p.x > maxX) p.x = maxX;
                if (p.y < minY) p.y = minY;
                if (p.y > maxY) p.y = maxY;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(fuenteNodo);

        if (grafo == null || grafo.obtenerCantidadVertices() == 0) {
            g2.dispose();
            return;
        }

        Set<String> keys = new HashSet<>();
        for (Arista a : grafo.obtenerAristas()) {
            keys.add(claveArista(a.getOrigen(), a.getDestino()));
        }

        for (Arista a : grafo.obtenerAristas()) {
            dibujarArista(g2, a, keys);
        }

        for (int i = 0; i < grafo.obtenerCantidadVertices(); i++) {
            dibujarNodo(g2, i);
        }

        g2.dispose();
    }

    private void dibujarNodo(Graphics2D g2, int indice) {
        Point2D.Double p = posicionPorIndice.get(indice);
        if (p == null) return;

        double x = p.x - radioNodo;
        double y = p.y - radioNodo;

        boolean enRuta = rutaIndices.contains(indice);

        g2.setColor(new Color(245, 245, 245));
        g2.fill(new Ellipse2D.Double(x, y, radioNodo * 2.0, radioNodo * 2.0));

        g2.setStroke(new BasicStroke(2.8f));
        g2.setColor(enRuta ? new Color(40, 120, 220) : new Color(60, 60, 60));
        g2.draw(new Ellipse2D.Double(x, y, radioNodo * 2.0, radioNodo * 2.0));

        String id = grafo.obtenerIdDe(indice);
        if (id == null) id = "";

        FontMetrics fm = g2.getFontMetrics();
        int tx = (int) (p.x - fm.stringWidth(id) / 2.0);
        int ty = (int) (p.y + fm.getAscent() / 2.0 - 2);

        g2.drawString(id, tx, ty);
    }

    private void dibujarArista(Graphics2D g2, Arista a, Set<String> keys) {
        int u = a.getOrigen();
        int v = a.getDestino();

        Point2D.Double pu = posicionPorIndice.get(u);
        Point2D.Double pv = posicionPorIndice.get(v);
        if (pu == null || pv == null) return;

        boolean enRuta = aristasEnRuta.contains(claveArista(u, v));

        boolean esBidireccional = keys.contains(claveArista(v, u));
        int signo = (u < v) ? 1 : -1;

        double dx = pv.x - pu.x;
        double dy = pv.y - pu.y;
        double dist = Math.hypot(dx, dy);
        if (dist < 0.0001) return;

        double ux = dx / dist;
        double uy = dy / dist;

        double nx = -uy;
        double ny = ux;

        double offset = esBidireccional ? (radioNodo * 0.32) * signo : 0.0;

        double x1 = pu.x + ux * (radioNodo + 3) + nx * offset;
        double y1 = pu.y + uy * (radioNodo + 3) + ny * offset;

        double x2 = pv.x - ux * (radioNodo + 9) + nx * offset;
        double y2 = pv.y - uy * (radioNodo + 9) + ny * offset;

        g2.setStroke(new BasicStroke(enRuta ? 3.6f : 2.0f));
        g2.setColor(enRuta ? new Color(40, 120, 220) : new Color(140, 140, 140));
        g2.draw(new Line2D.Double(x1, y1, x2, y2));



        String pesoTxt = formatearPeso(a.getPeso());
        dibujarEtiquetaPeso(g2, x1, y1, x2, y2, pesoTxt, enRuta, offset);
    }



    private void dibujarEtiquetaPeso(Graphics2D g2, double x1, double y1, double x2, double y2, String texto, boolean enRuta, double offset) {
        double mx = (x1 + x2) / 2.0;
        double my = (y1 + y2) / 2.0;

        double nx = -(y2 - y1);
        double ny = (x2 - x1);
        double nlen = Math.hypot(nx, ny);
        if (nlen > 0.0001) {
            nx /= nlen;
            ny /= nlen;
        }

        double sep = 16.0 + Math.abs(offset) + radioNodo * 0.12;
        mx += nx * sep;
        my += ny * sep;

        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(texto);
        int th = fm.getHeight();

        int bx = (int) (mx - tw / 2.0) - 6;
        int by = (int) (my - th / 2.0) - 3;

        g2.setColor(new Color(255, 255, 255, 220));
        g2.fillRoundRect(bx, by, tw + 12, th + 6, 10, 10);

        g2.setColor(enRuta ? new Color(40, 120, 220) : new Color(90, 90, 90));
        g2.drawString(texto, (int) (mx - tw / 2.0), (int) (my + fm.getAscent() / 2.0 - 2));
    }

    private String formatearPeso(double peso) {
        if (Math.abs(peso - Math.rint(peso)) < 1e-9) return String.valueOf((long) Math.rint(peso));
        String s = String.format(Locale.US, "%.2f", peso);
        while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) {
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
                break;
            }
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private String claveArista(int u, int v) {
        return u + "->" + v;
    }
}
