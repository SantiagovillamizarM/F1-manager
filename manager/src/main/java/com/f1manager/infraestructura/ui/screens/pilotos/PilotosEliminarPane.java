package com.f1manager.infraestructura.ui.screens.pilotos;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.excepcion.ValidacionException;
import com.f1manager.infraestructura.ui.components.CampoBusqueda;
import com.f1manager.dominio.modelo.Piloto;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;


public class PilotosEliminarPane extends VBox {

    private final VBox columnaLista = new VBox(10);
    private final TextField campoId = new TextField();
    private final Label mensaje = new Label();
    private final CampoBusqueda busqueda = new CampoBusqueda("Buscar por ID, nombre o equipo...");

    public PilotosEliminarPane() {
        setSpacing(20);

        Label titulo = new Label("Eliminar piloto");
        titulo.getStyleClass().add("titulo-seccion");

        ScrollPane scroll = new ScrollPane(columnaLista);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox panelAccion = new VBox(12);
        panelAccion.getStyleClass().add("panel-glow");
        panelAccion.setPadding(new Insets(24));
        panelAccion.setMaxWidth(480);

        Label etiqueta = new Label("Ingrese el ID del piloto a eliminar");
        etiqueta.getStyleClass().add("etiqueta-campo");

        campoId.getStyleClass().add("campo-texto");
        campoId.setPromptText("Ej: 4");

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
        var pilotos = DataStore.getInstancia().getPilotos().stream()
                .filter(p -> texto.isEmpty()
                        || String.valueOf(p.getId()).contains(texto)
                        || p.getNombre().toLowerCase().contains(texto)
                        || p.getEquipo().toLowerCase().contains(texto))
                .toList();
        if (pilotos.isEmpty()) {
            Label vacio = new Label(texto.isEmpty() ? "No hay pilotos registrados."
                    : "No se encontraron pilotos para \"" + filtro.trim() + "\".");
            vacio.getStyleClass().add("texto-secundario");
            columnaLista.getChildren().add(vacio);
            return;
        }
        for (Piloto p : pilotos) {
            StackPane avatar = IconFactory.avatarPiloto(p, 34);
            Label linea = new Label(String.format("ID %d   %s   ·   %s   ·   %s",
                    p.getId(), p.getNombre(), p.getEquipo(), p.getRol().getEtiqueta()));
            linea.getStyleClass().add("texto-normal");
            HBox fila = new HBox(12, avatar, linea);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getStyleClass().add("fila-lista");
            fila.setOnMouseClicked(e -> campoId.setText(String.valueOf(p.getId())));
            columnaLista.getChildren().add(fila);
        }
    }
    private void eliminar() {
        try {
            DataStore.getInstancia().eliminarPiloto(campoId.getText());
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Piloto eliminado correctamente.");
            campoId.clear();
           actualizarLista(busqueda.getTexto());
        } catch (ValidacionException ex) {
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
        }
    }
}
