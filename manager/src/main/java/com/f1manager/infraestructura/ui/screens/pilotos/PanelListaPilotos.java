package com.f1manager.infraestructura.ui.screens.pilotos;

import com.f1manager.dominio.modelo.Piloto;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Lista interactiva de pilotos. Al seleccionar uno se muestra una ficha
 * ampliada al estilo de un videojuego de gestión deportiva.
 */
public class PanelListaPilotos extends HBox {

    private final VBox columnaLista = new VBox(12);
    private final StackPane panelDetalle = new StackPane();
    private VBox filaSeleccionada;

    public PanelListaPilotos(List<Piloto> pilotos) {
        setSpacing(28);

        ScrollPane scroll = new ScrollPane(columnaLista);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPrefWidth(480);
        scroll.setPrefHeight(560);

        panelDetalle.getStyleClass().add("panel");
        panelDetalle.setPrefSize(560, 560);
        panelDetalle.setPadding(new Insets(28));
        mostrarMensajeVacio();

        HBox.setHgrow(panelDetalle, Priority.ALWAYS);
        getChildren().addAll(scroll, panelDetalle);

        actualizar(pilotos);
    }

    public void actualizar(List<Piloto> pilotos) {
        columnaLista.getChildren().clear();
        filaSeleccionada = null;
        mostrarMensajeVacio();

        if (pilotos.isEmpty()) {
            Label vacio = new Label("No hay pilotos registrados todavía.");
            vacio.getStyleClass().add("texto-secundario");
            columnaLista.getChildren().add(vacio);
            return;
        }
        for (Piloto piloto : pilotos) {
            columnaLista.getChildren().add(construirFila(piloto));
        }
    }

    private VBox construirFila(Piloto piloto) {
        Label nombre = new Label(piloto.getNombre());
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f5f6fa;");

        Label detalle = new Label(String.format("ID %d  ·  %s  ·  %s  ·  Habilidad %d/100",
                piloto.getId(), piloto.getEquipo(), piloto.getRol().getEtiqueta(), piloto.getHabilidad()));
        detalle.getStyleClass().add("texto-secundario");

        VBox fila = new VBox(4, nombre, detalle);
        fila.getStyleClass().add("fila-lista");
        fila.setOnMouseClicked(e -> seleccionar(piloto, fila));
        return fila;
    }

    private void seleccionar(Piloto piloto, VBox fila) {
        if (filaSeleccionada != null) {
            filaSeleccionada.getStyleClass().remove("fila-lista-seleccionada");
            filaSeleccionada.getStyleClass().add("fila-lista");
        }
        fila.getStyleClass().remove("fila-lista");
        fila.getStyleClass().add("fila-lista-seleccionada");
        filaSeleccionada = fila;
        mostrarFicha(piloto);
    }

    private void mostrarMensajeVacio() {
        Label mensaje = new Label("Selecciona un piloto de la lista\npara ver su ficha completa.");
        mensaje.getStyleClass().add("texto-secundario");
        mensaje.setStyle("-fx-text-alignment: center;");
        panelDetalle.getChildren().setAll(mensaje);
    }

    private void mostrarFicha(Piloto piloto) {
        StackPane casco = IconFactory.contenedor(IconFactory.casco(IconFactory.BLANCO), 90);

        Label nombre = new Label(piloto.getNombre());
        nombre.getStyleClass().add("titulo-seccion");

        Label equipoRol = new Label(piloto.getEquipo() + "  ·  " + piloto.getRol().getEtiqueta());
        equipoRol.getStyleClass().add("texto-rojo");

        HBox encabezado = new HBox(20, casco, new VBox(6, nombre, equipoRol));
        encabezado.setAlignment(Pos.CENTER_LEFT);

        GridPane datos = new GridPane();
        datos.setHgap(30);
        datos.setVgap(14);
        datos.setPadding(new Insets(20, 0, 0, 0));

        agregarDato(datos, 0, "ID", String.valueOf(piloto.getId()));
        agregarDato(datos, 1, "Experiencia", piloto.getExperienciaAnios() + " años");
        agregarDato(datos, 2, "Habilidad", piloto.getHabilidad() + " / 100");
        agregarDato(datos, 3, "Rol", piloto.getRol().getEtiqueta());

        VBox barraHabilidad = new VBox(6);
        Label etiquetaBarra = new Label("Nivel de habilidad");
        etiquetaBarra.getStyleClass().add("etiqueta-campo");
        Region fondoBarra = new Region();
        fondoBarra.setStyle("-fx-background-color: #232a3d; -fx-background-radius: 6;");
        fondoBarra.setPrefHeight(14);
        fondoBarra.setMaxWidth(Double.MAX_VALUE);
        Region relleno = new Region();
        relleno.setStyle("-fx-background-color: linear-gradient(to right, #e10600, #ff2b2b); -fx-background-radius: 6;");
        relleno.setPrefHeight(14);
        relleno.setPrefWidth(300 * (piloto.getHabilidad() / 100.0));
        StackPane pilaBarra = new StackPane(fondoBarra, relleno);
        StackPane.setAlignment(relleno, Pos.CENTER_LEFT);
        barraHabilidad.getChildren().addAll(etiquetaBarra, pilaBarra);

        VBox contenido = new VBox(20, encabezado, datos, barraHabilidad);
        panelDetalle.getChildren().setAll(contenido);
    }

    private void agregarDato(GridPane grid, int fila, String etiqueta, String valor) {
        Label labelEtiqueta = new Label(etiqueta);
        labelEtiqueta.getStyleClass().add("etiqueta-campo");
        Label labelValor = new Label(valor);
        labelValor.getStyleClass().add("texto-normal");
        labelValor.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        VBox caja = new VBox(4, labelEtiqueta, labelValor);
        grid.add(caja, fila % 2, fila / 2);
    }
}
