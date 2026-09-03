//Elige y carga las 3 fotos de un choque (individual o entre dos pilotos) desde
///imagenes/choque solo o /imagenes/choque grupo.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.util;

//Trae la clase del dominio Piloto, para poder relacionar cada choque con el piloto que lo tuvo.
import com.f1manager.dominio.modelo.Piloto;
//Trae la clase del dominio ResultadoCarrera, que guarda cómo terminó cada piloto en la carrera (posición, si fue DNF, etc.).
import com.f1manager.dominio.modelo.ResultadoCarrera;
//Trae SimuladorCarrera para poder usar su clase interna ResultadoSimulacion (el conjunto de resultados de una carrera ya simulada).
import com.f1manager.dominio.servicio.SimuladorCarrera;
//Trae la clase Image de JavaFX, que representa una imagen ya cargada en memoria lista para mostrarse en pantalla.
import javafx.scene.image.Image;

//Importa la clase de ArrayList para poder usar listas (Son las que tienen <>)
import java.util.ArrayList;
//Trae Collections, una clase de herramientas con métodos útiles para listas, como shuffle() (barajar/desordenar al azar).
import java.util.Collections;
//Trae HashMap, una implementación de diccionario (clave-valor) que no garantiza ningún orden particular.
import java.util.HashMap;
//Importa la interfaz List, que define el comportamiento general de una lista en Java (sirve como plantilla para clases como ArrayList)
import java.util.List;
//Importa la interfaz general Map, que sirve como plantilla para crear diccionarios de clave y valor
import java.util.Map;
//Trae WeakHashMap, un diccionario especial cuyas claves pueden ser "recogidas" por el recolector de basura
//cuando ya nadie más las está usando en el resto del programa (útil para no acumular memoria de carreras viejas).
import java.util.WeakHashMap;

//Clase publica y final (no se puede heredar de ella) llamada "FotosChoque"
public final class FotosChoque {

    // Cada grupo son las 3 fotos de un mismo incidente (mismo ángulo/momento, distintas tomas).
    //Arreglo de arreglos de String: cada fila es un grupo de 3 nombres de archivo para un choque individual.
    private static final String[][] GRUPOS_SOLO = {
            {"Choque solo 1.jpg", "Choque solo 1.2.jpg", "Choque solo 1.3.jpg"},
            {"Choque solo 2.jpg", "Choque solo 2.2.jpg", "Choque solo 2.3.jpg"},
            {"Choque solo 3.png", "Choque solo 3.2.jpg", "Choque solo 3.3.jpg"},
    };
    //Igual que el de arriba, pero con los grupos de fotos que se usan cuando el choque es entre dos pilotos.
    private static final String[][] GRUPOS_ENTRE_PILOTOS = {
            {"Choque grupo 1.jpg", "Choque grupo1.2.jpg", "Choque grupo1.3.jpg"},
            {"Choque grupo 2.jpg", "Choque grupo 2.2.jpg", "Choque grupo 2.3.jpg"},
            {"Choque grupo 3.png", "Choque grupo 3.2.jpg", "Choque grupo 3.3.jpg"},
    };

    // Por carrera (misma ResultadoSimulacion), a cada piloto chocado se le asigna un grupo de fotos
    // sin repetir hasta agotar los disponibles de esa categoría (solo/grupal); si un choque es
    // grupal, ambos pilotos comparten el mismo grupo asignado. WeakHashMap para no acumular
    // memoria de carreras viejas mientras la app sigue abierta.
    //Diccionario de diccionarios: por cada carrera (ResultadoSimulacion) guarda otro diccionario que
    //relaciona cada resultado de piloto con el índice del grupo de fotos que le tocó.
    private static final Map<SimuladorCarrera.ResultadoSimulacion, Map<ResultadoCarrera, Integer>> ASIGNACIONES =
            new WeakHashMap<>();

    //Constructor privado y vacío: esta clase es solo de utilidades (puros métodos estáticos),
    //no tiene sentido crear un objeto FotosChoque con "new", por eso se bloquea el constructor.
    private FotosChoque() {
    }

