//Mantiene en memoria (para que la UI de JavaFX reaccione sola) todo lo que hay en la base de
//datos MySQL: circuitos, pilotos, equipos y vehículos. Al arrancar, carga todo desde MySQL; cada
//operación de agregar/eliminar/configurar escribe también en MySQL, no solo en memoria.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.persistencia;

//Trae ValidacionException, el error "controlado" que se lanza cuando el usuario ingresa datos inválidos
import com.f1manager.dominio.excepcion.ValidacionException;
//Trae con el asterisco (*) TODAS las clases del paquete dominio.modelo (Circuito, Piloto, Equipo, Monoplaza, etc.) sin tener que importarlas una por una
import com.f1manager.dominio.modelo.*;
//Trae FXCollections, la fábrica de JavaFX que crea listas especiales "observables"
import javafx.collections.FXCollections;
//Trae ObservableList, una lista de JavaFX que avisa sola a la interfaz cuando algo se agrega o se quita, para que la pantalla se actualice sin código extra
import javafx.collections.ObservableList;

//Importa la interfaz List, que define el comportamiento general de una lista en Java (sirve como plantilla para clases como ArrayList)
import java.util.List;
//Trae Collectors, que ayuda a "recolectar" el resultado de un stream y convertirlo de nuevo en una lista normal (collect(Collectors.toList()))
import java.util.stream.Collectors;

//Una clase publica y final (no se puede heredar) llamada "DataStore"
public final class DataStore {

    //Variable privada, fija y de la clase (static) que guarda la única instancia de DataStore que va a existir en todo el programa (patrón Singleton)
    private static final DataStore INSTANCIA = new DataStore();

    //Lista observable privada y fija de objetos Circuito: guarda todos los circuitos que hay en memoria y avisa a la UI cuando cambian
    private final ObservableList<Circuito> circuitos = FXCollections.observableArrayList();
    //Lista observable privada y fija de objetos Piloto
    private final ObservableList<Piloto> pilotos = FXCollections.observableArrayList();
    //Lista observable privada y fija de objetos Equipo
    private final ObservableList<Equipo> equipos = FXCollections.observableArrayList();
    //Lista observable privada y fija de objetos Monoplaza (los vehículos)
    private final ObservableList<Monoplaza> vehiculos = FXCollections.observableArrayList();

    //Constructor privado (por ser Singleton nadie más puede crear un DataStore, solo se usa el de INSTANCIA de arriba)
    //Apenas se crea, carga todos los datos guardados en MySQL hacia las listas de memoria
    private DataStore() {
        cargarDesdeBaseDeDatos();
    }

    //Getter, pero de tipo static: devuelve siempre la misma instancia única de DataStore (así todo el programa comparte los mismos datos en memoria)
    public static DataStore getInstancia() {
        return INSTANCIA;
    }

    // =====================================================================
    // CARGA INICIAL DESDE MYSQL
    // =====================================================================

    //Este método pide a cada repositorio (Equipo, Circuito, Piloto, Vehiculo) su lista completa desde MySQL
    //y la mete de una vez en la lista observable correspondiente con setAll() (reemplaza todo el contenido anterior)
    private void cargarDesdeBaseDeDatos() {
        equipos.setAll(EquipoRepositorioMySQL.listarTodos());
        circuitos.setAll(CircuitoRepositorioMySQL.listarTodos());
        pilotos.setAll(PilotoRepositorioMySQL.listarTodos());
        vehiculos.setAll(VehiculoRepositorioMySQL.listarTodos());
    }

    // =====================================================================
    // CIRCUITOS
    // =====================================================================

    //Getter
    public ObservableList<Circuito> getCircuitos() {
        return circuitos;
    }

