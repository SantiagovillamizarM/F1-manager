//Define cómo se ve y se comporta una tarjeta clicable
//(las que aparecen en los menús, como "GESTIÓN DE CIRCUITOS").

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.components;

//Trae Pos, que sirve para alinear elementos (por ejemplo al centro)
import javafx.geometry.Pos;
//Trae Node, la clase general de la que heredan todos los elementos visuales de JavaFX
import javafx.scene.Node;
//Trae la etiqueta de texto (Label) de JavaFX
import javafx.scene.control.Label;
//Trae la caja vertical (VBox) de JavaFX que acomoda los elementos uno debajo del otro
import javafx.scene.layout.VBox;

//Clase pública llamada "TarjetaOpcion" que hereda de VBox (una caja que acomoda sus elementos uno debajo del otro)
public class TarjetaOpcion extends VBox {

    //Constructor
    //Arma la tarjeta con su ícono y su título, le pone el tamaño y el estilo según si es "grande" o normal,
    //y le asigna la acción (accion) que se ejecuta cuando el usuario hace click sobre ella.
    public TarjetaOpcion(Node icono, String titulo, Runnable accion, boolean grande) {
        //Según sea grande o no, cambia la clase de estilo, el espacio entre elementos y el tamaño de la tarjeta
        getStyleClass().add(grande ? "tarjeta-grande" : "tarjeta");
        setAlignment(Pos.CENTER);
        setSpacing(grande ? 22 : 14);
        setPrefSize(grande ? 420 : 210, grande ? 300 : 190);
        setMinSize(grande ? 320 : 170, grande ? 240 : 160);
        setMaxSize(grande ? 420 : 210, grande ? 300 : 190);

        //Etiqueta con el título de la tarjeta, con el texto centrado y que se puede partir en varias líneas si no cabe
        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add(grande ? "tarjeta-titulo-grande" : "tarjeta-titulo");
        etiqueta.setWrapText(true);
        etiqueta.setAlignment(Pos.CENTER);
        etiqueta.setStyle("-fx-text-alignment: center;");

        //Mete el ícono y la etiqueta dentro de la tarjeta
        getChildren().addAll(icono, etiqueta);
        //Cuando se hace click en cualquier parte de la tarjeta, ejecuta la acción que le pasaron por parámetro
        setOnMouseClicked(e -> accion.run());
    }
}
