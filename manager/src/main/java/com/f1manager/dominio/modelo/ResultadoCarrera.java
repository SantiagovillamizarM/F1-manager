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
    private boolean dnf;
    private double progresoChoque; // vueltas completadas al momento del choque (ej. 5.375), solo válido si dnf

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

    public boolean isDnf() {
        return dnf;
    }

    public double getProgresoChoque() {
        return progresoChoque;
    }

    /** Marca al piloto como no finalizador por choque en el punto de la carrera indicado. */
    public void marcarChoque(double progresoChoque) {
        this.dnf = true;
        this.progresoChoque = progresoChoque;
    }

    /** Cuántas vueltas completó realmente: todas si terminó, o las previas al choque si no. */
    public int getVueltasCompletadas() {
        return dnf ? (int) Math.floor(progresoChoque) : tiemposPorVuelta.size();
    }

    public double getTiempoPromedioVuelta() {
        int completadas = getVueltasCompletadas();
        if (completadas <= 0) {
            return 0;
        }
        if (!dnf) {
            return tiempoSegundos / tiemposPorVuelta.size();
        }
        double suma = 0;
        for (int i = 0; i < completadas; i++) {
            suma += tiemposPorVuelta.get(i);
        }
        return suma / completadas;
    }

    /** Formatea el tiempo total como m:ss.mmm, similar a una clasificación oficial. */
    public String getTiempoFormateado() {
        return formatearTiempo(tiempoSegundos);
    }

    /** Formatea una diferencia respecto al líder, por ejemplo "+3.512". */
    public String getDiferenciaFormateada(double tiempoLiderSegundos) {
        if (dnf) {
            return String.format("DNF (vuelta %d)", (int) Math.floor(progresoChoque) + 1);
        }
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
