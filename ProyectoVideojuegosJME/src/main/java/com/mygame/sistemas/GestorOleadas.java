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

    private Main app; 
    private int oleadaActual = 0;
    private int villanosRestantes = 0;
    private ArrayList<Spatial> ListaVillanos = new ArrayList<>();
    private ArrayList<Vector3f> PoolDeSpawns = new ArrayList<>();

    public GestorOleadas(Main app, Spatial modeloLaberinto) {
        this.app = app;
        
        this.PoolDeSpawns.clear();
        
        // 1. Escaneamos las cruces del mapa de Blender
        escanearSpawnsEnMapa(modeloLaberinto);
        
        // 2. Cargamos también las 33 coordenadas manuales para usar ambas
        cargarSpawns();
        
        if (PoolDeSpawns.isEmpty()) {
            System.out.println("Error crítico: No hay puntos de aparición cargados.");
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
        PoolDeSpawns.add(new Vector3f(81.34f, 2.5f, 375.94f));
        PoolDeSpawns.add(new Vector3f(61.38f, 2.5f, 376.10f));
        PoolDeSpawns.add(new Vector3f(-34.14f, 2.5f, 365.12f));
        PoolDeSpawns.add(new Vector3f(9.02f, 2.5f, 327.24f));
        PoolDeSpawns.add(new Vector3f(8.96f, 2.5f, 299.63f));
        PoolDeSpawns.add(new Vector3f(11.69f, 2.5f, 256.12f));
        PoolDeSpawns.add(new Vector3f(-11.32f, 2.5f, 189.30f));
        PoolDeSpawns.add(new Vector3f(-37.26f, 2.5f, 189.49f));
        PoolDeSpawns.add(new Vector3f(-115.77f, 2.5f, 189.68f));
        PoolDeSpawns.add(new Vector3f(-183.30f, 2.5f, 189.78f));
        PoolDeSpawns.add(new Vector3f(-245.50f, 2.5f, 189.87f));
        PoolDeSpawns.add(new Vector3f(-310.40f, 2.5f, 207.74f));
        PoolDeSpawns.add(new Vector3f(-332.54f, 2.5f, 256.98f));
        PoolDeSpawns.add(new Vector3f(-430.20f, 2.5f, 138.00f));
        PoolDeSpawns.add(new Vector3f(-430.01f, 2.5f, 154.30f));
        PoolDeSpawns.add(new Vector3f(-408.95f, 2.5f, 173.48f));
        PoolDeSpawns.add(new Vector3f(-393.44f, 2.5f, 147.26f));
        PoolDeSpawns.add(new Vector3f(-364.94f, 2.5f, 151.61f));
        PoolDeSpawns.add(new Vector3f(-366.67f, 2.5f, 164.38f));
        PoolDeSpawns.add(new Vector3f(-359.94f, 2.5f, 147.52f));
        PoolDeSpawns.add(new Vector3f(-362.96f, 2.5f, 123.32f));
        PoolDeSpawns.add(new Vector3f(-371.98f, 2.5f, 125.55f));
        PoolDeSpawns.add(new Vector3f(-452.20f, 2.5f, 327.21f));
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
            
            // ==========================================================
            // MARCADOR VISUAL Y RADAR (Esfera Roja)
            // ==========================================================
            com.jme3.scene.shape.Sphere formaMarcador = new com.jme3.scene.shape.Sphere(10, 10, 0.4f);
            com.jme3.scene.Geometry marcadorVisual = new com.jme3.scene.Geometry("MarcadorEnemigo", formaMarcador);
            marcadorVisual.setLocalTranslation(0, 3.5f, 0); 
            com.jme3.material.Material matMarcador = new com.jme3.material.Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            matMarcador.setColor("Color", com.jme3.math.ColorRGBA.Red); 
            marcadorVisual.setMaterial(matMarcador);
            NodoEnemigo.attachChild(marcadorVisual);
            // ==========================================================

            NodoEnemigo.setUserData("Vida", Constantes.OLEADA_VIDA_BASE + (oleadaActual * Constantes.OLEADA_VIDA_AUMENTO)); 
            
            BetterCharacterControl fisicasE = new BetterCharacterControl(0.8f, 2.5f, 40f);
            NodoEnemigo.addControl(fisicasE);
            
            app.getRootNode().attachChild(NodoEnemigo);
            app.getEstadoFisicas().getPhysicsSpace().add(fisicasE);
            
            // ==========================================================
            // LÓGICA DE SPAWN EN PAREJAS CON OFFSET
            // ==========================================================
            // 1. Agrupamos de dos en dos hacia el mismo punto de spawn
            int indiceSpawn = i / 2; 
            
            // Protección: Si hay más enemigos que puntos de spawn, usamos el último punto repetidamente
            if (indiceSpawn >= PoolDeSpawns.size()) {
                indiceSpawn = PoolDeSpawns.size() - 1; 
            }
            
            // Clonamos el vector para no modificar permanentemente la lista original
            Vector3f PuntoBase = PoolDeSpawns.get(indiceSpawn).clone();
            
            // 2. Aplicamos el desplazamiento físico para que no exploten al nacer
            if (i % 2 == 0) {
                 // Las Arañas aparecen 1.5 metros a la derecha del punto central
                PuntoBase.addLocal(1.5f, 0, 0);
            } else {
                // Los Tanques aparecen 1.5 metros a la izquierda del punto central
                PuntoBase.addLocal(-1.5f, 0, 0);
            }
            
            // Finalmente, los mandamos a su coordenada segura y desplazada
            fisicasE.warp(PuntoBase);
            
            ListaVillanos.add(NodoEnemigo); 
            // ==========================================================
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
                
                // Si implementaste el muro de escape, cambia esto por app.abrirSalida();
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
    
    // Getters para vigilancia
    public ArrayList<Vector3f> getPoolDeSpawns() {
        return PoolDeSpawns;
    }
}