//Este es un "puerto" (en arquitectura hexagonal): una interfaz que define QUÉ se puede hacer con los
//circuitos guardados (listar, insertar, actualizar, eliminar), pero sin decir CÓMO se hace ni dónde
//se guardan de verdad. La capa de aplicación (DataStore) solo conoce este contrato; quien lo cumple
//de verdad es un adaptador (por ejemplo CircuitoRepositorioMySQL, que guarda todo en MySQL). Así, si
//el día de mañana se quisiera cambiar de base de datos, solo habría que escribir OTRO adaptador que
//implemente esta misma interfaz, sin tocar nada de la capa de aplicación ni de la UI.

//Esta es la ruta que usa este .java
package com.f1manager.dominio.repositorio;

//Trae la clase Circuito del dominio, porque este puerto trabaja con objetos Circuito
import com.f1manager.dominio.modelo.Circuito;
//Importa la interfaz List, que define el comportamiento general de una lista en Java (sirve como plantilla para clases como ArrayList)
import java.util.List;

//Una interfaz pública llamada "CircuitoRepositorio": cualquier clase que "implements" esto queda
//obligada a tener estos 4 métodos con esta misma firma (nombre, parámetros y tipo de retorno)
public interface CircuitoRepositorio {

    //Debe devolver la lista completa de circuitos guardados (de donde sea que los guarde el adaptador)
    List<Circuito> listarTodos();

    //Debe guardar un circuito nuevo con estos datos y devolver el id que le haya asignado el almacenamiento
    int insertarYObtenerId(String nombre, String pais, double longitudKm, int vueltas, String descripcion);

    //Debe guardar los datos actuales del circuito recibido (ya lo busca por su id, que el circuito ya trae)
    void actualizar(Circuito circuito);

    //Debe borrar el circuito que tenga el id indicado
    void eliminar(int id);
}
