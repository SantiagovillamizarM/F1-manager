package com.f1manager.infraestructura.ui.screens.carrera;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.modelo.*;
import com.f1manager.dominio.servicio.SimuladorCarrera;
import com.f1manager.infraestructura.ui.util.FotosChoque;
import com.f1manager.infraestructura.ui.util.PistaGenerador;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.function.Consumer;

/**
 * Anima la carrera sobre el trazado del circuito seleccionado. Los
 * resultados finales se calculan de antemano mediante el
 * {@link SimuladorCarrera} (para que el ganador quede determinado por la
 * lógica de simulación) y la animación representa visualmente ese mismo
 * resultado: cada piloto avanza a una velocidad proporcional a su tiempo
 * final, por lo que el orden de llegada de la animación coincide con la
 * clasificación real.
 */
public class AnimacionCarreraPane extends BorderPane {

    private static final long DURACION_ANIMACION_MS = 24000;
    // Cuánto dura, en el tiempo simulado de la animación, la pausa visual de un piloto en boxes.
    private static final double PAUSA_PIT_MS = 2200;
    private static final Color[] PALETA = {
            Color.web("#ff2b2b"), Color.web("#00d4ff"), Color.web("#ffd400"), Color.web("#39ff88"),
            Color.web("#ff8a00"), Color.web("#b388ff"), Color.web("#ff4fd8"), Color.web("#8bc34a"),
            Color.web("#40c4ff"), Color.web("#ff6e6e"), Color.web("#c0ca33"), Color.web("#90a4ae")
    };

    private final Canvas lienzo = new Canvas(640, 380);
    private final VBox columnaClasificacionEnVivo = new VBox(8);
    private final Circuito circuito;
    private final List<ResultadoCarrera> resultados;
    private final Clima climaReal;
    private final Map<Piloto, Color> colores = new LinkedHashMap<>();
    private final Map<Piloto, Double> factorVelocidad = new LinkedHashMap<>();
    private final Slider sliderVelocidadSimulacion = new Slider(0.25, 3.0, 0.5);
    private final Label etiquetaVelocidadSimulacion = new Label();
    private final Map<Piloto, ResultadoCarrera> resultadoPorPiloto = new LinkedHashMap<>();
    // Un Label estable por piloto, creado una sola vez: si se recrearan cada fotograma (60/s), un
    // clic real del usuario (más lento que eso) nunca llegaría a completarse sobre el mismo nodo
    // y el evento de clic jamás se disparaba. Cada fotograma solo se actualiza su texto/estilo y
    // se reordenan los mismos objetos, nunca se destruyen.
    private final Map<Piloto, Label> etiquetasEnVivo = new LinkedHashMap<>();
    // Estado de la pausa visual en boxes de cada piloto (parar unos segundos en la línea de salida).
    private final Map<Piloto, Double> atrasoPitMs = new HashMap<>();
    private final Map<Piloto, Boolean> pausaPitActiva = new HashMap<>();
    private final Map<Piloto, Double> inicioPausaPitMs = new HashMap<>();
    private final Map<Piloto, Integer> siguienteIndicePit = new HashMap<>();
    // Panel de detalle en vivo: se despliega al hacer clic en un piloto de la tabla "EN VIVO".
    private final VBox panelDetallePiloto = new VBox(6);
    // Fotos de choques ocurridos durante la carrera, pegadas al costado izquierdo, en el orden
    // en que van sucediendo. Se les guarda una tarjeta a los pilotos ya mostrados para no
    // duplicar la misma tarjeta en cada fotograma mientras siguen "chocados".
    private final VBox columnaChoques = new VBox(12);
    private final Set<Piloto> fotosChoqueMostradas = new HashSet<>();
    private final SimuladorCarrera.ResultadoSimulacion simulacion;
    private Piloto pilotoSeleccionado;
    private AnimationTimer timer;
    private long ultimoFrameNs = -1;
    private double msSimuladosAcumulados = 0;

