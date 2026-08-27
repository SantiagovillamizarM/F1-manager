//Guarda la información de un piloto 
//(nombre, equipo, rol, experiencia, habilidad).
package com.f1manager.dominio.modelo;

import java.util.Objects;

public class Piloto {

    private final int id;
    private String nombre;
    private String equipo;
    private RolPiloto rol;
    private int experienciaAnios;
    private int habilidad; // 1 a 100

    public Piloto(int id, String nombre, String equipo, RolPiloto rol, int experienciaAnios, int habilidad) {
        this.id = id;
        this.nombre = nombre;
        this.equipo = equipo;
        this.rol = rol;
        this.experienciaAnios = experienciaAnios;
        this.habilidad = habilidad;
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

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public RolPiloto getRol() {
        return rol;
    }

    public void setRol(RolPiloto rol) {
        this.rol = rol;
    }

    public int getExperienciaAnios() {
        return experienciaAnios;
    }

    public void setExperienciaAnios(int experienciaAnios) {
        this.experienciaAnios = experienciaAnios;
    }

    public int getHabilidad() {
        return habilidad;
    }

    public void setHabilidad(int habilidad) {
        this.habilidad = habilidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Piloto)) return false;
        Piloto piloto = (Piloto) o;
        return id == piloto.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre + " - " + equipo;
    }
}
