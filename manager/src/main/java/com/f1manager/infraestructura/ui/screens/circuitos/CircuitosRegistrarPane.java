package com.f1manager.infraestructura.ui.screens.circuitos;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.excepcion.ValidacionException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Sub-vista "Registrar circuito": formulario con estilo de panel de
 * simulador que valida los datos y los guarda realmente en el sistema.
 */
public class CircuitosRegistrarPane extends VBox {

    private final Runnable alCancelar;
    private final Label mensaje = new Label();

    private final TextField campoNombre = new TextField();
    private final TextField campoPais = new TextField();
    private final TextField campoLongitud = new TextField();
    private final TextField campoVueltas = new TextField();
    private final TextArea campoDescripcion = new TextArea();

    public CircuitosRegistrarPane(Runnable alCancelar) {
        this.alCancelar = alCancelar;
        setSpacing(20);
        setMaxWidth(620);

        Label titulo = new Label("Registrar nuevo circuito");
        titulo.getStyleClass().add("titulo-seccion");

        VBox panelFormulario = new VBox(16);
        panelFormulario.getStyleClass().add("panel-glow");
        panelFormulario.setPadding(new Insets(30));

        campoNombre.getStyleClass().add("campo-texto");
        campoNombre.setPromptText("Ej: Circuit de Monaco");

        campoPais.getStyleClass().add("campo-texto");
        campoPais.setPromptText("Ej: Mónaco");

        campoLongitud.getStyleClass().add("campo-texto");
        campoLongitud.setPromptText("Ej: 5.891");

        campoVueltas.getStyleClass().add("campo-texto");
        campoVueltas.setPromptText("Ej: 52");

        campoDescripcion.getStyleClass().add("campo-area");
        campoDescripcion.setPromptText("Descripción del circuito...");
        campoDescripcion.setPrefRowCount(4);
        campoDescripcion.setWrapText(true);

        mensaje.setWrapText(true);

        Button guardar = new Button("GUARDAR");
        guardar.getStyleClass().add("boton-primario");
        guardar.setOnAction(e -> guardar());

        Button cancelar = new Button("CANCELAR");
        cancelar.getStyleClass().add("boton-secundario");
        cancelar.setOnAction(e -> {
            limpiar();
            alCancelar.run();
        });

        HBox botones = new HBox(14, guardar, cancelar);
        botones.setAlignment(Pos.CENTER_LEFT);

        panelFormulario.getChildren().addAll(
                etiquetaCampo("Nombre"), campoNombre,
                etiquetaCampo("País"), campoPais,
                etiquetaCampo("Longitud (km)"), campoLongitud,
                etiquetaCampo("Vueltas"), campoVueltas,
                etiquetaCampo("Descripción"), campoDescripcion,
                mensaje, botones
        );

        getChildren().addAll(titulo, panelFormulario);
    }

    private Label etiquetaCampo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("etiqueta-campo");
        return label;
    }

    private void guardar() {
        try {
            DataStore.getInstancia().registrarCircuito(
                    campoNombre.getText(), campoPais.getText(),
                    campoLongitud.getText(), campoVueltas.getText(), campoDescripcion.getText());
            mensaje.setText("Circuito registrado correctamente.");
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            limpiar();
        } catch (ValidacionException ex) {
            mensaje.setText(ex.getMessage());
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
        }
    }

    private void limpiar() {
        campoNombre.clear();
        campoPais.clear();
        campoLongitud.clear();
        campoVueltas.clear();
        campoDescripcion.clear();
    }
}
