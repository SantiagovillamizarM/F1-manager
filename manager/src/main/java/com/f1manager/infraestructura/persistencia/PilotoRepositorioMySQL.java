//Se encarga de leer y escribir los pilotos directamente en la tabla MySQL "pilotos"
//(osea que hace de puente entre los objetos "Piloto" de Java y las filas de la base de datos).

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.persistencia;

//Trae la clase Piloto del dominio, para poder armar objetos Piloto con lo que se lee de la base de datos
import com.f1manager.dominio.modelo.Piloto;
//Trae RolPiloto, el enum que dice si el piloto es titular o reserva (o el rol que sea que maneje el juego)
import com.f1manager.dominio.modelo.RolPiloto;

//Trae la clase Connection, que representa la conexión abierta hacia MySQL
import java.sql.Connection;
//Trae PreparedStatement, la herramienta para preparar consultas SQL de forma segura (con los "?" en vez de meter el texto a mano, evitando inyección SQL)
import java.sql.PreparedStatement;
//Trae ResultSet, que es como una tabla con los resultados que devuelve una consulta SELECT, y que se recorre fila por fila
import java.sql.ResultSet;
//Trae SQLException, el error que lanza Java cuando algo sale mal hablando con la base de datos
import java.sql.SQLException;
//Trae Statement, que aquí se usa solo para pedirle a MySQL que nos devuelva el id autoincremental que le puso a la fila recién insertada
import java.sql.Statement;
//Importa la clase de ArrayList para poder usar listas (Son las que tienen <>)
import java.util.ArrayList;
//Importa la interfaz List, que define el comportamiento general de una lista en Java (sirve como plantilla para clases como ArrayList)
import java.util.List;

//Una clase final (no se puede heredar) y sin "public" (osea que solo se puede usar dentro de este mismo paquete) llamada "PilotoRepositorioMySQL"
final class PilotoRepositorioMySQL {

    //Constructor privado y vacío: todos los métodos de aquí son static, entonces no hace falta crear objetos de esta clase
    private PilotoRepositorioMySQL() {
    }

