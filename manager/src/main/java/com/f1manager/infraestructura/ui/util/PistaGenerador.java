//Genera la forma visual de la pista de cada circuito de manera automática 
//y siempre igual para el mismo circuito, y calcula posiciones sobre esa pista 
//para animar los autos durante la carrera.
package com.f1manager.infraestructura.ui.util;

import com.f1manager.dominio.modelo.Circuito;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public final class PistaGenerador {

    private final List<Point2D> puntosNormalizados; // coordenadas en rango aproximado [-1, 1]
    private final double[] longitudAcumulada;        // longitud acumulada de la spline, para posicionar por fracción
    private final double longitudTotal;

    private PistaGenerador(List<Point2D> puntosNormalizados) {
        this.puntosNormalizados = puntosNormalizados;
        this.longitudAcumulada = new double[puntosNormalizados.size()];
        double acumulado = 0;
        Point2D anterior = puntosNormalizados.get(0);
        for (int i = 0; i < puntosNormalizados.size(); i++) {
            Point2D actual = puntosNormalizados.get(i);
            acumulado += actual.distance(anterior);
            longitudAcumulada[i] = acumulado;
            anterior = actual;
        }
        this.longitudTotal = acumulado;
    }

    /** Genera (de forma determinista) la pista asociada a un circuito. */
    //Esto mira:
    //Cuántos "puntos de control" tendrá la pista (entre 9 y 13).
    //El radio (qué tan lejos del centro) de cada punto, con variación aleatoria.
    //Si en ese punto se genera una "chicane" (una curva cerrada hacia adentro), con 25% de probabilidad.
    public static PistaGenerador paraCircuito(Circuito circuito) {
        //Este especificamente genera la semilla
        long semilla = ((long) circuito.getId() * 1_000_003L) ^ circuito.getNombre().hashCode();
        return generar(semilla);
    }
    //Este hace calculos matematicas y probabilidades con la semilla creada
    private static PistaGenerador generar(long semilla) {
        Random random = new Random(semilla);
        int puntosBase = 9 + random.nextInt(5); // entre 9 y 13 puntos de control

        List<Point2D> control = new ArrayList<>();
        for (int i = 0; i < puntosBase; i++) {
            double angulo = (2 * Math.PI * i) / puntosBase;
            // Radio variable para que la pista no sea un círculo perfecto
            double radio = 0.55 + random.nextDouble() * 0.42;
            // Ocasionalmente se genera una "chicane" (entrante) para dar aspecto de circuito real
            if (random.nextDouble() < 0.25) {
                radio *= 0.6;
            }
            double x = Math.cos(angulo) * radio;
            double y = Math.sin(angulo) * radio * 0.82; // aplanado leve, aspecto más "de pista"
            control.add(new Point2D(x, y));
        }

        List<Point2D> suavizados = suavizarCatmullRom(control, 22);
        return new PistaGenerador(suavizados);
    }

    /** Interpolación Catmull-Rom sobre una lista cerrada de puntos de control. */
    private static List<Point2D> suavizarCatmullRom(List<Point2D> control, int segmentosPorTramo) {
        List<Point2D> resultado = new ArrayList<>();
        int n = control.size();
        for (int i = 0; i < n; i++) {
            Point2D p0 = control.get((i - 1 + n) % n);
            Point2D p1 = control.get(i);
            Point2D p2 = control.get((i + 1) % n);
            Point2D p3 = control.get((i + 2) % n);

            for (int s = 0; s < segmentosPorTramo; s++) {
                double t = s / (double) segmentosPorTramo;
                resultado.add(catmullRom(p0, p1, p2, p3, t));
            }
        }
        return resultado;
    }

    private static Point2D catmullRom(Point2D p0, Point2D p1, Point2D p2, Point2D p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        double x = 0.5 * ((2 * p1.getX()) + (-p0.getX() + p2.getX()) * t
                + (2 * p0.getX() - 5 * p1.getX() + 4 * p2.getX() - p3.getX()) * t2
                + (-p0.getX() + 3 * p1.getX() - 3 * p2.getX() + p3.getX()) * t3);
        double y = 0.5 * ((2 * p1.getY()) + (-p0.getY() + p2.getY()) * t
                + (2 * p0.getY() - 5 * p1.getY() + 4 * p2.getY() - p3.getY()) * t2
                + (-p0.getY() + 3 * p1.getY() - 3 * p2.getY() + p3.getY()) * t3);
        return new Point2D(x, y);
    }

    /** Convierte una coordenada normalizada [-1,1] al espacio de píxeles del área de dibujo dada. */
    private Point2D aPixel(Point2D normalizado, double x, double y, double ancho, double alto) {
        double margen = 0.14; // deja aire alrededor de la pista
        double px = x + ancho / 2.0 + normalizado.getX() * (ancho / 2.0) * (1 - margen);
        double py = y + alto / 2.0 + normalizado.getY() * (alto / 2.0) * (1 - margen);
        return new Point2D(px, py);
    }

    /**
     * Dibuja la pista dentro del rectángulo indicado.
     *
     * @param destacada si es true, se dibuja con mayor brillo/grosor (pista seleccionada)
     */
    public void dibujar(GraphicsContext gc, double x, double y, double ancho, double alto, boolean destacada) {
        gc.save();

        // Asfalto (línea gruesa gris oscuro)
        gc.setStroke(Color.web(destacada ? "#5b6376" : "#3a4054"));
        gc.setLineWidth(destacada ? 14 : 11);
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        trazarRuta(gc, x, y, ancho, alto);
        gc.stroke();

        // Línea central (marcas de pista)
        gc.setStroke(Color.web(destacada ? "#e10600" : "#6b7280"));
        gc.setLineWidth(destacada ? 2.4 : 1.4);
        gc.setLineDashes(destacada ? 10 : 6, destacada ? 8 : 6);
        trazarRuta(gc, x, y, ancho, alto);
        gc.stroke();
        gc.setLineDashes(0);

        // Marca de salida/meta
        Point2D metaNorm = puntosNormalizados.get(0);
        Point2D meta = aPixel(metaNorm, x, y, ancho, alto);
        gc.setFill(destacada ? IconFactory.ROJO_BRILLANTE : Color.web("#8b93a8"));
        gc.fillOval(meta.getX() - 5, meta.getY() - 5, 10, 10);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeOval(meta.getX() - 5, meta.getY() - 5, 10, 10);

        gc.restore();
    }

    private void trazarRuta(GraphicsContext gc, double x, double y, double ancho, double alto) {
        gc.beginPath();
        Point2D primero = aPixel(puntosNormalizados.get(0), x, y, ancho, alto);
        gc.moveTo(primero.getX(), primero.getY());
        for (int i = 1; i < puntosNormalizados.size(); i++) {
            Point2D p = aPixel(puntosNormalizados.get(i), x, y, ancho, alto);
            gc.lineTo(p.getX(), p.getY());
        }
        gc.lineTo(primero.getX(), primero.getY());
        gc.closePath();
    }

    /**
     * Calcula la posición en píxeles correspondiente a una fracción de vuelta
     * (0.0 = línea de salida, 1.0 = vuelta completa), útil para animar los
     * puntos de los pilotos durante la carrera.
     */
    public Point2D posicionEnFraccion(double fraccion, double x, double y, double ancho, double alto) {
        double f = fraccion % 1.0;
        if (f < 0) f += 1.0;
        double distanciaObjetivo = f * longitudTotal;

        int idx = 0;
        for (int i = 0; i < longitudAcumulada.length; i++) {
            if (longitudAcumulada[i] >= distanciaObjetivo) {
                idx = i;
                break;
            }
        }
        int idxAnterior = (idx - 1 + puntosNormalizados.size()) % puntosNormalizados.size();
        double distAnterior = idx == 0 ? 0 : longitudAcumulada[idxAnterior];
        double distActual = longitudAcumulada[idx];
        double segmento = Math.max(distActual - distAnterior, 0.0001);
        double t = (distanciaObjetivo - distAnterior) / segmento;
        t = Math.max(0, Math.min(1, t));

        Point2D pAnterior = puntosNormalizados.get(idxAnterior);
        Point2D pActual = puntosNormalizados.get(idx);
        double ix = pAnterior.getX() + (pActual.getX() - pAnterior.getX()) * t;
        double iy = pAnterior.getY() + (pActual.getY() - pAnterior.getY()) * t;

        return aPixel(new Point2D(ix, iy), x, y, ancho, alto);
    }
}
