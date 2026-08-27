//Define la barra lateral de mini íconos que aparece dentro de cada módulo 
//(Listar/Registrar/Buscar/Eliminar).
package com.f1manager.infraestructura.ui.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
public class BarraMiniIconos extends VBox {

    /** Un elemento de la barra: ícono, texto descriptivo y acción a ejecutar al hacer click. */
    public record Item(Node icono, String descripcion, Runnable accion) {
    }

    public BarraMiniIconos(List<Item> items, int indiceActivo) {
        getStyleClass().add("barra-mini-iconos");
        setAlignment(Pos.TOP_CENTER);
        setPrefWidth(70);
        setMinWidth(70);

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            StackPane contenedor = new StackPane(item.icono());
            contenedor.setPrefSize(46, 46);
            contenedor.getStyleClass().add(i == indiceActivo ? "mini-icono-activo" : "mini-icono");
            Tooltip.install(contenedor, new Tooltip(item.descripcion()));
            contenedor.setOnMouseClicked(e -> item.accion().run());
            getChildren().add(contenedor);
        }
    }
}