    //Este método valida los datos que llegan como texto (desde un formulario de la UI), y si están bien,
    //crea un Circuito nuevo, lo guarda en MySQL y lo agrega también a la lista en memoria.
    public Circuito registrarCircuito(String nombre, String pais, String longitudTexto,
                                       String vueltasTexto, String descripcion) throws ValidacionException {
        //Si el nombre o el país vienen vacíos, se corta acá lanzando el error de validación
        if (esVacio(nombre) || esVacio(pais)) {
            throw new ValidacionException("El nombre y el país del circuito son obligatorios.");
        }
        //Convierte el texto de longitud a un número double, validando que sea mayor que 0
        double longitud = parsearDoublePositivo(longitudTexto, "La longitud debe ser un número mayor que 0.");
        //Convierte el texto de vueltas a un número entero, validando que sea mayor que 0
        int vueltas = parsearEnteroPositivo(vueltasTexto, "El número de vueltas debe ser un entero mayor que 0.");
        //Operador ternario: si la descripción viene vacía, se pone un texto por defecto; si no, se usa la que escribió el usuario (sin espacios sobrantes)
        String desc = esVacio(descripcion) ? "Sin descripción disponible." : descripcion.trim();

        //Primero se guarda en MySQL (así se obtiene el id real que le puso la base de datos)
        int id = CircuitoRepositorioMySQL.insertarYObtenerId(nombre.trim(), pais.trim(), longitud, vueltas, desc);
        //Se arma el objeto Circuito en memoria con ese mismo id
        Circuito circuito = new Circuito(id, nombre.trim(), pais.trim(), longitud, vueltas, desc);
        //Se agrega a la lista observable para que la UI se actualice sola
        circuitos.add(circuito);
        return circuito;
    }

    //Busca en la lista de circuitos en memoria todos los que su país contenga el texto ingresado (sin importar mayúsculas/minúsculas)
    public List<Circuito> buscarCircuitosPorPais(String pais) throws ValidacionException {
        if (esVacio(pais)) {
            throw new ValidacionException("Ingrese un país para buscar.");
        }
        //Pasa el filtro a minúsculas para que la búsqueda no distinga mayúsculas de minúsculas
        String filtro = pais.trim().toLowerCase();
        //Pone los circuitos en un stream (una especie de cinta transportadora) para poder filtrarlos fácilmente
        return circuitos.stream()
                //Se queda solo con los que el país (también en minúsculas) contenga el texto buscado
                .filter(c -> c.getPais().toLowerCase().contains(filtro))
                //Cierra el stream y junta el resultado de nuevo en una List normal
                .collect(Collectors.toList());
    }

    //Busca el circuito por su id, valida los nuevos datos, y si todo está bien los actualiza tanto en memoria como en MySQL
    public void editarCircuito(String idTexto, String nombre, String pais, String longitudTexto,
                                String vueltasTexto, String descripcion) throws ValidacionException {
        //Convierte el id de texto a número
        int id = parsearEnteroPositivo(idTexto, "Ingrese un ID numérico válido.");
        //Busca el circuito con ese id dentro de la lista en memoria; si no lo encuentra, lanza el error dentro del orElseThrow
        Circuito circuito = circuitos.stream().filter(c -> c.getId() == id).findFirst()
                .orElseThrow(() -> new ValidacionException("No existe ningún circuito con el ID " + id + "."));
        if (esVacio(nombre) || esVacio(pais)) {
            throw new ValidacionException("El nombre y el país del circuito son obligatorios.");
        }
        double longitud = parsearDoublePositivo(longitudTexto, "La longitud debe ser un número mayor que 0.");
        int vueltas = parsearEnteroPositivo(vueltasTexto, "El número de vueltas debe ser un entero mayor que 0.");
        String desc = esVacio(descripcion) ? "Sin descripción disponible." : descripcion.trim();

        //Se sobreescriben los datos del objeto que ya estaba en memoria (los setters van cambiando cada campo)
        circuito.setNombre(nombre.trim());
        circuito.setPais(pais.trim());
        circuito.setLongitudKm(longitud);
        circuito.setVueltas(vueltas);
        circuito.setDescripcion(desc);
        //Y se manda a guardar ese mismo objeto (ya actualizado) también en MySQL
        CircuitoRepositorioMySQL.actualizar(circuito);
    }

    //Busca el circuito por id y, si existe, lo borra tanto de MySQL como de la lista en memoria
    public void eliminarCircuito(String idTexto) throws ValidacionException {
        int id = parsearEnteroPositivo(idTexto, "Ingrese un ID numérico válido.");
        Circuito encontrado = circuitos.stream().filter(c -> c.getId() == id).findFirst()
                .orElseThrow(() -> new ValidacionException("No existe ningún circuito con el ID " + id + "."));
        //Primero se borra de la base de datos
        CircuitoRepositorioMySQL.eliminar(id);
        //Y despues se quita también de la lista observable en memoria
        circuitos.remove(encontrado);
    }

