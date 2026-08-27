//Define cómo se ve y se comporta una tarjeta clicable 
//(las que aparecen en los menús, como "GESTIÓN DE CIRCUITOS").
package com.f1manager.infraestructura.ui.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
public class TarjetaOpcion extends VBox {

    public TarjetaOpcion(Node icono, String titulo, Runnable accion, boolean grande) {
        getStyleClass().add(grande ? "tarjeta-grande" : "tarjeta");
        setAlignment(Pos.CENTER);
        setSpacing(grande ? 22 : 14);
        setPrefSize(grande ? 420 : 210, grande ? 300 : 190);
        setMinSize(grande ? 320 : 170, grande ? 240 : 160);
        setMaxSize(grande ? 420 : 210, grande ? 300 : 190);

        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add(grande ? "tarjeta-titulo-grande" : "tarjeta-titulo");
        etiqueta.setWrapText(true);
        etiqueta.setAlignment(Pos.CENTER);
        etiqueta.setStyle("-fx-text-alignment: center;");

        getChildren().addAll(icono, etiqueta);
        setOnMouseClicked(e -> accion.run());
    }
}
