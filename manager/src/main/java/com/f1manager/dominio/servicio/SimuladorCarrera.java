//Esta clase es el motor matematico del juego: calcula el resultado de una carrera combinando
//al piloto, su vehiculo, el clima y la configuracion elegida, usando una formula con varios
//multiplicadores mas una variacion aleatoria controlada (para que no de siempre el mismo resultado).

//Esta es la ruta que usa este .java
package com.f1manager.dominio.servicio;
//Trae de una sola vez todas las clases del paquete "modelo" (Circuito, Clima, Piloto, Monoplaza, etc),
//porque este simulador necesita usar casi todas para poder calcular la carrera.
import com.f1manager.dominio.modelo.*;

//Trae ArrayList, una lista a la que se le pueden ir agregando elementos (aca se usa para ir
//guardando los resultados y los tiempos de cada vuelta a medida que se van calculando).
import java.util.ArrayList;
//Trae Comparator, que sirve para decirle a Java "como" ordenar una lista de objetos
//(aca se usa para ordenar a los pilotos segun su posicion final en la carrera).
import java.util.Comparator;
//Trae la interfaz general List, que es el "tipo" que usan las listas (ArrayList es una
//implementacion de esto) para poder recibirlas y devolverlas como parametros.
import java.util.List;
//Trae Random, la herramienta de Java para generar numeros al azar (se usa para sortear el clima
//real, decidir si hay choques, y meterle "ruido" a los tiempos calculados).
import java.util.Random;
//Trae DoublePredicate, que representa "una pregunta que recibe un numero decimal (double) y
//responde true o false" (aca se usa para preguntar si cierto punto de la vuelta es una curva).
import java.util.function.DoublePredicate;
//Trae Function, que representa "una funcion que recibe un dato y devuelve otro dato"
//(aca se usa para, dado un piloto, conseguir el monoplaza (auto) que le corresponde).
import java.util.function.Function;

//La formula base es: tiempoBase = vueltas * segundosPorKmBase * longitudKm
//A partir de ese numero base se le aplican multiplicadores segun:
//  - La habilidad del piloto (va de 0 a 100).
//  - Los años de experiencia del piloto (le da un pequeño bono extra).
//  - La velocidad maxima y la aceleracion del monoplaza (el auto).
//  - La carga aerodinamica configurada (afecta agarre y velocidad punta).
//  - El modo de conduccion configurado (afecta ritmo y riesgo).
//  - El clima que salio en la carrera (afecta tiempo y que tan variable es).
//Al final se le suma una variacion aleatoria pero acotada (limitada, no se puede ir a cualquier
//extremo), como si fuera un "ruido" de carrera, para que el resultado no de siempre igual.
//Una clase publica llamada "SimuladorCarrera"
public class SimuladorCarrera {

    //Constante fija: cuantos segundos "cuesta" recorrer un kilometro en condiciones base, antes
    //de aplicarle cualquier multiplicador (piloto, auto, clima, etc). Es el punto de partida de todo.
    private static final double SEGUNDOS_POR_KM_BASE = 24.0;
    // Neumático adecuado para el clima real (ayuda) vs. inadecuado (se resiente / "se desgasta más rápido").
    private static final double FACTOR_NEUMATICO_ACERTADO = 0.97;
    private static final double FACTOR_NEUMATICO_INADECUADO = 1.08;
    // Cuánto más rápido se desgasta el neumático cuando el compuesto no corresponde al clima real.
    private static final double MULTIPLICADOR_DESGASTE_INADECUADO = 1.8;
    // Cuánto puede llegar a ralentizar un neumático 100% desgastado, comparado con uno nuevo.
    private static final double PENALIZACION_MAX_POR_DESGASTE = 0.15;
    // Cuánto crece la variabilidad (para bien o para mal) del tiempo por cada PSI de distancia
    // respecto a la presión óptima, cuando ninguna combinación favorece una dirección clara.
    // En la presión óptima el efecto es exactamente 0.
    private static final double ESCALA_EFECTO_PRESION = 0.01;
    // Cuánto ayuda (o perjudica) por PSI acertar (o no) la dirección de presión favorable,
    // cuando sí hay una combinación de compuesto/modo/carga/clima que la determina.
    private static final double ESCALA_EFECTO_PRESION_DIRIGIDA = 0.012;

    private final Random random = new Random();


