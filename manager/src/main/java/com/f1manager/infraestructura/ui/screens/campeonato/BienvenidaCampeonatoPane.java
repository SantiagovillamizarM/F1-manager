//Esta es la pantalla inicial del modo campeonato: muestra el titulo, la explicacion
//de como puntuan las carreras y el calendario completo de circuitos antes de arrancar.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.campeonato;

//Trae la clase Campeonato para poder leer el calendario y el total de carreras
import com.f1manager.dominio.modelo.Campeonato;
//Trae la clase Circuito para poder recorrer cada carrera del calendario
import com.f1manager.dominio.modelo.Circuito;
//Trae Insets, que sirve para poner margenes/espacios alrededor de los elementos
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear los elementos (centrado, arriba, etc)
import javafx.geometry.Pos;
//Trae Button, el boton que se puede hacer click
import javafx.scene.control.Button;
//Trae Label, que es un texto que se muestra en pantalla (no se puede editar)
import javafx.scene.control.Label;
//Trae ScrollPane, un contenedor que agrega una barra de desplazamiento (scroll) cuando el contenido no cabe
import javafx.scene.control.ScrollPane;
//Trae VBox, un contenedor que acomoda los elementos uno debajo del otro (en columna)
import javafx.scene.layout.VBox;

//Esta es la clase publica "BienvenidaCampeonatoPane" que extiende de VBox (osea que ES un VBox, un contenedor en columna)
public class BienvenidaCampeonatoPane extends VBox {

    //Constructor
    //Recibe el campeonato (para leer el calendario) y una accion (Runnable) que se ejecuta al darle "comenzar"
    public BienvenidaCampeonatoPane(Campeonato campeonato, Runnable alComenzar) {
        //Separacion entre cada elemento de la columna
        setSpacing(20);
        //Alinea todo arriba y centrado
        setAlignment(Pos.TOP_CENTER);
        //Margen alrededor de todo el panel
        setPadding(new Insets(10));

        //Texto grande con el titulo de la pantalla
        Label titulo = new Label("MODO CAMPEONATO");
        titulo.getStyleClass().add("titulo-principal");

        //Texto que explica cuantas carreras hay y como puntuan las primeras 10 posiciones (la tabla oficial de puntos de F1)
        Label subtitulo = new Label(campeonato.getTotalCarreras()
                + " carreras · puntúan las primeras 10 posiciones de cada una (25-18-15-12-10-8-6-4-2-1)");
        subtitulo.getStyleClass().add("texto-secundario");
        subtitulo.setWrapText(true);
        subtitulo.setStyle("-fx-text-alignment: center;");

        //Panel (caja) donde va a ir la lista del calendario completo
        VBox panelCalendario = new VBox(10);
        panelCalendario.getStyleClass().add("panel");
        panelCalendario.setPadding(new Insets(20));
        panelCalendario.setMaxWidth(480);

        //Texto que titula el panel del calendario
        Label tituloCalendario = new Label("CALENDARIO");
        tituloCalendario.getStyleClass().add("texto-rojo");
        panelCalendario.getChildren().add(tituloCalendario);

        //Numero que va contando la posicion de cada carrera en el calendario, empieza en 1 (formato humano, no como el indice interno que empieza en 0)
        int numero = 1;
        //Este es un bucle for-each que recorre el calendario del campeonato circuito por circuito
        for (Circuito circuito : campeonato.getCalendario()) {
            //Arma una fila con el numero de carrera, el nombre del circuito y el pais
            Label fila = new Label(numero + ".  " + circuito.getNombre() + "   ·   " + circuito.getPais());
            fila.getStyleClass().add("texto-normal");
            //Agrega la fila al panel del calendario
            panelCalendario.getChildren().add(fila);
            //Suma 1 para que la siguiente vuelta del bucle muestre el numero de carrera correcto
            numero++;
        }

        //Envuelve el panel del calendario en un ScrollPane para que se pueda desplazar si la lista es larga
        ScrollPane scroll = new ScrollPane(panelCalendario);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(360);
        scroll.setMaxWidth(500);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        //Boton para arrancar el campeonato
        Button botonComenzar = new Button("COMENZAR CAMPEONATO");
        botonComenzar.getStyleClass().add("boton-grande");
        //Cuando se hace click, ejecuta la accion "alComenzar" que le paso quien creo esta pantalla
        botonComenzar.setOnAction(e -> alComenzar.run());

        //Agrega todos los elementos (titulo, subtitulo, scroll del calendario y boton) a la columna en ese orden
        getChildren().addAll(titulo, subtitulo, scroll, botonComenzar);
    }
}
