//Pantalla que anima la carrera sobre el trazado del circuito elegido. Los resultados finales
//(quién gana, en qué orden llegan) ya se calcularon de antemano con el SimuladorCarrera, y esta
//pantalla solo los "actúa" visualmente: cada piloto avanza a una velocidad proporcional a su
//tiempo final, así que el orden de llegada de la animación coincide siempre con la simulación real.

package com.f1manager.infraestructura.ui.screens.carrera;

//Trae la clase que guarda y da acceso a todos los datos de la app (circuitos, pilotos, equipos, vehículos, etc.)
import com.f1manager.infraestructura.persistencia.DataStore;
//Trae todas las clases del modelo de dominio (Piloto, Circuito, Clima, ResultadoCarrera, etc.)
import com.f1manager.dominio.modelo.*;
//Trae la clase que calcula de antemano el resultado completo de la carrera (tiempos, choques, paradas en boxes)
import com.f1manager.dominio.servicio.SimuladorCarrera;
//Trae la clase que arma las fotos que se muestran cuando un piloto choca
import com.f1manager.infraestructura.ui.util.FotosChoque;
//Trae la clase que dibuja el trazado del circuito y calcula posiciones sobre esa pista
import com.f1manager.infraestructura.ui.util.PistaGenerador;
//Trae AnimationTimer, que es un "reloj" de JavaFX que llama a un método una y otra vez, en cada fotograma (unas 60 veces por segundo), y así es como se logra el efecto de movimiento
import javafx.animation.AnimationTimer;
//Trae Insets, que sirve para poner márgenes/espacios alrededor de los elementos visuales
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear elementos (centrado, arriba, etc.)
import javafx.geometry.Pos;
//Trae Canvas, que es como una hoja en blanco donde se puede dibujar a mano (líneas, óvalos, etc.)
import javafx.scene.canvas.Canvas;
//Trae GraphicsContext, que es el "pincel" que realmente dibuja encima del Canvas
import javafx.scene.canvas.GraphicsContext;
//Trae Label, el texto que se muestra en pantalla
import javafx.scene.control.Label;
//Trae ScrollPane, un contenedor que agrega una barra de desplazamiento cuando el contenido no cabe
import javafx.scene.control.ScrollPane;
//Trae Slider, la barra deslizante que deja al usuario elegir un número dentro de un rango (aquí, la velocidad de la simulación)
import javafx.scene.control.Slider;
//Trae Image, que representa una imagen cargada en memoria (las fotos de los choques)
import javafx.scene.image.Image;
//Trae ImageView, el componente visual que muestra una Image en pantalla
import javafx.scene.image.ImageView;
//Trae todas las clases de layout (VBox, HBox, BorderPane, StackPane, etc.) que organizan los elementos en pantalla
import javafx.scene.layout.*;
//Trae Color, para poder definir colores (los puntos de cada piloto en la pista, por ejemplo)
import javafx.scene.paint.Color;

//Importa varias utilidades de colecciones de una sola vez (List, Map, ArrayList, LinkedHashMap, HashMap, HashSet, Set, etc.)
import java.util.*;
//Importa Consumer, que es una "función" que recibe un dato y no devuelve nada (se usa para avisar que la carrera ya terminó)
import java.util.function.Consumer;

//Clase pública que arma y controla toda la animación visual de una carrera
public class AnimacionCarreraPane extends BorderPane {

    //Cuánto dura la animación completa, en milisegundos de "tiempo real" (a velocidad x1 del slider). Es un tiempo fijo
    //de pantalla que no tiene nada que ver con lo que dura la carrera en la simulación (esa dura lo que duren las vueltas).
    private static final long DURACION_ANIMACION_MS = 24000;
    // Cuánto dura, en el tiempo simulado de la animación, la pausa visual de un piloto en boxes.
    private static final double PAUSA_PIT_MS = 2200;
    //Paleta de colores fijos que se le van repartiendo a los pilotos (uno por uno) para que cada punto en la pista se distinga de los demás
    private static final Color[] PALETA = {
            Color.web("#ff2b2b"), Color.web("#00d4ff"), Color.web("#ffd400"), Color.web("#39ff88"),
            Color.web("#ff8a00"), Color.web("#b388ff"), Color.web("#ff4fd8"), Color.web("#8bc34a"),
            Color.web("#40c4ff"), Color.web("#ff6e6e"), Color.web("#c0ca33"), Color.web("#90a4ae")
    };

