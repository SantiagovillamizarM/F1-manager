//Guarda el resultado de un piloto al terminar una carrera 
//(posición, tiempo total, tiempos por vuelta, velocidad máxima alcanzada).
package com.f1manager.dominio.modelo;
import java.util.ArrayList;
import java.util.List;

public class ResultadoCarrera {

    private int posicion;
    private final Piloto piloto;
    private final Monoplaza monoplaza;
    private final double tiempoSegundos;

    public ResultadoCarrera(Piloto piloto, Monoplaza monoplaza, double tiempoSegundos) {
        this.piloto = piloto;
        this.monoplaza = monoplaza;
        this.tiempoSegundos = tiempoSegundos;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public Piloto getPiloto() {
        return piloto;
    }

    public Monoplaza getMonoplaza() {
        return monoplaza;
    }

    public double getTiempoSegundos() {
        return tiempoSegundos;
    }



        private List<Double> tiemposPorVuelta = new ArrayList<>();
    private double velocidadMaximaAlcanzada;

    public List<Double> getTiemposPorVuelta() {
        return tiemposPorVuelta;
    }

    public void setTiemposPorVuelta(List<Double> tiemposPorVuelta) {
        this.tiemposPorVuelta = tiemposPorVuelta;
    }

    public double getVelocidadMaximaAlcanzada() {
        return velocidadMaximaAlcanzada;
    }

    public void setVelocidadMaximaAlcanzada(double velocidadMaximaAlcanzada) {
        this.velocidadMaximaAlcanzada = velocidadMaximaAlcanzada;
    }

    public double getTiempoPromedioVuelta() {
        return tiemposPorVuelta.isEmpty() ? 0 : tiempoSegundos / tiemposPorVuelta.size();
    }

    /** Formatea el tiempo total como m:ss.mmm, similar a una clasificación oficial. */
    public String getTiempoFormateado() {
        return formatearTiempo(tiempoSegundos);
    }

    /** Formatea una diferencia respecto al líder, por ejemplo "+3.512". */
    public String getDiferenciaFormateada(double tiempoLiderSegundos) {
        if (posicion == 1) {
            return getTiempoFormateado();
        }
        double diferencia = tiempoSegundos - tiempoLiderSegundos;
        long minutos = (long) (diferencia / 60);
        double segundos = diferencia - minutos * 60;
        if (minutos > 0) {
            return String.format("+%d:%06.3f", minutos, segundos);
        }
        return String.format("+%.3f", diferencia);
    }

    public static String formatearTiempo(double totalSegundos) {
        long minutos = (long) (totalSegundos / 60);
        double segundos = totalSegundos - minutos * 60;
        return String.format("%d:%06.3f", minutos, segundos);
    }
}
