//Representa una temporada completa: un calendario de carreras, en qué
//fecha va, y la clasificación de puntos de pilotos y de equipos.
package com.f1manager.dominio.modelo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Campeonato {

    private static final int[] TABLA_PUNTOS = {25, 18, 15, 12, 10, 8, 6, 4, 2, 1};

    private final List<Circuito> calendario;
    private final Map<Piloto, Integer> puntosPilotos = new LinkedHashMap<>();
    private final Map<String, Integer> puntosEquipos = new LinkedHashMap<>();
    private int indice = 0;

    public Campeonato(List<Circuito> calendario, List<Piloto> pilotos, List<Equipo> equipos) {
        this.calendario = new ArrayList<>(calendario);
        for (Piloto piloto : pilotos) {
            puntosPilotos.put(piloto, 0);
        }
        for (Equipo equipo : equipos) {
            puntosEquipos.put(equipo.getNombre(), 0);
        }
    }

    public List<Circuito> getCalendario() {
        return List.copyOf(calendario);
    }

    public Circuito getCircuitoActual() {
        return calendario.get(indice);
    }

    public int getNumeroCarreraActual() {
        return indice + 1;
    }

    public int getTotalCarreras() {
        return calendario.size();
    }

    /** Si todavía hay una fecha del calendario pendiente de correr (llamar DESPUÉS de registrarResultado). */
    public boolean quedanCarreras() {
        return indice < calendario.size();
    }

    /** Suma los puntos de la carrera recién corrida (tabla oficial de F1) y avanza a la siguiente fecha. */
    public void registrarResultado(List<ResultadoCarrera> resultados) {
        for (ResultadoCarrera resultado : resultados) {
            int puntos = puntosDePosicion(resultado);
            puntosPilotos.merge(resultado.getPiloto(), puntos, Integer::sum);
            puntosEquipos.merge(resultado.getPiloto().getEquipo(), puntos, Integer::sum);
        }
        indice++;
    }

    private int puntosDePosicion(ResultadoCarrera resultado) {
        if (resultado.isDnf()) {
            return 0;
        }
        int posicion = resultado.getPosicion();
        return (posicion >= 1 && posicion <= TABLA_PUNTOS.length) ? TABLA_PUNTOS[posicion - 1] : 0;
    }

    /** Clasificación de pilotos, de mayor a menor puntaje. */
    public List<Map.Entry<Piloto, Integer>> getClasificacionPilotos() {
        return puntosPilotos.entrySet().stream()
                .sorted(Map.Entry.<Piloto, Integer>comparingByValue().reversed())
                .toList();
    }

    /** Clasificación de equipos (nombre del equipo), de mayor a menor puntaje. */
    public List<Map.Entry<String, Integer>> getClasificacionEquipos() {
        return puntosEquipos.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();
    }
}
