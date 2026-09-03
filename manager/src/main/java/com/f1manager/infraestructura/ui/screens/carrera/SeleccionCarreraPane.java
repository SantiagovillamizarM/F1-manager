//Pantalla de selección previa a la carrera: elegir el circuito (a la izquierda),
//ver su trazado dibujado en el centro y elegir las condiciones climáticas antes
//de darle al botón "EMPEZAR CARRERA".

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.carrera;

//Trae la clase que guarda y da acceso a todos los datos de la app (circuitos, pilotos, equipos, etc.)
import com.f1manager.aplicacion.DataStore;
//Trae el error personalizado que se lanza cuando algo no está listo para correr la carrera (por ejemplo un equipo sin vehículo)
import com.f1manager.dominio.excepcion.ValidacionException;
//Trae la clase Circuito para poder mostrar y elegir el circuito de la carrera
import com.f1manager.dominio.modelo.Circuito;
//Trae el enum Clima para poder mostrar y elegir las condiciones climáticas
import com.f1manager.dominio.modelo.Clima;
//Trae la clase que reproduce sonidos (por ejemplo el sonido de error)
import com.f1manager.infraestructura.ui.util.GestorSonido;
//Trae la clase que dibuja el trazado del circuito sobre el lienzo (Canvas)
import com.f1manager.infraestructura.ui.util.PistaGenerador;
//Trae Insets, que sirve para poner márgenes/espacios alrededor de los elementos visuales
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear elementos (centrado, arriba, etc.)
import javafx.geometry.Pos;
//Trae Canvas, que es como una hoja en blanco donde se puede dibujar a mano (líneas, óvalos, etc.)
import javafx.scene.canvas.Canvas;
//Trae GraphicsContext, que es el "pincel" que realmente dibuja encima del Canvas
import javafx.scene.canvas.GraphicsContext;
//Trae Button, el botón que el usuario puede presionar
import javafx.scene.control.Button;
//Trae Label, el texto que se muestra en pantalla
import javafx.scene.control.Label;
//Trae ScrollPane, un contenedor que agrega una barra de desplazamiento cuando el contenido no cabe
import javafx.scene.control.ScrollPane;
//Trae todas las clases de layout (VBox, HBox, BorderPane, StackPane, etc.) que organizan los elementos en pantalla
import javafx.scene.layout.*;

//Importa EnumMap, un diccionario optimizado para cuando la clave es un enum (en este caso Clima)
import java.util.EnumMap;
//Importa la interfaz general Map, que sirve como plantilla para crear diccionarios de clave y valor
import java.util.Map;
//Importa BiConsumer, que es una "función" que recibe dos datos y no devuelve nada (se usa para avisar "empezar carrera" pasando el circuito y el clima elegidos)
import java.util.function.BiConsumer;

//Clase pública que arma la pantalla de selección de circuito y clima antes de empezar la carrera
public class SeleccionCarreraPane extends BorderPane {

    //Columna vertical donde van las filas de los circuitos disponibles para elegir (izquierda)
    private final VBox columnaCircuitos = new VBox(12);
    //Columna central donde va el nombre del circuito, su dibujo, el clima y el botón de empezar
    private final VBox panelCentro = new VBox(20);
    //El lienzo (hoja en blanco) donde se dibuja el trazado del circuito seleccionado
    private final Canvas lienzoPista = new Canvas(560, 340);
    //Texto que muestra el nombre y el país del circuito seleccionado
    private final Label nombreCircuitoLabel = new Label();
    //Texto que muestra la descripción del circuito seleccionado
    private final Label descripcionCircuitoLabel = new Label();
    //Botón que arranca la carrera una vez que ya se eligió circuito y clima
    private final Button botonEmpezar = new Button("EMPEZAR CARRERA");
    //Texto (vacío normalmente) donde se muestra un mensaje de error si algo falla al intentar empezar
    private final Label mensajeError = new Label();

