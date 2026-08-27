package com.f1manager.infraestructura.ui.screens.circuitos;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.excepcion.ValidacionException;
import com.f1manager.dominio.modelo.Circuito;
import com.f1manager.infraestructura.ui.components.CampoBusqueda;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Sub-vista "Eliminar circuito": lista los circuitos actuales y permite
 * eliminar uno ingresando su ID, con validación de errores.
 */
public class CircuitosEliminarPane extends VBox {

    private final VBox columnaLista = new VBox(10);
    private final TextField campoId = new TextField();
    private final Label mensaje = new Label();
    private final CampoBusqueda busqueda = new CampoBusqueda("Buscar por ID, nombre o país...");

    public CircuitosEliminarPane() {
        setSpacing(20);

        Label titulo = new Label("Eliminar circuito");
        titulo.getStyleClass().add("titulo-seccion");

        ScrollPane scroll = new ScrollPane(columnaLista);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(420);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox panelAccion = new VBox(12);
        panelAccion.getStyleClass().add("panel-glow");
        panelAccion.setPadding(new Insets(24));
        panelAccion.setMaxWidth(480);

        Label etiqueta = new Label("Ingrese el ID del circuito a eliminar");
        etiqueta.getStyleClass().add("etiqueta-campo");

        campoId.getStyleClass().add("campo-texto");
        campoId.setPromptText("Ej: 3");

        Button eliminar = new Button("ELIMINAR");
        eliminar.getStyleClass().add("boton-primario");
        eliminar.setOnAction(e -> eliminar());

        Button cancelar = new Button("CANCELAR");
        cancelar.getStyleClass().add("boton-secundario");
        cancelar.setOnAction(e -> {
            campoId.clear();
            mensaje.setText("");
        });

        mensaje.getStyleClass().add("error-label");
        mensaje.setWrapText(true);

        HBox botones = new HBox(14, eliminar, cancelar);
        panelAccion.getChildren().addAll(etiqueta, campoId, mensaje, botones);

        busqueda.getCampoTexto().textProperty().addListener((obs, viejo, nuevo) -> actualizarLista(nuevo));

getChildren().addAll(titulo, busqueda, scroll, panelAccion);
actualizarLista("");
    }

       private void actualizarLista(String filtro) {
        columnaLista.getChildren().clear();
        String texto = filtro == null ? "" : filtro.trim().toLowerCase();
        var circuitos = DataStore.getInstancia().getCircuitos().stream()
                .filter(c -> texto.isEmpty()
                        || String.valueOf(c.getId()).contains(texto)
                        || c.getNombre().toLowerCase().contains(texto)
                        || c.getPais().toLowerCase().contains(texto))
                .toList();
        if (circuitos.isEmpty()) {
            Label vacio = new Label(texto.isEmpty() ? "No hay circuitos registrados."
                    : "No se encontraron circuitos para \"" + filtro.trim() + "\".");
            vacio.getStyleClass().add("texto-secundario");
            columnaLista.getChildren().add(vacio);
            return;
        }
        for (Circuito c : circuitos) {
            Label linea = new Label(String.format("ID %d   %s   ·   %s   ·   %.3f km   ·   %d vueltas",
                    c.getId(), c.getNombre(), c.getPais(), c.getLongitudKm(), c.getVueltas()));
            linea.getStyleClass().add("texto-normal");
            VBox fila = new VBox(linea);
            fila.getStyleClass().add("fila-lista");
            fila.setOnMouseClicked(e -> campoId.setText(String.valueOf(c.getId())));
            columnaLista.getChildren().add(fila);
        }
    }
    private void eliminar() {
        try {
            DataStore.getInstancia().eliminarCircuito(campoId.getText());
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Circuito eliminado correctamente.");
            campoId.clear();
            actualizarLista(busqueda.getTexto());
        } catch (ValidacionException ex) {
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
        }
    }
}
