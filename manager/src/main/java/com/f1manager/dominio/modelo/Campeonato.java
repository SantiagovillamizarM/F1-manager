//Representa una temporada completa: un calendario de carreras, en qué
//fecha va, y la clasificación de puntos de pilotos y de equipos.

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;


//Importa la clase de ArrayList para poder usar listas (Son las que tienen <>)
import java.util.ArrayList;
//Es una importación que trae la clase LinkedHashMap para crear diccionarios (parejas de clave y valor) 
//que conservan el orden en el que agregas los elementos
import java.util.LinkedHashMap;
//Importa la interfaz List, que define el comportamiento general de una lista en Java 
//(sirve como plantilla para clases como ArrayList)
import java.util.List;
//Importa la interfaz general Map, que sirve como plantilla para crear diccionarios de clave y valor
import java.util.Map;
//Importa Random para sortear el clima dinámico de cada carrera del calendario.
import java.util.Random;

//Clase publica llamada "Campeonato"
public class Campeonato {

    //Es una constante privada y fija (final) que guarda la tabla oficial de puntos de la F1 para que nadie pueda modificarla.
    private static final int[] TABLA_PUNTOS = {25, 18, 15, 12, 10, 8, 6, 4, 2, 1};
    //Esta es una lista privada de los objetos que trae "Circuito" que representa el calendario de las carreras de f1
    private final List<Circuito> calendario;
    //Generador de números aleatorios para el clima dinámico (una sola instancia para todo el campeonato).
    private final Random random = new Random();
    //Clima ya sorteado para la carrera actual (indice); se guarda para no volver a sortear si la
    //pantalla se reconstruye, y se limpia en registrarResultado() para que la siguiente carrera
    //sortee el suyo propio.
    private Clima climaActual;
    //Esto es un diccionario (o mapa, los cuales funcionan con clave valor que funcionan como un diccionario de la vida real, 
    //donde buscas una palabra y encuentras su significado) fijo (gracias al final) que relaciona a cada objeto "Piloto" Con sus puntos acumulados
    //(Integer) manteniendo el orden de registro y lo llama "puntosPiloto"
    private final Map<Piloto, Integer> puntosPilotos = new LinkedHashMap<>();//-> aca es donde se almacena la informacion de las competencias (los puntos del piloto)
    //Un diccionario fijo que relaciona cada equipo (Equipos siendo String) con sus puntos acumulados (Integer)  
    private final Map<String, Integer> puntosEquipos = new LinkedHashMap<>();//-> aca es donde se almacena la informacion de las competencias (los puntos del Equipo)
    //Es un contador privado que controla cuál es la carrera actual del calendario, iniciando en 0 (0 siendo la primera carrera que se corera en el campeonato)
    private int indice = 0;

    //Este es un constructor publico de la clase "Campeonato" que toma la lista de circuitos 
    //(Osea el calendario de circuitos en donde se va a correr), La lista con los pilotos y la lista con los equipos"
    public Campeonato(List<Circuito> calendario, List<Piloto> pilotos, List<Equipo> equipos) {
        //Aca guarda una copia independiente del calendario original recibido en una nueva lista ArrayList para proteger la lista original 
        //de modificaciones externas no deseadas (Una copia modificable de la informacion).
        this.calendario = new ArrayList<>(calendario);

        //Este bloque es un bucle for-each y su objetivo es arrancar el campeonato dejando a todos los pilotos con 0 puntos
        //Aca recore la lista "piloto" uno por uno 
        for (Piloto piloto : pilotos) {
            //Aca selecciona que los pilotos tengan 0 puntos
            puntosPilotos.put(piloto, 0);
        }

         //Este bloque es un bucle for-each y su objetivo es arrancar el campeonato dejando a todos los equipos con 0 puntos
         //Aca recorre los equipos uno por uno
        for (Equipo equipo : equipos) {
            //Aca selecciona que los equipos tengan 0 puntos
            puntosEquipos.put(equipo.getNombre(), 0);
        }
    }

    //Getter
    public List<Circuito> getCalendario() {
        //Hace una copia INMUTABLE(protegida contra modificaciones) de la lista calendario
        //Funcion de copyOf es crear copias inmodificables
        return List.copyOf(calendario);
    }

    //Getter
    public Circuito getCircuitoActual() {
        //Busca y retorna el objeto Circuito que está guardado en la posición indice de la lista calendario
        return calendario.get(indice);
    }

