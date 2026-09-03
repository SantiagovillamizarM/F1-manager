//Esta es la clase base (abstracta, osea que no se puede instanciar sola) que comparten todos los
//módulos de gestión (Circuitos, Pilotos, Equipos, Vehículos): arma la barra superior con el botón
//de volver, el área central donde se cambian las sub-pantallas con un efecto fade, y deja instalar
//la barra lateral de mini íconos cuando el usuario entra a una sub-sección.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui;

//Trae el gestor de escenas, que es el que se encarga de cambiar de una pantalla a otra
import com.f1manager.infraestructura.ui.util.GestorEscenas;
//Trae la barra lateral de mini íconos (Listar/Registrar/Buscar/Eliminar) para poder instalarla
import com.f1manager.infraestructura.ui.components.BarraMiniIconos;
//Trae la fábrica de íconos, que es la que arma las flechas y demás gráficos reutilizables
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae la animación de fundido (fade), la que hace que algo aparezca o desaparezca suavemente
import javafx.animation.FadeTransition;
//Trae Insets, que sirve para poner márgenes/espacios alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear elementos (por ejemplo a la izquierda o al centro)
import javafx.geometry.Pos;
//Trae Node, la clase general de la que heredan todos los elementos visuales de JavaFX
import javafx.scene.Node;
//Trae la etiqueta de texto (Label) de JavaFX
import javafx.scene.control.Label;
//Trae el ScrollPane, que es un panel con barras de desplazamiento (scroll) cuando el contenido no cabe
import javafx.scene.control.ScrollPane;
//Trae todas las clases de layout de JavaFX (BorderPane, HBox, StackPane, etc, las que acomodan los elementos en pantalla)
import javafx.scene.layout.*;
//Trae Duration, que sirve para indicar cuánto dura una animación
import javafx.util.Duration;

//Importa la interfaz List, que define el comportamiento general de una lista en Java
import java.util.List;

//Clase abstracta pública llamada "ModuloGestionBase" que hereda de BorderPane (un layout que divide la pantalla en top/bottom/left/right/center)
public abstract class ModuloGestionBase extends BorderPane {

    //Atributo protegido (los hijos lo pueden usar) que guarda el gestor de escenas para poder navegar entre pantallas
    protected final GestorEscenas gestor;
    //Panel apilado (StackPane) privado donde se muestra el contenido central de cada módulo (listar, registrar, buscar, eliminar)
    private final StackPane areaCentral = new StackPane();
    //Bandera privada que indica si en este momento se está reproduciendo la animación de cambio del área central (para no pisar una animación con otra)
    private boolean animandoCentro = false;

    //Constructor
    //Inicializa la pantalla base: guarda el gestor, le pone estilo y tamaño, arma la barra superior con el título del módulo
    //y mete el área central dentro de un ScrollPane transparente (por si el contenido no cabe en la pantalla).
    protected ModuloGestionBase(GestorEscenas gestor, String tituloModulo) {
        this.gestor = gestor;
        getStyleClass().add("pantalla");
        setPrefSize(1366, 820);
        setTop(construirBarraSuperior(tituloModulo));

        areaCentral.setPadding(new Insets(34));
        ScrollPane scroll = new ScrollPane(areaCentral);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setCenter(scroll);
    }

    //Arma la barra de arriba de cada módulo: el botón de volver (una flecha) y la etiqueta con el título del módulo
    private HBox construirBarraSuperior(String titulo) {
        //Crea el botón de volver con la flecha y le pone estilo (fondo transparente, borde, cursor de mano al pasar el mouse)
        StackPane botonVolver = IconFactory.contenedor(IconFactory.flechaVolver(IconFactory.BLANCO), 40);
        botonVolver.setStyle("-fx-background-color: transparent; -fx-border-color: #232a3d; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        //Cuando se hace click en la flecha, le pide al gestor de escenas que vuelva a la pantalla anterior
        botonVolver.setOnMouseClicked(e -> gestor.volver());

        //Etiqueta con el título del módulo (por ejemplo "GESTIÓN DE CIRCUITOS")
        Label etiquetaTitulo = new Label(titulo);
        etiquetaTitulo.getStyleClass().add("titulo-seccion");

        //Junta el botón de volver y el título en una fila horizontal (HBox) y le da estilo de barra superior
        HBox barra = new HBox(20, botonVolver, etiquetaTitulo);
        barra.getStyleClass().add("barra-superior");
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(18, 30, 18, 30));
        return barra;
    }

    //Instala (o reemplaza) la barra lateral de mini íconos de navegación interna (Listar/Registrar/Buscar/Eliminar) a la izquierda de la pantalla.
    protected void establecerBarraLateral(List<BarraMiniIconos.Item> items, int indiceActivo) {
        setLeft(new BarraMiniIconos(items, indiceActivo));
    }

    //Quita la barra lateral (se usa en la vista inicial de tarjetas grandes, donde todavía no hay ninguna sub-sección activa).
    protected void quitarBarraLateral() {
        setLeft(null);
    }

    //Cambia lo que se ve en el área central aplicando una transición de fade (para que el cambio no se vea brusco).
    //Si ya hay una animación de cambio en curso, cambia directo sin animar (para no pisarla). Si el área central está
    //vacía (primera vez), solo hace aparecer la vista nueva. Si ya había algo, primero hace desaparecer lo viejo y
    //cuando termina, mete lo nuevo y lo hace aparecer.
    protected void mostrarEnCentro(Node vista) {
        //Si ya hay una animación en curso, cambia sin animar para evitar pisar la transición que está corriendo
        if (animandoCentro) {
            areaCentral.getChildren().setAll(vista);
            return;
        }
        //Si el área central está vacía (primera vez que se muestra algo), solo hace aparecer la vista nueva con un fade
        if (areaCentral.getChildren().isEmpty()) {
            areaCentral.getChildren().setAll(vista);
            vista.setOpacity(0);
            FadeTransition entrada = new FadeTransition(Duration.millis(200), vista);
            entrada.setFromValue(0);
            entrada.setToValue(1);
            entrada.play();
            return;
        }
        //Ya había algo en el área central: primero se hace desaparecer (fade de salida) lo que estaba puesto
        animandoCentro = true;
        Node actual = areaCentral.getChildren().get(0);
        FadeTransition salida = new FadeTransition(Duration.millis(140), actual);
        salida.setFromValue(1);
        salida.setToValue(0);
        //Cuando termina de desaparecer lo viejo, mete la vista nueva y la hace aparecer con otro fade
        salida.setOnFinished(e -> {
            areaCentral.getChildren().setAll(vista);
            vista.setOpacity(0);
            FadeTransition entrada = new FadeTransition(Duration.millis(180), vista);
            entrada.setFromValue(0);
            entrada.setToValue(1);
            //Cuando termina de aparecer la vista nueva, se libera la bandera para permitir la siguiente animación
            entrada.setOnFinished(ev -> animandoCentro = false);
            entrada.play();
        });
        salida.play();
    }
}
