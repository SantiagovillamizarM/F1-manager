//Guarda el resultado de un piloto al terminar una carrera
//(posición, tiempo total, tiempos por vuelta, velocidad máxima alcanzada).

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;
//Importa la clase de ArrayList para poder usar listas (Son las que tienen <>)
import java.util.ArrayList;
//Importa la interfaz List, que define el comportamiento general de una lista en Java
//(sirve como plantilla para clases como ArrayList)
import java.util.List;

//Una clase publica llamada ResultadoCarrera
public class ResultadoCarrera {

    //Una variable privada del tipo int llamada posicion, en que puesto quedo el piloto
    private int posicion;
    //atributo final del tipo Piloto llamado piloto, el piloto dueño de este resultado
    private final Piloto piloto;
    //atributo final del tipo Monoplaza llamado monoplaza, el auto con el que corrio
    private final Monoplaza monoplaza;
    //atributo final del tipo double(Tipo de dato con muchos caracteres para numeros) llamado tiempoSegundos, el tiempo total que hizo en la carrera
    private final double tiempoSegundos;

    //Constructor
    //Inicializa y asigna los datos iniciales a un objeto cuando es creado.
    public ResultadoCarrera(Piloto piloto, Monoplaza monoplaza, double tiempoSegundos) {
        this.piloto = piloto;
        this.monoplaza = monoplaza;
        this.tiempoSegundos = tiempoSegundos;
    }

    //Getter
    public int getPosicion() {
        return posicion;
    }

    //Setter
    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    //Getter
    public Piloto getPiloto() {
        return piloto;
    }

    //Getter
    public Monoplaza getMonoplaza() {
        return monoplaza;
    }

    //Getter
    public double getTiempoSegundos() {
        return tiempoSegundos;
    }



    //Una lista privada del tipo Double llamada tiemposPorVuelta, guarda el tiempo (en segundos) que tardo cada vuelta; arranca vacia y se va llenando a medida que corre la carrera
        private List<Double> tiemposPorVuelta = new ArrayList<>();
    //Una lista privada del tipo Double llamada desgastePorVuelta, guarda cuanto desgaste (de 0 a 100) tenian las llantas al final de cada vuelta
    private List<Double> desgastePorVuelta = new ArrayList<>(); // desgaste de neumáticos (0-100) al final de cada vuelta
    //Una lista privada del tipo Integer llamada vueltasDePit, guarda en que numeros de vuelta el piloto entro a boxes a cambiar neumaticos
    private List<Integer> vueltasDePit = new ArrayList<>(); // en qué vueltas entró a boxes a cambiar neumáticos
    //Una lista privada del tipo Double llamada temperaturaLlantasPorVuelta, guarda la temperatura (en grados) de las llantas al final de cada vuelta
    private List<Double> temperaturaLlantasPorVuelta = new ArrayList<>(); // °C al final de cada vuelta
    //Una lista privada del tipo Double llamada temperaturaMotorPorVuelta, guarda la temperatura (en grados) del motor al final de cada vuelta
    private List<Double> temperaturaMotorPorVuelta = new ArrayList<>(); // °C al final de cada vuelta
    //Una variable privada del tipo double llamada velocidadMaximaAlcanzada, la velocidad mas alta que llego a tener durante la carrera
    private double velocidadMaximaAlcanzada;
    //Una variable privada del tipo boolean (osea que solo puede valer true o false) llamada dnf, dice si el piloto no logro terminar la carrera (Did Not Finish)
    private boolean dnf;
    //Una variable privada del tipo double llamada progresoChoque, guarda cuantas vueltas llevaba completadas al momento del choque (ej. 5.375); solo sirve si dnf es true
    private double progresoChoque; // vueltas completadas al momento del choque (ej. 5.375), solo válido si dnf
    //Una variable privada del tipo Piloto llamada rivalChoque, guarda al otro piloto involucrado si el choque fue entre dos (queda en null si el choque fue en solitario)
    private Piloto rivalChoque; // el otro piloto involucrado, si el choque fue entre dos (null si fue en solitario)

    //Getter
    public List<Double> getTiemposPorVuelta() {
        return tiemposPorVuelta;
    }

