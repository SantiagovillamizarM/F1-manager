package com.f1manager.infraestructura.ui;

import com.f1manager.infraestructura.ui.util.GestorEscenas;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
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

    public PantallaBienvenida(GestorEscenas gestor) {
        getStyleClass().add("pantalla");
        setPrefSize(1366, 820);

        var logo = IconFactory.logo(2.0);

        Label titulo = new Label("Bienvenido al F1 Manager");
        titulo.getStyleClass().add("titulo-principal");
        titulo.setOpacity(0);

        Label subtitulo = new Label("Presiona cualquier tecla para continuar");
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
        if (avanzando) {
            return;
        }
        avanzando = true;
        gestor.navegarA(new MenuPrincipal(gestor));
    }
}