    //Diccionario que relaciona cada clima con su tarjeta visual (VBox), para poder resaltarla cuando se selecciona
    private final Map<Clima, VBox> tarjetasClima = new EnumMap<>(Clima.class);
    //Guarda el circuito que el usuario ya eligió (null mientras todavía no elige ninguno)
    private Circuito circuitoSeleccionado;
    //Guarda el clima que el usuario ya eligió (null mientras todavía no elige ninguno)
    private Clima climaSeleccionado;
    //Guarda la fila (VBox) del circuito que está resaltada actualmente en la lista, para poder quitarle el resaltado si se elige otro
    private VBox filaCircuitoSeleccionada;

    //Constructor simplificado: se usa cuando no hay circuito ni clima fijo (el usuario elige todo desde cero)
    public SeleccionCarreraPane(BiConsumer<Circuito, Clima> alEmpezar) {
        this(alEmpezar, null, null);
    }

    //Constructor para el modo campeonato cuando el circuito ya viene decidido por la fecha del calendario:
    //si circuitoFijo no es null, se omite la lista de selección y ese circuito queda preseleccionado.
    public SeleccionCarreraPane(BiConsumer<Circuito, Clima> alEmpezar, Circuito circuitoFijo) {
        this(alEmpezar, circuitoFijo, null);
    }

