//Barra de búsqueda reutilizable (lupa + campo de texto), usada para filtrar listas largas en
//tiempo real, por ejemplo en las secciones de "Eliminar" de cada módulo.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.components;

//Trae la fábrica de íconos, que es la que arma el ícono de la lupa
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae Pos, que sirve para alinear elementos (por ejemplo a la izquierda)
import javafx.geometry.Pos;
//Trae el campo de texto (TextField) de JavaFX, donde el usuario escribe lo que quiere buscar
import javafx.scene.control.TextField;
//Trae la caja horizontal (HBox) de JavaFX que acomoda los elementos uno al lado del otro
import javafx.scene.layout.HBox;
//Trae Priority, que sirve para decirle a un elemento que se estire (crezca) dentro de su contenedor
import javafx.scene.layout.Priority;
//Trae el StackPane, un layout que apila los elementos uno encima de otro
import javafx.scene.layout.StackPane;

//Clase pública llamada "CampoBusqueda" que hereda de HBox (una caja que acomoda sus elementos uno al lado del otro)
public class CampoBusqueda extends HBox {

    //Campo de texto privado y fijo (final) donde el usuario escribe el texto de búsqueda
    private final TextField campoTexto = new TextField();

    //Constructor
    //Arma la barra de búsqueda: el ícono de la lupa a la izquierda y el campo de texto (con su texto de ejemplo
    //promptText) ocupando el resto del espacio disponible.
    public CampoBusqueda(String promptText) {
        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);

        //Ícono de la lupa dentro de un cuadradito (StackPane)
        StackPane icono = IconFactory.contenedor(IconFactory.lupa(IconFactory.BLANCO), 46);

        campoTexto.getStyleClass().addAll("campo-texto", "campo-busqueda-texto");
        campoTexto.setPromptText(promptText);
        campoTexto.setPrefHeight(46);
        //Hace que el campo de texto crezca y ocupe todo el espacio horizontal que sobra
        HBox.setHgrow(campoTexto, Priority.ALWAYS);

        getChildren().addAll(icono, campoTexto);
    }

    //Getter
    public TextField getCampoTexto() {
        return campoTexto;
    }

    //Getter, devuelve directamente el texto que el usuario escribió en el campo (más corto que llamar getCampoTexto().getText())
    public String getTexto() {
        return campoTexto.getText();
    }
}
