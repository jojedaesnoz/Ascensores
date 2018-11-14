package com.company.hilos;


import com.company.controlador.Edificio;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * Created by Javi on 27/10/2018.
 */
public class Ascensor extends Thread {
    enum ESTADOS {
        SUBIENDO, BAJANDO, QUIETO
    }
    private Edificio edificio;
    private boolean operativo, subiendo;
    private Semaphore pase;
    private int plantaActual;
    private static int autoincrement;
    private ESTADOS estado;
    private ArrayList<Integer> plantasDestino;


    private final int TIEMPO_BASE = 200;
    private final int TIEMPO_PARADA = 1000;

    public Ascensor (Edificio edificio, int tamAscensor) {
        this.edificio = edificio;
        pase = new Semaphore(tamAscensor);
        plantaActual = (int)(Math.random() * (tamAscensor + 1));
        operativo = false;

        plantasDestino = new ArrayList<>();

        setName(String.valueOf(autoincrement));
        autoincrement++;
    }

    @Override
    public void run() {
        operativo = true;
        subiendo = new Random().nextBoolean();
        try {
            do {
                // Notifica de donde esta
                edificio.avisarCambioDePlanta(this);

                // Esperar a que suba y baje la gente
                Thread.sleep(1000);

                //TODO: que solo se pare si se va a bajar o subir

                // Si ha llegado al final del recorrido, da la vuelta
                if (plantaActual >= edificio.getNumPlantas()) subiendo = false;
                else if (plantaActual <= 0) subiendo = true;

                // Se mueve
                if (subiendo) plantaActual++;
                else plantaActual--;

            } while (operativo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void detenerAscensor() {
        operativo = false;

        pase.release(pase.getQueueLength());
    }

    public boolean isSubiendo() {
        return subiendo;
    }

    public Semaphore getPase() {
        return pase;
    }

    public int getPlantaActual() {
        return plantaActual;
    }

    public ArrayList<Integer> getPlantasDestino() {
        return plantasDestino;
    }

    public ESTADOS getEstado() {
        return estado;
    }
}
