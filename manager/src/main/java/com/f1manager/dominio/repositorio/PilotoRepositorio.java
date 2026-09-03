//Este es un "puerto" (en arquitectura hexagonal): una interfaz que define QUÉ se puede hacer con los
//pilotos guardados (listar, insertar, actualizar, eliminar), pero sin decir CÓMO se hace ni dónde se
//guardan de verdad. La capa de aplicación (DataStore) solo conoce este contrato; quien lo cumple de
//verdad es un adaptador (por ejemplo PilotoRepositorioMySQL, que guarda todo en MySQL). Así, si el
//día de mañana se quisiera cambiar de base de datos, solo habría que escribir OTRO adaptador que
//implemente esta misma interfaz, sin tocar nada de la capa de aplicación ni de la UI.

//Esta es la ruta que usa este .java
package com.f1manager.dominio.repositorio;

//Trae la clase Piloto del dominio, porque este puerto trabaja con objetos Piloto
import com.f1manager.dominio.modelo.Piloto;
//Trae RolPiloto, el enum de Líder/Escudero que necesita el método para insertar un piloto nuevo
import com.f1manager.dominio.modelo.RolPiloto;
//Importa la interfaz List, que define el comportamiento general de una lista en Java (sirve como plantilla para clases como ArrayList)
import java.util.List;

//Una interfaz pública llamada "PilotoRepositorio": cualquier clase que "implements" esto queda
//obligada a tener estos 4 métodos con esta misma firma (nombre, parámetros y tipo de retorno)
public interface PilotoRepositorio {

    //Debe devolver la lista completa de pilotos guardados (de donde sea que los guarde el adaptador)
    List<Piloto> listarTodos();

    //Debe guardar un piloto nuevo con todos estos datos y devolver el id que le haya asignado el almacenamiento
    int insertarYObtenerId(String nombre, String equipo, RolPiloto rol, int experiencia,
                            int curva, int adelantamiento, int recta, int lluvia, int seco, int extremo,
                            String imagenUrl);

    //Debe guardar los datos actuales del piloto recibido (ya lo busca por su id, que el piloto ya trae)
    void actualizar(Piloto piloto);

    //Debe borrar el piloto que tenga el id indicado
    void eliminar(int id);
}