    //El lienzo (hoja en blanco) donde se dibuja la pista y los puntos de los pilotos en cada fotograma
    private final Canvas lienzo = new Canvas(640, 380);
    //Columna donde va la tabla "EN VIVO" con la posición y el estado de cada piloto
    private final VBox columnaClasificacionEnVivo = new VBox(8);
    //El circuito donde se corre esta carrera (se guarda para saber cuántas vueltas tiene, por ejemplo)
    private final Circuito circuito;
    //La lista de resultados finales (ya calculados por el simulador) de todos los pilotos, ordenada
    private final List<ResultadoCarrera> resultados;
    //El clima que realmente tocó en la carrera (puede venir de un sorteo si se eligió "Aleatorio")
    private final Clima climaReal;
    //Diccionario que relaciona cada piloto con el color de su punto en la pista
    private final Map<Piloto, Color> colores = new LinkedHashMap<>();
    //Diccionario que relaciona cada piloto con su factor de velocidad: qué tan rápido avanza su punto en la
    //animación en comparación con el líder (más cerca de 1.0 = casi tan rápido como el que va primero)
    private final Map<Piloto, Double> factorVelocidad = new LinkedHashMap<>();
    //Barra deslizante para acelerar o frenar qué tan rápido corre la animación (de 0.25x a 3.0x, empieza en 0.35x)
    private final Slider sliderVelocidadSimulacion = new Slider(0.25, 3.0, 0.35);
    //Texto que muestra el número de la velocidad actual del slider (por ejemplo "x0.35")
    private final Label etiquetaVelocidadSimulacion = new Label();
    //Diccionario que relaciona cada piloto con su ResultadoCarrera, para no tener que buscarlo cada vez
    private final Map<Piloto, ResultadoCarrera> resultadoPorPiloto = new LinkedHashMap<>();
    // Un Label estable por piloto, creado una sola vez: si se recrearan cada fotograma (60/s), un
    // clic real del usuario (más lento que eso) nunca llegaría a completarse sobre el mismo nodo
    // y el evento de clic jamás se disparaba. Cada fotograma solo se actualiza su texto/estilo y
    // se reordenan los mismos objetos, nunca se destruyen.
    private final Map<Piloto, Label> etiquetasEnVivo = new LinkedHashMap<>();
    // Estado de la pausa visual en boxes de cada piloto (parar unos segundos en la línea de salida).
    //Cuántos milisegundos de "atraso" acumulado tiene cada piloto por haber parado en boxes (para no saltar hacia adelante al retomar)
    private final Map<Piloto, Double> atrasoPitMs = new HashMap<>();
    //Si el piloto está en este momento parado en la pausa visual de boxes (true/false)
    private final Map<Piloto, Boolean> pausaPitActiva = new HashMap<>();
    //En qué milisegundo (del tiempo simulado) empezó la pausa en boxes de cada piloto
    private final Map<Piloto, Double> inicioPausaPitMs = new HashMap<>();
    //Qué parada en boxes le toca revisar a continuación a cada piloto (índice dentro de su lista de paradas)
    private final Map<Piloto, Integer> siguienteIndicePit = new HashMap<>();
    // Panel de detalle en vivo: se despliega al hacer clic en un piloto de la tabla "EN VIVO".
    private final VBox panelDetallePiloto = new VBox(6);
    // Fotos de choques ocurridos durante la carrera, pegadas al costado izquierdo, en el orden
    // en que van sucediendo. Se les guarda una tarjeta a los pilotos ya mostrados para no
    // duplicar la misma tarjeta en cada fotograma mientras siguen "chocados".
    private final VBox columnaChoques = new VBox(12);
    //Guarda qué pilotos ya tienen su tarjeta de choque mostrada, para no repetirla en cada fotograma
    private final Set<Piloto> fotosChoqueMostradas = new HashSet<>();
    //El resultado completo de la simulación (incluye los ResultadoCarrera y el clima real que salió)
    private final SimuladorCarrera.ResultadoSimulacion simulacion;
    //Guarda cuál piloto tiene seleccionado el usuario (al que le hizo clic en la tabla EN VIVO); null si no eligió a ninguno
    private Piloto pilotoSeleccionado;
    //El "reloj" de la animación (AnimationTimer) que va llamando a dibujarFrame en cada fotograma
    private AnimationTimer timer;
    //Guarda en qué nanosegundo ocurrió el fotograma anterior, para poder calcular cuánto tiempo pasó entre uno y otro (-1 significa "todavía no hay fotograma anterior")
    private long ultimoFrameNs = -1;
    //Cuántos milisegundos de "tiempo simulado" de carrera se han acumulado desde que arrancó la animación (ya multiplicados por la velocidad del slider)
    private double msSimuladosAcumulados = 0;

