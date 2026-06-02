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
        PoolDeSpawns.add(new Vector3f(56.51f, 2.5f, 379.04f));
        PoolDeSpawns.add(new Vector3f(29.43f, 2.5f, 379.08f));
        PoolDeSpawns.add(new Vector3f(-32.06f, 2.5f, 363.50f));
        PoolDeSpawns.add(new Vector3f(-13.41f, 2.5f, 346.52f));
        PoolDeSpawns.add(new Vector3f(11.71f, 2.5f, 310.04f));
        PoolDeSpawns.add(new Vector3f(18.16f, 2.5f, 250.95f));
        PoolDeSpawns.add(new Vector3f(-337.60f, 2.5f, 183.64f));
        PoolDeSpawns.add(new Vector3f(-337.05f, 2.5f, 64.89f));
        PoolDeSpawns.add(new Vector3f(-426.03f, 2.5f, 88.13f));
        PoolDeSpawns.add(new Vector3f(-449.81f, 2.5f, 203.55f));
        PoolDeSpawns.add(new Vector3f(-449.34f, 2.5f, 14.93f));
        PoolDeSpawns.add(new Vector3f(-450.90f, 2.5f, -156.66f));
        PoolDeSpawns.add(new Vector3f(-422.98f, 2.5f, -227.84f));
        PoolDeSpawns.add(new Vector3f(-366.32f, 2.5f, -228.53f));
        PoolDeSpawns.add(new Vector3f(-260.63f, 2.5f, -226.27f));
        PoolDeSpawns.add(new Vector3f(-218.63f, 2.5f, -184.14f));
        PoolDeSpawns.add(new Vector3f(-295.65f, 2.5f, -133.27f));
        PoolDeSpawns.add(new Vector3f(-382.10f, 2.5f, -94.77f));
        PoolDeSpawns.add(new Vector3f(-341.02f, 2.5f, -17.91f));
        PoolDeSpawns.add(new Vector3f(-269.45f, 2.5f, -16.18f));
        PoolDeSpawns.add(new Vector3f(-242.96f, 2.5f, 99.66f));
        PoolDeSpawns.add(new Vector3f(-196.59f, 2.5f, 119.16f));
        PoolDeSpawns.add(new Vector3f(-58.53f, 2.5f, 51.11f));
        PoolDeSpawns.add(new Vector3f(31.41f, 2.5f, -47.48f));
        PoolDeSpawns.add(new Vector3f(-102.97f, 2.5f, -54.81f));
        PoolDeSpawns.add(new Vector3f(-133.32f, 2.5f, 28.01f));
        PoolDeSpawns.add(new Vector3f(-196.01f, 2.5f, -12.00f));
        PoolDeSpawns.add(new Vector3f(-174.40f, 2.5f, -208.74f));
        PoolDeSpawns.add(new Vector3f(-197.16f, 2.5f, -314.72f));
        PoolDeSpawns.add(new Vector3f(-271.07f, 2.5f, -249.74f));
        PoolDeSpawns.add(new Vector3f(-379.51f, 2.5f, -271.36f));
        PoolDeSpawns.add(new Vector3f(-450.22f, 2.5f, -326.63f));
        PoolDeSpawns.add(new Vector3f(-398.20f, 2.5f, -458.22f));
        PoolDeSpawns.add(new Vector3f(-360.49f, 2.5f, -399.20f));
        PoolDeSpawns.add(new Vector3f(-328.84f, 2.5f, -365.63f));
        PoolDeSpawns.add(new Vector3f(-333.87f, 2.5f, -416.78f));
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
                
                // --- LA SOLUCIÓN DEL MURO ---
                // 1. En lugar de ganar automáticamente, abrimos la salida del laberinto.
                app.abrirSalida(); 
                
                // 2. Iniciamos una oleada extra "infinita". 
                // Esto obliga al jugador a correr por su vida hacia la salida 
                // porque los enemigos seguirán apareciendo.
                iniciarNuevaOleada(); 
                
            } else {
                // Si aún no llegamos a la ronda final, solo pasamos a la siguiente
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