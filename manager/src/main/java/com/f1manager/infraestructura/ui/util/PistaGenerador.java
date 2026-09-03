//Genera la forma visual de la pista de cada circuito de manera automática
//y siempre igual para el mismo circuito, y calcula posiciones sobre esa pista
//para animar los autos durante la carrera.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.util;

//Trae la clase del dominio Circuito, para poder generar una pista distinta (pero siempre igual) para cada uno.
import com.f1manager.dominio.modelo.Circuito;
//Trae Point2D, una clase de JavaFX que representa un punto (o vector) en 2 dimensiones, con coordenadas x e y.
import javafx.geometry.Point2D;
//Trae GraphicsContext, el "pincel" de JavaFX con el que se dibuja manualmente sobre un Canvas (líneas, óvalos, colores, etc.).
import javafx.scene.canvas.GraphicsContext;
//Trae Color, la clase de JavaFX que representa un color.
import javafx.scene.paint.Color;

//Importa la clase de ArrayList para poder usar listas (Son las que tienen <>)
import java.util.ArrayList;
//Trae Arrays, una clase de herramientas de Java con métodos útiles para arreglos, como sort() (ordenar).
import java.util.Arrays;
//Importa la interfaz List, que define el comportamiento general de una lista en Java (sirve como plantilla para clases como ArrayList)
import java.util.List;
//Trae la herramienta oficial de Java para generar valores aleatorios o al azar en tu programa.
import java.util.Random;
//Clase publica y final (no se puede heredar de ella) llamada "PistaGenerador"
public final class PistaGenerador {

    //Proporción del trazado considerada "curva" (el resto se considera "recta").
    private static final double PROPORCION_CURVA = 0.35;

    //Lista de puntos (Point2D) que forman el contorno de la pista ya suavizado, en coordenadas
    //"normalizadas" (un rango aproximado de -1 a 1, sin importar el tamaño real en píxeles todavía)
    private final List<Point2D> puntosNormalizados; // coordenadas en rango aproximado [-1, 1]
    //Arreglo con la distancia acumulada recorrida hasta cada punto de la pista (sirve para saber, dada
    //una fracción de vuelta, en qué punto exacto del contorno cae)
    private final double[] longitudAcumulada;        // longitud acumulada de la spline, para posicionar por fracción
    //La longitud total del contorno de la pista (la suma de todos los tramos entre puntos)
    private final double longitudTotal;
    //Arreglo con qué tan "cerrado" es el trazado en cada punto (mayor número = curva más cerrada)
    private final double[] curvaturas;               // qué tan cerrado es el trazado en cada punto (mayor = más cerrado)
    //A partir de qué valor de curvatura un punto se considera parte de una "curva" y no de una "recta"
    private final double umbralCurva;                // a partir de qué curvatura se considera "curva"

    //Constructor privado (solo se crea desde paraCircuito()/generar()): a partir de los puntos ya
    //suavizados de la pista, calcula de una vez toda la información auxiliar que se necesita después
    //(longitud acumulada para animar los autos, y curvaturas para saber dónde hay curvas).
    private PistaGenerador(List<Point2D> puntosNormalizados) {
        this.puntosNormalizados = puntosNormalizados;
        this.longitudAcumulada = new double[puntosNormalizados.size()];
        double acumulado = 0;
        Point2D anterior = puntosNormalizados.get(0);
        //Recorre todos los puntos sumando la distancia entre cada punto y el anterior, para ir
        //armando la distancia acumulada recorrida hasta llegar a cada uno
        for (int i = 0; i < puntosNormalizados.size(); i++) {
            Point2D actual = puntosNormalizados.get(i);
            acumulado += actual.distance(anterior);
            longitudAcumulada[i] = acumulado;
            anterior = actual;
        }
        this.longitudTotal = acumulado;
        this.curvaturas = calcularCurvaturas(puntosNormalizados);
        this.umbralCurva = calcularUmbralCurva(curvaturas);
    }

    //Curvatura aproximada (en radianes) en cada punto: ángulo entre el tramo anterior y el siguiente.
    //Entre más grande el ángulo, más brusco es el giro en ese punto (más "cerrada" la curva).
    private static double[] calcularCurvaturas(List<Point2D> puntos) {
        int n = puntos.size();
        double[] resultado = new double[n];
        for (int i = 0; i < n; i++) {
            //Toma un punto un poco antes y un poco después del actual (saltando de a 2, y usando
            //módulo % n para "dar la vuelta" cuando se pasa del principio o del final de la lista,
            //ya que la pista es un circuito cerrado)
            Point2D anterior = puntos.get((i - 2 + n) % n);
            Point2D actual = puntos.get(i);
            Point2D siguiente = puntos.get((i + 2) % n);
            //v1 es el vector (dirección) del tramo que llega al punto actual, v2 el del tramo que sale
            Point2D v1 = actual.subtract(anterior);
            Point2D v2 = siguiente.subtract(actual);
            resultado[i] = angulo(v1, v2);
        }
        return resultado;
    }

