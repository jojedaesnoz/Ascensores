package com.company;

import com.company.controlador.Edificio;
import com.company.hilos.Persona;

public class Main {

    public static void main(String[] args) {
        // Crear el edificio
        Edificio edificio = new Edificio(10, 3, 4);

        // Crear a las personas
        Persona[] personas = new Persona[30];
        for (int i = 0; i < personas.length; i++) {
            personas[i] = new Persona(edificio);
        }

        // Arrancar
        for (Persona persona : personas) {
            persona.start();
        }
        edificio.comienzoJornada();

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
