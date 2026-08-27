package com.f1manager.infraestructura.ui.screens.equipos;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.excepcion.ValidacionException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class EquiposRegistrarPane extends VBox {

    private final TextField campoNombre = new TextField();
    private final TextField campoPais = new TextField();
    private final TextField campoMotor = new TextField();
    private final Label mensaje = new Label();

    public EquiposRegistrarPane(Runnable alCancelar) {
        setSpacing(20);
        setMaxWidth(600);

        Label titulo = new Label("Registrar nuevo equipo");
        titulo.getStyleClass().add("titulo-seccion");

        VBox panel = new VBox(16);
        panel.getStyleClass().add("panel-glow");
        panel.setPadding(new Insets(30));

        campoNombre.getStyleClass().add("campo-texto");
        campoNombre.setPromptText("Ej: Williams Racing");

        campoPais.getStyleClass().add("campo-texto");
        campoPais.setPromptText("Ej: Reino Unido");

        campoMotor.getStyleClass().add("campo-texto");
        campoMotor.setPromptText("Ej: Mercedes");

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

        panel.getChildren().addAll(
                etiqueta("Nombre del equipo"), campoNombre,
                etiqueta("País"), campoPais,
                etiqueta("Motor"), campoMotor,
                mensaje, botones
        );

        getChildren().addAll(titulo, panel);
    }

    private Label etiqueta(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("etiqueta-campo");
        return label;
    }

    private void guardar() {
        try {
            DataStore.getInstancia().registrarEquipo(campoNombre.getText(), campoPais.getText(), campoMotor.getText());
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Equipo registrado correctamente.");
            limpiar();
        } catch (ValidacionException ex) {
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
        }
    }

    private void limpiar() {
        campoNombre.clear();
        campoPais.clear();
        campoMotor.clear();
    }
}
