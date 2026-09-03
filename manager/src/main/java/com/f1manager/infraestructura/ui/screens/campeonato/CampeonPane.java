//Esta es la pantalla final del modo campeonato: se muestra cuando ya no quedan
//mas carreras y corona al piloto campeon y al equipo campeon (constructores).

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.campeonato;

//Trae la clase Campeonato para poder leer la clasificacion final de pilotos y equipos
import com.f1manager.dominio.modelo.Campeonato;
//Trae la clase Piloto para poder identificar quien es el campeon
import com.f1manager.dominio.modelo.Piloto;
//Trae la fabrica de iconos/avatares para poder dibujar la foto del piloto campeon
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae Insets, que sirve para poner margenes/espacios alrededor de los elementos
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear los elementos (centrado, arriba, etc)
import javafx.geometry.Pos;
//Trae Button, el boton que se puede hacer click
import javafx.scene.control.Button;
//Trae Label, que es un texto que se muestra en pantalla (no se puede editar)
import javafx.scene.control.Label;
//Trae StackPane, un contenedor que apila elementos uno encima del otro (aca se usa para el "trofeo"/avatar)
import javafx.scene.layout.StackPane;
//Trae VBox, un contenedor que acomoda los elementos uno debajo del otro (en columna)
import javafx.scene.layout.VBox;

//Trae la interfaz Map para poder leer las parejas (clave, valor) de la clasificacion
import java.util.Map;

//Esta es la clase publica "CampeonPane" que extiende de VBox (osea que ES un VBox, un contenedor en columna)
public class CampeonPane extends VBox {

    //Constructor
    //Recibe el campeonato ya terminado (para sacar quien gano) y una accion (Runnable) que se ejecuta al volver al menu
    public CampeonPane(Campeonato campeonato, Runnable alMenu) {
        //Separacion entre cada elemento de la columna
        setSpacing(22);
        //Centra todo el contenido
        setAlignment(Pos.CENTER);
        //Margen alrededor de todo el panel
        setPadding(new Insets(10));

        //Trae la primera posicion (get(0)) de la clasificacion de pilotos, osea el piloto con mas puntos (el campeon)
        Map.Entry<Piloto, Integer> campeonPiloto = campeonato.getClasificacionPilotos().get(0);
        //Trae la primera posicion (get(0)) de la clasificacion de equipos, osea el equipo campeon (de constructores)
        Map.Entry<String, Integer> campeonEquipo = campeonato.getClasificacionEquipos().get(0);

        //Construye un avatar/foto grande (100 de tamaño) del piloto campeon, usado como si fuera un "trofeo"
        StackPane trofeo = IconFactory.avatarPiloto(campeonPiloto.getKey(), 100);

        //Texto grande con el titulo de la pantalla
        Label titulo = new Label("¡CAMPEÓN DE LA TEMPORADA!");
        titulo.getStyleClass().add("titulo-principal");

        //Texto con el nombre del piloto campeon (getKey() saca al piloto de la pareja clave-valor)
        Label nombrePiloto = new Label(campeonPiloto.getKey().getNombre());
        nombrePiloto.getStyleClass().add("texto-rojo");
        nombrePiloto.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        //Texto con el equipo del piloto y sus puntos totales (getValue() saca los puntos de la pareja clave-valor)
        Label detallePiloto = new Label(campeonPiloto.getKey().getEquipo() + "   ·   " + campeonPiloto.getValue() + " puntos");
        detallePiloto.getStyleClass().add("texto-secundario");

        //Texto con el nombre del equipo campeon (getKey() aca es el String del nombre) y sus puntos totales (getValue())
        Label equipoTitulo = new Label("Campeón de Constructores: " + campeonEquipo.getKey()
                + " (" + campeonEquipo.getValue() + " puntos)");
        equipoTitulo.getStyleClass().add("texto-normal");
        equipoTitulo.setStyle("-fx-font-size: 15px;");

        //Boton para volver al menu principal
        Button boton = new Button("MENÚ PRINCIPAL");
        boton.getStyleClass().add("boton-grande");
        //Cuando se hace click, ejecuta la accion "alMenu" que le paso quien creo esta pantalla
        boton.setOnAction(e -> alMenu.run());

        //Agrega todos los elementos (trofeo, titulo, nombre, detalle, equipo y boton) a la columna en ese orden
        getChildren().addAll(trofeo, titulo, nombrePiloto, detallePiloto, equipoTitulo, boton);
    }
}