    //Calcula el ángulo (en radianes) entre dos vectores usando el producto punto (dotProduct).
    //Geometría: el coseno del ángulo entre dos vectores es igual a su producto punto dividido entre
    //el producto de sus magnitudes (largos); por eso, para recuperar el ángulo, se usa Math.acos
    //(la función inversa del coseno) sobre ese resultado.
    private static double angulo(Point2D a, Point2D b) {
        double magA = a.magnitude();
        double magB = b.magnitude();
        //Si algún vector es prácticamente de largo cero, no se puede calcular un ángulo real: se asume 0
        if (magA < 1e-9 || magB < 1e-9) {
            return 0;
        }
        //Se recorta el coseno entre -1 y 1 (Math.max/Math.min) porque, por pequeños errores de
        //redondeo de los decimales, a veces el cálculo se pasa apenas de esos límites y Math.acos
        //no acepta valores fuera de ese rango
        double coseno = Math.max(-1, Math.min(1, a.dotProduct(b) / (magA * magB)));
        return Math.acos(coseno);
    }

    //Umbral de curvatura tal que aproximadamente PROPORCION_CURVA de la pista quede clasificada como curva.
    //La idea es ordenar todas las curvaturas de menor a mayor y tomar el valor que deja exactamente ese
    //porcentaje de puntos por encima (un percentil), en vez de usar un número fijo que no serviría igual
    //de bien para pistas con formas muy distintas.
    private static double calcularUmbralCurva(double[] curvaturas) {
        double[] ordenadas = curvaturas.clone();
        Arrays.sort(ordenadas);
        //Calcula qué posición del arreglo ordenado corresponde a ese percentil
        int indice = (int) Math.floor(ordenadas.length * (1 - PROPORCION_CURVA));
        //Se recorta el índice para que nunca se salga de los límites válidos del arreglo
        indice = Math.max(0, Math.min(ordenadas.length - 1, indice));
        return ordenadas[indice];
    }

    //Indica si la fracción de vuelta dada (0.0 a 1.0) cae en un tramo de curva del trazado real.
    public boolean esCurvaEnFraccion(double fraccion) {
        //El operador % (módulo/resto) deja la fracción siempre dentro del rango de una sola vuelta,
        //aunque venga un número más grande (ej. 1.3 vueltas) o negativo
        double f = fraccion % 1.0;
        if (f < 0) f += 1.0;
        //Convierte la fracción de vuelta al índice del punto más cercano dentro del arreglo de curvaturas
        int indice = (int) Math.round(f * curvaturas.length) % curvaturas.length;
        return curvaturas[indice] >= umbralCurva;
    }

    //Genera (de forma determinista) la pista asociada a un circuito.
    //Esto mira:
    //Cuántos "puntos de control" tendrá la pista (entre 9 y 13).
    //El radio (qué tan lejos del centro) de cada punto, con variación aleatoria.
    //Si en ese punto se genera una "chicane" (una curva cerrada hacia adentro), con 25% de probabilidad.
    public static PistaGenerador paraCircuito(Circuito circuito) {
        //Este especificamente genera la semilla
        //Combina el id del circuito con el hash de su nombre para que cada circuito tenga su propia
        //semilla fija: así la pista se genera siempre igual para el mismo circuito, pero distinta entre circuitos.
        long semilla = ((long) circuito.getId() * 1_000_003L) ^ circuito.getNombre().hashCode();
        return generar(semilla);
    }
    //Este hace calculos matematicas y probabilidades con la semilla creada
    private static PistaGenerador generar(long semilla) {
        //Random con semilla fija: con la misma semilla, siempre da la misma secuencia de números
        //"aleatorios", por eso la pista sale siempre igual para el mismo circuito.
        Random random = new Random(semilla);
        int puntosBase = 9 + random.nextInt(5); // entre 9 y 13 puntos de control

        List<Point2D> control = new ArrayList<>();
        //Reparte los puntos de control en círculo (coordenadas polares: un ángulo y un radio) y
        //después los convierte a coordenadas x/y normales con seno y coseno
        for (int i = 0; i < puntosBase; i++) {
            //Reparte los puntos en ángulos iguales alrededor de un círculo completo (2 * PI radianes = 360°)
            double angulo = (2 * Math.PI * i) / puntosBase;
            // Radio variable para que la pista no sea un círculo perfecto
            double radio = 0.55 + random.nextDouble() * 0.42;
            // Ocasionalmente se genera una "chicane" (entrante) para dar aspecto de circuito real
            if (random.nextDouble() < 0.25) {
                radio *= 0.6;
            }
            //Math.cos y Math.sin convierten el ángulo y el radio (coordenadas polares) a coordenadas
            //cartesianas x/y normales, que es como se representan los puntos en pantalla
            double x = Math.cos(angulo) * radio;
            double y = Math.sin(angulo) * radio * 0.82; // aplanado leve, aspecto más "de pista"
            control.add(new Point2D(x, y));
        }

        //Suaviza los puntos de control (que forman un polígono anguloso) en una curva suave tipo pista real
        List<Point2D> suavizados = suavizarCatmullRom(control, 22);
        return new PistaGenerador(suavizados);
    }