    //Setter
    public void setTiemposPorVuelta(List<Double> tiemposPorVuelta) {
        this.tiemposPorVuelta = tiemposPorVuelta;
    }

    //Getter
    public double getVelocidadMaximaAlcanzada() {
        return velocidadMaximaAlcanzada;
    }

    //Setter
    public void setVelocidadMaximaAlcanzada(double velocidadMaximaAlcanzada) {
        this.velocidadMaximaAlcanzada = velocidadMaximaAlcanzada;
    }

    //Getter
    public boolean isDnf() {
        return dnf;
    }

    //Getter
    public double getProgresoChoque() {
        return progresoChoque;
    }

    //Este metodo del tipo void (no devuelve nada) marca al piloto como que no termino la carrera por un choque,
    //pone dnf en true y guarda en que punto exacto de la carrera (progresoChoque) paso el choque.
    public void marcarChoque(double progresoChoque) {
        this.dnf = true;
        this.progresoChoque = progresoChoque;
    }

    //Getter
    public Piloto getRivalChoque() {
        return rivalChoque;
    }

    //Setter
    public void setRivalChoque(Piloto rivalChoque) {
        this.rivalChoque = rivalChoque;
    }

    //Este metodo del tipo boolean revisa si el choque fue grupal, osea que hubo otro piloto involucrado
    //(dnf en true Y ademas hay un rivalChoque guardado que no es null); si solo estuvo dnf sin rival, fue un choque en solitario.
    public boolean esChoqueGrupal() {
        return dnf && rivalChoque != null;
    }

    //Este metodo del tipo int calcula cuantas vueltas completo realmente el piloto:
    //si tuvo un dnf, redondea hacia abajo el progresoChoque (Math.floor) para quedarse solo con las vueltas enteras que si termino;
    //si no tuvo dnf (termino la carrera), simplemente cuenta cuantos tiempos de vuelta hay guardados en la lista.
    public int getVueltasCompletadas() {
        return dnf ? (int) Math.floor(progresoChoque) : tiemposPorVuelta.size();
    }

    //Getter
    public List<Double> getDesgastePorVuelta() {
        return desgastePorVuelta;
    }

    //Setter
    public void setDesgastePorVuelta(List<Double> desgastePorVuelta) {
        this.desgastePorVuelta = desgastePorVuelta;
    }

    //Este metodo del tipo double devuelve el desgaste de neumaticos (de 0 a 100) que tenia el piloto en el momento
    //justo en que dejo de correr (ya sea porque llego a la meta o porque choco).
    public double getDesgasteFinal() {
        //Math.min evita salirse del tamaño de la lista comparando las vueltas completadas contra cuantos datos de desgaste hay guardados
        int ultimaVueltaValida = Math.min(getVueltasCompletadas(), desgastePorVuelta.size());
        //Operador ternario: si no hay ninguna vuelta valida devuelve 0, si no, devuelve el desgaste de la ultima vuelta valida (restando 1 por el indice base 0)
        return ultimaVueltaValida <= 0 ? 0 : desgastePorVuelta.get(ultimaVueltaValida - 1);
    }

    //Setter
    public void setVueltasDePit(List<Integer> vueltasDePit) {
        this.vueltasDePit = vueltasDePit;
    }

    //Getter
    public List<Double> getTemperaturaLlantasPorVuelta() {
        return temperaturaLlantasPorVuelta;
    }

    //Setter
    public void setTemperaturaLlantasPorVuelta(List<Double> temperaturaLlantasPorVuelta) {
        this.temperaturaLlantasPorVuelta = temperaturaLlantasPorVuelta;
    }

    //Getter
    public List<Double> getTemperaturaMotorPorVuelta() {
        return temperaturaMotorPorVuelta;
    }

    //Setter
    public void setTemperaturaMotorPorVuelta(List<Double> temperaturaMotorPorVuelta) {
        this.temperaturaMotorPorVuelta = temperaturaMotorPorVuelta;
    }

