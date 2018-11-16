package com.company;

import com.company.ui.Vista;

public class Prueba {

    public static void main (String[] args) throws InterruptedException {
        Vista vista = new Vista(10, 2);

//        Random rando = new Random();
        vista.colocarAscensor(1, 5);
        vista.colocarAscensor(1, 0);
    }
}
