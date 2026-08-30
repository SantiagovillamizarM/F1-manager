//Mantiene en memoria (para que la UI de JavaFX reaccione sola) todo lo que hay en la base de
//datos MySQL: circuitos, pilotos, equipos y vehículos. Al arrancar, carga todo desde MySQL; cada
//operación de agregar/eliminar/configurar escribe también en MySQL, no solo en memoria.
package com.f1manager.infraestructura.persistencia;

import com.f1manager.dominio.excepcion.ValidacionException;
import com.f1manager.dominio.modelo.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

public final class DataStore {

    private static final DataStore INSTANCIA = new DataStore();

    private final ObservableList<Circuito> circuitos = FXCollections.observableArrayList();
    private final ObservableList<Piloto> pilotos = FXCollections.observableArrayList();
    private final ObservableList<Equipo> equipos = FXCollections.observableArrayList();
    private final ObservableList<Monoplaza> vehiculos = FXCollections.observableArrayList();

    private DataStore() {
        cargarDesdeBaseDeDatos();
    }

    public static DataStore getInstancia() {
        return INSTANCIA;
    }

    // =====================================================================
    // CARGA INICIAL DESDE MYSQL
    // =====================================================================

    private void cargarDesdeBaseDeDatos() {
        equipos.setAll(EquipoRepositorioMySQL.listarTodos());
        circuitos.setAll(CircuitoRepositorioMySQL.listarTodos());
        pilotos.setAll(PilotoRepositorioMySQL.listarTodos());
        vehiculos.setAll(VehiculoRepositorioMySQL.listarTodos());
    }

    // =====================================================================
    // CIRCUITOS
    // =====================================================================

    public ObservableList<Circuito> getCircuitos() {
        return circuitos;
    }

    public Circuito registrarCircuito(String nombre, String pais, String longitudTexto,
                                       String vueltasTexto, String descripcion) throws ValidacionException {
        if (esVacio(nombre) || esVacio(pais)) {
            throw new ValidacionException("El nombre y el país del circuito son obligatorios.");
        }
        double longitud = parsearDoublePositivo(longitudTexto, "La longitud debe ser un número mayor que 0.");
        int vueltas = parsearEnteroPositivo(vueltasTexto, "El número de vueltas debe ser un entero mayor que 0.");
        String desc = esVacio(descripcion) ? "Sin descripción disponible." : descripcion.trim();

        int id = CircuitoRepositorioMySQL.insertarYObtenerId(nombre.trim(), pais.trim(), longitud, vueltas, desc);
        Circuito circuito = new Circuito(id, nombre.trim(), pais.trim(), longitud, vueltas, desc);
        circuitos.add(circuito);
        return circuito;
    }

    public List<Circuito> buscarCircuitosPorPais(String pais) throws ValidacionException {
        if (esVacio(pais)) {
            throw new ValidacionException("Ingrese un país para buscar.");
        }
        String filtro = pais.trim().toLowerCase();
        return circuitos.stream()
                .filter(c -> c.getPais().toLowerCase().contains(filtro))
                .collect(Collectors.toList());
    }

    public void eliminarCircuito(String idTexto) throws ValidacionException {
        int id = parsearEnteroPositivo(idTexto, "Ingrese un ID numérico válido.");
        Circuito encontrado = circuitos.stream().filter(c -> c.getId() == id).findFirst()
                .orElseThrow(() -> new ValidacionException("No existe ningún circuito con el ID " + id + "."));
        CircuitoRepositorioMySQL.eliminar(id);
        circuitos.remove(encontrado);
    }

    // =====================================================================
    // PILOTOS
    // =====================================================================

    public ObservableList<Piloto> getPilotos() {
        return pilotos;
    }

    public List<Piloto> getPilotosPorEquipo(String nombreEquipo) {
        return pilotos.stream()
                .filter(p -> p.getEquipo().equalsIgnoreCase(nombreEquipo))
                .collect(Collectors.toList());
    }

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

