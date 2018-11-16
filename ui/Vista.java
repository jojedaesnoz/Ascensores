package com.company.ui;

import com.company.hilos.Persona;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Vista extends JFrame {

    private int numPlantas;
    private int numAscensores;
    private GridBagConstraints gbc;

    public ArrayList<DefaultListModel<Persona>> modelosEsperando, modelosBajadas, modelosAscensores;
    public ArrayList<JList<Persona>> listasEsperando, listasBajadas, listasAscensores;
    public ArrayList<JScrollPane> panelesAscensores;

    public Vista(int numPlantas, int numAscensores) {
        this.numPlantas = numPlantas;
        this.numAscensores = numAscensores;

        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();

        crearComponentes();
        colocarPlantas();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize((numAscensores + 2) * 200, numPlantas * 100);
        setVisible(true);
    }

    private void crearComponentes() {
        listasEsperando = new ArrayList<>();
        listasBajadas = new ArrayList<>();
        listasAscensores = new ArrayList<>();
        modelosEsperando = new ArrayList<>();
        modelosBajadas = new ArrayList<>();
        modelosAscensores = new ArrayList<>();
        panelesAscensores = new ArrayList<>();
    }

    private void colocarPlantas() {
        Dimension tamPlanta = new Dimension();

        // ETIQUETAS
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        for (int i = 0; i <= numPlantas; i++) {
            JLabel labelPlanta = new JLabel("Planta " + i, SwingConstants.CENTER);
            add(labelPlanta, gbc);
            gbc.gridy++;
        }

        // GENTE ESPERANDO
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        for (int i = 0; i <= numPlantas; i++) {
            DefaultListModel<Persona> modelo = new DefaultListModel<>();
            JList<Persona> lista = new JList<>(modelo);
            JScrollPane panel = new JScrollPane(lista);

            modelosEsperando.add(modelo);
            listasEsperando.add(lista);
            add(panel, gbc);
            gbc.gridy++;
        }

        // GENTE QUE SE BAJA
        gbc.gridx = numAscensores + 2;
        gbc.gridy = 0;
        for (int i = 0; i <= numPlantas; i++) {
            DefaultListModel<Persona> modelo = new DefaultListModel<>();
            JList<Persona> lista = new JList<>(modelo);
            JScrollPane panel = new JScrollPane(lista);

            modelosBajadas.add(modelo);
            listasBajadas.add(lista);
            add(panel, gbc);
            gbc.gridy++;
        }
        gbc.weighty = 0;

        // ASCENSORES
        gbc.gridx = 2;
        gbc.gridy = 0;
        for (int i = 0; i < numAscensores; i++) {
            DefaultListModel<Persona> modelo = new DefaultListModel<>();
            JList<Persona> lista = new JList<>(modelo);
            JScrollPane panel = new JScrollPane(lista);

            modelosAscensores.add(modelo);
            listasAscensores.add(lista);
            panelesAscensores.add(panel);

            add(panel, gbc);
            gbc.gridx++;
        }
    }

    public void colocarAscensor(int id, int planta) {
        gbc.gridx = 2 + id;
        gbc.gridy = planta;

        add(panelesAscensores.get(id), gbc);

        revalidate();
        repaint();
    }
}