    //Este es el metodo principal: simula toda la carrera para la lista de pilotos que le pasen.
    //circuito = la pista donde se corre, climaElegido = el clima que se configuro (puede ser
    //Aleatorio), pilotos = todos los que van a correr, monoplazaDe = una funcion que, dado un
    //piloto, devuelve el auto (monoplaza) que le corresponde, y esCurvaEnFraccion sirve para
    //preguntar si, en un punto cualquiera de la vuelta (un numero de 0.0 a 1.0), ese pedazo del
    //trazado real es una curva o una recta. Esto lo pasa quien llama a este metodo (la pantalla,
    //que es la que dibuja el circuito) para que esta clase no tenga que saber nada de dibujar pistas.
    public ResultadoSimulacion simular(Circuito circuito, Clima climaElegido, List<Piloto> pilotos,
                                        Function<Piloto, Monoplaza> monoplazaDe, DoublePredicate esCurvaEnFraccion) {

        //Aca se decide el clima real de la carrera: si eligieron Aleatorio se sortea entre
        //Seco/Lluvioso/Extremo, si no, se queda tal cual lo eligieron.
        Clima climaReal = climaElegido.resolver(random);
        //Aplica la formula base explicada arriba de la clase: vueltas * segundos por km * largo de la pista.
        double tiempoBase = circuito.getVueltas() * SEGUNDOS_POR_KM_BASE * circuito.getLongitudKm();

        //Aca se van a ir guardando los resultados de cada piloto a medida que se calculan.
        List<ResultadoCarrera> resultados = new ArrayList<>();

        //Recorre a todos los pilotos uno por uno para calcularle su carrera individual.
        for (Piloto piloto : pilotos) {

            //Busca el auto (monoplaza) que le corresponde a este piloto usando la funcion recibida.
            Monoplaza monoplaza = monoplazaDe.apply(piloto);
            //Calcula el tiempo total de este piloto aplicando todos los multiplicadores (habilidad, auto, clima, etc).
            double tiempo = calcularTiempoPiloto(tiempoBase, piloto, monoplaza, climaReal);
            //Saca el promedio por vuelta para poder repartir ese tiempo total entre todas las vueltas de la carrera.
            double tiempoPromedioPorVuelta = tiempo / circuito.getVueltas();
            //Simula vuelta por vuelta (con su propio desgaste de neumaticos, paradas en boxes, temperaturas, etc).
            VueltasSimuladas vueltas = simularVueltas(monoplaza, climaReal, circuito.getVueltas(), tiempoPromedioPorVuelta);
            //Suma todos los tiempos de cada vuelta ya simulada, para tener el tiempo final real (con paradas incluidas).
            double tiempoFinal = vueltas.tiempos().stream().mapToDouble(Double::doubleValue).sum();

            //Arma el resultado de este piloto con todo lo que se calculo, para poder mostrarlo despues.
            ResultadoCarrera resultado = new ResultadoCarrera(piloto, monoplaza, tiempoFinal);
            resultado.setTiemposPorVuelta(vueltas.tiempos());
            resultado.setDesgastePorVuelta(vueltas.desgaste());
            resultado.setVueltasDePit(vueltas.vueltasDePit());
            resultado.setTemperaturaLlantasPorVuelta(vueltas.temperaturaLlantas());
            resultado.setTemperaturaMotorPorVuelta(vueltas.temperaturaMotor());
            resultado.setVelocidadMaximaAlcanzada(generarVelocidadMaxima(monoplaza));
            resultados.add(resultado);
        }

        //Con todos los pilotos ya "corriendo", revisa si a alguno le toca chocar en algun punto de la carrera.
        aplicarChoques(circuito.getVueltas(), esCurvaEnFraccion, resultados);

        //Ordena a los pilotos: primero los que terminaron antes que los que no (DNF), y entre los
        //que si terminaron, del que hizo menos tiempo al que hizo mas (ver comparadorClasificacion()).
        resultados.sort(comparadorClasificacion());
        //Ya ordenados, les asigna la posicion final (1ro, 2do, 3ro...) segun el lugar que les quedo en la lista.
        for (int i = 0; i < resultados.size(); i++) {
            resultados.get(i).setPosicion(i + 1);
        }

        return new ResultadoSimulacion(climaReal, resultados);
    }

    //Este metodo arma la "regla" de como ordenar la lista de resultados (Comparator es justamente
    //eso: una regla de comparacion que se le puede pasar al metodo sort() de una lista).
    private static Comparator<ResultadoCarrera> comparadorClasificacion() {
        //Esto es una funcion lambda (una funcion cortita escrita "al vuelo"): recibe dos resultados
        //(a y b) y devuelve un numero negativo, positivo o cero para decir cual va primero.
        return (a, b) -> {
            if (a.isDnf() != b.isDnf()) {
                return a.isDnf() ? 1 : -1; // los que no terminan quedan siempre después
            }
            if (a.isDnf()) {
                // Entre los que no terminaron, gana quien llegó más lejos antes de chocar.
                return Double.compare(b.getProgresoChoque(), a.getProgresoChoque());
            }
            return Double.compare(a.getTiempoSegundos(), b.getTiempoSegundos());
        };
    }

    // Cuántas veces por vuelta se evalúa el riesgo de choque (repartidas a lo largo del trazado).
    private static final int CHEQUEOS_POR_VUELTA = 8;
    // Probabilidad base de choque por chequeo, antes de aplicar la habilidad del piloto.
    private static final double PROB_BASE_CHOQUE = 0.0005;
    // De los choques que ocurren, qué proporción involucra también a otro piloto (ambos DNF).
    private static final double PROB_CHOQUE_CON_RIVAL = 0.25;