    public AnimacionCarreraPane(Circuito circuito, Clima climaElegido, Consumer<SimuladorCarrera.ResultadoSimulacion> alFinalizar) {
        this.circuito = circuito;

        PistaGenerador pista = PistaGenerador.paraCircuito(circuito);
        SimuladorCarrera simulador = new SimuladorCarrera();
        List<Piloto> pilotos = DataStore.getInstancia().getPilotos();
        SimuladorCarrera.ResultadoSimulacion simulacion = simulador.simular(circuito, climaElegido, pilotos,
                p -> DataStore.getInstancia().getVehiculoPorEquipo(p.getEquipo()), pista::esCurvaEnFraccion);
        this.simulacion = simulacion;
        this.resultados = simulacion.getResultados();
        this.climaReal = simulacion.getClimaReal();
        for (ResultadoCarrera r : resultados) {
            resultadoPorPiloto.put(r.getPiloto(), r);
        }

        double tiempoLider = resultados.get(0).getTiempoSegundos();
        int i = 0;
        for (ResultadoCarrera r : resultados) {
            Piloto p = r.getPiloto();
            colores.put(p, PALETA[i % PALETA.length]);
            factorVelocidad.put(p, tiempoLider / r.getTiempoSegundos());
            i++;

            Label etiqueta = new Label();
            etiqueta.setWrapText(true);
            etiqueta.setMinWidth(0);
            etiqueta.setPrefWidth(250);
            etiqueta.setMaxWidth(250);
            etiqueta.setCursor(javafx.scene.Cursor.HAND);
            etiqueta.setOnMouseClicked(e -> pilotoSeleccionado = p);
            etiquetasEnVivo.put(p, etiqueta);
        }

        setPadding(new Insets(6));

        Label titulo = new Label("Carrera en curso — " + circuito.getNombre() + "  ·  Clima: " + climaReal.getEtiqueta());
        titulo.getStyleClass().add("titulo-seccion");
        VBox cajaTitulo = new VBox(titulo);
        cajaTitulo.setPadding(new Insets(0, 0, 8, 0));
        setTop(cajaTitulo);

        StackPane contenedorLienzo = new StackPane(lienzo);
        contenedorLienzo.getStyleClass().add("panel");
        contenedorLienzo.setPadding(new Insets(12));
        setCenter(contenedorLienzo);

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

        VBox panelDerecho = new VBox(10, tituloClasificacion, scrollClasificacion, panelDetallePiloto);
        panelDerecho.getStyleClass().add("panel");
        panelDerecho.setPadding(new Insets(14));
        panelDerecho.setPrefWidth(300);
        panelDerecho.setMaxWidth(300);
        setRight(panelDerecho);
        BorderPane.setMargin(panelDerecho, new Insets(0, 0, 0, 12));

        setBottom(construirControlVelocidad());

        iniciarAnimacion(alFinalizar, simulacion, pista);
    }

