//Esta es la pantalla de resultados de una carrera: muestra la tabla con la
//posicion final de todos los pilotos y, al hacer click en una fila, la ficha
//detallada de ese piloto (vueltas, desgaste, paradas en boxes, fotos de choque, etc).

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.carrera;

//Trae la clase Circuito para poder mostrar el nombre, pais y vueltas del circuito corrido
import com.f1manager.dominio.modelo.Circuito;
//Trae la clase ResultadoCarrera, que guarda el resultado de un piloto en la carrera (posicion, tiempos, choques, etc)
import com.f1manager.dominio.modelo.ResultadoCarrera;
//Trae el SimuladorCarrera, para poder leer el objeto con todos los resultados de la simulacion ya corrida
import com.f1manager.dominio.servicio.SimuladorCarrera;
//Trae la utilidad que arma las fotos/imagenes de un choque
import com.f1manager.infraestructura.ui.util.FotosChoque;
//Trae la fabrica de iconos/avatares para poder dibujar la foto del piloto en la ficha de detalle
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae Insets, que sirve para poner margenes/espacios alrededor de los elementos
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear los elementos (centrado, arriba, izquierda, etc)
import javafx.geometry.Pos;
//Trae Button, el boton que se puede hacer click
import javafx.scene.control.Button;
//Trae Label, que es un texto que se muestra en pantalla (no se puede editar)
import javafx.scene.control.Label;
//Trae ScrollPane, un contenedor que agrega una barra de desplazamiento (scroll) cuando el contenido no cabe
import javafx.scene.control.ScrollPane;
//Trae Image, que representa una imagen cargada en memoria
import javafx.scene.image.Image;
//Trae ImageView, el elemento visual que dibuja una Image en pantalla
import javafx.scene.image.ImageView;
//Trae FlowPane, un contenedor que acomoda los elementos en fila y salta de linea automaticamente cuando no caben (como texto que se envuelve)
import javafx.scene.layout.FlowPane;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila)
import javafx.scene.layout.HBox;
//Trae Priority, que sirve para decirle a un elemento que crezca para llenar el espacio libre
import javafx.scene.layout.Priority;
//Trae Region, un elemento vacio que se usa como "espaciador" o para medidas
import javafx.scene.layout.Region;
//Trae StackPane, un contenedor que apila elementos uno encima del otro (aca se usa para el panel de detalle del piloto)
import javafx.scene.layout.StackPane;
//Trae VBox, un contenedor que acomoda los elementos uno debajo del otro (en columna)
import javafx.scene.layout.VBox;

//Trae la interfaz List, que define el comportamiento general de una lista en Java
import java.util.List;
//Trae Collectors, que sirve para juntar/unir los elementos de un stream (por ejemplo, para pegar textos con una coma)
import java.util.stream.Collectors;

//Esta es la clase publica "ResultadosCarreraPane" que extiende de VBox (osea que ES un VBox, un contenedor en columna)
public class ResultadosCarreraPane extends VBox {

    //Panel donde se dibuja la ficha de detalle del piloto seleccionado (se le va cambiando el contenido, no se recrea el panel)
    private final StackPane panelDetalle = new StackPane();
    //Guarda el resultado completo de la simulacion, lo necesita mostrarDetalle/construirSeccionFotosChoque para sacar las fotos de choque
    private final SimuladorCarrera.ResultadoSimulacion simulacion;
    //Guarda cual fila de la tabla esta seleccionada actualmente, para poder quitarle el resaltado cuando se elige otra
    private HBox filaSeleccionada;

    //Constructor
    //Version corta: usa los textos por defecto de los botones ("NUEVA CARRERA" y "MENÚ PRINCIPAL"), usada por la carrera suelta
    public ResultadosCarreraPane(Circuito circuito, SimuladorCarrera.ResultadoSimulacion simulacion,
                                  Runnable nuevaCarrera, Runnable alMenu) {
        //LLama al otro constructor (el completo) pasandole los textos por defecto
        this(circuito, simulacion, "NUEVA CARRERA", nuevaCarrera, "MENÚ PRINCIPAL", alMenu);
    }

