package com.mygame.sistemas;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.mygame.Constantes;
import com.mygame.Main;
import java.util.ArrayList;
import java.util.Collections;

public class GestorOleadas {

    private Main app; // Referencia al juego principal
    private int oleadaActual = 0;
    private int villanosRestantes = 0;
    private ArrayList<Spatial> ListaVillanos = new ArrayList<>();
    private ArrayList<Vector3f> PoolDeSpawns = new ArrayList<>();

    public GestorOleadas(Main app, Spatial modeloLaberinto) {
        this.app = app;
        
        this.PoolDeSpawns.clear();
        escanearSpawnsEnMapa(modeloLaberinto);
        
        if (PoolDeSpawns.isEmpty()) {
            System.out.println("No se encontraron nodos 'Spawn_' en el mapa");
            cargarSpawns();
        }
    }

    private void cargarSpawns() {
        PoolDeSpawns.add(new Vector3f(14.43f, 2.5f, 378.24f));
        PoolDeSpawns.add(new Vector3f(12.29f, 2.5f, 285.62f));
        PoolDeSpawns.add(new Vector3f(11.50f, 2.5f, 270.16f));
        PoolDeSpawns.add(new Vector3f(-40.95f, 2.5f, 191.44f));
        PoolDeSpawns.add(new Vector3f(-62.91f, 2.5f, 191.77f));
        PoolDeSpawns.add(new Vector3f(-258.61f, 2.5f, 192.83f));
        PoolDeSpawns.add(new Vector3f(-310.77f, 2.5f, 202.06f));
        PoolDeSpawns.add(new Vector3f(-327.76f, 2.5f, 260.86f));
        PoolDeSpawns.add(new Vector3f(-338.89f, 2.5f, 185.40f));
        PoolDeSpawns.add(new Vector3f(-428.48f, 2.5f, 88.78f));
    }

    public void iniciarNuevaOleada() {
        oleadaActual++;
        
        int enemigosAInstanciar = Constantes.OLEADA_ENEMIGOS_BASE + (oleadaActual * Constantes.OLEADA_ENEMIGOS_MULTIPLICADOR); 
        if (enemigosAInstanciar > PoolDeSpawns.size()) enemigosAInstanciar = PoolDeSpawns.size(); 

        Spatial modeloAraniaBase = app.getAssetManager().loadModel("Models/arania.j3o");
        Spatial modeloTanqueBase = app.getAssetManager().loadModel("Models/En_Tanque.j3o");
        
        Collections.shuffle(PoolDeSpawns); 

        for (int i = 0; i < enemigosAInstanciar; i++) {
            Node NodoEnemigo = new Node(); 
            Spatial visualEnemigo;
            if (i % 2 == 0) {
                visualEnemigo = modeloAraniaBase.clone();
                NodoEnemigo.setName("Arania");
                visualEnemigo.setLocalTranslation(0, 0f, 0); 
            } else {
                visualEnemigo = modeloTanqueBase.clone();
                NodoEnemigo.setName("Tanque");
                visualEnemigo.setLocalTranslation(0, 1.2f, 0); 
            }

            NodoEnemigo.attachChild(visualEnemigo);
            NodoEnemigo.setUserData("Vida", Constantes.OLEADA_VIDA_BASE + (oleadaActual * Constantes.OLEADA_VIDA_AUMENTO)); 
            
            BetterCharacterControl fisicasE = new BetterCharacterControl(0.8f, 2.5f, 40f);
            NodoEnemigo.addControl(fisicasE);
            
            app.getRootNode().attachChild(NodoEnemigo);
            app.getEstadoFisicas().getPhysicsSpace().add(fisicasE);
            fisicasE.warp(PoolDeSpawns.get(i));
            
            ListaVillanos.add(NodoEnemigo); 
        }
        
        villanosRestantes = ListaVillanos.size();
        
        // Avisamos a la UI del Main que se actualice
        app.actualizarTextoContador(villanosRestantes);
        app.actualizarTextoOleada(oleadaActual);
    }

    public void reducirContadorVillanos() {
        if (villanosRestantes > 0) {
            villanosRestantes--;
            app.actualizarTextoContador(villanosRestantes);
        }
        
        if (villanosRestantes <= 0) {
            if (oleadaActual >= Constantes.OLEADAS_PARA_GANAR) { 
                app.mostrarPantallaVictoria();
            } else {
                iniciarNuevaOleada(); 
            }
        }
    }
    
    private void escanearSpawnsEnMapa(Spatial nodoRaiz) {
        if (nodoRaiz.getName() != null && nodoRaiz.getName().startsWith("Spawn_")) {
            PoolDeSpawns.add(nodoRaiz.getWorldTranslation().clone());
            System.out.println("Punto de Spawn detectado en: " + nodoRaiz.getWorldTranslation());
        }
        
        if (nodoRaiz instanceof Node) {
            Node contenedor = (Node) nodoRaiz;
            for (Spatial hijo : contenedor.getChildren()) {
                escanearSpawnsEnMapa(hijo);
            }
        }
    }

    // Getters para que otras clases puedan consultar el estado
    public ArrayList<Spatial> getListaVillanos() {
        return ListaVillanos;
    }
}