    //Constructor
    //Este es el constructor completo (los otros dos solo le pasan valores por defecto a este) y es el
    //que realmente arma toda la pantalla. circuitoFijo y climaFijo, si no son null, indican que en modo
    //campeonato el circuito y/o el clima de esa fecha ya se decidieron solos y no se dejan elegir a mano.
    public SeleccionCarreraPane(BiConsumer<Circuito, Clima> alEmpezar, Circuito circuitoFijo, Clima climaFijo) {
        setPadding(new Insets(10));

        //Si no viene un circuito fijo, se arma la lista de circuitos para elegir en la parte izquierda
        if (circuitoFijo == null) {
            // ---- Izquierda: lista de circuitos ----
            Label tituloCircuitos = new Label("Circuitos disponibles");
            tituloCircuitos.getStyleClass().add("titulo-seccion");

            ScrollPane scroll = new ScrollPane(columnaCircuitos);
            scroll.setFitToWidth(true);
            scroll.setPrefWidth(360);
            scroll.getStyleClass().add("scroll-oscuro");
            scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

            //Recorre todos los circuitos guardados en DataStore y agrega una fila clickeable por cada uno
            for (Circuito c : DataStore.getInstancia().getCircuitos()) {
                columnaCircuitos.getChildren().add(construirFilaCircuito(c));
            }

            VBox columnaIzquierda = new VBox(16, tituloCircuitos, scroll);
            columnaIzquierda.setPadding(new Insets(0, 20, 0, 0));
            columnaIzquierda.setPrefWidth(380);
            setLeft(columnaIzquierda);
        }

        // ---- Centro: pista + clima + botón ----
        nombreCircuitoLabel.getStyleClass().add("titulo-seccion");
        descripcionCircuitoLabel.getStyleClass().add("texto-secundario");
        descripcionCircuitoLabel.setWrapText(true);
        //Al arrancar todavía no hay circuito elegido, entonces se muestra el mensaje por defecto
        mostrarSinCircuito();

        StackPane contenedorLienzo = new StackPane(lienzoPista);
        contenedorLienzo.getStyleClass().add("panel");
        contenedorLienzo.setPadding(new Insets(14));

        //El título cambia según si el clima se puede elegir (normal) o ya viene fijo (modo campeonato)
        Label tituloClima = new Label(climaFijo == null
                ? "Seleccione las condiciones climáticas" : "Pronóstico del clima para esta carrera");
        tituloClima.getStyleClass().add("titulo-seccion");

        HBox filaClima = new HBox(16);
        filaClima.setAlignment(Pos.CENTER);
        if (climaFijo == null) {
            //Sin clima fijo: se muestran las 4 opciones de clima como tarjetas clickeables
            for (Clima clima : Clima.values()) {
                VBox tarjeta = construirTarjetaClima(clima);
                tarjetasClima.put(clima, tarjeta);
                filaClima.getChildren().add(tarjeta);
            }
        } else {
            //Con clima fijo: se muestra una sola tarjeta informativa (no se puede tocar) y queda ya preseleccionada
            filaClima.getChildren().add(construirTarjetaClimaFija(climaFijo));
            climaSeleccionado = climaFijo;
        }

        botonEmpezar.getStyleClass().add("boton-grande");
        //Arranca deshabilitado porque todavía no hay circuito ni clima elegidos
        botonEmpezar.setDisable(true);
        botonEmpezar.setOnAction(e -> {
            if (circuitoSeleccionado != null && climaSeleccionado != null) {
                try {
                    // Si algún equipo con pilotos no tiene vehículo, esa carrera lo simularía con
                    // velocidad 0 y sin neumático — se corta aquí en vez de dejarlo llegar así.
                    DataStore.getInstancia().validarEquiposListosParaCarrera();
                    mensajeError.setText("");
                    //Avisa hacia afuera (a quien creó esta pantalla) que ya se puede empezar, pasando circuito y clima elegidos
                    alEmpezar.accept(circuitoSeleccionado, climaSeleccionado);
                } catch (ValidacionException ex) {
                    //Si la validación falla, se muestra el motivo en pantalla y suena un sonido de error
                    mensajeError.setText(ex.getMessage());
                    GestorSonido.reproducir("Error sound.mp3");
                }
            }
        });

        mensajeError.getStyleClass().add("error-label");
        mensajeError.setWrapText(true);
        mensajeError.setMaxWidth(420);

        VBox cajaBoton = new VBox(10, botonEmpezar, mensajeError);
        cajaBoton.setAlignment(Pos.CENTER);
        cajaBoton.setPadding(new Insets(10, 0, 0, 0));

        panelCentro.setAlignment(Pos.TOP_CENTER);
        panelCentro.getChildren().addAll(nombreCircuitoLabel, descripcionCircuitoLabel, contenedorLienzo,
                tituloClima, filaClima, cajaBoton);
        panelCentro.setPadding(new Insets(0, 0, 0, 10));

        // Si la ventana es pequeña y el contenido no cabe completo, se puede hacer
        // scroll en vez de recortar el botón "EMPEZAR CARRERA" fuera de la vista.
        ScrollPane scrollCentro = new ScrollPane(panelCentro);
        scrollCentro.setFitToWidth(true);
        scrollCentro.getStyleClass().add("scroll-oscuro");
        scrollCentro.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setCenter(scrollCentro);

        //Si viene un circuito fijo (modo campeonato), se selecciona automáticamente sin que el usuario tenga que tocar nada
        if (circuitoFijo != null) {
            seleccionarCircuito(circuitoFijo, null);
        }
    }

    //Arma una fila clickeable (nombre + país/vueltas) para un circuito de la lista de la izquierda
    private VBox construirFilaCircuito(Circuito circuito) {
        Label nombre = new Label(circuito.getNombre());
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f5f6fa;");
        Label detalle = new Label(circuito.getPais() + "  ·  " + circuito.getVueltas() + " vueltas");
        detalle.getStyleClass().add("texto-secundario");

        VBox fila = new VBox(4, nombre, detalle);
        fila.getStyleClass().add("fila-lista");
        //Al hacer clic en la fila, se marca este circuito como el elegido
        fila.setOnMouseClicked(e -> seleccionarCircuito(circuito, fila));
        return fila;
    }