    //Constructor
    //Permite personalizar el texto y la accion de los dos botones (usado por el modo campeonato,
    //que necesita botones distintos como "VER TABLA DE POSICIONES" y "ABANDONAR CAMPEONATO")
    public ResultadosCarreraPane(Circuito circuito, SimuladorCarrera.ResultadoSimulacion simulacion,
                                  String textoBoton1, Runnable accionBoton1,
                                  String textoBoton2, Runnable accionBoton2) {
        this.simulacion = simulacion;
        setSpacing(20);
        setPadding(new Insets(10));

        //Texto grande con el titulo de la pantalla
        Label titulo = new Label("Resultado de la carrera");
        titulo.getStyleClass().add("titulo-principal");

        //Texto con el nombre del circuito, el pais, el clima real que salio y el numero de vueltas
        Label subtitulo = new Label(circuito.getNombre() + "  ·  " + circuito.getPais()
                + "  ·  Clima: " + simulacion.getClimaReal().getEtiqueta()
                + "  ·  " + circuito.getVueltas() + " vueltas");
        subtitulo.getStyleClass().add("texto-secundario");

        //Columna donde van a ir todas las filas de la tabla de resultados
        VBox tabla = new VBox(2);
        tabla.getStyleClass().add("panel");
        tabla.setPadding(new Insets(10));

        //Trae la lista de resultados ya ordenada por posicion (el orden lo arma el SimuladorCarrera, aca solo se dibuja)
        var resultados = simulacion.getResultados();
        //Tiempo del ganador (primera posicion), se usa como referencia para calcular la diferencia de todos los demas
        double tiempoLider = resultados.get(0).getTiempoSegundos();

        //Este es un bucle for-each que recorre cada resultado de la carrera y arma su fila en la tabla
        for (ResultadoCarrera r : resultados) {
            tabla.getChildren().add(construirFila(r, tiempoLider));
        }

        //Envuelve la tabla en un ScrollPane para que se pueda desplazar si hay muchos pilotos
        ScrollPane scroll = new ScrollPane(tabla);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(460);
        scroll.setPrefWidth(620);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        //Prepara el panel de detalle (todavia vacio, se llena mas abajo con mostrarDetalle)
        panelDetalle.getStyleClass().add("panel");
        panelDetalle.setPadding(new Insets(24));
        panelDetalle.setPrefSize(480, 460);
        panelDetalle.setMinWidth(380);

        //Junta la tabla (scroll) y el panel de detalle uno al lado del otro
        HBox cuerpo = new HBox(24, scroll, panelDetalle);
        cuerpo.setAlignment(Pos.TOP_CENTER);

        //Primer boton (por ejemplo "NUEVA CARRERA" o "VER TABLA DE POSICIONES")
        Button boton1 = new Button(textoBoton1);
        boton1.getStyleClass().add("boton-primario");
        boton1.setOnAction(e -> accionBoton1.run());

        //Segundo boton (por ejemplo "MENÚ PRINCIPAL" o "ABANDONAR CAMPEONATO")
        Button boton2 = new Button(textoBoton2);
        boton2.getStyleClass().add("boton-secundario");
        boton2.setOnAction(e -> accionBoton2.run());

        //Fila que junta los dos botones
        HBox botones = new HBox(14, boton1, boton2);
        botones.setAlignment(Pos.CENTER);
        botones.setPadding(new Insets(10, 0, 0, 0));

        //Columna final que centra el titulo, subtitulo, cuerpo (tabla+detalle) y botones
        VBox contenedorCentrado = new VBox(18, titulo, subtitulo, cuerpo, botones);
        contenedorCentrado.setAlignment(Pos.TOP_CENTER);
        getChildren().add(contenedorCentrado);

        // Por defecto se muestra la ficha del ganador
        mostrarDetalle(resultados.get(0), tiempoLider);
    }

    //Este metodo privado arma una fila de la tabla de resultados: posicion, nombre, equipo y tiempos
    private HBox construirFila(ResultadoCarrera r, double tiempoLider) {
        //Si la posicion es 1, este es el ganador (para pintarlo distinto, con color dorado y texto rojo de acento)
        boolean esGanador = r.getPosicion() == 1;

        //Texto con la posicion en formato "P 1", "P 2", etc
        Label posicion = new Label("P " + r.getPosicion());
        posicion.setPrefWidth(50);
        posicion.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        //Si es el ganador se pinta dorado (#ffd400), si no del color normal (#f5f6fa)
        posicion.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: "
                + (esGanador ? "#ffd400" : "#f5f6fa") + ";");

        //Texto con el nombre del piloto
        Label nombre = new Label(r.getPiloto().getNombre());
        nombre.setPrefWidth(190);
        nombre.setStyle("-fx-font-weight: bold; -fx-text-fill: #f5f6fa; -fx-font-size: 14px;");

        //Texto con el equipo del piloto
        Label equipo = new Label(r.getPiloto().getEquipo());
        equipo.getStyleClass().add("texto-secundario");
        equipo.setPrefWidth(190);

        //Texto con la diferencia de tiempo contra el lider (o el tiempo total si es el mismo lider)
        Label tiempo = new Label("Tiempo: " + r.getDiferenciaFormateada(tiempoLider));
        tiempo.getStyleClass().add(esGanador ? "texto-rojo" : "texto-normal");

