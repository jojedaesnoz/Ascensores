package com.company.controlador;

import com.company.ui.Vista;
import com.company.hilos.Ascensor;
import com.company.hilos.Persona;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Semaphore;


public class Edificio {

    private Semaphore permisoModelo;
    private Modelo modelo;
    private int numPlantas;
    private Vista vista;

    public Edificio(int numPlantas, int numAscensores, int tamAscensores) {

        this.numPlantas = numPlantas;
        modelo = new Modelo();
        crearAscensores(numAscensores, tamAscensores);
        permisoModelo = new Semaphore(1); // MUTEX
        vista = new Vista(numAscensores);
    }

    public void llamarAscensor(Persona persona) throws InterruptedException{

        // La persona espera a que haya ascensores disponibles en su planta
        ponerEnEspera(persona);
        Ascensor ascensor = buscarAscensor(persona);
        ascensor.getPase().acquire();

        // Actualizar modelo
        permisoModelo.acquire();
        modelo.quitarPersonaEsperando(persona);
        modelo.conectarAscensorPersona(ascensor, persona);
        permisoModelo.release();

        // Actualizar ui
        ponerPersonaAscensor(ascensor, persona);
    }

    public void avisarCambioDePlanta(Ascensor ascensor) {
        try {
            // Mostrar visualmente el cambio de planta
            cambiarDePlanta(ascensor);

            // Operar con el modelo de forma segura
            permisoModelo.acquire();

            avisarBajadas(ascensor);
            avisarSubidas(ascensor);

            permisoModelo.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void avisarSubidas(Ascensor ascensor) {
        // Si el ascensor esta libre, notifica a las personas cuya planta coincida con la suya
        if (ascensor.getPase().availablePermits() > 0) {
            for (Persona persona: modelo.getPersonasEsperando()) {
                if (persona.getOrigen() == ascensor.getPlantaActual()) {
                    persona.getLlamadaAscensor().release();
                }
            }
        }
    }

    private void avisarBajadas(Ascensor ascensorActual) {
        ArrayList<Persona> aux = new ArrayList<>();

        // Revisar que personas han llegado ya a su destino
        for (Persona persona: modelo.getPersonasEnAscensor(ascensorActual)) {
            if (persona.getDestino() == ascensorActual.getPlantaActual()) {
                aux.add(persona);
            }
        }

        // Procesar las salidas
        for (Persona persona: aux) {
            modelo.borrarPersonaDeAscensor(ascensorActual, persona);
            persona.getSalidaAscensor().release();
            ascensorActual.getPase().release();

            bajarPersonaAscensor(ascensorActual, persona);
        }
    }

    private Ascensor buscarAscensor(Persona persona) throws InterruptedException {
        Ascensor ascensor;
        do {
            persona.getLlamadaAscensor().acquire();
        } while (null == (ascensor = ascensorEnPlanta(persona.getOrigen())));
//
//        // Recorre la lista, si hay algun ascensor con huecos en esa planta
//        ArrayList<Ascensor> ascensoresLibres = new ArrayList<>();
//        for (Ascensor ascensor : modelo.getAscensores()) {
//            if (ascensor.getPase().availablePermits() > 0 && ascensor.getPlantaActual() == planta) {
//                ascensoresLibres.add(ascensor);
//            }
//        }

//        // Devuelve null si no ha encontrado ninguno o el que tenga mas plazas disponibles
//        if (ascensoresLibres.isEmpty()) {
//            return null;
//        } else {
//            ascensoresLibres.sort(Comparator.comparingInt((Ascensor a) -> a.getPase().availablePermits()));
//            return ascensoresLibres.get(0);
//        }


        return ascensor;
    }

    private Ascensor ascensorEnPlanta(int planta) {
        // Recorre la lista, si hay algun ascensor con huecos en esa planta
        ArrayList<Ascensor> ascensoresLibres = new ArrayList<>();
        for (Ascensor ascensor : modelo.getAscensores()) {
            if (ascensor.getPase().availablePermits() > 0 && ascensor.getPlantaActual() == planta) {
                ascensoresLibres.add(ascensor);
            }
        }

        // Devuelve null si no ha encontrado ninguno o el que tenga mas plazas disponibles
        if (ascensoresLibres.isEmpty()) {
            return null;
        } else {
            ascensoresLibres.sort(Comparator.comparingInt((Ascensor a) -> a.getPase().availablePermits()));
            return ascensoresLibres.get(0);
        }
    }

    private void ponerEnEspera(Persona persona) throws InterruptedException {
        permisoModelo.acquire();
        modelo.nuevaPersonaEsperando(persona);
        refrescarPersonasEsperando();
        permisoModelo.release();


    }


    private void cambiarDePlanta(Ascensor ascensor) {
        int idAscensor = Integer.parseInt(ascensor.getName());
        int numPlanta = ascensor.getPlantaActual();
        String direccion = ascensor.isSubiendo() ? "Subiendo" : "Bajando";
//        String texto = String.format("Ascensor %d    Planta  %d  %s", idAscensor, numPlanta, direccion);
        String texto = String.format("%d    %s", numPlanta, direccion);

        SwingUtilities.invokeLater(() -> vista.getPlantasAscensores().get(idAscensor).setText(texto));
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

    private void refrescarPersonasEsperando() {
        SwingUtilities.invokeLater(() -> {
            vista.getModeloEsperando().clear();
            modelo.getPersonasEsperando().forEach(vista.getModeloEsperando()::addElement);
        });
    }
    public void comienzoJornada() {
        modelo.getAscensores().forEach(Ascensor::start);
    }

    public void finDeJornada() {
        modelo.getAscensores().forEach(Ascensor::detenerAscensor);
        vista.dispose();
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
}
