//Abre conexiones JDBC hacia la base de datos MySQL "f1_manager".
//IMPORTANTE: reemplaza CONTRASENA por la contraseña real de tu usuario de MySQL.
package com.f1manager.infraestructura.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionMySQL {

    private static final String URL = "jdbc:mysql://localhost:3306/f1_manager";
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "p14048ci";

    private ConexionMySQL() {
    }

    /** Abre una conexión nueva. Quien la use debe cerrarla (try-with-resources) cuando termine. */
    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}