    //Interpolación Catmull-Rom sobre una lista cerrada de puntos de control.
    //Catmull-Rom es un tipo de curva (spline) que pasa exactamente por todos los puntos de control,
    //pero dibuja tramos curvos suaves entre ellos en vez de líneas rectas; por eso la pista final se
    //ve como un circuito real y no como un polígono con esquinas.
    private static List<Point2D> suavizarCatmullRom(List<Point2D> control, int segmentosPorTramo) {
        List<Point2D> resultado = new ArrayList<>();
        int n = control.size();
        //Recorre cada tramo entre dos puntos de control consecutivos
        for (int i = 0; i < n; i++) {
            //La fórmula de Catmull-Rom necesita, además de los dos puntos del tramo (p1 y p2), el
            //punto anterior (p0) y el siguiente (p3) para calcular la curvatura; el módulo % n hace
            //que la lista se trate como un círculo cerrado (el punto después del último es el primero)
            Point2D p0 = control.get((i - 1 + n) % n);
            Point2D p1 = control.get(i);
            Point2D p2 = control.get((i + 1) % n);
            Point2D p3 = control.get((i + 2) % n);

            //Dentro de cada tramo, genera varios puntos intermedios (segmentosPorTramo) para que la
            //curva se vea suave y no como saltos bruscos entre p1 y p2
            for (int s = 0; s < segmentosPorTramo; s++) {
                double t = s / (double) segmentosPorTramo;
                resultado.add(catmullRom(p0, p1, p2, p3, t));
            }
        }
        return resultado;
    }

    //Fórmula matemática de Catmull-Rom: dado un valor t entre 0 y 1 (qué tan avanzado está dentro
    //del tramo entre p1 y p2), calcula la posición exacta de la curva en ese punto, usando también
    //los puntos vecinos p0 y p3 para darle la curvatura suave. Se calcula por separado para x y para y.
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

    //Convierte una coordenada normalizada [-1,1] al espacio de píxeles del área de dibujo dada.
    //Es decir, traduce las coordenadas "abstractas" con las que se generó la pista a coordenadas
    //reales de pantalla, según dónde y de qué tamaño se vaya a dibujar el rectángulo (x, y, ancho, alto).
    private Point2D aPixel(Point2D normalizado, double x, double y, double ancho, double alto) {
        double margen = 0.14; // deja aire alrededor de la pista
        //Centra el punto en el rectángulo (sumando la mitad del ancho/alto) y lo escala dejando un
        //margen (1 - margen) para que la pista no toque los bordes del área de dibujo
        double px = x + ancho / 2.0 + normalizado.getX() * (ancho / 2.0) * (1 - margen);
        double py = y + alto / 2.0 + normalizado.getY() * (alto / 2.0) * (1 - margen);
        return new Point2D(px, py);
    }

