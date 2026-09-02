package com.f1manager.infraestructura.ui.screens.vehiculos;

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
 * Módulo de Gestión de Vehículos: Listar Monoplazas / Configurar vehículo / Volver.
 */
public class ModuloVehiculos extends ModuloGestionBase {

    public ModuloVehiculos(GestorEscenas gestor) {
        super(gestor, "GESTIÓN DE VEHÍCULOS");
        mostrarMenuTarjetas();
    }

    private void mostrarMenuTarjetas() {
        quitarBarraLateral();

        TarjetaOpcion listar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.monoplaza(IconFactory.BLANCO), 60),
                "LISTAR\nMONOPLAZAS", () -> irA(0), false);
        TarjetaOpcion configurar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.logoConfigurarVehiculo(), 60),
                "CONFIGURAR\nVEHÍCULO", () -> irA(1), false);
        TarjetaOpcion registrar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.documento(IconFactory.BLANCO), 60),
                "REGISTRAR\nVEHÍCULO", () -> irA(2), false);

        HBox fila = new HBox(24, listar, configurar, registrar);
        fila.setAlignment(Pos.CENTER);
        fila.setPadding(new Insets(40, 0, 0, 0));
        mostrarEnCentro(fila);
    }

    private List<BarraMiniIconos.Item> construirItemsBarra() {
        return List.of(
                new BarraMiniIconos.Item(IconFactory.monoplaza(IconFactory.BLANCO), "Listar monoplazas", () -> irA(0)),
                new BarraMiniIconos.Item(IconFactory.logoConfigurarVehiculo(), "Configurar vehículo", () -> irA(1)),
                new BarraMiniIconos.Item(IconFactory.documento(IconFactory.BLANCO), "Registrar vehículo", () -> irA(2))
        );
    }

    private void irA(int indice) {
        establecerBarraLateral(construirItemsBarra(), indice);
        switch (indice) {
            case 0 -> mostrarEnCentro(new VehiculosListarPane());
            case 1 -> mostrarEnCentro(new VehiculosConfigurarPane());
            case 2 -> mostrarEnCentro(new VehiculosRegistrarPane(this::irAListar));
        }
    }

    private void irAListar() {
        irA(0);
    }
}
