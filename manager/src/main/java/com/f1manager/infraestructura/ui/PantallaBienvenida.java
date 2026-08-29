package com.f1manager.infraestructura.ui;

import com.f1manager.infraestructura.ui.util.GestorEscenas;
import com.f1manager.infraestructura.ui.util.GestorSonido;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Pantalla de bienvenida mostrada al iniciar el programa.
 *
 * Comienza completamente oscura y el logo, el título y el subtítulo van
 * apareciendo progresivamente mediante fundidos (fade), como la intro de
 * un videojuego o simulador profesional. Al presionar cualquier tecla (o
 * hacer click), se produce un fade suave hacia el menú principal.
 */
public class PantallaBienvenida extends StackPane {

    private boolean avanzando = false;
    private boolean puedeAvanzar = false;

    public PantallaBienvenida(GestorEscenas gestor) {
        getStyleClass().add("pantalla");
        setPrefSize(1366, 820);

        var logo = IconFactory.logo(2.0);

        Label titulo = new Label("Bienvenido al F1 Manager");
        titulo.getStyleClass().add("titulo-principal");
        titulo.setOpacity(0);

        Label subtitulo = new Label("Cargando");
        subtitulo.getStyleClass().add("subtitulo");
        subtitulo.setOpacity(0);

        VBox caja = new VBox(26, logo, titulo, subtitulo);
        caja.setAlignment(Pos.CENTER);
        logo.setOpacity(0);

        getChildren().add(caja);
        setOpacity(0);

        // --- Secuencia de aparición progresiva ---
        FadeTransition fadePantalla = new FadeTransition(Duration.millis(600), this);
        fadePantalla.setFromValue(0);
        fadePantalla.setToValue(1);

        FadeTransition fadeLogo = new FadeTransition(Duration.millis(900), logo);
        fadeLogo.setFromValue(0);
        fadeLogo.setToValue(1);

        FadeTransition fadeTitulo = new FadeTransition(Duration.millis(700), titulo);
        fadeTitulo.setFromValue(0);
        fadeTitulo.setToValue(1);

        FadeTransition fadeSubtitulo = new FadeTransition(Duration.millis(700), subtitulo);
        fadeSubtitulo.setFromValue(0);
        fadeSubtitulo.setToValue(1);

        new SequentialTransition(fadePantalla, fadeLogo, fadeTitulo, fadeSubtitulo).play();

        // Solo suena una vez: esta pantalla solo se construye una vez, al arrancar la aplicación.
        GestorSonido.reproducir("Intro audio.m4a");

        // "Cargando..." con los puntos escribiéndose mientras suena la intro, para que la espera no se vea rara.
        int[] puntos = {0};
        Timeline animacionCarga = new Timeline(new KeyFrame(Duration.millis(400), e -> {
            puntos[0] = (puntos[0] + 1) % 4;
            subtitulo.setText("Cargando" + ".".repeat(puntos[0]));
        }));
        animacionCarga.setCycleCount(Timeline.INDEFINITE);
        animacionCarga.play();

        // Al terminar la carga (4.5s), se cambia el mensaje y recién ahí se puede avanzar.
        PauseTransition esperaParaAvanzar = new PauseTransition(Duration.seconds(4.5));
        esperaParaAvanzar.setOnFinished(e -> {
            animacionCarga.stop();
            subtitulo.setText("Presiona cualquier tecla para continuar");
            puedeAvanzar = true;
        });
        esperaParaAvanzar.play();

        // --- Avanzar al menú principal con cualquier tecla ---
        sceneProperty().addListener((obs, anterior, nuevaEscena) -> {
            if (nuevaEscena != null) {
                nuevaEscena.setOnKeyPressed(e -> avanzar(gestor));
            }
        });
        setOnMouseClicked(e -> avanzar(gestor));
        setFocusTraversable(true);
    }

    private void avanzar(GestorEscenas gestor) {
        if (!puedeAvanzar || avanzando) {
            return;
        }
        avanzando = true;
        gestor.navegarA(new MenuPrincipal(gestor));
    }
}
