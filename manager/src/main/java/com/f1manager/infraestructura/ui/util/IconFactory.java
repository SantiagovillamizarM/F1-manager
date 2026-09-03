//Dibuja (o carga por URL) todos los íconos que
// se usan en la app (casco, bandera, lupa, auto, escudo, etc.).

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.util;

//Trae Group, un contenedor de JavaFX que agrupa varios nodos (formas, imágenes) para tratarlos como uno solo.
import javafx.scene.Group;
//Trae Node, la clase base de todo lo que se puede dibujar/mostrar en una escena de JavaFX.
import javafx.scene.Node;
//Trae DropShadow, un efecto visual de JavaFX que agrega una sombra/resplandor detrás de un nodo.
import javafx.scene.effect.DropShadow;
//Trae la clase Image de JavaFX, que representa una imagen ya cargada en memoria lista para mostrarse en pantalla.
import javafx.scene.image.Image;
//Trae StackPane, un contenedor que apila sus hijos uno encima del otro (se usa aquí como "marco" cuadrado de los íconos).
import javafx.scene.layout.StackPane;
//Trae Color, la clase de JavaFX que representa un color.
import javafx.scene.paint.Color;
//Trae la clase del dominio Equipo, para poder dibujar el logo/monoplaza correspondiente a cada equipo.
import com.f1manager.dominio.modelo.Equipo;
//Trae la clase del dominio Piloto, para poder dibujar el avatar/foto correspondiente a cada piloto.
import com.f1manager.dominio.modelo.Piloto;
//Trae todas las formas geométricas de JavaFX de una vez (Circle, Line, Rectangle, Polygon, Polyline, Ellipse, etc.) usadas para dibujar íconos a mano.
import javafx.scene.shape.*;
//Trae ImageView, el nodo de JavaFX que muestra una Image dentro de la escena (con su tamaño, proporción, etc.).
import javafx.scene.image.ImageView;
//Importa la interfaz general Map, que sirve como plantilla para crear diccionarios de clave y valor
import java.util.Map;
//Clase publica y final (no se puede heredar de ella) llamada "IconFactory"
public final class IconFactory {

    //Color rojo principal de la app (marca F1)
    public static final Color ROJO = Color.web("#e10600");
    //Color rojo más claro/brillante, usado en resplandores (glow) y detalles destacados
    public static final Color ROJO_BRILLANTE = Color.web("#ff2b2b");
    //Color blanco (casi blanco) usado como color por defecto de los íconos
    public static final Color BLANCO = Color.web("#f5f6fa");
    //Color gris usado en detalles secundarios
    public static final Color GRIS = Color.web("#464d5e");

    //Asocia una palabra clave del nombre del equipo (en minúsculas) con su logo local en /imagenes/scuderias.
    private static final Map<String, String> ARCHIVOS_LOGO_SCUDERIA = Map.of(
            "ferrari", "scuderia ferrari.png",
            "mercedes", "scuderia mercedes.png",
            "red bull", "scuderia redbull.png",
            "mclaren", "scuderia mclaren.png",
            "aston martin", "scuderia aston martin.png",
            "alpine", "scuderia alpine.png"
    );

    //Asocia una palabra clave del nombre del equipo (en minúsculas) con la foto de su monoplaza en /imagenes/monoplazas.
    private static final Map<String, String> ARCHIVOS_MONOPLAZA_EQUIPO = Map.of(
            "ferrari", "ferrari f1.jpg",
            "mercedes", "mercedes f1.jpg",
            "red bull", "redbull f1.jpg",
            "mclaren", "mclaren f1.jpg",
            "aston martin", "aston martin f1.jpg",
            "alpine", "F1 Alpine.jpg"
    );

    //Constructor privado y vacío: esta clase es solo de utilidades (puros métodos estáticos),
    //no tiene sentido crear un objeto IconFactory con "new", por eso se bloquea el constructor.
    private IconFactory() {
    }

    //Logo real de la escudería según su nombre (ej. "Scuderia Ferrari" -> logo de Ferrari),
    //cargado desde /imagenes/scuderias. Si el equipo no coincide con ninguna escudería
    //conocida (por ejemplo, uno registrado manualmente por el usuario), se usa el
    //escudo genérico como respaldo.
    public static Group logoEquipo(String nombreEquipo, Color colorReserva) {
        //Pasa el nombre a minúsculas para buscar sin importar mayúsculas (o texto vacío si viene nulo, para no romper el programa)
        String normalizado = nombreEquipo == null ? "" : nombreEquipo.toLowerCase();
        //Recorre el diccionario de escuderías conocidas buscando si el nombre del equipo contiene alguna palabra clave
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
        //Si no coincidió con ninguna escudería conocida (o la imagen no se pudo cargar), usa el escudo genérico
        return logoGestionEquipos();
    }

