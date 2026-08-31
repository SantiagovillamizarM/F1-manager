//Controla el cambio entre pantallas de toda la aplicación, aplicando las transiciones de 
// fundido (fade) y guardando el historial para poder "volver".
package com.f1manager.infraestructura.ui.util;
import com.f1manager.infraestructura.ui.components.FondoAnimado;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayDeque;
import java.util.Deque;


public final class GestorEscenas {
      private final StackPane contenido = new StackPane();

    private static final Duration DURACION_FADE = Duration.millis(260);

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
        raiz.getChildren().addAll(fondo, contenido);
        Scene escena = new Scene(raiz, 1366, 820);
        escena.getStylesheets().add(getClass().getResource("/estilo.css").toExternalForm());
        escena.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (esBotonOHijoDeBoton(e.getTarget())) {
                GestorSonido.reproducir("Click.mp3");
            }
        });
        stage.setScene(escena);
        stage.setTitle("F1 Manager");
        stage.setMinWidth(1024);
        stage.setMinHeight(680);
        stage.setMaximized(true);
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
        // Se ignora mientras haya una transición en curso: si no, clics rápidos
        // podían desapilar varias veces aunque solo la primera transición se
        // llegara a mostrar, desincronizando el historial de lo que se ve en
        // pantalla (y dejando "saltar" pantallas intermedias, como el menú
        // principal, en un "volver" posterior).
        if (animando || historial.size() <= 1) {
            return;
        }
        historial.pop();
        Region anterior = historial.peek();
        cambiarConInterno(anterior, false);
    }

    private void cambiarCon(Region vista, boolean apilar) {
        if (animando) {
            return;
        }
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
        GestorSonido.reproducir("Transicion de esena.m4a");

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

    /**
     * El click puede caer sobre un hijo interno del elemento clicable (su texto o ícono),
     * por eso se sube por los padres. Cualquier nodo con un manejador de click propio
     * (tarjetas de menú, íconos de la barra lateral, botón de volver) cuenta como "botón",
     * además de los controles Button/ButtonBase reales.
     */
    private static boolean esBotonOHijoDeBoton(EventTarget objetivo) {
        Node nodo = objetivo instanceof Node ? (Node) objetivo : null;
        while (nodo != null) {
            if (nodo instanceof ButtonBase || nodo.getOnMouseClicked() != null) {
                return true;
            }
            nodo = nodo.getParent();
        }
        return false;
    }

    /** Cierra correctamente la aplicación. */
    public void salir() {
        Platform.exit();
    }
}