    // =====================================================================
    // PILOTOS
    // =====================================================================

    //Getter
    public ObservableList<Piloto> getPilotos() {
        return pilotos;
    }

    //Filtra en memoria y devuelve solo los pilotos que pertenecen al equipo indicado (sin importar mayúsculas/minúsculas)
    public List<Piloto> getPilotosPorEquipo(String nombreEquipo) {
        return pilotos.stream()
                .filter(p -> p.getEquipo().equalsIgnoreCase(nombreEquipo))
                .collect(Collectors.toList());
    }

    //Valida todos los datos del formulario de piloto (nombre, equipo, rol y las 6 habilidades) y, si todo está bien,
    //crea el Piloto nuevo, lo guarda en MySQL y lo agrega también a la lista en memoria.
    public Piloto registrarPiloto(String nombre, String equipo, RolPiloto rol, String experienciaTexto,
                                   String curvaTexto, String adelantamientoTexto, String rectaTexto,
                                   String lluviaTexto, String secoTexto, String extremoTexto,
                                   String imagenUrl) throws ValidacionException {
        if (esVacio(nombre)) {
            throw new ValidacionException("El nombre del piloto es obligatorio.");
        }
        if (esVacio(equipo)) {
            throw new ValidacionException("Debe seleccionar un equipo para el piloto.");
        }
        //anyMatch recorre la lista de equipos y devuelve true en cuanto encuentra uno cuyo nombre coincida (ignorando mayúsculas/minúsculas)
        boolean equipoExiste = equipos.stream().anyMatch(e -> e.getNombre().equalsIgnoreCase(equipo));
        if (!equipoExiste) {
            throw new ValidacionException("El equipo seleccionado no existe.");
        }
        if (rol == null) {
            throw new ValidacionException("Debe seleccionar un rol para el piloto.");
        }
        //Convierte y valida cada campo numérico de texto a su tipo real (entero >= 0 para experiencia, entero entre 1 y 100 para cada habilidad)
        int experiencia = parsearEnteroNoNegativo(experienciaTexto, "Los años de experiencia deben ser un entero mayor o igual a 0.");
        int curva = parsearHabilidad(curvaTexto, "curva");
        int adelantamiento = parsearHabilidad(adelantamientoTexto, "adelantamiento");
        int recta = parsearHabilidad(rectaTexto, "recta");
        int lluvia = parsearHabilidad(lluviaTexto, "lluvia");
        int seco = parsearHabilidad(secoTexto, "seco");
        int extremo = parsearHabilidad(extremoTexto, "clima extremo");

        //Primero se guarda en MySQL (así se obtiene el id real que le puso la base de datos)
        int id = PilotoRepositorioMySQL.insertarYObtenerId(nombre.trim(), equipo, rol, experiencia,
                curva, adelantamiento, recta, lluvia, seco, extremo, imagenUrl);
        //Se arma el objeto Piloto en memoria con ese mismo id
        Piloto piloto = new Piloto(id, nombre.trim(), equipo, rol, experiencia,
                curva, adelantamiento, recta, lluvia, seco, extremo);
        piloto.setImagenUrl(imagenUrl);
        //Se agrega a la lista observable para que la UI se actualice sola
        pilotos.add(piloto);
        return piloto;
    }