    //Este método trae TODOS los pilotos guardados en la tabla "pilotos" de MySQL y los devuelve como una lista de objetos Piloto
    static List<Piloto> listarTodos() {
        //El texto de la consulta SQL: le pide a MySQL todas las columnas de la tabla pilotos
        String sql = "SELECT id, nombre, equipo, rol, experiencia_anios, habilidad_curva, "
                + "habilidad_adelantamiento, habilidad_recta, habilidad_lluvia, habilidad_seco, "
                + "habilidad_extremo, imagen_url FROM pilotos";
        //Lista vacía donde se van a ir guardando los pilotos que se lean de la base de datos
        List<Piloto> pilotos = new ArrayList<>();
        //try-with-resources: abre la conexión, prepara la consulta y ejecuta el SELECT, y al terminar (o si algo falla) cierra las tres cosas solo
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            //rs.next() mueve el cursor del ResultSet a la siguiente fila y devuelve false cuando ya no quedan filas, por eso funciona como condición del while
            while (rs.next()) {
                //Arma el Piloto con los datos de la fila actual; RolPiloto.valueOf() convierte el texto guardado (ej. "TITULAR") de vuelta al valor real del enum
                Piloto piloto = new Piloto(rs.getInt("id"), rs.getString("nombre"), rs.getString("equipo"),
                        RolPiloto.valueOf(rs.getString("rol")), rs.getInt("experiencia_anios"),
                        rs.getInt("habilidad_curva"), rs.getInt("habilidad_adelantamiento"),
                        rs.getInt("habilidad_recta"), rs.getInt("habilidad_lluvia"),
                        rs.getInt("habilidad_seco"), rs.getInt("habilidad_extremo"));
                //La imagen no se usa tal cual, se manda al método de abajo para resolverla bien (puede ser una URL completa o solo un nombre de archivo)
                piloto.setImagenUrl(resolverImagen(rs.getString("imagen_url")));
                //Agrega el piloto recién armado a la lista de resultados
                pilotos.add(piloto);
            }
        } catch (SQLException ex) {
            //Si algo sale mal, se convierte el error en uno más simple de entender y se relanza
            throw new RuntimeException("No se pudieron cargar los pilotos desde MySQL: " + ex.getMessage(), ex);
        }
        //Devuelve la lista ya llena con todos los pilotos leídos
        return pilotos;
    }

    //Este método decide cómo interpretar el valor de imagen guardado en la base de datos:
    //si ya es una URL completa (una foto que el usuario subió, o un avatar elegido al registrarlo) se usa tal cual.
    //Si es solo un nombre de archivo (foto real de un piloto predeterminado, ej. "lewis.jpg"), se busca dentro
    //de la carpeta de recursos /imagenes/corredores predeterminados. Así en la base de datos se guarda algo
    //portable (un nombre) y no una ruta absoluta que solo serviría en esta máquina.
    private static String resolverImagen(String valorGuardado) {
        //Si no hay nada guardado (o está vacío/en blanco), no hay imagen que resolver
        if (valorGuardado == null || valorGuardado.isBlank()) {
            return null;
        }
        // Una URL completa empieza con un esquema (ej. "file:/..." o "http://..."). En Windows,
        // "file:" URIs suelen tener una sola barra ("file:/C:/..."), no "file://", así que buscar
        // literalmente "://" no las detectaba y esos valores caían al caso de "nombre de archivo".
        //Esta expresión regular (matches) verifica si el texto empieza con letras seguidas de ":" (el patrón de un esquema tipo "file:" o "http:")
        if (valorGuardado.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:.*")) {
            return valorGuardado;
        }
        //Si no es una URL, se busca el archivo dentro de los recursos empaquetados del programa (carpeta de imágenes predeterminadas)
        var recurso = PilotoRepositorioMySQL.class.getResource("/imagenes/corredores predeterminados/" + valorGuardado);
        //Operador ternario: si encontró el recurso, devuelve su ruta como texto (toExternalForm); si no lo encontró, devuelve null
        return recurso != null ? recurso.toExternalForm() : null;
    }

    //Inserta un piloto nuevo en MySQL y devuelve el id que MySQL le asignó automáticamente (autoincremental)
    static int insertarYObtenerId(String nombre, String equipo, RolPiloto rol, int experiencia,
                                   int curva, int adelantamiento, int recta, int lluvia, int seco, int extremo,
                                   String imagenUrl) {
        //Consulta con 11 "?" que son los espacios reservados donde luego se ponen los valores reales de forma segura
        String sql = "INSERT INTO pilotos (nombre, equipo, rol, experiencia_anios, habilidad_curva, "
                + "habilidad_adelantamiento, habilidad_recta, habilidad_lluvia, habilidad_seco, "
                + "habilidad_extremo, imagen_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        //Statement.RETURN_GENERATED_KEYS le avisa a MySQL que después vamos a pedirle el id que generó para esta fila nueva
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            //Rellena cada "?" de la consulta, en orden, con el dato correspondiente que llega por parámetro
            stmt.setString(1, nombre);
            stmt.setString(2, equipo);
            //.name() convierte el enum RolPiloto a su texto (ej. TITULAR -> "TITULAR") para poder guardarlo como String en MySQL
            stmt.setString(3, rol.name());
            stmt.setInt(4, experiencia);
            stmt.setInt(5, curva);
            stmt.setInt(6, adelantamiento);
            stmt.setInt(7, recta);
            stmt.setInt(8, lluvia);
            stmt.setInt(9, seco);
            stmt.setInt(10, extremo);
            stmt.setString(11, imagenUrl);
            //Ejecuta el INSERT de verdad contra la base de datos
            stmt.executeUpdate();
            //try-with-resources otra vez: abre un ResultSet chiquito solo con la clave generada y lo cierra solo al terminar
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                //Mueve el cursor a la única fila que trae este ResultSet (el id nuevo)
                claves.next();
                //Devuelve ese id (la primera y única columna, por eso el índice 1)
                return claves.getInt(1);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo guardar el piloto en MySQL: " + ex.getMessage(), ex);
        }
    }

    //Guarda en MySQL los datos actuales (ya modificados en memoria) de un piloto que ya existía, buscándolo por su id
    static void actualizar(Piloto piloto) {
        //Consulta UPDATE: cambia todas las columnas menos el id, y usa el id solo para saber cuál fila tocar (WHERE id = ?)
        String sql = "UPDATE pilotos SET nombre = ?, equipo = ?, rol = ?, experiencia_anios = ?, "
                + "habilidad_curva = ?, habilidad_adelantamiento = ?, habilidad_recta = ?, "
                + "habilidad_lluvia = ?, habilidad_seco = ?, habilidad_extremo = ?, imagen_url = ? WHERE id = ?";
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            //Rellena cada "?" con el dato actual del objeto piloto que llega por parámetro
            stmt.setString(1, piloto.getNombre());
            stmt.setString(2, piloto.getEquipo());
            stmt.setString(3, piloto.getRol().name());
            stmt.setInt(4, piloto.getExperienciaAnios());
            stmt.setInt(5, piloto.getHabilidadCurva());
            stmt.setInt(6, piloto.getHabilidadAdelantamiento());
            stmt.setInt(7, piloto.getHabilidadRecta());
            stmt.setInt(8, piloto.getHabilidadLluvia());
            stmt.setInt(9, piloto.getHabilidadSeco());
            stmt.setInt(10, piloto.getHabilidadExtremo());
            stmt.setString(11, piloto.getImagenUrl());
            //Este último "?" es el del WHERE, para que MySQL sepa exactamente qué fila actualizar
            stmt.setInt(12, piloto.getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo actualizar el piloto en MySQL: " + ex.getMessage(), ex);
        }
    }

    //Borra (DELETE) de MySQL el piloto que tenga el id indicado
    static void eliminar(int id) {
        String sql = "DELETE FROM pilotos WHERE id = ?";
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo eliminar el piloto en MySQL: " + ex.getMessage(), ex);
        }
    }
}
