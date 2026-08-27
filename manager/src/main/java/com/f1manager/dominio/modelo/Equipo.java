//Guarda la información de un equipo/escudería 
//(nombre, país, motor).
package com.f1manager.dominio.modelo;

import java.util.Objects;

public class Equipo {

    private String nombre;
    private String pais;
    private String motor;

    public Equipo(String nombre, String pais, String motor) {
        this.nombre = nombre;
        this.pais = pais;
        this.motor = motor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getMotor() {
        return motor;
    }

    public void setMotor(String motor) {
        this.motor = motor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equipo)) return false;
        Equipo equipo = (Equipo) o;
        return nombre.equalsIgnoreCase(equipo.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre.toLowerCase());
    }

    @Override
    public String toString() {
        return nombre;
    }
}