    //Constructor
    //Arma toda la pantalla de animación: primero corre la simulación completa de la carrera (para saber
    //quién gana), después prepara colores/velocidades/etiquetas por piloto, arma todos los paneles visuales
    //(lienzo, clasificación en vivo, choques, control de velocidad) y finalmente arranca el reloj de animación.
    public AnimacionCarreraPane(Circuito circuito, Clima climaElegido, Consumer<SimuladorCarrera.ResultadoSimulacion> alFinalizar) {
        this.circuito = circuito;

        //Genera el trazado visual (la forma de la pista) que le corresponde a este circuito
        PistaGenerador pista = PistaGenerador.paraCircuito(circuito);
        SimuladorCarrera simulador = new SimuladorCarrera();
        List<Piloto> pilotos = DataStore.getInstancia().getPilotos();
        //Corre toda la carrera "de una sola vez" (sin animación) para calcular el resultado final:
        //tiempos de cada piloto, choques, paradas en boxes, etc. La animación de abajo solo representa esto.
        SimuladorCarrera.ResultadoSimulacion simulacion = simulador.simular(circuito, climaElegido, pilotos,
                p -> DataStore.getInstancia().getVehiculoPorEquipo(p.getEquipo()), pista::esCurvaEnFraccion);
        this.simulacion = simulacion;
        this.resultados = simulacion.getResultados();
        this.climaReal = simulacion.getClimaReal();
        //Llena el diccionario piloto -> resultado para poder consultarlo rápido durante la animación
        for (ResultadoCarrera r : resultados) {
            resultadoPorPiloto.put(r.getPiloto(), r);
        }

        //El tiempo del que llegó primero (el líder) sirve de referencia para calcular qué tan rápido se ve cada uno de los demás
        double tiempoLider = resultados.get(0).getTiempoSegundos();
        int i = 0;
        for (ResultadoCarrera r : resultados) {
            Piloto p = r.getPiloto();
            //Le reparte un color de la paleta a cada piloto (si hay más pilotos que colores, se repiten con el módulo %)
            colores.put(p, PALETA[i % PALETA.length]);
            //El factor de velocidad es la razón entre el tiempo del líder y el tiempo de este piloto:
            //si tardó más que el líder, este número queda por debajo de 1 y su punto avanza más lento en la animación
            factorVelocidad.put(p, tiempoLider / r.getTiempoSegundos());
            i++;

            //Crea la etiqueta de este piloto UNA sola vez (ver el comentario del campo etiquetasEnVivo de arriba)
            Label etiqueta = new Label();
            etiqueta.setWrapText(true);
            etiqueta.setMinWidth(0);
            etiqueta.setPrefWidth(250);
            etiqueta.setMaxWidth(250);
            etiqueta.setCursor(javafx.scene.Cursor.HAND);
            //Al hacer clic en la etiqueta de un piloto, ese piloto queda "seleccionado" y se muestra su detalle abajo
            etiqueta.setOnMouseClicked(e -> pilotoSeleccionado = p);
            etiquetasEnVivo.put(p, etiqueta);
        }

        setPadding(new Insets(6));

        Label titulo = new Label("Carrera en curso — " + circuito.getNombre() + "  ·  Clima: " + climaReal.getEtiqueta());
        titulo.getStyleClass().add("titulo-seccion");
        VBox cajaTitulo = new VBox(titulo);
        cajaTitulo.setPadding(new Insets(0, 0, 8, 0));
        setTop(cajaTitulo);

        //El lienzo donde se dibuja la pista y los pilotos va en el centro de la pantalla
        StackPane contenedorLienzo = new StackPane(lienzo);
        contenedorLienzo.getStyleClass().add("panel");
        contenedorLienzo.setPadding(new Insets(12));
        setCenter(contenedorLienzo);

        //A la izquierda va la columna con las tarjetas de fotos de los choques, con su propio scroll
        ScrollPane scrollChoques = new ScrollPane(columnaChoques);
        scrollChoques.setFitToWidth(true);
        scrollChoques.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollChoques.getStyleClass().add("scroll-oscuro");
        scrollChoques.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollChoques.setPrefWidth(220);
        scrollChoques.setPrefHeight(404);
        setLeft(scrollChoques);

        Label tituloClasificacion = new Label("EN VIVO");
        tituloClasificacion.getStyleClass().add("texto-rojo");
        panelDetallePiloto.setMinHeight(70);
        panelDetallePiloto.setStyle("-fx-border-color: #232a3d; -fx-border-width: 1 0 0 0;");
        panelDetallePiloto.setPadding(new Insets(8, 0, 0, 0));
        //Al arrancar todavía no hay ningún piloto seleccionado, entonces se muestra el mensaje por defecto
        mostrarMensajeSinSeleccion();

        // La lista va en su propio scroll con altura acotada, para que el panel de detalle de
        // abajo SIEMPRE tenga su espacio reservado y visible (si no, con filas largas la lista
        // se comía todo el alto del panel y el detalle quedaba empujado fuera de la vista).
        columnaClasificacionEnVivo.setMaxWidth(250);
        ScrollPane scrollClasificacion = new ScrollPane(columnaClasificacionEnVivo);
        scrollClasificacion.setFitToWidth(true);
        scrollClasificacion.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollClasificacion.getStyleClass().add("scroll-oscuro");
        scrollClasificacion.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        // Sin una altura acotada explícita, un ScrollPane reporta la altura COMPLETA de su
        // contenido como su tamaño preferido (no recorta nada), así que el "scroll propio" de la
        // lista nunca llegaba a activarse: esa altura completa se sumaba a panelDerecho y de ahí
        // se propagaba hasta forzar el scroll de toda la pantalla. Con altura fija, ahora sí es
        // este ScrollPane el que recorta y hace scroll internamente cuando la lista no cabe.
        scrollClasificacion.setPrefHeight(240);
        scrollClasificacion.setMinHeight(100);
        VBox.setVgrow(scrollClasificacion, Priority.ALWAYS);

        //A la derecha va el panel con la clasificación en vivo arriba y el detalle del piloto seleccionado abajo
        VBox panelDerecho = new VBox(10, tituloClasificacion, scrollClasificacion, panelDetallePiloto);
        panelDerecho.getStyleClass().add("panel");
        panelDerecho.setPadding(new Insets(14));
        panelDerecho.setPrefWidth(300);
        panelDerecho.setMaxWidth(300);
        setRight(panelDerecho);
        BorderPane.setMargin(panelDerecho, new Insets(0, 0, 0, 12));

        //Abajo del todo va el control deslizante para cambiar la velocidad de la animación
        setBottom(construirControlVelocidad());

        //Ya con todo armado visualmente, se arranca el reloj (AnimationTimer) que va a ir dibujando fotograma por fotograma
        iniciarAnimacion(alFinalizar, simulacion, pista);
    }