    //Busca el piloto por su id, valida los nuevos datos, y si todo está bien los actualiza tanto en memoria como en MySQL
    public void editarPiloto(String idTexto, String nombre, String equipo, RolPiloto rol, String experienciaTexto,
                              String curvaTexto, String adelantamientoTexto, String rectaTexto,
                              String lluviaTexto, String secoTexto, String extremoTexto,
                              String imagenUrl) throws ValidacionException {
        int id = parsearEnteroPositivo(idTexto, "Ingrese un ID numérico válido.");
        //Busca el piloto con ese id dentro de la lista en memoria; si no lo encuentra, lanza el error dentro del orElseThrow
        Piloto piloto = pilotos.stream().filter(p -> p.getId() == id).findFirst()
                .orElseThrow(() -> new ValidacionException("No existe ningún piloto con el ID " + id + "."));
        if (esVacio(nombre)) {
            throw new ValidacionException("El nombre del piloto es obligatorio.");
        }
        if (esVacio(equipo)) {
            throw new ValidacionException("Debe seleccionar un equipo para el piloto.");
        }
        boolean equipoExiste = equipos.stream().anyMatch(e -> e.getNombre().equalsIgnoreCase(equipo));
        if (!equipoExiste) {
            throw new ValidacionException("El equipo seleccionado no existe.");
        }
        if (rol == null) {
            throw new ValidacionException("Debe seleccionar un rol para el piloto.");
        }
        int experiencia = parsearEnteroNoNegativo(experienciaTexto, "Los años de experiencia deben ser un entero mayor o igual a 0.");
        int curva = parsearHabilidad(curvaTexto, "curva");
        int adelantamiento = parsearHabilidad(adelantamientoTexto, "adelantamiento");
        int recta = parsearHabilidad(rectaTexto, "recta");
        int lluvia = parsearHabilidad(lluviaTexto, "lluvia");
        int seco = parsearHabilidad(secoTexto, "seco");
        int extremo = parsearHabilidad(extremoTexto, "clima extremo");

        //Se sobreescriben los datos del objeto que ya estaba en memoria (los setters van cambiando cada campo)
        piloto.setNombre(nombre.trim());
        piloto.setEquipo(equipo);
        piloto.setRol(rol);
        piloto.setExperienciaAnios(experiencia);
        piloto.setHabilidadCurva(curva);
        piloto.setHabilidadAdelantamiento(adelantamiento);
        piloto.setHabilidadRecta(recta);
        piloto.setHabilidadLluvia(lluvia);
        piloto.setHabilidadSeco(seco);
        piloto.setHabilidadExtremo(extremo);
        piloto.setImagenUrl(imagenUrl);
        //Y se manda a guardar ese mismo objeto (ya actualizado) también en MySQL
        PilotoRepositorioMySQL.actualizar(piloto);
    }

    //Busca el piloto por id y, si existe, lo borra tanto de MySQL como de la lista en memoria
    public void eliminarPiloto(String idTexto) throws ValidacionException {
        int id = parsearEnteroPositivo(idTexto, "Ingrese un ID numérico válido.");
        Piloto encontrado = pilotos.stream().filter(p -> p.getId() == id).findFirst()
                .orElseThrow(() -> new ValidacionException("No existe ningún piloto con el ID " + id + "."));
        //Primero se borra de la base de datos
        PilotoRepositorioMySQL.eliminar(id);
        //Y despues se quita también de la lista observable en memoria
        pilotos.remove(encontrado);
    }

    // =====================================================================
    // EQUIPOS
    // =====================================================================

    //Getter
    public ObservableList<Equipo> getEquipos() {
        return equipos;
    }

    //Filtra los pilotos en memoria que pertenecen al equipo indicado y devuelve solo sus ids (usando map para transformar cada Piloto en su id)
    public List<Integer> getIdsPilotosDeEquipo(String nombreEquipo) {
        return pilotos.stream()
                .filter(p -> p.getEquipo().equalsIgnoreCase(nombreEquipo))
                //map() transforma cada elemento del stream: aquí convierte cada Piloto en su Integer (el id), usando referencia a método (Piloto::getId)
                .map(Piloto::getId)
                .collect(Collectors.toList());
    }

