//Define un tipo de "error controlado" que se usa para avisar cuando el usuario 
//ingresó datos inválidos, sin que el programa se cierre de golpe.

//Esta es la ruta que usa este .java
package com.f1manager.dominio.excepcion;

//Es una clase publica llamada "ValidacionException" que funciona como un error oficial del sistema
public class ValidacionException extends Exception {
    //Dentro de la clase pública hay un constructor público que recibe un mensaje de tipo String cuando 
    //lanzamos el error (usando throw new), y se lo pasa a la clase padre de Java mediante super(mensaje) para registrar la causa del fallo.
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
