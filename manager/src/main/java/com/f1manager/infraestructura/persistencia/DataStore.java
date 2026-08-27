//Guarda toda la información del programa (todos los circuitos, pilotos, equipos y vehículos) 
//mientras está abierto, y contiene las funciones para agregar, buscar y eliminar cada cosa, 
// validando que los datos ingresados sean correctos.
package com.f1manager.infraestructura.persistencia;

import com.f1manager.dominio.excepcion.ValidacionException;
import com.f1manager.dominio.modelo.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class DataStore {

    private static final DataStore INSTANCIA = new DataStore();

    private final ObservableList<Circuito> circuitos = FXCollections.observableArrayList();
    private final ObservableList<Piloto> pilotos = FXCollections.observableArrayList();
    private final ObservableList<Equipo> equipos = FXCollections.observableArrayList();
    private final ObservableList<Monoplaza> vehiculos = FXCollections.observableArrayList();

    private final AtomicInteger idCircuitos = new AtomicInteger(1);
    private final AtomicInteger idPilotos = new AtomicInteger(1);
    private final AtomicInteger idVehiculos = new AtomicInteger(1);

    private DataStore() {
        cargarDatosIniciales();
    }

    public static DataStore getInstancia() {
        return INSTANCIA;
    }

    // =====================================================================
    // DATOS INICIALES
    // =====================================================================

    private void cargarDatosIniciales() {
        // ---- Equipos ----
        equipos.add(new Equipo("Mercedes-AMG Petronas", "Alemania", "Mercedes"));
        equipos.add(new Equipo("Scuderia Ferrari", "Italia", "Ferrari"));
        equipos.add(new Equipo("Red Bull Racing", "Austria", "Honda RBPT"));
        equipos.add(new Equipo("McLaren", "Reino Unido", "Mercedes"));
        equipos.add(new Equipo("Aston Martin", "Reino Unido", "Mercedes"));
        equipos.add(new Equipo("Alpine", "Francia", "Renault"));

        // ---- Pilotos ----
        pilotos.add(new Piloto(sigId(idPilotos), "Lewis Hamilton", "Mercedes-AMG Petronas", RolPiloto.LIDER, 18, 96));
        pilotos.add(new Piloto(sigId(idPilotos), "George Russell", "Mercedes-AMG Petronas", RolPiloto.ESCUDERO, 5, 88));
        pilotos.add(new Piloto(sigId(idPilotos), "Charles Leclerc", "Scuderia Ferrari", RolPiloto.LIDER, 7, 93));
        pilotos.add(new Piloto(sigId(idPilotos), "Carlos Sainz", "Scuderia Ferrari", RolPiloto.ESCUDERO, 9, 89));
        pilotos.add(new Piloto(sigId(idPilotos), "Max Verstappen", "Red Bull Racing", RolPiloto.LIDER, 10, 99));
        pilotos.add(new Piloto(sigId(idPilotos), "Sergio Pérez", "Red Bull Racing", RolPiloto.ESCUDERO, 13, 85));
        pilotos.add(new Piloto(sigId(idPilotos), "Lando Norris", "McLaren", RolPiloto.LIDER, 6, 91));
        pilotos.add(new Piloto(sigId(idPilotos), "Oscar Piastri", "McLaren", RolPiloto.ESCUDERO, 2, 86));
        pilotos.add(new Piloto(sigId(idPilotos), "Fernando Alonso", "Aston Martin", RolPiloto.LIDER, 22, 92));
        pilotos.add(new Piloto(sigId(idPilotos), "Lance Stroll", "Aston Martin", RolPiloto.ESCUDERO, 7, 78));
        pilotos.add(new Piloto(sigId(idPilotos), "Pierre Gasly", "Alpine", RolPiloto.LIDER, 8, 84));
        pilotos.add(new Piloto(sigId(idPilotos), "Esteban Ocon", "Alpine", RolPiloto.ESCUDERO, 8, 82));

        // ---- Circuitos ----
        circuitos.add(new Circuito(sigId(idCircuitos), "Circuit de Monaco", "Mónaco", 3.337, 78,
                "Trazado urbano estrecho y sinuoso por las calles de Montecarlo. Prioriza el "
                        + "monoplaza y la precisión del piloto por encima de la potencia bruta."));
        circuitos.add(new Circuito(sigId(idCircuitos), "Silverstone Circuit", "Reino Unido", 5.891, 52,
                "Circuito rápido y fluido, cuna de la Fórmula 1, con curvas de alta velocidad "
                        + "como Copse y Maggotts-Becketts."));
        circuitos.add(new Circuito(sigId(idCircuitos), "Nürburgring", "Alemania", 5.148, 60,
                "Trazado técnico y exigente, célebre por su combinación de curvas rápidas y "
                        + "sectores lentos que ponen a prueba el equilibrio del monoplaza."));
        circuitos.add(new Circuito(sigId(idCircuitos), "Autodromo Nazionale Monza", "Italia", 5.793, 53,
                "El templo de la velocidad. Rectas larguísimas donde la baja carga aerodinámica "
                        + "y la potencia del motor marcan la diferencia."));
        circuitos.add(new Circuito(sigId(idCircuitos), "Circuit de Spa-Francorchamps", "Bélgica", 7.004, 44,
                "Uno de los circuitos más largos y espectaculares del calendario, con el "
                        + "legendario sector de Eau Rouge-Raidillon."));
        circuitos.add(new Circuito(sigId(idCircuitos), "Suzuka International Racing Course", "Japón", 5.807, 53,
                "Trazado en forma de ocho, técnico y muy respetado por los pilotos por su "
                        + "fluidez y exigencia física."));

        // ---- Monoplazas ----
        vehiculos.add(new Monoplaza(sigId(idVehiculos), "W15", "Mercedes-AMG Petronas", "Mercedes", 345, 2.6));
        vehiculos.add(new Monoplaza(sigId(idVehiculos), "SF-24", "Scuderia Ferrari", "Ferrari", 348, 2.5));
        vehiculos.add(new Monoplaza(sigId(idVehiculos), "RB20", "Red Bull Racing", "Honda RBPT", 352, 2.4));
        vehiculos.add(new Monoplaza(sigId(idVehiculos), "MCL38", "McLaren", "Mercedes", 347, 2.5));
        vehiculos.add(new Monoplaza(sigId(idVehiculos), "AMR24", "Aston Martin", "Mercedes", 343, 2.7));
        vehiculos.add(new Monoplaza(sigId(idVehiculos), "A524", "Alpine", "Renault", 340, 2.8));
    }

    private static int sigId(AtomicInteger contador) {
        return contador.getAndIncrement();
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

        Circuito circuito = new Circuito(sigId(idCircuitos), nombre.trim(), pais.trim(), longitud, vueltas, desc);
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

    public Piloto registrarPiloto(String nombre, String equipo, RolPiloto rol,
                                   String experienciaTexto, String habilidadTexto) throws ValidacionException {
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
        int habilidad = parsearEnteroPositivo(habilidadTexto, "La habilidad debe ser un entero entre 1 y 100.");
        if (habilidad < 1 || habilidad > 100) {
            throw new ValidacionException("La habilidad debe estar entre 1 y 100.");
        }

        Piloto piloto = new Piloto(sigId(idPilotos), nombre.trim(), equipo, rol, experiencia, habilidad);
        pilotos.add(piloto);
        return piloto;
    }

    public void eliminarPiloto(String idTexto) throws ValidacionException {
        int id = parsearEnteroPositivo(idTexto, "Ingrese un ID numérico válido.");
        Piloto encontrado = pilotos.stream().filter(p -> p.getId() == id).findFirst()
                .orElseThrow(() -> new ValidacionException("No existe ningún piloto con el ID " + id + "."));
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
        equipos.remove(encontrado);
    }

    // =====================================================================
    // VEHÍCULOS
    // =====================================================================

    public ObservableList<Monoplaza> getVehiculos() {
        return vehiculos;
    }

    public void configurarVehiculo(int idVehiculo, CargaAerodinamica carga, ModoConduccion modo) throws ValidacionException {
        Monoplaza vehiculo = vehiculos.stream().filter(v -> v.getId() == idVehiculo).findFirst()
                .orElseThrow(() -> new ValidacionException("El vehículo seleccionado no existe."));
        if (carga == null || modo == null) {
            throw new ValidacionException("Debe seleccionar carga aerodinámica y modo de conducción.");
        }
        vehiculo.setCargaAerodinamica(carga);
        vehiculo.setModoConduccion(modo);
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
