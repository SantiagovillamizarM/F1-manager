//Dibuja (o carga por URL) todos los íconos que
// se usan en la app (casco, bandera, lupa, auto, escudo, etc.).
package com.f1manager.infraestructura.ui.util;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import com.f1manager.dominio.modelo.Piloto;
import javafx.scene.shape.*;
import javafx.scene.image.ImageView;
import java.util.Map;
public final class IconFactory {

    public static final Color ROJO = Color.web("#e10600");
    public static final Color ROJO_BRILLANTE = Color.web("#ff2b2b");
    public static final Color BLANCO = Color.web("#f5f6fa");
    public static final Color GRIS = Color.web("#464d5e");

    /** Asocia una palabra clave del nombre del equipo (en minúsculas) con su logo local en /imagenes/scuderias. */
    private static final Map<String, String> ARCHIVOS_LOGO_SCUDERIA = Map.of(
            "ferrari", "scuderia ferrari.png",
            "mercedes", "scuderia mercedes.png",
            "red bull", "scuderia redbull.png",
            "mclaren", "scuderia mclaren.png",
            "aston martin", "scuderia aston martin.png",
            "alpine", "scuderia alpine.png"
    );

    private IconFactory() {
    }

    /**
     * Logo real de la escudería según su nombre (ej. "Scuderia Ferrari" -> logo de Ferrari),
     * cargado desde /imagenes/scuderias. Si el equipo no coincide con ninguna escudería
     * conocida (por ejemplo, uno registrado manualmente por el usuario), se usa el
     * escudo genérico como respaldo.
     */
    public static Group logoEquipo(String nombreEquipo, Color colorReserva) {
        String normalizado = nombreEquipo == null ? "" : nombreEquipo.toLowerCase();
        for (var entrada : ARCHIVOS_LOGO_SCUDERIA.entrySet()) {
            if (normalizado.contains(entrada.getKey())) {
                Image imagen = GestorImagenes.cargar("scuderias/" + entrada.getValue());
                if (imagen != null) {
                    ImageView vista = new ImageView(imagen);
                    vista.setPreserveRatio(true);
                    vista.setFitWidth(50);
                    return new Group(vista);
                }
            }
        }
        return escudoEquipo(colorReserva);
    }

    /** Envuelve un ícono en un contenedor cuadrado con fondo rojo redondeado, usado en las tarjetas del menú. */
    public static StackPane contenedor(Node icono, double tamano) {
        StackPane fondo = new StackPane();
        fondo.getStyleClass().add("tarjeta-icono-fondo");
        fondo.setPrefSize(tamano, tamano);
        fondo.setMinSize(tamano, tamano);
        fondo.setMaxSize(tamano, tamano);
        fondo.getChildren().add(icono);
        return fondo;
    }

    public static StackPane contenedor(Node icono, double ancho, double alto) {
        StackPane fondo = new StackPane();
        fondo.getStyleClass().add("tarjeta-icono-fondo");
        fondo.setPrefSize(ancho, alto);
        fondo.setMinSize(ancho, alto);
        fondo.setMaxSize(ancho, alto);
        fondo.getChildren().add(icono);
        return fondo;
    }

