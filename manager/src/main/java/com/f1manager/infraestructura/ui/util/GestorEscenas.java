//Controla el cambio entre pantallas de toda la aplicación, aplicando las transiciones de 
// fundido (fade) y guardando el historial para poder "volver".
package com.f1manager.infraestructura.ui.util;
import com.f1manager.infraestructura.ui.components.FondoAnimado;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayDeque;
import java.util.Deque;


public final class GestorEscenas {
      private final StackPane contenido = new StackPane();

    private static final Duration DURACION_FADE = Duration.millis(260);

    /** Resolución "de diseño" sobre la que están maquetadas todas las pantallas. */
    private static final double ANCHO_DISENO = 1366;
    private static final double ALTO_DISENO = 820;

    private final Stage stage;
    private final StackPane raiz = new StackPane();
    private final Deque<Region> historial = new ArrayDeque<>();
    private boolean animando = false;


    public GestorEscenas(Stage stage) {
        this.stage = stage;
        raiz.setStyle("-fx-background-color: #05070d;");
        FondoAnimado fondo = new FondoAnimado();
        fondo.prefWidthProperty().bind(raiz.widthProperty());
        fondo.prefHeightProperty().bind(raiz.heightProperty());

        // El contenido siempre se maqueta al tamaño de diseño fijo y luego se
        // escala como un todo para que quepa en la ventana real, sin recortes
        // ni reflujo de los layouts internos (que no son responsive).
        contenido.setMinSize(ANCHO_DISENO, ALTO_DISENO);
        contenido.setPrefSize(ANCHO_DISENO, ALTO_DISENO);
        contenido.setMaxSize(ANCHO_DISENO, ALTO_DISENO);
        Scale escalaContenido = new Scale(1, 1, ANCHO_DISENO / 2.0, ALTO_DISENO / 2.0);
        contenido.getTransforms().add(escalaContenido);

        raiz.getChildren().addAll(fondo, contenido);
        Scene escena = new Scene(raiz, 1366, 820);
        escena.getStylesheets().add(getClass().getResource("/estilo.css").toExternalForm());
        stage.setScene(escena);
        stage.setTitle("F1 Manager");
        stage.setMinWidth(1024);
        stage.setMinHeight(680);
        stage.setMaximized(true);

        javafx.beans.value.ChangeListener<Number> actualizarEscala = (obs, anterior, nuevo) -> {
            if (raiz.getWidth() <= 0 || raiz.getHeight() <= 0) {
                return;
            }
            double factor = Math.min(raiz.getWidth() / ANCHO_DISENO, raiz.getHeight() / ALTO_DISENO);
            escalaContenido.setX(factor);
            escalaContenido.setY(factor);
        };
        raiz.widthProperty().addListener(actualizarEscala);
        raiz.heightProperty().addListener(actualizarEscala);
    }

    public Stage getStage() {
        return stage;
    }

    /** Muestra la primera pantalla de la aplicación sin transición (no hay nada previo). */
    public void mostrarInicial(Region vista) {
        contenido.getChildren().setAll(vista);
        historial.clear();
        historial.push(vista);
    }

    /** Navega hacia una nueva pantalla, apilándola en el historial, con transición fade. */
    public void navegarA(Region vista) {
        cambiarCon(vista, true);
    }

    /** Reemplaza la pantalla actual sin apilar en el historial (por ejemplo, al cambiar de sub-sección). */
    public void reemplazar(Region vista) {
        cambiarCon(vista, false);
    }

    /** Vuelve a la pantalla anterior del historial, si existe. */
    public void volver() {
        if (historial.size() <= 1) {
            return;
        }
        historial.pop();
        Region anterior = historial.peek();
        cambiarConInterno(anterior, false);
    }

    private void cambiarCon(Region vista, boolean apilar) {
        if (apilar) {
            historial.push(vista);
        }
        cambiarConInterno(vista, false);
    }

       private void cambiarConInterno(Region vista, boolean ignorar) {
        if (animando) {
            return;
        }
        Node actual = contenido.getChildren().isEmpty() ? null : contenido.getChildren().get(0);

        if (actual == null) {
            vista.setOpacity(0);
            contenido.getChildren().setAll(vista);
            FadeTransition entrada = new FadeTransition(DURACION_FADE, vista);
            entrada.setFromValue(0);
            entrada.setToValue(1);
            entrada.play();
            return;
        }

        animando = true;
        FadeTransition salida = new FadeTransition(DURACION_FADE, actual);
        salida.setFromValue(1);
        salida.setToValue(0);

        vista.setOpacity(0);
        FadeTransition entrada = new FadeTransition(DURACION_FADE, vista);
        entrada.setFromValue(0);
        entrada.setToValue(1);

        salida.setOnFinished(e -> {
            contenido.getChildren().setAll(vista);
            entrada.play();
        });
        entrada.setOnFinished(e -> animando = false);

        new SequentialTransition(salida).play();
    }

    /** Cierra correctamente la aplicación. */
    public void salir() {
        Platform.exit();
    }
}
