package com.company.controlador;

import com.company.hilos.Ascensor;
import com.company.hilos.Persona;

import java.util.*;
import java.util.concurrent.Semaphore;


public class Modelo {

    private ArrayList<Ascensor> ascensores;
    private HashMap<Ascensor, ArrayList<Persona>> personasAscensor;
    private ArrayList<Persona> personasEsperando;

    {
        ascensores = new ArrayList<>();
        personasAscensor = new HashMap<>();
        personasEsperando = new ArrayList<>();
    }

    public void nuevoAscensor(Ascensor ascensor){
        ascensores.add(ascensor);
        personasAscensor.put(ascensor, new ArrayList<>());
    }

    public void conectarAscensorPersona(Ascensor ascensor, Persona persona) {
        // Asocia la persona al ascensor en el mapa
        personasAscensor.get(ascensor).add(persona);
    }

    public ArrayList<Ascensor> getAscensores() {
        return ascensores;
    }


    public ArrayList<Persona> getPersonasEnAscensor(Ascensor ascensorActual) {
        return personasAscensor.get(ascensorActual);
    }

    public void borrarPersonaDeAscensor(Ascensor ascensor, Persona persona) {
        personasAscensor.get(ascensor).remove(persona);
    }

    public void nuevaPersonaEsperando(Persona persona) {
        if (!personasEsperando.contains(persona)) {
            personasEsperando.add(persona);
            personasEsperando.sort(Comparator.comparingInt(Persona::getOrigen));
        }
    }

    public ArrayList<Persona> getPersonasEsperando() {
        return personasEsperando;
    }

    public void quitarPersonaEsperando(Persona persona) {
        personasEsperando.remove(persona);
    }


}