    //Valida los datos del formulario de equipo (nombre, país y motor obligatorios, y que el nombre no esté repetido) y,
    //si todo está bien, crea el Equipo nuevo, lo guarda en MySQL y lo agrega también a la lista en memoria.
    public Equipo registrarEquipo(String nombre, String pais, String motor, String imagenUrl) throws ValidacionException {
        if (esVacio(nombre) || esVacio(pais) || esVacio(motor)) {
            throw new ValidacionException("Nombre, país y motor son obligatorios.");
        }
        //anyMatch busca si ya hay algún equipo con ese mismo nombre (ignorando mayúsculas/minúsculas) para no dejar duplicados
        boolean existe = equipos.stream().anyMatch(e -> e.getNombre().equalsIgnoreCase(nombre.trim()));
        if (existe) {
            throw new ValidacionException("Ya existe un equipo registrado con ese nombre.");
        }
        Equipo equipo = new Equipo(nombre.trim(), pais.trim(), motor.trim());
        equipo.setImagenUrl(imagenUrl);
        //Se guarda primero en MySQL...
        EquipoRepositorioMySQL.insertar(equipo);
        //...y luego se agrega a la lista observable para que la UI se actualice sola
        equipos.add(equipo);
        return equipo;
    }

    //Busca el equipo por nombre y, si no tiene pilotos ni vehículo asignado, lo borra tanto de MySQL como de la lista en memoria
    public void eliminarEquipo(String nombreTexto) throws ValidacionException {
        if (esVacio(nombreTexto)) {
            throw new ValidacionException("Ingrese el nombre del equipo a eliminar.");
        }
        String nombre = nombreTexto.trim();
        Equipo encontrado = equipos.stream().filter(e -> e.getNombre().equalsIgnoreCase(nombre)).findFirst()
                .orElseThrow(() -> new ValidacionException("No existe ningún equipo llamado \"" + nombre + "\"."));

        // Se valida en Java antes de tocar MySQL: si no, la restricción de llave foránea
        // (pilotos/vehiculos -> equipos) rechaza el DELETE y sube como una excepción cruda,
        // sin un mensaje que el usuario pueda entender.
        //Osea, primero revisamos aquí en Java si el equipo tiene pilotos o vehículo, para poder avisarle
        //al usuario con un mensaje claro, en vez de dejar que MySQL rechace el borrado con un error feo
        boolean tienePilotos = !getIdsPilotosDeEquipo(encontrado.getNombre()).isEmpty();
        boolean tieneVehiculo = getVehiculoPorEquipo(encontrado.getNombre()) != null;
        if (tienePilotos || tieneVehiculo) {
            throw new ValidacionException("No se puede eliminar \"" + encontrado.getNombre()
                    + "\": todavía tiene pilotos o un vehículo asignado. Elimínalos primero.");
        }

        //Primero se borra de la base de datos
        EquipoRepositorioMySQL.eliminar(encontrado.getNombre());
        //Y despues se quita también de la lista observable en memoria
        equipos.remove(encontrado);
    }

    // =====================================================================
    // VEHÍCULOS
    // =====================================================================

    //Getter
    public ObservableList<Monoplaza> getVehiculos() {
        return vehiculos;
    }

    //Valida los datos del formulario de vehículo (modelo/equipo/motor obligatorios, que el equipo exista y que no tenga ya un vehículo)
    //y, si todo está bien, crea el Monoplaza nuevo, lo guarda en MySQL y lo agrega también a la lista en memoria.
    public Monoplaza registrarVehiculo(String modelo, String equipo, String motor,
                                        String velocidadTexto, String aceleracionTexto) throws ValidacionException {
        if (esVacio(modelo) || esVacio(equipo) || esVacio(motor)) {
            throw new ValidacionException("Modelo, equipo y motor son obligatorios.");
        }
        boolean equipoExiste = equipos.stream().anyMatch(e -> e.getNombre().equalsIgnoreCase(equipo));
        if (!equipoExiste) {
            throw new ValidacionException("El equipo seleccionado no existe.");
        }
        //Cada equipo solo puede tener un vehículo: si buscando por equipo ya aparece uno, se corta acá
        if (getVehiculoPorEquipo(equipo) != null) {
            throw new ValidacionException("El equipo \"" + equipo + "\" ya tiene un vehículo asignado.");
        }
        //Convierte y valida velocidad máxima y aceleración según los rangos que permite el reglamento (ver los métodos parsearVelocidad/parsearAceleracion más abajo)
        double velocidad = parsearVelocidad(velocidadTexto);
        double aceleracion = parsearAceleracion(aceleracionTexto);

        //Primero se guarda en MySQL (así se obtiene el id real que le puso la base de datos)
        int id = VehiculoRepositorioMySQL.insertarYObtenerId(modelo.trim(), equipo, motor.trim(), velocidad, aceleracion);
        //Se arma el objeto Monoplaza en memoria con ese mismo id
        Monoplaza vehiculo = new Monoplaza(id, modelo.trim(), equipo, motor.trim(), velocidad, aceleracion);
        //Se agrega a la lista observable para que la UI se actualice sola
        vehiculos.add(vehiculo);
        return vehiculo;
    }

