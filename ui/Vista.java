package com.company.ui;

import com.company.hilos.Persona;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Vista extends JFrame {

    private DefaultListModel<Persona> modeloEsperando, modeloBajadas;
    private JList<Persona> listaEsperando, listaBajadas;
    private ArrayList<DefaultListModel<Persona>> modelosAscensores;
    private ArrayList<JList<Persona>> listasAscensores;

    public Vista(int numAscensores) {
        prepararVentana();

        add(listaEsperando());
        add(panelAscensores(numAscensores));
        add(crearListaBajadas());

        pack();
        setVisible(true);



    }


    private void prepararVentana() {
        setLayout(new GridLayout(1, 2));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JPanel crearListaBajadas() {
        JPanel panel = new JPanel(new BorderLayout());

        modeloBajadas = new DefaultListModel<>();
        listaBajadas = new JList<>(modeloBajadas);

        panel.add(new JScrollPane(listaBajadas), BorderLayout.CENTER);
        panel.add(new JLabel("Personas que se han bajado"), BorderLayout.NORTH);
        return panel;
    }

    private JPanel panelAscensores(int numAscensores) {
        modelosAscensores = new ArrayList<>();
        listasAscensores = new ArrayList<>();

        // Crear panel
        JPanel contenedor = new JPanel();
        BoxLayout vBox = new BoxLayout(contenedor, BoxLayout.Y_AXIS);
        contenedor.setLayout(vBox);

        // Crear ascensores
        for (int i = 0; i < numAscensores; i++) {
            JPanel panel = new JPanel(new BorderLayout());

            modelosAscensores.add(new DefaultListModel<>());
            listasAscensores.add(new JList<>(modelosAscensores.get(i)));

            panel.add(new JLabel("Ascensor " + (i + 1)), BorderLayout.NORTH);
            panel.add(new JScrollPane(listasAscensores.get(i)), BorderLayout.CENTER);

            contenedor.add(panel);
        }

        return contenedor;
    }

    private JPanel listaEsperando(){
        JPanel panel = new JPanel(new BorderLayout());
        modeloEsperando = new DefaultListModel<>();
        listaEsperando = new JList<>(modeloEsperando);

        panel.add(new JScrollPane(listaEsperando), BorderLayout.CENTER);
        panel.add(new JLabel("Personas esperando"), BorderLayout.NORTH);
        return panel;
    }

    public DefaultListModel<Persona> getModeloBajadas() {
        return modeloBajadas;
    }

    public DefaultListModel<Persona> getModeloEsperando() {
        return modeloEsperando;
    }

    public JList<Persona> getListaEsperando() {
        return listaEsperando;
    }

    public JList<Persona> getListaAscensor(int id) {
        return listasAscensores.get(id);
    }

    public DefaultListModel<Persona> getModeloAscensor(int id) {
        return modelosAscensores.get(id);
    }
}