    //Arma la cajita de abajo con el título, el slider y el número de velocidad de la simulación
    private HBox construirControlVelocidad() {
        Label titulo = new Label("VELOCIDAD DE SIMULACIÓN");
        titulo.getStyleClass().add("texto-rojo");
        titulo.setStyle("-fx-font-size: 11px;");

        sliderVelocidadSimulacion.setPrefWidth(180);
        sliderVelocidadSimulacion.setShowTickMarks(false);
        sliderVelocidadSimulacion.setMajorTickUnit(0.25);
        //Cada vez que el usuario mueve el slider, se actualiza el texto que muestra el número de velocidad actual
        sliderVelocidadSimulacion.valueProperty().addListener((obs, anterior, nuevo) -> actualizarEtiquetaVelocidad());
        actualizarEtiquetaVelocidad();

        HBox caja = new HBox(12, titulo, sliderVelocidadSimulacion, etiquetaVelocidadSimulacion);
        caja.getStyleClass().add("panel");
        caja.setAlignment(Pos.CENTER);
        caja.setPadding(new Insets(6, 14, 6, 14));
        caja.setMaxWidth(420);
        BorderPane.setMargin(caja, new Insets(8, 0, 4, 0));
        BorderPane.setAlignment(caja, Pos.CENTER);
        return caja;
    }

    //Actualiza el texto que muestra la velocidad actual del slider, con formato "x0.35" (dos decimales)
    private void actualizarEtiquetaVelocidad() {
        etiquetaVelocidadSimulacion.setText(String.format("x%.2f", sliderVelocidadSimulacion.getValue()));
    }