    //Logo de un equipo: si tiene una imagen propia subida por el usuario, se usa esa; si no,
    //cae al logo real de la escudería por nombre (logoEquipo) y, en último caso, al
    //escudo genérico.
    public static Group imagenEquipo(Equipo equipo, double ancho) {
        Image imagen = cargarImagenEquipo(equipo);
        if (imagen == null) {
            return logoEquipo(equipo.getNombre(), BLANCO);
        }
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(ancho);
        return new Group(vista);
    }

    //Intenta cargar la imagen propia del equipo (subida por el usuario) desde su URL guardada.
    //Si no tiene URL, o la imagen falla al cargar (por ejemplo, un link roto), devuelve null en vez de fallar.
    private static Image cargarImagenEquipo(Equipo equipo) {
        String url = equipo.getImagenUrl();
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

    //Envuelve un ícono en un contenedor cuadrado con fondo rojo redondeado, usado en las tarjetas del menú.
    public static StackPane contenedor(Node icono, double tamano) {
        StackPane fondo = new StackPane();
        fondo.getStyleClass().add("tarjeta-icono-fondo");
        fondo.setPrefSize(tamano, tamano);
        fondo.setMinSize(tamano, tamano);
        fondo.setMaxSize(tamano, tamano);
        fondo.getChildren().add(icono);
        return fondo;
    }

    //Igual que el de arriba, pero permitiendo un contenedor rectangular (ancho y alto distintos) en vez de solo cuadrado
    public static StackPane contenedor(Node icono, double ancho, double alto) {
        StackPane fondo = new StackPane();
        fondo.getStyleClass().add("tarjeta-icono-fondo");
        fondo.setPrefSize(ancho, alto);
        fondo.setMinSize(ancho, alto);
        fondo.setMaxSize(ancho, alto);
        fondo.getChildren().add(icono);
        return fondo;
    }

    //Le agrega a un nodo un efecto de resplandor (sombra difusa de color) alrededor, usado para destacar el logo principal
    private static void aplicarGlow(Node nodo, Color color) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(14);
        glow.setSpread(0.25);
        nodo.setEffect(glow);
    }

    //Logo principal de la app (el de la pantalla de inicio), cargado desde /imagenes, con resplandor rojo y escalable
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



    //Casco de piloto estilizado, cargado por URL (el "true" del final activa la carga en segundo plano, para no congelar la interfaz mientras baja la imagen).
    public static Group casco(Color color) {
        Image imagen = new Image("https://i.ibb.co/mrFCTM4n/upscalemedia-transformed-22.png", true);
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(50);
        return new Group(vista);
    }

    //Bandera a cuadros, cargada por URL.
    public static Group banderaCuadros() {
        Image imagen = new Image("https://i.ibb.co/bMJDPLkT/upscalemedia-transformed-21.png", true);
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(50);
        return new Group(vista);
    }

    //Lupa (buscar), dibujada a mano con un círculo (el aro) y una línea (el mango).
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

    //Documento (registrar), dibujado a mano: una hoja rectangular con las esquinas redondeadas y unas líneas simulando texto.
    public static Group documento(Color color) {
        Rectangle hoja = new Rectangle(6, 2, 22, 30);
        hoja.setArcWidth(4);
        hoja.setArcHeight(4);
        hoja.setFill(Color.TRANSPARENT);
        hoja.setStroke(color);
        hoja.setStrokeWidth(2.2);

        Group lineas = new Group();
        //Dibuja 4 líneas paralelas (una debajo de otra, separadas 5 en 5) simulando renglones de texto
        for (int i = 0; i < 4; i++) {
            Line linea = new Line(11, 9 + i * 5, 23, 9 + i * 5);
            linea.setStroke(color);
            linea.setStrokeWidth(1.6);
            lineas.getChildren().add(linea);
        }
        return new Group(hoja, lineas);
    }

    //X (eliminar), dibujada a mano con dos líneas cruzadas en diagonal.
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

    //Lápiz (editar), cargado desde /imagenes.
    public static Group lapiz(Color color) {
        return logoLocal("lapiz icon.png");
    }

    //Silueta del circuito de Nürburgring (estilo genérico de pista), cargada desde una imagen por URL, usada como ícono de "Listar circuitos".
    public static Group pistaSilueta(Color color) {
        Image imagen = new Image("https://i.ibb.co/Bm0jFpJ/upscalemedia-transformed-18.png", true);
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(50);
        return new Group(vista);
    }

