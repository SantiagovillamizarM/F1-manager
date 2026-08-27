package com.f1manager.infraestructura.ui.screens.circuitos;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.excepcion.ValidacionException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Collections;

/**
 * Sub-vista "Buscar circuito": permite buscar circuitos por país y
 * reutiliza el mismo panel visual de listado/detalle que "Listar circuitos".
 */
public class CircuitosBuscarPane extends VBox {

    public CircuitosBuscarPane() {
        setSpacing(20);

        Label titulo = new Label("Buscar circuito por país");
        titulo.getStyleClass().add("titulo-seccion");

        TextField campoPais = new TextField();
        campoPais.getStyleClass().add("campo-texto");
        campoPais.setPromptText("Ingrese el país, ej: Italia");
        campoPais.setPrefWidth(320);

        Label mensaje = new Label();
        mensaje.getStyleClass().add("error-label");

        PanelListaCircuitos panel = new PanelListaCircuitos(Collections.emptyList(),
                "Ingrese un país y presione Buscar.");

        Button buscar = new Button("BUSCAR");
        buscar.getStyleClass().add("boton-primario");
        buscar.setOnAction(e -> {
            try {
                var resultados = DataStore.getInstancia().buscarCircuitosPorPais(campoPais.getText());
                mensaje.setText("");
                panel.actualizar(resultados);
                if (resultados.isEmpty()) {
                    mensaje.setText("No se encontraron circuitos para \"" + campoPais.getText().trim() + "\".");
                }
            } catch (ValidacionException ex) {
                mensaje.setText(ex.getMessage());
            }
        });
        campoPais.setOnAction(e -> buscar.fire());

        HBox filaBusqueda = new HBox(14, campoPais, buscar);
        filaBusqueda.setAlignment(Pos.CENTER_LEFT);

        VBox encabezado = new VBox(10, filaBusqueda, mensaje);
        encabezado.setPadding(new Insets(0, 0, 6, 0));

        getChildren().addAll(titulo, encabezado, panel);
    }
}
