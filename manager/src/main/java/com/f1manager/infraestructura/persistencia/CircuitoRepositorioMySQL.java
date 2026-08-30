//Lee y escribe circuitos en la tabla MySQL "circuitos".
package com.f1manager.infraestructura.persistencia;

import com.f1manager.dominio.modelo.Circuito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

final class CircuitoRepositorioMySQL {

    private CircuitoRepositorioMySQL() {
    }

    static List<Circuito> listarTodos() {
        String sql = "SELECT id, nombre, pais, longitud_km, vueltas, descripcion FROM circuitos";
        List<Circuito> circuitos = new ArrayList<>();
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                circuitos.add(new Circuito(rs.getInt("id"), rs.getString("nombre"), rs.getString("pais"),
                        rs.getDouble("longitud_km"), rs.getInt("vueltas"), rs.getString("descripcion")));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudieron cargar los circuitos desde MySQL: " + ex.getMessage(), ex);
        }
        return circuitos;
    }

    /** Inserta el circuito y devuelve el id que le asignó MySQL (autoincremental). */
    static int insertarYObtenerId(String nombre, String pais, double longitudKm, int vueltas, String descripcion) {
        String sql = "INSERT INTO circuitos (nombre, pais, longitud_km, vueltas, descripcion) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
            stmt.setString(2, pais);
            stmt.setDouble(3, longitudKm);
            stmt.setInt(4, vueltas);
            stmt.setString(5, descripcion);
            stmt.executeUpdate();
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo guardar el circuito en MySQL: " + ex.getMessage(), ex);
        }
    }

    static void eliminar(int id) {
        String sql = "DELETE FROM circuitos WHERE id = ?";
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo eliminar el circuito en MySQL: " + ex.getMessage(), ex);
        }
    }
}
