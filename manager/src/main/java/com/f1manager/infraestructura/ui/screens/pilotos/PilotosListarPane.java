package com.f1manager.infraestructura.ui.screens.pilotos;

import com.f1manager.infraestructura.persistencia.DataStore;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PilotosListarPane extends VBox {
    public PilotosListarPane() {
        setSpacing(20);
        Label titulo = new Label("Pilotos registrados");
        titulo.getStyleClass().add("titulo-seccion");
        PanelListaPilotos panel = new PanelListaPilotos(DataStore.getInstancia().getPilotos());
        getChildren().addAll(titulo, panel);
    }
}
