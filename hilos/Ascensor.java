package com.company.hilos;


import com.company.controlador.Edificio;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;

import static com.company.controlador.Edificio.Direccion.*;

/**
 * Created by Javi on 27/10/2018.
 */
public class Ascensor extends Thread {
    private Edificio edificio;
    private boolean operativo;
    private Semaphore pase;
    private int numPlantas, planta;
    private Edificio.Direccion direccion;
    private static int autoincrement;

    private ArrayList<Integer> plantasDestino;

    public Ascensor (Edificio edificio, int tamAscensor, int numPlantas) {
        this.edificio = edificio;
        pase = new Semaphore(tamAscensor);
        this.numPlantas = numPlantas;
        planta = (int)(Math.random() * (tamAscensor + 1));
        operativo = false;

        plantasDestino = new ArrayList<>();

        setName(String.valueOf(autoincrement));
        autoincrement++;
    }

    /*
    Se mueve de planta en planta con un pequeño wait hasta que llega a la primera planta de destino
    alli, notifica bajadas y subidas, borra la primera planta de destino y empieza a ir a por la siguiente
    hasta que no le queden y se pare.

    notifica subidas:
        si el ascensor esta parado  OOOO
        si la persona va en la misma direccion que el ascensor YYYY
        la persona esta entre la planta actual y la de destino
     */

    public ArrayList<Integer> getPlantasDestino() {
        return plantasDestino;
    }

    @Override
    public void run() {
        operativo = true;
//        direccion = new Random().nextBoolean()? SUBIENDO : BAJANDO;
        direccion = PARADO;
        try {
            do {
                /*
                TODO: no deberia coger automaticamente a la gente, si no añadirla a su lista de paradas y luego cogerla
                 */
                // Notifica de donde esta
                edificio.avisarCambioDePlanta(this);

                if (plantasDestino.isEmpty()) {
                    direccion = PARADO;
                    esperar();
                }
                else {
                    // Comprobacion de la direccion asegurandonos de que no pueda salirse del limite
                    if (plantasDestino.get(0) > planta) {
                        direccion = SUBIENDO;
                    }
                    else if (plantasDestino.get(0) < planta) {
                        direccion = BAJANDO;
                    }

                    // Avanza hasta que llega a la planta de destino y la borra
                    while (planta != plantasDestino.get(0)) {
                        avanzarUnaPlanta();
                    }
                    plantasDestino.remove(0);
                }

//                for (int i = primeraPlanta; i >= ultimaPlanta ; i--) {
//                    personas.addAll(personasEsperando.get(i));
//                }
            } while (operativo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void avanzarUnaPlanta() throws InterruptedException {

        // Si ha llegado al final del recorrido, da la vuelta
        if (planta == numPlantas) direccion = BAJANDO;
        else if (planta == 0) direccion = SUBIENDO;


        // Esperar a que suba y baje la gente
        Thread.sleep(500);

        // Se mueve
        if (direccion.equals(SUBIENDO))
            planta++;
        else planta--;
    }

    private void esperar() throws InterruptedException {
        Thread.sleep(3000);
    }
    public void detenerAscensor() {
        operativo = false;

        pase.release(pase.getQueueLength());
    }

    public Edificio.Direccion getDireccion() {
        return direccion;
    }

    public Semaphore getPase() {
        return pase;
    }

    public int getPlanta() {
        return planta;
    }


    public int getIdAscensor() {
        return Integer.parseInt(getName());
    }
}
