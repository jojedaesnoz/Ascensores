package com.company.hilos;


import com.company.controlador.Edificio;

import java.util.concurrent.Semaphore;

/**
 * Created by Javi on 27/10/2018.
 */
public class Ascensor extends Thread {
    private Edificio edificio;
    private Semaphore pase;
    private boolean operativo;
    private static int autoincrement;
    private int planta;

    private final int TIEMPO_BASE = 200;
    private final int TIEMPO_PARADA = 1000;

    public Ascensor (Edificio edificio, int tamAscensor) {
        this.edificio = edificio;
        pase = new Semaphore(tamAscensor);
        planta = (int)(Math.random() * (tamAscensor + 1));
        operativo = false;

        setName(String.valueOf(autoincrement));
        autoincrement++;
    }

    @Override
    public void run() {
        boolean subiendo = true;
        operativo = true;

        try {
            do {
                // Notifica de donde esta
                System.out.println(getName() + " Planta " + planta);
                edificio.avisarCambioDePlanta(this);

                // Esperar a que suba y baje la gente
                Thread.sleep(1000);


                //TODO: que solo se pare si se va a bajar o subir

                // Si ha llegado al final del recorrido, da la vuelta
                if (planta >= edificio.getNumPlantas()) subiendo = false;
                else if (planta <= 0) subiendo = true;

                // Se mueve
                if (subiendo) planta++;
                else planta--;
            } while (operativo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void detenerAscensor() {
        operativo = false;

        pase.release(pase.getQueueLength());
    }

    public Semaphore getPase() {
        return pase;
    }

    public int getPlanta() {
        return planta;
    }
}