    //Dibuja la pista dentro del rectángulo indicado.
    //Si "destacada" es true, se dibuja con mayor brillo/grosor (pista seleccionada).
    public void dibujar(GraphicsContext gc, double x, double y, double ancho, double alto, boolean destacada) {
        //Guarda el estado actual del "pincel" (colores, grosores, etc.) para poder restaurarlo al final
        //y no afectar otros dibujos que se hagan después sobre el mismo Canvas
        gc.save();

        // Asfalto (línea gruesa gris oscuro)
        gc.setStroke(Color.web(destacada ? "#5b6376" : "#3a4054"));
        gc.setLineWidth(destacada ? 14 : 11);
        //Hace que las uniones entre segmentos de línea y las puntas se vean redondeadas en vez de en ángulo recto
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        trazarRuta(gc, x, y, ancho, alto);
        gc.stroke();

        // Línea central (marcas de pista)
        gc.setStroke(Color.web(destacada ? "#e10600" : "#6b7280"));
        gc.setLineWidth(destacada ? 2.4 : 1.4);
        //Dibuja la línea a rayas (segmento visible, espacio, segmento visible...) para simular las marcas de pista
        gc.setLineDashes(destacada ? 10 : 6, destacada ? 8 : 6);
        trazarRuta(gc, x, y, ancho, alto);
        gc.stroke();
        //Se quitan las rayas (vuelve a línea sólida) para no afectar el próximo dibujo que se haga con este mismo gc
        gc.setLineDashes(0);

        // Marca de salida/meta
        Point2D metaNorm = puntosNormalizados.get(0);
        Point2D meta = aPixel(metaNorm, x, y, ancho, alto);
        gc.setFill(destacada ? IconFactory.ROJO_BRILLANTE : Color.web("#8b93a8"));
        //Dibuja un círculo relleno (fillOval con igual ancho y alto) centrado en el punto de meta
        gc.fillOval(meta.getX() - 5, meta.getY() - 5, 10, 10);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeOval(meta.getX() - 5, meta.getY() - 5, 10, 10);

        //Devuelve el "pincel" al estado que tenía antes de gc.save(), para no dejar colores/grosores raros puestos
        gc.restore();
    }

    //Dibuja sobre el GraphicsContext el contorno completo de la pista, uniendo con líneas rectas
    //todos los puntos ya suavizados (como el suavizado ya generó muchos puntos intermedios, esas
    //líneas rectas cortas terminan viéndose como una curva continua) y cerrando la figura al final.
    private void trazarRuta(GraphicsContext gc, double x, double y, double ancho, double alto) {
        gc.beginPath();
        Point2D primero = aPixel(puntosNormalizados.get(0), x, y, ancho, alto);
        gc.moveTo(primero.getX(), primero.getY());
        for (int i = 1; i < puntosNormalizados.size(); i++) {
            Point2D p = aPixel(puntosNormalizados.get(i), x, y, ancho, alto);
            gc.lineTo(p.getX(), p.getY());
        }
        //Vuelve a unir el último punto con el primero para cerrar el circuito (la pista es un lazo cerrado)
        gc.lineTo(primero.getX(), primero.getY());
        gc.closePath();
    }

    //Calcula la posición en píxeles correspondiente a una fracción de vuelta
    //(0.0 = línea de salida, 1.0 = vuelta completa), útil para animar los
    //puntos de los pilotos durante la carrera.
    public Point2D posicionEnFraccion(double fraccion, double x, double y, double ancho, double alto) {
        //El operador % (módulo/resto) deja la fracción siempre dentro del rango de una sola vuelta,
        //aunque venga un número más grande o negativo
        double f = fraccion % 1.0;
        if (f < 0) f += 1.0;
        //Convierte la fracción de vuelta a una distancia real recorrida sobre el contorno de la pista
        double distanciaObjetivo = f * longitudTotal;

        //Busca el primer punto cuya distancia acumulada ya alcanza o supera la distancia objetivo
        //(es decir, el punto donde "cae" esa fracción de vuelta dentro del contorno)
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
        //Evita dividir entre cero si dos puntos quedaran a la misma distancia acumulada
        double segmento = Math.max(distActual - distAnterior, 0.0001);
        //t indica qué tan avanzado está entre el punto anterior y el actual (0 = justo en el anterior, 1 = justo en el actual)
        double t = (distanciaObjetivo - distAnterior) / segmento;
        t = Math.max(0, Math.min(1, t));

        //Interpolación lineal: calcula el punto intermedio exacto entre el punto anterior y el actual, según t
        Point2D pAnterior = puntosNormalizados.get(idxAnterior);
        Point2D pActual = puntosNormalizados.get(idx);
        double ix = pAnterior.getX() + (pActual.getX() - pAnterior.getX()) * t;
        double iy = pAnterior.getY() + (pActual.getY() - pAnterior.getY()) * t;

        //Por último, convierte esa coordenada normalizada a coordenadas de píxeles reales en pantalla
        return aPixel(new Point2D(ix, iy), x, y, ancho, alto);
    }
}
