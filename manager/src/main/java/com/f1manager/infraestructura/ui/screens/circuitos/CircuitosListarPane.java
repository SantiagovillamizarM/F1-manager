package com.f1manager.infraestructura.ui.screens.circuitos;

import com.f1manager.infraestructura.persistencia.DataStore;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Sub-vista "Listar circuitos": muestra todos los circuitos registrados
 * y permite ver la información ampliada y el trazado de cada uno.
 */
public class CircuitosListarPane extends VBox {

    public CircuitosListarPane() {
        setSpacing(20);

        Label titulo = new Label("Circuitos registrados");
        titulo.getStyleClass().add("titulo-seccion");

        PanelListaCircuitos panel = new PanelListaCircuitos(
                DataStore.getInstancia().getCircuitos(), "No hay circuitos registrados todavía.");

        getChildren().addAll(titulo, panel);
    }
}