    // Antes de arrancar una carrera: si algún equipo con pilotos asignados no tiene ningún
    // vehículo, esa carrera lo simularía con velocidad 0 y sin neumático — se bloquea aquí en vez
    // de dejarlo llegar a la simulación.
    //Recorre todos los equipos y, si alguno tiene pilotos pero no tiene vehículo, lanza el error de validación de una vez (corta el bucle apenas encuentra el problema)
    public void validarEquiposListosParaCarrera() throws ValidacionException {
        for (Equipo equipo : equipos) {
            boolean tienePilotos = !getIdsPilotosDeEquipo(equipo.getNombre()).isEmpty();
            if (tienePilotos && getVehiculoPorEquipo(equipo.getNombre()) == null) {
                throw new ValidacionException("El equipo \"" + equipo.getNombre()
                        + "\" tiene pilotos pero no tiene ningún vehículo asignado. Regístrale uno en Gestión de Vehículos antes de correr.");
            }
        }
    }

    //Busca el vehículo por id, valida la configuración elegida (carga, modo, neumático, presión) y la actualiza tanto en memoria como en MySQL
    public void configurarVehiculo(int idVehiculo, CargaAerodinamica carga, ModoConduccion modo,
                                    TipoNeumatico neumatico, String presionTexto) throws ValidacionException {
        Monoplaza vehiculo = vehiculos.stream().filter(v -> v.getId() == idVehiculo).findFirst()
                .orElseThrow(() -> new ValidacionException("El vehículo seleccionado no existe."));
        if (carga == null || modo == null || neumatico == null) {
            throw new ValidacionException("Debe seleccionar carga aerodinámica, modo de conducción y tipo de neumático.");
        }
        double presion = parsearPresion(presionTexto);
        //Se sobreescribe la configuración del objeto que ya estaba en memoria
        vehiculo.setCargaAerodinamica(carga);
        vehiculo.setModoConduccion(modo);
        vehiculo.setTipoNeumatico(neumatico);
        vehiculo.setPresionAire(presion);
        //Y se manda a guardar esa misma configuración también en MySQL
        VehiculoRepositorioMySQL.actualizar(vehiculo);
    }