    //Carga un logo local desde /imagenes; si el archivo no existe, usa el escudo genérico como respaldo.
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

    //Logo del botón "Carrera" del menú principal, cargado desde /imagenes.
    public static Group logoCarrera() {
        return logoLocal("bandera a cuadros.png");
    }

    //Logo del botón "Gestión de equipos" del menú principal, cargado desde /imagenes.
    public static Group logoGestionEquipos() {
        return logoLocal("logo gestion de equipo.png");
    }

    //Logo del botón "Modo campeonato" del menú principal, cargado desde /imagenes.
    public static Group logoCampeonato() {
        return logoLocal("logo campeonato.png");
    }

    //Logo del botón "Gestión de pilotos" del menú principal, cargado desde /imagenes.
    public static Group logoGestionPilotos() {
        return logoLocal("avatars predeterminados/avatar 1.png");
    }

    //Logo del botón "Gestión de vehículos" del menú principal, cargado desde /imagenes.
    public static Group logoGestionVehiculos() {
        return logoLocal("monoplaza.png");
    }

    //Logo del botón "Gestión de circuitos" del menú principal, cargado desde /imagenes.
    public static Group logoGestionCircuitos() {
        return logoLocal("Pista logo principal.png");
    }

    //Logo de la tarjeta "Configurar vehículo" dentro de Gestión de vehículos, cargado desde /imagenes.
    public static Group logoConfigurarVehiculo() {
        return logoLocal("Imagen configuracion autos.png");
    }

    //Foto del piloto (subida por el usuario o avatar predeterminado elegido al registrarlo),
    //recortada en un cuadrado. Si el piloto no tiene foto asignada, o la URL guardada ya no
    //carga, se usa el casco genérico como respaldo.
    public static StackPane avatarPiloto(Piloto piloto, double tamano) {
        Image imagen = cargarImagenPiloto(piloto);
        if (imagen == null) {
            return contenedor(casco(BLANCO), tamano);
        }
        // "Cover": se escala manteniendo proporción hasta cubrir el cuadrado completo y
        // se recorta el sobrante centrado, en vez de estirar la foto a la fuerza (lo cual
        // la distorsionaba y se veía de baja calidad).
        //Se elige la escala más grande entre "cuánto hay que agrandar el ancho" y "cuánto hay que
        //agrandar el alto" para que la imagen termine cubriendo todo el cuadrado sin dejar huecos
        double escala = Math.max(tamano / imagen.getWidth(), tamano / imagen.getHeight());
        double anchoEscalado = imagen.getWidth() * escala;
        double altoEscalado = imagen.getHeight() * escala;

        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(anchoEscalado);
        vista.setFitHeight(altoEscalado);
        //Desplaza la imagen para centrarla dentro del cuadrado (la mitad de lo que sobra para cada lado)
        vista.setTranslateX(-(anchoEscalado - tamano) / 2.0);
        vista.setTranslateY(-(altoEscalado - tamano) / 2.0);

        Group recortado = new Group(vista);
        //Recorte (clip): solo se ve la parte de la imagen que cae dentro de este rectángulo con esquinas redondeadas
        Rectangle recorte = new Rectangle(tamano, tamano);
        recorte.setArcWidth(12);
        recorte.setArcHeight(12);
        recortado.setClip(recorte);
        return contenedor(recortado, tamano);
    }

    //Intenta cargar la imagen propia del piloto (subida por el usuario o avatar elegido) desde su
    //URL guardada. Si no tiene URL, o la imagen falla al cargar (por ejemplo, un link roto), devuelve
    //null en vez de fallar.
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

    //Ícono de equipo: escudo con franja diagonal, cargado por URL.
    public static Group escudoEquipo(Color color) {
        Image imagen = new Image("https://i.ibb.co/7dcG7G6n/upscalemedia-transformed-19.png", true);
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(50);
        return new Group(vista);
    }

    //Ícono de monoplaza estilizado (vista superior simplificada), cargado desde /imagenes.
    public static Group monoplaza(Color color) {
        Image imagen = GestorImagenes.cargar("monoplaza.png");
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(50);
        return new Group(vista);
    }

