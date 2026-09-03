//Esta es la pantalla que solo muestra (lista) los pilotos que ya están
//registrados, usando el panel con la ficha ampliada de cada uno.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.pilotos;

//Trae el DataStore, que es donde se guarda toda la información del juego (pilotos, equipos, etc.) en memoria
import com.f1manager.infraestructura.persistencia.DataStore;
//Trae Label, que es un componente de JavaFX para mostrar texto en pantalla (no se puede editar)
import javafx.scene.control.Label;
//Trae VBox, que es un contenedor de JavaFX que acomoda sus elementos uno debajo del otro (en vertical)
import javafx.scene.layout.VBox;

//Clase publica que extiende de VBox, osea que esta pantalla ES un VBox (una caja vertical)
public class PilotosListarPane extends VBox {
    //Constructor
    //Arma la pantalla: pone el titulo arriba y debajo el panel con la lista de pilotos
    public PilotosListarPane() {
        //Deja 20 pixeles de espacio entre cada elemento de la caja
        setSpacing(20);
        //Crea el texto del titulo de la pantalla
        Label titulo = new Label("Pilotos registrados");
        //Le pone el estilo visual (CSS) de titulo de sección
        titulo.getStyleClass().add("titulo-seccion");
        //Crea el panel con la lista, pasandole todos los pilotos que ya estan guardados en el DataStore
        PanelListaPilotos panel = new PanelListaPilotos(DataStore.getInstancia().getPilotos());
        //Agrega el titulo y el panel a la pantalla, en ese orden
        getChildren().addAll(titulo, panel);
    }
}
