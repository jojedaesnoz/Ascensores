package com.company.controlador;

import com.company.hilos.Ascensor;
import com.company.hilos.Persona;
import com.company.ui.Vista;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.Semaphore;


public class Edificio {

    // ESTRUCTURAS DE DATOS
    private ArrayList<Ascensor> ascensores;
    private HashMap<Ascensor, ArrayList<Persona>> personasAscensor;
    private HashMap<Integer, ArrayList<Persona>> personasEsperando;

    // VARIABLES Y ATRIBUTOS
    private Semaphore permisoDatos;
    private Vista vista;

    public Edificio(int numPlantas, int numAscensores, int tamAscensores) {

        // INICIALIZACION
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
     *       buscarAscensor                  *
     *                                       *
     *****************************************
     */
    public void llamarAscensor(Persona persona) throws InterruptedException{

        // La persona espera y consigue un ascensor libre en su planta
        ponerPersonaEsperando(persona);
        Ascensor ascensor = buscarAscensor(persona);
        ascensor.getPase().acquire();

        // Actualizar datos
        permisoDatos.acquire();
        personasEsperando.get(persona.getOrigen()).remove(persona);
        personasAscensor.get(ascensor).add(persona);
        permisoDatos.release();

        // Actualizar ui
        subirAlAscensor(ascensor, persona);
        refrescarGenteEsperando(ascensor.getPlanta());
    }

    private Ascensor buscarAscensor(Persona persona) throws InterruptedException {
        ArrayList<Ascensor> ascensoresLibres = new ArrayList<>();
        Ascensor ascensorLibre = null;

        do {
            // Hace que la persona espere hasta que se libere algun ascensor
            persona.getLlamadaAscensor().acquire();

            // Busca posibles candidatos
            for (Ascensor ascensor: ascensores) {
                if (ascensor.getPase().availablePermits() > 0 && ascensor.getPlanta() == persona.getOrigen()) {
                    ascensoresLibres.add(ascensor);
                }
            }

            // Si ha encontrado candidatos, devuelve el que tenga mas permisos libres
            if (!ascensoresLibres.isEmpty()) {
                ascensoresLibres.sort(Comparator.comparingInt(
                        (Ascensor a) -> a.getPase().availablePermits()).reversed());
                ascensorLibre = ascensoresLibres.get(0);
            }
        } while (ascensorLibre == null);

        return ascensorLibre;
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

            cambiarDePlanta(ascensor);
            avisarBajadas(ascensor);
            avisarSubidas(ascensor);
            refrescarGenteEsperando(ascensor.getPlanta());

            permisoDatos.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void avisarSubidas(Ascensor ascensor) {
        // Si el ascensor esta libre, notifica a las personas cuya planta coincida con la suya
        if (ascensor.getPase().availablePermits() > 0) {
            for (Persona persona: personasEsperando.get(ascensor.getPlanta())) {
                persona.getLlamadaAscensor().release();
            }
        }
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

            bajarDelAscensor(ascensorActual, persona);
        }
    }

    /*
     *****************************************
     *                                       *
     *   METODOS DE LA VISTA:                *
     *       ponerPersonaEsperando           *
     *       cambiarDePlanta                 *
     *       subirAlAscensor                 *
     *       bajarDelAscensor                *
     *       refrescarGenteEsperando         *
     *                                       *
     *****************************************
     */
    private void ponerPersonaEsperando(Persona persona) throws InterruptedException {
        permisoDatos.acquire();

        personasEsperando.get(persona.getOrigen()).add(persona);
        SwingUtilities.invokeLater(() -> {
            vista.modelosEsperando.get(persona.getOrigen()).addElement(persona);
        });

        permisoDatos.release();
    }

    private void cambiarDePlanta(Ascensor ascensor) {
        SwingUtilities.invokeLater(() ->
        {
            vista.colocarAscensor(ascensor.getIdAscensor(), ascensor.getPlanta());
        });
    }


    private void subirAlAscensor(Ascensor ascensor, Persona persona) {
        SwingUtilities.invokeLater(() -> {
            // Borrar de la lista de gente esperando y poner en el ascensor
            vista.modelosAscensores.get(ascensor.getIdAscensor()).addElement(persona);
        });
    }

    private void bajarDelAscensor(Ascensor ascensor, Persona persona) {
        SwingUtilities.invokeLater(() -> {
            vista.modelosAscensores.get(ascensor.getIdAscensor()).removeElement(persona);
            vista.modelosBajadas.get(ascensor.getPlanta()).addElement(persona);
            vista.revalidate();
            vista.repaint();
        });
    }

    private void refrescarGenteEsperando(int planta) {
        SwingUtilities.invokeLater(() -> {
            vista.modelosEsperando.get(planta).removeAllElements();
            for (Persona persona: personasEsperando.get(planta)) {
                vista.modelosEsperando.get(planta).addElement(persona);
            }
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
}
