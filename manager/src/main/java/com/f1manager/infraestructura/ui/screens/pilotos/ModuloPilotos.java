package com.f1manager.infraestructura.ui.screens.pilotos;

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
 * Módulo de Gestión de Pilotos: Listar / Registrar / Eliminar,
 * siguiendo el mismo sistema de tarjetas grandes + mini íconos laterales
 * utilizado en Gestión de Circuitos.
 */
public class ModuloPilotos extends ModuloGestionBase {

    public ModuloPilotos(GestorEscenas gestor) {
        super(gestor, "GESTIÓN DE PILOTOS");
        mostrarMenuTarjetas();
    }

    private void mostrarMenuTarjetas() {
        quitarBarraLateral();

        TarjetaOpcion listar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.logoGestionPilotos(), 60),
                "LISTAR\nPILOTOS", () -> irA(0), false);
        TarjetaOpcion registrar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.documento(IconFactory.BLANCO), 60),
                "REGISTRAR\nPILOTO", () -> irA(1), false);
        TarjetaOpcion eliminar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.equis(IconFactory.BLANCO), 60),
                "ELIMINAR\nPILOTO", () -> irA(2), false);
        TarjetaOpcion editar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.lapiz(IconFactory.BLANCO), 60),
                "EDITAR\nPILOTO", () -> irA(3), false);

        HBox fila = new HBox(24, listar, registrar, eliminar, editar);
        fila.setAlignment(Pos.CENTER);
        fila.setPadding(new Insets(40, 0, 0, 0));
        mostrarEnCentro(fila);
    }

    private List<BarraMiniIconos.Item> construirItemsBarra() {
        return List.of(
                new BarraMiniIconos.Item(IconFactory.logoGestionPilotos(), "Listar pilotos", () -> irA(0)),
                new BarraMiniIconos.Item(IconFactory.documento(IconFactory.BLANCO), "Registrar piloto", () -> irA(1)),
                new BarraMiniIconos.Item(IconFactory.equis(IconFactory.BLANCO), "Eliminar piloto", () -> irA(2)),
                new BarraMiniIconos.Item(IconFactory.lapiz(IconFactory.BLANCO), "Editar piloto", () -> irA(3))
        );
    }

    private void irA(int indice) {
        establecerBarraLateral(construirItemsBarra(), indice);
        switch (indice) {
            case 0 -> mostrarEnCentro(new PilotosListarPane());
            case 1 -> mostrarEnCentro(new PilotosRegistrarPane(this::irAListar));
            case 2 -> mostrarEnCentro(new PilotosEliminarPane());
            case 3 -> mostrarEnCentro(new PilotosEditarPane());
        }
    }

    private void irAListar() {
        irA(0);
    }
}
