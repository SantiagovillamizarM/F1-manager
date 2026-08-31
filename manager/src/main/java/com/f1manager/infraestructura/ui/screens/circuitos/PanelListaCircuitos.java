package com.f1manager.infraestructura.ui.screens.circuitos;

import com.f1manager.dominio.modelo.Circuito;
import com.f1manager.infraestructura.ui.util.PistaGenerador;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Componente reutilizado por "Listar circuitos" y "Buscar circuito":
 * muestra a la izquierda una lista interactiva de circuitos y a la
 * derecha, al seleccionar uno, su descripción y una representación
 * visual generada de la pista.
 */
public class PanelListaCircuitos extends HBox {

    private final VBox columnaLista = new VBox(12);
    private final StackPane panelDetalle = new StackPane();
    private final String mensajeListaVacia;
    private VBox filaSeleccionada;

    public PanelListaCircuitos(List<Circuito> inicial, String mensajeListaVacia) {
        this.mensajeListaVacia = mensajeListaVacia;
        setSpacing(28);

        ScrollPane scrollLista = new ScrollPane(columnaLista);
        scrollLista.setFitToWidth(true);
        scrollLista.getStyleClass().add("scroll-oscuro");
        scrollLista.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        // Ancho proporcional al del panel completo (no un valor fijo), para que la
        // lista y el detalle mantengan una proporción equilibrada sin importar cuánto
        // espacio termine sobrando (antes el detalle se quedaba con todo lo restante).
        scrollLista.prefWidthProperty().bind(widthProperty().multiply(0.42));
        scrollLista.setMinWidth(340);
        scrollLista.setPrefHeight(560);

        panelDetalle.getStyleClass().add("panel");
        panelDetalle.setPrefSize(560, 560);
        panelDetalle.setPadding(new Insets(24));
        mostrarMensajeSeleccionVacia();

        HBox.setHgrow(scrollLista, Priority.NEVER);
        HBox.setHgrow(panelDetalle, Priority.ALWAYS);
        getChildren().addAll(scrollLista, panelDetalle);

        actualizar(inicial);
    }

    /** Reemplaza el conjunto de circuitos mostrados en la lista (usado por la búsqueda). */
    public void actualizar(List<Circuito> circuitos) {
        columnaLista.getChildren().clear();
        filaSeleccionada = null;
        mostrarMensajeSeleccionVacia();

        if (circuitos.isEmpty()) {
            Label vacio = new Label(mensajeListaVacia);
            vacio.getStyleClass().add("texto-secundario");
            columnaLista.getChildren().add(vacio);
            return;
        }

        for (Circuito circuito : circuitos) {
            columnaLista.getChildren().add(construirFila(circuito));
        }
    }

    private VBox construirFila(Circuito circuito) {
        Label nombre = new Label(circuito.getNombre());
        nombre.getStyleClass().add("texto-normal");
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label detalle = new Label(String.format("ID %d  ·  %s  ·  %.3f km  ·  %d vueltas",
                circuito.getId(), circuito.getPais(), circuito.getLongitudKm(), circuito.getVueltas()));
        detalle.getStyleClass().add("texto-secundario");

        VBox fila = new VBox(4, nombre, detalle);
        fila.getStyleClass().add("fila-lista");
        fila.setOnMouseClicked(e -> seleccionar(circuito, fila));
        return fila;
    }

    private void seleccionar(Circuito circuito, VBox fila) {
        if (filaSeleccionada != null) {
            filaSeleccionada.getStyleClass().remove("fila-lista-seleccionada");
            filaSeleccionada.getStyleClass().add("fila-lista");
        }
        fila.getStyleClass().remove("fila-lista");
        fila.getStyleClass().add("fila-lista-seleccionada");
        filaSeleccionada = fila;

        mostrarDetalle(circuito);
    }

    private void mostrarMensajeSeleccionVacia() {
        Label mensaje = new Label("Selecciona un circuito de la lista\npara ver su información y trazado.");
        mensaje.getStyleClass().add("texto-secundario");
        mensaje.setStyle("-fx-text-alignment: center;");
        panelDetalle.getChildren().setAll(mensaje);
    }

    private void mostrarDetalle(Circuito circuito) {
        Label titulo = new Label(circuito.getNombre());
        titulo.getStyleClass().add("titulo-seccion");

        Label subtitulo = new Label(circuito.getPais() + "  ·  " + circuito.getLongitudKm() + " km  ·  "
                + circuito.getVueltas() + " vueltas  ·  " + String.format("%.1f", circuito.getDistanciaTotalKm()) + " km totales");
        subtitulo.getStyleClass().add("texto-secundario");

        Label descripcion = new Label(circuito.getDescripcion());
        descripcion.getStyleClass().add("texto-normal");
        descripcion.setWrapText(true);

        Canvas lienzo = new Canvas(480, 300);
        GraphicsContext gc = lienzo.getGraphicsContext2D();
        PistaGenerador pista = PistaGenerador.paraCircuito(circuito);
        pista.dibujar(gc, 0, 0, lienzo.getWidth(), lienzo.getHeight(), true);

        VBox contenido = new VBox(14, titulo, subtitulo, lienzo, descripcion);
        contenido.setAlignment(Pos.TOP_LEFT);

        ScrollPane scrollDetalle = new ScrollPane(contenido);
        scrollDetalle.setFitToWidth(true);
        scrollDetalle.getStyleClass().add("scroll-oscuro");
        scrollDetalle.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        panelDetalle.getChildren().setAll(scrollDetalle);
    }
}
