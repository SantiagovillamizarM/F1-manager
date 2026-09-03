//Define la barra lateral de mini íconos que aparece dentro de cada módulo
//(Listar/Registrar/Buscar/Eliminar).

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.components;

//Trae Pos, que sirve para alinear elementos (por ejemplo arriba y al centro)
import javafx.geometry.Pos;
//Trae Node, la clase general de la que heredan todos los elementos visuales de JavaFX
import javafx.scene.Node;
//Trae el Tooltip, que es el textito que aparece cuando dejas el mouse quieto sobre un elemento
import javafx.scene.control.Tooltip;
//Trae el StackPane, un layout que apila los elementos uno encima de otro
import javafx.scene.layout.StackPane;
//Trae la caja vertical (VBox) de JavaFX que acomoda los elementos uno debajo del otro
import javafx.scene.layout.VBox;

//Importa la interfaz List, que define el comportamiento general de una lista en Java
import java.util.List;

//Clase pública llamada "BarraMiniIconos" que hereda de VBox (una caja que acomoda sus elementos uno debajo del otro)
public class BarraMiniIconos extends VBox {

    //Un "record" (una clase corta y automática de Java solo para guardar datos) que representa un elemento de la
    //barra: su ícono, el texto descriptivo del tooltip y la acción (Runnable) que se ejecuta al hacer click en él.
    public record Item(Node icono, String descripcion, Runnable accion) {
    }

    //Constructor
    //Arma la barra lateral: recorre la lista de items y por cada uno crea un cuadradito con el ícono, le pone el
    //tooltip con la descripción, lo marca como activo si es el que corresponde al indiceActivo, y le asigna la
    //acción a ejecutar cuando se hace click sobre él.
    public BarraMiniIconos(List<Item> items, int indiceActivo) {
        getStyleClass().add("barra-mini-iconos");
        setAlignment(Pos.TOP_CENTER);
        setPrefWidth(70);
        setMinWidth(70);

        //Recorre cada item de la lista, uno por uno, junto con su posición (i)
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            //Crea el cuadradito (StackPane) que contiene el ícono de este item
            StackPane contenedor = new StackPane(item.icono());
            contenedor.setPrefSize(46, 46);
            //Si la posición actual (i) es la misma que el indiceActivo, le pone el estilo de "activo" (resaltado); si no, el estilo normal
            contenedor.getStyleClass().add(i == indiceActivo ? "mini-icono-activo" : "mini-icono");
            //Instala el tooltip (el textito flotante) con la descripción del item
            Tooltip.install(contenedor, new Tooltip(item.descripcion()));
            //Cuando se hace click en el cuadradito, ejecuta la acción guardada en el item
            contenedor.setOnMouseClicked(e -> item.accion().run());
            getChildren().add(contenedor);
        }
    }
}
