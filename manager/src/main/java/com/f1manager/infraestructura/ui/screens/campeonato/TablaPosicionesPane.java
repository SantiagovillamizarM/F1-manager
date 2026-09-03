//Esta es la pantalla de clasificacion del campeonato: muestra las dos tablas
//(pilotos y equipos) con sus puntos acumulados, y se ve entre carrera y carrera.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.campeonato;

//Trae la clase Campeonato para poder leer las clasificaciones de pilotos y equipos
import com.f1manager.dominio.modelo.Campeonato;
//Trae la clase Piloto para poder identificar cada fila de la tabla de pilotos
import com.f1manager.dominio.modelo.Piloto;
//Trae la fabrica de iconos/avatares para poder dibujar la foto de cada piloto en su fila
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae Insets, que sirve para poner margenes/espacios alrededor de los elementos
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear los elementos (centrado, arriba, izquierda, etc)
import javafx.geometry.Pos;
//Trae Button, el boton que se puede hacer click
import javafx.scene.control.Button;
//Trae Label, que es un texto que se muestra en pantalla (no se puede editar)
import javafx.scene.control.Label;
//Trae ScrollPane, un contenedor que agrega una barra de desplazamiento (scroll) cuando el contenido no cabe
import javafx.scene.control.ScrollPane;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila)
import javafx.scene.layout.HBox;
//Trae Priority, que sirve para decirle a un elemento que crezca para llenar el espacio libre
import javafx.scene.layout.Priority;
//Trae Region, un elemento vacio que se usa como "espaciador" o para medidas
import javafx.scene.layout.Region;
//Trae StackPane, un contenedor que apila elementos uno encima del otro (aca se usa para el avatar del piloto)
import javafx.scene.layout.StackPane;
//Trae VBox, un contenedor que acomoda los elementos uno debajo del otro (en columna)
import javafx.scene.layout.VBox;

//Trae la interfaz Map para poder leer las parejas (clave, valor) de las clasificaciones
import java.util.Map;

//Esta es la clase publica "TablaPosicionesPane" que extiende de VBox (osea que ES un VBox, un contenedor en columna)
public class TablaPosicionesPane extends VBox {

    //Constructor
    //Recibe el campeonato (para leer las clasificaciones) y una accion (Runnable) que se ejecuta al continuar
    public TablaPosicionesPane(Campeonato campeonato, Runnable alContinuar) {
        //Separacion entre cada elemento de la columna
        setSpacing(20);
        //Alinea todo arriba y centrado
        setAlignment(Pos.TOP_CENTER);
        //Margen alrededor de todo el panel
        setPadding(new Insets(10));

        //Texto grande con el titulo de la pantalla
        Label titulo = new Label("CLASIFICACIÓN DEL CAMPEONATO");
        titulo.getStyleClass().add("titulo-principal");

        //Cuantas carreras van corridas hasta ahora (getNumeroCarreraActual ya esta en formato humano, entonces se le resta 1
        //para que muestre la ultima carrera ya corrida y no la que viene)
        int carrerasCorridas = campeonato.getNumeroCarreraActual() - 1;
        //Texto que muestra "Despues de la carrera X de Y"
        Label subtitulo = new Label("Después de la carrera " + carrerasCorridas
                + " de " + campeonato.getTotalCarreras());
        subtitulo.getStyleClass().add("texto-secundario");

        //Columna donde van a ir las filas de la tabla de pilotos
        VBox columnaPilotos = new VBox(2);
        //Contador de la posicion actual en la tabla, empieza en 1 (P1, P2, P3...)
        int posicion = 1;
        //Este es un bucle for-each que recorre la clasificacion de pilotos ya ordenada de mayor a menor puntaje
        //(el orden lo hace Campeonato.getClasificacionPilotos(), aca solo se dibuja)
        for (Map.Entry<Piloto, Integer> entrada : campeonato.getClasificacionPilotos()) {
            //Arma la fila con la posicion, el nombre del piloto, su equipo, sus puntos (getValue()) y su avatar
            columnaPilotos.getChildren().add(construirFila(posicion,
                    entrada.getKey().getNombre() + "  ·  " + entrada.getKey().getEquipo(), entrada.getValue(),
                    IconFactory.avatarPiloto(entrada.getKey(), 30)));
            //Suma 1 para que la siguiente vuelta muestre la posicion correcta
            posicion++;
        }

        //Columna donde van a ir las filas de la tabla de equipos
        VBox columnaEquipos = new VBox(2);
        //Reinicia el contador de posicion para la tabla de equipos
        posicion = 1;
        //Este es un bucle for-each que recorre la clasificacion de equipos ya ordenada de mayor a menor puntaje
        for (Map.Entry<String, Integer> entrada : campeonato.getClasificacionEquipos()) {
            //Arma la fila con la posicion, el nombre del equipo (getKey()) y sus puntos (getValue()), sin avatar
            columnaEquipos.getChildren().add(construirFila(posicion, entrada.getKey(), entrada.getValue()));
            posicion++;
        }

        //Junta las dos tablas (pilotos y equipos) una al lado de la otra, cada una envuelta en su panel con titulo
        HBox filas = new HBox(24, envolver("PILOTOS", columnaPilotos), envolver("EQUIPOS", columnaEquipos));
        filas.setAlignment(Pos.TOP_CENTER);

        //Boton para continuar: si quedan carreras dice "SIGUIENTE CARRERA", si ya no quedan dice "VER CAMPEÓN"
        //(quedanCarreras() compara el indice actual contra el total del calendario)
        Button boton = new Button(campeonato.quedanCarreras() ? "SIGUIENTE CARRERA" : "VER CAMPEÓN");
        boton.getStyleClass().add("boton-grande");
        //Cuando se hace click, ejecuta la accion "alContinuar" que le paso quien creo esta pantalla
        boton.setOnAction(e -> alContinuar.run());

        //Agrega todos los elementos (titulo, subtitulo, las dos tablas y el boton) a la columna en ese orden
        getChildren().addAll(titulo, subtitulo, filas, boton);
    }

