package com.company.hilos;

import com.company.controlador.Edificio;

import java.util.Random;
import java.util.concurrent.Semaphore;

public class Persona extends Thread {

    private Semaphore llamadaAscensor, salidaAscensor;
    private Edificio edificio;
    private int origen, destino;
    private static int autoincrement;

    public Persona(Edificio edificio) {
        this.edificio = edificio;
        salidaAscensor = new Semaphore(0);
        llamadaAscensor = new Semaphore(0);
        autoincrement++;
        setName("[" + "Persona " + autoincrement + "]");

        generarPlantasOrigenDestino();
        System.out.println(getName() + " Origen:" + origen + " Destino:" + destino);

        edificio.apuntarPersona(this);
    }
    
    @Override
    public void run() {
        try {
            // Llamar al ascensor
            edificio.llamarAscensor(this);

            // Subir al ascensor
            salidaAscensor.acquire();

            // Bajar del ascensor

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public int getDestino() {
        return destino;
    }

    private void generarPlantasOrigenDestino() {
        Random random = new Random();

        origen = random.nextInt(edificio.getNumPlantas() + 1);
        do {
            destino = random.nextInt(edificio.getNumPlantas() + 1);
        } while (destino == origen);
    }

    public Semaphore getSalidaAscensor() {
        return salidaAscensor;
    }

    public Semaphore getLlamadaAscensor() {
        return llamadaAscensor;
    }

    public int getOrigen() {
        return origen;
    }

    @Override
    public String toString() {
        return getName();
    }
}
