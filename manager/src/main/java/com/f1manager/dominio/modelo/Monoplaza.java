//Guarda la información de un vehículo 
//(modelo, equipo, motor, velocidad, aceleración, y su configuración actual).
package com.f1manager.dominio.modelo;

import java.util.Objects;
public class Monoplaza {

    private final int id;
    private String modelo;
    private String equipo;
    private String motor;
    private double velocidadMaxKmh;
    private double aceleracion0a100; // segundos, menor es mejor
    private CargaAerodinamica cargaAerodinamica;
    private ModoConduccion modoConduccion;

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