    //Este metodo recorre la carrera vuelta por vuelta y, en varios puntos de cada una (los
    //"chequeos"), evalua si algun piloto choca. La probabilidad es baja pero nunca es cero del
    //todo, y depende de su habilidad de adelantamiento combinada con su habilidad en curva o en
    //recta (segun el tramo real del trazado en el que este en ese momento). Un choque puede
    //ademas involucrar al rival con el que mas cerca este peleando en ese instante, dejando a
    //los dos fuera de carrera (DNF, osea que no terminan) en el mismo punto de la pista.
    private void aplicarChoques(int vueltas, DoublePredicate esCurvaEnFraccion, List<ResultadoCarrera> resultados) {
        //Recorre vuelta por vuelta, desde la 1 hasta la ultima.
        for (int vuelta = 1; vuelta <= vueltas; vuelta++) {
            //Dentro de cada vuelta, la revisa en varios "chequeos" repartidos a lo largo de ella.
            for (int chequeo = 0; chequeo < CHEQUEOS_POR_VUELTA; chequeo++) {
                //fraccionVuelta va de 0.0 (arranca la vuelta) a un numero cercano a 1.0 (casi terminandola).
                double fraccionVuelta = chequeo / (double) CHEQUEOS_POR_VUELTA;
                //Le pregunta a la funcion que le pasaron (viene de la UI, que conoce la forma real
                //del circuito) si en ese punto exacto de la vuelta hay una curva o una recta.
                boolean curva = esCurvaEnFraccion.test(fraccionVuelta);

                //Revisa a cada piloto que sigue en carrera para ver si le toca chocar en este chequeo.
                for (ResultadoCarrera resultado : resultados) {
                    if (resultado.isDnf()) {
                        continue; // si ya choco antes, se lo salta
                    }
                    //Mira si, en la vuelta actual, el desgaste del neumatico de este piloto ya esta al limite.
                    boolean desgasteAlMaximo = vuelta - 1 < resultado.getDesgastePorVuelta().size()
                            && resultado.getDesgastePorVuelta().get(vuelta - 1) >= UMBRAL_DESGASTE_RIESGOSO;
                    //Calcula que tan probable es que este piloto choque justo en este chequeo.
                    double probabilidad = probabilidadChoque(resultado.getPiloto(), curva, desgasteAlMaximo);
                    //random.nextDouble() tira un numero al azar entre 0.0 y 1.0; si sale mayor o
                    //igual a la probabilidad, no pasa nada y sigue con el siguiente piloto.
                    if (random.nextDouble() >= probabilidad) {
                        continue;
                    }

                    //Si llego hasta aca, le toco chocar: se calcula el progreso exacto de la carrera
                    //en el que paso (numero de vuelta + la fraccion) y se marca como DNF en ese punto.
                    double progreso = (vuelta - 1) + fraccionVuelta;
                    resultado.marcarChoque(progreso);

                    //Con una probabilidad de PROB_CHOQUE_CON_RIVAL, el choque tambien se lleva a otro piloto.
                    if (random.nextDouble() < PROB_CHOQUE_CON_RIVAL) {
                        //Busca al rival mas cercano (el que mas cerca este peleando con este piloto) para involucrarlo.
                        ResultadoCarrera rival = buscarRivalMasCercano(resultado, vuelta, fraccionVuelta, resultados);
                        if (rival != null) {
                            rival.marcarChoque(progreso);
                            //Se guardan mutuamente como el rival con el que chocaron, para poder mostrarlo despues.
                            resultado.setRivalChoque(rival.getPiloto());
                            rival.setRivalChoque(resultado.getPiloto());
                        }
                    }
                }
            }
        }
    }

    //Calcula la probabilidad de choque en un chequeo puntual: es mas baja cuanto mejor sea la
    //habilidad relevante del piloto (curva o recta segun donde este, combinada con adelantamiento),
    //nunca llega a ser exactamente 0 (siempre hay algo de riesgo), y sube un poco si justo en ese
    //momento el neumatico esta al limite de desgaste.
    private double probabilidadChoque(Piloto piloto, boolean curva, boolean desgasteAlMaximo) {
        //Segun si esta en curva o en recta, usa la habilidad que corresponda a ese tramo.
        int habilidadTramo = curva ? piloto.getHabilidadCurva() : piloto.getHabilidadRecta();
        //Promedia esa habilidad de tramo con la de adelantamiento (las dos influyen en el riesgo).
        double habilidadPromedioRelevante = (habilidadTramo + piloto.getHabilidadAdelantamiento()) / 2.0;
        //Entre mas baja la habilidad, mas alto sale este factor (mas riesgo). Math.max(0.15, ...)
        //evita que el factor baje de 0.15, para que nunca el riesgo sea practicamente cero.
        double factorRiesgo = Math.max(0.15, (100 - habilidadPromedioRelevante) / 100.0);
        double probabilidad = PROB_BASE_CHOQUE * factorRiesgo;
        //Si el neumatico esta al limite de desgaste en este momento, se multiplica el riesgo (mas facil que choque).
        if (desgasteAlMaximo) {
            probabilidad *= MULTIPLICADOR_RIESGO_DESGASTE;
        }
        return probabilidad;
    }

