package com.company.controlador;

import com.company.hilos.Ascensor;
import com.company.hilos.Persona;
import com.company.ui.Vista;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import static com.company.controlador.Edificio.Direccion.BAJANDO;
import static com.company.controlador.Edificio.Direccion.PARADO;
import static com.company.controlador.Edificio.Direccion.SUBIENDO;


public class Edificio {

    // ENUM
    public enum Direccion {
        SUBIENDO, BAJANDO, PARADO
    }

    // ESTRUCTURAS DE DATOS
    private ArrayList<Ascensor> ascensores;
    private HashMap<Ascensor, ArrayList<Persona>> personasAscensor;
    private HashMap<Integer, ArrayList<Persona>> personasEsperando;

    // VARIABLES Y ATRIBUTOS
    private Semaphore permisoDatos;
    private int numPlantas;
    private Vista vista;

    public Edificio(int numPlantas, int numAscensores, int tamAscensores) {

        // INICIALIZACION
        this.numPlantas = numPlantas;
        ascensores = new ArrayList<>();
        personasAscensor = new HashMap<>();
        personasEsperando = new HashMap<>();
        for (int i = 0; i <= numPlantas; i++) {
            personasEsperando.put(i, new ArrayList<>());
        }
        permisoDatos = new Semaphore(1); // MUTEX para escribir sobre las listas
        vista = new Vista(numPlantas, numAscensores);

        // CREAR ASCENSORES
        for (int i = 0; i < numAscensores; i++) {
            Ascensor ascensor = new Ascensor(this, tamAscensores, numPlantas);
            ascensores.add(ascensor);
            personasAscensor.put(ascensor, new ArrayList<>());
        }
    }

    /*
     *****************************************
     *                                       *
     *   METODOS DE LOS HILOS PERSONA:       *
     *       llamarAscensor                  *
     *                                       *
     *****************************************
     */
    public void llamarAscensor(Persona persona) throws InterruptedException{

        // La persona espera hasta que un ascensor le avise (avisarSubidas)
        nuevaPersonaEsperando(persona);
        persona.getLlamadaAscensor().acquire();

        // Toma el pase de ascensor
        Ascensor ascensor = persona.getAscensor();
        ascensor.getPase().acquire();

        // Inserta su planta en las de destino y las ordena
        ascensor.getPlantasDestino().add(persona.getDestino());
        ascensor.getPlantasDestino().sort(Comparator.comparingInt(ascensor.getPlantasDestino()::get));

        // Actualizar los datos y la vista
        subirAlAscensorDatos(persona, ascensor);
        subirAlAscensorVista(persona, ascensor);
    }

    private void subirAlAscensorDatos(Persona persona, Ascensor ascensor) throws InterruptedException {
        permisoDatos.acquire();
        personasEsperando.get(persona.getOrigen()).remove(persona);
        personasAscensor.get(ascensor).add(persona);
        permisoDatos.release();
    }