    //Este metodo del tipo List<Integer> devuelve en que vueltas entro a boxes, pero solo contando las paradas
    //que de verdad ocurrieron antes de que el piloto terminara de correr (para no mostrar paradas "fantasma" despues de un choque).
    public List<Integer> getParadasEnBoxes() {
        //Guarda cuantas vueltas completo realmente el piloto
        int completadas = getVueltasCompletadas();
        //Crea una lista nueva y vacia para ir guardando ahi las paradas que si son validas
        List<Integer> validas = new ArrayList<>();
        //Recorre una por una todas las vueltas de pit guardadas
        for (Integer vuelta : vueltasDePit) {
            //Si esa parada paso en una vuelta que si se llego a completar, se agrega a la lista de validas
            if (vuelta <= completadas) {
                validas.add(vuelta);
            }
        }
        //Devuelve la lista ya filtrada con las paradas validas
        return validas;
    }

    //Este metodo del tipo double calcula el tiempo promedio por vuelta del piloto.
    public double getTiempoPromedioVuelta() {
        //Cuantas vueltas completo realmente
        int completadas = getVueltasCompletadas();
        //Si no completo ninguna vuelta, no hay nada que promediar, devuelve 0
        if (completadas <= 0) {
            return 0;
        }
        //Si el piloto SI termino la carrera (no tuvo dnf), el promedio es simplemente el tiempo total dividido entre el numero de vueltas
        if (!dnf) {
            return tiempoSegundos / tiemposPorVuelta.size();
        }
        //Si tuvo dnf, hay que sumar manualmente solo los tiempos de las vueltas que si completo
        double suma = 0;
        //Recorre desde la vuelta 0 hasta la ultima vuelta completada
        for (int i = 0; i < completadas; i++) {
            //Le va sumando a "suma" el tiempo de cada vuelta
            suma += tiemposPorVuelta.get(i);
        }
        //Devuelve el promedio dividiendo la suma entre la cantidad de vueltas completadas
        return suma / completadas;
    }

    //Este metodo del tipo String devuelve el tiempo total ya formateado como minutos:segundos.milisegundos, reutilizando formatearTiempo().
    public String getTiempoFormateado() {
        return formatearTiempo(tiempoSegundos);
    }

    //Este metodo del tipo String arma el texto de diferencia respecto al lider de la carrera, por ejemplo "+3.512".
    public String getDiferenciaFormateada(double tiempoLiderSegundos) {
        //Si el piloto tuvo un dnf, en vez de un tiempo se muestra en que vuelta se quedo (sumando 1 porque nosotros contamos las vueltas desde 1 no desde 0)
        if (dnf) {
            return String.format("DNF (vuelta %d)", (int) Math.floor(progresoChoque) + 1);
        }
        //Si el piloto quedo en la posicion 1 (el lider), no hay diferencia que mostrar, se muestra su propio tiempo
        if (posicion == 1) {
            return getTiempoFormateado();
        }
        //Calcula cuanto tiempo de mas hizo este piloto comparado con el tiempo del lider
        double diferencia = tiempoSegundos - tiempoLiderSegundos;
        //Saca cuantos minutos completos hay en esa diferencia
        long minutos = (long) (diferencia / 60);
        //Le resta esos minutos (convertidos a segundos) a la diferencia para quedarse solo con los segundos sueltos
        double segundos = diferencia - minutos * 60;
        //Si hay al menos un minuto de diferencia, se muestra en formato "+minutos:segundos"
        if (minutos > 0) {
            return String.format("+%d:%06.3f", minutos, segundos);
        }
        //Si no llega a un minuto, se muestra solo en formato "+segundos"
        return String.format("+%.3f", diferencia);
    }

    //Este metodo estatico (se puede usar sin crear un objeto) del tipo String convierte un numero de
    //segundos en total a un texto con formato minutos:segundos.milisegundos, como se ve en una clasificacion oficial de F1.
    public static String formatearTiempo(double totalSegundos) {
        //Saca cuantos minutos completos caben en el total de segundos
        long minutos = (long) (totalSegundos / 60);
        //Le resta esos minutos (en segundos) al total para quedarse con los segundos que sobran
        double segundos = totalSegundos - minutos * 60;
        //Arma el texto final rellenando con ceros para que siempre se vea igual de ancho (ej. 1:03.045)
        return String.format("%d:%06.3f", minutos, segundos);
    }
}