    //Busca, entre los pilotos que siguen en carrera, el que tenga el tiempo acumulado mas
    //parecido al de "propio" (osea el que esta peleando rueda a rueda con el en ese momento).
    private ResultadoCarrera buscarRivalMasCercano(ResultadoCarrera propio, int vuelta, double fraccionVuelta,
                                                    List<ResultadoCarrera> resultados) {
        double tiempoPropio = tiempoAcumuladoHasta(propio, vuelta, fraccionVuelta);
        ResultadoCarrera masCercano = null;
        double menorDiferencia = Double.MAX_VALUE; // arranca en el numero mas grande posible para que cualquier diferencia real sea menor
        for (ResultadoCarrera candidato : resultados) {
            if (candidato == propio || candidato.isDnf()) {
                continue; // no se compara consigo mismo, ni con los que ya chocaron
            }
            //Compara que tan parecido es el tiempo acumulado del candidato con el del piloto propio.
            double diferencia = Math.abs(tiempoAcumuladoHasta(candidato, vuelta, fraccionVuelta) - tiempoPropio);
            //Si esta diferencia es la mas chica hasta ahora, este candidato queda como el "mas cercano".
            if (diferencia < menorDiferencia) {
                menorDiferencia = diferencia;
                masCercano = candidato;
            }
        }
        return masCercano;
    }

    //Calcula el tiempo acumulado de un piloto hasta cierta fraccion de una vuelta, usando los
    //tiempos por vuelta que ya se le generaron (suma las vueltas completas anteriores mas el
    //pedacito de la vuelta actual que ya recorrio, segun la fraccion).
    private double tiempoAcumuladoHasta(ResultadoCarrera resultado, int vuelta, double fraccionVuelta) {
        List<Double> tiempos = resultado.getTiemposPorVuelta();
        double acumulado = 0;
        //Suma el tiempo de todas las vueltas anteriores a la actual (completas).
        for (int i = 0; i < vuelta - 1; i++) {
            acumulado += tiempos.get(i);
        }
        //Le suma solo la parte proporcional de la vuelta actual (segun que tanto lleva recorrido).
        acumulado += tiempos.get(vuelta - 1) * fraccionVuelta;
        return acumulado;
    }

    //Escoge la habilidad del piloto que corresponde al clima real de la carrera (cada piloto
    //tiene una habilidad distinta para Seco, Lluvia y Extremo). El switch de aca abajo es un
    //"switch expression" (una forma moderna de switch que directamente devuelve un valor).
    private int habilidadPorClima(Piloto piloto, Clima climaReal) {
        return switch (climaReal) {
            case SECO -> piloto.getHabilidadSeco();
            case LLUVIOSO -> piloto.getHabilidadLluvia();
            case EXTREMO -> piloto.getHabilidadExtremo();
            case ALEATORIO -> piloto.getHabilidadSeco(); // climaReal ya viene resuelto, no debería ocurrir
        };
    }

