package com.f1manager.infraestructura.ui.screens.carrera;

import com.f1manager.infraestructura.ui.util.GestorEscenas;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Pantalla de Carrera: la sección visualmente más importante de la
 * aplicación. Orquesta tres estados con transición fade entre ellos:
 *   1) Selección de circuito y clima.
 *   2) Animación de la carrera sobre el trazado.
 *   3) Resultados finales (clasificación).
 */
public class PantallaCarrera extends BorderPane {

    private final StackPane areaCentral = new StackPane();
    private boolean animando = false;

    public PantallaCarrera(GestorEscenas gestor) {
        getStyleClass().add("pantalla");
        setPrefSize(1366, 820);
        setTop(construirBarraSuperior(gestor));
        setCenter(areaCentral);

        mostrarSeleccion(gestor);
    }

    private HBox construirBarraSuperior(GestorEscenas gestor) {
        StackPane botonVolver = IconFactory.contenedor(IconFactory.flechaVolver(IconFactory.BLANCO), 40);
        botonVolver.setStyle("-fx-background-color: transparent; -fx-border-color: #232a3d; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        botonVolver.setOnMouseClicked(e -> gestor.volver());

        Label titulo = new Label("CARRERA");
        titulo.getStyleClass().add("titulo-seccion");

        HBox barra = new HBox(20, botonVolver, titulo);
        barra.getStyleClass().add("barra-superior");
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(18, 30, 18, 30));
        return barra;
    }

    private void mostrarSeleccion(GestorEscenas gestor) {
        SeleccionCarreraPane seleccion = new SeleccionCarreraPane((circuito, clima) ->
                cambiar(new AnimacionCarreraPane(circuito, clima, simulacion ->
                        cambiar(new ResultadosCarreraPane(circuito, simulacion,
                                () -> mostrarSeleccion(gestor), gestor::volver)))));
        cambiar(seleccion);
    }

    private void cambiar(Node vista) {
        if (areaCentral.getChildren().isEmpty()) {
            areaCentral.getChildren().setAll(vista);
            return;
        }
        if (animando) {
            areaCentral.getChildren().setAll(vista);
            return;
        }
        animando = true;
        Node actual = areaCentral.getChildren().get(0);
        FadeTransition salida = new FadeTransition(Duration.millis(220), actual);
        salida.setFromValue(1);
        salida.setToValue(0);
        salida.setOnFinished(e -> {
            areaCentral.getChildren().setAll(vista);
            vista.setOpacity(0);
            FadeTransition entrada = new FadeTransition(Duration.millis(260), vista);
            entrada.setFromValue(0);
            entrada.setToValue(1);
            entrada.setOnFinished(ev -> animando = false);
            entrada.play();
        });
        salida.play();
    }
}