    private HBox construirControlVelocidad() {
        Label titulo = new Label("VELOCIDAD DE SIMULACIÓN");
        titulo.getStyleClass().add("texto-rojo");
        titulo.setStyle("-fx-font-size: 11px;");

        sliderVelocidadSimulacion.setPrefWidth(180);
        sliderVelocidadSimulacion.setShowTickMarks(false);
        sliderVelocidadSimulacion.setMajorTickUnit(0.25);
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

    private void actualizarEtiquetaVelocidad() {
        etiquetaVelocidadSimulacion.setText(String.format("x%.2f", sliderVelocidadSimulacion.getValue()));
    }

    private void iniciarAnimacion(Consumer<SimuladorCarrera.ResultadoSimulacion> alFinalizar,
                                   SimuladorCarrera.ResultadoSimulacion simulacion, PistaGenerador pista) {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (ultimoFrameNs < 0) {
                    ultimoFrameNs = now;
                }
                double deltaMs = (now - ultimoFrameNs) / 1_000_000.0;
                ultimoFrameNs = now;
                msSimuladosAcumulados += deltaMs * sliderVelocidadSimulacion.getValue();

                double fraccionTiempo = Math.min(1.0, msSimuladosAcumulados / DURACION_ANIMACION_MS);

                dibujarFrame(pista, fraccionTiempo, (long) msSimuladosAcumulados);

                if (fraccionTiempo >= 1.0) {
                    stop();
                    alFinalizar.accept(simulacion);
                }
            }
        };
        timer.start();
    }

    private void dibujarFrame(PistaGenerador pista, double fraccionTiempo, long transcurridoMs) {
        GraphicsContext gc = lienzo.getGraphicsContext2D();
        double ancho = lienzo.getWidth();
        double alto = lienzo.getHeight();
        gc.clearRect(0, 0, ancho, alto);
        pista.dibujar(gc, 0, 0, ancho, alto, true);

        // Calcular avance de cada piloto y ordenarlos para la clasificación en vivo.
        // A un piloto que choca se le congela el avance en el punto exacto del choque, y a uno
        // que entra a boxes se le detiene unos segundos en la línea de salida (ver calcularVueltasConPit).
        List<Piloto> ordenActual = new ArrayList<>(colores.keySet());
        Map<Piloto, Double> vueltasAvanzadas = new HashMap<>();
        Set<Piloto> chocados = new HashSet<>();
        Set<Piloto> enBoxes = new HashSet<>();
        for (Piloto p : ordenActual) {
            double vueltasNotables = calcularVueltasConPit(p, transcurridoMs, enBoxes);
            ResultadoCarrera resultado = resultadoPorPiloto.get(p);
            if (resultado.isDnf() && vueltasNotables >= resultado.getProgresoChoque()) {
                vueltasAvanzadas.put(p, resultado.getProgresoChoque());
                chocados.add(p);
            } else {
                vueltasAvanzadas.put(p, vueltasNotables);
            }
        }
        ordenActual.sort((a, b) -> Double.compare(vueltasAvanzadas.get(b), vueltasAvanzadas.get(a)));
        mostrarNuevasFotosChoque(chocados);

        // Dibujar cada piloto como un punto de color sobre la pista (gris con X si chocó)
        for (Piloto p : ordenActual) {
            double vueltas = vueltasAvanzadas.get(p);
            double fraccionVuelta = vueltas % 1.0;
            var punto = pista.posicionEnFraccion(fraccionVuelta, 0, 0, ancho, alto);

            if (chocados.contains(p)) {
                gc.setFill(Color.web("#6b7280"));
                gc.fillOval(punto.getX() - 6, punto.getY() - 6, 12, 12);
                gc.setStroke(Color.web("#05070d"));
                gc.setLineWidth(1.5);
                gc.strokeOval(punto.getX() - 6, punto.getY() - 6, 12, 12);

                gc.setStroke(Color.web("#e10600"));
                gc.setLineWidth(2);
                gc.strokeLine(punto.getX() - 7, punto.getY() - 7, punto.getX() + 7, punto.getY() + 7);
                gc.strokeLine(punto.getX() - 7, punto.getY() + 7, punto.getX() + 7, punto.getY() - 7);
            } else {
                gc.setFill(colores.get(p));
                gc.fillOval(punto.getX() - 6, punto.getY() - 6, 12, 12);
                gc.setStroke(Color.web("#05070d"));
                gc.setLineWidth(1.5);
                gc.strokeOval(punto.getX() - 6, punto.getY() - 6, 12, 12);
            }
        }

        actualizarClasificacionEnVivo(ordenActual, vueltasAvanzadas, chocados, enBoxes, transcurridoMs);
        actualizarDetallePiloto(vueltasAvanzadas);
    }

    /**
     * Progreso (en vueltas) de un piloto, incluyendo la parada visual en boxes: al llegar a la
     * línea de salida de una vuelta en la que le tocaba parar, se congela ahí unos segundos
     * (PAUSA_PIT_MS) antes de retomar el ritmo normal sin saltos hacia adelante.
     */
    private double calcularVueltasConPit(Piloto p, double msActual, Set<Piloto> enBoxes) {
        List<Integer> paradas = resultadoPorPiloto.get(p).getParadasEnBoxes();
        int indice = siguienteIndicePit.getOrDefault(p, 0);
        boolean enPausa = pausaPitActiva.getOrDefault(p, false);

        if (enPausa) {
            double inicio = inicioPausaPitMs.get(p);
            int vueltaPit = paradas.get(indice - 1);
            if (msActual - inicio < PAUSA_PIT_MS) {
                enBoxes.add(p);
                return vueltaPit;
            }
            // Termina la pausa: se calcula el atraso justo para retomar desde este punto sin saltos.
            double vueltasTotales = circuito.getVueltas();
            double factor = factorVelocidad.get(p);
            double msNecesarios = (vueltaPit / (vueltasTotales * factor)) * DURACION_ANIMACION_MS;
            atrasoPitMs.put(p, msActual - msNecesarios);
            pausaPitActiva.put(p, false);
            enPausa = false;
        }

        double atraso = atrasoPitMs.getOrDefault(p, 0.0);
        double vueltasCrudo = ((msActual - atraso) / DURACION_ANIMACION_MS) * circuito.getVueltas() * factorVelocidad.get(p);

        if (!enPausa && indice < paradas.size() && vueltasCrudo >= paradas.get(indice)) {
            pausaPitActiva.put(p, true);
            inicioPausaPitMs.put(p, msActual);
            siguienteIndicePit.put(p, indice + 1);
            enBoxes.add(p);
            return paradas.get(indice);
        }

        return vueltasCrudo;
    }

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
                estado = "DNF (choque)";
                colorTexto = "#6b7280";
            } else if (enBox) {
                estado = "EN BOX";
                colorTexto = "#ffd400";
            } else {
                estado = "Vuelta " + Math.min(circuito.getVueltas(), (int) Math.floor(vueltasAvanzadas.get(p)) + 1)
                        + "/" + circuito.getVueltas() + "   ·   " + tiempo
                        + "   ·   Llantas " + (int) Math.round(valorPorVueltaActual(
                                resultadoPorPiloto.get(p).getDesgastePorVuelta(), vueltasAvanzadas.get(p))) + "/100";
                colorTexto = toHex(colores.get(p));
            }
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

    /** Agrega una tarjeta con las 3 fotos del choque por cada piloto recién chocado (una sola vez por incidente). */
    private void mostrarNuevasFotosChoque(Set<Piloto> chocados) {
        for (Piloto p : chocados) {
            if (fotosChoqueMostradas.contains(p)) {
                continue;
            }
            ResultadoCarrera resultado = resultadoPorPiloto.get(p);
            fotosChoqueMostradas.add(p);

            String titulo;
            if (resultado.esChoqueGrupal()) {
                fotosChoqueMostradas.add(resultado.getRivalChoque());
                titulo = "Choque entre " + p.getNombre() + " y " + resultado.getRivalChoque().getNombre();
            } else {
                titulo = "Choque de " + p.getNombre();
            }

            List<Image> fotos = FotosChoque.paraChoque(simulacion, resultado);
            columnaChoques.getChildren().add(construirTarjetaChoque(titulo, fotos));
        }
    }

    private VBox construirTarjetaChoque(String titulo, List<Image> fotos) {
        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("texto-rojo");
        etiqueta.setWrapText(true);
        etiqueta.setStyle("-fx-font-size: 11px;");

        VBox tarjeta = new VBox(6, etiqueta);
        tarjeta.getStyleClass().add("panel");
        tarjeta.setPadding(new Insets(10));
        for (Image foto : fotos) {
            ImageView vista = new ImageView(foto);
            vista.setPreserveRatio(true);
            vista.setFitWidth(220);
            tarjeta.getChildren().add(vista);
        }
        return tarjeta;
    }

    private void mostrarMensajeSinSeleccion() {
        panelDetallePiloto.getChildren().clear();
        Label mensaje = new Label("Toca un piloto de la lista\npara ver su detalle en vivo.");
        mensaje.getStyleClass().add("texto-secundario");
        mensaje.setWrapText(true);
        mensaje.setStyle("-fx-font-size: 11px;");
        panelDetallePiloto.getChildren().add(mensaje);
    }

    /** Refresca el panel de detalle (desgaste, temperatura de llantas y de motor) del piloto seleccionado. */
    private void actualizarDetallePiloto(Map<Piloto, Double> vueltasAvanzadas) {
        if (pilotoSeleccionado == null) {
            mostrarMensajeSinSeleccion();
            return;
        }
        ResultadoCarrera resultado = resultadoPorPiloto.get(pilotoSeleccionado);
        double vueltas = vueltasAvanzadas.getOrDefault(pilotoSeleccionado, 0.0);

        Label titulo = new Label(pilotoSeleccionado.getNombre());
        titulo.getStyleClass().add("texto-rojo");
        titulo.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

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

    /** Valor de una lista por-vuelta (desgaste o temperatura) correspondiente a la vuelta actual. */
    private double valorPorVueltaActual(List<Double> valoresPorVuelta, double vueltasAvanzadas) {
        if (valoresPorVuelta.isEmpty()) {
            return 0;
        }
        int indice = Math.max(0, Math.min(valoresPorVuelta.size() - 1, (int) Math.floor(vueltasAvanzadas)));
        return valoresPorVuelta.get(indice);
    }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X", (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
    }
}
