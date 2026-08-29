package com.f1manager.infraestructura.ui.screens.circuitos;


import com.f1manager.infraestructura.ui.util.GestorEscenas;
import com.f1manager.infraestructura.ui.components.BarraMiniIconos;
import com.f1manager.infraestructura.ui.components.TarjetaOpcion;
import com.f1manager.infraestructura.ui.ModuloGestionBase;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * Módulo de Gestión de Circuitos.
 *
 * Al entrar se muestran cuatro tarjetas grandes (Listar / Registrar /
 * Buscar / Eliminar). Al seleccionar cualquiera, esas mismas opciones se
 * transforman en una barra lateral de mini íconos y el área central pasa
 * a mostrar la sub-sección elegida.
 */
public class ModuloCircuitos extends ModuloGestionBase {

    public ModuloCircuitos(GestorEscenas gestor) {
        super(gestor, "GESTIÓN DE CIRCUITOS");
        mostrarMenuTarjetas();
    }

    private void mostrarMenuTarjetas() {
        quitarBarraLateral();

        TarjetaOpcion listar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.logoGestionCircuitos(), 60),
                "LISTAR\nCIRCUITOS", () -> irA(0), false);
        TarjetaOpcion registrar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.documento(IconFactory.BLANCO), 60),
                "REGISTRAR\nCIRCUITO", () -> irA(1), false);
        TarjetaOpcion buscar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.lupa(IconFactory.BLANCO), 60),
                "BUSCAR\nCIRCUITO", () -> irA(2), false);
        TarjetaOpcion eliminar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.equis(IconFactory.BLANCO), 60),
                "ELIMINAR\nCIRCUITO", () -> irA(3), false);

        HBox fila = new HBox(24, listar, registrar, buscar, eliminar);
        fila.setAlignment(Pos.CENTER);
        fila.setPadding(new Insets(40, 0, 0, 0));

        mostrarEnCentro(fila);
    }

    private List<BarraMiniIconos.Item> construirItemsBarra() {
        return List.of(
                new BarraMiniIconos.Item(IconFactory.logoGestionCircuitos(), "Listar circuitos", () -> irA(0)),
                new BarraMiniIconos.Item(IconFactory.documento(IconFactory.BLANCO), "Registrar circuito", () -> irA(1)),
                new BarraMiniIconos.Item(IconFactory.lupa(IconFactory.BLANCO), "Buscar circuito", () -> irA(2)),
                new BarraMiniIconos.Item(IconFactory.equis(IconFactory.BLANCO), "Eliminar circuito", () -> irA(3))
        );
    }

    private void irA(int indice) {
        establecerBarraLateral(construirItemsBarra(), indice);
        switch (indice) {
            case 0 -> mostrarEnCentro(new CircuitosListarPane());
            case 1 -> mostrarEnCentro(new CircuitosRegistrarPane(this::irAListar));
            case 2 -> mostrarEnCentro(new CircuitosBuscarPane());
            case 3 -> mostrarEnCentro(new CircuitosEliminarPane());
        }
    }

    private void irAListar() {
        irA(0);
    }
}
