//Pantalla "Listar circuitos": muestra todos los circuitos que ya están
//registrados y deja ver la información ampliada y el trazado de cada uno.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.circuitos;

//Trae el DataStore, que es donde se guarda toda la información del programa (circuitos, pilotos, equipos, etc.)
import com.f1manager.aplicacion.DataStore;
//Trae Label, que es un texto que se muestra en pantalla (no se puede editar)
import javafx.scene.control.Label;
//Trae VBox, un contenedor que acomoda los elementos uno debajo del otro (en columna)
import javafx.scene.layout.VBox;

//Clase publica llamada "CircuitosListarPane" que hereda de VBox (osea que ella misma es una columna de elementos)
public class CircuitosListarPane extends VBox {

    //Constructor
    //Arma la pantalla: pone el título y el panel que muestra la lista de circuitos junto con su detalle
    public CircuitosListarPane() {
        //Deja 20 pixeles de espacio entre cada elemento de la columna
        setSpacing(20);

        //Crea el texto del título de la pantalla
        Label titulo = new Label("Circuitos registrados");
        //Le agrega el estilo visual "titulo-seccion" (definido en el CSS)
        titulo.getStyleClass().add("titulo-seccion");

        //Crea el panel reutilizable que muestra la lista de circuitos y el detalle de cada uno,
        //pasándole todos los circuitos que ya están guardados en el DataStore
        PanelListaCircuitos panel = new PanelListaCircuitos(
                DataStore.getInstancia().getCircuitos(), "No hay circuitos registrados todavía.");

        //Agrega el título y el panel a la columna para que se vean en pantalla
        getChildren().addAll(titulo, panel);
    }
}
