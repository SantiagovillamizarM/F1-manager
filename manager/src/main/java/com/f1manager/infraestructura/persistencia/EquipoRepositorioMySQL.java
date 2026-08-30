//Lee y escribe equipos en la tabla MySQL "equipos".
package com.f1manager.infraestructura.persistencia;

import com.f1manager.dominio.modelo.Equipo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

final class EquipoRepositorioMySQL {

    private EquipoRepositorioMySQL() {
    }

    static List<Equipo> listarTodos() {
        String sql = "SELECT nombre, pais, motor FROM equipos";
        List<Equipo> equipos = new ArrayList<>();
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                equipos.add(new Equipo(rs.getString("nombre"), rs.getString("pais"), rs.getString("motor")));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudieron cargar los equipos desde MySQL: " + ex.getMessage(), ex);
        }
        return equipos;
    }

    static void insertar(Equipo equipo) {
        String sql = "INSERT INTO equipos (nombre, pais, motor) VALUES (?, ?, ?)";
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, equipo.getNombre());
            stmt.setString(2, equipo.getPais());
            stmt.setString(3, equipo.getMotor());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo guardar el equipo en MySQL: " + ex.getMessage(), ex);
        }
    }

    static void eliminar(String nombre) {
        String sql = "DELETE FROM equipos WHERE nombre = ?";
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo eliminar el equipo en MySQL: " + ex.getMessage(), ex);
        }
    }
}
