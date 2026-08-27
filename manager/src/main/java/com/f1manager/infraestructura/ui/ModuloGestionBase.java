package com.f1manager.infraestructura.ui;

import com.f1manager.infraestructura.ui.util.GestorEscenas;
import com.f1manager.infraestructura.ui.components.BarraMiniIconos;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;

/**
 * Clase base compartida por todos los módulos de gestión (Circuitos,
 * Pilotos, Equipos, Vehículos). Centraliza:
 *
 *   - La barra superior con botón de "volver" y título del módulo.
 *   - El área central donde se intercambian las distintas sub-pantallas
 *     (listar / registrar / buscar / eliminar) mediante un fade suave.
 *   - La instalación de la barra lateral de mini íconos una vez que el
 *     usuario entra a alguna de las sub-secciones.
 */
public abstract class ModuloGestionBase extends BorderPane {

    protected final GestorEscenas gestor;
    private final StackPane areaCentral = new StackPane();
    private boolean animandoCentro = false;

    protected ModuloGestionBase(GestorEscenas gestor, String tituloModulo) {
        this.gestor = gestor;
        getStyleClass().add("pantalla");
        setPrefSize(1366, 820);
        setTop(construirBarraSuperior(tituloModulo));

        areaCentral.setPadding(new Insets(34));
        ScrollPane scroll = new ScrollPane(areaCentral);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setCenter(scroll);
    }

    private HBox construirBarraSuperior(String titulo) {
        StackPane botonVolver = IconFactory.contenedor(IconFactory.flechaVolver(IconFactory.BLANCO), 40);
        botonVolver.setStyle("-fx-background-color: transparent; -fx-border-color: #232a3d; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        botonVolver.setOnMouseClicked(e -> gestor.volver());

        Label etiquetaTitulo = new Label(titulo);
        etiquetaTitulo.getStyleClass().add("titulo-seccion");

        HBox barra = new HBox(20, botonVolver, etiquetaTitulo);
        barra.getStyleClass().add("barra-superior");
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(18, 30, 18, 30));
        return barra;
    }

    /** Instala (o reemplaza) la barra lateral de mini íconos de navegación interna. */
    protected void establecerBarraLateral(List<BarraMiniIconos.Item> items, int indiceActivo) {
        setLeft(new BarraMiniIconos(items, indiceActivo));
    }

    /** Quita la barra lateral (usado en la vista inicial de tarjetas grandes). */
    protected void quitarBarraLateral() {
        setLeft(null);
    }

    /** Cambia el contenido del área central aplicando una transición fade suave y rápida. */
    protected void mostrarEnCentro(Node vista) {
        if (animandoCentro) {
            areaCentral.getChildren().setAll(vista);
            return;
        }
        if (areaCentral.getChildren().isEmpty()) {
            areaCentral.getChildren().setAll(vista);
            vista.setOpacity(0);
            FadeTransition entrada = new FadeTransition(Duration.millis(200), vista);
            entrada.setFromValue(0);
            entrada.setToValue(1);
            entrada.play();
            return;
        }
        animandoCentro = true;
        Node actual = areaCentral.getChildren().get(0);
        FadeTransition salida = new FadeTransition(Duration.millis(140), actual);
        salida.setFromValue(1);
        salida.setToValue(0);
        salida.setOnFinished(e -> {
            areaCentral.getChildren().setAll(vista);
            vista.setOpacity(0);
            FadeTransition entrada = new FadeTransition(Duration.millis(180), vista);
            entrada.setFromValue(0);
            entrada.setToValue(1);
            entrada.setOnFinished(ev -> animandoCentro = false);
            entrada.play();
        });
        salida.play();
    }
}
