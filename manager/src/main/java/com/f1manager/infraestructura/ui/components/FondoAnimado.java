//Dibuja el fondo animado (el fundido rojo desde abajo y las partículas subiendo)
//que se ve detrás de toda la aplicación.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.components;

//Trae AnimationTimer, que ejecuta código en cada fotograma (frame) para hacer animaciones fluidas
import javafx.animation.AnimationTimer;
//Trae Canvas, un lienzo en blanco de JavaFX donde se puede dibujar a mano (líneas, rectángulos, óvalos, etc)
import javafx.scene.canvas.Canvas;
//Trae GraphicsContext, la "herramienta de dibujo" que se usa para pintar sobre el Canvas
import javafx.scene.canvas.GraphicsContext;
//Trae el StackPane, un layout que apila los elementos uno encima de otro
import javafx.scene.layout.StackPane;
//Trae Color, para definir colores en JavaFX
import javafx.scene.paint.Color;
//Trae CycleMethod, que define cómo se repite un degradado (gradient) si no cubre todo el espacio
import javafx.scene.paint.CycleMethod;
//Trae LinearGradient, un degradado (transición de un color a otro) en línea recta
import javafx.scene.paint.LinearGradient;
//Trae Stop, que marca en qué punto del degradado va cada color
import javafx.scene.paint.Stop;

//Importa la clase de ArrayList para poder usar listas (Son las que tienen <>)
import java.util.ArrayList;
//Importa la interfaz List, que define el comportamiento general de una lista en Java
import java.util.List;
//Trae la herramienta oficial de Java para generar valores aleatorios o al azar en tu programa
import java.util.Random;

//Clase pública llamada "FondoAnimado" que hereda de StackPane (un layout que apila los elementos uno encima de otro)
public class FondoAnimado extends StackPane {

    //Constante privada y fija (final) que dice cuántas partículas (los puntitos que suben) hay en pantalla
    private static final int CANTIDAD_PARTICULAS = 80;

    //Lienzo privado y fijo donde se dibuja el fundido rojo y las partículas
    private final Canvas lienzo = new Canvas();
    //Lista privada y fija con todas las partículas activas en este momento
    private final List<Particula> particulas = new ArrayList<>();
    //Generador de números aleatorios para la posición, velocidad, tamaño y opacidad de cada partícula
    private final Random random = new Random();

    //Constructor
    //Arma el fondo animado: prepara el lienzo (Canvas) para que ocupe todo el espacio disponible, crea las
    //partículas iniciales y arranca un AnimationTimer que redibuja el fondo en cada fotograma.
    public FondoAnimado() {
        setMouseTransparent(true); // es puramente decorativo, no debe bloquear clicks
        getChildren().add(lienzo);
        //El ancho y el alto del lienzo quedan "atados" (bind) al ancho y alto de este panel, así siempre lo cubre entero
        lienzo.widthProperty().bind(widthProperty());
        lienzo.heightProperty().bind(heightProperty());

        //Crea todas las partículas iniciales, ya repartidas por toda la pantalla (no todas abajo)
        for (int i = 0; i < CANTIDAD_PARTICULAS; i++) {
            particulas.add(nuevaParticula(true));
        }

        //AnimationTimer es un temporizador especial de JavaFX que llama a handle() en cada fotograma (muchas veces por segundo)
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                dibujar();
            }
        };
        timer.start();
    }

    //Crea una partícula nueva con posición, velocidad, radio y opacidad aleatorios. Si posicionInicialAleatoria es
    //true, puede aparecer en cualquier parte de la pantalla (se usa al arrancar); si es false, aparece justo debajo
    //del borde inferior (se usa cuando una partícula vieja "muere" y hay que reemplazarla, para que no se note el cambio).
    private Particula nuevaParticula(boolean posicionInicialAleatoria) {
        double ancho = Math.max(getWidth(), 1);
        double alto = Math.max(getHeight(), 1);
        Particula p = new Particula();
        p.x = random.nextDouble() * ancho;
        p.y = posicionInicialAleatoria ? random.nextDouble() * alto : alto + random.nextDouble() * 40;
        p.velocidad = 0.4 + random.nextDouble() * 1.1;
        p.radio = 1.0 + random.nextDouble() * 2.2;
        p.opacidadBase = 0.25 + random.nextDouble() * 0.5;
        return p;
    }

    //Este es el método que se llama en cada fotograma: limpia el lienzo, pinta el degradado rojo de abajo hacia
    //arriba y después mueve y dibuja cada partícula, haciendo que las que "mueren" (se salen de la pantalla o se
    //vuelven invisibles) se reemplacen por una nueva que entra por abajo.
    private void dibujar() {
        double ancho = getWidth();
        double alto = getHeight();
        if (ancho <= 0 || alto <= 0) return;

        GraphicsContext gc = lienzo.getGraphicsContext2D();
        gc.clearRect(0, 0, ancho, alto);

        // --- Fundido rojo desde abajo hacia arriba ---
        //Degradado que va de transparente arriba a rojo semi-transparente abajo, simulando un resplandor
        LinearGradient fundido = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.55, Color.web("#3a0605", 0.35)),
                new Stop(1, Color.web("#e10600", 0.55))
        );
        gc.setFill(fundido);
        gc.fillRect(0, 0, ancho, alto);

        // --- Partículas ascendentes ---
        //Recorre todas las partículas para moverlas hacia arriba y dibujarlas
        for (Particula p : particulas) {
            p.y -= p.velocidad;
            double opacidad = p.opacidadBase * (p.y / alto); // se desvanecen al subir
            //Si la partícula ya se salió de la pantalla por arriba o ya es casi invisible, se reemplaza por una nueva que entra por abajo
            if (p.y < -10 || opacidad <= 0.02) {
                Particula nueva = nuevaParticula(false);
                p.x = nueva.x;
                p.y = nueva.y;
                p.velocidad = nueva.velocidad;
                p.radio = nueva.radio;
                p.opacidadBase = nueva.opacidadBase;
                continue;
            }
            //Dibuja la partícula como un óvalo (prácticamente un puntito) del color rojo con su opacidad actual
            gc.setFill(Color.web("#ff2b2b", Math.max(0, Math.min(1, opacidad))));
            gc.fillOval(p.x, p.y, p.radio, p.radio);
        }
    }

    //Clase privada interna que solo guarda los datos de una partícula: su posición (x, y), su velocidad, su
    //radio (tamaño) y su opacidad base (qué tan visible es antes de empezar a desvanecerse al subir).
    private static class Particula {
        double x, y, velocidad, radio, opacidadBase;
    }
}