    //Clima dinámico de la carrera actual: se sortea una sola vez (según el país del circuito) y
    //se reutiliza el mismo resultado mientras siga siendo la carrera actual.
    public Clima getClimaActual() {
        if (climaActual == null) {
            climaActual = Clima.ALEATORIO.resolverDinamico(random, getCircuitoActual().getPais());
        }
        return climaActual;
    }

    //Getter
    public int getNumeroCarreraActual() {
        //Retorna el número de la carrera actual adaptado a formato humano (sumando 1 al indice, ya que nosotros empezamos a contar desde el 1 no desde el 0)
        //Mejora visual
        return indice + 1;
    }
    //Getter
    public int getTotalCarreras() {
        //trae la informacion de calendario viendo cuantas carreras hay dentro de esa lista
        return calendario.size();
    }

    //Simplemente compara dónde vas (indice) contra el total de carreras que hay (calendario.size()) para saber si la temporada sigue activa o si ya finalizó.
    public boolean quedanCarreras() {
        return indice < calendario.size();
    }

    //Este es un metodo de tipo void(No devuelve nada) llamado "registrarResultado" que toma los datos de la lista de "ResultadoCarrera"
    public void registrarResultado(List<ResultadoCarrera> resultados) {
        //Este es un cliclo for each que toma cada resultado de la lista "ResultadoCarrera"
        for (ResultadoCarrera resultado : resultados) {
            //calcular cuántos puntos le corresponden a ese piloto según la posición en la que terminó. 
            int puntos = puntosDePosicion(resultado);
            //Busca al piloto en el mapa y le suma sus puntos nuevos al total que ya tenia acumulando
            //Merge lo que hace especificamente es actualizar un valor
            //sum simplemente suma
            puntosPilotos.merge(resultado.getPiloto(), puntos, Integer::sum);
            //Hace lo mismo que la línea anterior, pero buscando al equipo del piloto para actualizar y sumar sus puntos en el mapa 
            //de posiciones de constructores (puntosEquipos).
            puntosEquipos.merge(resultado.getPiloto().getEquipo(), puntos, Integer::sum);
        }
        //Al indice se le suma uno para pasar a la siguiente carrera con los puntos ya actualizados
        indice++;
        //Se limpia el clima ya sorteado: la siguiente carrera (nuevo indice) sorteará el suyo.
        climaActual = null;
    }

    //Esto es un metodo privado del tipo int (osea numeros) que recibe la informacion de ResultadoCarrera llamado puntosDePosicion 
    private int puntosDePosicion(ResultadoCarrera resultado) {
        //Si el resultado de la carrera es un DNF
        if (resultado.isDnf()) {
            //De 0 puntos
            return 0;
        }
        //Esto dice que la posicion (del tipo int osea numero) es igual al resultado que se obtiene del getPosicion
        int posicion = resultado.getPosicion();
        //Operador ternario que verifica si la posición del piloto está dentro del rango que otorga puntos. 
        //Si es así, retorna los puntos del arreglo TABLA_PUNTOS (usando posicion - 1 por el índice base 0); de lo contrario, retorna 0.
        return (posicion >= 1 && posicion <= TABLA_PUNTOS.length) ? TABLA_PUNTOS[posicion - 1] : 0;
    }

    // Método público que obtiene y devuelve la tabla de clasificación de los pilotos con sus puntos acumulados
    public List<Map.Entry<Piloto, Integer>> getClasificacionPilotos() {
        //Retorna los puntos del piloto
        //entrySet extrae las combinaciones de (Clave, Valor) juntas en lugar de tener las claves y los valores por separado.
        //Pone los elementos en una fila india para poder aplicarles filtros, transformaciones u ordenamientos de forma rápida.
        return puntosPilotos.entrySet().stream()
                //Ordena todos los elementos que van pasando por la cinta transportadora del stream()
                //En una pareja Map.Entry, la clave es el Piloto y el valor es el Integer (los puntos)
                //comparingByValue() le indica a Java que debe comparar las parejas mirando el valor (los puntos) y 
                //NO el nombre del piloto. Por defecto, esto los ordena de menor a mayor (ascendente: 0, 10, 25...)
                // y el reversed Invierte la regla del ordenamiento osea que vayan de mayor a menor
                .sorted(Map.Entry.<Piloto, Integer>comparingByValue().reversed())
                //Cierra el flujo (stream) y empaqueta todo el resultado final dentro de una List para poder retornarla.
                .toList();
    }

    //Lo mismo pero con los equipos :D
    public List<Map.Entry<String, Integer>> getClasificacionEquipos() {
        return puntosEquipos.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();
    }
}