    //Este es el metodo que calcula el tiempo total de UN piloto en la carrera: arranca del
    //tiempoBase (la formula de vueltas*segundos*km) y le va aplicando, uno por uno, todos los
    //multiplicadores segun el piloto, el auto, la configuracion y el clima, y al final le suma
    //un poco de azar. Cada "tiempo *= algo" hace que el tiempo suba (mas lento) si "algo" es
    //mayor a 1, o que baje (mas rapido) si "algo" es menor a 1.
    private double calcularTiempoPiloto(double tiempoBase, Piloto piloto, Monoplaza monoplaza, Clima climaReal) {
        double tiempo = tiempoBase;

        // --- Habilidad del piloto en el clima real de la carrera: hasta ±10% según qué tan lejos esté de 80 ---
        //Si la habilidad es menor a 80, este factor sale mayor a 1 (va mas lento); si es mayor a
        //80, sale menor a 1 (va mas rapido). 80 es el punto "neutro" y el maximo efecto es del 20%.
        double factorHabilidad = 1.0 + ((80 - habilidadPorClima(piloto, climaReal)) / 100.0) * 0.20;
        tiempo *= factorHabilidad;

        // --- Experiencia: pequeño bono de hasta -3% con 20+ años ---
        //Math.min(..., 20) le pone un techo: no importa si tiene 25 o 30 años de experiencia, el
        //bono no sigue creciendo despues de los 20. Con el maximo (20 años) el bono es -3% (20*0.0015).
        double factorExperiencia = 1.0 - Math.min(piloto.getExperienciaAnios(), 20) * 0.0015;
        tiempo *= factorExperiencia;

        // --- Vehículo: velocidad máxima y aceleración ---
        //Si no hay monoplaza asignado (viene null) se salta todo este bloque y el auto no afecta el tiempo.
        if (monoplaza != null) {
            //Entre mas lejos este la velocidad maxima del auto por debajo de 350 km/h, mas crece
            //este factor (va mas lento); si supera los 350, el factor baja de 1 (va mas rapido).
            double factorVelocidad = 1.0 + ((350 - monoplaza.getVelocidadMaxKmh()) / 350.0) * 0.10;
            tiempo *= factorVelocidad;

            //La aceleracion de 0 a 100 se mide en segundos: entre mas alto ese numero (osea que se
            //demora mas en acelerar), mas crece este factor y mas lento va. 2.5 es el punto de referencia.
            double factorAceleracion = 1.0 + ((monoplaza.getAceleracion0a100() - 2.5) * 0.02);
            tiempo *= factorAceleracion;

            // --- Configuración: carga aerodinámica y modo de conducción ---
            //Cada opcion de carga aerodinamica (Baja/Media/Alta) y cada modo de conduccion
            //(Normal/Agresivo/Ahorro) ya trae sus propios factores definidos en su enum, aca solo se aplican.
            CargaAerodinamica carga = monoplaza.getCargaAerodinamica();
            tiempo *= carga.getFactorVelocidadPunta();
            tiempo *= carga.getFactorAgarre();

            ModoConduccion modo = monoplaza.getModoConduccion();
            tiempo *= modo.getFactorRitmo();

            // --- Neumáticos: ritmo propio del compuesto, y acierto o error según el clima ---
            TipoNeumatico neumatico = monoplaza.getTipoNeumatico();
            if (neumatico != null) {
                //Cada compuesto de neumatico (Blando, Medio, Duro, etc) tiene su propio ritmo de fabrica.
                tiempo *= neumatico.getFactorRitmo();
                //Se considera "mojado" tanto el clima Lluvioso como el Extremo.
                boolean climaMojado = climaReal == Clima.LLUVIOSO || climaReal == Clima.EXTREMO;
                //Operador ternario (una forma corta de escribir un if/else que devuelve un valor):
                //si el neumatico es para lluvia Y el clima esta mojado, o si NINGUNO de los dos pasa
                //(neumatico de seco con clima seco), entonces se acerto con el neumatico y va un
                //poco mas rapido; si no, se equivocaron de compuesto y va mas lento.
                tiempo *= (neumatico.isParaLluvia() == climaMojado)
                        ? FACTOR_NEUMATICO_ACERTADO
                        : FACTOR_NEUMATICO_INADECUADO;
            }

            // --- Presión de aire: en el valor óptimo no cambia nada. Si la combinación actual
            // (compuesto, modo, carga, clima) favorece presión alta o baja, acertar la dirección
            // ayuda y equivocarla perjudica; si ninguna combinación aplica, no hay una dirección
            // "correcta" que se pueda saber de antemano, así que el efecto es puro azar ---
            //Que tan lejos (y en que direccion, por eso "firmada": el numero puede ser positivo o
            //negativo) esta la presion actual respecto a la presion optima del auto.
            double desviacionFirmada = monoplaza.getPresionAire() - Monoplaza.PRESION_OPTIMA;
            //Le pregunta al metodo de mas abajo si para esta combinacion de compuesto/clima/modo/carga
            //conviene presion alta (1), baja (-1), o si da igual (0).
            int direccionFavorable = direccionPresionFavorable(neumatico, climaReal, modo, carga);
            if (direccionFavorable == 0) {
                //Si da igual (0), no hay una direccion "correcta" que se pueda saber de antemano,
                //asi que el efecto de la presion es puro azar: puede ayudar o perjudicar por igual.
                if (desviacionFirmada != 0) {
                    //random.nextGaussian() da un numero al azar con forma de campana (la mayoria de
                    //las veces sale cerca de 0, pocas veces sale un numero grande). Aca puede sumar o restar tiempo.
                    tiempo *= 1.0 + random.nextGaussian() * ESCALA_EFECTO_PRESION * Math.abs(desviacionFirmada);
                }
            } else {
                double alineacion = direccionFavorable * desviacionFirmada; // >0 si acertó la dirección
                //Si "alineacion" es positiva (acerto la direccion de presion favorable) el tiempo
                //baja (mas rapido); si es negativa (se equivoco de direccion) el tiempo sube (mas lento).
                tiempo *= 1.0 - alineacion * ESCALA_EFECTO_PRESION_DIRIGIDA;
                if (desviacionFirmada != 0) {
                    // Un poco de azar residual: acertar la dirección ayuda en promedio, pero no es infalible.
                    tiempo *= 1.0 + random.nextGaussian() * ESCALA_EFECTO_PRESION * 0.3 * Math.abs(desviacionFirmada);
                }
            }
        }

        // --- Clima ---
        //El clima tambien trae su propio factor de tiempo ya definido en su enum (Seco es el mas
        //rapido con 1.0, Extremo el mas lento).
        tiempo *= climaReal.getFactorTiempo();

        // --- Variabilidad aleatoria acotada (ruido de carrera) ---
        double variabilidadBase = 0.012; // 1.2% de variación estándar en condiciones normales
        //Si no hay monoplaza, se usa 1.0 (sin cambios); si hay, se usa el factor de variabilidad
        //de su modo de conduccion (Agresivo es mas inconsistente, Ahorro es mas parejo).
        double factorVariabilidadModo = monoplaza != null ? monoplaza.getModoConduccion().getFactorVariabilidad() : 1.0;
        //Junta todo en una sola desviacion: el tiempo total, el 1.2% base, el modo, y el clima
        //(si el clima no trae variabilidad definida, osea 0 o menos, se usa 1.0 para no romper la cuenta).
        double desviacion = tiempo * variabilidadBase * factorVariabilidadModo * (climaReal.getFactorVariabilidad() <= 0 ? 1.0 : climaReal.getFactorVariabilidad());
        //Genera el "ruido" real: un numero al azar con forma de campana multiplicado por la desviacion de arriba.
        double ruido = random.nextGaussian() * desviacion;
        // Se acota el ruido para evitar valores extremos poco realistas
        double limite = tiempo * 0.06 * factorVariabilidadModo;
        //Esto es un "clamp" (un recorte): Math.min lo topa por arriba y Math.max lo topa por abajo,
        //para que el ruido nunca se pase de estos limites en ninguna de las dos direcciones.
        ruido = Math.max(-limite, Math.min(limite, ruido));

        tiempo += ruido;

        //Por si con tantos multiplicadores el tiempo termino dando algo absurdamente bajo, esto
        //nunca deja que el tiempo final baje de la mitad del tiempo base.
        return Math.max(tiempo, tiempoBase * 0.5); // seguridad ante configuraciones extremas
    }