    //Las 3 fotos del choque de este piloto en esta carrera (lista vacía si no tuvo choque).
    public static List<Image> paraChoque(SimuladorCarrera.ResultadoSimulacion simulacion, ResultadoCarrera resultado) {
        //Si el piloto no tuvo un DNF (abandono/choque), no hay fotos que mostrar
        if (!resultado.isDnf()) {
            return List.of();
        }
        //Busca (o calcula por primera vez con asignarGrupos) el reparto de grupos de fotos de esta carrera
        Map<ResultadoCarrera, Integer> asignacion = ASIGNACIONES.computeIfAbsent(simulacion, FotosChoque::asignarGrupos);
        //Busca qué grupo de fotos le tocó a este resultado en particular
        Integer indice = asignacion.get(resultado);
        //Si por alguna razón no tiene grupo asignado, no hay fotos que mostrar
        if (indice == null) {
            return List.of();
        }
        //Según si el choque fue grupal (entre dos pilotos) o individual, elige el arreglo y la carpeta correctos
        boolean esGrupal = resultado.esChoqueGrupal();
        String[][] grupos = esGrupal ? GRUPOS_ENTRE_PILOTOS : GRUPOS_SOLO;
        String carpeta = esGrupal ? "choque grupo/" : "choque solo/";
        return cargar(carpeta, grupos[indice]);
    }

    //Reparte un grupo de fotos a cada choque de la carrera, sin repetir hasta agotar los disponibles.
    private static Map<ResultadoCarrera, Integer> asignarGrupos(SimuladorCarrera.ResultadoSimulacion simulacion) {
        //Diccionario auxiliar que permite encontrar rápido el ResultadoCarrera de un Piloto dado
        //(se usa para encontrar al rival cuando el choque es entre dos pilotos)
        Map<Piloto, ResultadoCarrera> porPiloto = new HashMap<>();
        for (ResultadoCarrera r : simulacion.getResultados()) {
            porPiloto.put(r.getPiloto(), r);
        }

        //Diccionario final que se va a devolver: qué índice de grupo le tocó a cada resultado
        Map<ResultadoCarrera, Integer> asignacion = new HashMap<>();
        //"Bolsas" con los índices de grupos disponibles todavía sin repartir, una para choques individuales y otra para grupales
        List<Integer> bolsaSolo = new ArrayList<>();
        List<Integer> bolsaGrupo = new ArrayList<>();

        //Recorre todos los resultados de la carrera buscando los que tuvieron un choque
        for (ResultadoCarrera r : simulacion.getResultados()) {
            //Si no fue DNF, o si ya se le asignó grupo antes (por ser rival de otro choque grupal), se salta
            if (!r.isDnf() || asignacion.containsKey(r)) {
                continue;
            }
            boolean esGrupal = r.esChoqueGrupal();
            List<Integer> bolsa = esGrupal ? bolsaGrupo : bolsaSolo;
            //Si la bolsa de índices disponibles ya se vació, se vuelve a llenar barajada (para no repetir
            //un grupo hasta agotar todos los disponibles de esa categoría)
            if (bolsa.isEmpty()) {
                bolsa.addAll(barajada(esGrupal ? GRUPOS_ENTRE_PILOTOS.length : GRUPOS_SOLO.length));
            }
            //Saca el último índice de la bolsa (ya viene barajado, así que da igual sacar del final) y se lo asigna a este resultado
            int indice = bolsa.remove(bolsa.size() - 1);
            asignacion.put(r, indice);

            //Si el choque fue grupal, el rival del choque debe compartir el mismo grupo de fotos
            if (esGrupal) {
                ResultadoCarrera rival = porPiloto.get(r.getRivalChoque());
                if (rival != null) {
                    asignacion.put(rival, indice);
                }
            }
        }
        return asignacion;
    }

    //Genera una lista de índices del 0 al (tamano - 1) en orden aleatorio (barajados), usada como
    //"bolsa" de la que se van sacando grupos de fotos sin repetir.
    private static List<Integer> barajada(int tamano) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < tamano; i++) {
            indices.add(i);
        }
        //Desordena la lista al azar
        Collections.shuffle(indices);
        return indices;
    }

    //Carga las imágenes de un grupo de fotos (dentro de la carpeta indicada), descartando las que
    //no se hayan podido cargar (por ejemplo, si el archivo todavía no existe en el disco).
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