    //Foto real del monoplaza según su equipo (ej. "Scuderia Ferrari" -> monoplaza de Ferrari),
    //cargada desde /imagenes/monoplazas. Si el equipo no coincide con ninguna escudería conocida
    //(por ejemplo, uno registrado manualmente por el usuario), se usa el ícono genérico de
    //monoplaza como respaldo.
    public static Group monoplazaDeEquipo(String nombreEquipo, double ancho) {
        //Pasa el nombre a minúsculas para buscar sin importar mayúsculas (o texto vacío si viene nulo, para no romper el programa)
        String normalizado = nombreEquipo == null ? "" : nombreEquipo.toLowerCase();
        Image imagen = null;
        //Recorre el diccionario de escuderías conocidas buscando si el nombre del equipo contiene alguna palabra clave
        for (var entrada : ARCHIVOS_MONOPLAZA_EQUIPO.entrySet()) {
            if (normalizado.contains(entrada.getKey())) {
                imagen = GestorImagenes.cargar("monoplazas/" + entrada.getValue());
                break;
            }
        }
        //Si no coincidió con ninguna escudería conocida, usa el ícono genérico de monoplaza
        if (imagen == null) {
            imagen = GestorImagenes.cargar("monoplaza.png");
        }
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(ancho);
        return new Group(vista);
    }

    //Engranaje simple (configuración), cargado por URL.
    public static Group engranaje(Color color) {
    Image imagen = new Image("https://i.ibb.co/tph33mBY/upscalemedia-transformed-23.png", true);
        ImageView vista = new ImageView(imagen);
        vista.setPreserveRatio(true);
        vista.setFitWidth(50);
        return new Group(vista);
    }

    //Flecha simple para "volver", dibujada a mano con una línea (el palo) y una polilínea en forma de punta.
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

    //Sol, usado para selección de clima seco: un círculo central con 8 rayos alrededor.
    //Los rayos se reparten en un círculo completo dividiéndolo en 8 partes iguales (360° / 8 = 45°
    //cada uno) y, con seno y coseno de ese ángulo, se calcula dónde empieza y termina cada rayo.
    public static Group sol(Color color) {
        Circle centro = new Circle(16, 16, 6);
        centro.setFill(color);
        Group rayos = new Group();
        for (int i = 0; i < 8; i++) {
            //toRadians convierte el ángulo de grados (más fácil de pensar) a radianes (lo que usan Math.cos/Math.sin)
            double angulo = Math.toRadians(i * 45);
            //Cada rayo va de un punto un poco alejado del centro (radio 9) a otro más lejano (radio 14),
            //en la dirección marcada por el ángulo (coordenadas polares otra vez)
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

    //Gota de lluvia (nube con gotas cayendo), usado para selección de clima lluvioso: una nube ovalada y 3 gotas debajo.
    public static Group lluvia(Color color) {
        Group grupo = new Group();
        Ellipse nube = new Ellipse(16, 10, 12, 7);
        nube.setFill(color);
        grupo.getChildren().add(nube);
        //Dibuja 3 gotas en diagonal, separadas horizontalmente entre sí
        for (int i = 0; i < 3; i++) {
            Line gota = new Line(9 + i * 7, 20, 6 + i * 7, 28);
            gota.setStroke(color);
            gota.setStrokeWidth(2.2);
            gota.setStrokeLineCap(StrokeLineCap.ROUND);
            grupo.getChildren().add(gota);
        }
        return grupo;
    }

    //Nube con rayo, usado para selección de clima extremo/tormenta: una nube ovalada y un rayo en zigzag (Polygon con varios puntos).
    public static Group tormenta(Color color) {
        Group grupo = new Group();
        Ellipse nube = new Ellipse(16, 9, 13, 7);
        nube.setFill(color);
        Polygon rayo = new Polygon(17, 16, 11, 27, 16, 27, 12, 36, 23, 22, 17, 22);
        rayo.setFill(ROJO_BRILLANTE);
        grupo.getChildren().addAll(nube, rayo);
        return grupo;
    }

    //Dado, usado para selección de clima aleatorio: un marco cuadrado redondeado con 5 puntos (como la cara del "5" de un dado).
    public static Group dado(Color color) {
        Rectangle marco = new Rectangle(4, 4, 24, 24);
        marco.setArcWidth(6);
        marco.setArcHeight(6);
        marco.setFill(Color.TRANSPARENT);
        marco.setStroke(color);
        marco.setStrokeWidth(2.2);
        Group puntos = new Group();
        //Coordenadas fijas de los 5 puntos del dado: centro y las 4 esquinas
        int[][] posiciones = {{10, 10}, {16, 16}, {22, 22}, {22, 10}, {10, 22}};
        for (int[] p : posiciones) {
            Circle punto = new Circle(p[0], p[1], 1.8);
            punto.setFill(color);
            puntos.getChildren().add(punto);
        }
        return new Group(marco, puntos);
    }
}
