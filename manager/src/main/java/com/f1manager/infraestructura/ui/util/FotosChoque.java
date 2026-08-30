//Elige y carga las 3 fotos de un choque (individual o entre dos pilotos) desde
///imagenes/choque solo o /imagenes/choque grupo.
package com.f1manager.infraestructura.ui.util;

import com.f1manager.dominio.modelo.Piloto;
import com.f1manager.dominio.modelo.ResultadoCarrera;
import com.f1manager.dominio.servicio.SimuladorCarrera;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class FotosChoque {

    // Cada grupo son las 3 fotos de un mismo incidente (mismo ángulo/momento, distintas tomas).
    private static final String[][] GRUPOS_SOLO = {
            {"Choque solo 1.jpg", "Choque solo 1.2.jpg", "Choque solo 1.3.jpg"},
            {"Choque solo 2.jpg", "Choque solo 2.2.jpg", "Choque solo 2.3.jpg"},
            {"Choque solo 3.png", "Choque solo 3.2.jpg", "Choque solo 3.3.jpg"},
    };
    private static final String[][] GRUPOS_ENTRE_PILOTOS = {
            {"Choque grupo 1.jpg", "Choque grupo1.2.jpg", "Choque grupo1.3.jpg"},
            {"Choque grupo 2.jpg", "Choque grupo 2.2.jpg", "Choque grupo 2.3.jpg"},
            {"Choque grupo 3.png", "Choque grupo 3.2.jpg", "Choque grupo 3.3.jpg"},
    };

    // Por carrera (misma ResultadoSimulacion), a cada piloto chocado se le asigna un grupo de fotos
    // sin repetir hasta agotar los disponibles de esa categoría (solo/grupal); si un choque es
    // grupal, ambos pilotos comparten el mismo grupo asignado. WeakHashMap para no acumular
    // memoria de carreras viejas mientras la app sigue abierta.
    private static final Map<SimuladorCarrera.ResultadoSimulacion, Map<ResultadoCarrera, Integer>> ASIGNACIONES =
            new WeakHashMap<>();

    private FotosChoque() {
    }

    /** Las 3 fotos del choque de este piloto en esta carrera (lista vacía si no tuvo choque). */
    public static List<Image> paraChoque(SimuladorCarrera.ResultadoSimulacion simulacion, ResultadoCarrera resultado) {
        if (!resultado.isDnf()) {
            return List.of();
        }
        Map<ResultadoCarrera, Integer> asignacion = ASIGNACIONES.computeIfAbsent(simulacion, FotosChoque::asignarGrupos);
        Integer indice = asignacion.get(resultado);
        if (indice == null) {
            return List.of();
        }
        boolean esGrupal = resultado.esChoqueGrupal();
        String[][] grupos = esGrupal ? GRUPOS_ENTRE_PILOTOS : GRUPOS_SOLO;
        String carpeta = esGrupal ? "choque grupo/" : "choque solo/";
        return cargar(carpeta, grupos[indice]);
    }

    /** Reparte un grupo de fotos a cada choque de la carrera, sin repetir hasta agotar los disponibles. */
    private static Map<ResultadoCarrera, Integer> asignarGrupos(SimuladorCarrera.ResultadoSimulacion simulacion) {
        Map<Piloto, ResultadoCarrera> porPiloto = new HashMap<>();
        for (ResultadoCarrera r : simulacion.getResultados()) {
            porPiloto.put(r.getPiloto(), r);
        }

        Map<ResultadoCarrera, Integer> asignacion = new HashMap<>();
        List<Integer> bolsaSolo = new ArrayList<>();
        List<Integer> bolsaGrupo = new ArrayList<>();

        for (ResultadoCarrera r : simulacion.getResultados()) {
            if (!r.isDnf() || asignacion.containsKey(r)) {
                continue;
            }
            boolean esGrupal = r.esChoqueGrupal();
            List<Integer> bolsa = esGrupal ? bolsaGrupo : bolsaSolo;
            if (bolsa.isEmpty()) {
                bolsa.addAll(barajada(esGrupal ? GRUPOS_ENTRE_PILOTOS.length : GRUPOS_SOLO.length));
            }
            int indice = bolsa.remove(bolsa.size() - 1);
            asignacion.put(r, indice);

            if (esGrupal) {
                ResultadoCarrera rival = porPiloto.get(r.getRivalChoque());
                if (rival != null) {
                    asignacion.put(rival, indice);
                }
            }
        }
        return asignacion;
    }

    private static List<Integer> barajada(int tamano) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < tamano; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);
        return indices;
    }

    private static List<Image> cargar(String carpeta, String[] archivos) {
        List<Image> fotos = new ArrayList<>();
        for (String archivo : archivos) {
            Image imagen = GestorImagenes.cargar(carpeta + archivo);
            if (imagen != null) {
                fotos.add(imagen);
            }
        }
        return fotos;
    }
}
