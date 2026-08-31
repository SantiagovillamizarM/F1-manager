package com.f1manager.infraestructura.ui.screens.circuitos;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.excepcion.ValidacionException;
import com.f1manager.dominio.modelo.Circuito;
import com.f1manager.infraestructura.ui.components.CampoBusqueda;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Sub-vista "Editar circuito": se elige un circuito de la lista (o se
 * ingresa su ID) para cargar sus datos actuales en el formulario; al
 * guardar, los cambios se validan y se persisten tanto en memoria como
 * en MySQL.
 */
public class CircuitosEditarPane extends VBox {

    private final VBox columnaLista = new VBox(10);
    private final CampoBusqueda busqueda = new CampoBusqueda("Buscar por ID, nombre o país...");

    private final TextField campoId = new TextField();
    private final TextField campoNombre = new TextField();
    private final TextField campoPais = new TextField();
    private final TextField campoLongitud = new TextField();
    private final TextField campoVueltas = new TextField();
    private final TextArea campoDescripcion = new TextArea();
    private final Label mensaje = new Label();

    private int idCargado = -1;

    public CircuitosEditarPane() {
        setSpacing(20);

        Label titulo = new Label("Editar circuito");
        titulo.getStyleClass().add("titulo-seccion");

        ScrollPane scroll = new ScrollPane(columnaLista);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(560);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox panelFormulario = new VBox(16);
        panelFormulario.getStyleClass().add("panel-glow");
        panelFormulario.setPadding(new Insets(30));

        campoId.getStyleClass().add("campo-texto");
        campoId.setPromptText("Seleccione un circuito de la lista");
        campoId.setEditable(false);

        campoNombre.getStyleClass().add("campo-texto");
        campoPais.getStyleClass().add("campo-texto");
        campoLongitud.getStyleClass().add("campo-texto");
        campoVueltas.getStyleClass().add("campo-texto");

        campoDescripcion.getStyleClass().add("campo-area");
        campoDescripcion.setPrefRowCount(4);
        campoDescripcion.setWrapText(true);

        mensaje.setWrapText(true);

        Button guardar = new Button("GUARDAR CAMBIOS");
        guardar.getStyleClass().add("boton-primario");
        guardar.setOnAction(e -> guardar());

        Button cancelar = new Button("CANCELAR");
        cancelar.getStyleClass().add("boton-secundario");
        cancelar.setOnAction(e -> limpiar());

        HBox botones = new HBox(14, guardar, cancelar);
        botones.setAlignment(Pos.CENTER_LEFT);

        panelFormulario.getChildren().addAll(
                etiquetaCampo("ID seleccionado"), campoId,
                etiquetaCampo("Nombre"), campoNombre,
                etiquetaCampo("País"), campoPais,
                etiquetaCampo("Longitud (km)"), campoLongitud,
                etiquetaCampo("Vueltas"), campoVueltas,
                etiquetaCampo("Descripción"), campoDescripcion,
                mensaje, botones
        );

        busqueda.getCampoTexto().textProperty().addListener((obs, viejo, nuevo) -> actualizarLista(nuevo));

        VBox columnaIzquierda = new VBox(16, busqueda, scroll);
        columnaIzquierda.setPrefWidth(400);
        columnaIzquierda.setMinWidth(340);
        columnaIzquierda.setMaxWidth(420);

        HBox.setHgrow(panelFormulario, Priority.ALWAYS);
        HBox contenido = new HBox(24, columnaIzquierda, panelFormulario);

        getChildren().addAll(titulo, contenido);
        actualizarLista("");
    }

    private Label etiquetaCampo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("etiqueta-campo");
        return label;
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
            fila.setOnMouseClicked(e -> cargar(c));
            columnaLista.getChildren().add(fila);
        }
    }

    private void cargar(Circuito circuito) {
        idCargado = circuito.getId();
        campoId.setText(String.valueOf(circuito.getId()));
        campoNombre.setText(circuito.getNombre());
        campoPais.setText(circuito.getPais());
        campoLongitud.setText(String.valueOf(circuito.getLongitudKm()));
        campoVueltas.setText(String.valueOf(circuito.getVueltas()));
        campoDescripcion.setText(circuito.getDescripcion());
        mensaje.setText("");
    }

    private void guardar() {
        try {
            if (idCargado < 0) {
                throw new ValidacionException("Seleccione un circuito de la lista para editarlo.");
            }
            DataStore.getInstancia().editarCircuito(String.valueOf(idCargado),
                    campoNombre.getText(), campoPais.getText(),
                    campoLongitud.getText(), campoVueltas.getText(), campoDescripcion.getText());
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Circuito actualizado correctamente.");
            actualizarLista(busqueda.getTexto());
        } catch (ValidacionException ex) {
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
        }
    }

    private void limpiar() {
        idCargado = -1;
        campoId.clear();
        campoNombre.clear();
        campoPais.clear();
        campoLongitud.clear();
        campoVueltas.clear();
        campoDescripcion.clear();
        mensaje.setText("");
    }
}
