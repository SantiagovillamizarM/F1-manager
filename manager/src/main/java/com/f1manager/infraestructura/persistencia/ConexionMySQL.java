//Abre conexiones JDBC hacia la base de datos MySQL "f1_manager".
//IMPORTANTE: reemplaza CONTRASENA por la contraseña real de tu usuario de MySQL.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.persistencia;

//Trae la clase Connection, que representa una conexión ya abierta hacia la base de datos (por ahí es por donde se mandan las consultas)
import java.sql.Connection;
//Trae DriverManager, que es quien de verdad abre la conexión hacia MySQL usando la URL, el usuario y la contraseña de abajo
import java.sql.DriverManager;
//Trae SQLException, el tipo de error que lanza Java cuando algo sale mal hablando con la base de datos
import java.sql.SQLException;

//Una clase publica y final (final significa que nadie puede heredar de ella) llamada "ConexionMySQL"
public final class ConexionMySQL {

    //Variable privada, fija (final) y de la clase (static, osea que es una sola para todo el programa) del tipo String que guarda la dirección de la base de datos
    private static final String URL = "jdbc:mysql://localhost:3306/f1_manager";
    //Variable privada, fija y de la clase del tipo String que guarda el usuario de MySQL con el que nos conectamos
    private static final String USUARIO = "root";
    //Variable privada, fija y de la clase del tipo String que guarda la contraseña de ese usuario de MySQL
    private static final String CONTRASENA = "p14048ci";

    //Constructor privado y vacío: como todo en esta clase es static, no hace falta crear objetos "ConexionMySQL", por eso se bloquea que alguien la instancie
    private ConexionMySQL() {
    }

    //Abre una conexión nueva hacia MySQL usando la URL, el usuario y la contraseña de arriba.
    //Quien la use es responsable de cerrarla despues (por eso siempre se llama dentro de un try-with-resources, que la cierra solo al terminar).
    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}
