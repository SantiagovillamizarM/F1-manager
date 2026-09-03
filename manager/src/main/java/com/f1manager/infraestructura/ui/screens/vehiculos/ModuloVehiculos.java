//Esta es la pantalla principal del módulo de Vehículos: muestra las
//tarjetas para elegir entre Listar Monoplazas, Configurar Vehículo o
//Registrar Vehículo, y arma la barrita de iconos de arriba para moverse
//entre esas sub-pantallas.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.vehiculos;

//Trae la clase que maneja el cambio de pantallas (escenas) de toda la app
import com.f1manager.infraestructura.ui.util.GestorEscenas;
//Trae la barra de mini iconos de arriba que sirve para moverse entre las sub-pantallas
import com.f1manager.infraestructura.ui.components.BarraMiniIconos;
//Trae la tarjeta clickeable (el cuadro con icono y texto) que se usa en el menú principal del módulo
import com.f1manager.infraestructura.ui.components.TarjetaOpcion;
//Trae la clase base de la que heredan todos los módulos de gestión (Equipos, Vehiculos, etc), ya trae cosas comunes como el título y el centro de la pantalla
import com.f1manager.infraestructura.ui.ModuloGestionBase;
//Trae la fábrica de iconos, de aca se sacan todos los dibujitos/logos que se usan en la pantalla
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae Insets, que sirve para poner márgenes/espacios alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear elementos (centrado, izquierda, etc)
import javafx.geometry.Pos;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila horizontal)
import javafx.scene.layout.HBox;

//Importa la interfaz List, que define el comportamiento general de una lista en Java
import java.util.List;

//Clase publica llamada "ModuloVehiculos" que hereda de ModuloGestionBase (osea que ya trae armado el título y el layout general)
public class ModuloVehiculos extends ModuloGestionBase {

    //Constructor
    //Recibe el gestor de escenas, le pone el título "GESTIÓN DE VEHÍCULOS" a la pantalla y muestra de una vez el menú con las tarjetas
    public ModuloVehiculos(GestorEscenas gestor) {
        super(gestor, "GESTIÓN DE VEHÍCULOS");
        mostrarMenuTarjetas();
    }

    //Este método arma y muestra el menú principal del módulo con las 3 tarjetas (Listar, Configurar, Registrar)
    private void mostrarMenuTarjetas() {
        //Como estamos en el menú principal todavía no hay que mostrar la barra lateral con los iconos
        quitarBarraLateral();

        //Tarjeta para ir a la sub-pantalla de listar monoplazas (indice 0)
        TarjetaOpcion listar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.monoplaza(IconFactory.BLANCO), 60),
                "LISTAR\nMONOPLAZAS", () -> irA(0), false);
        //Tarjeta para ir a la sub-pantalla de configurar un vehículo existente (indice 1)
        TarjetaOpcion configurar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.logoConfigurarVehiculo(), 60),
                "CONFIGURAR\nVEHÍCULO", () -> irA(1), false);
        //Tarjeta para ir a la sub-pantalla de registrar un vehículo nuevo (indice 2)
        TarjetaOpcion registrar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.documento(IconFactory.BLANCO), 60),
                "REGISTRAR\nVEHÍCULO", () -> irA(2), false);

        //Pone las 3 tarjetas en fila, centradas y con un espacio arriba
        HBox fila = new HBox(24, listar, configurar, registrar);
        fila.setAlignment(Pos.CENTER);
        fila.setPadding(new Insets(40, 0, 0, 0));
        mostrarEnCentro(fila);
    }

    //Arma la lista de items que va a mostrar la barra lateral de mini iconos (uno por cada sub-pantalla)
    private List<BarraMiniIconos.Item> construirItemsBarra() {
        return List.of(
                new BarraMiniIconos.Item(IconFactory.monoplaza(IconFactory.BLANCO), "Listar monoplazas", () -> irA(0)),
                new BarraMiniIconos.Item(IconFactory.logoConfigurarVehiculo(), "Configurar vehículo", () -> irA(1)),
                new BarraMiniIconos.Item(IconFactory.documento(IconFactory.BLANCO), "Registrar vehículo", () -> irA(2))
        );
    }

    //Cambia de sub-pantalla según el índice que le llega: activa la barra lateral con ese índice marcado
    //y en el centro pone la pantalla que corresponda (Listar, Configurar o Registrar)
    private void irA(int indice) {
        establecerBarraLateral(construirItemsBarra(), indice);
        switch (indice) {
            //Caso 0: pantalla de listar monoplazas
            case 0 -> mostrarEnCentro(new VehiculosListarPane());
            //Caso 1: pantalla de configurar un vehículo existente
            case 1 -> mostrarEnCentro(new VehiculosConfigurarPane());
            //Caso 2: pantalla de registrar vehículo nuevo (al terminar/cancelar vuelve a la lista, ver irAListar())
            case 2 -> mostrarEnCentro(new VehiculosRegistrarPane(this::irAListar));
        }
    }

    //Método de conveniencia para volver a la sub-pantalla de listar (indice 0), se usa como acción de "volver" desde Registrar
    private void irAListar() {
        irA(0);
    }
}
