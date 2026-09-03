//Define las condiciones climáticas posibles 
//(Seco, Lluvioso, Extremo, Aleatorio) y cuánto afectan a la carrera.

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;

//Importa la interfaz general Map, que sirve como plantilla para crear diccionarios de clave y valor
import java.util.Map;
//trae la herramienta oficial de Java para generar valores aleatorios o al azar en tu programa.
import java.util.Random;
//Si algo es de tipo enum significa que solo tiene los valores que le demos de opcion, NO PUEDE HABER MAS VALORES
public enum Clima {
    // Tipo de clima seco, la etiqueta (como se ve) es "seco", el factor tiempo
    //Significa que entre mas alto el numero, mas lento corre y el factorVariabilidad
    //es cuánto más impredecibles/erráticas son las vueltas con ese clima
    SECO("Seco", 1.000, 1.00),
    LLUVIOSO("Lluvioso", 1.150, 1.80),
    EXTREMO("Extremo", 1.320, 2.60),
    ALEATORIO("Aleatorio", 0, 0);

    //Variable privada final inmodificable (final) del tipo String llamada "Etiqueta"
    private final String etiqueta;
    //Variable privada final inmodificable (final) del tipo Double llamada "factorTiempo"
    private final double factorTiempo;
    //Variable privada final inmodificable (final) del tipo double llamada "factorVariabilidad"
    private final double factorVariabilidad;

    //Constructor
    //Inicializa y asigna los datos iniciales a un objeto cuando es creado.
    Clima(String etiqueta, double factorTiempo, double factorVariabilidad) {
        this.etiqueta = etiqueta;
        this.factorTiempo = factorTiempo;
        this.factorVariabilidad = factorVariabilidad;
    }

    //Getter
    public String getEtiqueta() {
        return etiqueta;
    }
    //Getter
    public double getFactorTiempo() {
        return factorTiempo;
    }
    //Getter
    public double getFactorVariabilidad() {
        return factorVariabilidad;
    }

    //Este método decide el clima real que se va a usar en la simulación. Si el clima elegido es
    //ALEATORIO, sortea entre SECO, LLUVIOSO y EXTREMO con una distribución ponderada
    //(el clima seco es el más probable, el extremo el menos probable). Si no es ALEATORIO, simplemente devuelve el mismo clima.
    public Clima resolver(Random random) {
        //Si es igual a aleatorio
        if (this != ALEATORIO) {
            //Devolverlo
            return this;
        }
        //Valor del tipo double es igual a random
        //nextDouble() elije un numero decimal aletario entre 0.0 y 1.0 
        double valor = random.nextDouble();
        //Si el valor obtenido de ese nextDouble() es mas pequeño que 0.55
        if (valor < 0.55) {
            //Que salga seco
            return SECO;
            //Si no y el valor es mas grande que ese pero mas pequeño que 0.90
        } else if (valor < 0.90) {
            //Que salga lluvioso
            return LLUVIOSO;
            //Y si no que salga extremo
        } else {
            return EXTREMO;
        }
    }

    // Distribución pareja usada por resolver() y como respaldo de resolverDinamico() cuando el
    // país no coincide con ninguno de la tabla de abajo: {peso Seco, peso Lluvioso, peso Extremo}.
    private static final double[] PESOS_PREDETERMINADOS = {0.55, 0.35, 0.10};

    // Pesos por país (Seco, Lluvioso, Extremo) para el clima dinámico del modo Campeonato: se
    // busca por coincidencia de una palabra clave del país (en minúsculas); si no coincide con
    // ninguno, se usa PESOS_PREDETERMINADOS. Los climas históricamente más lluviosos (Bélgica,
    // Reino Unido, Japón, Brasil) pesan más hacia Lluvioso/Extremo; los desérticos (Baréin,
    // Arabia Saudita, Catar, Emiratos) casi siempre Seco.
    //CLIMA DINAMICO DEPENDIENDO DEL PAIS SELECCIONADO
    private static final Map<String, double[]> PESOS_POR_PAIS = Map.ofEntries(
            Map.entry("bélgica", new double[]{0.35, 0.45, 0.20}),
            Map.entry("reino unido", new double[]{0.40, 0.45, 0.15}),
            Map.entry("japón", new double[]{0.45, 0.40, 0.15}),
            Map.entry("brasil", new double[]{0.40, 0.40, 0.20}),
            Map.entry("singapur", new double[]{0.45, 0.40, 0.15}),
            Map.entry("países bajos", new double[]{0.45, 0.40, 0.15}),
            Map.entry("austria", new double[]{0.50, 0.38, 0.12}),
            Map.entry("italia", new double[]{0.65, 0.28, 0.07}),
            Map.entry("españa", new double[]{0.70, 0.25, 0.05}),
            Map.entry("méxico", new double[]{0.75, 0.20, 0.05}),
            Map.entry("hungría", new double[]{0.70, 0.24, 0.06}),
            Map.entry("baréin", new double[]{0.85, 0.12, 0.03}),
            Map.entry("arabia saudita", new double[]{0.85, 0.12, 0.03}),
            Map.entry("catar", new double[]{0.85, 0.12, 0.03}),
            Map.entry("emiratos árabes", new double[]{0.85, 0.12, 0.03})
    );
    //Si se elije cualquier opcion manual no se usa aletario y no pasa todo esto
    public Clima resolverDinamico(Random random, String pais) {
        if (this != ALEATORIO) {
            return this;
        }
        //Es el mecanismo que decide qué clima va a tocar según las probabilidades del país
        double[] pesos = pesosParaPais(pais);
        double valor = random.nextDouble();
        //Si el valor es mas pequeño que el peso
        if (valor < pesos[0]) {
            //Que sea seco
            return SECO;
            //Si el valor es mas pequeo que el primer peso mas el segundo
        } else if (valor < pesos[0] + pesos[1]) {
            //que sea lluvioso
            return LLUVIOSO;
            //Y si no que sea extremo
        } else {
            return EXTREMO;
        }
    }

    //Estos son los pesos para los paises
    private static double[] pesosParaPais(String pais) {
        //Se crea la variable normalizado del tipo String, pasa el texto del pais
        //a minuscula para facilitar la busqueda o asigna texto vacío si viene nulo para evitar errores.
        String normalizado = pais == null ? "" : pais.toLowerCase();
        // Recorre una a una cada entrada (país y sus porcentajes) guardada en el mapa PESOS_POR_PAIS.
        //entrySet() se usa para extraer todas las parejas "clave valor"
        for (var entrada : PESOS_POR_PAIS.entrySet()) {
            //Si el texto del país ingresado contiene el nombre del país guardado como clave (Key), devuelve sus porcentajes de clima (Value).
            if (normalizado.contains(entrada.getKey())) {
                return entrada.getValue();
            }
        }
        //Si no pasa nada de eso que devulva los pesos predeterminados
        return PESOS_PREDETERMINADOS;
    }

    //Un string publico que personaliza como se ve el texto (Osea las etiquetas de arriba)
    @Override
    public String toString() {
        return etiqueta;
    }
}