    //Este metodo decide si, para esta combinacion de compuesto, clima real, modo de conduccion y
    //carga aerodinamica, conviene presion alta (+1), baja (-1), o si no hay una direccion clara (0):
    //  - Blando + Agresivo + Seco -> alta (controla el calor y desgaste extremos de esa combinacion)
    //  - Duro + Ahorro -> baja (ayuda a poner en temperatura un compuesto que ya cuesta calentar)
    //  - Carga aerodinamica Baja -> alta (setup de velocidad punta: menos resistencia a la rodadura)
    //  - Carga aerodinamica Alta -> baja (setup de curvas tecnicas: mas agarre mecanico)
    //  - Neumatico de lluvia (intermedio o lluvia) en pista mojada -> baja (mas agarre mecanico)
    //Cuando varias de estas combinaciones aplican a la vez, sus puntos se suman; si no aplica
    //ninguna (por ejemplo Medio + Normal + Seco), el resultado final es 0 y no hay direccion favorable.
    private int direccionPresionFavorable(TipoNeumatico neumatico, Clima climaReal, ModoConduccion modo, CargaAerodinamica carga) {
        boolean climaMojado = climaReal == Clima.LLUVIOSO || climaReal == Clima.EXTREMO;
        int puntaje = 0;

        //Cada if de aca abajo es una de las reglas de la lista de arriba: si se cumple, suma o
        //resta un punto al puntaje total (varias reglas pueden cumplirse a la vez).
        if (neumatico == TipoNeumatico.BLANDO && modo == ModoConduccion.AGRESIVO && !climaMojado) {
            puntaje += 1;
        }
        if (neumatico == TipoNeumatico.DURO && modo == ModoConduccion.AHORRO) {
            puntaje -= 1;
        }
        if (carga == CargaAerodinamica.BAJA) {
            puntaje += 1;
        }
        if (carga == CargaAerodinamica.ALTA) {
            puntaje -= 1;
        }
        if (neumatico != null && neumatico.isParaLluvia() && climaMojado) {
            puntaje -= 1;
        }

        //Integer.signum devuelve solo el "signo" del numero: -1 si es negativo, 0 si es cero, o 1
        //si es positivo. Asi no importa si el puntaje sumado fue 1 o 2, el resultado siempre es -1, 0 o 1.
        return Integer.signum(puntaje);
    }

    // A partir de qué nivel de desgaste (cercano al máximo de 100) el piloto entra a boxes a cambiar neumáticos.
    private static final double UMBRAL_DESGASTE_PIT = 95.0;
    // Tiempo perdido por una parada en boxes (calle de pits + cambio de neumáticos), en segundos.
    private static final double TIEMPO_PERDIDO_EN_PIT = 22.0;
    // Desgaste a partir del cual, mientras recorre ese tramo, el riesgo de choque es un poco mayor.
    private static final double UMBRAL_DESGASTE_RIESGOSO = 95.0;
    // Cuánto se multiplica la probabilidad de choque cuando el desgaste está al límite (o sea, cuánto más riesgoso es).
    private static final double MULTIPLICADOR_RIESGO_DESGASTE = 1.6;

