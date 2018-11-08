package com.company.controlador;

import com.company.ui.Vista;
import com.company.hilos.Ascensor;
import com.company.hilos.Persona;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;


public class Edificio {

    private Semaphore permisoModelo, permisoVista;
    private Modelo modelo;
    private int numPlantas;
    private Vista vista;

    public Edificio(int numPlantas, int numAscensores, int tamAscensores) {

        this.numPlantas = numPlantas;
        modelo = new Modelo();
        crearAscensores(numAscensores, tamAscensores);
        permisoModelo = new Semaphore(1); // MUTEX
        permisoVista = new Semaphore(1);
        vista = new Vista(numAscensores);
    }

    public void llamarAscensor(Persona persona) throws InterruptedException{

        // La persona espera a que haya ascensores disponibles en su planta
        persona.getLlamadaAscensor().acquire();
        Ascensor ascensor;
        while ((ascensor = buscarAscensor(persona.getOrigen())) == null) {
            persona.getLlamadaAscensor().acquire();
        }

        // Pasa al ascensor y se visualiza
        ascensor.getPase().acquire();
        System.out.println(persona.getName() + " Subiendo a " + ascensor.getName());

        // Actualizar modelo
        permisoModelo.acquire();
        modelo.quitarPersonaEsperando(persona);
        modelo.conectarAscensorPersona(ascensor, persona);
        permisoModelo.release();

        // Actualizar ui
        permisoVista.acquire();
        ponerPersonaAscensor(ascensor, persona);
        permisoVista.release();

    }

    private void avisarSubidas(Ascensor ascensor) {

        // Si el ascensor esta libre, notifica a las personas cuya planta coincida con la suya
        if (ascensor.getPase().availablePermits() > 0) {
            for (Persona persona: modelo.getPersonasEsperando()) {
                if (persona.getOrigen() == ascensor.getPlanta()) {
                    persona.getLlamadaAscensor().release();
                }
            }
        }

    }

    private void avisarBajadas(Ascensor ascensorActual) throws InterruptedException{

        ArrayList<Persona> aux = new ArrayList<>();

        // Revisar que personas han llegado ya a su destino
        for (Persona persona: modelo.getPersonasEnAscensor(ascensorActual)) {
            if (persona.getDestino() == ascensorActual.getPlanta()) {
                aux.add(persona);
            }
        }

        // Procesar las salidas
        for (Persona persona: aux) {
            modelo.borrarPersonaDeAscensor(ascensorActual, persona);
            System.out.println(persona.getName() + " Bajando de " + ascensorActual.getName());
            persona.getSalidaAscensor().release();
            ascensorActual.getPase().release();

            permisoVista.acquire();
            bajarPersonaAscensor(ascensorActual, persona);
            permisoVista.release();
        }

    }

    public void avisarCambioDePlanta(Ascensor ascensor) {
        try {
            // Solicitamos permiso para operar con el modelo antes de hacer nada
            permisoModelo.acquire();

            avisarBajadas(ascensor);
            avisarSubidas(ascensor);

            permisoModelo.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Ascensor buscarAscensor(int planta) {
        // Recorre la lista, si hay algun ascensor con huecos en esa planta
        for (Ascensor ascensor : modelo.getAscensores()) {
            if (ascensor.getPase().availablePermits() > 0 && ascensor.getPlanta() == planta) {
                return ascensor;
            }
        }
        return null;
    }

    private void crearAscensores(int numAscensores, int tamAscensores) {
        // Rellenar el ArrayList y preparar el mapa
        for (int i = 0; i < numAscensores; i++) {
            Ascensor ascensor = new Ascensor(this, tamAscensores);
            modelo.nuevoAscensor(ascensor);
        }
    }

    public int getNumPlantas() {
        return numPlantas;
    }

    public void comienzoJornada() {
        for (Ascensor ascensor: modelo.getAscensores()) {
            ascensor.start();
        }
    }

    public void finDeJornada() {
        for (Ascensor ascensor : modelo.getAscensores()) {
            ascensor.detenerAscensor();
        }

        vista.dispose();
    }

    public void apuntarPersona(Persona persona) {
        modelo.nuevaPersonaEsperando(persona);

        try {
            permisoVista.acquire();
            vista.getModeloEsperando().addElement(persona);
            permisoVista.release();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }


    private void ponerPersonaAscensor(Ascensor ascensor, Persona persona) {
        SwingUtilities.invokeLater(() -> {
            vista.getModeloEsperando().removeElement(persona);
            vista.getModeloAscensor(Integer.parseInt(ascensor.getName())).addElement(persona);
        });
    }

    private void bajarPersonaAscensor(Ascensor ascensor, Persona persona) {
        SwingUtilities.invokeLater(() -> {
            vista.getModeloAscensor(Integer.parseInt(ascensor.getName())).removeElement(persona);
            vista.getModeloBajadas().addElement(persona);
        });
    }

    private void borrarPersonaEsperando(Persona persona) {
        vista.getModeloEsperando().removeElement(persona);
    }
}
