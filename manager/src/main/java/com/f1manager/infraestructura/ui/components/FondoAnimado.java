//Dibuja el fondo animado (el fundido rojo desde abajo y las partículas subiendo) 
//que se ve detrás de toda la aplicación.
package com.f1manager.infraestructura.ui.components;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public class FondoAnimado extends StackPane {

    private static final int CANTIDAD_PARTICULAS = 80;

    private final Canvas lienzo = new Canvas();
    private final List<Particula> particulas = new ArrayList<>();
    private final Random random = new Random();

    public FondoAnimado() {
        setMouseTransparent(true); // es puramente decorativo, no debe bloquear clicks
        getChildren().add(lienzo);
        lienzo.widthProperty().bind(widthProperty());
        lienzo.heightProperty().bind(heightProperty());

        for (int i = 0; i < CANTIDAD_PARTICULAS; i++) {
            particulas.add(nuevaParticula(true));
        }

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                dibujar();
            }
        };
        timer.start();
    }

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

    private void dibujar() {
        double ancho = getWidth();
        double alto = getHeight();
        if (ancho <= 0 || alto <= 0) return;

        GraphicsContext gc = lienzo.getGraphicsContext2D();
        gc.clearRect(0, 0, ancho, alto);

        // --- Fundido rojo desde abajo hacia arriba ---
        LinearGradient fundido = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.55, Color.web("#3a0605", 0.35)),
                new Stop(1, Color.web("#e10600", 0.55))
        );
        gc.setFill(fundido);
        gc.fillRect(0, 0, ancho, alto);

        // --- Partículas ascendentes ---
        for (Particula p : particulas) {
            p.y -= p.velocidad;
            double opacidad = p.opacidadBase * (p.y / alto); // se desvanecen al subir
            if (p.y < -10 || opacidad <= 0.02) {
                Particula nueva = nuevaParticula(false);
                p.x = nueva.x;
                p.y = nueva.y;
                p.velocidad = nueva.velocidad;
                p.radio = nueva.radio;
                p.opacidadBase = nueva.opacidadBase;
                continue;
            }
            gc.setFill(Color.web("#ff2b2b", Math.max(0, Math.min(1, opacidad))));
            gc.fillOval(p.x, p.y, p.radio, p.radio);
        }
    }

    private static class Particula {
        double x, y, velocidad, radio, opacidadBase;
    }
}