    // Temperaturas de referencia (°C) en condiciones normales, antes de aplicar compuesto/modo/clima/desgaste.
    private static final double TEMP_LLANTA_BASE = 90.0;
    private static final double TEMP_MOTOR_BASE = 95.0;

    //Un "record" es una forma corta de Java para crear una clase que solo guarda datos (aca guarda
    //el resultado completo de simular vuelta por vuelta a un piloto: sus tiempos, el desgaste de
    //neumaticos, en que vueltas paro en boxes, y las temperaturas de llantas y motor).
    private record VueltasSimuladas(List<Double> tiempos, List<Double> desgaste, List<Integer> vueltasDePit,
                                     List<Double> temperaturaLlantas, List<Double> temperaturaMotor) {
    }

    //Dice cuanto mas caliente (numero positivo) o mas frio (numero negativo) corre cada compuesto
    //de neumatico, comparado con la temperatura base. El switch expression de aca abajo directamente
    //devuelve el numero segun el compuesto que le pasen.
    private double factorTemperaturaCompuesto(TipoNeumatico neumatico) {
        if (neumatico == null) {
            return 0.0;
        }
        return switch (neumatico) {
            case BLANDO -> 8.0;
            case MEDIO -> 3.0;
            case DURO -> -4.0;
            case INTERMEDIO -> -3.0;
            case LLUVIA -> -6.0;
        };
    }

    //Este metodo simula la carrera de UN piloto vuelta por vuelta: el ritmo base varia un poco
    //entre vueltas (para que no sean todas identicas), y el desgaste de neumaticos (que se
    //acelera si el compuesto no corresponde al clima real) va penalizando el tiempo poco a poco.
    //Al llegar a un desgaste alto, el piloto entra a boxes: pierde tiempo en la parada, cambia de
    //neumaticos (a uno adecuado para el clima si el que traia no lo era) y el desgaste vuelve a 0.
    private VueltasSimuladas simularVueltas(Monoplaza monoplaza, Clima climaReal, int vueltas, double tiempoPromedioPorVuelta) {
        //Estas listas se van llenando vuelta por vuelta, una posicion por cada vuelta corrida.
        List<Double> tiempos = new ArrayList<>();
        List<Double> desgaste = new ArrayList<>();
        List<Integer> vueltasDePit = new ArrayList<>();
        List<Double> temperaturaLlantas = new ArrayList<>();
        List<Double> temperaturaMotor = new ArrayList<>();

        //Arranca con el neumatico que traiga puesto el auto (puede ser null si no hay monoplaza).
        TipoNeumatico neumaticoActual = monoplaza != null ? monoplaza.getTipoNeumatico() : null;
        //Se considera "mojado" tanto el clima Lluvioso como el Extremo.
        boolean climaMojado = climaReal == Clima.LLUVIOSO || climaReal == Clima.EXTREMO;
        double acumuladoDesgaste = 0; // arranca sin nada de desgaste, neumaticos nuevos

        // Cada piloto/auto tiene su propio ritmo de desgaste (no todos son iguales): depende del
        // modo de conducción configurado (agresivo desgasta más rápido, ahorro más lento) y de
        // una variación propia del auto/piloto, fija para toda la carrera (no cambia vuelta a vuelta).
        double factorModo = monoplaza != null ? monoplaza.getModoConduccion().getFactorDesgasteNeumatico() : 1.0;
        double factorIndividual = 0.85 + random.nextDouble() * 0.3; // entre 0.85 y 1.15, distinto por piloto

        //Recorre la carrera de este piloto vuelta por vuelta.
        for (int vuelta = 1; vuelta <= vueltas; vuelta++) {
            double variacionRitmo = 1 + random.nextGaussian() * 0.02; // ±2% entre vueltas
            double tiempoVuelta = tiempoPromedioPorVuelta * variacionRitmo;

            //Solo se aplica desgaste si el auto tiene neumatico asignado.
            if (neumaticoActual != null) {
                //Entre mas desgaste acumulado lleve, mas crece este factor y mas lento va esta
                //vuelta (hasta un maximo del 15% de penalizacion, definido en PENALIZACION_MAX_POR_DESGASTE).
                double factorDesgaste = 1.0 + (acumuladoDesgaste / 100.0) * PENALIZACION_MAX_POR_DESGASTE;
                tiempoVuelta *= factorDesgaste;

                //Cuanto se desgasta el neumatico en ESTA vuelta puntual: el ritmo propio del
                //compuesto, ajustado por el modo de conduccion y por la variacion individual de este piloto/auto.
                double ritmoDesgaste = neumaticoActual.getDesgastePorVuelta() * factorModo * factorIndividual;
                //Si el compuesto no es el que corresponde al clima real, se desgasta bastante mas rapido.
                if (neumaticoActual.isParaLluvia() != climaMojado) {
                    ritmoDesgaste *= MULTIPLICADOR_DESGASTE_INADECUADO;
                }
                // Incremento parejo dentro de la propia carrera de este piloto (sin ruido vuelta a
                // vuelta): empieza en 0 y sube de forma predecible, a SU ritmo particular.
                //Math.min(100, ...) evita que el desgaste se pase de 100 (el maximo posible).
                acumuladoDesgaste = Math.min(100, acumuladoDesgaste + ritmoDesgaste);
            }

            // Se guarda el desgaste alcanzado AL RECORRER esta vuelta (antes de resetear por el
            // pit, si corresponde), para que quede registrado que se condujo con el neumático al
            // límite justo en esta vuelta.
            tiempos.add(tiempoVuelta);
            desgaste.add(acumuladoDesgaste);

            // Temperaturas: suben con compuestos más blandos, conducción más agresiva, más
            // desgaste acumulado, y bajan con clima mojado (refrigeración del agua). Con algo de
            // variación vuelta a vuelta para que se vea que van cambiando, no un número fijo.
            double tempLlanta = TEMP_LLANTA_BASE
                    + factorTemperaturaCompuesto(neumaticoActual)
                    + (factorModo - 1.0) * 10.0
                    + (acumuladoDesgaste / 100.0) * 8.0
                    + (climaMojado ? -10.0 : 0.0)
                    + random.nextGaussian() * 2.5;
            temperaturaLlantas.add(tempLlanta);

            //La temperatura del motor sube segun que tan agresivo sea el modo de conduccion, y
            //tambien tiene su propia variacion al azar (no depende del compuesto ni del clima).
            double tempMotor = TEMP_MOTOR_BASE
                    + (factorModo - 1.0) * 15.0
                    + random.nextGaussian() * 3.0;
            temperaturaMotor.add(tempMotor);

            //Si el desgaste ya llego al umbral de pit Y todavia quedan vueltas por correr (no
            //tendria sentido entrar a boxes en la ultima vuelta), el piloto para a cambiar neumaticos.
            if (neumaticoActual != null && acumuladoDesgaste >= UMBRAL_DESGASTE_PIT && vuelta < vueltas) {
                //Le suma el tiempo perdido en boxes a la vuelta que se acaba de correr (no se
                //crea una vuelta nueva, se le agrega el tiempo a esta misma).
                tiempos.set(tiempos.size() - 1, tiempos.get(tiempos.size() - 1) + TIEMPO_PERDIDO_EN_PIT);
                vueltasDePit.add(vuelta); // guarda en que vuelta paro, para poder mostrarlo despues
                //Cambia de neumatico (puede quedar el mismo compuesto si ya era el correcto, o
                //cambiar a uno adecuado para el clima si no lo era).
                neumaticoActual = elegirNeumaticoParaPit(neumaticoActual, climaMojado);
                acumuladoDesgaste = 0; // neumaticos nuevos, el desgaste vuelve a cero
            }
        }

        return new VueltasSimuladas(tiempos, desgaste, vueltasDePit, temperaturaLlantas, temperaturaMotor);
    }