        int id = PilotoRepositorioMySQL.insertarYObtenerId(nombre.trim(), equipo, rol, experiencia,
                curva, adelantamiento, recta, lluvia, seco, extremo, imagenUrl);
        Piloto piloto = new Piloto(id, nombre.trim(), equipo, rol, experiencia,
                curva, adelantamiento, recta, lluvia, seco, extremo);
        piloto.setImagenUrl(imagenUrl);
        pilotos.add(piloto);
        return piloto;
    }

    public void eliminarPiloto(String idTexto) throws ValidacionException {
        int id = parsearEnteroPositivo(idTexto, "Ingrese un ID numérico válido.");
        Piloto encontrado = pilotos.stream().filter(p -> p.getId() == id).findFirst()
                .orElseThrow(() -> new ValidacionException("No existe ningún piloto con el ID " + id + "."));
        PilotoRepositorioMySQL.eliminar(id);
        pilotos.remove(encontrado);
    }

    // =====================================================================
    // EQUIPOS
    // =====================================================================

    public ObservableList<Equipo> getEquipos() {
        return equipos;
    }

    public List<Integer> getIdsPilotosDeEquipo(String nombreEquipo) {
        return pilotos.stream()
                .filter(p -> p.getEquipo().equalsIgnoreCase(nombreEquipo))
                .map(Piloto::getId)
                .collect(Collectors.toList());
    }

    public Equipo registrarEquipo(String nombre, String pais, String motor) throws ValidacionException {
        if (esVacio(nombre) || esVacio(pais) || esVacio(motor)) {
            throw new ValidacionException("Nombre, país y motor son obligatorios.");
        }
        boolean existe = equipos.stream().anyMatch(e -> e.getNombre().equalsIgnoreCase(nombre.trim()));
        if (existe) {
            throw new ValidacionException("Ya existe un equipo registrado con ese nombre.");
        }
        Equipo equipo = new Equipo(nombre.trim(), pais.trim(), motor.trim());
        EquipoRepositorioMySQL.insertar(equipo);
        equipos.add(equipo);
        return equipo;
    }

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
        boolean tienePilotos = !getIdsPilotosDeEquipo(encontrado.getNombre()).isEmpty();
        boolean tieneVehiculo = getVehiculoPorEquipo(encontrado.getNombre()) != null;
        if (tienePilotos || tieneVehiculo) {
            throw new ValidacionException("No se puede eliminar \"" + encontrado.getNombre()
                    + "\": todavía tiene pilotos o un vehículo asignado. Elimínalos primero.");
        }

        EquipoRepositorioMySQL.eliminar(encontrado.getNombre());
        equipos.remove(encontrado);
    }

    // =====================================================================
    // VEHÍCULOS
    // =====================================================================

    public ObservableList<Monoplaza> getVehiculos() {
        return vehiculos;
    }

    public void configurarVehiculo(int idVehiculo, CargaAerodinamica carga, ModoConduccion modo,
                                    TipoNeumatico neumatico, String presionTexto) throws ValidacionException {
        Monoplaza vehiculo = vehiculos.stream().filter(v -> v.getId() == idVehiculo).findFirst()
                .orElseThrow(() -> new ValidacionException("El vehículo seleccionado no existe."));
        if (carga == null || modo == null || neumatico == null) {
            throw new ValidacionException("Debe seleccionar carga aerodinámica, modo de conducción y tipo de neumático.");
        }
        double presion = parsearPresion(presionTexto);
        vehiculo.setCargaAerodinamica(carga);
        vehiculo.setModoConduccion(modo);
        vehiculo.setTipoNeumatico(neumatico);
        vehiculo.setPresionAire(presion);
        VehiculoRepositorioMySQL.actualizar(vehiculo);
    }

    private static double parsearPresion(String texto) throws ValidacionException {
        String mensajeError = String.format("La presión de aire debe ser un número entre %.0f y %.0f PSI.",
                Monoplaza.PRESION_MINIMA, Monoplaza.PRESION_MAXIMA);
        if (esVacio(texto)) {
            throw new ValidacionException(mensajeError);
        }
        try {
            double valor = Double.parseDouble(texto.trim().replace(",", "."));
            if (valor < Monoplaza.PRESION_MINIMA || valor > Monoplaza.PRESION_MAXIMA) {
                throw new ValidacionException(mensajeError);
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new ValidacionException(mensajeError);
        }
    }

    public Monoplaza getVehiculoPorEquipo(String equipo) {
        return vehiculos.stream().filter(v -> v.getEquipo().equalsIgnoreCase(equipo)).findFirst().orElse(null);
    }

    // =====================================================================
    // UTILIDADES DE VALIDACIÓN
    // =====================================================================

    private static boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    private static double parsearDoublePositivo(String texto, String mensajeError) throws ValidacionException {
        if (esVacio(texto)) {
            throw new ValidacionException(mensajeError);
        }
        try {
            double valor = Double.parseDouble(texto.trim().replace(",", "."));
            if (valor <= 0) {
                throw new ValidacionException(mensajeError);
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new ValidacionException(mensajeError);
        }
    }

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

    private static int parsearHabilidad(String texto, String etiqueta) throws ValidacionException {
        int valor = parsearEnteroPositivo(texto, "La habilidad de " + etiqueta + " debe ser un entero entre 1 y 100.");
        if (valor > 100) {
            throw new ValidacionException("La habilidad de " + etiqueta + " debe estar entre 1 y 100.");
        }
        return valor;
    }

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
