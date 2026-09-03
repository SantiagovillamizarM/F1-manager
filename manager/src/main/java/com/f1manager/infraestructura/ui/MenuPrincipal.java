//Esta es la pantalla del menú principal de la aplicación: muestra la columna de Administración y
//Equipo (circuitos, pilotos, equipos, vehículos), la columna de Competencia y Simulación (Carrera
//y Modo Campeonato), la barra de arriba con el logo y el botón de mutear música, y el botón de Salir.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui;

//Trae el gestor de escenas, que es el que se encarga de cambiar de una pantalla a otra
import com.f1manager.infraestructura.ui.util.GestorEscenas;
//Trae la tarjeta clicable que se usa para cada opción del menú
import com.f1manager.infraestructura.ui.components.TarjetaOpcion;
//Trae la pantalla del modo campeonato para poder abrirla al hacer click en su tarjeta
import com.f1manager.infraestructura.ui.screens.campeonato.PantallaCampeonato;
//Trae la pantalla de carrera para poder abrirla al hacer click en su tarjeta
import com.f1manager.infraestructura.ui.screens.carrera.PantallaCarrera;
//Trae el módulo de gestión de circuitos para poder abrirlo al hacer click en su tarjeta
import com.f1manager.infraestructura.ui.screens.circuitos.ModuloCircuitos;
//Trae el módulo de gestión de equipos para poder abrirlo al hacer click en su tarjeta
import com.f1manager.infraestructura.ui.screens.equipos.ModuloEquipos;
//Trae el módulo de gestión de pilotos para poder abrirlo al hacer click en su tarjeta
import com.f1manager.infraestructura.ui.screens.pilotos.ModuloPilotos;
//Trae el módulo de gestión de vehículos para poder abrirlo al hacer click en su tarjeta
import com.f1manager.infraestructura.ui.screens.vehiculos.ModuloVehiculos;
//Trae el gestor de sonido, que controla la música de fondo y los efectos de audio
import com.f1manager.infraestructura.ui.util.GestorSonido;
//Trae la fábrica de íconos, que es la que arma los logos y demás gráficos reutilizables
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae Insets, que sirve para poner márgenes/espacios alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear elementos (por ejemplo a la izquierda o al centro)
import javafx.geometry.Pos;
//Trae el botón (Button) de JavaFX
import javafx.scene.control.Button;
//Trae la etiqueta de texto (Label) de JavaFX
import javafx.scene.control.Label;
//Trae el Separator, que es la línea divisoria que se pone entre las dos columnas del menú
import javafx.scene.control.Separator;
//Trae todas las clases de layout de JavaFX (BorderPane, GridPane, HBox, VBox, Priority, Region, etc)
import javafx.scene.layout.*;

//Clase pública llamada "MenuPrincipal" que hereda de BorderPane (un layout que divide la pantalla en top/bottom/left/right/center)
public class MenuPrincipal extends BorderPane {

