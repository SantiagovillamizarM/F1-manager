//Guarda la información de un vehículo 
//(modelo, equipo, motor, velocidad, aceleración, y su configuración actual).

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;

import java.util.Objects;
public class Monoplaza {

    // Rango seguro de presión de neumáticos (PSI): fuera de él no se puede configurar.
    // Dentro del rango, alejarse del valor óptimo (el centro) hace que su efecto en carrera
    // sea cada vez más impredecible: a veces ayuda, a veces perjudica.
    public static final double PRESION_MINIMA = 18.0;
    public static final double PRESION_MAXIMA = 26.0;
    public static final double PRESION_OPTIMA = (PRESION_MINIMA + PRESION_MAXIMA) / 2.0;

    // Rango realista para un monoplaza de F1 actual (reglamento técnico de la FIA): la potencia,
    // aerodinámica y neumáticos permitidos limitan tanto la velocidad punta como la aceleración
    // a valores conocidos; fuera de este rango no se deja registrar un vehículo.
    public static final double VELOCIDAD_MINIMA_KMH = 300.0;
    public static final double VELOCIDAD_MAXIMA_KMH = 380.0;
    public static final double ACELERACION_MINIMA_S = 2.0;
    public static final double ACELERACION_MAXIMA_S = 4.0;

    private final int id;
    private String modelo;
    private String equipo;
    private String motor;
    private double velocidadMaxKmh;
    private double aceleracion0a100; // segundos, menor es mejor
    private CargaAerodinamica cargaAerodinamica;
    private ModoConduccion modoConduccion;
    private TipoNeumatico tipoNeumatico;
    private double presionAire; // PSI

    //Constructor
    //Inicializa y asigna los datos iniciales a un objeto cuando es creado.
    public Monoplaza(int id, String modelo, String equipo, String motor,
                      double velocidadMaxKmh, double aceleracion0a100) {
        this.id = id;
        this.modelo = modelo;
        this.equipo = equipo;
        this.motor = motor;
        this.velocidadMaxKmh = velocidadMaxKmh;
        this.aceleracion0a100 = aceleracion0a100;
        this.cargaAerodinamica = CargaAerodinamica.MEDIA;
        this.modoConduccion = ModoConduccion.NORMAL;
        this.tipoNeumatico = TipoNeumatico.MEDIO;
        this.presionAire = PRESION_OPTIMA;
    }

    public int getId() {
        return id;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public String getMotor() {
        return motor;
    }

    public void setMotor(String motor) {
        this.motor = motor;
    }

    public double getVelocidadMaxKmh() {
        return velocidadMaxKmh;
    }

    public void setVelocidadMaxKmh(double velocidadMaxKmh) {
        this.velocidadMaxKmh = velocidadMaxKmh;
    }

    public double getAceleracion0a100() {
        return aceleracion0a100;
    }

    public void setAceleracion0a100(double aceleracion0a100) {
        this.aceleracion0a100 = aceleracion0a100;
    }

    public CargaAerodinamica getCargaAerodinamica() {
        return cargaAerodinamica;
    }

    public void setCargaAerodinamica(CargaAerodinamica cargaAerodinamica) {
        this.cargaAerodinamica = cargaAerodinamica;
    }

    public ModoConduccion getModoConduccion() {
        return modoConduccion;
    }

    public void setModoConduccion(ModoConduccion modoConduccion) {
        this.modoConduccion = modoConduccion;
    }

    public TipoNeumatico getTipoNeumatico() {
        return tipoNeumatico;
    }

    public void setTipoNeumatico(TipoNeumatico tipoNeumatico) {
        this.tipoNeumatico = tipoNeumatico;
    }

    public double getPresionAire() {
        return presionAire;
    }

    public void setPresionAire(double presionAire) {
        this.presionAire = presionAire;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Monoplaza)) return false;
        Monoplaza that = (Monoplaza) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return modelo + " (" + equipo + ")";
    }
}