    //Crea y arranca el AnimationTimer: este es el "motor" de toda la animación. JavaFX lo llama automáticamente
    //en cada fotograma (unas 60 veces por segundo) pasándole "now" (el instante actual en nanosegundos).
    private void iniciarAnimacion(Consumer<SimuladorCarrera.ResultadoSimulacion> alFinalizar,
                                   SimuladorCarrera.ResultadoSimulacion simulacion, PistaGenerador pista) {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                //En el primerísimo fotograma todavía no hay un "fotograma anterior" con el que comparar, entonces se usa el actual como punto de partida
                if (ultimoFrameNs < 0) {
                    ultimoFrameNs = now;
                }
                //Calcula cuánto tiempo real pasó desde el fotograma anterior (now viene en nanosegundos, se divide entre 1 millón para pasarlo a milisegundos)
                double deltaMs = (now - ultimoFrameNs) / 1_000_000.0;
                ultimoFrameNs = now;
                //Al tiempo real transcurrido se le aplica la velocidad elegida en el slider, y se va acumulando el "tiempo simulado" total de la carrera
                msSimuladosAcumulados += deltaMs * sliderVelocidadSimulacion.getValue();

                //Qué fracción de la animación completa ya se recorrió (0.0 = recién arranca, 1.0 = ya terminó). Nunca pasa de 1.0 gracias al Math.min
                double fraccionTiempo = Math.min(1.0, msSimuladosAcumulados / DURACION_ANIMACION_MS);

                //Con esa fracción se dibuja el fotograma actual: dónde va cada piloto en la pista en este instante
                dibujarFrame(pista, fraccionTiempo, (long) msSimuladosAcumulados);

                //Cuando la fracción llega a 1.0 la animación ya terminó: se detiene el reloj y se avisa hacia afuera con el resultado final
                if (fraccionTiempo >= 1.0) {
                    stop();
                    alFinalizar.accept(simulacion);
                }
            }
        };
        timer.start();
    }

    //Dibuja UN fotograma completo de la animación: limpia el lienzo, redibuja la pista, calcula el avance
    //de cada piloto en este instante, los dibuja como puntos de color sobre la pista y actualiza la tabla
    //"EN VIVO" y el panel de detalle con esos mismos datos.
    private void dibujarFrame(PistaGenerador pista, double fraccionTiempo, long transcurridoMs) {
        GraphicsContext gc = lienzo.getGraphicsContext2D();
        double ancho = lienzo.getWidth();
        double alto = lienzo.getHeight();
        //Borra todo lo que se dibujó en el fotograma anterior antes de dibujar el nuevo (si no, se iría dibujando uno encima del otro)
        gc.clearRect(0, 0, ancho, alto);
        pista.dibujar(gc, 0, 0, ancho, alto, true);

        // Calcular avance de cada piloto y ordenarlos para la clasificación en vivo.
        // A un piloto que choca se le congela el avance en el punto exacto del choque, y a uno
        // que entra a boxes se le detiene unos segundos en la línea de salida (ver calcularVueltasConPit).
        List<Piloto> ordenActual = new ArrayList<>(colores.keySet());
        //Cuántas vueltas lleva avanzadas cada piloto en este instante (puede ser un número con decimales, por ejemplo 3.42 vueltas)
        Map<Piloto, Double> vueltasAvanzadas = new HashMap<>();
        //Qué pilotos están chocados (DNF) en este fotograma
        Set<Piloto> chocados = new HashSet<>();
        //Qué pilotos están en este momento parados visualmente en boxes
        Set<Piloto> enBoxes = new HashSet<>();
        for (Piloto p : ordenActual) {
            //Calcula cuántas vueltas lleva este piloto, teniendo en cuenta si le toca alguna pausa en boxes
            double vueltasNotables = calcularVueltasConPit(p, transcurridoMs, enBoxes);
            ResultadoCarrera resultado = resultadoPorPiloto.get(p);
            //Si el piloto está destinado a chocar (isDnf) y ya alcanzó el punto exacto del choque, se queda "congelado" ahí para siempre
            if (resultado.isDnf() && vueltasNotables >= resultado.getProgresoChoque()) {
                vueltasAvanzadas.put(p, resultado.getProgresoChoque());
                chocados.add(p);
            } else {
                vueltasAvanzadas.put(p, vueltasNotables);
            }
        }
        //Ordena a los pilotos de mayor a menor avance (el que más vueltas lleva va primero, como en una carrera real)
        ordenActual.sort((a, b) -> Double.compare(vueltasAvanzadas.get(b), vueltasAvanzadas.get(a)));
        //Revisa si algún piloto acaba de chocar en este fotograma para mostrarle su tarjeta de fotos
        mostrarNuevasFotosChoque(chocados);

        // Dibujar cada piloto como un punto de color sobre la pista (gris con X si chocó)
        for (Piloto p : ordenActual) {
            double vueltas = vueltasAvanzadas.get(p);
            //El resto de dividir entre 1.0 (%1.0) deja solo la parte decimal: en qué punto de LA VUELTA ACTUAL va (0.0 = línea de salida, 0.99 = a punto de completarla)
            double fraccionVuelta = vueltas % 1.0;
            //Le pregunta a PistaGenerador en qué pixel exacto de la pantalla cae ese punto de la vuelta
            var punto = pista.posicionEnFraccion(fraccionVuelta, 0, 0, ancho, alto);

            if (chocados.contains(p)) {
                //Piloto chocado: se dibuja un círculo gris...
                gc.setFill(Color.web("#6b7280"));
                gc.fillOval(punto.getX() - 6, punto.getY() - 6, 12, 12);
                gc.setStroke(Color.web("#05070d"));
                gc.setLineWidth(1.5);
                gc.strokeOval(punto.getX() - 6, punto.getY() - 6, 12, 12);

                //...con una X roja encima (dos líneas cruzadas) para que se note claramente que ya no sigue en carrera
                gc.setStroke(Color.web("#e10600"));
                gc.setLineWidth(2);
                gc.strokeLine(punto.getX() - 7, punto.getY() - 7, punto.getX() + 7, punto.getY() + 7);
                gc.strokeLine(punto.getX() - 7, punto.getY() + 7, punto.getX() + 7, punto.getY() - 7);
            } else {
                //Piloto en carrera: círculo relleno con su color asignado y un borde oscuro para que resalte sobre la pista
                gc.setFill(colores.get(p));
                gc.fillOval(punto.getX() - 6, punto.getY() - 6, 12, 12);
                gc.setStroke(Color.web("#05070d"));
                gc.setLineWidth(1.5);
                gc.strokeOval(punto.getX() - 6, punto.getY() - 6, 12, 12);
            }
        }

        //Con el orden y el avance de este fotograma ya calculados, se refresca la tabla "EN VIVO" y el panel de detalle
        actualizarClasificacionEnVivo(ordenActual, vueltasAvanzadas, chocados, enBoxes, transcurridoMs);
        actualizarDetallePiloto(vueltasAvanzadas);
    }

    //Progreso (en vueltas) de un piloto, incluyendo la parada visual en boxes: al llegar a la
    //línea de salida de una vuelta en la que le tocaba parar, se congela ahí unos segundos
    //(PAUSA_PIT_MS) antes de retomar el ritmo normal sin saltos hacia adelante.
    private double calcularVueltasConPit(Piloto p, double msActual, Set<Piloto> enBoxes) {
        //Lista de vueltas en las que este piloto para en boxes según la simulación (por ejemplo [12, 30])
        List<Integer> paradas = resultadoPorPiloto.get(p).getParadasEnBoxes();
        //Qué parada le toca revisar ahora (empieza en 0 si todavía no la tenía guardada)
        int indice = siguienteIndicePit.getOrDefault(p, 0);
        //Si ahora mismo está en plena pausa visual de boxes
        boolean enPausa = pausaPitActiva.getOrDefault(p, false);

        if (enPausa) {
            double inicio = inicioPausaPitMs.get(p);
            //La vuelta exacta en la que se quedó parado (la última parada que ya empezó)
            int vueltaPit = paradas.get(indice - 1);
            //Si todavía no pasaron los PAUSA_PIT_MS milisegundos desde que empezó a parar, se queda congelado ahí mismo
            if (msActual - inicio < PAUSA_PIT_MS) {
                enBoxes.add(p);
                return vueltaPit;
            }
            // Termina la pausa: se calcula el atraso justo para retomar desde este punto sin saltos.
            double vueltasTotales = circuito.getVueltas();
            double factor = factorVelocidad.get(p);
            //Calcula cuántos milisegundos "le tomaría" normalmente llegar hasta la vuelta del pit, para poder
            //restarle ese atraso al reloj y que el piloto retome exactamente desde ahí (sin teletransportarse)
            double msNecesarios = (vueltaPit / (vueltasTotales * factor)) * DURACION_ANIMACION_MS;
            atrasoPitMs.put(p, msActual - msNecesarios);
            pausaPitActiva.put(p, false);
            enPausa = false;
        }

        //Cuánto atraso acumulado tiene este piloto por sus paradas anteriores (0 si nunca ha parado)
        double atraso = atrasoPitMs.getOrDefault(p, 0.0);
        //Fórmula del avance "normal" (sin contar la pausa que pueda venir): tiempo transcurrido (ya descontado el atraso) dividido
        //entre la duración total de la animación, multiplicado por las vueltas totales del circuito y por el factor de velocidad del piloto
        double vueltasCrudo = ((msActual - atraso) / DURACION_ANIMACION_MS) * circuito.getVueltas() * factorVelocidad.get(p);

        //Si el avance ya alcanzó la vuelta en la que le tocaba parar (y todavía no había arrancado esa pausa), se arranca la pausa ahora
        if (!enPausa && indice < paradas.size() && vueltasCrudo >= paradas.get(indice)) {
            pausaPitActiva.put(p, true);
            inicioPausaPitMs.put(p, msActual);
            siguienteIndicePit.put(p, indice + 1);
            enBoxes.add(p);
            return paradas.get(indice);
        }

        return vueltasCrudo;
    }

    //Redibuja el texto de cada etiqueta de la tabla "EN VIVO": posición, nombre, equipo y estado (vuelta actual,
    //DNF o EN BOX). Reutiliza siempre las mismas etiquetas (etiquetasEnVivo) y solo cambia su orden en la columna.
    private void actualizarClasificacionEnVivo(List<Piloto> orden, Map<Piloto, Double> vueltasAvanzadas,
                                                Set<Piloto> chocados, Set<Piloto> enBoxes, long transcurridoMs) {
        double segundosTranscurridos = transcurridoMs / 1000.0;
        String tiempo = ResultadoCarrera.formatearTiempo(segundosTranscurridos);

        List<Label> enOrden = new ArrayList<>(orden.size());
        int posicion = 1;
        for (Piloto p : orden) {
            boolean choco = chocados.contains(p);
            boolean enBox = enBoxes.contains(p);
            String estado;
            String colorTexto;
            if (choco) {
                //Piloto chocado: texto gris con "DNF (choque)"
                estado = "DNF (choque)";
                colorTexto = "#6b7280";
            } else if (enBox) {
                //Piloto parado en boxes: texto amarillo con "EN BOX"
                estado = "EN BOX";
                colorTexto = "#ffd400";
            } else {
                //Piloto en carrera normal: muestra la vuelta actual, el tiempo transcurrido y el desgaste de llantas de esa vuelta
                estado = "Vuelta " + Math.min(circuito.getVueltas(), (int) Math.floor(vueltasAvanzadas.get(p)) + 1)
                        + "/" + circuito.getVueltas() + "   ·   " + tiempo
                        + "   ·   Llantas " + (int) Math.round(valorPorVueltaActual(
                                resultadoPorPiloto.get(p).getDesgastePorVuelta(), vueltasAvanzadas.get(p))) + "/100";
                //Reutiliza el mismo color asignado al piloto en el lienzo, para que la etiqueta combine con su punto en la pista
                colorTexto = toHex(colores.get(p));
            }
            //Si este es el piloto que el usuario tiene seleccionado, se le agrega una flechita "▶" delante
            String prefijo = p.equals(pilotoSeleccionado) ? "▶ " : "";
            Label etiqueta = etiquetasEnVivo.get(p);
            etiqueta.setText(prefijo + posicion + "  " + p.getNombre() + "   ·   " + p.getEquipo() + "   ·   " + estado);
            etiqueta.setStyle("-fx-text-fill: " + colorTexto + "; -fx-font-size: 11px; -fx-font-weight: bold;");
            enOrden.add(etiqueta);
            posicion++;
        }
        // Se reordenan los MISMOS objetos Label (nunca se destruyen ni se crean nuevos), para que
        // sus manejadores de clic sigan siendo válidos entre un fotograma y el siguiente.
        columnaClasificacionEnVivo.getChildren().setAll(enOrden);
    }

    //Agrega una tarjeta con las 3 fotos del choque por cada piloto recién chocado (una sola vez por incidente)
    private void mostrarNuevasFotosChoque(Set<Piloto> chocados) {
        for (Piloto p : chocados) {
            //Si a este piloto ya se le mostró su tarjeta de choque, no se repite en este fotograma
            if (fotosChoqueMostradas.contains(p)) {
                continue;
            }
            ResultadoCarrera resultado = resultadoPorPiloto.get(p);
            fotosChoqueMostradas.add(p);

            String titulo;
            if (resultado.esChoqueGrupal()) {
                //Si el choque fue entre dos pilotos, se marca también al rival como "ya mostrado" para no duplicar la tarjeta cuando le toque su turno
                fotosChoqueMostradas.add(resultado.getRivalChoque());
                titulo = "Choque entre " + p.getNombre() + " y " + resultado.getRivalChoque().getNombre();
            } else {
                titulo = "Choque de " + p.getNombre();
            }

            //Pide las fotos que le corresponden a este choque puntual y arma la tarjeta con ellas
            List<Image> fotos = FotosChoque.paraChoque(simulacion, resultado);
            columnaChoques.getChildren().add(construirTarjetaChoque(titulo, fotos));
        }
    }

    //Arma la tarjeta visual de un choque: un título arriba y debajo las fotos correspondientes
    private VBox construirTarjetaChoque(String titulo, List<Image> fotos) {
        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("texto-rojo");
        etiqueta.setWrapText(true);
        etiqueta.setStyle("-fx-font-size: 11px;");

        VBox tarjeta = new VBox(6, etiqueta);
        tarjeta.getStyleClass().add("panel");
        tarjeta.setPadding(new Insets(10));
        //Agrega una ImageView por cada foto de la lista, todas con el mismo ancho fijo
        for (Image foto : fotos) {
            ImageView vista = new ImageView(foto);
            vista.setPreserveRatio(true);
            vista.setFitWidth(220);
            tarjeta.getChildren().add(vista);
        }
        return tarjeta;
    }

    //Mensaje por defecto del panel de detalle cuando todavía no se ha seleccionado ningún piloto
    private void mostrarMensajeSinSeleccion() {
        panelDetallePiloto.getChildren().clear();
        Label mensaje = new Label("Toca un piloto de la lista\npara ver su detalle en vivo.");
        mensaje.getStyleClass().add("texto-secundario");
        mensaje.setWrapText(true);
        mensaje.setStyle("-fx-font-size: 11px;");
        panelDetallePiloto.getChildren().add(mensaje);
    }

    //Refresca el panel de detalle (desgaste, temperatura de llantas y de motor) del piloto seleccionado, según la vuelta en la que va ahora mismo
    private void actualizarDetallePiloto(Map<Piloto, Double> vueltasAvanzadas) {
        //Si no hay ningún piloto seleccionado, se muestra el mensaje por defecto en vez del detalle
        if (pilotoSeleccionado == null) {
            mostrarMensajeSinSeleccion();
            return;
        }
        ResultadoCarrera resultado = resultadoPorPiloto.get(pilotoSeleccionado);
        double vueltas = vueltasAvanzadas.getOrDefault(pilotoSeleccionado, 0.0);

        Label titulo = new Label(pilotoSeleccionado.getNombre());
        titulo.getStyleClass().add("texto-rojo");
        titulo.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        //Cada valor se busca en la lista "por vuelta" correspondiente, usando la vuelta actual del piloto
        Label desgaste = new Label(String.format("Desgaste de las llantas: %.0f/100",
                valorPorVueltaActual(resultado.getDesgastePorVuelta(), vueltas)));
        Label tempLlantas = new Label(String.format("Temperatura de llantas: %.0f °C",
                valorPorVueltaActual(resultado.getTemperaturaLlantasPorVuelta(), vueltas)));
        Label tempMotor = new Label(String.format("Temperatura de motor: %.0f °C",
                valorPorVueltaActual(resultado.getTemperaturaMotorPorVuelta(), vueltas)));
        for (Label l : List.of(desgaste, tempLlantas, tempMotor)) {
            l.getStyleClass().add("texto-normal");
            l.setStyle("-fx-font-size: 11px;");
            l.setWrapText(true);
        }

        VBox caja = new VBox(4, titulo, desgaste, tempLlantas, tempMotor);
        panelDetallePiloto.getChildren().setAll(caja);
    }

    //Valor de una lista por-vuelta (desgaste o temperatura) correspondiente a la vuelta actual del piloto
    private double valorPorVueltaActual(List<Double> valoresPorVuelta, double vueltasAvanzadas) {
        //Si la lista viene vacía (por ejemplo el piloto ni siquiera arrancó a rodar), no hay nada que mostrar
        if (valoresPorVuelta.isEmpty()) {
            return 0;
        }
        //Convierte las vueltas avanzadas (con decimales) en un índice entero válido de la lista, sin pasarse de sus límites
        int indice = Math.max(0, Math.min(valoresPorVuelta.size() - 1, (int) Math.floor(vueltasAvanzadas)));
        return valoresPorVuelta.get(indice);
    }

    //Convierte un color de JavaFX (Color) a su texto hexadecimal (por ejemplo "#FF2B2B") para poder usarlo en un -fx-text-fill
    private String toHex(Color c) {
        return String.format("#%02X%02X%02X", (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
    }
}
