//Sub-pantalla para registrar un vehículo nuevo: crea el monoplaza de un
//equipo que todavía no tiene uno (sin esto, ese equipo podía correr con
//velocidad 0 y sin neumático asignado).

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.vehiculos;

//Trae la clase que guarda y maneja toda la información del programa (equipos, pilotos, vehiculos, etc), funciona como la "base de datos" en memoria
import com.f1manager.infraestructura.persistencia.DataStore;
//Trae el error personalizado que se lanza cuando el usuario ingresa datos inválidos
import com.f1manager.dominio.excepcion.ValidacionException;
//Trae la clase Monoplaza, que es el objeto con los datos técnicos del vehículo (y trae las constantes de velocidad/aceleración mínimas y máximas)
import com.f1manager.dominio.modelo.Monoplaza;
//Trae la clase que reproduce los sonidos de confirmación/error de la app
import com.f1manager.infraestructura.ui.util.GestorSonido;
//Trae FXCollections, que sirve para crear listas observables (listas que avisan a la UI cuando cambian, para que el ComboBox se actualice solo)
import javafx.collections.FXCollections;
//Trae Insets, que sirve para poner márgenes/espacios alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear elementos (centrado, izquierda, etc)
import javafx.geometry.Pos;
//Trae Button, el botón que se puede clickear
import javafx.scene.control.Button;
//Trae ComboBox, que es el menú desplegable donde el usuario elige una opción de una lista (aca se usa para elegir el equipo)
import javafx.scene.control.ComboBox;
//Trae Label, que es un texto que se muestra en pantalla (no editable)
import javafx.scene.control.Label;
//Trae TextField, que es el cuadro de texto donde el usuario puede escribir
import javafx.scene.control.TextField;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila horizontal)
import javafx.scene.layout.HBox;
//Trae VBox, un contenedor que acomoda los elementos uno debajo del otro (en columna vertical)
import javafx.scene.layout.VBox;

//Clase publica llamada "VehiculosRegistrarPane" que hereda de VBox (osea que ella misma es una columna donde se van poniendo los elementos)
public class VehiculosRegistrarPane extends VBox {

    //Cuadro de texto donde se escribe el modelo del vehículo nuevo
    private final TextField campoModelo = new TextField();
    //Menú desplegable donde se elige a qué equipo (de los que todavía no tienen vehículo) se le asigna este monoplaza
    private final ComboBox<String> comboEquipo = new ComboBox<>();
    //Cuadro de texto donde se escribe el motor del vehículo nuevo
    private final TextField campoMotor = new TextField();
    //Cuadro de texto donde se escribe la velocidad máxima del vehículo nuevo
    private final TextField campoVelocidad = new TextField();
    //Cuadro de texto donde se escribe la aceleración de 0 a 100 km/h del vehículo nuevo
    private final TextField campoAceleracion = new TextField();
    //Label donde se muestra el resultado de la operación (si se guardó bien o si hubo un error)
    private final Label mensaje = new Label();

    //Constructor
    //Recibe una acción para ejecutar cuando se cancela (normalmente volver a la pantalla de listar) y arma todo el formulario
    public VehiculosRegistrarPane(Runnable alCancelar) {
        setSpacing(20);
        setMaxWidth(600);

        Label titulo = new Label("Registrar nuevo vehículo");
        titulo.getStyleClass().add("titulo-seccion");

        VBox panel = new VBox(16);
        panel.getStyleClass().add("panel-glow");
        panel.setPadding(new Insets(30));

        campoModelo.getStyleClass().add("campo-texto");
        campoModelo.setPromptText("Ej: SF-24");

        comboEquipo.getStyleClass().add("combo-oscuro");
        comboEquipo.setMaxWidth(Double.MAX_VALUE);
        comboEquipo.setPromptText("Seleccione un equipo sin vehículo asignado");
        actualizarEquiposDisponibles();

        campoMotor.getStyleClass().add("campo-texto");
        campoMotor.setPromptText("Ej: Ferrari");

        campoVelocidad.getStyleClass().add("campo-texto");
        campoVelocidad.setPromptText(String.format("Ej: 350 (km/h, entre %.0f y %.0f)",
                Monoplaza.VELOCIDAD_MINIMA_KMH, Monoplaza.VELOCIDAD_MAXIMA_KMH));

        campoAceleracion.getStyleClass().add("campo-texto");
        campoAceleracion.setPromptText(String.format("Ej: 2.6 (segundos 0-100 km/h, entre %.1f y %.1f)",
                Monoplaza.ACELERACION_MINIMA_S, Monoplaza.ACELERACION_MAXIMA_S));

        mensaje.setWrapText(true);

        Button guardar = new Button("GUARDAR");
        guardar.getStyleClass().add("boton-primario");
        guardar.setOnAction(e -> guardar());

        Button cancelar = new Button("CANCELAR");
        cancelar.getStyleClass().add("boton-secundario");
        cancelar.setOnAction(e -> {
            limpiar();
            alCancelar.run();
        });

        HBox botones = new HBox(14, guardar, cancelar);
        botones.setAlignment(Pos.CENTER_LEFT);

        panel.getChildren().addAll(
                etiqueta("Modelo"), campoModelo,
                etiqueta("Equipo"), comboEquipo,
                etiqueta("Motor"), campoMotor,
                etiqueta("Velocidad máxima"), campoVelocidad,
                etiqueta("Aceleración 0-100 km/h"), campoAceleracion,
                mensaje, botones
        );

        getChildren().addAll(titulo, panel);
    }

    //Crea un Label con el estilo de etiqueta de campo, para no repetir código en cada campo del formulario
    private Label etiqueta(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("etiqueta-campo");
        return label;
    }

    //Este método vuelve a llenar el ComboBox solo con los equipos que todavía no tienen un vehículo asignado (un vehículo por equipo nada más)
    private void actualizarEquiposDisponibles() {
        var nombres = DataStore.getInstancia().getEquipos().stream()
                .filter(e -> DataStore.getInstancia().getVehiculoPorEquipo(e.getNombre()) == null)
                .map(com.f1manager.dominio.modelo.Equipo::getNombre).toList();
        comboEquipo.setItems(FXCollections.observableArrayList(nombres));
    }

    //Este método intenta guardar el vehículo nuevo con los datos escritos en el formulario
    private void guardar() {
        try {
            //Se refresca la lista de equipos disponibles por si cambió algo desde que se abrió la pantalla
            actualizarEquiposDisponibles();
            //Le pide al DataStore que registre el vehículo con el modelo, el equipo elegido, el motor, la velocidad y la aceleración
            DataStore.getInstancia().registrarVehiculo(campoModelo.getText(), comboEquipo.getValue(),
                    campoMotor.getText(), campoVelocidad.getText(), campoAceleracion.getText());
            //Si todo salió bien se muestra el mensaje en rojo (positivo), se reproduce el sonido de confirmación y se limpia el formulario
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Vehículo registrado correctamente.");
            GestorSonido.reproducir("Confirmado sound.mp3");
            limpiar();
        } catch (ValidacionException ex) {
            //Si algo salió mal (equipo no elegido, valores fuera de rango, etc) se muestra el mensaje de error y se reproduce el sonido de error
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
            GestorSonido.reproducir("Error sound.mp3");
        }
    }

    //Limpia todos los campos del formulario y refresca la lista de equipos disponibles, dejando la pantalla lista para un nuevo registro
    private void limpiar() {
        campoModelo.clear();
        comboEquipo.setValue(null);
        campoMotor.clear();
        campoVelocidad.clear();
        campoAceleracion.clear();
        actualizarEquiposDisponibles();
    }
}