    //Marca un circuito como seleccionado: lo resalta en la lista, actualiza los textos y dibuja su trazado en el lienzo
    private void seleccionarCircuito(Circuito circuito, VBox fila) {
        // fila es null en modo campeonato (circuito fijo, sin lista para elegir).
        if (fila != null) {
            //Le quita el resaltado a la fila que estaba seleccionada antes (si había una)
            if (filaCircuitoSeleccionada != null) {
                filaCircuitoSeleccionada.getStyleClass().remove("fila-lista-seleccionada");
                filaCircuitoSeleccionada.getStyleClass().add("fila-lista");
            }
            //Le pone el resaltado a la fila recién elegida
            fila.getStyleClass().remove("fila-lista");
            fila.getStyleClass().add("fila-lista-seleccionada");
            filaCircuitoSeleccionada = fila;
        }

        circuitoSeleccionado = circuito;
        nombreCircuitoLabel.setText(circuito.getNombre() + " — " + circuito.getPais());
        descripcionCircuitoLabel.setText(circuito.getDescripcion());

        //Limpia el lienzo (borra el dibujo anterior) y dibuja el trazado del nuevo circuito elegido
        GraphicsContext gc = lienzoPista.getGraphicsContext2D();
        gc.clearRect(0, 0, lienzoPista.getWidth(), lienzoPista.getHeight());
        PistaGenerador.paraCircuito(circuito).dibujar(gc, 0, 0, lienzoPista.getWidth(), lienzoPista.getHeight(), true);

        //Revisa si con este circuito ya elegido se puede habilitar el botón de empezar
        actualizarBotonEmpezar();
    }

    //Textos por defecto que se muestran mientras todavía no se ha elegido ningún circuito
    private void mostrarSinCircuito() {
        nombreCircuitoLabel.setText("Selecciona un circuito");
        descripcionCircuitoLabel.setText("Elige un circuito de la lista para ver su trazado antes de comenzar la carrera.");
    }

    //Arma una tarjeta de clima clickeable (para cuando el usuario puede elegir el clima libremente)
    private VBox construirTarjetaClima(Clima clima) {
        Label etiqueta = new Label(clima.getEtiqueta());
        etiqueta.getStyleClass().add("texto-normal");

        VBox tarjeta = new VBox(etiqueta);
        tarjeta.getStyleClass().add("opcion-clima");
        tarjeta.setPrefWidth(130);
        tarjeta.setAlignment(Pos.CENTER);
        //Al hacer clic en la tarjeta, se marca este clima como el elegido
        tarjeta.setOnMouseClicked(e -> seleccionarClima(clima));
        return tarjeta;
    }

    //Tarjeta de clima informativa (no clickeable), usada en modo campeonato cuando el clima ya se decidió solo
    private VBox construirTarjetaClimaFija(Clima clima) {
        Label etiqueta = new Label(clima.getEtiqueta());
        etiqueta.getStyleClass().add("texto-normal");

        VBox tarjeta = new VBox(etiqueta);
        tarjeta.getStyleClass().add("opcion-clima-seleccionada");
        tarjeta.setPrefWidth(130);
        tarjeta.setAlignment(Pos.CENTER);
        return tarjeta;
    }

    //Marca un clima como seleccionado y actualiza cuál tarjeta se ve resaltada
    private void seleccionarClima(Clima clima) {
        climaSeleccionado = clima;
        //Recorre todas las tarjetas de clima: a la que coincide con el clima elegido le pone el estilo "seleccionada", al resto el normal
        for (var entrada : tarjetasClima.entrySet()) {
            entrada.getValue().getStyleClass().setAll(entrada.getKey() == clima ? "opcion-clima-seleccionada" : "opcion-clima");
        }
        //Revisa si con este clima ya elegido se puede habilitar el botón de empezar
        actualizarBotonEmpezar();
    }

    //El botón de empezar solo se habilita cuando YA se eligieron tanto el circuito como el clima
    private void actualizarBotonEmpezar() {
        botonEmpezar.setDisable(circuitoSeleccionado == null || climaSeleccionado == null);
    }
}
