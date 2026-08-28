package com.f1manager.infraestructura.ui;

import com.f1manager.infraestructura.ui.util.GestorEscenas;
import com.f1manager.infraestructura.ui.components.TarjetaOpcion;
import com.f1manager.infraestructura.ui.screens.campeonato.PantallaCampeonato;
import com.f1manager.infraestructura.ui.screens.carrera.PantallaCarrera;
import com.f1manager.infraestructura.ui.screens.circuitos.ModuloCircuitos;
import com.f1manager.infraestructura.ui.screens.equipos.ModuloEquipos;
import com.f1manager.infraestructura.ui.screens.pilotos.ModuloPilotos;
import com.f1manager.infraestructura.ui.screens.vehiculos.ModuloVehiculos;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

/**
 * Menú principal de la aplicación: sección de Administración y Equipo
 * (circuitos, pilotos, equipos, vehículos) y sección de Competencia y
 * Simulación (Carrera), además del botón Salir.
 */
public class MenuPrincipal extends BorderPane {

    public MenuPrincipal(GestorEscenas gestor) {
        getStyleClass().add("pantalla");
        setPrefSize(1366, 820);

        setTop(construirBarraSuperior());

        // --- Columna izquierda: administración y equipo ---
        Label tituloAdmin = new Label("ADMINISTRACIÓN Y EQUIPO");
        tituloAdmin.getStyleClass().add("titulo-seccion");

        GridPane grilla = new GridPane();
        grilla.setHgap(20);
        grilla.setVgap(20);

        grilla.add(new TarjetaOpcion(IconFactory.contenedor(IconFactory.pistaSilueta(IconFactory.BLANCO), 60),
                "GESTIÓN DE CIRCUITOS", () -> gestor.navegarA(new ModuloCircuitos(gestor)), false), 0, 0);
        grilla.add(new TarjetaOpcion(IconFactory.contenedor(IconFactory.casco(IconFactory.BLANCO), 60),
                "GESTIÓN DE PILOTOS", () -> gestor.navegarA(new ModuloPilotos(gestor)), false), 1, 0);
        grilla.add(new TarjetaOpcion(IconFactory.contenedor(IconFactory.escudoEquipo(IconFactory.BLANCO), 60),
                "GESTIÓN DE EQUIPOS", () -> gestor.navegarA(new ModuloEquipos(gestor)), false), 0, 1);
        grilla.add(new TarjetaOpcion(IconFactory.contenedor(IconFactory.monoplaza(IconFactory.BLANCO), 60),
                "GESTIÓN DE VEHÍCULOS", () -> gestor.navegarA(new ModuloVehiculos(gestor)), false), 1, 1);

        VBox columnaAdmin = new VBox(20, tituloAdmin, grilla);
        columnaAdmin.setPadding(new Insets(10, 30, 10, 10));

        // --- Columna derecha: competencia y simulación ---
        Label tituloCompetencia = new Label("COMPETENCIA Y SIMULACIÓN");
        tituloCompetencia.getStyleClass().add("titulo-seccion");

        TarjetaOpcion tarjetaCarrera = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.banderaCuadros(), 60),
                "CARRERA", () -> gestor.navegarA(new PantallaCarrera(gestor)), false);

        TarjetaOpcion tarjetaCampeonato = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.escudoEquipo(IconFactory.BLANCO), 60),
                "MODO CAMPEONATO", () -> gestor.navegarA(new PantallaCampeonato(gestor)), false);

        VBox columnaCarrera = new VBox(20, tituloCompetencia, tarjetaCarrera, tarjetaCampeonato);
        columnaCarrera.setPadding(new Insets(10, 10, 10, 30));

        HBox cuerpo = new HBox(10, columnaAdmin, new Separator(javafx.geometry.Orientation.VERTICAL), columnaCarrera);
        cuerpo.setAlignment(Pos.TOP_CENTER);
        cuerpo.setPadding(new Insets(40));
        HBox.setHgrow(columnaAdmin, Priority.NEVER);
        HBox.setHgrow(columnaCarrera, Priority.NEVER);

        // Si la ventana es pequeña y las tarjetas no caben completas, se puede
        // hacer scroll en vez de recortar la tarjeta "CARRERA" fuera de la vista.
        ScrollPane scrollCentro = new ScrollPane(cuerpo);
        scrollCentro.setFitToWidth(true);
        scrollCentro.getStyleClass().add("scroll-oscuro");
        scrollCentro.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setCenter(scrollCentro);

        // --- Pie: botón salir ---
        Button botonSalir = new Button("SALIR");
        botonSalir.getStyleClass().add("boton-salir");
        botonSalir.setOnAction(e -> gestor.salir());

        HBox pie = new HBox(botonSalir);
        pie.setAlignment(Pos.CENTER);
        pie.setPadding(new Insets(0, 0, 30, 0));
        setBottom(pie);
    }

    private HBox construirBarraSuperior() {
        var logo = IconFactory.logo(0.9);
        Label marca = new Label("F1 MANAGER");
        marca.getStyleClass().add("marca-logo");
        HBox marcaBox = new HBox(14, logo, marca);
        marcaBox.setAlignment(Pos.CENTER_LEFT);

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        HBox barra = new HBox(20, marcaBox, espaciador);
        barra.getStyleClass().add("barra-superior");
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(16, 30, 16, 30));
        return barra;
    }
}
