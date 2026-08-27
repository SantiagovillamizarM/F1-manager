package com.f1manager;


import com.f1manager.infraestructura.ui.util.GestorEscenas;
import com.f1manager.infraestructura.ui.PantallaBienvenida;
import javafx.application.Application;
import javafx.stage.Stage;

//Este proyecto solo se ejecuta por powershell por que el "Run Java" es un atajo generico
//con mvn clean javafx:run ejecuta todo lo que javafx necesita para funcionar
public class Main extends Application {

    @Override
    public void start(Stage stagePrimario) {
        GestorEscenas gestor = new GestorEscenas(stagePrimario);
        gestor.mostrarInicial(new PantallaBienvenida(gestor));
        stagePrimario.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
/*
Aquí están **todos** los imports usados en el proyecto completo (sin contar `com.f1manager.*`, que son nuestras propias clases), explicados en simple:

**📦 Paquete `java.util` (utilidades básicas de Java)**

- `java.util.*` — trae todas las utilidades de esta categoría de una vez (listas, mapas, etc.), en vez de importarlas una por una.
- `ArrayDeque` — una "cola de dos puntas": se usa para el historial de pantallas (poder ir hacia adelante y hacia atrás).
- `ArrayList` — una lista común donde se pueden agregar/quitar elementos (usada, por ejemplo, para guardar los tiempos por vuelta).
- `Collections` — trae herramientas listas para usar sobre listas, como crear una lista vacía.
- `Comparator` — permite decir "cómo ordenar algo" (por ejemplo, ordenar pilotos por tiempo, del más rápido al más lento).
- `Deque` — el "tipo general" de cola de dos puntas (ArrayDeque es una implementación de esto).
- `EnumMap` — un mapa (como un diccionario) optimizado para cuando las claves son opciones fijas (ej. los climas: Seco, Lluvioso, etc.).
- `List` — el "tipo general" de lista (ArrayList es una implementación de esto).
- `Map` — el "tipo general" de diccionario (clave → valor), por ejemplo piloto → color en la animación.
- `Objects` — trae herramientas para comparar objetos (usado en los métodos `equals`/`hashCode` de los modelos).
- `Random` — el generador de números al azar (usado en la simulación de carreras y en la forma de las pistas).
- `concurrent.atomic.AtomicInteger` — un número que se puede aumentar de forma segura (usado para generar IDs únicos automáticamente).
- `function.BiConsumer` — representa "una acción que recibe dos datos y no devuelve nada" (usado, por ejemplo, cuando eliges circuito + clima y eso dispara la carrera).
- `function.Consumer` — representa "una acción que recibe un dato y no devuelve nada" (usado para pasar funciones como parámetros, ej. qué hacer al terminar la animación).
- `stream.Collectors` — ayuda a transformar listas filtradas/procesadas en resultados finales (como juntar texto separado por comas).

**📦 Paquete `javafx` (todo lo visual)**

- `animation.AnimationTimer` — permite ejecutar código muchas veces por segundo (usado para animar los puntos de la carrera).
- `animation.FadeTransition` — crea el efecto de aparecer/desaparecer suavemente (fade).
- `animation.SequentialTransition` — permite encadenar varias animaciones, una después de la otra.
- `application.Application` — la clase base que todo programa JavaFX debe extender para poder ejecutarse.
- `application.Platform` — trae funciones generales del programa, como cerrarlo correctamente.
- `collections.FXCollections` — crea listas especiales de JavaFX que "avisan" automáticamente a la pantalla cuando cambian.
- `collections.ObservableList` — el "tipo" de esas listas especiales (las usa `DataStore` para guardar pilotos, equipos, etc.).
- `geometry.Insets` — define márgenes/espacios internos (padding) alrededor de los elementos.
- `geometry.Point2D` — representa un punto con coordenadas X e Y (usado para calcular posiciones sobre la pista).
- `geometry.Pos` — define alineaciones (centrado, izquierda, arriba, etc.).
- `scene.Group` — un contenedor simple que junta varias formas dibujadas como si fueran una sola (usado en los íconos).
- `scene.Node` — el "tipo general" de cualquier cosa que se pueda mostrar en pantalla (botón, texto, imagen, etc.).
- `scene.Scene` — representa el contenido completo que se muestra dentro de la ventana.
- `scene.canvas.Canvas` — un "lienzo" en blanco donde se puede dibujar directamente (usado para las pistas y la animación de la carrera).
- `scene.canvas.GraphicsContext` — la "herramienta de dibujo" que se usa sobre el Canvas (como el pincel).
- `scene.control.*` — trae todos los controles interactivos de una vez (botones, campos de texto, etc.).
- `scene.control.Button` — un botón clicable.
- `scene.control.ComboBox` — una lista desplegable donde eliges una opción.
- `scene.control.Label` — un texto simple que no se puede editar.
- `scene.control.ScrollPane` — agrega una barra de desplazamiento cuando el contenido no cabe.
- `scene.control.Separator` — una línea divisoria visual.
- `scene.control.TextField` — un campo donde el usuario escribe texto.
- `scene.control.Tooltip` — el textito que aparece al dejar el mouse quieto sobre algo.
- `scene.effect.DropShadow` — agrega sombra o brillo (glow) alrededor de un elemento.
- `scene.layout.*` — trae todos los contenedores de organización de una vez.
- `scene.layout.BorderPane` — organiza el contenido en 5 zonas (arriba, abajo, izquierda, derecha, centro).
- `scene.layout.FlowPane` — acomoda elementos en fila y va bajando de línea automáticamente cuando no caben (como texto).
- `scene.layout.HBox` — acomoda elementos en fila (horizontal).
- `scene.layout.Priority` — define qué elemento debe "crecer más" cuando hay espacio libre.
- `scene.layout.Region` — el "tipo general" de cualquier contenedor con forma rectangular.
- `scene.layout.StackPane` — apila elementos uno encima de otro.
- `scene.layout.VBox` — acomoda elementos en columna (vertical).
- `scene.paint.Color` — define colores.
- `scene.paint.CycleMethod` — define cómo se repite un degradado (gradiente) si no alcanza a cubrir todo el espacio.
- `scene.paint.LinearGradient` — crea un degradado de color en línea recta (usado en el fondo animado).
- `scene.paint.Stop` — define un "punto de color" dentro de un degradado (ej. "en el 50% del camino, este color").
- `scene.shape.*` — trae todas las formas geométricas de una vez (círculos, líneas, polígonos, etc., usados en los íconos).
- `scene.transform.Scale` — permite agrandar/achicar visualmente un elemento (usado para el diseño responsive).
- `stage.Stage` — representa la ventana física del programa.
- `util.Duration` — define cuánto tiempo dura algo (ej. una animación de fade).

*/