    //Este metodo privado arma un panel con un titulo arriba (PILOTOS o EQUIPOS) y las filas dentro de un scroll
    private VBox envolver(String tituloColumna, VBox filas) {
        //Texto que titula la columna (PILOTOS o EQUIPOS)
        Label titulo = new Label(tituloColumna);
        titulo.getStyleClass().add("texto-rojo");

        //Envuelve las filas en un ScrollPane para que se puedan desplazar si la lista es larga
        ScrollPane scroll = new ScrollPane(filas);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(380);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        //Panel final que junta el titulo y el scroll con las filas
        VBox panel = new VBox(12, titulo, scroll);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(18));
        panel.setPrefWidth(340);
        return panel;
    }

    //Version corta de construirFila para las filas que no llevan avatar (la tabla de equipos)
    private HBox construirFila(int posicion, String nombre, int puntos) {
        //Le pasa null como avatar a la version completa de abajo
        return construirFila(posicion, nombre, puntos, null);
    }

    //Este metodo privado arma una fila completa de la tabla: posicion, nombre (con avatar opcional) y puntos
    private HBox construirFila(int posicion, String nombre, int puntos, StackPane avatar) {
        //Si la posicion es 1, esta fila es la del lider (para pintarla distinto, con color dorado)
        boolean lider = posicion == 1;

        //Texto con la posicion en formato "P1", "P2", etc
        Label posicionLabel = new Label("P" + posicion);
        posicionLabel.setPrefWidth(40);
        // Sin esto, el HBox comprime la etiqueta por debajo de su ancho preferido cuando el
        // nombre es largo, y JavaFX le pone "…" en vez de mostrar el texto completo.
        posicionLabel.setMinWidth(Region.USE_PREF_SIZE);
        //Si es el lider se pinta dorado (#ffd400), si no del color normal (#f5f6fa)
        posicionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: "
                + (lider ? "#ffd400" : "#f5f6fa") + ";");

        //Texto con el nombre (de piloto+equipo, o solo del equipo segun quien llamo al metodo)
        Label nombreLabel = new Label(nombre);
        nombreLabel.getStyleClass().add("texto-normal");
        nombreLabel.setWrapText(true);
        nombreLabel.setPrefWidth(170);
        nombreLabel.setMaxWidth(170);

        //Region vacia que se estira (Priority.ALWAYS) para empujar los puntos hacia el borde derecho de la fila
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        //Texto con los puntos acumulados
        Label puntosLabel = new Label(puntos + " pts");
        puntosLabel.setMinWidth(Region.USE_PREF_SIZE);
        //Si es el lider se pinta con el color rojo de acento, si no con el color secundario
        puntosLabel.getStyleClass().add(lider ? "texto-rojo" : "texto-secundario");
        puntosLabel.setStyle(puntosLabel.getStyle() + "-fx-font-weight: bold;");

        //Si hay avatar (tabla de pilotos) se arma la fila con el avatar incluido; si no (tabla de equipos), sin el
        HBox fila = avatar != null
                ? new HBox(10, posicionLabel, avatar, nombreLabel, espaciador, puntosLabel)
                : new HBox(10, posicionLabel, nombreLabel, espaciador, puntosLabel);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(8, 10, 8, 10));
        return fila;
    }
}