    //Convierte el texto de presión a double y valida que esté dentro del rango permitido (entre PRESION_MINIMA y PRESION_MAXIMA)
    private static double parsearPresion(String texto) throws ValidacionException {
        //String.format arma el mensaje de error metiendo los números mínimo y máximo dentro del texto (%.0f es "número decimal sin decimales")
        String mensajeError = String.format("La presión de aire debe ser un número entre %.0f y %.0f PSI.",
                Monoplaza.PRESION_MINIMA, Monoplaza.PRESION_MAXIMA);
        if (esVacio(texto)) {
            throw new ValidacionException(mensajeError);
        }
        //try/catch porque parseDouble revienta con NumberFormatException si el texto no es un número válido (ej. si el usuario escribió letras)
        try {
            //replace(",", ".") deja pasar tanto "1,5" como "1.5" como número decimal válido
            double valor = Double.parseDouble(texto.trim().replace(",", "."));
            if (valor < Monoplaza.PRESION_MINIMA || valor > Monoplaza.PRESION_MAXIMA) {
                throw new ValidacionException(mensajeError);
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new ValidacionException(mensajeError);
        }
    }

    //Convierte el texto de velocidad máxima a double y valida que esté dentro del rango que permite el reglamento FIA
    private static double parsearVelocidad(String texto) throws ValidacionException {
        String mensajeError = String.format("La velocidad máxima debe ser un número entre %.0f y %.0f km/h (reglamento FIA para monoplazas actuales).",
                Monoplaza.VELOCIDAD_MINIMA_KMH, Monoplaza.VELOCIDAD_MAXIMA_KMH);
        if (esVacio(texto)) {
            throw new ValidacionException(mensajeError);
        }
        try {
            double valor = Double.parseDouble(texto.trim().replace(",", "."));
            if (valor < Monoplaza.VELOCIDAD_MINIMA_KMH || valor > Monoplaza.VELOCIDAD_MAXIMA_KMH) {
                throw new ValidacionException(mensajeError);
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new ValidacionException(mensajeError);
        }
    }

    //Convierte el texto de aceleración 0-100 km/h a double y valida que esté dentro del rango que permite el reglamento FIA
    private static double parsearAceleracion(String texto) throws ValidacionException {
        String mensajeError = String.format("La aceleración 0-100 km/h debe ser un número entre %.1f y %.1f segundos (reglamento FIA para monoplazas actuales).",
                Monoplaza.ACELERACION_MINIMA_S, Monoplaza.ACELERACION_MAXIMA_S);
        if (esVacio(texto)) {
            throw new ValidacionException(mensajeError);
        }
        try {
            double valor = Double.parseDouble(texto.trim().replace(",", "."));
            if (valor < Monoplaza.ACELERACION_MINIMA_S || valor > Monoplaza.ACELERACION_MAXIMA_S) {
                throw new ValidacionException(mensajeError);
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new ValidacionException(mensajeError);
        }
    }

    //Busca en memoria el vehículo que pertenece al equipo indicado; si no hay ninguno, devuelve null (orElse(null))
    public Monoplaza getVehiculoPorEquipo(String equipo) {
        return vehiculos.stream().filter(v -> v.getEquipo().equalsIgnoreCase(equipo)).findFirst().orElse(null);
    }

    // =====================================================================
    // UTILIDADES DE VALIDACIÓN
    // =====================================================================

    //Revisa si un texto está vacío: cuenta como vacío si es null (no existe) o si al quitarle los espacios de los lados no queda nada
    private static boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    //Convierte un texto a double y valida que sea mayor que 0, usando el mensaje de error que le pasen por parámetro
    private static double parsearDoublePositivo(String texto, String mensajeError) throws ValidacionException {
        if (esVacio(texto)) {
            throw new ValidacionException(mensajeError);
        }
        try {
            //replace(",", ".") deja pasar tanto "1,5" como "1.5" como número decimal válido
            double valor = Double.parseDouble(texto.trim().replace(",", "."));
            if (valor <= 0) {
                throw new ValidacionException(mensajeError);
            }
            return valor;
        } catch (NumberFormatException e) {
            //Si el texto no era un número válido, parseDouble lanza esta excepción y acá se convierte en el error de validación con mensaje claro
            throw new ValidacionException(mensajeError);
        }
    }

    //Convierte un texto a entero y valida que sea mayor que 0
    private static int parsearEnteroPositivo(String texto, String mensajeError) throws ValidacionException {
        if (esVacio(texto)) {
            throw new ValidacionException(mensajeError);
        }
        try {
            int valor = Integer.parseInt(texto.trim());
            if (valor <= 0) {
                throw new ValidacionException(mensajeError);
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new ValidacionException(mensajeError);
        }
    }

    //Convierte un texto de habilidad a entero y valida que esté entre 1 y 100 (reutiliza parsearEnteroPositivo para la parte de "mayor que 0")
    private static int parsearHabilidad(String texto, String etiqueta) throws ValidacionException {
        int valor = parsearEnteroPositivo(texto, "La habilidad de " + etiqueta + " debe ser un entero entre 1 y 100.");
        if (valor > 100) {
            throw new ValidacionException("La habilidad de " + etiqueta + " debe estar entre 1 y 100.");
        }
        return valor;
    }

    //Convierte un texto a entero y valida que sea mayor o igual a 0 (a diferencia de parsearEnteroPositivo, aquí el 0 sí es válido)
    private static int parsearEnteroNoNegativo(String texto, String mensajeError) throws ValidacionException {
        if (esVacio(texto)) {
            throw new ValidacionException(mensajeError);
        }
        try {
            int valor = Integer.parseInt(texto.trim());
            if (valor < 0) {
                throw new ValidacionException(mensajeError);
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new ValidacionException(mensajeError);
        }
    }
}
