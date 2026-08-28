package com.f1manager.infraestructura.ui.screens.pilotos;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.excepcion.ValidacionException;
import com.f1manager.dominio.modelo.RolPiloto;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PilotosRegistrarPane extends VBox {

    private final TextField campoNombre = new TextField();
    private final ComboBox<String> comboEquipo = new ComboBox<>();
    private final ComboBox<RolPiloto> comboRol = new ComboBox<>();
    private final TextField campoExperiencia = new TextField();
    private final TextField campoSeco = new TextField();
    private final TextField campoLluvia = new TextField();
    private final TextField campoExtremo = new TextField();
    private final TextField campoCurva = new TextField();
    private final TextField campoAdelantamiento = new TextField();
    private final TextField campoRecta = new TextField();
    private final Label mensaje = new Label();

    public PilotosRegistrarPane(Runnable alCancelar) {
        setSpacing(20);
        setMaxWidth(620);

        Label titulo = new Label("Registrar nuevo piloto");
        titulo.getStyleClass().add("titulo-seccion");

        VBox panel = new VBox(16);
        panel.getStyleClass().add("panel-glow");
        panel.setPadding(new Insets(30));

        campoNombre.getStyleClass().add("campo-texto");
        campoNombre.setPromptText("Nombre completo del piloto");

        comboEquipo.getStyleClass().add("combo-oscuro");
        comboEquipo.setMaxWidth(Double.MAX_VALUE);
        comboEquipo.setPromptText("Seleccione un equipo");
        actualizarEquipos();

        comboRol.getStyleClass().add("combo-oscuro");
        comboRol.setMaxWidth(Double.MAX_VALUE);
        comboRol.setItems(FXCollections.observableArrayList(RolPiloto.values()));
        comboRol.setPromptText("Seleccione un rol");

        campoExperiencia.getStyleClass().add("campo-texto");
        campoExperiencia.setPromptText("Ej: 5");

        campoSeco.getStyleClass().add("campo-texto");
        campoSeco.setPromptText("Ej: 88 (1 a 100)");

        campoLluvia.getStyleClass().add("campo-texto");
        campoLluvia.setPromptText("Ej: 88 (1 a 100)");

        campoExtremo.getStyleClass().add("campo-texto");
        campoExtremo.setPromptText("Ej: 88 (1 a 100)");

        campoCurva.getStyleClass().add("campo-texto");
        campoCurva.setPromptText("Ej: 88 (1 a 100)");

        campoAdelantamiento.getStyleClass().add("campo-texto");
        campoAdelantamiento.setPromptText("Ej: 88 (1 a 100)");

        campoRecta.getStyleClass().add("campo-texto");
        campoRecta.setPromptText("Ej: 88 (1 a 100)");

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
                etiqueta("Nombre"), campoNombre,
                etiqueta("Equipo"), comboEquipo,
                etiqueta("Rol"), comboRol,
                etiqueta("Años de experiencia"), campoExperiencia,
                etiqueta("Habilidad en seco (1-100)"), campoSeco,
                etiqueta("Habilidad en lluvia (1-100)"), campoLluvia,
                etiqueta("Habilidad en clima extremo (1-100)"), campoExtremo,
                etiqueta("Habilidad en curva (1-100)"), campoCurva,
                etiqueta("Habilidad de adelantamiento (1-100)"), campoAdelantamiento,
                etiqueta("Habilidad en recta (1-100)"), campoRecta,
                mensaje, botones
        );

        getChildren().addAll(titulo, panel);
    }

    private void actualizarEquipos() {
        var nombres = DataStore.getInstancia().getEquipos().stream()
                .map(com.f1manager.dominio.modelo.Equipo::getNombre).toList();
        comboEquipo.setItems(FXCollections.observableArrayList(nombres));
    }

    private Label etiqueta(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("etiqueta-campo");
        return label;
    }

    private void guardar() {
        try {
            actualizarEquipos();
            DataStore.getInstancia().registrarPiloto(
                    campoNombre.getText(), comboEquipo.getValue(), comboRol.getValue(),
                    campoExperiencia.getText(),
                    campoCurva.getText(), campoAdelantamiento.getText(), campoRecta.getText(),
                    campoLluvia.getText(), campoSeco.getText(), campoExtremo.getText());
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Piloto registrado correctamente.");
            limpiar();
        } catch (ValidacionException ex) {
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
        }
    }

    private void limpiar() {
        campoNombre.clear();
        comboEquipo.setValue(null);
        comboRol.setValue(null);
        campoExperiencia.clear();
        campoSeco.clear();
        campoLluvia.clear();
        campoExtremo.clear();
        campoCurva.clear();
        campoAdelantamiento.clear();
        campoRecta.clear();
    }
}