        //Texto con el tiempo promedio por vuelta de ese piloto
        Label promedio = new Label("Promedio/vuelta: " + ResultadoCarrera.formatearTiempo(r.getTiempoPromedioVuelta()));
        promedio.getStyleClass().add("texto-secundario");

        //Columna que junta el tiempo y el promedio, uno debajo del otro
        VBox columnaTiempo = new VBox(2, tiempo, promedio);
        columnaTiempo.setPrefWidth(220);

        //Region vacia que se estira (Priority.ALWAYS) para empujar la columna de tiempo hacia el borde derecho de la fila
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        //Arma la fila completa con la posicion, nombre, equipo, espaciador y columna de tiempo
        HBox fila = new HBox(10, posicion, nombre, equipo, espaciador, columnaTiempo);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(12, 16, 12, 16));
        fila.getStyleClass().add("fila-lista");
        //Cuando se hace click en la fila, la selecciona y muestra su ficha de detalle
        fila.setOnMouseClicked(e -> seleccionar(fila, r, tiempoLider));
        return fila;
    }

    //Este metodo privado se encarga de resaltar la fila que se acaba de clickear y quitarle el resaltado a la anterior
    private void seleccionar(HBox fila, ResultadoCarrera r, double tiempoLider) {
        //Si ya habia una fila seleccionada antes, le quita el estilo de "seleccionada" y le vuelve a poner el normal
        if (filaSeleccionada != null) {
            filaSeleccionada.getStyleClass().remove("fila-lista-seleccionada");
            filaSeleccionada.getStyleClass().add("fila-lista");
        }
        //A la fila nueva le quita el estilo normal y le pone el de "seleccionada" (para resaltarla)
        fila.getStyleClass().remove("fila-lista");
        fila.getStyleClass().add("fila-lista-seleccionada");
        //Guarda esta fila como la seleccionada actual, para la proxima vez que se haga click en otra
        filaSeleccionada = fila;
        //Muestra la ficha de detalle de este piloto
        mostrarDetalle(r, tiempoLider);
    }

    //Este metodo privado arma toda la ficha de detalle de un piloto (encabezado, datos generales, vueltas y fotos de choque si aplica)
    //y la pone dentro del panelDetalle
    private void mostrarDetalle(ResultadoCarrera r, double tiempoLider) {
        //Avatar/foto del piloto
        StackPane avatar = IconFactory.avatarPiloto(r.getPiloto(), 70);

        //Texto con el nombre del piloto
        Label nombre = new Label(r.getPiloto().getNombre());
        nombre.getStyleClass().add("titulo-seccion");

        //Texto con el equipo y el rol del piloto (por ejemplo, titular o reserva)
        Label equipoRol = new Label(r.getPiloto().getEquipo() + "  ·  " + r.getPiloto().getRol().getEtiqueta());
        equipoRol.getStyleClass().add("texto-rojo");

        //Fila que junta el avatar con el nombre y el equipo/rol (uno al lado del otro)
        HBox encabezado = new HBox(18, avatar, new VBox(6, nombre, equipoRol));
        encabezado.setAlignment(Pos.CENTER_LEFT);

        //Texto con el modelo del monoplaza usado (o "N/D" si no hay monoplaza asignado)
        Label vehiculo = new Label("Vehículo: " + (r.getMonoplaza() != null ? r.getMonoplaza().getModelo() : "N/D"));
        //Texto con la velocidad maxima que alcanzo el piloto durante la carrera
        Label velocidadMax = new Label(String.format("Velocidad máxima alcanzada: %.0f km/h", r.getVelocidadMaximaAlcanzada()));
        //Texto con la posicion final del piloto
        Label posicionFinal = new Label("Posición final: P" + r.getPosicion());
        //Texto con el tiempo total (diferencia contra el lider)
        Label tiempoTotal = new Label("Tiempo total: " + r.getDiferenciaFormateada(tiempoLider));
        //Texto con el tiempo promedio por vuelta
        Label promedio = new Label("Promedio por vuelta: " + ResultadoCarrera.formatearTiempo(r.getTiempoPromedioVuelta()));
        //Saca la etiqueta del tipo de neumatico usado, o "N/D" si no hay monoplaza o no tiene neumatico asignado
        String neumatico = r.getMonoplaza() != null && r.getMonoplaza().getTipoNeumatico() != null
                ? r.getMonoplaza().getTipoNeumatico().getEtiqueta() : "N/D";
        //Texto con el tipo de neumatico y cuanto desgaste tiene (sobre 100)
        Label desgaste = new Label(String.format("Neumático: %s  ·  Desgaste de las llantas: %.0f/100", neumatico, r.getDesgasteFinal()));
        //Lista con los numeros de vuelta en los que el piloto paso por boxes
        List<Integer> paradas = r.getParadasEnBoxes();
        //Si la lista de paradas esta vacia, dice que no hubo paradas; si no, arma un texto uniendo cada vuelta con comas
        //(map convierte cada numero a texto y collect/joining los pega separados por ", vuelta ")
        String textoParadas = paradas.isEmpty() ? "Sin paradas en boxes"
                : "Paradas en boxes: vuelta " + paradas.stream().map(String::valueOf).collect(Collectors.joining(", vuelta "));
        Label pits = new Label(textoParadas);
        //Este es un bucle for-each que le pone a todos estos textos el mismo estilo, para no repetir la linea 7 veces
        for (Label l : List.of(vehiculo, velocidadMax, posicionFinal, tiempoTotal, promedio, desgaste, pits)) {
            l.getStyleClass().add("texto-normal");
        }

        //Titulo de la seccion de vueltas: si el piloto no termino (DNF, osea que no acabo la carrera), aclara que son
        //los tiempos de antes del choque
        Label tituloVueltas = new Label(r.isDnf() ? "Tiempos por vuelta (antes del choque)" : "Tiempos por vuelta");
        tituloVueltas.getStyleClass().add("etiqueta-campo");

        //Columna donde van las lineas con el tiempo de cada vuelta
        VBox listaVueltas = new VBox(4);
        //Lista con el tiempo de cada vuelta que corrio el piloto
        List<Double> vueltas = r.getTiemposPorVuelta();
        //Cuantas vueltas alcanzo a completar el piloto (menos que el total si choco antes de terminar)
        int vueltasCompletadas = r.getVueltasCompletadas();
        //Este es un bucle for que recorre solo las vueltas que el piloto alcanzo a completar, mostrando su tiempo formateado
        for (int i = 0; i < vueltasCompletadas; i++) {
            Label linea = new Label("Vuelta " + (i + 1) + ":  " + ResultadoCarrera.formatearTiempo(vueltas.get(i)));
            linea.getStyleClass().add("texto-secundario");
            listaVueltas.getChildren().add(linea);
        }
        //Envuelve la lista de vueltas en un ScrollPane para que se pueda desplazar si hay muchas vueltas
        ScrollPane scrollVueltas = new ScrollPane(listaVueltas);
        scrollVueltas.setFitToWidth(true);
        scrollVueltas.setPrefHeight(200);
        scrollVueltas.getStyleClass().add("scroll-oscuro");
        scrollVueltas.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        //Junta todo el contenido de la ficha en una sola columna
        VBox contenido = new VBox(12, encabezado, vehiculo, velocidadMax, posicionFinal, tiempoTotal, promedio,
                desgaste, pits, tituloVueltas, scrollVueltas);
        //Si el piloto no termino la carrera (DNF), le agrega ademas la seccion con las fotos del choque
        if (r.isDnf()) {
            contenido.getChildren().add(construirSeccionFotosChoque(r));
        }

        //Envuelve todo el contenido de la ficha en un ScrollPane, por si no cabe completo en el panel de detalle
        ScrollPane scrollDetalle = new ScrollPane(contenido);
        scrollDetalle.setFitToWidth(true);
        scrollDetalle.getStyleClass().add("scroll-oscuro");
        scrollDetalle.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        //Reemplaza el contenido del panel de detalle por la ficha recien armada
        panelDetalle.getChildren().setAll(scrollDetalle);
    }

    //Este metodo privado arma la seccion con las 3 fotos del choque de este piloto, con el titulo
    //segun si fue un choque en solitario o contra otro piloto (choque grupal)
    private VBox construirSeccionFotosChoque(ResultadoCarrera r) {
        //Si el choque fue contra otro piloto (choque grupal), arma el titulo con los dos nombres; si fue en solitario, con uno solo
        String titulo = r.esChoqueGrupal()
                ? "Imágenes del choque entre " + r.getPiloto().getNombre() + " y " + r.getRivalChoque().getNombre()
                : "Imagen de choque de " + r.getPiloto().getNombre();

        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("etiqueta-campo");
        etiqueta.setWrapText(true);

        //Fila que acomoda las fotos y salta de linea si no caben todas en el ancho disponible
        FlowPane filaFotos = new FlowPane(10, 10);
        //Este es un bucle for-each que recorre las fotos de este choque (las trae FotosChoque.paraChoque) y las dibuja una por una
        for (Image foto : FotosChoque.paraChoque(simulacion, r)) {
            ImageView vista = new ImageView(foto);
            vista.setPreserveRatio(true);
            vista.setFitWidth(170);
            filaFotos.getChildren().add(vista);
        }

        return new VBox(8, etiqueta, filaFotos);
    }
}