    //Constructor
    //Arma toda la pantalla del menú principal: la barra de arriba, la columna de administración y equipo con
    //la grilla de tarjetas, la columna de competencia y simulación, y el botón de salir abajo.
    public MenuPrincipal(GestorEscenas gestor) {
        getStyleClass().add("pantalla");
        setPrefSize(1366, 820);

        setTop(construirBarraSuperior());

        // --- Columna izquierda: administración y equipo ---
        //Título de la columna izquierda
        Label tituloAdmin = new Label("ADMINISTRACIÓN Y EQUIPO");
        tituloAdmin.getStyleClass().add("titulo-seccion");

        //Grilla (GridPane) donde se acomodan las 4 tarjetas de gestión en dos filas y dos columnas
        GridPane grilla = new GridPane();
        grilla.setHgap(20);
        grilla.setVgap(20);

        //Crea las 4 tarjetas (Circuitos, Pilotos, Equipos, Vehículos) y les pone la acción de abrir su módulo al hacer click
        grilla.add(new TarjetaOpcion(IconFactory.contenedor(IconFactory.logoGestionCircuitos(), 60),
                "GESTIÓN DE CIRCUITOS", () -> gestor.navegarA(new ModuloCircuitos(gestor)), false), 0, 0);
        grilla.add(new TarjetaOpcion(IconFactory.contenedor(IconFactory.logoGestionPilotos(), 60),
                "GESTIÓN DE PILOTOS", () -> gestor.navegarA(new ModuloPilotos(gestor)), false), 1, 0);
        grilla.add(new TarjetaOpcion(IconFactory.contenedor(IconFactory.logoGestionEquipos(), 60),
                "GESTIÓN DE EQUIPOS", () -> gestor.navegarA(new ModuloEquipos(gestor)), false), 0, 1);
        grilla.add(new TarjetaOpcion(IconFactory.contenedor(IconFactory.logoGestionVehiculos(), 60),
                "GESTIÓN DE VEHÍCULOS", () -> gestor.navegarA(new ModuloVehiculos(gestor)), false), 1, 1);

        //Junta el título y la grilla en una columna vertical (VBox)
        VBox columnaAdmin = new VBox(20, tituloAdmin, grilla);
        columnaAdmin.setPadding(new Insets(10, 30, 10, 10));

        // --- Columna derecha: competencia y simulación ---
        //Título de la columna derecha
        Label tituloCompetencia = new Label("COMPETENCIA Y SIMULACIÓN");
        tituloCompetencia.getStyleClass().add("titulo-seccion");

        //Tarjeta que abre la pantalla de una carrera individual al hacer click
        TarjetaOpcion tarjetaCarrera = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.logoCarrera(), 60),
                "CARRERA", () -> gestor.navegarA(new PantallaCarrera(gestor)), false);

        //Tarjeta que abre la pantalla del modo campeonato (temporada completa) al hacer click
        TarjetaOpcion tarjetaCampeonato = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.logoCampeonato(), 60),
                "MODO CAMPEONATO", () -> gestor.navegarA(new PantallaCampeonato(gestor)), false);

        //Junta el título y las dos tarjetas en una columna vertical (VBox)
        VBox columnaCarrera = new VBox(20, tituloCompetencia, tarjetaCarrera, tarjetaCampeonato);
        columnaCarrera.setPadding(new Insets(10, 10, 10, 30));

        //Junta las dos columnas con una línea divisoria (Separator) vertical en el medio
        HBox cuerpo = new HBox(10, columnaAdmin, new Separator(javafx.geometry.Orientation.VERTICAL), columnaCarrera);
        cuerpo.setAlignment(Pos.TOP_CENTER);
        cuerpo.setPadding(new Insets(40));
        //Evita que las columnas se estiren de más cuando la ventana crece
        HBox.setHgrow(columnaAdmin, Priority.NEVER);
        HBox.setHgrow(columnaCarrera, Priority.NEVER);

        setCenter(cuerpo);

        // --- Pie: botón salir ---
        //Botón que cierra la aplicación al hacer click (le pide al gestor de escenas que salga)
        Button botonSalir = new Button("SALIR");
        botonSalir.getStyleClass().add("boton-salir");
        botonSalir.setOnAction(e -> gestor.salir());

        HBox pie = new HBox(botonSalir);
        pie.setAlignment(Pos.CENTER);
        pie.setPadding(new Insets(0, 0, 30, 0));
        setBottom(pie);
    }

    //Arma la barra de arriba del menú: el logo con el nombre "F1 MANAGER" a la izquierda, un espacio que se
    //estira para empujar todo lo demás, y el botón de mutear/activar la música de fondo a la derecha.
    private HBox construirBarraSuperior() {
        var logo = IconFactory.logo(0.9);
        Label marca = new Label("F1 MANAGER");
        marca.getStyleClass().add("marca-logo");
        HBox marcaBox = new HBox(14, logo, marca);
        marcaBox.setAlignment(Pos.CENTER_LEFT);

        //Region invisible que se estira (crece) para empujar el botón de mute hacia la derecha
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        //Botón que muestra si la música de fondo está muteada o no, y al hacer click alterna (cambia) ese estado
        Button botonMute = new Button(GestorSonido.isMusicaDeFondoMuteada() ? "ACTIVAR MÚSICA" : "MUTEAR MÚSICA");
        botonMute.getStyleClass().add("boton-secundario");
        botonMute.setOnAction(e -> botonMute.setText(
                GestorSonido.alternarMuteMusicaDeFondo() ? "ACTIVAR MÚSICA" : "MUTEAR MÚSICA"));

        HBox barra = new HBox(20, marcaBox, espaciador, botonMute);
        barra.getStyleClass().add("barra-superior");
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(16, 30, 16, 30));
        return barra;
    }
}
