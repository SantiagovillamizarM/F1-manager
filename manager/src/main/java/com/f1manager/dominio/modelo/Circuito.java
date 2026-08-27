//Como lo dice su nombre, guarda la información de un circuito 
//(nombre, país, longitud, vueltas, descripción).

package com.f1manager.dominio.modelo;

import java.util.Objects;
public class Circuito {

    private final int id;
    private String nombre;
    private String pais;
    private double longitudKm;
    private int vueltas;
    private String descripcion;

    public Circuito(int id, String nombre, String pais, double longitudKm, int vueltas, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.pais = pais;
        this.longitudKm = longitudKm;
        this.vueltas = vueltas;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
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

    public double getLongitudKm() {
        return longitudKm;
    }

    public void setLongitudKm(double longitudKm) {
        this.longitudKm = longitudKm;
    }

    public int getVueltas() {
        return vueltas;
    }

    public void setVueltas(int vueltas) {
        this.vueltas = vueltas;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Distancia total de la carrera (longitud de vuelta x número de vueltas). */
    public double getDistanciaTotalKm() {
        return longitudKm * vueltas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Circuito)) return false;
        Circuito circuito = (Circuito) o;
        return id == circuito.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre + " (" + pais + ")";
    }
}
