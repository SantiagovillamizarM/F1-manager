//Esta es la clase que arranca todo el programa: es el punto de entrada de la aplicacion JavaFX.
//Ademas de mostrar la primera pantalla, esta clase es el "compositor" de la arquitectura hexagonal:
//es el UNICO lugar de todo el proyecto donde se decide con que adaptadores concretos (hoy, los de
//MySQL) arranca la capa de aplicacion (DataStore). Ni DataStore ni la UI conocen las clases MySQL
//directamente, solo los puertos (las interfaces del paquete dominio.repositorio); es aca donde se
//conectan unos con otros antes de mostrar cualquier pantalla.

//Esta es la ruta que usa este .java
package com.f1manager;

//Trae DataStore, la capa de aplicacion que hay que inicializar con los adaptadores concretos antes de mostrar cualquier pantalla
import com.f1manager.aplicacion.DataStore;
//Trae GestorEscenas, la clase encargada de manejar el cambio entre las distintas pantallas
//(escenas) del programa, y de guardar el historial para poder ir "atras" y "adelante".
import com.f1manager.infraestructura.ui.util.GestorEscenas;
//Trae PantallaBienvenida, que es la primera pantalla que se ve cuando arranca el programa.
import com.f1manager.infraestructura.ui.PantallaBienvenida;
//Trae los 4 adaptadores de MySQL: son las unicas clases concretas de persistencia que este archivo
//necesita conocer, justo porque es el compositor quien las conecta con los puertos de DataStore.
import com.f1manager.infraestructura.persistencia.CircuitoRepositorioMySQL;
import com.f1manager.infraestructura.persistencia.EquipoRepositorioMySQL;
import com.f1manager.infraestructura.persistencia.PilotoRepositorioMySQL;
import com.f1manager.infraestructura.persistencia.VehiculoRepositorioMySQL;
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
        //Aca pasa lo importante de la arquitectura hexagonal: se crean los 4 adaptadores concretos
        //de MySQL y se le entregan a DataStore.iniciar(), que los guarda tipados como sus interfaces
        //(los puertos). De aca para adelante, ni DataStore ni ninguna pantalla vuelve a nombrar
        //"MySQL": todo el resto del programa solo conoce los puertos. Si algun dia se quisiera
        //cambiar de base de datos, esta es la UNICA linea que habria que tocar.
        DataStore.iniciar(new CircuitoRepositorioMySQL(), new PilotoRepositorioMySQL(),
                new EquipoRepositorioMySQL(), new VehiculoRepositorioMySQL());

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