    //Decide con que neumatico sale el piloto despues de pasar por boxes: si el compuesto actual
    //ya era el correcto para el clima, se pone un juego nuevo del mismo; si no lo era, se cambia
    //a uno adecuado (Lluvia si esta mojado, Medio si esta seco).
    private TipoNeumatico elegirNeumaticoParaPit(TipoNeumatico actual, boolean climaMojado) {
        if (actual.isParaLluvia() == climaMojado) {
            return actual;
        }
        return climaMojado ? TipoNeumatico.LLUVIA : TipoNeumatico.MEDIO;
    }

    //Calcula una velocidad maxima "vistosa" para mostrar en pantalla, con algo de variacion al
    //azar para que no sea siempre exactamente la velocidad de ficha tecnica del auto.
    private double generarVelocidadMaxima(Monoplaza monoplaza) {
        if (monoplaza == null) return 0;
        double factor = 0.96 + random.nextDouble() * 0.06; // 96% a 102% de la velocidad máxima del auto
        return monoplaza.getVelocidadMaxKmh() * factor;
    }

    //Es una clase publica y fija (final) que envuelve el resultado completo de una simulacion:
    //el clima que realmente se uso (por si era Aleatorio) y la lista final de resultados ya ordenados.
    public static final class ResultadoSimulacion {
        //Guarda el clima que efectivamente salio en la carrera (ya resuelto, no el que se eligio antes de sortear).
        private final Clima climaReal;
        //Guarda la lista final con el resultado de cada piloto, ya ordenada por posicion.
        private final List<ResultadoCarrera> resultados;

        //Constructor
        //Inicializa y asigna los datos iniciales a un objeto cuando es creado.
        public ResultadoSimulacion(Clima climaReal, List<ResultadoCarrera> resultados) {
            this.climaReal = climaReal;
            this.resultados = resultados;
        }

        //Getter
        public Clima getClimaReal() {
            return climaReal;
        }

        //Getter
        public List<ResultadoCarrera> getResultados() {
            return resultados;
        }
    }
}
