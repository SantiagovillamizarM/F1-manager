//Esta es la clase que arranca todo el programa: es el punto de entrada de la aplicacion JavaFX.
//Lo unico que hace es crear el gestor de pantallas y mostrar la primera pantalla (la de bienvenida).

//Esta es la ruta que usa este .java
package com.f1manager;

//Trae GestorEscenas, la clase encargada de manejar el cambio entre las distintas pantallas
//(escenas) del programa, y de guardar el historial para poder ir "atras" y "adelante".
import com.f1manager.infraestructura.ui.util.GestorEscenas;
//Trae PantallaBienvenida, que es la primera pantalla que se ve cuando arranca el programa.
import com.f1manager.infraestructura.ui.PantallaBienvenida;
//Trae Application, la clase base de JavaFX que hay que extender para que el programa pueda
//ejecutarse como una aplicacion visual (con ventana, botones, etc).
import javafx.application.Application;
//Trae Stage, que representa la ventana fisica del programa (el marco donde se ve todo).
import javafx.stage.Stage;

//Este proyecto solo se ejecuta por powershell por que el "Run Java" es un atajo generico
//con mvn clean javafx:run ejecuta todo lo que javafx necesita para funcionar
//Una clase publica llamada "Main" que extiende de Application (asi es como JavaFX sabe que esta es la clase que hay que arrancar)
public class Main extends Application {

    //Este metodo lo llama JavaFX automaticamente al arrancar: recibe el "stagePrimario"
    //(la ventana principal, ya creada por JavaFX) y aca se arma y se muestra la primera pantalla.
    @Override
    public void start(Stage stagePrimario) {
        //Crea el gestor de pantallas, pasandole la ventana principal para que la controle.
        GestorEscenas gestor = new GestorEscenas(stagePrimario);
        //Le pide al gestor que muestre, como pantalla inicial, la de bienvenida.
        gestor.mostrarInicial(new PantallaBienvenida(gestor));
        //Muestra la ventana en pantalla (sin esto la ventana existiria pero no se veria).
        stagePrimario.show();
    }

    //Este es el metodo main de toda la vida en Java: es lo primero que se ejecuta. Aca solo
    //llama a launch(), que es el metodo de JavaFX que se encarga de inicializar todo y despues llamar a start().
    public static void main(String[] args) {
        launch(args);
    }
}