    /*
     *****************************************
     *                                       *
     *   METODOS DE LOS HILOS ASCENSOR:      *
     *       avisarCambioDePlanta            *
     *       avisarSubidas                   *
     *       avisarBajadas                   *
     *                                       *
     *****************************************
     */
    public void avisarCambioDePlanta(Ascensor ascensor) {
        try {
            permisoDatos.acquire();

            cambiarPlantaAscensor(ascensor);
            avisarBajadas(ascensor);
            avisarSubidas(ascensor);

            permisoDatos.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void avisarSubidas(Ascensor ascensor) {
//        int numPermisos = ascensor.getPase().availablePermits();
//        Iterator<Persona> iterador = personasEsperando.get(ascensor.getPlanta()).iterator();

        // Mientras tenga permisos libres y quede gente, deja pasar
//        while (numPermisos > 0 && iterador.hasNext()) {
//            Persona persona = iterador.next();
//            persona.setAscensor(ascensor);
//            persona.getLlamadaAscensor().release();
//            numPermisos--;
//        }

        Iterator<Persona> iterador = personasAComprobar(ascensor).iterator();
        int numPermisos = ascensor.getPase().availablePermits();
        Direccion direccionPersona;

        while (numPermisos > 0 && iterador.hasNext()) {
            Persona persona = iterador.next();
            if (persona.getDestino() > persona.getOrigen())
                direccionPersona = SUBIENDO;
            else
                direccionPersona = BAJANDO;

            // Si ascensor y persona van en la misma direccion
            if (direccionPersona.equals(ascensor.getDireccion())) {
                persona.setAscensor(ascensor);
                persona.getLlamadaAscensor().release();
                numPermisos--;
            }
        }
    }

    private ArrayList<Persona> personasAComprobar(Ascensor ascensor) {
        ArrayList<Persona> personas = new ArrayList<>();

        // Si esta parado, devuelve todas las plantas para comprobar
        if (ascensor.getDireccion() == PARADO) {
            for (ArrayList<Persona> listaPersonas: personasEsperando.values()) {
                personas.addAll(listaPersonas);
            }
            return personas;
        }

        // Si no esta parado, devuelve las plantas entre la actual y la ultima parada
        int primeraPlanta = ascensor.getPlanta();
        int ultimaPlanta = ascensor.getPlantasDestino().get(ascensor.getPlantasDestino().size() - 1);

        if (ascensor.getDireccion() == SUBIENDO) {
            for (int i = primeraPlanta; i <= ultimaPlanta; i++) {
                personas.addAll(personasEsperando.get(i));
            }
        }
        else if (ascensor.getDireccion() == BAJANDO) {
            for (int i = primeraPlanta; i >= ultimaPlanta ; i--) {
                personas.addAll(personasEsperando.get(i));
            }
        }

        return personas;
    }

    private void avisarBajadas(Ascensor ascensorActual) {
        ArrayList<Persona> aux = new ArrayList<>();

        // Revisar que personas han llegado ya a su destino
        for (Persona persona: personasAscensor.get(ascensorActual)) {
            if (persona.getDestino() == ascensorActual.getPlanta()) {
                aux.add(persona);
            }
        }

        // Procesar las salidas
        for (Persona persona: aux) {
            personasAscensor.get(ascensorActual).remove(persona);
            persona.getSalidaAscensor().release();
            ascensorActual.getPase().release();

            bajarDelAscensorVista(ascensorActual, persona);
        }
    }

    /*
     *****************************************
     *                                       *
     *   METODOS DE LA VISTA:                *
     *       nuevaPersonaEsperando           *
     *       cambiarPlantaAscensor           *
     *       subirAlAscensorVista            *
     *       bajarDelAscensorVista           *
     *                                       *
     *****************************************
     */
    private void nuevaPersonaEsperando(Persona persona) throws InterruptedException {
        permisoDatos.acquire();

        // Pone a la persona en la lista de esperando tanto en datos como vista
        personasEsperando.get(persona.getOrigen()).add(persona);
        SwingUtilities.invokeLater(() -> {
            vista.modelosEsperando.get(persona.getOrigen()).addElement(persona);
        });

        permisoDatos.release();
    }

    private void cambiarPlantaAscensor(Ascensor ascensor) {
        SwingUtilities.invokeLater(() ->
        {
            vista.colocarAscensor(ascensor.getIdAscensor(), ascensor.getPlanta());
        });
    }

    private void subirAlAscensorVista(Persona persona, Ascensor ascensor) {
        SwingUtilities.invokeLater(() -> {
            // Borrar de la lista de gente esperando y poner en el ascensor
            vista.modelosEsperando.get(ascensor.getPlanta()).removeElement(persona);
            vista.modelosAscensores.get(ascensor.getIdAscensor()).addElement(persona);
        });
    }

    private void bajarDelAscensorVista(Ascensor ascensor, Persona persona) {
        SwingUtilities.invokeLater(() -> {
            // Borrar del ascensor y poner en la lista de bajadas
            vista.modelosAscensores.get(ascensor.getIdAscensor()).removeElement(persona);
            vista.modelosBajadas.get(ascensor.getPlanta()).addElement(persona);
        });
    }

    /*
     *****************************************
     *                                       *
     *   METODOS DEL PROCESO:                *
     *       comienzoJornada                 *
     *       finDeJornada                    *
     *****************************************
     */
    public void comienzoJornada() {
        for (Ascensor ascensor : ascensores) {
            ascensor.start();
        }
    }

    public void finDeJornada() {
        for (Ascensor ascensor : ascensores) {
            ascensor.detenerAscensor();
        }
        vista.dispose();
    }

    public int getNumPlantas() {
        return numPlantas;
    }
}