    private static void aplicarGlow(Node nodo, Color color) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(14);
        glow.setSpread(0.25);
        nodo.setEffect(glow);
    }

    public static Group logo(double escala) {
        Image imagen = GestorImagenes.cargar("logo principal (f1).png");
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitHeight(40);

        Group grupo = new Group(vista);
        grupo.setScaleX(escala);
        grupo.setScaleY(escala);
        aplicarGlow(grupo, ROJO_BRILLANTE);
        return grupo;
    }

    

    /** Casco de piloto estilizado. */
    public static Group casco(Color color) {
        Image imagen = new Image("https://i.ibb.co/mrFCTM4n/upscalemedia-transformed-22.png", true);
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(50);
        return new Group(vista);
    }

    /** Bandera a cuadros. */
    public static Group banderaCuadros() {
        Image imagen = new Image("https://i.ibb.co/bMJDPLkT/upscalemedia-transformed-21.png", true);
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(50);
        return new Group(vista);
    }

    /** Lupa (buscar). */
    public static Group lupa(Color color) {
        Circle aro = new Circle(14, 14, 10);
        aro.setFill(Color.TRANSPARENT);
        aro.setStroke(color);
        aro.setStrokeWidth(3.2);

        Line mango = new Line(21, 21, 32, 32);
        mango.setStroke(color);
        mango.setStrokeWidth(3.6);
        mango.setStrokeLineCap(StrokeLineCap.ROUND);

        return new Group(aro, mango);
    }

    /** Documento (registrar). */
    public static Group documento(Color color) {
        Rectangle hoja = new Rectangle(6, 2, 22, 30);
        hoja.setArcWidth(4);
        hoja.setArcHeight(4);
        hoja.setFill(Color.TRANSPARENT);
        hoja.setStroke(color);
        hoja.setStrokeWidth(2.2);

        Group lineas = new Group();
        for (int i = 0; i < 4; i++) {
            Line linea = new Line(11, 9 + i * 5, 23, 9 + i * 5);
            linea.setStroke(color);
            linea.setStrokeWidth(1.6);
            lineas.getChildren().add(linea);
        }
        return new Group(hoja, lineas);
    }

    /** X (eliminar). */
    public static Group equis(Color color) {
        Line l1 = new Line(4, 4, 26, 26);
        Line l2 = new Line(26, 4, 4, 26);
        for (Line l : new Line[]{l1, l2}) {
            l.setStroke(color);
            l.setStrokeWidth(4);
            l.setStrokeLineCap(StrokeLineCap.ROUND);
        }
        return new Group(l1, l2);
    }

    /** Silueta simplificada de un trazado de circuito (estilo Nürburgring), usada como ícono de "Listar circuitos". */
    /** Silueta del circuito de Nürburgring, cargada desde una imagen por URL. */
    public static Group pistaSilueta(Color color) {
        Image imagen = new Image("https://i.ibb.co/Bm0jFpJ/upscalemedia-transformed-18.png", true);
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(50);
        return new Group(vista);
    }

    /** Carga un logo local desde /imagenes; si el archivo no existe, usa el escudo genérico como respaldo. */
    private static Group logoLocal(String nombreArchivo) {
        Image imagen = GestorImagenes.cargar(nombreArchivo);
        if (imagen != null) {
            ImageView vista = new ImageView(imagen);
            vista.setPreserveRatio(true);
            vista.setFitWidth(50);
            return new Group(vista);
        }
        return escudoEquipo(BLANCO);
    }

    /** Logo del botón "Carrera" del menú principal, cargado desde /imagenes. */
    public static Group logoCarrera() {
        return logoLocal("bandera a cuadros.png");
    }

    /** Logo del botón "Gestión de equipos" del menú principal, cargado desde /imagenes. */
    public static Group logoGestionEquipos() {
        return logoLocal("logo gestion de equipo.png");
    }

    /** Logo del botón "Modo campeonato" del menú principal, cargado desde /imagenes. */
    public static Group logoCampeonato() {
        return logoLocal("logo campeonato.png");
    }

    /** Logo del botón "Gestión de pilotos" del menú principal, cargado desde /imagenes. */
    public static Group logoGestionPilotos() {
        return logoLocal("avatars predeterminados/avatar 1.png");
    }

    /** Logo del botón "Gestión de vehículos" del menú principal, cargado desde /imagenes. */
    public static Group logoGestionVehiculos() {
        return logoLocal("monoplaza.png");
    }

    /** Logo del botón "Gestión de circuitos" del menú principal, cargado desde /imagenes. */
    public static Group logoGestionCircuitos() {
        return logoLocal("Pista logo principal.png");
    }

    /** Logo de la tarjeta "Configurar vehículo" dentro de Gestión de vehículos, cargado desde /imagenes. */
    public static Group logoConfigurarVehiculo() {
        return logoLocal("Imagen configuracion autos.png");
    }

    /**
     * Foto del piloto (subida por el usuario o avatar predeterminado elegido al registrarlo),
     * recortada en un cuadrado. Si el piloto no tiene foto asignada, o la URL guardada ya no
     * carga, se usa el casco genérico como respaldo.
     */
    public static StackPane avatarPiloto(Piloto piloto, double tamano) {
        Image imagen = cargarImagenPiloto(piloto);
        if (imagen == null) {
            return contenedor(casco(BLANCO), tamano);
        }
        // "Cover": se escala manteniendo proporción hasta cubrir el cuadrado completo y
        // se recorta el sobrante centrado, en vez de estirar la foto a la fuerza (lo cual
        // la distorsionaba y se veía de baja calidad).
        double escala = Math.max(tamano / imagen.getWidth(), tamano / imagen.getHeight());
        double anchoEscalado = imagen.getWidth() * escala;
        double altoEscalado = imagen.getHeight() * escala;

        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(anchoEscalado);
        vista.setFitHeight(altoEscalado);
        vista.setTranslateX(-(anchoEscalado - tamano) / 2.0);
        vista.setTranslateY(-(altoEscalado - tamano) / 2.0);

        Group recortado = new Group(vista);
        Rectangle recorte = new Rectangle(tamano, tamano);
        recorte.setArcWidth(12);
        recorte.setArcHeight(12);
        recortado.setClip(recorte);
        return contenedor(recortado, tamano);
    }

    private static Image cargarImagenPiloto(Piloto piloto) {
        String url = piloto.getImagenUrl();
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            Image imagen = new Image(url);
            return imagen.isError() ? null : imagen;
        } catch (Exception ex) {
            return null;
        }
    }

    /** Ícono de equipo: escudo con franja diagonal. */
    public static Group escudoEquipo(Color color) {
        Image imagen = new Image("https://i.ibb.co/7dcG7G6n/upscalemedia-transformed-19.png", true);
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(50);
        return new Group(vista);
    }

    /** Ícono de monoplaza estilizado (vista superior simplificada). */
    public static Group monoplaza(Color color) {
    Image imagen = new Image("https://i.ibb.co/TMSc4M3t/upscalemedia-transformed-20.png", true);
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(50);
        return new Group(vista);
    }

    /** Engranaje simple (configuración). */
    public static Group engranaje(Color color) {
    Image imagen = new Image("https://i.ibb.co/tph33mBY/upscalemedia-transformed-23.png", true);
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(50);
        return new Group(vista);
    }

    /** Flecha simple para "volver". */
    public static Group flechaVolver(Color color) {
        Polyline flecha = new Polyline(14, 2, 2, 12, 14, 22);
        flecha.setStroke(color);
        flecha.setStrokeWidth(2.6);
        flecha.setStrokeLineCap(StrokeLineCap.ROUND);
        flecha.setStrokeLineJoin(StrokeLineJoin.ROUND);
        flecha.setFill(Color.TRANSPARENT);

        Line linea = new Line(2, 12, 24, 12);
        linea.setStroke(color);
        linea.setStrokeWidth(2.6);
        linea.setStrokeLineCap(StrokeLineCap.ROUND);

        return new Group(linea, flecha);
    }

    /** Gota de lluvia / sol simples para selección de clima. */
    public static Group sol(Color color) {
        Circle centro = new Circle(16, 16, 6);
        centro.setFill(color);
        Group rayos = new Group();
        for (int i = 0; i < 8; i++) {
            double angulo = Math.toRadians(i * 45);
            Line rayo = new Line(
                    16 + Math.cos(angulo) * 9, 16 + Math.sin(angulo) * 9,
                    16 + Math.cos(angulo) * 14, 16 + Math.sin(angulo) * 14
            );
            rayo.setStroke(color);
            rayo.setStrokeWidth(2);
            rayo.setStrokeLineCap(StrokeLineCap.ROUND);
            rayos.getChildren().add(rayo);
        }
        return new Group(rayos, centro);
    }

    public static Group lluvia(Color color) {
        Group grupo = new Group();
        Ellipse nube = new Ellipse(16, 10, 12, 7);
        nube.setFill(color);
        grupo.getChildren().add(nube);
        for (int i = 0; i < 3; i++) {
            Line gota = new Line(9 + i * 7, 20, 6 + i * 7, 28);
            gota.setStroke(color);
            gota.setStrokeWidth(2.2);
            gota.setStrokeLineCap(StrokeLineCap.ROUND);
            grupo.getChildren().add(gota);
        }
        return grupo;
    }

    public static Group tormenta(Color color) {
        Group grupo = new Group();
        Ellipse nube = new Ellipse(16, 9, 13, 7);
        nube.setFill(color);
        Polygon rayo = new Polygon(17, 16, 11, 27, 16, 27, 12, 36, 23, 22, 17, 22);
        rayo.setFill(ROJO_BRILLANTE);
        grupo.getChildren().addAll(nube, rayo);
        return grupo;
    }

    public static Group dado(Color color) {
        Rectangle marco = new Rectangle(4, 4, 24, 24);
        marco.setArcWidth(6);
        marco.setArcHeight(6);
        marco.setFill(Color.TRANSPARENT);
        marco.setStroke(color);
        marco.setStrokeWidth(2.2);
        Group puntos = new Group();
        int[][] posiciones = {{10, 10}, {16, 16}, {22, 22}, {22, 10}, {10, 22}};
        for (int[] p : posiciones) {
            Circle punto = new Circle(p[0], p[1], 1.8);
            punto.setFill(color);
            puntos.getChildren().add(punto);
        }
        return new Group(marco, puntos);
    }
}
