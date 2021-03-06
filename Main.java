package com.company;

import com.company.controlador.Edificio;
import com.company.hilos.Persona;

public class Main {

    public static void main(String[] args) {
        // Crear el edificio
        Edificio edificio = new Edificio(10, 2, 4);

        // Crear a las personas
        Persona[] personas = new Persona[30];
        for (int i = 0; i < personas.length; i++) {
            personas[i] = new Persona(edificio);
        }

        // Arrancar
        edificio.comienzoJornada();
        for (Persona persona : personas) {
            persona.start();
        }

        // Esperar a que acabe
        try {
            for (Persona persona : personas) {
                persona.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        edificio.finDeJornada();
    }
}
