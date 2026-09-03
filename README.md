# F1 Manager

Aplicación de escritorio hecha en **Java 21 + JavaFX** para gestionar una escudería de Fórmula 1: registrar circuitos, pilotos, equipos y vehículos, configurar el setup de cada monoplaza, y simular carreras individuales o una temporada completa (Modo Campeonato) con un motor de simulación propio que combina habilidad del piloto, características del auto, configuración y clima.

Los datos se guardan en una base de datos **MySQL** y se cachean en memoria mientras la aplicación está abierta.

---

## Tabla de contenido

- [Stack tecnológico](#stack-tecnológico)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Requisitos previos](#requisitos-previos)
- [Puesta en marcha](#puesta-en-marcha)
- [Base de datos](#base-de-datos)
- [Funcionalidades](#funcionalidades)
- [Modelo de dominio](#modelo-de-dominio)
- [Motor de simulación](#motor-de-simulación)
- [Capa de UI](#capa-de-ui)
- [Recursos (imágenes, audio, estilos)](#recursos-imágenes-audio-estilos)
- [Notas y limitaciones conocidas](#notas-y-limitaciones-conocidas)

---

## Stack tecnológico

| Componente         | Detalle                                                              |
|---------------------|-----------------------------------------------------------------------|
| Lenguaje            | Java 21                                                                |
| UI                  | JavaFX 21.0.2 (`javafx-controls`, `javafx-fxml`, `javafx-media`)       |
| Build               | Maven (`javafx-maven-plugin`, `maven-compiler-plugin`)                 |
| Base de datos       | MySQL (driver `mysql-connector-j` 8.4.0, acceso vía JDBC puro)         |
| Persistencia        | JDBC manual (sin ORM) + una caché en memoria (`DataStore`, patrón Singleton) respaldada por `ObservableList` de JavaFX |

No se usa FXML: todas las pantallas se arman por código Java extendiendo layouts de JavaFX (`BorderPane`, `VBox`, `HBox`, `GridPane`, etc.).

## Estructura del proyecto

```
f1_manager/
└── manager/                          ← módulo Maven único del proyecto
    ├── pom.xml
    └── src/main/
        ├── java/com/f1manager/
        │   ├── Main.java                        ← punto de entrada Y compositor (wiring hexagonal, ver más abajo)
        │   ├── dominio/                          ← núcleo: no depende de JavaFX ni de MySQL
        │   │   ├── modelo/                      ← entidades y enums de negocio
        │   │   ├── servicio/SimuladorCarrera.java  ← motor matemático de la carrera
        │   │   ├── repositorio/                 ← PUERTOS: interfaces que el núcleo necesita para persistir datos
        │   │   └── excepcion/ValidacionException.java
        │   ├── aplicacion/
        │   │   └── DataStore.java               ← casos de uso: valida, orquesta reglas y cachea en memoria; solo conoce los puertos
        │   └── infraestructura/                  ← todo lo que depende de tecnología externa (adaptadores)
        │       ├── persistencia/                ← ADAPTADORES: implementan los puertos hablando con MySQL
        │       └── ui/                           ← ADAPTADOR de entrada: JavaFX llama a la aplicación, nunca a MySQL
        │           ├── MenuPrincipal.java, PantallaBienvenida.java, ModuloGestionBase.java
        │           ├── components/              ← controles reutilizables (tarjetas, buscador, fondo animado...)
        │           ├── screens/                 ← pantallas agrupadas por módulo (campeonato, carrera, circuitos, equipos, pilotos, vehiculos)
        │           └── util/                    ← navegación, sonido, imágenes, íconos, generación de pistas
        └── resources/
            ├── estilo.css
            ├── audio/                            ← música y efectos de sonido
            └── imagenes/                         ← logos, avatares, fotos de pilotos, autos, choques...
```

### Arquitectura hexagonal (puertos y adaptadores)

El proyecto sigue arquitectura hexagonal de verdad, con inversión de dependencias real (no solo una separación de carpetas):

- **Puertos** (`dominio/repositorio`): interfaces como `CircuitoRepositorio`, `PilotoRepositorio`, `EquipoRepositorio` y `VehiculoRepositorio`. Definen QUÉ operaciones de persistencia necesita el negocio (listar, insertar, actualizar, eliminar), sin decir CÓMO ni con qué tecnología se cumplen.
- **Adaptadores de salida** (`infraestructura/persistencia`): `CircuitoRepositorioMySQL`, `PilotoRepositorioMySQL`, `EquipoRepositorioMySQL` y `VehiculoRepositorioMySQL` implementan esos puertos hablando con MySQL por JDBC. Son la única parte del proyecto que sabe que existe MySQL.
- **Aplicación** (`aplicacion/DataStore`): el caso de uso central. Valida formularios, aplica reglas de negocio (un equipo no se borra si tiene pilotos o vehículo, cada equipo solo puede tener un vehículo, etc.) y mantiene la caché en memoria (`ObservableList`) que la UI consume. Solo depende de los 4 puertos — nunca de una clase `*MySQL` — así que no tiene ni idea de que la base de datos es MySQL.
- **Adaptador de entrada** (`infraestructura/ui`): las pantallas de JavaFX llaman a `DataStore.getInstancia()` para todo (nunca a un repositorio MySQL directamente).
- **Compositor** (`Main.java`): el único lugar del programa que conoce a la vez los puertos y los adaptadores concretos. En `start()`, antes de mostrar cualquier pantalla, crea los 4 adaptadores de MySQL y se los entrega a `DataStore.iniciar(...)`, que los guarda tipados como sus interfaces.

Gracias a esto, cambiar de motor de base de datos (o agregar, por ejemplo, un modo de pruebas con datos en memoria) solo requiere escribir nuevas clases que implementen los 4 puertos y cambiar esa única línea en `Main.start()` — ni `DataStore` ni ninguna pantalla de la UI se tocan.

## Requisitos previos

- **JDK 21** instalado y configurado.
- **Maven** (o usar el wrapper si se agrega uno; actualmente se asume `mvn` en el PATH).
- **MySQL Server** corriendo en `localhost:3306`, con una base de datos llamada `f1_manager` y las tablas descritas en la sección [Base de datos](#base-de-datos).

## Puesta en marcha

1. **Configurar la conexión a MySQL** en [`ConexionMySQL.java`](manager/src/main/java/com/f1manager/infraestructura/persistencia/ConexionMySQL.java):

   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/f1_manager";
   private static final String USUARIO = "root";
   private static final String CONTRASENA = "..."; // reemplazar por tu contraseña real
   ```

   > Actualmente la URL, el usuario y la contraseña están **hardcodeados** en el código fuente (no hay `.env` ni archivo de configuración externo).

2. **Crear la base de datos y las tablas** en MySQL (ver el esquema más abajo).

3. **Ejecutar la aplicación** desde `manager/`:

   ```powershell
   mvn clean javafx:run
   ```

   > El propio código (`Main.java`) deja la nota de que este proyecto se debe correr desde PowerShell con `mvn clean javafx:run` — el botón "Run Java" del IDE es un atajo genérico que no configura correctamente el módulo de JavaFX.

Al arrancar se abre `PantallaBienvenida`, y desde ahí se navega al `MenuPrincipal`, que es el punto de partida para todos los módulos.

## Base de datos

`DataStore` es un singleton que, al crearse, carga en memoria (en listas `ObservableList`) el contenido completo de 4 tablas, a través de los puertos (`dominio.repositorio`), cumplidos en tiempo de ejecución por los adaptadores `*RepositorioMySQL`. Cada alta/edición/baja hecha desde la UI escribe primero en MySQL (vía el puerto correspondiente) y luego refleja el cambio en la lista en memoria, para que la interfaz se actualice sola (gracias a `ObservableList`).

No existe un script `.sql` de creación de esquema incluido en el repositorio; las tablas usadas, deducidas de las consultas SQL de cada repositorio, son:

**`equipos`**
| Columna     | Tipo sugerido      |
|-------------|---------------------|
| `nombre`    | VARCHAR (clave, se usa como identificador natural) |
| `pais`      | VARCHAR |
| `motor`     | VARCHAR |
| `imagen_url`| VARCHAR (nullable — logo subido) |

**`circuitos`**
| Columna         | Tipo sugerido |
|------------------|----------------|
| `id`             | INT AUTO_INCREMENT (PK) |
| `nombre`         | VARCHAR |
| `pais`           | VARCHAR |
| `longitud_km`    | DOUBLE |
| `vueltas`        | INT |
| `descripcion`    | VARCHAR/TEXT |

**`pilotos`**
| Columna                     | Tipo sugerido |
|------------------------------|----------------|
| `id`                         | INT AUTO_INCREMENT (PK) |
| `nombre`                     | VARCHAR |
| `equipo`                     | VARCHAR (FK lógica a `equipos.nombre`) |
| `rol`                        | VARCHAR (`LIDER` / `ESCUDERO`) |
| `experiencia_anios`          | INT |
| `habilidad_curva`            | INT (1-100) |
| `habilidad_adelantamiento`   | INT (1-100) |
| `habilidad_recta`            | INT (1-100) |
| `habilidad_lluvia`           | INT (1-100) |
| `habilidad_seco`             | INT (1-100) |
| `habilidad_extremo`          | INT (1-100) |
| `imagen_url`                 | VARCHAR (nullable) |

**`vehiculos`**
| Columna               | Tipo sugerido |
|------------------------|----------------|
| `id`                   | INT AUTO_INCREMENT (PK) |
| `modelo`               | VARCHAR |
| `equipo`               | VARCHAR (FK lógica a `equipos.nombre`, un vehículo por equipo) |
| `motor`                | VARCHAR |
| `velocidad_max_kmh`    | DOUBLE (300-380) |
| `aceleracion_0_100`    | DOUBLE (2.0-4.0 segundos) |
| `carga_aerodinamica`   | VARCHAR (`BAJA`/`MEDIA`/`ALTA`) |
| `modo_conduccion`      | VARCHAR (`NORMAL`/`AGRESIVO`/`AHORRO`) |
| `tipo_neumatico`       | VARCHAR (`BLANDO`/`MEDIO`/`DURO`/`INTERMEDIO`/`LLUVIA`) |
| `presion_aire`         | DOUBLE (18-26 PSI) |

`equipos` y `circuitos` tienen borrado directo; en `EquipoRepositorioMySQL` el borrado se bloquea desde `DataStore` (en Java, antes de llegar a MySQL) si el equipo todavía tiene pilotos o vehículo asignado, para evitar que una restricción de llave foránea reviente con un error críptico.

## Funcionalidades

Desde el **Menú Principal** se accede a dos grandes columnas:

**Administración y equipo** (CRUD completo sobre cada entidad, con validación de formularios vía `ValidacionException`):
- **Gestión de Circuitos**: registrar, listar, buscar por país, editar y eliminar.
- **Gestión de Pilotos**: registrar (con nombre, equipo, rol Líder/Escudero, experiencia, 6 habilidades de 1 a 100, y foto/avatar), listar, editar, eliminar.
- **Gestión de Equipos**: registrar (nombre, país, motor, logo), listar, eliminar (bloqueado si tiene pilotos o vehículo).
- **Gestión de Vehículos**: registrar (modelo, equipo, motor, velocidad máxima y aceleración según rango FIA), listar, y **configurar** el setup de carrera (carga aerodinámica, modo de conducción, tipo de neumático, presión de aire).

**Competencia y simulación**:
- **Carrera**: se elige un circuito, un clima (o "Aleatorio") y los pilotos participantes, y se simula una carrera puntual con animación en vivo del recorrido, clasificación en tiempo real y resultados finales (con posibilidad de fotos de choque si hubo algún DNF).
- **Modo Campeonato**: una temporada completa sobre un calendario de circuitos, con clasificación de pilotos y de equipos acumulando puntos carrera a carrera (sistema de puntos estilo F1: 25-18-15-12-10-8-6-4-2-1), clima dinámico por país (ver más abajo) y pantalla de campeón al finalizar.

Un botón de **Mutear/Activar música** en la barra superior controla la música de fondo (`GestorSonido`).

## Modelo de dominio

Ubicado en `dominio/modelo`:

| Clase / Enum | Qué representa |
|---|---|
| `Circuito` | Un trazado: id, nombre, país, longitud (km), vueltas, descripción. |
| `Piloto` | Nombre, equipo, `RolPiloto` (Líder/Escudero), años de experiencia, y 6 habilidades independientes de 1 a 100: curva, adelantamiento, recta, lluvia, seco y extremo. |
| `Equipo` | Nombre, país, motor, logo. |
| `Monoplaza` | El auto de un equipo: modelo, motor, velocidad máxima (300-380 km/h) y aceleración 0-100 (2.0-4.0 s) según reglamento FIA, más la configuración de carrera vigente (carga aerodinámica, modo de conducción, neumático, presión de aire entre 18 y 26 PSI). |
| `CargaAerodinamica` (enum) | `BAJA` / `MEDIA` / `ALTA` — cada una define un factor de velocidad punta y un factor de agarre. |
| `ModoConduccion` (enum) | `NORMAL` / `AGRESIVO` / `AHORRO` — cada uno define ritmo, variabilidad (consistencia) y desgaste de neumáticos. |
| `TipoNeumatico` (enum) | `BLANDO` / `MEDIO` / `DURO` / `INTERMEDIO` / `LLUVIA` — cada uno define ritmo propio, si es de lluvia, y ritmo de desgaste por vuelta. |
| `Clima` (enum) | `SECO` / `LLUVIOSO` / `EXTREMO` / `ALEATORIO` — cada uno afecta el tiempo de vuelta y la variabilidad. `ALEATORIO` se resuelve al momento de correr, con una distribución de probabilidad pareja (`resolver`) o ponderada por país del circuito (`resolverDinamico`, usada en Modo Campeonato — climas como Bélgica, Reino Unido, Japón o Brasil tienen más chance de lluvia; los desérticos como Baréin o Catar, casi siempre clima seco). |
| `RolPiloto` (enum) | `LIDER` / `ESCUDERO`. |
| `ResultadoCarrera` | El resultado de un piloto en una carrera simulada: posición, tiempo total, si tuvo DNF (y en qué punto/con qué rival chocó), tiempos por vuelta, desgaste de neumáticos por vuelta, en qué vueltas paró en boxes, temperaturas de llantas y motor por vuelta, velocidad máxima alcanzada. |
| `Campeonato` | Una temporada: calendario de circuitos, índice de la carrera actual, clima ya sorteado para esa carrera, y las tablas de puntos de pilotos y de equipos (se actualizan con `registrarResultado()` después de cada carrera). |
| `ValidacionException` | Excepción "controlada" para avisar de datos inválidos en los formularios, sin romper el programa. |

`SimuladorCarrera` (en `dominio/servicio`) es el motor que produce los `ResultadoCarrera` a partir de todo lo anterior.

## Motor de simulación

`SimuladorCarrera.simular(...)` calcula el resultado de una carrera combinando piloto, auto, clima y configuración con una fórmula de multiplicadores, más algo de variación aleatoria acotada para que no dé siempre el mismo resultado. A grandes rasgos:

1. **Tiempo base**: `vueltas × 24 segundos/km × longitud del circuito`.
2. **Por cada piloto**, ese tiempo base se ajusta multiplicando por (en orden):
   - **Habilidad del piloto** en el clima real de la carrera (hasta ±10% según qué tan lejos esté de 80/100).
   - **Experiencia**: bono de hasta -3% con 20+ años.
   - **Velocidad máxima** y **aceleración** del auto.
   - **Carga aerodinámica** (velocidad punta + agarre) y **modo de conducción** (ritmo) configurados.
   - **Compuesto de neumático**: acierto o error según si el compuesto es o no de lluvia y si el clima está mojado.
   - **Presión de aire**: en el valor óptimo no afecta nada; ciertas combinaciones de compuesto/modo/carga/clima favorecen presión alta o baja, y acertar esa dirección ayuda (equivocarla perjudica); si ninguna combinación aplica, el efecto es puro azar.
   - **Factor de tiempo del clima** (seco es el más rápido).
   - **Ruido aleatorio acotado** (una campana de Gauss recortada) cuya magnitud depende del modo de conducción (agresivo es más inconsistente, ahorro más parejo) y del clima.
3. Ese tiempo total se reparte en un **tiempo por vuelta promedio**, y luego se **simula vuelta por vuelta** (`simularVueltas`): pequeña variación de ritmo entre vueltas, desgaste de neumáticos acumulado (se acelera si el compuesto no corresponde al clima), temperaturas de llantas y motor variables, y **paradas en boxes automáticas** cuando el desgaste llega al 95% (pierde ~22 segundos y cambia de neumático, a uno adecuado si el anterior no lo era).
4. **Choques**: en 8 puntos de cada vuelta se evalúa el riesgo de choque de cada piloto todavía en carrera, según su habilidad de curva/recta (según el tramo real del trazado) combinada con adelantamiento; el riesgo nunca es exactamente cero y sube si el neumático está al límite de desgaste. Un choque puede arrastrar también al rival con el que el piloto esté peleando más de cerca en ese instante (ambos quedan DNF).
5. **Clasificación final**: los pilotos que terminan van antes que los DNF; entre los que terminan, ordena por tiempo total; entre los DNF, gana quien llegó más lejos antes de chocar.

El resultado (`ResultadoSimulacion`) incluye el clima real que salió (si se había elegido "Aleatorio") y la lista de `ResultadoCarrera` ya ordenada y con la posición asignada.

## Capa de UI

- **Navegación**: `GestorEscenas` (en `infraestructura/ui/util`) centraliza el cambio entre pantallas sobre un único `Stage`, manteniendo historial para poder volver atrás.
- **`ModuloGestionBase`**: layout base común para los módulos de gestión (circuitos/pilotos/equipos/vehículos), con una barra lateral de navegación entre sus sub-pantallas (listar/buscar/registrar/editar/eliminar).
- **Componentes reutilizables** (`ui/components`): `TarjetaOpcion` (tarjeta clicable del menú), `CampoBusqueda`, `BarraMiniIconos`, `FondoAnimado`.
- **Utilidades** (`ui/util`): `GestorImagenes` (carga y cachea imágenes), `GestorSonido` (música de fondo y efectos), `IconFactory` (logos e íconos dibujados/generados por código), `PistaGenerador` (dibuja proceduralmente la silueta del circuito y calcula sus curvas, usado tanto para elegir dónde puede ocurrir un choque como para la animación de la carrera), `FotosChoque` (elige las fotos a mostrar cuando hay un DNF).
- **Pantallas de carrera** (`ui/screens/carrera`): `SeleccionCarreraPane` (elegir circuito/clima/pilotos), `AnimacionCarreraPane` (dibuja la carrera en vivo sobre un `Canvas`, con los autos moviéndose sobre la silueta generada por `PistaGenerador`), `ResultadosCarreraPane` (tabla final).
- **Pantallas de campeonato** (`ui/screens/campeonato`): `BienvenidaCampeonatoPane`, `PantallaCampeonato` (orquesta las carreras del calendario), `TablaPosicionesPane` (clasificación de pilotos/equipos), `CampeonPane` (pantalla final con el campeón).

## Recursos (imágenes, audio, estilos)

Todo bajo `src/main/resources/`:
- `estilo.css`: hoja de estilos única para toda la aplicación JavaFX.
- `audio/`: música de fondo y efectos (click, confirmación, error, transición de escena, intro).
- `imagenes/`: logos de la app y de cada módulo, avatares predeterminados de piloto, fotos de los 20 pilotos reales incluidos por defecto, siluetas/fotos de escuderías y monoplazas reales, y fotos de choque (individual y grupal) que se muestran cuando un piloto queda DNF.

## Notas y limitaciones conocidas

- Las credenciales de MySQL están hardcodeadas en `ConexionMySQL.java` — para compartir el proyecto o subirlo a un repositorio público conviene moverlas a variables de entorno o un archivo de configuración ignorado por git.
- No hay script de creación del esquema de base de datos incluido; hay que crear las tablas a mano siguiendo las columnas usadas por los repositorios (ver [Base de datos](#base-de-datos)).
- Cada equipo solo puede tener **un** vehículo asignado (`registrarVehiculo` lo valida).
- No hay tests automatizados (`src/test/java` existe pero está vacío) — con los puertos ya definidos, sería fácil agregarle a `DataStore` tests unitarios usando implementaciones falsas (fakes/mocks) de los 4 repositorios, sin necesitar una MySQL real corriendo.
- No hay puertos/interfaces del lado de la UI (el "driving side"): las pantallas llaman directo a los métodos públicos de `DataStore`. Se optó por no agregar una interfaz ahí porque solo existe un adaptador de entrada (la UI de JavaFX); si el día de mañana se sumara, por ejemplo, una API REST además de la UI, ahí sí valdría la pena extraer esas interfaces.
