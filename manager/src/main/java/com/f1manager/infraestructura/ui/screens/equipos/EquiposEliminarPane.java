package com.f1manager.infraestructura.ui.screens.equipos;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.excepcion.ValidacionException;
import com.f1manager.dominio.modelo.Equipo;
import com.f1manager.infraestructura.ui.components.CampoBusqueda;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class EquiposEliminarPane extends VBox {

    private final VBox columnaLista = new VBox(10);
    private final TextField campoNombre = new TextField();
    private final Label mensaje = new Label();
    private final CampoBusqueda busqueda = new CampoBusqueda("Buscar por nombre, país o motor...");

    public EquiposEliminarPane() {
        setSpacing(20);

        Label titulo = new Label("Eliminar equipo");
        titulo.getStyleClass().add("titulo-seccion");

        ScrollPane scroll = new ScrollPane(columnaLista);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(360);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox panelAccion = new VBox(12);
        panelAccion.getStyleClass().add("panel-glow");
        panelAccion.setPadding(new Insets(24));
        panelAccion.setMaxWidth(480);

        Label etiqueta = new Label("Ingrese el nombre del equipo a eliminar");
        etiqueta.getStyleClass().add("etiqueta-campo");

        campoNombre.getStyleClass().add("campo-texto");
        campoNombre.setPromptText("Ej: Alpine");

        Button eliminar = new Button("ELIMINAR");
        eliminar.getStyleClass().add("boton-primario");
        eliminar.setOnAction(e -> eliminar());

        Button cancelar = new Button("CANCELAR");
        cancelar.getStyleClass().add("boton-secundario");
        cancelar.setOnAction(e -> {
            campoNombre.clear();
            mensaje.setText("");
        });

        mensaje.getStyleClass().add("error-label");
        mensaje.setWrapText(true);

        HBox botones = new HBox(14, eliminar, cancelar);
        panelAccion.getChildren().addAll(etiqueta, campoNombre, mensaje, botones);

        busqueda.getCampoTexto().textProperty().addListener((obs, viejo, nuevo) -> actualizarLista(nuevo));

getChildren().addAll(titulo, busqueda, scroll, panelAccion);
actualizarLista("");
    }

       private void actualizarLista(String filtro) {
        columnaLista.getChildren().clear();
        String texto = filtro == null ? "" : filtro.trim().toLowerCase();
        var equipos = DataStore.getInstancia().getEquipos().stream()
                .filter(eq -> texto.isEmpty()
                        || eq.getNombre().toLowerCase().contains(texto)
                        || eq.getPais().toLowerCase().contains(texto)
                        || eq.getMotor().toLowerCase().contains(texto))
                .toList();
        if (equipos.isEmpty()) {
            Label vacio = new Label(texto.isEmpty() ? "No hay equipos registrados."
                    : "No se encontraron equipos para \"" + filtro.trim() + "\".");
            vacio.getStyleClass().add("texto-secundario");
            columnaLista.getChildren().add(vacio);
            return;
        }
        for (Equipo eq : equipos) {
            Label linea = new Label(eq.getNombre() + "   ·   " + eq.getPais() + "   ·   " + eq.getMotor());
            linea.getStyleClass().add("texto-normal");
            VBox fila = new VBox(linea);
            fila.getStyleClass().add("fila-lista");
            fila.setOnMouseClicked(e -> campoNombre.setText(eq.getNombre()));
            columnaLista.getChildren().add(fila);
        }
    }

    private void eliminar() {
        try {
            DataStore.getInstancia().eliminarEquipo(campoNombre.getText());
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Equipo eliminado correctamente.");
            campoNombre.clear();
actualizarLista(busqueda.getTexto());       
         } catch (ValidacionException ex) {
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
        }
    }
}
